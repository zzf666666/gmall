package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.feign.SmsClient;
import com.atguigu.gmall.pms.mapper.SkuMapper;
import com.atguigu.gmall.pms.mapper.SpuDescMapper;
import com.atguigu.gmall.pms.service.*;
import com.atguigu.gmall.pms.vo.SkuVo;
import com.atguigu.gmall.pms.vo.SpuAttrValueVo;
import com.atguigu.gmall.pms.vo.SpuVo;
import com.atguigu.gmall.smsinterface.vo.SkuSaleVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.SpuMapper;
import org.springframework.util.CollectionUtils;


@Service("spuService")
public class SpuServiceImpl extends ServiceImpl<SpuMapper, SpuEntity> implements SpuService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SpuEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SpuEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public PageResultVo querySpuInfo(Long categoryId, PageParamVo pageParamVo) {
        QueryWrapper<SpuEntity> queryWrapper = new QueryWrapper<>();
        if(categoryId != 0){
            queryWrapper.eq("category_id", categoryId);
        }

        String key = pageParamVo.getKey();

        if(StringUtils.isNotBlank(key)){
            queryWrapper.and(t -> t.like("name", key).or().eq("id", key));
        }

        IPage<SpuEntity> iPage = baseMapper.selectPage(pageParamVo.getPage(), queryWrapper);
        return new PageResultVo(iPage);
    }

    @Autowired
    private SpuDescMapper spuDescMapper;

    @Autowired
    private SpuAttrValueService spuAttrValueService;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SkuAttrValueService skuAttrValueService;

    @Autowired
    private SmsClient smsClient;

    @Override
    public void bigSave(SpuVo spuVo) {
        spuVo.setCreateTime(new Date());
        spuVo.setUpdateTime(spuVo.getCreateTime());
        save(spuVo);
        Long spuId = spuVo.getId();

        List<String> spuImages = spuVo.getSpuImages();
        System.out.println("==============>>>>>>>>>>" + spuImages);
        if(!CollectionUtils.isEmpty(spuImages)){
            SpuDescEntity spuDescEntity = new SpuDescEntity();
            spuDescEntity.setSpuId(spuId);
            spuDescEntity.setDecript(StringUtils.join(spuImages,","));
            spuDescMapper.insert(spuDescEntity);
        }

        List<SpuAttrValueVo> baseAttrs = spuVo.getBaseAttrs();
        if(!CollectionUtils.isEmpty(baseAttrs)){
            List<SpuAttrValueEntity> collect = baseAttrs.stream().map(sv -> {
                SpuAttrValueEntity spuAttrValueEntity = new SpuAttrValueEntity();
                BeanUtils.copyProperties(sv, spuAttrValueEntity);
                spuAttrValueEntity.setSpuId(spuId);
                return spuAttrValueEntity;
            }).collect(Collectors.toList());

            spuAttrValueService.saveBatch(collect);
        };

        List<SkuVo> skus = spuVo.getSkus();
        if(!CollectionUtils.isEmpty(skus)){
            skus.forEach(sku -> {
                sku.setSpuId(spuId);
                sku.setBrandId(spuVo.getBrandId());
                sku.setCatagoryId(spuVo.getCategoryId());
                List<String> images = sku.getImages();
                sku.setDefaultImage(StringUtils.isNotBlank(sku.getDefaultImage()) ? sku.getDefaultImage() : images.get(0));
                skuMapper.insert(sku);
                Long skuId = sku.getId();

                if(!CollectionUtils.isEmpty(images)){
                    List<SkuImagesEntity> skuImagesList = images.stream().map((img -> {
                        SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                        skuImagesEntity.setSkuId(skuId);
                        skuImagesEntity.setUrl(img);
                        skuImagesEntity.setDefaultStatus(img.equals(sku.getDefaultImage()) ? 1 : 0);
                        return skuImagesEntity;
                    })).collect(Collectors.toList());

                    skuImagesService.saveBatch(skuImagesList);
                }

                List<SkuAttrValueEntity> saleAttrs = sku.getSaleAttrs();
                if(!CollectionUtils.isEmpty(saleAttrs)){
                    saleAttrs.forEach(saleAttr -> {
                        saleAttr.setSkuId(skuId);
                    });
                    skuAttrValueService.saveBatch(saleAttrs);
                }

                SkuSaleVo saleVo = new SkuSaleVo();
                saleVo.setSkuId(skuId);
                BeanUtils.copyProperties(sku, saleVo);
                smsClient.saveSkuSales(saleVo);
            });
        }
    }

}