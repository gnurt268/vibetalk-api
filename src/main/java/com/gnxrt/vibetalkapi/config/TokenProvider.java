package com.gnxrt.vibetalkapi.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import javax.crypto.SecretKey;
import java.util.Date;

import static com.gnxrt.vibetalkapi.config.JwtConstant.JWT_EXPIRATION;

@Service
public class TokenProvider {
    private final JwtConstant jwtConstant;

    public TokenProvider(JwtConstant jwtConstant) {
        this.jwtConstant = jwtConstant;
    }

    public String generateToken(Authentication authentication) {
        SecretKey key = Keys.hmacShaKeyFor(jwtConstant.getSecretKey().getBytes());
        String jwt = Jwts.builder()
                .setIssuer("Trung Nguyen")
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + JWT_EXPIRATION))
                .claim("email", authentication.getName())
                .signWith(key)
                .compact();
        return jwt;
    }

    public String getEmailFromToken(String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        SecretKey key = Keys.hmacShaKeyFor(jwtConstant.getSecretKey().getBytes());
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Object emailClaim = claims.get("email");
        return emailClaim != null ? emailClaim.toString() : null;
    }
}