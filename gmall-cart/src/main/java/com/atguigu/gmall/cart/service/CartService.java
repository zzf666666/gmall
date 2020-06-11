package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.cart.entity.Cart;

import java.util.List;

public interface CartService {
    Cart cart(Integer skuId);

    void addCart(Cart cart);

    List<Cart> toCart();

    void updateNum(Cart cart);

    void deleteCart(Long skuId);

    void updateStatus(Cart cart);

    List<Cart> queryCheckedCartsByUserId(Integer userId);

//    String test1();
//
//    String test2();

//    public void test();
}
