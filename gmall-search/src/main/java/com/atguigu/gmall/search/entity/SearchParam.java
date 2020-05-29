package com.atguigu.gmall.search.entity;

import io.swagger.models.auth.In;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchParam {

    private String keyword;
    private List<Long> brandId;
    private Long categoryId;
    // 规格参数过滤条件props=5:128G-256G&props=4:8G-12G
    // ["5:128G-256G", "4:8G-12G"]
    private List<String> props;

    //默认得分排序，1-价格降序 2-价格升序 3-销量降序 4-新品降序
    private Integer sort;
    private Integer priceFrom;
    private Integer priceTo;

    private Integer pageNum;
    private final Integer pageSize = 20;

    // 是否有货
    private Boolean store;
}
