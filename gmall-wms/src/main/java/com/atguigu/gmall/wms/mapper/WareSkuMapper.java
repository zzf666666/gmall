package com.atguigu.gmall.wms.mapper;

import com.atguigu.gamll.wmsinterface.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品库存
 * 
 * @author mj
 * @email mj@110.com
 * @date 2020-05-19 21:59:33
 */
@Mapper
public interface WareSkuMapper extends BaseMapper<WareSkuEntity> {
    List<WareSkuEntity> selectWare(@Param("skuId") Long skyId, @Param("count") Integer count);

    Integer lockWare(@Param("wareId") Integer wareId, @Param("skuId") Long skuId,@Param("count")Integer count);

    Integer unLockWare(@Param("wareId") Integer wareId, @Param("skuId") Long skuId,@Param("count")Integer count);
}
