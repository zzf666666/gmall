package com.atguigu.gmall.auth.controller;

import com.atguigu.gmall.auth.config.JwtProperties;
import com.atguigu.gmall.auth.service.AuthService;
import com.atguigu.gmall.common.utils.CookieUtils;
import com.atguigu.gmall.common.utils.JwtUtils;
import com.atguigu.gmall.umsinterface.entity.UserEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.context.request.RequestScope;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;


@Controller
public class SsoController {



    @Autowired
    private AuthService authService;

    @GetMapping("/toLogin.html")
    public String auth(String returnUrl, Map map){

        map.put("returnUrl", returnUrl);

        return "login";
    }

    @PostMapping("/login")
    public String login(String loginName, String password,
                        String returnUrl, HttpServletRequest request, HttpServletResponse response) throws Exception {
        authService.login(loginName, password,request, response);

        returnUrl = StringUtils.isBlank(returnUrl) ? "www.gmall.com" : returnUrl;

        return "redirect:" + returnUrl;
    }
}
