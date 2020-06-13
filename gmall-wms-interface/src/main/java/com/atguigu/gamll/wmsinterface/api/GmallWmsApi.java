package com.atguigu.gamll.wmsinterface.api;

import com.atguigu.gamll.wmsinterface.entity.WareSkuEntity;
import com.atguigu.gamll.wmsinterface.vo.SkuLockVO;
import com.atguigu.gmall.common.bean.ResponseVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface GmallWmsApi {
    @GetMapping("/wms/waresku/sku/{skuId}")
    ResponseVo<List<WareSkuEntity>> queryWareBySkuId(@PathVariable Long skuId);

    @PostMapping("/wms/waresku/check/lock/{orderToken}")
    ResponseVo<List<SkuLockVO>> checkAndLock(@RequestBody List<SkuLockVO> orderItems, @PathVariable("orderToken")String orderToken);
}
