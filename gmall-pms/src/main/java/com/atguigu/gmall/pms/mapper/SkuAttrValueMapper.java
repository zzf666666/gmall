package com.atguigu.gmall.pms.mapper;

import com.atguigu.gmall.pmsinterface.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pmsinterface.entity.SpuAttrValueEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * sku销售属性&值
 * 
 * @author mj
 * @email mj@110.com
 * @date 2020-05-17 15:57:59
 */
@Mapper
public interface SkuAttrValueMapper extends BaseMapper<SkuAttrValueEntity> {


    List<SkuAttrValueEntity> querySkuAttrValuesBySkuId(Long skuId);
}
