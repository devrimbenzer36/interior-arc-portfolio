package com.portfolio.domain.project.dto.response;

import com.portfolio.domain.project.enums.ProjectCategory;
import com.portfolio.domain.project.enums.ProjectStatus;
import com.portfolio.domain.project.enums.SpaceType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Tek proje detay sayfası için tam response.
 * Tüm alanları, galeri görselleri, etiketler ve malzemeleri içerir.
 */
@Getter
@Builder
public class ProjectDetailResponse {

    private Long id;
    private String title;
    private String slug;
    private String shortDesc;
    private String detailedStory;
    private ProjectCategory category;
    private String style;
    private SpaceType spaceType;
    private String location;
    private LocalDate projectDate;
    private BigDecimal squareMeters;
    private String budgetRange;
    private ProjectStatus status;
    private Boolean featured;
    private Integer viewCount;
    private String coverImageUrl;
    private List<ProjectImageResponse> images;
    private List<String> tags;
    private List<String> materials;
    private Instant createdAt;
    private Instant updatedAt;
}
