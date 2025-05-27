package com.contract.backend.common.util.jwt;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final Key key;
    private final long accessValidityInMilliseconds;
    private final long refreshValidityInMilliseconds;

    // 애플리케이션 환경(dev, prod 등)
    @Value("${app.env}")
    private String appEnv;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long accessValidityInMilliseconds,
            @Value("${jwt.refreshExpiration}") long refreshValidityInMilliseconds
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessValidityInMilliseconds = accessValidityInMilliseconds;
        this.refreshValidityInMilliseconds = refreshValidityInMilliseconds;
    }

    public String createToken(String userUuid) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessValidityInMilliseconds);
        return Jwts.builder()
                .setSubject(userUuid)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken(String userUuid) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshValidityInMilliseconds);
        return Jwts.builder()
                .setSubject(userUuid)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUserUuid(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    //HttpOnly + Secure 쿠키 형태의 리프레시 토큰 생성.
    public ResponseCookie createRefreshTokenCookie(String refreshToken) {
        // dev 환경에선 secure=false, prod 환경에선 secure=true
        boolean secureFlag = !"dev".equalsIgnoreCase(appEnv);
        // 쿠키 maxAge는 초 단위
        long maxAgeSeconds = refreshValidityInMilliseconds / 1000;

        return ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(secureFlag)
                .path("/")
                .maxAge(maxAgeSeconds)
                .sameSite("Strict")
                .build();
    }

    /**
     * 쿠키 maxAge 설정을 위해 외부에서 초 단위 만료시간을 조회할 수도 있도록.
     */
    public long getRefreshTokenMaxAgeSeconds() {
        return refreshValidityInMilliseconds / 1000;
    }

}
