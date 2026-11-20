package com.example.Gym_Management_System.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey key;
    private final long accessTokenMinutes;

    public JwtService(@Value("${gym.jwt.secret}") String secret,
                      @Value("${gym.jwt.access-token-minutes:30}") long accessTokenMinutes) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("gym.jwt.secret must be at least 32 characters");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
        this.accessTokenMinutes = accessTokenMinutes;
    }

    public String generateToken(SecurityUser user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenMinutes * 60_000);

        return Jwts.builder()
                .subject(user.getUsername())
                .claim("uid", user.getId())
                .claim("role", user.getRole())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    // بترمي JwtException لو التوكن مزوّر أو منتهي أو بايظ
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long getExpiresInSeconds() {
        return accessTokenMinutes * 60;
    }
}