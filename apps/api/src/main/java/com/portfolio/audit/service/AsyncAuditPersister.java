package com.portfolio.audit.service;

import com.portfolio.audit.entity.AuditLog;
import com.portfolio.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Audit log'u ayrı bir thread pool'da async olarak DB'ye yazar.
 *
 * Neden ayrı bir bean?
 *   Spring @Async, yalnızca Spring proxy üzerinden yapılan harici çağrılarda çalışır.
 *   AuditLogService içindeki Builder, 'this' (gerçek bean) referansını tuttuğundan
 *   aynı bean'deki persist() çağrısı proxy'yi bypass eder ve sync çalışır.
 *   Bu sınıfı AuditLogService'e inject edince Spring proxy garantilenir.
 *
 * Kural: Yalnızca AuditLogService.Builder tarafından çağrılmalıdır.
 */
@Slf4j
@Component
@RequiredArgsConstructor
class AsyncAuditPersister {

    private final AuditLogRepository repository;

    @Async("auditExecutor")
    public void persist(AuditLog entry) {
        try {
            repository.save(entry);
        } catch (Exception e) {
            log.error("Audit log could not be persisted: action={}, ip={}, error={}",
                    entry.getAction(), entry.getIpAddress(), e.getMessage());
        }
    }
}
