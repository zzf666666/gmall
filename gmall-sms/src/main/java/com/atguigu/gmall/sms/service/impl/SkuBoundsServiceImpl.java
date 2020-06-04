package com.atguigu.gmall.sms.service.impl;

import com.atguigu.gmall.sms.entity.SkuFullReductionEntity;
import com.atguigu.gmall.sms.entity.SkuLadderEntity;
import com.atguigu.gmall.sms.mapper.SkuFullReductionMapper;
import com.atguigu.gmall.sms.mapper.SkuLadderMapper;
import com.atguigu.gmall.smsinterface.vo.ItemSaleVo;
import com.atguigu.gmall.smsinterface.vo.SkuSaleVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.sms.mapper.SkuBoundsMapper;
import com.atguigu.gmall.sms.entity.SkuBoundsEntity;
import com.atguigu.gmall.sms.service.SkuBoundsService;
import org.springframework.transaction.annotation.Transactional;


@Service("skuBoundsService")
public class SkuBoundsServiceImpl extends ServiceImpl<SkuBoundsMapper, SkuBoundsEntity> implements SkuBoundsService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SkuBoundsEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SkuBoundsEntity>()
        );

        return new PageResultVo(page);
    }

    @Autowired
    private SkuFullReductionMapper skuFullReductionMapper;

    @Autowired
    private SkuLadderMapper skuLadderMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveSkuSales(SkuSaleVo skuSaleVo) {
        SkuBoundsEntity boundsEntity = new SkuBoundsEntity();
        BeanUtils.copyProperties(skuSaleVo, boundsEntity);
        List<Integer> work = skuSaleVo.getWork();
        boundsEntity.setWork(work.get(0) * 1 + work.get(1) * 2 +work.get(2) * 4 + work.get(3) * 8);
        baseMapper.insert(boundsEntity);

        SkuFullReductionEntity fullReductionEntity = new SkuFullReductionEntity();
        BeanUtils.copyProperties(skuSaleVo, fullReductionEntity);
        skuFullReductionMapper.insert(fullReductionEntity);

        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        BeanUtils.copyProperties(skuSaleVo, skuLadderEntity);
        skuLadderMapper.insert(skuLadderEntity);
    }

    @Override
    public List<ItemSaleVo> querySaleVosBySkuId(Long skuId) {

        ItemSaleVo broundSaleVo = new ItemSaleVo();
        SkuBoundsEntity bround = baseMapper.selectOne(new QueryWrapper<SkuBoundsEntity>().eq("sku_id", skuId));

        if(bround != null){
            broundSaleVo.setType("积分");
            broundSaleVo.setDesc("送" + bround.getGrowBounds() + "成长积分,送" + bround.getBuyBounds() + "购物积分");
        }

        ItemSaleVo fullReductionSaleVo = new ItemSaleVo();
        SkuFullReductionEntity fullReductionEntity = skuFullReductionMapper.selectOne(new QueryWrapper<SkuFullReductionEntity>().eq("sku_id", skuId));

        if(fullReductionEntity != null){
            fullReductionSaleVo.setType("满减");
            fullReductionSaleVo.setDesc("满" + fullReductionEntity.getFullPrice() + "元,减" + fullReductionEntity.getReducePrice() + "元");
        }

        ItemSaleVo ladderSaleVo = new ItemSaleVo();
        SkuLadderEntity ladderEntity = skuLadderMapper.selectOne(new QueryWrapper<SkuLadderEntity>().eq("sku_id", skuId));

        if(ladderEntity != null){
            ladderSaleVo.setType("打折");
            ladderSaleVo.setDesc("满" + ladderEntity.getFullCount() + "元,打" + ladderEntity.getDiscount().divide(new BigDecimal(10)) + "折");
        }

        return Arrays.asList(broundSaleVo,fullReductionSaleVo,ladderSaleVo);
    }

}