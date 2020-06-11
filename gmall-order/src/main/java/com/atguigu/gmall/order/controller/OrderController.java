package com.atguigu.gmall.order.controller;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.order.vo.OrderConfirmVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/confirm")
    public String orderConfirm(Map<String,OrderConfirmVo> map){
        OrderConfirmVo orderConfirmVo = orderService.orderConfirm();

        map.put("confirmVo", orderConfirmVo);

        return "trade";
    }
}
