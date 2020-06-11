package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.entity.Cart;
import com.atguigu.gmall.cart.entity.UserInfo;
import com.atguigu.gmall.cart.interceptor.LoginInterceptor;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.cart.service.impl.CartServiceImpl;
import com.atguigu.gmall.common.bean.ResponseVo;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

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

    @GetMapping("addCart")
    public String addCart(Cart cart){

        cartService.addCart(cart);

        return "redirect:http://cart.gmall.com/cart?skuId=" + cart.getSkuId();
    }

    @GetMapping("cart")
    public String cart(Integer skuId, Map<String,Cart> map){
        Cart cart = cartService.cart(skuId);

        map.put("cart", cart);

        return "addCart";
    }

    @GetMapping("cart.html")
    public String toCart(Map<String,List<Cart>> map){

        List<Cart> carts = cartService.toCart();

        map.put("carts", carts);

        return "cart";
    }

    @PostMapping("/updateNum")
    @ResponseBody
    public ResponseVo<Object> updateNum(@RequestBody Cart cart){

        cartService.updateNum(cart);

        return ResponseVo.ok();
    }

    @PostMapping("/deleteCart")
    @ResponseBody
    public ResponseVo<Object> deleteCart(Long skuId){
        cartService.deleteCart(skuId);
        return ResponseVo.ok();
    }

    @PostMapping("/updateStatus")
    @ResponseBody
    public ResponseVo<Object> updateStatus(@RequestBody Cart cart){
        cartService.updateStatus(cart);
        return ResponseVo.ok();
    }

    @GetMapping("query/{userId}")
    @ResponseBody
    public ResponseVo<List<Cart>> queryCheckedCartsByUserId(@PathVariable("userId")Integer userId){
        List<Cart> carts = cartService.queryCheckedCartsByUserId(userId);
        return ResponseVo.ok(carts);
    }

//    @GetMapping("/test")
//    @ResponseBody
//    public String test() throws ExecutionException, InterruptedException {
//
//        long start = System.currentTimeMillis();
//        String test1 = cartService.test1();
//        ListenableFuture<String> test2 = cartService.test2();
//        System.out.println(test1);
//        test2.addCallback(str -> System.out.println(str), e -> System.out.println(e));
////        System.out.println(test1.get() + "\t" + test2.get());
//        long end = System.currentTimeMillis();
//        System.out.println(end - start);
//        return "test";
//    }

//    @GetMapping("test")
//    public void test(){
//        cartService.test();
//    }

}
