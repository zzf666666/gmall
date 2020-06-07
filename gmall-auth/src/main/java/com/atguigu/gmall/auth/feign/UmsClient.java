package com.atguigu.gmall.auth.feign;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.umsinterface.api.UmsApi;
import com.atguigu.gmall.umsinterface.entity.UserEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("ums-service")
public interface UmsClient extends UmsApi {

}
