package com.portfolio.domain.project.entity;

import com.portfolio.domain.media.entity.MediaFile;
import com.portfolio.domain.project.enums.ImageType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * Projeye ait galeri görseli.
 *
 * displayOrder ile admin sıralama yapabilir.
 * type ile GALLERY / BEFORE_AFTER ayrımı tutulur.
 * Kapak görseli burada değil, Project.coverImage üzerinden yönetilir.
 */
@Entity
@Table(name = "project_images")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    // RESTRICT: MediaFile silinmek istenirse önce image kaydı silinmeli
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "media_file_id", nullable = false)
    private MediaFile mediaFile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ImageType type = ImageType.GALLERY;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @Column(name = "alt_text", length = 255)
    private String altText;

    @Column(columnDefinition = "TEXT")
    private String caption;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // ── Factory method ─────────────────────────────────────────────────────

    public static ProjectImage of(Project project, MediaFile mediaFile, ImageType type, int displayOrder) {
        ProjectImage image = new ProjectImage();
        image.project = project;
        image.mediaFile = mediaFile;
        image.type = type;
        image.displayOrder = displayOrder;
        return image;
    }

    public void updateOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public void updateMeta(String altText, String caption) {
        this.altText = altText;
        this.caption = caption;
    }
}
