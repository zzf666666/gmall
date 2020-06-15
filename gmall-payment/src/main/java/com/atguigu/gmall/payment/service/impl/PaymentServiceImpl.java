package com.atguigu.gmall.payment.service.impl;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.payment.entity.PayEntity;
import com.atguigu.gmall.payment.feign.OmsClient;
import com.atguigu.gmall.payment.mapper.PayMapper;
import com.atguigu.gmall.payment.service.PaymentService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private OmsClient omsClient;

    @Autowired
    private PayMapper payMapper;

    @Override
    public OrderEntity queryOrder(String orderToken) {
        ResponseVo<OrderEntity> orderEntityResponseVo = omsClient.queryBySn(orderToken);
        OrderEntity orderEntity = orderEntityResponseVo.getData();

        return orderEntity;
    }

    @Override
    public Long savePayment(OrderEntity orderEntity) {
        PayEntity payEntity = payMapper.selectOne(new QueryWrapper<PayEntity>().eq("out_trade_no", orderEntity.getOrderSn()));

        if(payEntity != null){
            return payEntity.getId();
        }

        payEntity = new PayEntity();

        payEntity.setOutTradeNo(orderEntity.getOrderSn());
        payEntity.setPaymentType(orderEntity.getPayType());
        payEntity.setTotalAmount(new BigDecimal("0.01"));
        payEntity.setSubject("谷粒支付");
        payEntity.setPaymentStatus(0);
        payEntity.setCreateTime(new Date());

        payMapper.insert(payEntity);

        return payEntity.getId();
    }

    @Override
    public PayEntity queryPaymentByPayId(String payId) {
        return payMapper.selectById(payId);
    }

    @Override
    public void updatePaystatus(PayEntity payEntity) {
        payMapper.updateById(payEntity);
    }
}
