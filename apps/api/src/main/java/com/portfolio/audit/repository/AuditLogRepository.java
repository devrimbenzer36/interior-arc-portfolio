package com.portfolio.audit.repository;

import com.portfolio.audit.entity.AuditLog;
import com.portfolio.audit.enums.AuditAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

/**
 * Audit log repository.
 *
 * Kasıtlı olarak delete / update metodu tanımlanmamıştır.
 * Loglar uygulama katmanından asla silinmez veya değiştirilmez.
 */
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    // ── Listeleme ─────────────────────────────────────────────────────────

    /** Belirli bir adminin tüm aksiyonları */
    Page<AuditLog> findByActorEmailOrderByCreatedAtDesc(String actorEmail, Pageable pageable);

    /** Belirli bir aksiyon türünün tüm kayıtları */
    Page<AuditLog> findByActionOrderByCreatedAtDesc(AuditAction action, Pageable pageable);

    /** Belirli zaman aralığındaki tüm loglar */
    List<AuditLog> findByCreatedAtBetweenOrderByCreatedAtDesc(Instant from, Instant to);

    // ── Güvenlik analizi ──────────────────────────────────────────────────

    /** Son N dakikadaki başarısız login sayısı (brute-force analizi) */
    long countByActionAndIpAddressAndCreatedAtAfter(
            AuditAction action,
            String ipAddress,
            Instant since
    );
}
