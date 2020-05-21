package com.atguigu.gmall.smsinterface.api;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.smsinterface.vo.SkuSaleVo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface GmallSmsApi {
    @PostMapping("/sms/skubounds/sku/sales")
    ResponseVo<Object> saveSkuSales(@RequestBody SkuSaleVo skuSaleVo);
}
