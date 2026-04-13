package com.portfolio.domain.contact.entity;

import com.portfolio.domain.contact.enums.ContactStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * Ziyaretçiden gelen iletişim formu mesajı.
 *
 * ipAddress: spam tespiti ve rate limiting için tutulur.
 * Mesaj içeriği hiçbir zaman değiştirilmez — sadece status güncellenir.
 */
@Entity
@Table(name = "contact_messages")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContactMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(length = 50)
    private String phone;

    @Column(length = 255)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ContactStatus status = ContactStatus.NEW;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // ── Factory method ─────────────────────────────────────────────────────

    public static ContactMessage of(
            String fullName,
            String email,
            String phone,
            String subject,
            String message,
            String ipAddress
    ) {
        ContactMessage cm = new ContactMessage();
        cm.fullName = fullName;
        cm.email = email;
        cm.phone = phone;
        cm.subject = subject;
        cm.message = message;
        cm.ipAddress = ipAddress;
        cm.status = ContactStatus.NEW;
        return cm;
    }

    // ── Domain method ──────────────────────────────────────────────────────

    public void updateStatus(ContactStatus status) {
        this.status = status;
    }
}
