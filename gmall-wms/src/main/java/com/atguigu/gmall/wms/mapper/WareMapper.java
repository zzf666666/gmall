package com.atguigu.gmall.wms.mapper;

import com.atguigu.gamll.wmsinterface.entity.WareEntity;
import com.atguigu.gamll.wmsinterface.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 仓库信息
 * 
 * @author mj
 * @email mj@110.com
 * @date 2020-05-19 21:59:33
 */
@Mapper
public interface WareMapper extends BaseMapper<WareEntity> {

}
