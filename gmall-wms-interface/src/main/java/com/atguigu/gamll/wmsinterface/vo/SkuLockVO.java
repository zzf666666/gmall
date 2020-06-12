package com.atguigu.gamll.wmsinterface.vo;

import lombok.Data;

@Data
public class SkuLockVO {

    private Long skuId;
    private Integer count;
    private Integer wareId;
    private Boolean lock;
}
