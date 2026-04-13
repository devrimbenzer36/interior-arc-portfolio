package com.portfolio.domain.media.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Dosya depolama işlemleri için abstraction.
 *
 * Phase 1: LocalStorageService (local filesystem)
 * Phase 2: S3StorageService   (AWS S3 / Cloudflare R2 / MinIO)
 *
 * Implementasyon seçimi application.yml'deki app.storage.type
 * değerine göre @Profile veya @ConditionalOnProperty ile yapılır.
 */
public interface StorageService {

    /**
     * Dosyayı storage backend'ine yükler.
     *
     * @param file yüklenecek dosya
     * @return storedName — UUID tabanlı benzersiz dosya adı.
     *         MediaFile.storedName alanına kaydedilir.
     *         getUrl() ile erişim URL'ine çevrilir.
     */
    String upload(MultipartFile file);

    /**
     * Dosyayı storage backend'inden siler.
     *
     * @param storedName MediaFile.storedName değeri
     */
    void delete(String storedName);

    /**
     * storedName değerinden erişim URL'i üretir.
     *
     * LOCAL → http://localhost:8080/files/{storedName}
     * S3    → https://bucket.s3.region.amazonaws.com/{storedName}
     *
     * @param storedName MediaFile.storedName değeri
     * @return tam erişim URL'i
     */
    String getUrl(String storedName);
}
