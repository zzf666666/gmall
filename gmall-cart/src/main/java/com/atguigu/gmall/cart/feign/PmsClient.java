package com.atguigu.gmall.cart.feign;

import com.atguigu.gmall.pmsinterface.api.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("pms-service")
public interface PmsClient extends GmallPmsApi {
}
