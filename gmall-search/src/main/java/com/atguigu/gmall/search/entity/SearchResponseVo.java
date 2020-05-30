package com.atguigu.gmall.search.entity;

import com.atguigu.gmall.pmsinterface.entity.BrandEntity;
import com.atguigu.gmall.pmsinterface.entity.CategoryEntity;
import io.swagger.models.auth.In;
import lombok.Data;

import java.util.List;

@Data
public class SearchResponseVo {

    private List<BrandEntity> brands;
    private List<CategoryEntity> categories;
    private List<SearchResposneAttrVo> filters;

    private Integer pageNum;
    private Integer pageSize;
    private Long total;

    private List<Goods> goodsList;
}
