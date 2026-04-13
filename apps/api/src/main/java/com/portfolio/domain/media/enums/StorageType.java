package com.portfolio.domain.media.enums;

/**
 * Dosyanın hangi storage backend'inde saklandığını belirtir.
 *
 * LOCAL  → Phase 1: sunucunun local dosya sistemi
 * S3     → Phase 2: AWS S3 veya S3-uyumlu servisler (MinIO, R2 vb.)
 */
public enum StorageType {
    LOCAL,
    S3
}
