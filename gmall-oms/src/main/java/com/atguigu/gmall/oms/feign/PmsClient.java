package com.atguigu.gmall.oms.feign;

import com.atguigu.gmall.pmsinterface.api.GmallPmsApi;
import com.atguigu.gmall.umsinterface.api.UmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("pms-service")
public interface PmsClient extends GmallPmsApi {

}
