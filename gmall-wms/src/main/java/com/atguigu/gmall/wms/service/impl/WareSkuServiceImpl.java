package com.atguigu.gmall.wms.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gamll.wmsinterface.vo.SkuLockVO;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.wms.mapper.WareSkuMapper;
import com.atguigu.gamll.wmsinterface.entity.WareSkuEntity;
import com.atguigu.gmall.wms.service.WareSkuService;
import org.springframework.util.CollectionUtils;

import java.util.List;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuMapper, WareSkuEntity> implements WareSkuService {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String PAY_PREFIX = "pay:stock:";

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<WareSkuEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<WareSkuEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<SkuLockVO> lockWare(List<SkuLockVO> orderItems, String orderToken) {

        if (CollectionUtils.isEmpty(orderItems)){
            return null;
        }

        checkLock(orderItems);

        boolean flag = orderItems.stream().allMatch(SkuLockVO::getLock);

        if(!flag){
            orderItems.stream().filter(SkuLockVO::getLock).forEach(item -> {
                baseMapper.unLockWare(item.getWareId(),item.getSkuId(),item.getCount());
            });
            return orderItems;
        }

        redisTemplate.opsForValue().set(PAY_PREFIX + orderToken, JSON.toJSONString(orderItems));
        return null;
    }

    private void checkLock(List<SkuLockVO> orderItems) {
        orderItems.forEach(item -> {
            RLock lock = redissonClient.getFairLock("lock" + item.getSkuId());
            lock.lock();

            List<WareSkuEntity> wareSkuEntities = baseMapper.selectWare(item.getSkuId(), item.getCount());
            if(CollectionUtils.isEmpty(wareSkuEntities)){
                item.setLock(false);
                lock.unlock();
                return;
            }

            WareSkuEntity wareSkuEntity = wareSkuEntities.get(0);
            item.setWareId(wareSkuEntity.getWareId().intValue());
            Integer total = baseMapper.lockWare(wareSkuEntity.getWareId().intValue(), item.getSkuId(), item.getCount());

            if(total == 1){
                item.setLock(true);
            }else{
                item.setLock(false);
            }
            lock.unlock();
        });
    }

}