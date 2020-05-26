package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.vo.AttrGroupVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.pmsinterface.entity.AttrGroupEntity;

import java.util.List;

/**
 * 属性分组
 *
 * @author mj
 * @email mj@110.com
 * @date 2020-05-17 15:57:59
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    List<AttrGroupEntity> queryByCidPage(Long cId);

    List<AttrGroupVo> queryByCid(Long cId);
}

