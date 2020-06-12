package com.atguigu.gmall.oms.vo;

import com.atguigu.gmall.pmsinterface.entity.SkuAttrValueEntity;
import com.atguigu.gmall.smsinterface.vo.ItemSaleVo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderItemVo {
    private Long skuId;
    private String title;
    private String defaultImage;
    private BigDecimal price;
    private Integer count;
    private List<SkuAttrValueEntity> saleAttrs;
    private List<ItemSaleVo> sales;
    private Boolean store = false;
    private Integer weight;
}
