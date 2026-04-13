package com.portfolio.domain.project.dto.response;

import com.portfolio.domain.project.enums.ImageType;
import lombok.Builder;
import lombok.Getter;

/**
 * Galeri görseli response DTO'su.
 */
@Getter
@Builder
public class ProjectImageResponse {

    private Long id;
    private String url;
    private ImageType type;
    private Integer displayOrder;
    private String altText;
    private String caption;
    private Integer width;
    private Integer height;
}
