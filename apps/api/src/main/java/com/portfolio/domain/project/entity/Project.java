package com.portfolio.domain.project.entity;

import com.portfolio.common.exception.BusinessException;
import com.portfolio.domain.media.entity.MediaFile;
import com.portfolio.domain.project.enums.ImageType;
import com.portfolio.domain.project.enums.ProjectCategory;
import com.portfolio.domain.project.enums.ProjectStatus;
import com.portfolio.domain.project.enums.SpaceType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Portfolyo projesinin aggregate root'u.
 *
 * İlişkiler:
 * - images, tags, materials → CascadeType.ALL, orphanRemoval=true
 *   Project silinince bunlar da silinir.
 * - coverImage → MediaFile'a FK, cascade yok.
 *   Medya dosyası bağımsız bir varlık — başka projeler de referans alabilir.
 *
 * Domain metodlar (publish, addImage, removeImageById vb.) iş kurallarını
 * entity içinde tutar — service katmanı persistence ve orchestration'a odaklanır.
 */
@Entity
@Table(name = "projects")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, unique = true, length = 255)
    private String slug;

    @Column(name = "short_desc", length = 500)
    private String shortDesc;

    @Column(name = "detailed_story", columnDefinition = "TEXT")
    private String detailedStory;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private ProjectCategory category;

    @Column(length = 100)
    private String style;

    @Enumerated(EnumType.STRING)
    @Column(name = "space_type", length = 100)
    private SpaceType spaceType;

    @Column(length = 255)
    private String location;

    @Column(name = "project_date")
    private LocalDate projectDate;

    @Column(name = "square_meters", precision = 10, scale = 2)
    private BigDecimal squareMeters;

    // Opsiyonel, serbest metin — "500K-1M TL" gibi
    @Column(name = "budget_range", length = 100)
    private String budgetRange;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProjectStatus status = ProjectStatus.DRAFT;

    @Column(nullable = false)
    private Boolean featured = false;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;

    // Kapak görseli — galeri dışında ayrıca tutulan özel görsel
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cover_image_id")
    private MediaFile coverImage;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<ProjectImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectTag> tags = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectMaterial> materials = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // ── Factory method ─────────────────────────────────────────────────────

    /**
     * Yeni proje oluşturur. Sadece invariant'ları (default değerleri) set eder.
     * İçerik alanları updateDetails() ile doldurulur.
     */
    public static Project create() {
        Project project = new Project();
        project.status = ProjectStatus.DRAFT;
        project.featured = false;
        project.viewCount = 0;
        return project;
    }

    // ── Domain methods — içerik ────────────────────────────────────────────

    /**
     * Tüm içerik alanlarını set eder. Create path'inde kullanılır.
     */
    public void updateDetails(
            String title,
            String slug,
            String shortDesc,
            String detailedStory,
            ProjectCategory category,
            String style,
            SpaceType spaceType,
            String location,
            LocalDate projectDate,
            BigDecimal squareMeters,
            String budgetRange
    ) {
        this.title = title;
        this.slug = slug;
        this.shortDesc = shortDesc;
        this.detailedStory = detailedStory;
        this.category = category;
        this.style = style;
        this.spaceType = spaceType;
        this.location = location;
        this.projectDate = projectDate;
        this.squareMeters = squareMeters;
        this.budgetRange = budgetRange;
    }

    /**
     * PATCH semantiği: sadece null olmayan alanları günceller.
     * Update path'inde kullanılır.
     */
    public void applyPatch(
            String title,
            String slug,
            String shortDesc,
            String detailedStory,
            ProjectCategory category,
            String style,
            SpaceType spaceType,
            String location,
            LocalDate projectDate,
            BigDecimal squareMeters,
            String budgetRange
    ) {
        if (title != null)        this.title = title;
        if (slug != null)         this.slug = slug;
        if (shortDesc != null)    this.shortDesc = shortDesc;
        if (detailedStory != null) this.detailedStory = detailedStory;
        if (category != null)     this.category = category;
        if (style != null)        this.style = style;
        if (spaceType != null)    this.spaceType = spaceType;
        if (location != null)     this.location = location;
        if (projectDate != null)  this.projectDate = projectDate;
        if (squareMeters != null) this.squareMeters = squareMeters;
        if (budgetRange != null)  this.budgetRange = budgetRange;
    }

    // ── Domain methods — durum ─────────────────────────────────────────────

    /**
     * Projeyi yayınlar. Zaten yayındaysa no-op (idempotent).
     */
    public void publish() {
        this.status = ProjectStatus.PUBLISHED;
    }

    public void unpublish() {
        this.status = ProjectStatus.DRAFT;
    }

    public void markAsFeatured() {
        this.featured = true;
    }

    public void unmarkAsFeatured() {
        this.featured = false;
    }

    public void incrementViewCount() {
        this.viewCount++;
    }

    // ── Domain methods — görsel ────────────────────────────────────────────

    public void setCoverImage(MediaFile coverImage) {
        this.coverImage = coverImage;
    }

    public void removeCoverImage() {
        this.coverImage = null;
    }

    /**
     * Galeriye yeni görsel ekler.
     * displayOrder dışarıdan hesaplanarak verilir (DB sorgusu gerektirdiği için service'de).
     */
    public ProjectImage addImage(MediaFile mediaFile, ImageType type, int displayOrder) {
        ProjectImage image = ProjectImage.of(this, mediaFile, type, displayOrder);
        this.images.add(image);
        return image;
    }

    /**
     * Galeriden görsel kaldırır. orphanRemoval=true otomatik DB'den siler.
     * Bu projeye ait olmayan id verilirse BusinessException fırlar.
     */
    public void removeImageById(Long imageId) {
        boolean removed = this.images.removeIf(img -> img.getId().equals(imageId));
        if (!removed) {
            throw new BusinessException("Image not found in this project: id=" + imageId);
        }
    }

    // ── Sorgulama ──────────────────────────────────────────────────────────

    public boolean isPublished() {
        return ProjectStatus.PUBLISHED.equals(this.status);
    }
}
