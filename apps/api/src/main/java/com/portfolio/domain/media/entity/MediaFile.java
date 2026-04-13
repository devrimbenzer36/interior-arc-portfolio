package com.portfolio.domain.media.entity;

import com.portfolio.domain.media.enums.StorageType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * Yüklenen medya dosyalarının metadata kaydı.
 *
 * Dosyanın fiziksel içeriği burada tutulmaz — sadece nerede olduğu ve
 * nasıl erişileceği bilgisi saklanır.
 *
 * storedName: UUID tabanlı benzersiz dosya adı (path traversal'a karşı)
 * url:        storage_type'a göre relative path (LOCAL) veya S3 key
 * width/height: sadece görseller için dolu, diğer tipler null
 */
@Entity
@Table(name = "media_files")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = {})
public class MediaFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_name", nullable = false, length = 255)
    private String originalName;

    @Column(name = "stored_name", nullable = false, unique = true, length = 255)
    private String storedName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String url;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    @Column(nullable = false)
    private Long size;

    // Sadece görsel dosyalar için — diğerleri null
    @Column
    private Integer width;

    @Column
    private Integer height;

    @Enumerated(EnumType.STRING)
    @Column(name = "storage_type", nullable = false, length = 20)
    private StorageType storageType;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // ── Factory method — new keyword yerine bu kullanılır ────────────────

    public static MediaFile of(
            String originalName,
            String storedName,
            String url,
            String mimeType,
            Long size,
            StorageType storageType
    ) {
        MediaFile file = new MediaFile();
        file.originalName = originalName;
        file.storedName = storedName;
        file.url = url;
        file.mimeType = mimeType;
        file.size = size;
        file.storageType = storageType;
        return file;
    }

    // ── Görsel boyutlarını sonradan set etmek için ────────────────────────

    public void setDimensions(int width, int height) {
        this.width = width;
        this.height = height;
    }
}
