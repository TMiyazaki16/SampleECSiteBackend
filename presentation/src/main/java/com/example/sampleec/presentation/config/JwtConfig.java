package com.example.sampleec.presentation.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.io.Decoders;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT ユーティリティ。
 * jjwt 0.12.x API を使用。
 */
@Slf4j
@Component
public class JwtConfig {

    private final SecretKey secretKey;
    private final long expirationMillis;

    public JwtConfig(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-hours}") int expirationHours
    ) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expirationMillis = (long) expirationHours * 60 * 60 * 1000;
    }

    /**
     * JWT トークンを生成する。
     *
     * @param userId ユーザーID（subject に格納）
     * @return JWT 文字列
     */
    public String generateToken(String userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
                .subject(userId)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    /**
     * JWT からユーザーIDを抽出する。
     *
     * @param token JWT 文字列
     * @return ユーザーID
     */
    public String extractUserId(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * JWT が有効かどうかを検証する。
     *
     * @param token JWT 文字列
     * @return 有効な場合 true
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception e) {
            log.warn("JWT 検証失敗: {}", e.getMessage());
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
