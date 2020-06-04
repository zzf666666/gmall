package com.atguigu.gmall.item.feign;

import com.atguigu.gmall.pmsinterface.api.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("pms-service")
public interface PmsClient extends GmallPmsApi {
}
