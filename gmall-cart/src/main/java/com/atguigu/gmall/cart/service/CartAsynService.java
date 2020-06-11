package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.cart.entity.Cart;
import com.atguigu.gmall.cart.mapper.CartMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class CartAsynService {

    @Autowired
    private CartMapper cartMapper;

    @Async
    public void mysqlUpdate(Cart cart, String userId){
        cartMapper.update(cart, new QueryWrapper<Cart>().eq("user_id", userId).eq("sku_id", cart.getSkuId()));
    }

    @Async
    public void mysqlInsert(Cart cart){
        cartMapper.insert(cart);
    }

    @Async
    public void mysqlDelete(Cart cart) {
        cartMapper.delete(new QueryWrapper<Cart>().eq("user_id", cart.getUserId()).eq("sku_id", cart.getSkuId()));
    }

    @Async
    public void mysqlDeleteBySkuId(String userId, Long skuId) {
        cartMapper.delete(new QueryWrapper<Cart>().eq("user_id", userId).eq("sku_id", skuId));
    }
}
