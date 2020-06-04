package com.atguigu.gmall.pms.mapper;

import com.atguigu.gmall.pmsinterface.entity.SpuAttrValueEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * spu属性值
 * 
 * @author mj
 * @email mj@110.com
 * @date 2020-05-17 15:57:59
 */
@Mapper
public interface SpuAttrValueMapper extends BaseMapper<SpuAttrValueEntity> {

    List<SpuAttrValueEntity> querySpuAttrValuesBySpuId(Long spuId);

    List<SpuAttrValueEntity> querySpuAttrValuesBySpuIdAndGId(@Param("spuId") Long spuId, @Param("groupId") Long id);
}
