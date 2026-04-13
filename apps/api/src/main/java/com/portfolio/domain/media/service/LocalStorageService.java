package com.portfolio.domain.media.service;

import com.portfolio.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

/**
 * StorageService'in local filesystem implementasyonu.
 *
 * application.yml'de app.storage.type=local olduğunda aktif olur.
 * Phase 2'de app.storage.type=s3 yapılırsa bu bean devre dışı kalır,
 * S3StorageService devreye girer — başka hiçbir şey değişmez.
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "app.storage.type", havingValue = "local", matchIfMissing = true)
public class LocalStorageService implements StorageService {

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif"
    );

    private final Path uploadDir;
    private final String baseUrl;

    public LocalStorageService(
            @Value("${app.storage.local.upload-dir}") String uploadDir,
            @Value("${app.storage.local.base-url}") String baseUrl
    ) {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.baseUrl = baseUrl;
        createUploadDirectory();
    }

    @Override
    public String upload(MultipartFile file) {
        validateFile(file);

        String storedName = generateStoredName(file.getOriginalFilename());
        Path targetPath = resolveSecurePath(storedName);

        try {
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("File uploaded: {}", storedName);
            return storedName;
        } catch (IOException e) {
            log.error("File upload failed: {}", storedName, e);
            throw new BusinessException("File could not be saved", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void delete(String storedName) {
        Path filePath = resolveSecurePath(storedName);
        try {
            boolean deleted = Files.deleteIfExists(filePath);
            if (deleted) {
                log.info("File deleted: {}", storedName);
            } else {
                log.warn("File not found for deletion: {}", storedName);
            }
        } catch (IOException e) {
            log.error("File deletion failed: {}", storedName, e);
            throw new BusinessException("File could not be deleted", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public String getUrl(String storedName) {
        return baseUrl + "/" + storedName;
    }

    // ── Private helpers ────────────────────────────────────────────────────

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("File must not be empty");
        }

        String mimeType = file.getContentType();
        if (mimeType == null || !ALLOWED_MIME_TYPES.contains(mimeType.toLowerCase())) {
            throw new BusinessException(
                    "File type not allowed. Allowed types: " + ALLOWED_MIME_TYPES
            );
        }
    }

    /**
     * UUID tabanlı benzersiz dosya adı üretir.
     * Orijinal dosya adındaki uzantıyı alır, adı tamamen değiştirir.
     * Orijinal ad filesystem'e hiçbir zaman yazılmaz.
     */
    private String generateStoredName(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            String ext = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            // Sadece güvenli uzantılara izin ver
            if (Set.of(".jpg", ".jpeg", ".png", ".webp", ".gif").contains(ext)) {
                extension = ext;
            }
        }
        return UUID.randomUUID() + extension;
    }

    /**
     * Path traversal saldırısını engeller.
     *
     * storedName içinde ".." veya "/" varsa ya da
     * normalize edilmiş path upload dizini dışına çıkıyorsa reddeder.
     */
    private Path resolveSecurePath(String storedName) {
        if (storedName == null || storedName.contains("..") || storedName.contains("/")) {
            throw new BusinessException("Invalid file name");
        }

        Path resolved = uploadDir.resolve(storedName).normalize();

        if (!resolved.startsWith(uploadDir)) {
            throw new BusinessException("Invalid file path");
        }

        return resolved;
    }

    private void createUploadDirectory() {
        try {
            Files.createDirectories(uploadDir);
            log.info("Upload directory ready: {}", uploadDir);
        } catch (IOException e) {
            throw new IllegalStateException("Could not create upload directory: " + uploadDir, e);
        }
    }
}
