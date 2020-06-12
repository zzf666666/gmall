package com.atguigu.gmall.oms.feign;

import com.atguigu.gmall.pmsinterface.api.GmallPmsApi;
import com.atguigu.gmall.smsinterface.api.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("sms-service")
public interface SmsClient extends GmallSmsApi {

}
