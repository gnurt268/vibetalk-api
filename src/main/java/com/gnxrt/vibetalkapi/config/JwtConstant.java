package com.gnxrt.vibetalkapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtConstant {

    public static final String JWT_HEADER = "Authorization";
    public static final long JWT_EXPIRATION = 86400000;

    @Value("${jwt.secret}")
    private String secretKey;

    public String getSecretKey() {
        return secretKey;
    }
}