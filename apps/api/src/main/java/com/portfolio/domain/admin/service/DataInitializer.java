package com.portfolio.domain.admin.service;

import com.portfolio.domain.admin.entity.AdminRole;
import com.portfolio.domain.admin.entity.AdminUser;
import com.portfolio.domain.admin.repository.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Uygulama ilk başladığında admin kullanıcı yoksa oluşturur.
 *
 * İdempotent: her zaman çalışır ama kayıt zaten varsa atlar.
 * Şifre: application.yml → app.admin.initial-password
 *        (env: APP_ADMIN_PASSWORD)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.initial-email}")
    private String initialEmail;

    @Value("${app.admin.initial-password}")
    private String initialPassword;

    @Override
    public void run(ApplicationArguments args) {
        if (initialEmail == null || initialEmail.isBlank()) {
            throw new IllegalStateException(
                "APP_ADMIN_EMAIL environment variable is not set. " +
                "Application cannot start without an admin email."
            );
        }
        if (initialPassword == null || initialPassword.isBlank()) {
            throw new IllegalStateException(
                "APP_ADMIN_PASSWORD environment variable is not set. " +
                "Application cannot start without an admin password."
            );
        }

        if (adminUserRepository.findByEmail(initialEmail).isEmpty()) {
            AdminUser admin = new AdminUser();
            admin.setEmail(initialEmail);
            admin.setPasswordHash(passwordEncoder.encode(initialPassword));
            admin.setFullName("Admin");
            admin.setRole(AdminRole.ADMIN);
            admin.setActive(true);
            adminUserRepository.save(admin);
            log.info("Initial admin user created: {}", initialEmail);
        } else {
            log.debug("Admin user already exists, skipping initialization.");
        }
    }
}