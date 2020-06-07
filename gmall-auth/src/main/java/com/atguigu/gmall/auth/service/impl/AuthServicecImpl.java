package com.atguigu.gmall.auth.service.impl;

import com.atguigu.gmall.auth.config.JwtProperties;
import com.atguigu.gmall.auth.feign.UmsClient;
import com.atguigu.gmall.auth.service.AuthService;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.utils.CookieUtils;
import com.atguigu.gmall.common.utils.IpUtil;
import com.atguigu.gmall.common.utils.JwtUtils;
import com.atguigu.gmall.umsinterface.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@EnableConfigurationProperties(JwtProperties.class)
@Service
public class AuthServicecImpl implements AuthService {

    @Autowired
    private UmsClient umsClient;

    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public UserEntity login(String loginName, String password, HttpServletRequest request, HttpServletResponse response) throws Exception {
        ResponseVo<UserEntity> query = umsClient.query(loginName,password);
        UserEntity userEntity = query.getData();

        Map<String, Object> map = new HashMap();
        map.put("userId", userEntity.getId());
        map.put("userName", userEntity.getUsername());
        map.put("ip", IpUtil.getIpAddressAtService(request));
        String token = JwtUtils.generateToken(map, jwtProperties.getPrivateKey(), 180);

        CookieUtils.setCookie(request, response, jwtProperties.getUnick(), userEntity.getNickname() , "UTF-8");
        CookieUtils.setCookie(request, response, jwtProperties.getCookieName(),token , "UTF-8");
        return userEntity;
    }
}
