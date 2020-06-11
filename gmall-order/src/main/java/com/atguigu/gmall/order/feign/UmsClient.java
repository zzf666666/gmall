package com.atguigu.gmall.order.feign;

import com.atguigu.gmall.umsinterface.api.UmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("ums-service")
public interface UmsClient extends UmsApi {
}
