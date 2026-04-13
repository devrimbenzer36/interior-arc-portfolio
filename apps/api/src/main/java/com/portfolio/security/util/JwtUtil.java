package com.portfolio.security.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT oluşturma, parse ve doğrulama işlemleri.
 *
 * Algorithm : HS256 (HMAC + SHA-256)
 * Claims    : sub (email), role, userId, iat, exp
 *
 * Güvenlik notları:
 * - JJWT 0.12.x, Keys.hmacShaKeyFor() ile HS256 için min 256-bit (32 byte)
 *   key zorunlu kılar. Kısa key → WeakKeyException → uygulama başlamaz.
 * - Dev default secret kullanılıyorsa startup'ta WARN log üretilir.
 * - isTokenValid, claims'i tek parse ile doğrular — double-parse yok.
 */
@Slf4j
@Component
public class JwtUtil {

    private static final String DEV_SECRET_PREFIX = "dev-secret-key";

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtUtil(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs
    ) {
        if (secret.startsWith(DEV_SECRET_PREFIX)) {
            log.warn("JWT is using the dev default secret. " +
                     "Set the APP_JWT_SECRET environment variable before deploying to production!");
        }
        // Keys.hmacShaKeyFor throws WeakKeyException if key < 256 bits — fast-fail at startup
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    // ── Token üretimi ─────────────────────────────────────────────────────

    public String generateToken(Long userId, String email, String role) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .claim("role", role)
                .issuedAt(new Date(now))
                .expiration(new Date(now + expirationMs))
                .signWith(signingKey)
                .compact();
    }

    // ── Token okuma ───────────────────────────────────────────────────────

    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    public Long extractUserId(String token) {
        return parseClaims(token).get("userId", Long.class);
    }

    // ── Doğrulama ─────────────────────────────────────────────────────────

    /**
     * Token geçerliyse true döner.
     *
     * Claims tek seferinde parse edilir — önceki double-parse hatası giderildi.
     * Kontroller: imza geçerliliği (parseClaims içinde), email eşleşmesi, expiration.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            Claims claims = parseClaims(token);
            String email = claims.getSubject();
            Date expiration = claims.getExpiration();
            return email.equals(userDetails.getUsername()) && expiration.after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
