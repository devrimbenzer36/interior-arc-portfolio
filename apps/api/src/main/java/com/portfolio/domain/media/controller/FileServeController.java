package com.portfolio.domain.media.controller;

import com.portfolio.common.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Local storage modunda yüklenen dosyaları serve eder.
 * Phase 2'de S3 kullanıldığında bu controller devre dışı kalır.
 *
 * GET /files/{filename} → upload dizininden dosyayı döner.
 *
 * Güvenlik: filename doğrudan path'e yazılmaz,
 * Paths.get ile normalize edilerek upload dizini dışına çıkış engellenir.
 */
@Slf4j
@RestController
@RequestMapping("/files")
@ConditionalOnProperty(name = "app.storage.type", havingValue = "local", matchIfMissing = true)
public class FileServeController {

    private final Path uploadDir;

    public FileServeController(
            @Value("${app.storage.local.upload-dir}") String uploadDir
    ) {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        // Path traversal koruması
        if (filename.contains("..") || filename.contains("/")) {
            throw ResourceNotFoundException.of("File", filename);
        }

        try {
            Path filePath = uploadDir.resolve(filename).normalize();

            if (!filePath.startsWith(uploadDir)) {
                throw ResourceNotFoundException.of("File", filename);
            }

            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw ResourceNotFoundException.of("File", filename);
            }

            String contentType = resolveContentType(filename);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .header(HttpHeaders.CACHE_CONTROL, "public, max-age=31536000")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);

        } catch (MalformedURLException e) {
            log.warn("Malformed file URL for: {}", filename);
            throw ResourceNotFoundException.of("File", filename);
        }
    }

    private String resolveContentType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".gif")) return "image/gif";
        return "application/octet-stream";
    }
}
