package com.portfolio.domain.project.dto.response;

import com.portfolio.domain.project.enums.ProjectCategory;
import com.portfolio.domain.project.enums.ProjectStatus;
import com.portfolio.domain.project.enums.SpaceType;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Proje listesi için özet response.
 * detailedStory, images, tags, materials gibi ağır alanlar dahil edilmez.
 * Sayfalı liste endpoint'lerinde kullanılır.
 */
@Getter
@Builder
public class ProjectSummaryResponse {

    private Long id;
    private String title;
    private String slug;
    private String shortDesc;
    private ProjectCategory category;
    private SpaceType spaceType;
    private String location;
    private LocalDate projectDate;
    private ProjectStatus status;
    private Boolean featured;
    private String coverImageUrl;
    private Instant createdAt;
}
