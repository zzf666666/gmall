package com.atguigu.gmall.cart.service.impl;

import com.atguigu.gamll.wmsinterface.entity.WareSkuEntity;
import com.atguigu.gmall.cart.entity.Cart;
import com.atguigu.gmall.cart.entity.UserInfo;
import com.atguigu.gmall.cart.feign.PmsClient;
import com.atguigu.gmall.cart.feign.SmsClient;
import com.atguigu.gmall.cart.feign.WmsClient;
import com.atguigu.gmall.cart.interceptor.LoginInterceptor;
import com.atguigu.gmall.cart.mapper.CartMapper;
import com.atguigu.gmall.cart.service.CartAsynService;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pmsinterface.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pmsinterface.entity.SkuEntity;
import com.atguigu.gmall.smsinterface.vo.ItemSaleVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String PREFIX = "cart:info:";

    private static final String PRICE_PREFIX = "cart:price:";

    @Autowired
    private PmsClient pmsClient;

    @Autowired
    private SmsClient smsClient;

    @Autowired
    private WmsClient wmsClient;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private CartAsynService cartAsynService;

    @Autowired
    private static final ObjectMapper MAPPER = new ObjectMapper();

//    @Override
    public Cart cart(Integer skuId) {

        try {
            String userId = getUserId();

            BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(PREFIX + userId);
            Object obj = hashOps.get(skuId.toString());
            String cartJson = "";
            if(obj != null){
                cartJson = obj.toString();
            }
            Cart cart = MAPPER.readValue(cartJson, Cart.class);
            return cart;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

//    @Override
    public void addCart(Cart cart) {
        String userId = getUserId();

        String key = PREFIX + userId;

        Long skuId = cart.getSkuId();

        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(key);

        Integer count = cart.getCount();
        try {
            String cartJson = (String)hashOps.get(skuId.toString());
            if(StringUtils.isNotBlank(cartJson)){
                cart = MAPPER.readValue(cartJson, Cart.class);
            }

            if(hashOps.hasKey(skuId.toString())){
                cart.setCount(cart.getCount() + count);
                cartJson = MAPPER.writeValueAsString(cart);
                hashOps.put(skuId.toString(), cartJson);
                cartAsynService.mysqlUpdate(cart,userId);
            }else{
                cart.setCheck(true);
                cart.setUserId(userId.toString());

                ResponseVo<SkuEntity> skuEntityResponseVo = pmsClient.querySkuById(skuId);
                SkuEntity skuEntity = skuEntityResponseVo.getData();
                if(skuEntity != null){
                    cart.setTitle(skuEntity.getTitle());
                    cart.setDefaultImage(skuEntity.getDefaultImage());
                    cart.setPrice(skuEntity.getPrice());
                }

                ResponseVo<List<SkuAttrValueEntity>> attrValueResponseVo = pmsClient.querySaleAttrValuesBySkuId(skuId);
                List<SkuAttrValueEntity> attrValueEntities = attrValueResponseVo.getData();
                cart.setSaleAttrs(MAPPER.writeValueAsString(attrValueEntities));

                ResponseVo<List<ItemSaleVo>> SalesResponseVo = smsClient.querySaleVosBySkuId(skuId);
                List<ItemSaleVo> saleVos = SalesResponseVo.getData();
                cart.setSales(MAPPER.writeValueAsString(saleVos));


                ResponseVo<List<WareSkuEntity>> StoreResponseVo = wmsClient.queryWareBySkuId(skuId);
                List<WareSkuEntity> storeList = StoreResponseVo.getData();

                cart.setStore(storeList.stream().anyMatch(store -> store.getStock() - store.getStockLocked() > 0));

                hashOps.put(skuId.toString(), MAPPER.writeValueAsString(cart));
                cartAsynService.mysqlInsert(cart);

                redisTemplate.opsForValue().set(PRICE_PREFIX + skuId, cart.getPrice().toString());
            }

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

    }

    @Override
    public List<Cart> toCart() {
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        String userKey = userInfo.getUserKey();

        String unlogKey = PREFIX + userKey;

        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(unlogKey);
        List<Object> userKeyCartJsons = hashOps.values();

        List<Cart> userKeyCarts = null;

        if(!CollectionUtils.isEmpty(userKeyCartJsons)){
            userKeyCarts = userKeyCartJsons.stream().map(cartJson -> {
                try {
                    Cart cart = MAPPER.readValue((String) cartJson, Cart.class);
                    String price = redisTemplate.opsForValue().get(PRICE_PREFIX + cart.getSkuId());
                    if(StringUtils.isNotBlank(price)){
                        cart.setCurrentPrice(new BigDecimal(price));
                    }
                    return cart;
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                    return new Cart();
                }
            }).collect(Collectors.toList());
        }
        if(userInfo.getUserId() == null){
            return userKeyCarts;
        }

        String logKey = PREFIX + userInfo.getUserId();

//        redisTemplate.opsForHash().put(logKey, "非空","非空");
        BoundHashOperations<String, Object, Object> loginHashOps = redisTemplate.boundHashOps(logKey);

        if(!CollectionUtils.isEmpty(userKeyCarts)){
            userKeyCarts.forEach(cart -> {
                if(loginHashOps.hasKey(cart.getSkuId().toString())){
                    try {
                        Integer count = cart.getCount();
                        String cartJson = loginHashOps.get(cart.getSkuId().toString()).toString();
                        cart = MAPPER.readValue(cartJson, Cart.class);
                        cart.setCount(cart.getCount() + count);

                        String logCartJson = MAPPER.writeValueAsString(cart);
                        loginHashOps.put(cart.getSkuId().toString(), logCartJson);
                        cartAsynService.mysqlUpdate(cart, cart.getUserId());
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                }else{
                    try {
                        cart.setUserId(userInfo.getUserId().toString());
                        String cartJson = MAPPER.writeValueAsString(cart);

                        loginHashOps.put(cart.getSkuId().toString(), cartJson);
                        cartAsynService.mysqlInsert(cart);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                }

                hashOps.delete(cart.getSkuId().toString());
                cartAsynService.mysqlDelete(cart);
            });
        }

        List<Object> loginCartJsons = loginHashOps.values();
        if(!CollectionUtils.isEmpty(loginCartJsons)){
            return loginCartJsons.stream().map(cartJson -> {
                try {
                    Cart cart = MAPPER.readValue(cartJson.toString(), Cart.class);
                    String price = redisTemplate.opsForValue().get(PRICE_PREFIX);
                    if(StringUtils.isNotBlank(price)){
                        cart.setCurrentPrice(new BigDecimal(price));
                    }
                    return cart;
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                    return null;
                }
            }).collect(Collectors.toList());
        }

        return null;
    }

    @Override
    public void updateNum(Cart cart) {
        String userId = getUserId();

        Long skuId = cart.getSkuId();
        Integer count = cart.getCount();

        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(PREFIX + userId);
        if(hashOps != null){
            try {
                String cartJson = hashOps.get(skuId.toString()).toString();
                cart = MAPPER.readValue(cartJson, Cart.class);
                cart.setCount(count);

                hashOps.put(skuId.toString(), MAPPER.writeValueAsString(cart));
                cartAsynService.mysqlUpdate(cart, userId);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void deleteCart(Long skuId) {
        String userId = getUserId();

        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(PREFIX + userId);
        if(hashOps != null){
            hashOps.delete(skuId.toString());
            cartAsynService.mysqlDeleteBySkuId(userId, skuId);
        }
    }

    @Override
    public void updateStatus(Cart cart) {

        String userId = getUserId();
        Long skuId = cart.getSkuId();
        Boolean check = cart.getCheck();

        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(PREFIX + userId);
        if(hashOps != null){
            try {
                String cartJson = hashOps.get(skuId.toString()).toString();
                cart = MAPPER.readValue(cartJson, Cart.class);
                cart.setCheck(check);

                hashOps.put(skuId.toString(), MAPPER.writeValueAsString(cart));
                cartAsynService.mysqlUpdate(cart,userId);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public List<Cart> queryCheckedCartsByUserId(Integer userId) {

        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(PREFIX + userId);
        return hashOps.values().stream().map(cartJson -> {
            try {
                return MAPPER.readValue(cartJson.toString(), Cart.class);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                return null;
            }
        }).filter(cart -> cart.getCheck()).collect(Collectors.toList());
    }

    private String getUserId() {
        UserInfo userInfo = LoginInterceptor.getUserInfo();

        String userId = null;
        if(userInfo.getUserId() != null){
            userId = userInfo.getUserId().toString();
        }

        if(userId == null){
            userId = userInfo.getUserKey();
        }
        return userId;
    }

//    @Scheduled(fixedRate = 10000)
//    @ResponseBody
//    public void test(){
//        System.out.println(System.currentTimeMillis());
//    }


//    @Async
//    public String test1(){
//        try {
//            System.out.println("test1开始执行");
//            TimeUnit.SECONDS.sleep(4);
//            int i = 1/0;
//            System.out.println("test1结束执行");
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        return "test1方法";
//    }
//
//    @Async
//    public ListenableFuture<String> test2(){
//        try {
//            System.out.println("test2开始执行");
//            TimeUnit.SECONDS.sleep(5);
//            System.out.println("test2结束执行");
//            int i = 1/0;
//            return AsyncResult.forValue("test2方法");
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//            return AsyncResult.forValue("test2方法出现异常了");
//        }
//
//    }
}
