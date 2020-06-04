package com.atguigu.gmall.pms.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.client.naming.utils.CollectionUtils;
import com.atguigu.gmall.pms.mapper.SkuMapper;
import com.atguigu.gmall.pmsinterface.entity.*;
import com.atguigu.gmall.pmsinterface.vo.SaleAttrValueVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.pms.service.SkuAttrValueService;


@Service("skuAttrValueService")
public class SkuAttrValueServiceImpl extends ServiceImpl<SkuAttrValueMapper, SkuAttrValueEntity> implements SkuAttrValueService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SkuAttrValueEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SkuAttrValueEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<SkuAttrValueEntity> querySkuAttrValuesBySkuId(Long skuId) {

        return baseMapper.querySkuAttrValuesBySkuId(skuId);
    }

    @Autowired
    private SkuMapper skuMapper;

    @Override
    public List<SaleAttrValueVo> querySaleAttrValuesBySpuId(Long spuId) {

        List<SkuAttrValueEntity> attrValueEntities = skuMapper.querySaleAttrValuesBySpuId(spuId);
        List<SaleAttrValueVo> saleAttrValueVos = new ArrayList<>();

        Map<Long, List<SkuAttrValueEntity>> maps = attrValueEntities.stream().collect(Collectors.groupingBy(SkuAttrValueEntity::getAttrId));

        maps.forEach((attrId,attrentities) -> {
            if(!CollectionUtils.isEmpty(attrentities)){
                SaleAttrValueVo saleAttrValueVo = new SaleAttrValueVo();

                saleAttrValueVo.setAttrId(attrId);
                saleAttrValueVo.setAttrName(attrentities.get(0).getAttrName());
                saleAttrValueVo.setAttrValues(attrentities.stream().map(SkuAttrValueEntity::getAttrValue).collect(Collectors.toSet()));

                saleAttrValueVos.add(saleAttrValueVo);
            }
        });

        return saleAttrValueVos;
    }

    @Override
    public String querySkuJsonsBySpuId(Long spuId) {

        List<Map<String, Object>> maps = baseMapper.querySkuJsonsBySpuId(spuId);

        Map attrValuesSkuIdMap  = maps.stream().collect(Collectors.toMap(map -> map.get("attr_values"), map -> map.get("sku_id")));

        return JSON.toJSONString(attrValuesSkuIdMap);
    }

}