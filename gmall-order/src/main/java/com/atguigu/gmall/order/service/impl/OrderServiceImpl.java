package com.atguigu.gmall.order.service.impl;

import com.atguigu.gamll.wmsinterface.entity.WareSkuEntity;
import com.atguigu.gmall.cart.entity.Cart;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.order.exception.OrderException;
import com.atguigu.gmall.order.feign.*;
import com.atguigu.gmall.order.interceptor.LoginInterceptor;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.order.vo.OrderConfirmVo;
import com.atguigu.gmall.order.vo.OrderItemVo;
import com.atguigu.gmall.order.vo.UserInfo;
import com.atguigu.gmall.pmsinterface.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pmsinterface.entity.SkuEntity;
import com.atguigu.gmall.smsinterface.vo.ItemSaleVo;
import com.atguigu.gmall.umsinterface.entity.UserAddressEntity;
import com.atguigu.gmall.umsinterface.entity.UserEntity;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.thymeleaf.standard.expression.OrExpression;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private PmsClient pmsClient;

    @Autowired
    private SmsClient smsClient;

    @Autowired
    private WmsClient wmsClient;

    @Autowired
    private UmsClient umsClient;

    @Autowired
    private CartClient cartClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String PREFIX = "order:token:";

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @Override
    public OrderConfirmVo orderConfirm() {

        UserInfo userInfo = LoginInterceptor.getUserInfo();
        Integer userId = userInfo.getUserId();

        OrderConfirmVo orderConfirmVo = new OrderConfirmVo();

        CompletableFuture<Void> userAddressCompletableFuture = CompletableFuture.runAsync(() -> {
            ResponseVo<List<UserAddressEntity>> addressResponseVo = umsClient.queryByUserId(userId);
            List<UserAddressEntity> addressEntities = addressResponseVo.getData();
            orderConfirmVo.setAddresses(addressEntities);
        },threadPoolExecutor);

        CompletableFuture<List<Cart>> cartsCompletableFuture = CompletableFuture.supplyAsync(() -> {
            ResponseVo<List<Cart>> cartsResponseVo = cartClient.queryCheckedCartsByUserId(userId);
            List<Cart> carts = cartsResponseVo.getData();
            if (CollectionUtils.isEmpty(carts)) {
                throw new OrderException("您没有对应的商品");
            }
            return carts;
        },threadPoolExecutor);


        CompletableFuture<Void> orderItemsCompletableFuture = cartsCompletableFuture.thenAcceptAsync(carts -> {
            List<OrderItemVo> itemVos = carts.stream().map(cart -> {
                OrderItemVo orderItemVo = new OrderItemVo();
                orderItemVo.setSkuId(cart.getSkuId());
                orderItemVo.setCount(cart.getCount());

                CompletableFuture<Void> skuEntityCompletableFuture = CompletableFuture.runAsync(() -> {
                    ResponseVo<SkuEntity> skuEntityResponseVo = pmsClient.querySkuById(cart.getSkuId());
                    SkuEntity skuEntity = skuEntityResponseVo.getData();

                    if (skuEntity != null) {
                        orderItemVo.setTitle(skuEntity.getTitle());
                        orderItemVo.setPrice(skuEntity.getPrice());
                        orderItemVo.setDefaultImage(skuEntity.getDefaultImage());
                        orderItemVo.setWeight(skuEntity.getWeight());
                    }
                },threadPoolExecutor);

                CompletableFuture<Void> skuAttrValueCompletableFuture = CompletableFuture.runAsync(() -> {
                    ResponseVo<List<SkuAttrValueEntity>> skuAttrValueResponseVo = pmsClient.querySkuAttrValuesBySkuId(cart.getSkuId());
                    List<SkuAttrValueEntity> skuAttrValueEntities = skuAttrValueResponseVo.getData();
                    orderItemVo.setSaleAttrs(skuAttrValueEntities);
                },threadPoolExecutor);

//                CompletableFuture<Void> itemSaleCompletableFuture = CompletableFuture.runAsync(() -> {
//                    ResponseVo<List<ItemSaleVo>> itemSaleResponseVo = smsClient.querySaleVosBySkuId(cart.getSkuId());
//                    List<ItemSaleVo> sales = itemSaleResponseVo.getData();
//                    orderItemVo.setSales(sales);
//                },threadPoolExecutor);
//
//                CompletableFuture<Void> wareCompletableFuture = CompletableFuture.runAsync(() -> {
//                    ResponseVo<List<WareSkuEntity>> wareResponseVo = wmsClient.queryWareBySkuId(cart.getSkuId());
//                    List<WareSkuEntity> wareSkuEntities = wareResponseVo.getData();
//
//                    boolean store = wareSkuEntities.stream().anyMatch(ware -> ware.getStock() - ware.getStockLocked() > 0);
//                    orderItemVo.setStore(store);
//                },threadPoolExecutor);

                CompletableFuture.allOf(skuEntityCompletableFuture, skuAttrValueCompletableFuture/*,itemSaleCompletableFuture,itemSaleCompletableFuture*/);

                return orderItemVo;
            }).collect(Collectors.toList());

            orderConfirmVo.setOrderItems(itemVos);
        },threadPoolExecutor);


        CompletableFuture<Void> userEntityCompletableFuture = CompletableFuture.runAsync(() -> {
            ResponseVo<UserEntity> userEntityResponseVo = umsClient.queryUserById(userId.longValue());
            UserEntity userEntity = userEntityResponseVo.getData();
            if (userEntity != null) {
                orderConfirmVo.setBounds(userEntity.getIntegration());
            }
        },threadPoolExecutor);

        String orderToken = IdWorker.getTimeId();
        orderConfirmVo.setOrderToken(orderToken);
        redisTemplate.opsForValue().set(PREFIX + orderToken, orderToken);

        CompletableFuture.allOf(userAddressCompletableFuture, orderItemsCompletableFuture, userEntityCompletableFuture);
        return orderConfirmVo;
    }
}
