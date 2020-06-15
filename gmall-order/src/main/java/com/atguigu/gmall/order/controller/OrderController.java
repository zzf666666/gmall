package com.atguigu.gmall.order.controller;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.oms.exception.OrderException;
import com.atguigu.gmall.oms.vo.OrderSubmitVo;
import com.atguigu.gmall.order.interceptor.LoginInterceptor;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.order.vo.OrderConfirmVo;
import com.atguigu.gmall.order.vo.UserInfo;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RLock;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @GetMapping("/confirm")
    public String orderConfirm(Map<String,OrderConfirmVo> map){
        OrderConfirmVo orderConfirmVo = orderService.orderConfirm();

        map.put("confirmVo", orderConfirmVo);

        return "trade";
    }

    @PostMapping("submit")
    @ResponseBody
    public ResponseVo<Object> submit(@RequestBody OrderSubmitVo submitVo){
       OrderEntity orderEntity = orderService.submit(submitVo);
       return ResponseVo.ok(orderEntity.getOrderSn());
    }

    @GetMapping("sec/kill/{skuId}")
    public ResponseVo<Object> seckill(@PathVariable("skuId")Long skuId){

        UserInfo userInfo = LoginInterceptor.getUserInfo();
        Integer userId = userInfo.getUserId();

        String countString = redisTemplate.opsForValue().get("sec:kill:" + skuId);
        if(StringUtils.isBlank(countString)){
            throw new OrderException("没有对应的商品信息");
        }

        Integer count = Integer.parseInt(countString);

        RSemaphore semaphore = redissonClient.getSemaphore("semaphore:" + skuId);
        semaphore.trySetPermits(count);

        RLock lock = redissonClient.getFairLock("lock" + skuId);
        if(count == 0){
            throw new OrderException("商品已经秒杀完毕");
        }

        redisTemplate.opsForValue().set("sec:kill:" + skuId, String.valueOf(--count));

        Map<String,Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("skuId", skuId);
        map.put("count", 1);

        RCountDownLatch countDownLatch = redissonClient.getCountDownLatch("countDownLatch" + userId);
        countDownLatch.trySetCount(1);
        rabbitTemplate.convertAndSend("ORDER_EXCHANGE", "sec.kill", map);

        lock.unlock();

        return ResponseVo.ok();
    }

    @GetMapping("sec/kill/success")
    public ResponseVo<Object> seckillSuccess() throws InterruptedException {
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        Integer userId = userInfo.getUserId();

        //根据用户id查询秒杀订单，因为创建订单是异步的，可能会查不到，所以通过闭锁解决
        RCountDownLatch countDownLatch = redissonClient.getCountDownLatch("countDownLatch" + userId);
        countDownLatch.await();

        //查询秒杀订单

        return ResponseVo.ok();
    }
}
