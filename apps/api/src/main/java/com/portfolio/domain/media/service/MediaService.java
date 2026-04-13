package com.portfolio.domain.media.service;

import com.portfolio.common.exception.BusinessException;
import com.portfolio.common.exception.ResourceNotFoundException;
import com.portfolio.domain.media.dto.response.MediaFileResponse;
import com.portfolio.domain.media.entity.MediaFile;
import com.portfolio.domain.media.enums.StorageType;
import com.portfolio.domain.media.repository.MediaFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaService {

    private final StorageService storageService;
    private final MediaFileRepository mediaFileRepository;

    /**
     * Dosyayı storage'a yükler, metadata'yı DB'ye kaydeder.
     * Görsel ise width/height bilgisini okur.
     */
    @Transactional
    public MediaFileResponse upload(MultipartFile file) {
        String storedName = storageService.upload(file);
        String url = storageService.getUrl(storedName);

        MediaFile mediaFile = MediaFile.of(
                file.getOriginalFilename(),
                storedName,
                url,
                file.getContentType(),
                file.getSize(),
                StorageType.LOCAL
        );

        // Görsel boyutlarını oku — hata olursa yüklemeyi engelleme, sadece logla
        readImageDimensions(file, mediaFile);

        MediaFile saved = mediaFileRepository.save(mediaFile);
        log.info("Media file saved: id={}, storedName={}", saved.getId(), storedName);

        return toResponse(saved);
    }

    /**
     * Dosyayı storage'dan ve DB'den siler.
     * Projeye bağlı görseller silinemez (FK RESTRICT).
     * Bu kontrolü DB constraint sağlar — exception GlobalExceptionHandler'da yakalanır.
     */
    @Transactional
    public void delete(Long id) {
        MediaFile mediaFile = mediaFileRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("MediaFile", id));

        storageService.delete(mediaFile.getStoredName());
        mediaFileRepository.delete(mediaFile);
        log.info("Media file deleted: id={}", id);
    }

    @Transactional(readOnly = true)
    public MediaFileResponse getById(Long id) {
        return toResponse(mediaFileRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("MediaFile", id)));
    }

    // ── Private helpers ────────────────────────────────────────────────────

    private void readImageDimensions(MultipartFile file, MediaFile mediaFile) {
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image != null) {
                mediaFile.setDimensions(image.getWidth(), image.getHeight());
            }
        } catch (IOException e) {
            log.warn("Could not read image dimensions for: {}", file.getOriginalFilename());
        }
    }

    private MediaFileResponse toResponse(MediaFile f) {
        return MediaFileResponse.builder()
                .id(f.getId())
                .originalName(f.getOriginalName())
                .url(f.getUrl())
                .mimeType(f.getMimeType())
                .size(f.getSize())
                .width(f.getWidth())
                .height(f.getHeight())
                .storageType(f.getStorageType())
                .createdAt(f.getCreatedAt())
                .build();
    }
}
