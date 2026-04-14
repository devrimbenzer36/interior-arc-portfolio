package com.portfolio.audit.enums;

/**
 * Logun hangi domain nesnesiyle ilgili olduğunu belirtir.
 * entity_type + entity_id birlikte "kim etkilendi" sorusunu yanıtlar.
 *
 * Auth olayları gibi belirli bir nesneye bağlı olmayan aksiyonlar için
 * SYSTEM kullanılır ya da entityType null bırakılır.
 */
public enum AuditEntityType {
    PROJECT,
    CONTACT_MESSAGE,
    MEDIA_FILE,
    ADMIN_USER,
    SYSTEM
}
