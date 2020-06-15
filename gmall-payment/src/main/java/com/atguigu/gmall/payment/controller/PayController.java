package com.atguigu.gmall.payment.controller;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.oms.exception.OrderException;
import com.atguigu.gmall.payment.entity.PayEntity;
import com.atguigu.gmall.payment.service.PaymentService;
import com.atguigu.gmall.payment.utils.AlipayTemplate;
import com.atguigu.gmall.payment.vo.PayAsyncVo;
import com.atguigu.gmall.payment.vo.PayVo;
import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

@Controller
public class PayController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private AlipayTemplate alipayTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @GetMapping("pay.html")
    public String pay(String orderToken, Map<String,OrderEntity> map){

        OrderEntity orderEntity = paymentService.queryOrder(orderToken);
        map.put("orderEntity", orderEntity);

        return "pay";
    }

    @GetMapping("alipay.html")
    @ResponseBody
    public String alipay(String orderToken){

        OrderEntity orderEntity = paymentService.queryOrder(orderToken);

        if(orderEntity.getStatus() != 0){
            throw new OrderException("该订单不是待支付状态");
        }

        String form = null;
        try {
            Long payId = paymentService.savePayment(orderEntity);

            PayVo payVo = new PayVo();

            payVo.setOut_trade_no(orderToken);
            payVo.setTotal_amount("0.01");
            payVo.setSubject("欢迎RMB玩家来到变强页面-.-");
            payVo.setPassback_params(payId.toString());
            form = alipayTemplate.pay(payVo);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return form;
    }

//    @GetMapping("/pay/{id}")
//    public void tt(@PathVariable("id") Long id){
//        PayEntity payEntity = paymentService.queryPaymentByPayId(id.toString());
//        payEntity.setPaymentStatus(10);
//        paymentService.updatePaystatus(payEntity);
//    }

    @GetMapping("pay/return")
    public String returnA(PayAsyncVo payAsyncVo, Map<String,String> map){

        map.put("total_amount", payAsyncVo.getTotal_amount());

        return "paysuccess";
    }

    @PostMapping("pay/async")
    @ResponseBody
    public String async(PayAsyncVo payAsyncVo){

        System.out.println(payAsyncVo);

        Boolean flag = alipayTemplate.checkSignature(payAsyncVo);

        if(!flag){
            return "failure";
        }

        String payId = payAsyncVo.getPassback_params();
        PayEntity payEntity = paymentService.queryPaymentByPayId(payId);

        if(payEntity == null
        || !StringUtils.equals(payAsyncVo.getOut_trade_no(), payEntity.getOutTradeNo())
        || payEntity.getTotalAmount().compareTo(new BigDecimal(payAsyncVo.getBuyer_pay_amount())) != 0){
            return "failure";
        }

        if(!StringUtils.equals("TRADE_SUCCESS", payAsyncVo.getTrade_status())){
            return "failure";
        }

        payEntity.setCallbackTime(new Date());
        payEntity.setCallbackContent(JSON.toJSONString(payAsyncVo));
        payEntity.setPaymentStatus(1);
        payEntity.setTradeNo(payAsyncVo.getTrade_no());

        System.out.println(payEntity);

        paymentService.updatePaystatus(payEntity);

        rabbitTemplate.convertAndSend("ORDER_EXCHANGE", "order.pay", payAsyncVo.getOut_trade_no());

        return "success";
    }
}
