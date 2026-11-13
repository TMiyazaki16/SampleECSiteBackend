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

    /** HMAC-SHA256 の署名に必要な鍵長（バイト）。 */
    private static final int MIN_SECRET_BYTES = 32;

    public JwtConfig(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-hours}") int expirationHours
    ) {
        this.secretKey = Keys.hmacShaKeyFor(decodeSecret(secret));
        this.expirationMillis = (long) expirationHours * 60 * 60 * 1000;
    }

    /**
     * 署名鍵をデコードし、鍵として妥当かを検証する。
     * 不正な場合は起動を失敗させ、脆弱な鍵のまま稼働することを防ぐ。
     *
     * @param secret Base64 エンコードされた署名鍵
     * @return デコード済みの鍵バイト列
     * @throws IllegalStateException 鍵が未設定・不正・長さ不足の場合
     */
    private static byte[] decodeSecret(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "環境変数 JWT_SECRET が設定されていません。"
                            + "Base64 エンコードした 32 バイト以上の値を指定してください"
                            + "（生成例: openssl rand -base64 32）。");
        }

        byte[] decoded;
        try {
            decoded = Decoders.BASE64.decode(secret.trim());
        } catch (RuntimeException e) {
            throw new IllegalStateException(
                    "環境変数 JWT_SECRET が Base64 として解釈できません。"
                            + "Base64 エンコードした値を指定してください"
                            + "（生成例: openssl rand -base64 32）。", e);
        }

        if (decoded.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "環境変数 JWT_SECRET が短すぎます（デコード後 " + decoded.length
                            + " バイト）。HMAC-SHA256 の署名には "
                            + MIN_SECRET_BYTES + " バイト以上が必要です"
                            + "（生成例: openssl rand -base64 32）。");
        }

        return decoded;
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
