package com.atguigu.gmall.index.config;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.pmsinterface.entity.CategoryEntity;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class GmallCacheAspect {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Around("@annotation(com.atguigu.gmall.index.config.GmallCache)")
    public Object around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable{
        Object[] args = proceedingJoinPoint.getArgs();
        MethodSignature signature = (MethodSignature)proceedingJoinPoint.getSignature();
        Method method = signature.getMethod();
        Class returnType = method.getReturnType();

        GmallCache gmallCache = method.getAnnotation(GmallCache.class);


        String key = gmallCache.prefix() + Arrays.asList(args).get(0);
        String json = redisTemplate.opsForValue().get(key);
        if(StringUtils.isNotBlank(json)){
            return JSON.parseArray(json, returnType);
        }

        String lock = gmallCache.lock() + ":" + args;
        redissonClient.getFairLock(lock).lock();

        String json2 = redisTemplate.opsForValue().get(gmallCache.prefix() + Arrays.asList(args).get(0));
        if(StringUtils.isNotBlank(json2)){
            return JSON.parseArray(json2, returnType);
        }

        Object result = proceedingJoinPoint.proceed(args);

        Integer random = gmallCache.random();
        Integer timeout = gmallCache.timeout();
        Long timeOut = Long.sum(random.longValue(), timeout.longValue());
        redisTemplate.opsForValue().set(key,JSON.toJSONString(result),timeOut, TimeUnit.MINUTES);

        redissonClient.getFairLock(lock).unlock();
        return result;
    }
}
