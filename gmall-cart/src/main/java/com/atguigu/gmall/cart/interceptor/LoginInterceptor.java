package com.atguigu.gmall.cart.interceptor;

import com.atguigu.gmall.cart.config.JwtProperties;
import com.atguigu.gmall.cart.entity.UserInfo;
import com.atguigu.gmall.common.utils.CookieUtils;
import com.atguigu.gmall.common.utils.JwtUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;

@EnableConfigurationProperties(JwtProperties.class)
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    public static final UserInfo USER_INFO = new UserInfo();

    private static final ThreadLocal<UserInfo> THREADLOCAL = new ThreadLocal();

    public static UserInfo getUserInfo(){
        return THREADLOCAL.get();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        System.out.println("目标方法前");
        String token = CookieUtils.getCookieValue(request, jwtProperties.getCookieName());
        String userKey = CookieUtils.getCookieValue(request, jwtProperties.getUserKey());

        if(StringUtils.isBlank(userKey)){
            userKey = UUID.randomUUID().toString();

            CookieUtils.setCookie(request, response, jwtProperties.getUserKey(), userKey, 15552000);
        }

        if(StringUtils.isBlank(token)){
//            USER_INFO.setUserKey(userKey);
//            request.setAttribute("userKey", userKey);
            THREADLOCAL.set(new UserInfo(null,userKey));
            return true;
        }

        try {
            Map<String, Object> infoFromToken = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());
            Integer userId = (Integer)infoFromToken.get("userId");
//            request.setAttribute("userId", userId);
//            request.setAttribute("userKey", userKey);
//            USER_INFO.setUserId(userId);
//            USER_INFO.setUserKey(userKey);
            THREADLOCAL.set(new UserInfo(userId,userKey));
        } catch (Exception e) {
            e.printStackTrace();
//            request.setAttribute("userKey", userKey);
//            USER_INFO.setUserKey(userKey);
            THREADLOCAL.set(new UserInfo(null,userKey));
        }


        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        System.out.println("目标方法后,渲染前");
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        System.out.println("渲染后");
        THREADLOCAL.remove();
    }
}
