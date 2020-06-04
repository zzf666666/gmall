package com.atguigu.gmall.item.feign;

import com.atguigu.gmall.smsinterface.api.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("sms-service")
public interface SmsClient extends GmallSmsApi {
}
