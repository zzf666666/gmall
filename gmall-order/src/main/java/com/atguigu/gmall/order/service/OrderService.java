package com.atguigu.gmall.order.service;

import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.oms.vo.OrderSubmitVo;
import com.atguigu.gmall.order.vo.OrderConfirmVo;

public interface OrderService {
    OrderConfirmVo orderConfirm();

    OrderEntity submit(OrderSubmitVo submitVo);
}
