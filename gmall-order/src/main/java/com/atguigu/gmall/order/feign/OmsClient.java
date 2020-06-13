package com.atguigu.gmall.order.feign;

import com.atguigu.gamll.wmsinterface.api.GmallWmsApi;
import com.atguigu.gmall.oms.api.GmallOmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("oms-service")
public interface OmsClient extends GmallOmsApi {
}
