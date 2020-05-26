package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pmsinterface.entity.AttrGroupEntity;
import org.springframework.stereotype.Service;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.AttrMapper;
import com.atguigu.gmall.pmsinterface.entity.AttrEntity;
import com.atguigu.gmall.pms.service.AttrService;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrMapper, AttrEntity> implements AttrService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<AttrEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<AttrEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<AttrEntity> querySkuAttr(Long cId, Integer type, Integer searchType) {
        QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<>();

        if(cId != 0){
            queryWrapper.eq("category_id", cId);
        }

        if(type != null){
            queryWrapper.eq("type", type);
        }

        if(searchType != null){
            queryWrapper.eq("search_type", searchType);
        }
        return baseMapper.selectList(queryWrapper);
    }

}