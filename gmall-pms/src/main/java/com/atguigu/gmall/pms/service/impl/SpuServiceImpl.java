package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SpuDescEntity;
import com.atguigu.gmall.pms.mapper.SpuAttrValueMapper;
import com.atguigu.gmall.pms.mapper.SpuDescMapper;
import com.atguigu.gmall.pms.service.SpuAttrValueService;
import com.atguigu.gmall.pms.vo.SpuAttrValueVo;
import com.atguigu.gmall.pms.vo.SpuVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.SpuMapper;
import com.atguigu.gmall.pms.entity.SpuEntity;
import com.atguigu.gmall.pms.service.SpuService;
import org.springframework.util.CollectionUtils;

import javax.xml.crypto.Data;


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
    }

}