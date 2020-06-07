package com.atguigu.gmall.umsinterface.api;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.umsinterface.entity.UserEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

public interface UmsApi {

    @GetMapping("/ums/user/query")
    ResponseVo<UserEntity> query(@RequestParam("username") String username, @RequestParam("password")String password);
}
