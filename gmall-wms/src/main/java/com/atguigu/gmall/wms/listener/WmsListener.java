package com.atguigu.gmall.wms.listener;

import com.alibaba.fastjson.JSON;
import com.atguigu.gamll.wmsinterface.vo.SkuLockVO;
import com.atguigu.gmall.wms.mapper.WareSkuMapper;
import com.rabbitmq.client.Channel;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.cms.bc.BcKEKRecipientInfoGenerator;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class WmsListener {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private WareSkuMapper wareSkuMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final String PAY_PREFIX = "pay:stock:";

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "ORDER_STOCK_QUEUE", durable = "true"),
            exchange = @Exchange(value = "ORDER_EXCHANGE", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
            key = {"stock.unlock"}
    ))
    public void unLock(String orderToken, Channel channel, Message message) throws IOException {
        String itemJson = redisTemplate.opsForValue().get(PAY_PREFIX + orderToken);

        try {
            if(StringUtils.isNotBlank(itemJson)){
                List<SkuLockVO> skuLockVOS = JSON.parseArray(itemJson, SkuLockVO.class);
                skuLockVOS.forEach(item -> {
                    wareSkuMapper.unLockWare(item.getWareId(),item.getSkuId(),item.getCount());
                });
                redisTemplate.delete(PAY_PREFIX + orderToken);
            }

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

    @RabbitListener(queues = "STOCK_DEAD_QUEUE")
    public void dsunLock(String orderTocken, Channel channel, Message message) throws IOException {

        try {
            rabbitTemplate.convertAndSend("ORDER_EXCHANGE", "stock.unlock", orderTocken);

            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }  catch (IOException e) {
        e.printStackTrace();
        if(message.getMessageProperties().getRedelivered()){
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
        }else{
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        }
      }

    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "STOCK_MINUS_QUEUE", durable = "true"),
            exchange = @Exchange(value = "ORDER_EXCHANGE", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
            key = {"store.minus"}
    ))
    public void storeMinus(String orderToken, Channel channel, Message message) throws IOException {
        String itemJson = redisTemplate.opsForValue().get(PAY_PREFIX + orderToken);

        try {
            if(StringUtils.isNotBlank(itemJson)){
                List<SkuLockVO> skuLockVOS = JSON.parseArray(itemJson, SkuLockVO.class);
                skuLockVOS.forEach(item -> {
                    wareSkuMapper.minus(item.getWareId(),item.getSkuId(),item.getCount());
                });
                redisTemplate.delete(PAY_PREFIX + orderToken);
            }

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
