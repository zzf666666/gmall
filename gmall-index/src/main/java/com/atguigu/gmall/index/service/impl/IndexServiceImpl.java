package com.atguigu.gmall.index.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.common.util.UuidUtils;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.config.GmallCache;
import com.atguigu.gmall.index.feign.PmsClient;
import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.pmsinterface.entity.CategoryEntity;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class IndexServiceImpl implements IndexService {

    @Autowired
    private PmsClient pmsClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "index:cats:";

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public List<CategoryEntity> queryLvl1Categories() {
        ResponseVo<List<CategoryEntity>> listResponseVo = pmsClient.queryCategory(0l);
        List<CategoryEntity> data = listResponseVo.getData();
        return data;
    }

    @GmallCache(prefix = KEY_PREFIX,lock = "lock",timeout = 60,random = 10080)
    @Override
    public List<CategoryEntity> queryLvl2CategoriesWithSubs(Long pid) {


        ResponseVo<List<CategoryEntity>> listResponseVo = pmsClient.queryCategoriesWithSubByPid(pid);
        List<CategoryEntity> data = listResponseVo.getData();

        return data;
    }

//    @Override
//    public List<CategoryEntity> queryLvl2CategoriesWithSubs(Long pid) {
//        String json = redisTemplate.opsForValue().get(KEY_PREFIX + pid);
//        if(StringUtils.isNotBlank(json)){
//            return JSON.parseArray(json,CategoryEntity.class);
//        }
//
//        RLock lock = redissonClient.getLock("lock");
//        lock.lock();
//
//        String json2 = redisTemplate.opsForValue().get(KEY_PREFIX + pid);
//        if(StringUtils.isNotBlank(json2)){
//            return JSON.parseArray(json2,CategoryEntity.class);
//        }
//
//        ResponseVo<List<CategoryEntity>> listResponseVo = pmsClient.queryCategoriesWithSubByPid(pid);
//        List<CategoryEntity> data = listResponseVo.getData();
//
//        redisTemplate.opsForValue().set(KEY_PREFIX + pid,JSON.toJSONString(data),3 * 30 + new Random().nextInt(7), TimeUnit.DAYS);
//        lock.unlock();
//        return data;
//    }

//    实现分布式锁要满足以下条件:
//        1、排它
//        2、防死锁
//        3、原子性，加过期时间和释放锁要保证原子性
//        4、只能删自己的锁
    //redis分布式锁的两个难点:
//    1、过期时间如何确定
//    2、集群状态下,如果保证同步(当拿到锁，向redis主中写入锁key，还没有来得及同步给从，主挂掉，从上位为主，此时这个主并没有这个锁key，可以再次获取)
    @Override
    public void test(){
        String uuid = UUID.randomUUID().toString();
        //避免死锁,当获取锁还没来得及执行业务代码删除锁,服务器宕机,导致锁没有删除,给锁一个过期时间,即使服务器宕机没有删除锁,过一段时间后锁自动删除,避免了死锁问题
        Boolean aBoolean = redisTemplate.opsForValue().setIfAbsent("lock", uuid,5,TimeUnit.SECONDS);
        //当拿到锁执行业务代码
        if(aBoolean){
            String numString = redisTemplate.opsForValue().get("num");
            if(StringUtils.isBlank(numString)){
                return;
            }

            Integer num = Integer.parseInt(numString);
            num++;

            redisTemplate.opsForValue().set("num", String.valueOf(num));

            //解铃还需系铃人，只能删除自己的锁(如果业务代码在过期时间内没有执行完成,会导致其他人进来,你一执行完,就会删除锁,由于锁名都是lock,所以会删除它的锁,导致又有其他人进来)
              //为了保证原子性,不能使用if判断再删除,因为判断时成立,进去后有可能又不满足了,比如,凭判断时成立,进去if后,key过期,其他人拿到锁,然后删除就会删除其他人的锁
//            if(redisTemplate.opsForValue().get("lock").equals(uuid)){
//                redisTemplate.delete("lock");
//            }
            //为了保证原子性，使用lua脚本来实现，redis内置lua解释器
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] " +
                    "then return redis.call('del', KEYS[1]) " +
                    "else return 0 end";

            redisTemplate.execute(new DefaultRedisScript<>(script),Arrays.asList("lock"), uuid);
        }else{
            try {
                //没有拿到锁,过一段时间后重试
                Thread.sleep(200);
                test();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
