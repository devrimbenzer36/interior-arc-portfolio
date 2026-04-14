package com.portfolio.audit.enums;

/**
 * Sistemde izlenen tüm aksiyonlar.
 *
 * Adlandırma kuralı: {DOMAIN}_{VERB}
 * Yeni aksiyon eklerken DB'deki string uzunluğunu (VARCHAR 100) aşmamalı.
 */
public enum AuditAction {

    // ── Auth ──────────────────────────────────────────────────
    LOGIN_SUCCESS,
    LOGIN_FAILURE,
    LOGIN_BLOCKED,          // Rate limit — çok fazla başarısız deneme

    // ── Proje ─────────────────────────────────────────────────
    PROJECT_CREATED,
    PROJECT_UPDATED,
    PROJECT_DELETED,
    PROJECT_PUBLISHED,
    PROJECT_UNPUBLISHED,

    // ── İletişim ──────────────────────────────────────────────
    CONTACT_FORM_SUBMITTED,
    CONTACT_MESSAGE_READ,
    CONTACT_MESSAGE_DELETED,

    // ── Medya ─────────────────────────────────────────────────
    MEDIA_UPLOADED,
    MEDIA_DELETED
}
