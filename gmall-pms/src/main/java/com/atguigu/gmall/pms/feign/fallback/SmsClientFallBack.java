package com.atguigu.gmall.pms.feign.fallback;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.feign.SmsClient;
import com.atguigu.gmall.smsinterface.vo.SkuSaleVo;
import org.springframework.stereotype.Component;

@Component
public class SmsClientFallBack implements SmsClient {
    @Override
    public ResponseVo<Object> saveSkuSales(SkuSaleVo skuSaleVo) {
        return ResponseVo.ok("服务熔断 降级 处理");
    }
}
