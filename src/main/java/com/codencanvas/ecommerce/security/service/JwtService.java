package com.codencanvas.ecommerce.security.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.codencanvas.ecommerce.user.UserPrincipal;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.access-token-expiration}")
    private long accessTokenExpiration;

    private final RedisTemplate<String, String> redisTemplate;

    // ==========================================
    // Access Token — JWT
    // ==========================================

    public String generateAccessToken(UserPrincipal principal) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", principal.getUser().getRole().name());
        claims.put("userId", principal.getUserId());
        return buildToken(claims, principal.getUsername(), accessTokenExpiration);
    }

    private String buildToken(Map<String, Object> claims,
            String subject,
            long expiration) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    // ==========================================
    // Refresh Token — Random String
    // ==========================================

    public String generateRawRefreshToken() {
        return UUID.randomUUID().toString() + UUID.randomUUID().toString();
    }

    public String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(
                    rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    // ==========================================
    // Claims Extraction
    // ==========================================

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extractAllClaims(token));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // ==========================================
    // Blacklist — Access Token في Redis
    // ==========================================

    public void blacklistToken(String accessToken) {
        try {
            Date expiration = extractClaim(accessToken, Claims::getExpiration);
            long ttl = expiration.getTime() - System.currentTimeMillis();

            if (ttl > 0) {
                redisTemplate.opsForValue().set(
                        "blacklist:" + accessToken,
                        "true",
                        ttl,
                        TimeUnit.MILLISECONDS);

            }
        } catch (JwtException e) {

        }
    }

    // ==========================================
    // Signing Key
    // ==========================================

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
