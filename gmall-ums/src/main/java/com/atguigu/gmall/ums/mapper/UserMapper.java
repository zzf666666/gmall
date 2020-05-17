package com.atguigu.gmall.ums.mapper;

import com.atguigu.gmall.ums.entity.UserEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户表
 * 
 * @author mj
 * @email mj@110.com
 * @date 2020-05-17 16:26:32
 */
@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {
	
}
