package com.atguigu.gmall.pms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.pmsinterface.entity.SpuAttrValueEntity;

import java.util.List;

/**
 * spu属性值
 *
 * @author mj
 * @email mj@110.com
 * @date 2020-05-17 15:57:59
 */
public interface SpuAttrValueService extends IService<SpuAttrValueEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    List<SpuAttrValueEntity> querySpuAttrValuesBySpuId(Long spuId);
}

