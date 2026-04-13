package com.portfolio.domain.project.dto.request;

import com.portfolio.domain.project.enums.ProjectCategory;
import com.portfolio.domain.project.enums.SpaceType;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Yeni proje oluşturma isteği.
 *
 * slug boş gelirse service katmanı title'dan otomatik üretir.
 *
 * coverImageId  → opsiyonel, proje oluşturulurken kapak görseli bağlanır.
 * imageIds      → opsiyonel, gönderilen sıra display_order olur (GALLERY tipi).
 *                 Sonradan addImage endpoint'i ile de eklenebilir.
 */
@Getter
@NoArgsConstructor
public class CreateProjectRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @Size(max = 255, message = "Slug must not exceed 255 characters")
    @Pattern(regexp = "^[a-z0-9-]*$", message = "Slug may only contain lowercase letters, numbers and hyphens")
    private String slug;

    @Size(max = 500, message = "Short description must not exceed 500 characters")
    private String shortDesc;

    private String detailedStory;

    @NotNull(message = "Category is required")
    private ProjectCategory category;

    @Size(max = 100, message = "Style must not exceed 100 characters")
    private String style;

    private SpaceType spaceType;

    @Size(max = 255, message = "Location must not exceed 255 characters")
    private String location;

    private LocalDate projectDate;

    @DecimalMin(value = "0.0", inclusive = false, message = "Square meters must be positive")
    @Digits(integer = 8, fraction = 2, message = "Invalid square meters value")
    private BigDecimal squareMeters;

    @Size(max = 100, message = "Budget range must not exceed 100 characters")
    private String budgetRange;

    private Long coverImageId;

    @NotNull(message = "Image IDs list must not be null")
    private List<Long> imageIds = new ArrayList<>();

    @NotNull(message = "Tags list must not be null")
    private List<String> tags = new ArrayList<>();

    @NotNull(message = "Materials list must not be null")
    private List<String> materials = new ArrayList<>();
}