package com.atguigu.gmall.index.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.feign.PmsClient;
import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.pmsinterface.entity.CategoryEntity;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IndexServiceImpl implements IndexService {

    @Autowired
    private PmsClient pmsClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "index:cats:";

    @Override
    public List<CategoryEntity> queryLvl1Categories() {
        ResponseVo<List<CategoryEntity>> listResponseVo = pmsClient.queryCategory(0l);
        List<CategoryEntity> data = listResponseVo.getData();
        return data;
    }

    @Override
    public List<CategoryEntity> queryLvl2CategoriesWithSubs(Long pid) {
        String json = redisTemplate.opsForValue().get(KEY_PREFIX + pid);
        if(StringUtils.isNotBlank(json)){
            return JSON.parseArray(json,CategoryEntity.class);
        }
        ResponseVo<List<CategoryEntity>> listResponseVo = pmsClient.queryCategoriesWithSubByPid(pid);
        List<CategoryEntity> data = listResponseVo.getData();

        redisTemplate.opsForValue().set(KEY_PREFIX + pid,JSON.toJSONString(data));
        return data;
    }
}
