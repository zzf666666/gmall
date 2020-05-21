package com.atguigu.gmall.pms.feign;

import com.atguigu.gmall.pms.feign.fallback.SmsClientFallBack;
import com.atguigu.gmall.smsinterface.api.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "sms-service",fallback = SmsClientFallBack.class)
public interface SmsClient extends GmallSmsApi {

}
