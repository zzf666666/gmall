package com.atguigu.gmall.cart.listener;

import com.atguigu.gmall.cart.entity.Cart;
import com.atguigu.gmall.cart.feign.PmsClient;
import com.atguigu.gmall.cart.mapper.CartMapper;
import com.atguigu.gmall.cart.service.CartAsynService;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pmsinterface.entity.SkuEntity;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class MyListener {

    @Autowired
    private PmsClient pmsClient;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private CartAsynService cartAsynService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String PRICE_PREFIX = "cart:price:";

    private static final String CART_PREFIX = "cart:info:";

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "CART_ITEM_QUEUE", durable = "true"),
            exchange = @Exchange(value = "GMALL_ITEM_EXCHANGE", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
            key = {"update"}
    ))
    public void listener(Long spuId, Channel channel, Message message) throws IOException {
        ResponseVo<List<SkuEntity>> skuEntitiesResponseVo = pmsClient.querySkyBySpuId(spuId);
        List<SkuEntity> skuEntities = skuEntitiesResponseVo.getData();

        try {
            if(!CollectionUtils.isEmpty(skuEntities)){
                skuEntities.forEach(sku -> {
                    Long skuId = sku.getId();
                    if(redisTemplate.opsForValue().get(PRICE_PREFIX + skuId) != null){
                        BigDecimal price = sku.getPrice();
                        redisTemplate.opsForValue().set(PRICE_PREFIX + skuId, price.toString());
                    }

                });
            }
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            e.printStackTrace();
            if (message.getMessageProperties().getRedelivered()){
                channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
            }
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        }
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue("ORDER_CART_QUEUE"),
            exchange = @Exchange(value = "ORDER_EXCHANGE", ignoreDeclarationExceptions = "ture", type = ExchangeTypes.TOPIC),
            key = {"cart.delete"}
    ))
    public void deleteCart(Map<String,Object> map, Channel channel, Message message) throws IOException {
        Integer userId = (Integer)map.get("userId");
        List<Integer> sku_ids = (List)map.get("sku_ids");

        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(CART_PREFIX + userId.toString());

        try {
            hashOps.delete(sku_ids.stream().map(skuId -> skuId.toString()).collect(Collectors.toList()).toArray());

            cartAsynService.mysqlDeleteCart(userId, sku_ids);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (IOException e) {
            e.printStackTrace();
            if(message.getMessageProperties().getRedelivered()){
                channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
            }else{
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
            }
        }
    }
}
