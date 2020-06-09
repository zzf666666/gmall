package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.cart.entity.Cart;

public interface CartService {
    Cart cart(Integer skuId);

    void addCart(Cart cart);
}
