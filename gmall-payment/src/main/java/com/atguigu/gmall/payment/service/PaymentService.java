package com.atguigu.gmall.payment.service;

import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.payment.entity.PayEntity;

public interface PaymentService {
    OrderEntity queryOrder(String orderToken);

    Long savePayment(OrderEntity orderEntity);

    PayEntity queryPaymentByPayId(String payId);

    void updatePaystatus(PayEntity payEntity);
}
