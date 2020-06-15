package com.atguigu.gmall.order.service.impl;

import com.atguigu.gamll.wmsinterface.vo.SkuLockVO;
import com.atguigu.gmall.cart.entity.Cart;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.oms.exception.OrderException;
import com.atguigu.gmall.oms.vo.OrderSubmitVo;
import com.atguigu.gmall.order.feign.*;
import com.atguigu.gmall.order.interceptor.LoginInterceptor;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.order.vo.OrderConfirmVo;
import com.atguigu.gmall.oms.vo.OrderItemVo;
import com.atguigu.gmall.order.vo.UserInfo;
import com.atguigu.gmall.pmsinterface.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pmsinterface.entity.SkuEntity;
import com.atguigu.gmall.umsinterface.entity.UserAddressEntity;
import com.atguigu.gmall.umsinterface.entity.UserEntity;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
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
    private OmsClient omsClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

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
        redisTemplate.opsForValue().set(PREFIX + orderToken, orderToken,5, TimeUnit.MINUTES);

        CompletableFuture.allOf(userAddressCompletableFuture, orderItemsCompletableFuture, userEntityCompletableFuture).join();
        return orderConfirmVo;
    }

    @Override
    public OrderEntity submit(OrderSubmitVo submitVo) {

        String orderToken = submitVo.getOrderToken();
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

        Boolean execute = redisTemplate.execute(new DefaultRedisScript<>(script, Boolean.class), Arrays.asList(PREFIX + orderToken), orderToken);

        if(!execute){
            throw new OrderException("页面重复提交");
        }

        BigDecimal totalPrice = submitVo.getTotalPrice();
        List<OrderItemVo> items = submitVo.getItems();

        BigDecimal currentPrice = items.stream().map(item -> {
            ResponseVo<SkuEntity> skuEntityResponseVo = pmsClient.querySkuById(item.getSkuId());
            SkuEntity skuEntity = skuEntityResponseVo.getData();
            if (skuEntity != null) {
                return skuEntity.getPrice().multiply(new BigDecimal(item.getCount()));
            }
            return new BigDecimal(0);
        }).reduce(BigDecimal::add).get();

        if(currentPrice.compareTo(totalPrice) != 0){
            throw new OrderException("页面已过期");
        }

        List<SkuLockVO> skuLockVOS = items.stream().map(item -> {
            SkuLockVO skuLockVO = new SkuLockVO();
            if (item != null) {
                skuLockVO.setSkuId(item.getSkuId());
                skuLockVO.setCount(item.getCount());
            }
            return skuLockVO;
        }).collect(Collectors.toList());

        ResponseVo<List<SkuLockVO>> checkAndLockResponseVo = wmsClient.checkAndLock(skuLockVOS, orderToken);
        List<SkuLockVO> skuLockVOList = checkAndLockResponseVo.getData();

        if(!CollectionUtils.isEmpty(skuLockVOList)){
            throw new OrderException("库存锁定失败，锁定状况为: " + skuLockVOList);
        }

//        int i =  1/0;

        UserInfo userInfo = LoginInterceptor.getUserInfo();
        Integer userId = userInfo.getUserId();
        OrderEntity orderEntity = null;
        try {
            submitVo.setUserId(userId.longValue());
            ResponseVo<OrderEntity> responseVo = omsClient.saveOrder(submitVo);

            orderEntity = responseVo.getData();
        } catch (Exception e) {
            e.printStackTrace();

            rabbitTemplate.convertAndSend("ORDER_EXCHANGE", "stock.unlock", orderToken);
            throw new OrderException("订单创建失败");
        }


//        Map<String,Object> map = new HashMap();
//        map.put("userId", userId.intValue());
//        List<Integer> skuIds = items.stream().map(item -> item.getSkuId().intValue()).collect(Collectors.toList());
//        map.put("sku_ids",skuIds);
//        rabbitTemplate.convertAndSend("ORDER_EXCHANGE", "cart.delete", map);

        return orderEntity;
    }
}
