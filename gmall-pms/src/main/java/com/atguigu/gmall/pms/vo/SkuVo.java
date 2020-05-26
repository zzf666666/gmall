package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pmsinterface.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pmsinterface.entity.SkuEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SkuVo extends SkuEntity {
    private List<String> images;

    private List<SkuAttrValueEntity> saleAttrs;

    private BigDecimal growBounds;

    private BigDecimal buyBounds;

    private List<Integer> work;

    private BigDecimal fullPrice;

    private BigDecimal reducePrice;

    private Integer fullAddOther;

    private Integer fullCount;

    private BigDecimal discount;

    private Integer ladderAddOther;
}
