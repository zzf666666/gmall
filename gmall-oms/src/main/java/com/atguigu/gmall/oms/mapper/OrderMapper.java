package com.atguigu.gmall.oms.mapper;

import com.atguigu.gmall.oms.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author mj
 * @email mj@110.com
 * @date 2020-05-17 16:48:09
 */
@Mapper
public interface OrderMapper extends BaseMapper<OrderEntity> {
	
}
