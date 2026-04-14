package com.portfolio.audit.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.audit.entity.AuditLog;
import com.portfolio.audit.enums.AuditAction;
import com.portfolio.audit.enums.AuditEntityType;
import com.portfolio.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Audit log servisi.
 *
 * Kullanım (fluent builder):
 * <pre>{@code
 *   auditLog.record(AuditAction.PROJECT_CREATED)
 *       .entity(AuditEntityType.PROJECT, project.getId())
 *       .actor(currentAdminEmail())
 *       .ip(ipAddress)
 *       .meta("title", project.getTitle())
 *       .save();
 * }</pre>
 *
 * Async davranış:
 *   {@code .save()} çağrısı {@code persist()} metodunu tetikler.
 *   persist() {@code @Async("auditExecutor")} ile işaretlidir:
 *   ayrı bir thread pool'da, ana iş akışını bloke etmeden çalışır.
 *   DB yazma hatası ana akışı etkilemez — sadece error log üretilir.
 *
 * Önemli:
 *   .save() her zaman servis metodu başarıyla döndükten SONRA çağrılmalı.
 *   Başarısız işlemler loglanmamalıdır (transaction rollback sonrasında
 *   audit kaydı kalması yanıltıcıdır).
 *   İstisna: LOGIN_FAILURE gibi kasıtlı başarısızlık olayları loglanır.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository repository;
    private final ObjectMapper objectMapper;
    private final AsyncAuditPersister persister;  // ayrı bean → @Async proxy garantili

    // ── Public API ────────────────────────────────────────────────────────

    /**
     * Fluent builder başlatır.
     *
     * @param action loglanacak aksiyon — zorunlu
     */
    public Builder record(AuditAction action) {
        return new Builder(persister, objectMapper, action);
    }

    // ── Sorgular (admin paneli) ───────────────────────────────────────────

    public Page<AuditLog> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<AuditLog> findByActor(String email, Pageable pageable) {
        return repository.findByActorEmailOrderByCreatedAtDesc(email, pageable);
    }

    public Page<AuditLog> findByAction(AuditAction action, Pageable pageable) {
        return repository.findByActionOrderByCreatedAtDesc(action, pageable);
    }

    public List<AuditLog> findInRange(Instant from, Instant to) {
        return repository.findByCreatedAtBetweenOrderByCreatedAtDesc(from, to);
    }

    // ── Fluent Builder ────────────────────────────────────────────────────

    /**
     * Thread-safe değildir. Her çağrıda yeni Builder üretilir.
     * {@code .save()} ile tamamlanmalıdır — aksi halde log yazılmaz.
     */
    public static final class Builder {

        private final AsyncAuditPersister persister;
        private final ObjectMapper objectMapper;
        private final AuditAction action;
        private AuditEntityType entityType;
        private Long entityId;
        private String actorEmail;
        private String ipAddress;
        private String userAgent;
        private Map<String, Object> meta;

        Builder(AsyncAuditPersister persister, ObjectMapper objectMapper, AuditAction action) {
            this.persister     = persister;
            this.objectMapper  = objectMapper;
            this.action        = action;
        }

        /** Etkilenen domain nesnesi */
        public Builder entity(AuditEntityType type, Long id) {
            this.entityType = type;
            this.entityId   = id;
            return this;
        }

        /** Aksiyonu gerçekleştiren admin e-postası. Anonim ise çağrılmaz. */
        public Builder actor(String email) {
            this.actorEmail = email;
            return this;
        }

        /** İstemci IP adresi */
        public Builder ip(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        /** HTTP User-Agent (opsiyonel) */
        public Builder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        /**
         * Aksiyon bazlı ek bilgi.
         * Çağrılar zincirlenebilir: .meta("title", x).meta("category", y)
         *
         * ASLA şifre veya token eklenmemelidir.
         */
        public Builder meta(String key, Object value) {
            if (this.meta == null) this.meta = new LinkedHashMap<>();
            this.meta.put(key, value);
            return this;
        }

        /**
         * Logu async olarak kaydeder.
         * Bu metod çağrılmazsa hiçbir şey yazılmaz.
         */
        public void save() {
            String metaJson = serializeMeta();
            AuditLog entry  = AuditLog.of(
                    action, entityType, entityId,
                    actorEmail, ipAddress, userAgent, metaJson
            );
            // persister ayrı @Component bean'i — Spring proxy üzerinden çağrılır → @Async çalışır
            persister.persist(entry);
        }

        private String serializeMeta() {
            if (meta == null || meta.isEmpty()) return null;
            try {
                return objectMapper.writeValueAsString(meta);
            } catch (JsonProcessingException e) {
                log.warn("Audit metadata serialization failed: {}", e.getMessage());
                return meta.toString();
            }
        }
    }
}
