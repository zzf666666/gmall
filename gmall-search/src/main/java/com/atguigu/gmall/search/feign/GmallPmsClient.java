package com.atguigu.gmall.search.feign;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.pmsinterface.api.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("pms-service")
public interface GmallPmsClient extends GmallPmsApi{


}
