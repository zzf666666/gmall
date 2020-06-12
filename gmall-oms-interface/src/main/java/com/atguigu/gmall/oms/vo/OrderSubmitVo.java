package com.atguigu.gmall.oms.vo;

import com.atguigu.gmall.umsinterface.entity.UserAddressEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderSubmitVo {

    private Long userId;
    private UserAddressEntity address;
    private List<OrderItemVo> items;
    private Long bounds;
    private String orderToken;
    private Integer payType;
    private String deliveryCompany;
    private BigDecimal totalPrice;
}
