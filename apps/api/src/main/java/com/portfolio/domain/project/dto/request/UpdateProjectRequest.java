package com.portfolio.domain.project.dto.request;

import com.portfolio.domain.project.enums.ProjectCategory;
import com.portfolio.domain.project.enums.SpaceType;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Proje güncelleme isteği — PATCH semantiği.
 *
 * null alan → mevcut değer korunur, o alan dokunulmaz.
 * null olmayan alan → o alan güncellenir.
 *
 * tags / materials için özel kural:
 *   null  → mevcut liste korunur
 *   []    → tüm etiketler / malzemeler silinir
 *   [...] → gönderilen liste ile replace edilir
 *
 * Görsel yönetimi (kapak görseli, galeri sırası) → kendi endpoint'leri üzerinden.
 */
@Getter
@NoArgsConstructor
public class UpdateProjectRequest {

    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @Size(max = 255, message = "Slug must not exceed 255 characters")
    @Pattern(regexp = "^[a-z0-9-]*$", message = "Slug may only contain lowercase letters, numbers and hyphens")
    private String slug;

    @Size(max = 500, message = "Short description must not exceed 500 characters")
    private String shortDesc;

    private String detailedStory;

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

    private List<String> tags;

    private List<String> materials;
}