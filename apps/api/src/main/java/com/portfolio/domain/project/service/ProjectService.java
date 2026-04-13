package com.portfolio.domain.project.service;

import com.portfolio.common.exception.BusinessException;
import com.portfolio.common.exception.ResourceNotFoundException;
import com.portfolio.common.util.SlugUtil;
import com.portfolio.domain.media.entity.MediaFile;
import com.portfolio.domain.media.repository.MediaFileRepository;
import com.portfolio.domain.project.dto.request.CreateProjectRequest;
import com.portfolio.domain.project.dto.request.ReorderImagesRequest;
import com.portfolio.domain.project.dto.request.UpdateProjectRequest;
import com.portfolio.domain.project.dto.response.ProjectDetailResponse;
import com.portfolio.domain.project.dto.response.ProjectImageResponse;
import com.portfolio.domain.project.dto.response.ProjectSummaryResponse;
import com.portfolio.domain.project.entity.Project;
import com.portfolio.domain.project.entity.ProjectImage;
import com.portfolio.domain.project.entity.ProjectMaterial;
import com.portfolio.domain.project.entity.ProjectTag;
import com.portfolio.domain.project.enums.ImageType;
import com.portfolio.domain.project.enums.ProjectCategory;
import com.portfolio.domain.project.enums.ProjectStatus;
import com.portfolio.domain.project.repository.ProjectImageRepository;
import com.portfolio.domain.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectImageRepository projectImageRepository;
    private final MediaFileRepository mediaFileRepository;

    // ── Public: Ziyaretçi endpoint'leri ──────────────────────────────────

    @Transactional(readOnly = true)
    public Page<ProjectSummaryResponse> getPublishedProjects(Pageable pageable) {
        return projectRepository
                .findAllByStatus(ProjectStatus.PUBLISHED, pageable)
                .map(this::toSummary);
    }

    @Transactional(readOnly = true)
    public Page<ProjectSummaryResponse> getPublishedProjectsByCategory(
            ProjectCategory category, Pageable pageable) {
        return projectRepository
                .findAllByStatusAndCategory(ProjectStatus.PUBLISHED, category, pageable)
                .map(this::toSummary);
    }

    @Transactional(readOnly = true)
    public List<ProjectSummaryResponse> getFeaturedProjects() {
        return projectRepository
                .findAllByStatusAndFeaturedTrue(ProjectStatus.PUBLISHED)
                .stream()
                .map(this::toSummary)
                .toList();
    }

    @Transactional
    public ProjectDetailResponse getPublishedProjectBySlug(String slug) {
        Project project = projectRepository
                .findBySlugAndStatus(slug, ProjectStatus.PUBLISHED)
                .orElseThrow(() -> ResourceNotFoundException.of("Project", slug));

        // View count DB'de atomic olarak artırılır
        projectRepository.incrementViewCount(project.getId());

        return toDetail(project);
    }

    // ── Admin: Proje yönetimi ─────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<ProjectSummaryResponse> getAllProjects(Pageable pageable) {
        return projectRepository
                .findAllByStatusIn(List.of(ProjectStatus.values()), pageable)
                .map(this::toSummary);
    }

    @Transactional(readOnly = true)
    public ProjectDetailResponse getProjectById(Long id) {
        Project project = projectRepository.findByIdWithDetails(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Project", id));
        return toDetail(project);
    }

    @Transactional
    public ProjectDetailResponse createProject(CreateProjectRequest request) {
        String slug = resolveSlug(request.getSlug(), request.getTitle(), null);

        Project project = Project.create();
        project.updateDetails(
                request.getTitle(),
                slug,
                request.getShortDesc(),
                request.getDetailedStory(),
                request.getCategory(),
                request.getStyle(),
                request.getSpaceType(),
                request.getLocation(),
                request.getProjectDate(),
                request.getSquareMeters(),
                request.getBudgetRange()
        );

        syncTags(project, request.getTags());
        syncMaterials(project, request.getMaterials());

        Project saved = projectRepository.save(project);

        // Kapak görseli bağla
        if (request.getCoverImageId() != null) {
            MediaFile cover = mediaFileRepository.findById(request.getCoverImageId())
                    .orElseThrow(() -> ResourceNotFoundException.of("MediaFile", request.getCoverImageId()));
            saved.setCoverImage(cover);
        }

        // Galeri görsellerini bağla — gönderilen sıra display_order olur
        // distinct() ile aynı mediaFileId'nin iki kez gönderilmesi engellenir
        List<Long> dedupedImageIds = request.getImageIds().stream().distinct().toList();
        for (int i = 0; i < dedupedImageIds.size(); i++) {
            Long mediaFileId = dedupedImageIds.get(i);
            MediaFile mediaFile = mediaFileRepository.findById(mediaFileId)
                    .orElseThrow(() -> ResourceNotFoundException.of("MediaFile", mediaFileId));
            ProjectImage image = saved.addImage(mediaFile, ImageType.GALLERY, i);
            projectImageRepository.save(image);
        }

        log.info("Project created: id={}, slug={}, images={}", saved.getId(), saved.getSlug(), request.getImageIds().size());
        return toDetail(saved);
    }

    @Transactional
    public ProjectDetailResponse updateProject(Long id, UpdateProjectRequest request) {
        Project project = projectRepository.findByIdWithDetails(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Project", id));

        // Slug: title veya explicit slug gönderildiyse yeniden çözümle.
        // request.getSlug() → null ise title'dan üret (resolveSlug bunu halleder).
        // BUG FIX: daha önce request.getTitle() requestSlug'a geçiyordu →
        //          "New Title" geçerli slug formatında olmadığından validation hatası veriyordu.
        String resolvedSlug = null;
        if (request.getSlug() != null || request.getTitle() != null) {
            String effectiveTitle = request.getTitle() != null ? request.getTitle() : project.getTitle();
            resolvedSlug = resolveSlug(request.getSlug(), effectiveTitle, id);
        }

        // PATCH: sadece null olmayan alanlar güncellenir
        project.applyPatch(
                request.getTitle(),
                resolvedSlug,
                request.getShortDesc(),
                request.getDetailedStory(),
                request.getCategory(),
                request.getStyle(),
                request.getSpaceType(),
                request.getLocation(),
                request.getProjectDate(),
                request.getSquareMeters(),
                request.getBudgetRange()
        );

        // null → korunur | [] → temizlenir | [...] → replace
        syncTags(project, request.getTags());
        syncMaterials(project, request.getMaterials());

        log.info("Project patched: id={}", id);
        return toDetail(project);
    }

    @Transactional
    public void deleteProject(Long id) {
        Project project = findProjectById(id);
        projectRepository.delete(project);
        log.info("Project deleted: id={}", id);
    }

    @Transactional
    public ProjectDetailResponse publishProject(Long id) {
        Project project = findProjectById(id);
        project.publish();
        log.info("Project published: id={}", id);
        return toDetail(project);
    }

    @Transactional
    public ProjectDetailResponse unpublishProject(Long id) {
        Project project = findProjectById(id);
        project.unpublish();
        log.info("Project unpublished: id={}", id);
        return toDetail(project);
    }

    @Transactional
    public ProjectDetailResponse setFeatured(Long id, boolean featured) {
        Project project = findProjectById(id);
        if (featured) {
            project.markAsFeatured();
        } else {
            project.unmarkAsFeatured();
        }
        return toDetail(project);
    }

    @Transactional
    public ProjectDetailResponse setCoverImage(Long projectId, Long mediaFileId) {
        Project project = findProjectById(projectId);
        MediaFile mediaFile = mediaFileRepository.findById(mediaFileId)
                .orElseThrow(() -> ResourceNotFoundException.of("MediaFile", mediaFileId));

        project.setCoverImage(mediaFile);
        log.info("Cover image set: projectId={}, mediaFileId={}", projectId, mediaFileId);
        return toDetail(project);
    }

    // ── Admin: Görsel yönetimi ────────────────────────────────────────────

    @Transactional
    public ProjectDetailResponse addImage(Long projectId, Long mediaFileId, ImageType type) {
        Project project = findProjectById(projectId);
        MediaFile mediaFile = mediaFileRepository.findById(mediaFileId)
                .orElseThrow(() -> ResourceNotFoundException.of("MediaFile", mediaFileId));

        if (projectImageRepository.existsByProjectIdAndMediaFileId(projectId, mediaFileId)) {
            throw new BusinessException("This image is already added to the project");
        }

        int nextOrder = projectImageRepository.findMaxDisplayOrderByProjectId(projectId) + 1;
        ProjectImage image = project.addImage(mediaFile, type, nextOrder);
        projectImageRepository.save(image);

        log.info("Image added to project: projectId={}, mediaFileId={}", projectId, mediaFileId);
        return toDetail(project);
    }

    @Transactional
    public void removeImage(Long projectId, Long imageId) {
        // images koleksiyonunu yükleyip entity domain metoduna delege et
        // ownership kontrolü entity içinde yapılır (başka projenin image'ı atılamaz)
        Project project = findProjectById(projectId);
        project.removeImageById(imageId);
        log.info("Image removed: projectId={}, imageId={}", projectId, imageId);
    }

    @Transactional
    public void reorderImages(Long projectId, ReorderImagesRequest request) {
        List<ProjectImage> images = projectImageRepository
                .findAllByProjectIdOrderByDisplayOrderAsc(projectId);

        if (images.size() != request.getImageIds().size()) {
            throw new BusinessException("Image ID list size does not match project image count");
        }

        // Her image'ın bu projeye ait olduğunu doğrula
        List<Long> existingIds = images.stream().map(ProjectImage::getId).toList();
        boolean allMatch = request.getImageIds().stream().allMatch(existingIds::contains);
        if (!allMatch) {
            throw new BusinessException("One or more image IDs do not belong to this project");
        }

        for (int i = 0; i < request.getImageIds().size(); i++) {
            final int order = i;
            Long imageId = request.getImageIds().get(i);
            images.stream()
                    .filter(img -> img.getId().equals(imageId))
                    .findFirst()
                    .ifPresent(img -> img.updateOrder(order));
        }
    }

    // ── Private helpers ────────────────────────────────────────────────────

    private Project findProjectById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Project", id));
    }

    /**
     * Slug çözümleme:
     * 1. Request'te slug varsa kullan (validate et)
     * 2. Yoksa title'dan üret
     * 3. Her iki durumda da çakışma kontrolü yap
     */
    private String resolveSlug(String requestSlug, String title, Long excludeId) {
        String slug = (requestSlug != null && !requestSlug.isBlank())
                ? requestSlug.trim()
                : SlugUtil.toSlug(title);

        if (slug.isBlank()) {
            throw new BusinessException("Could not generate a valid slug from the title");
        }

        boolean exists = (excludeId == null)
                ? projectRepository.existsBySlug(slug)
                : projectRepository.existsBySlugAndIdNot(slug, excludeId);

        if (exists) {
            throw new BusinessException("Slug already in use: " + slug);
        }

        return slug;
    }

    /**
     * PATCH semantiği:
     *   null   → mevcut liste korunur (dokunulmaz)
     *   []     → tüm etiketler silinir
     *   [...] → gönderilen liste ile replace edilir
     */
    private void syncTags(Project project, List<String> tagNames) {
        if (tagNames == null) return;
        project.getTags().clear();
        tagNames.stream()
                .filter(name -> name != null && !name.isBlank())
                .map(String::trim)
                .distinct()
                .forEach(name -> project.getTags().add(ProjectTag.of(project, name)));
    }

    private void syncMaterials(Project project, List<String> materialNames) {
        if (materialNames == null) return;
        project.getMaterials().clear();
        materialNames.stream()
                .filter(name -> name != null && !name.isBlank())
                .map(String::trim)
                .distinct()
                .forEach(name -> project.getMaterials().add(ProjectMaterial.of(project, name)));
    }

    // ── Mapping ────────────────────────────────────────────────────────────

    private ProjectSummaryResponse toSummary(Project p) {
        return ProjectSummaryResponse.builder()
                .id(p.getId())
                .title(p.getTitle())
                .slug(p.getSlug())
                .shortDesc(p.getShortDesc())
                .category(p.getCategory())
                .spaceType(p.getSpaceType())
                .location(p.getLocation())
                .projectDate(p.getProjectDate())
                .status(p.getStatus())
                .featured(p.getFeatured())
                .coverImageUrl(p.getCoverImage() != null ? p.getCoverImage().getUrl() : null)
                .createdAt(p.getCreatedAt())
                .build();
    }

    private ProjectDetailResponse toDetail(Project p) {
        List<ProjectImage> images = projectImageRepository
                .findAllWithMediaByProjectId(p.getId());

        return ProjectDetailResponse.builder()
                .id(p.getId())
                .title(p.getTitle())
                .slug(p.getSlug())
                .shortDesc(p.getShortDesc())
                .detailedStory(p.getDetailedStory())
                .category(p.getCategory())
                .style(p.getStyle())
                .spaceType(p.getSpaceType())
                .location(p.getLocation())
                .projectDate(p.getProjectDate())
                .squareMeters(p.getSquareMeters())
                .budgetRange(p.getBudgetRange())
                .status(p.getStatus())
                .featured(p.getFeatured())
                .viewCount(p.getViewCount())
                .coverImageUrl(p.getCoverImage() != null ? p.getCoverImage().getUrl() : null)
                .images(images.stream().map(this::toImageResponse).toList())
                .tags(p.getTags().stream().map(ProjectTag::getName).toList())
                .materials(p.getMaterials().stream().map(ProjectMaterial::getName).toList())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    private ProjectImageResponse toImageResponse(ProjectImage img) {
        return ProjectImageResponse.builder()
                .id(img.getId())
                .url(img.getMediaFile().getUrl())
                .type(img.getType())
                .displayOrder(img.getDisplayOrder())
                .altText(img.getAltText())
                .caption(img.getCaption())
                .width(img.getMediaFile().getWidth())
                .height(img.getMediaFile().getHeight())
                .build();
    }
}
