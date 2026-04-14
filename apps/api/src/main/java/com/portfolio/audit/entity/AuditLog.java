package com.portfolio.audit.entity;

import com.portfolio.audit.enums.AuditAction;
import com.portfolio.audit.enums.AuditEntityType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Değiştirilemez olay kaydı.
 *
 * Tasarım kararları:
 * - @Setter yok: kayıt oluşturulduktan sonra hiçbir alan değiştirilemez.
 * - NoArgsConstructor(PROTECTED): JPA proxy için gerekli, dışarıdan new yapılamaz.
 * - Factory method {@link #of}: tüm alanlar tek seferde set edilir.
 * - @PrePersist yoktur: createdAt factory metodunda set edilir, persist öncesinde kesin değer taşır.
 *
 * Güvenlik notu:
 *   Şifre, token veya kişisel kimlik bilgisi (TC, pasaport no)
 *   hiçbir zaman bu entity'e set edilmemelidir.
 */
@Entity
@Table(name = "audit_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Aksiyonu gerçekleştiren admin e-postası. Anonim işlemlerde NULL. */
    @Column(name = "actor_email", length = 255)
    private String actorEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    private AuditAction action;

    /** Etkilenen domain nesnesi türü. Yok ise NULL. */
    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", length = 100)
    private AuditEntityType entityType;

    /** Etkilenen domain nesnesinin PK'sı. Yok ise NULL. */
    @Column(name = "entity_id")
    private Long entityId;

    /** İstemci IPv4/IPv6 adresi. */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /** HTTP User-Agent başlığı. Opsiyonel, cihaz/tarayıcı analizi için. */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /**
     * Aksiyon bazlı ek bilgi — JSON string.
     * Örn: {"title":"Salon Projesi","category":"RESIDENTIAL"}
     *
     * ASLA şifre, token veya hassas kişisel veri içermemeli.
     */
    @Column(columnDefinition = "TEXT")
    private String metadata;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // ── Factory method ────────────────────────────────────────────────────

    public static AuditLog of(
            AuditAction action,
            AuditEntityType entityType,
            Long entityId,
            String actorEmail,
            String ipAddress,
            String userAgent,
            String metadata
    ) {
        AuditLog log = new AuditLog();
        log.action      = action;
        log.entityType  = entityType;
        log.entityId    = entityId;
        log.actorEmail  = actorEmail;
        log.ipAddress   = ipAddress;
        log.userAgent   = userAgent;
        log.metadata    = metadata;
        log.createdAt   = Instant.now();
        return log;
    }
}
