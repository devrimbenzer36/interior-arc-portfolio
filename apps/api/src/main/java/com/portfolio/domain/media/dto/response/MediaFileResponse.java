package com.portfolio.domain.media.dto.response;

import com.portfolio.domain.media.enums.StorageType;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * Yüklenen medya dosyası response DTO'su.
 */
@Getter
@Builder
public class MediaFileResponse {

    private Long id;
    private String originalName;
    private String url;
    private String mimeType;
    private Long size;
    private Integer width;
    private Integer height;
    private StorageType storageType;
    private Instant createdAt;
}
