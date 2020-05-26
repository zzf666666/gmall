package com.atguigu.gmall.search.feign;


import com.atguigu.gamll.wmsinterface.api.GmallWmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("wms-service")
public interface GmallWmsClient extends GmallWmsApi {
}
