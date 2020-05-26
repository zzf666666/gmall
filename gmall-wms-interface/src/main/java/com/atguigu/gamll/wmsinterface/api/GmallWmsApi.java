package com.atguigu.gamll.wmsinterface.api;

import com.atguigu.gamll.wmsinterface.entity.WareSkuEntity;
import com.atguigu.gmall.common.bean.ResponseVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public interface GmallWmsApi {
    @GetMapping("/wms/waresku/sku/{skuId}")
    ResponseVo<List<WareSkuEntity>> queryWareBySkuId(@PathVariable Long skuId);
}
