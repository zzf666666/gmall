package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.service.CategoryService;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.CategoryMapper;
import com.atguigu.gmall.pmsinterface.entity.CategoryEntity;

import java.util.Arrays;
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

    @Override
    public List<CategoryEntity> queryCategoriesWithSubByPid(Long pid) {
        return baseMapper.queryCategoriesWithSubByPid(pid);
    }

    @Override
    public List<CategoryEntity> queryCategoriesByCid3(Long cid3) {
        CategoryEntity category3 = baseMapper.selectById(cid3);
        Long cid2 = category3.getParentId();

        CategoryEntity category2 = baseMapper.selectById(cid2);
        Long cid1 = category2.getParentId();

        CategoryEntity category1 = baseMapper.selectById(cid1);

        return Arrays.asList(category1,category2,category3);
    }
}