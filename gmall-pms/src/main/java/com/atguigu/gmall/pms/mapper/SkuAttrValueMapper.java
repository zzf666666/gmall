package com.atguigu.gmall.pms.mapper;

import com.atguigu.gmall.pmsinterface.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pmsinterface.entity.SpuAttrValueEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

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

    List<Map<String, Object>> querySkuJsonsBySpuId(Long spuId);

    List<SkuAttrValueEntity> querySkuAttrValuesBySkuIdAndGid(@Param("skuId") Long skuId, @Param("groupId") Long groupId);
}
