package com.atguigu.gmall.cart.entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class item {
    private Long id;
    private Long userId;
    private Long skuId;
    private String title;
    private String defaultImage;
    private String saleAttrs;
    private BigDecimal price;
    private String attrs;
    private Boolean store;
    private Integer count;
    private Boolean check;
}
