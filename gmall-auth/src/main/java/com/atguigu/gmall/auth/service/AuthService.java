package com.atguigu.gmall.auth.service;

import com.atguigu.gmall.umsinterface.entity.UserEntity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface AuthService {
    UserEntity login(String loginName, String password, HttpServletRequest request, HttpServletResponse response) throws Exception;
}
