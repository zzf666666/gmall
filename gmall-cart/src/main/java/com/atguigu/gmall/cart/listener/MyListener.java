package com.atguigu.gmall.cart.listener;

import com.atguigu.gmall.cart.feign.PmsClient;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pmsinterface.entity.SkuEntity;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@Component
public class MyListener {

    @Autowired
    private PmsClient pmsClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String PRICE_PREFIX = "cart:price:";


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
}
