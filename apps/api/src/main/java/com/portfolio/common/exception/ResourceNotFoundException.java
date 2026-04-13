package com.portfolio.common.exception;

/**
 * 404 — İstenen kaynak bulunamadı.
 * Örn: Project.findBySlug(), MediaFile.findById()
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException of(String resource, Object identifier) {
        return new ResourceNotFoundException(
                String.format("%s not found: %s", resource, identifier)
        );
    }
}