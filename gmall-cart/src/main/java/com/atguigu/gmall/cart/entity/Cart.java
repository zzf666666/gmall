package com.atguigu.gmall.cart.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("cart_info")
public class Cart {

    @TableId
    private Long id;
    private String userId;
    private Long skuId;
    private String title;
    private String defaultImage;
    private String saleAttrs;
    private BigDecimal price;
    private String sales;
    private Boolean store;
    private Integer count;
    @TableField("`check`")
    private Boolean check;
}
