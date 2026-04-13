package com.portfolio.domain.media.controller;

import com.portfolio.common.response.ApiResponse;
import com.portfolio.domain.media.dto.response.MediaFileResponse;
import com.portfolio.domain.media.service.MediaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/admin/media")
@RequiredArgsConstructor
@Tag(name = "Media", description = "Media file upload and management")
public class MediaController {

    private final MediaService mediaService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "[Admin] Upload media file")
    public ResponseEntity<ApiResponse<MediaFileResponse>> upload(
            @RequestPart("file") MultipartFile file
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(mediaService.upload(file), "File uploaded"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "[Admin] Get media file info")
    public ResponseEntity<ApiResponse<MediaFileResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(mediaService.getById(id)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "[Admin] Delete media file")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        mediaService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("File deleted"));
    }
}
