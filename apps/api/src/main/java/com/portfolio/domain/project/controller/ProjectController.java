package com.portfolio.domain.project.controller;

import com.portfolio.common.response.ApiResponse;
import com.portfolio.domain.project.dto.request.CreateProjectRequest;
import com.portfolio.domain.project.dto.request.ReorderImagesRequest;
import com.portfolio.domain.project.dto.request.UpdateProjectRequest;
import com.portfolio.domain.project.dto.response.ProjectDetailResponse;
import com.portfolio.domain.project.dto.response.ProjectSummaryResponse;
import com.portfolio.domain.project.enums.ImageType;
import com.portfolio.domain.project.enums.ProjectCategory;
import com.portfolio.domain.project.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Projects", description = "Portfolio project endpoints")
public class ProjectController {

    private final ProjectService projectService;

    // ═══════════════════════════════════════════════════════════════════════
    // PUBLIC — Ziyaretçi endpoint'leri
    // ═══════════════════════════════════════════════════════════════════════

    @GetMapping("/api/v1/projects")
    @Operation(summary = "List published projects (paginated)")
    public ResponseEntity<ApiResponse<Page<ProjectSummaryResponse>>> getPublishedProjects(
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.ok(projectService.getPublishedProjects(pageable)));
    }

    @GetMapping("/api/v1/projects/featured")
    @Operation(summary = "List featured projects")
    public ResponseEntity<ApiResponse<List<ProjectSummaryResponse>>> getFeaturedProjects() {
        return ResponseEntity.ok(ApiResponse.ok(projectService.getFeaturedProjects()));
    }

    @GetMapping("/api/v1/projects/category/{category}")
    @Operation(summary = "List published projects by category")
    public ResponseEntity<ApiResponse<Page<ProjectSummaryResponse>>> getByCategory(
            @PathVariable ProjectCategory category,
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(projectService.getPublishedProjectsByCategory(category, pageable))
        );
    }

    @GetMapping("/api/v1/projects/{slug}")
    @Operation(summary = "Get published project by slug")
    public ResponseEntity<ApiResponse<ProjectDetailResponse>> getBySlug(
            @PathVariable String slug
    ) {
        return ResponseEntity.ok(ApiResponse.ok(projectService.getPublishedProjectBySlug(slug)));
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ADMIN — Proje yönetimi
    // Phase 1: auth yok, tüm /admin/** endpoint'leri açık
    // Phase 2: SecurityConfig'de JWT korumasına alınacak
    // ═══════════════════════════════════════════════════════════════════════

    @GetMapping("/api/v1/admin/projects")
    @Operation(summary = "[Admin] List all projects")
    public ResponseEntity<ApiResponse<Page<ProjectSummaryResponse>>> getAllProjects(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.ok(projectService.getAllProjects(pageable)));
    }

    @GetMapping("/api/v1/admin/projects/{id}")
    @Operation(summary = "[Admin] Get project by ID")
    public ResponseEntity<ApiResponse<ProjectDetailResponse>> getProjectById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(ApiResponse.ok(projectService.getProjectById(id)));
    }

    @PostMapping("/api/v1/admin/projects")
    @Operation(summary = "[Admin] Create project")
    public ResponseEntity<ApiResponse<ProjectDetailResponse>> createProject(
            @Valid @RequestBody CreateProjectRequest request
    ) {
        ProjectDetailResponse response = projectService.createProject(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response, "Project created"));
    }

    @PatchMapping("/api/v1/admin/projects/{id}")
    @Operation(summary = "[Admin] Partial update project (PATCH — null fields are ignored)")
    public ResponseEntity<ApiResponse<ProjectDetailResponse>> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProjectRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(projectService.updateProject(id, request), "Project updated"));
    }

    @DeleteMapping("/api/v1/admin/projects/{id}")
    @Operation(summary = "[Admin] Delete project")
    public ResponseEntity<ApiResponse<Void>> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.ok(ApiResponse.ok("Project deleted"));
    }

    @PatchMapping("/api/v1/admin/projects/{id}/publish")
    @Operation(summary = "[Admin] Publish project")
    public ResponseEntity<ApiResponse<ProjectDetailResponse>> publishProject(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(projectService.publishProject(id), "Project published"));
    }

    @PatchMapping("/api/v1/admin/projects/{id}/unpublish")
    @Operation(summary = "[Admin] Unpublish project")
    public ResponseEntity<ApiResponse<ProjectDetailResponse>> unpublishProject(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(projectService.unpublishProject(id), "Project unpublished"));
    }

    @PatchMapping("/api/v1/admin/projects/{id}/featured")
    @Operation(summary = "[Admin] Set featured status")
    public ResponseEntity<ApiResponse<ProjectDetailResponse>> setFeatured(
            @PathVariable Long id,
            @RequestParam boolean value
    ) {
        return ResponseEntity.ok(ApiResponse.ok(projectService.setFeatured(id, value)));
    }

    @PatchMapping("/api/v1/admin/projects/{id}/cover-image/{mediaFileId}")
    @Operation(summary = "[Admin] Set cover image")
    public ResponseEntity<ApiResponse<ProjectDetailResponse>> setCoverImage(
            @PathVariable Long id,
            @PathVariable Long mediaFileId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(projectService.setCoverImage(id, mediaFileId)));
    }

    // ── Görsel yönetimi ───────────────────────────────────────────────────

    @PostMapping("/api/v1/admin/projects/{id}/images/{mediaFileId}")
    @Operation(summary = "[Admin] Add image to project gallery")
    public ResponseEntity<ApiResponse<ProjectDetailResponse>> addImage(
            @PathVariable Long id,
            @PathVariable Long mediaFileId,
            @RequestParam(defaultValue = "GALLERY") ImageType type
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(projectService.addImage(id, mediaFileId, type)));
    }

    @DeleteMapping("/api/v1/admin/projects/{id}/images/{imageId}")
    @Operation(summary = "[Admin] Remove image from project gallery")
    public ResponseEntity<ApiResponse<Void>> removeImage(
            @PathVariable Long id,
            @PathVariable Long imageId
    ) {
        projectService.removeImage(id, imageId);
        return ResponseEntity.ok(ApiResponse.ok("Image removed"));
    }

    @PutMapping("/api/v1/admin/projects/{id}/images/reorder")
    @Operation(summary = "[Admin] Reorder project gallery images")
    public ResponseEntity<ApiResponse<Void>> reorderImages(
            @PathVariable Long id,
            @Valid @RequestBody ReorderImagesRequest request
    ) {
        projectService.reorderImages(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Images reordered"));
    }
}
