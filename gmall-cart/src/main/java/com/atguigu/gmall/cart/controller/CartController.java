package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.entity.UserInfo;
import com.atguigu.gmall.cart.interceptor.LoginInterceptor;
import com.atguigu.gmall.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping
    @ResponseBody
    public String cart(HttpServletRequest request){

        System.out.println("目标方法被调用了");
//        Integer userId = (Integer)request.getAttribute("userId");
//        String userKey = request.getAttribute("userKey").toString();

//        System.out.println(LoginInterceptor.USER_INFO);

//        System.out.println(userId);
//        System.out.println(userKey);

        UserInfo userInfo = LoginInterceptor.getUserInfo();
        System.out.println(userInfo);

        return "cart";
    }
}
