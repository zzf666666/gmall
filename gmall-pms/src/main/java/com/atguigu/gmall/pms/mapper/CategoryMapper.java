package com.atguigu.gmall.pms.mapper;

import com.atguigu.gmall.pmsinterface.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 商品三级分类
 * 
 * @author mj
 * @email mj@110.com
 * @date 2020-05-17 15:57:59
 */
@Mapper
public interface CategoryMapper extends BaseMapper<CategoryEntity> {

    List<CategoryEntity> queryCategoriesWithSubByPid(Long pid);
}
