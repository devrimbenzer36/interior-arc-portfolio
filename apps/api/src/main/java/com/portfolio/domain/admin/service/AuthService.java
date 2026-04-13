package com.portfolio.domain.admin.service;

import com.portfolio.common.exception.BusinessException;
import com.portfolio.domain.admin.dto.request.LoginRequest;
import com.portfolio.domain.admin.dto.response.LoginResponse;
import com.portfolio.domain.admin.entity.AdminUser;
import com.portfolio.domain.admin.repository.AdminUserRepository;
import com.portfolio.security.util.JwtUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Admin authentication servisi.
 *
 * Güvenlik notları:
 *
 * 1. Timing attack koruması: kullanıcı bulunamasa bile BCrypt.matches() her zaman
 *    çalıştırılır (dummy hash ile). Böylece response süresi email'in varlığından
 *    bağımsız olur — geçerli email enumerate edilemez.
 *
 * 2. Belirsiz hata mesajı: email mi yanlış, şifre mi yanlış belli değil.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    /**
     * Startup'ta üretilen dummy BCrypt hash.
     * Kullanıcı bulunamadığında sahte bir karşılaştırma yaparak response süresini sabit tutar.
     */
    private String dummyHash;

    @PostConstruct
    void init() {
        dummyHash = passwordEncoder.encode("timing-attack-prevention-dummy");
    }

    public LoginResponse login(LoginRequest request) {
        Optional<AdminUser> adminOpt = adminUserRepository.findByEmail(request.email());

        // Kullanıcı yoksa dummy hash ile karşılaştır — timing saldırısını engeller
        String hashToCheck = adminOpt.map(AdminUser::getPasswordHash).orElse(dummyHash);
        boolean passwordMatches = passwordEncoder.matches(request.password(), hashToCheck);

        if (adminOpt.isEmpty() || !passwordMatches || !adminOpt.get().isActive()) {
            log.warn("Failed login attempt for email: {}", request.email());
            throw invalidCredentials();
        }

        AdminUser admin = adminOpt.get();
        String token = jwtUtil.generateToken(admin.getId(), admin.getEmail(), admin.getRole().name());
        log.info("Admin logged in: {}", admin.getEmail());

        return LoginResponse.of(token, expirationMs, admin.getEmail(), admin.getRole().name());
    }

    private BusinessException invalidCredentials() {
        return new BusinessException("Invalid email or password", HttpStatus.UNAUTHORIZED);
    }
}
