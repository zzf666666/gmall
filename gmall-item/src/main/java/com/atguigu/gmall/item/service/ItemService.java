package com.atguigu.gmall.item.service;

import com.atguigu.gmall.item.vo.ItemVo;

public interface ItemService {
    ItemVo queryItemBySkuId(Long skuId);
}
