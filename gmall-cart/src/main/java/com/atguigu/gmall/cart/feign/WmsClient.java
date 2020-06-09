package com.atguigu.gmall.cart.feign;

import com.atguigu.gamll.wmsinterface.api.GmallWmsApi;
import com.atguigu.gmall.pmsinterface.api.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("wms-service")
public interface WmsClient extends GmallWmsApi {
}
