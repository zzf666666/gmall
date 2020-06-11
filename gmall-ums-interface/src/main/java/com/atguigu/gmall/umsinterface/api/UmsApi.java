package com.atguigu.gmall.umsinterface.api;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.umsinterface.entity.UserAddressEntity;
import com.atguigu.gmall.umsinterface.entity.UserEntity;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface UmsApi {

    @GetMapping("/ums/user/query")
    ResponseVo<UserEntity> query(@RequestParam("username") String username, @RequestParam("password")String password);

    @GetMapping("/ums/useraddress/user/{userId}")
    ResponseVo<List<UserAddressEntity>> queryByUserId(@PathVariable("userId") Integer userId);

    @GetMapping("/ums/useraddress/{id}")
    ResponseVo<UserEntity> queryUserById(@PathVariable("id") Long id);
}
