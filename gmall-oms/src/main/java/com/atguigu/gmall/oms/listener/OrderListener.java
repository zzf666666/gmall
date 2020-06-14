package com.atguigu.gmall.oms.listener;

import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.oms.mapper.OrderMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OrderListener {

    @Autowired
    private OrderMapper orderMapper;

    @RabbitListener(queues = "ORDER_DEAD_QUEUE")
    public void downOrder(String orderToken, Channel channel, Message message) throws IOException {
        OrderEntity orderEntity = orderMapper.selectOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderToken).eq("status", 0));

        try {
            if(orderEntity != null){
                orderEntity.setStatus(4);
                orderMapper.updateById(orderEntity);
            }

            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (IOException e) {
            e.printStackTrace();
            if(message.getMessageProperties().getRedelivered()){
                channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
            }
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false,true);
        }
    }
}
