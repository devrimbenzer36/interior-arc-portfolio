package com.portfolio.domain.project.enums;

/**
 * Projenin yayın durumu.
 *
 * DRAFT     → Taslak. Sadece admin görebilir.
 * PUBLISHED → Yayında. Ziyaretçiler görebilir.
 */
public enum ProjectStatus {
    DRAFT,
    PUBLISHED
}
