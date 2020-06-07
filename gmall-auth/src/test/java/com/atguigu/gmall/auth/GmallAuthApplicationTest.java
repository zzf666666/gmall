package com.atguigu.gmall.auth;

import com.atguigu.gmall.common.utils.JwtUtils;
import com.atguigu.gmall.common.utils.RsaUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.PostConstruct;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
class GmallAuthApplicationTest {

    private static final String pubKeyPath = "F:\\ideaproject\\guli_gmall\\rsa\\rsa.pub";
    private static final String priKeyPath = "F:\\ideaproject\\guli_gmall\\rsa\\rsa.pri";

    private PublicKey publicKey;
    private PrivateKey privateKey;

    @Test
    void contextLoads() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "14250^*(#&*@.;`wcnm");
    }

    @PostConstruct
    void init() throws Exception{
        publicKey = RsaUtils.getPublicKey(pubKeyPath);
        privateKey = RsaUtils.getPrivateKey(priKeyPath);

        System.out.println(publicKey);
        System.out.println(privateKey);
    }

    @Test
    void test() throws Exception {
        Map<String, Object> map = new HashMap<>();

        map.put("柳岩", "36D");
        map.put("德玛", 118);

        String token = JwtUtils.generateToken(map, privateKey, 3);
        System.out.println(token);


    }

    @Test
    void parse() throws Exception {

        String token = "eyJhbGciOiJSUzI1NiJ9.eyLlvrfnjpsiOjExOCwi5p-z5bKpIjoiMzZEIiwiZXhwIjoxNTkxNTM4ODQ1fQ.fkT--6TOvb3cDmyEYnn0Z5qs4V00SRP2vcgLbsDHQWqNlyBMHMDK0D_uCV97PuHGDkJxp6ScqziEl0N1UMTOTmjGztWo039jC9FqEgKTT-OpCFIhWSyfsVG6Lm6gBfJ0KRGzku1blzDMLiSNrmIsiXjzW30JPLShZpgtadWSankJRL9pXVouzYqlIXWLTQy9d2a0Uj8myyRXHgKb0dNdM7eTw4vRXcyAhkUC0kbby5jGiXeV5pmBPxdyxXce9iXH9DIAvjQ1h_iJij5Xpb5W0xd9aR6HM4CAWVZrxBWRWi1YfPrSbHaR1uuMQkeeBnun_hD8UiiUbv2EoNG4zMQ8Rw";

        Map<String, Object> info = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println(info);
    }

}