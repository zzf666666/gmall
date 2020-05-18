package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.service.AttrService;
import com.atguigu.gmall.pms.service.CategoryService;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.CategoryMapper;
import com.atguigu.gmall.pms.entity.CategoryEntity;

import java.util.List;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, CategoryEntity> implements CategoryService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<CategoryEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<CategoryEntity> queryCategory(Long pId) {
        QueryWrapper<CategoryEntity> queryWrapper = new QueryWrapper<>();

        if(pId != -1){
            queryWrapper.eq("parent_id", pId);
        }

        return baseMapper.selectList(queryWrapper);
    }
}