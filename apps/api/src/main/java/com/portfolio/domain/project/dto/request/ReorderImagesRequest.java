package com.portfolio.domain.project.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Galeri görsellerini yeniden sıralama isteği.
 *
 * imageIds: yeni sırayla image id listesi.
 * Liste uzunluğu projenin mevcut image sayısıyla eşleşmeli —
 * bu kontrol service katmanında yapılır.
 */
@Getter
@NoArgsConstructor
public class ReorderImagesRequest {

    @NotEmpty(message = "Image ID list must not be empty")
    private List<Long> imageIds;
}
