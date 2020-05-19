package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.mapper.AttrMapper;
import com.atguigu.gmall.pms.vo.AttrGroupVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.AttrGroupMapper;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.gmall.pms.service.AttrGroupService;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupMapper, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    private AttrMapper attrMapper;

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

}