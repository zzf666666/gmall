package com.atguigu.gmall.pms.mapper;

import com.atguigu.gmall.pmsinterface.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pmsinterface.entity.SkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * sku信息
 * 
 * @author mj
 * @email mj@110.com
 * @date 2020-05-17 15:57:59
 */
@Mapper
public interface SkuMapper extends BaseMapper<SkuEntity> {

    List<SkuAttrValueEntity> querySaleAttrValuesBySpuId(Long spuId);
}
