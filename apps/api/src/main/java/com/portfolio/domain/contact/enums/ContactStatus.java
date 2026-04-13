package com.portfolio.domain.contact.enums;

/**
 * İletişim mesajının durumu.
 *
 * NEW     → Yeni geldi, henüz okunmadı
 * READ    → Admin okudu
 * REPLIED → Yanıtlandı
 */
public enum ContactStatus {
    NEW,
    READ,
    REPLIED
}
