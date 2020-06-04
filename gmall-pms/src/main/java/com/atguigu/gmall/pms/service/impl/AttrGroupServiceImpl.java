package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.pms.mapper.SpuAttrValueMapper;
import com.atguigu.gmall.pmsinterface.entity.AttrEntity;
import com.atguigu.gmall.pms.mapper.AttrMapper;
import com.atguigu.gmall.pms.vo.AttrGroupVo;
import com.atguigu.gmall.pmsinterface.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pmsinterface.entity.SpuAttrValueEntity;
import com.atguigu.gmall.pmsinterface.vo.AttrValueVo;
import com.atguigu.gmall.pmsinterface.vo.ItemGroupVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.AttrGroupMapper;
import com.atguigu.gmall.pmsinterface.entity.AttrGroupEntity;
import com.atguigu.gmall.pms.service.AttrGroupService;
import org.springframework.util.CollectionUtils;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupMapper, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    private AttrMapper attrMapper;

    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    private SpuAttrValueMapper spuAttrValueMapper;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<AttrGroupEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<AttrGroupEntity> queryByCidPage(Long cId) {
        QueryWrapper<AttrGroupEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("category_id", cId);

        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public List<AttrGroupVo> queryByCid(Long cId) {
        QueryWrapper<AttrGroupEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("category_id", cId);
        List<AttrGroupEntity> attrGroupList = baseMapper.selectList(queryWrapper);

        List<AttrGroupVo> attrGroupVoList = attrGroupList.stream().map(attrGroup -> {
            AttrGroupVo attrGroupVo = new AttrGroupVo();
            System.out.println("---------->>>复制前" + attrGroupVo);
            BeanUtils.copyProperties(attrGroup, attrGroupVo);
            System.out.println("---------->>>复制后" + attrGroupVo);
            List<AttrEntity> attrList = attrMapper.selectList(new QueryWrapper<AttrEntity>().eq("group_id", attrGroup.getId()).eq("type",1));
            attrGroupVo.setAttrEntities(attrList);
            return attrGroupVo;
        }).collect(Collectors.toList());

        return attrGroupVoList;
    }

    @Override
    public List<ItemGroupVo> queryItemGroupVoByCidAndSpuIdAndSkuId(Long cid, Long spuId, Long skuId) {

        List<AttrGroupEntity> groups = baseMapper.selectList(new QueryWrapper<AttrGroupEntity>().eq("category_id", cid));

        List<ItemGroupVo> itemGroupVos = new ArrayList<>();

        if(!CollectionUtils.isEmpty(groups)){
            itemGroupVos = groups.stream().map(group -> {

                ItemGroupVo itemGroupVo = new ItemGroupVo();
                itemGroupVo.setGroupName(group.getName());

                List<AttrValueVo> attrValueVos = new ArrayList<>();

                List<SkuAttrValueEntity> skuAttrValueEntities = skuAttrValueMapper.querySkuAttrValuesBySkuIdAndGid(skuId, group.getId());
                if (!CollectionUtils.isEmpty(skuAttrValueEntities)) {
                    attrValueVos.addAll(skuAttrValueEntities.stream().map(skuAttr -> {
                        AttrValueVo attrValueVo = new AttrValueVo();
                        BeanUtils.copyProperties(skuAttr, attrValueVo);
                        return attrValueVo;
                    }).collect(Collectors.toList()));
                }

                List<SpuAttrValueEntity> spuAttrValueEntities = spuAttrValueMapper.querySpuAttrValuesBySpuIdAndGId(spuId, group.getId());
                if (!CollectionUtils.isEmpty(spuAttrValueEntities)) {
                    attrValueVos.addAll(spuAttrValueEntities.stream().map(spuAttr -> {
                        AttrValueVo attrValueVo = new AttrValueVo();
                        BeanUtils.copyProperties(spuAttr, attrValueVo);
                        return attrValueVo;
                    }).collect(Collectors.toList()));
                }

                itemGroupVo.setAttrValues(attrValueVos);

                return itemGroupVo;
            }).collect(Collectors.toList());
        }

        return itemGroupVos;
    }

}