package com.swifttrack.AuthService.util;

import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;
import org.springframework.http.HttpStatus;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import com.swifttrack.exception.CustomException;

@Component
public class JwtUtil {
    private static final long EXPIRATION_TIME = 1000 * 60 * 450; // 7.5 hours
    private static final String SECRET = "GpwEjc0vvHsjtisOgFIZ2tN7MZz2piuEM3QMKIdZ6Oo=";

    private SecretKey getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(UUID id, String mobile) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", id);
        claims.put("mobile", mobile);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(mobile)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Map<String, Object> decodeToken(String token) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(SECRET);
            SecretKey key = Keys.hmacShaKeyFor(keyBytes);

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            Map<String, Object> extracted = new HashMap<>();
            extracted.put("userId", claims.get("userId"));
            extracted.put("mobile", claims.get("mobile"));
            return extracted;
        } catch (Exception e) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        }
    }
}