package com.atguigu.gmall.wms.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Configuration
@Slf4j
public class RabbitMqConfig implements RabbitTemplate.ConfirmCallback,RabbitTemplate.ReturnCallback {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostConstruct
    public void init(){
        rabbitTemplate.setConfirmCallback(this);
        rabbitTemplate.setReturnCallback(this);
    }

    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        if(ack){
            log.info("消息到达了交换机");
        }else{
            log.error("消息没有到达交换机-。-");
        }
    }

    @Override
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
        log.error("消息没有到达队列, 消息是{}, 交换机是{}, routingKey是{}",message,exchange,routingKey );
    }

    @Bean
    public Queue ttlQueue(){
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-message-ttl", 120000);
        arguments.put("x-dead-letter-exchange", "ORDER_EXCHANGE");
        arguments.put("x-dead-letter-routing-key", "stock.dead");
        return new Queue("STOCK_TTL_QUEUE",true,false,false,arguments);
    }

    @Bean
    public Binding ttlBinding(){
        return new Binding("STOCK_TTL_QUEUE", Binding.DestinationType.QUEUE, "ORDER_EXCHANGE", "stock.ttl",null);
    }

    @Bean
    public Queue deadQueue(){
        return new Queue("STOCK_DEAD_QUEUE", true,false,false);
    }

    @Bean
    public Binding deadBinding(){
        return new Binding("STOCK_DEAD_QUEUE", Binding.DestinationType.QUEUE, "ORDER_EXCHANGE", "stock.dead", null);
    }
}
