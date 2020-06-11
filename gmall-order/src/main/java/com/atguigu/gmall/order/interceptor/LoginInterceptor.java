package com.atguigu.gmall.order.interceptor;

import com.atguigu.gmall.order.config.JwtProperties;
import com.atguigu.gmall.order.vo.UserInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

        String userId = request.getHeader("userId");
        if(StringUtils.isNotBlank(userId)){
            USER_INFO.setUserId(Integer.parseInt(userId));
            THREADLOCAL.set(USER_INFO);
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
