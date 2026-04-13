package com.portfolio.domain.admin.dto.response;

/**
 * Login başarılıysa dönen token bilgisi.
 *
 * expiresIn: token ömrü saniye cinsinden (client-side expiry takibi için)
 */
public record LoginResponse(
        String token,
        String tokenType,
        long expiresIn,
        String email,
        String role
) {
    public static LoginResponse of(String token, long expiresInMs, String email, String role) {
        return new LoginResponse(token, "Bearer", expiresInMs / 1000, email, role);
    }
}