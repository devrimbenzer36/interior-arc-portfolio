package com.portfolio.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.time.Instant;

/**
 * Tüm API endpoint'lerinin döndüğü standart wrapper.
 *
 * Başarılı response:  { "success": true,  "data": {...} }
 * Hata response:      { "success": false, "message": "...", "errors": [...] }
 *
 * Generic T ile her türlü payload taşınabilir.
 * @JsonInclude ile null alanlar JSON'a dahil edilmez — temiz çıktı.
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final String message;
    private final Object errors;
    private final Instant timestamp;

    private ApiResponse(boolean success, T data, String message, Object errors) {
        this.success = success;
        this.data = data;
        this.message = message;
        this.errors = errors;
        this.timestamp = Instant.now();
    }

    // ── Başarılı response'lar ──────────────────────────────────────────────

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null, null);
    }

    public static <T> ApiResponse<T> ok(T data, String message) {
        return new ApiResponse<>(true, data, message, null);
    }

    public static ApiResponse<Void> ok(String message) {
        return new ApiResponse<>(true, null, message, null);
    }

    // ── Hata response'ları ─────────────────────────────────────────────────

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, null, message, null);
    }

    public static <T> ApiResponse<T> error(String message, Object errors) {
        return new ApiResponse<>(false, null, message, errors);
    }
}