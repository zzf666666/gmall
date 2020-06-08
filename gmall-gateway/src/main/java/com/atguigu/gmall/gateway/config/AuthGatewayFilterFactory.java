package com.atguigu.gmall.gateway.config;

import com.atguigu.gmall.common.utils.CookieUtils;
import com.atguigu.gmall.common.utils.IpUtil;
import com.atguigu.gmall.common.utils.JwtUtils;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@EnableConfigurationProperties(JwtProperties.class)
@Component
public class AuthGatewayFilterFactory extends AbstractGatewayFilterFactory<AuthGatewayFilterFactory.AuthConfig> {

//    private static final String AA = "aa";
//    private static final String BB = "bb";
//    private static final String CC = "cc";

    @Autowired
    private JwtProperties jwtProperties;

    private static final String PATHS = "paths";

    public AuthGatewayFilterFactory() {
        super(AuthGatewayFilterFactory.AuthConfig.class);
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList(PATHS);
    }

    @Override
    public ShortcutType shortcutType() {
        return ShortcutType.GATHER_LIST;
    }

    @Override
    public GatewayFilter apply(AuthGatewayFilterFactory.AuthConfig config) {
        return new GatewayFilter(){
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

                ServerHttpRequest request = exchange.getRequest();
                ServerHttpResponse response = exchange.getResponse();

                String uri = request.getURI().toString();

                List<String> paths = config.getPaths();
                if(paths.stream().allMatch(path -> !uri.contains(path))){
                    chain.filter(exchange);
                }


                String token = request.getHeaders().getFirst("token");
                if(StringUtils.isBlank(token)){
                    MultiValueMap<String, HttpCookie> cookies = request.getCookies();

                    if(!CollectionUtils.isEmpty(cookies) && cookies.containsKey(jwtProperties.getCookieName())){
                        token = cookies.getFirst(jwtProperties.getCookieName()).getValue();
                    }
                }
                if(StringUtils.isBlank(token)){
                    response.setStatusCode(HttpStatus.SEE_OTHER);
                    response.getHeaders().set(HttpHeaders.LOCATION,"http://sso.gmall.com/toLogin.html?returnUrl=" + request.getURI().toString());
                    return response.setComplete();
                }

                try {
                    Map<String, Object> infoFromToken = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());
                    String userId = infoFromToken.get("userId").toString();
                    String ip = infoFromToken.get("ip").toString();
                    if(!StringUtils.equals(IpUtil.getIpAddressAtGateway(request),ip)){
                        response.setStatusCode(HttpStatus.SEE_OTHER);
                        response.getHeaders().set(HttpHeaders.LOCATION,"http://sso.gmall.com/toLogin.html?returnUrl=" + request.getURI().toString());
                        return response.setComplete();
                    }

                    request.mutate().header("userId", userId).build();
                    exchange.mutate().request(request).build();
                } catch (Exception e) {
                    e.printStackTrace();
                    response.setStatusCode(HttpStatus.SEE_OTHER);
                    response.getHeaders().set(HttpHeaders.LOCATION,"http://sso.gmall.com/toLogin.html?returnUrl=" + request.getURI().toString());
                    return response.setComplete();
                }

                return chain.filter(exchange);
            }
        };
    }

    @Data
    public static class AuthConfig{
        private List<String> paths;
    }

}
