package com.atguigu.gmall.oms.exception;

public class OrderException extends RuntimeException{

    public OrderException() {
    }

    public OrderException(String message) {
        super(message);
    }
}
