package com.atguigu.gmall.pms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.pmsinterface.entity.AttrEntity;

import java.util.List;

/**
 * 商品属性
 *
 * @author mj
 * @email mj@110.com
 * @date 2020-05-17 15:57:59
 */
public interface AttrService extends IService<AttrEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    List<AttrEntity> querySkuAttr(Long cId, Integer type, Integer searchType);
}

