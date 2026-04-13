package com.portfolio.domain.admin.controller;

import com.portfolio.common.exception.BusinessException;
import com.portfolio.common.response.ApiResponse;
import com.portfolio.domain.admin.dto.request.LoginRequest;
import com.portfolio.domain.admin.dto.response.LoginResponse;
import com.portfolio.domain.admin.service.AuthService;
import com.portfolio.security.ratelimit.RateLimiterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentication", description = "Admin login")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RateLimiterService rateLimiter;

    @Operation(summary = "Admin login — returns JWT token")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        String ip = httpRequest.getRemoteAddr();
        if (rateLimiter.isLoginBlocked(ip)) {
            throw new BusinessException(
                "Too many failed login attempts. Please try again in 15 minutes.",
                HttpStatus.TOO_MANY_REQUESTS
            );
        }
        try {
            LoginResponse response = authService.login(request);
            rateLimiter.clearLoginFailures(ip);
            return ResponseEntity.ok(ApiResponse.ok(response, "Login successful"));
        } catch (BusinessException e) {
            rateLimiter.recordLoginFailure(ip);
            throw e;
        }
    }
}