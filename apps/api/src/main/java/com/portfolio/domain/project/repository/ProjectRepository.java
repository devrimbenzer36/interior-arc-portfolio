package com.portfolio.domain.project.repository;

import com.portfolio.domain.project.entity.Project;
import com.portfolio.domain.project.enums.ProjectCategory;
import com.portfolio.domain.project.enums.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Project veri erişim katmanı.
 *
 * Public sorgular sadece PUBLISHED projeleri döner.
 * Admin sorguları tüm projeleri döner (status fark etmeksizin).
 *
 * N+1 stratejisi:
 * - List sorgular (toSummary): coverImage @EntityGraph — tek LEFT JOIN, güvenli pagination
 * - Detail sorgular (toDetail): coverImage + tags + materials @EntityGraph — tek entity, koleksiyon fetch güvenli
 * - Images ayrı sorguda JOIN FETCH ile gelir (ProjectImageRepository)
 */
public interface ProjectRepository extends JpaRepository<Project, Long> {

    // ── Public ────────────────────────────────────────────────────────────

    /**
     * Slug + status ile tek proje — detail view için tags/materials da yüklenir.
     * @ManyToOne olan coverImage ve koleksiyonlar tek entity üzerinde fetch edildiğinden
     * in-memory pagination uyarısı oluşmaz.
     */
    @EntityGraph(attributePaths = {"coverImage", "tags", "materials"})
    Optional<Project> findBySlugAndStatus(String slug, ProjectStatus status);

    /**
     * Sayfalı liste — sadece coverImage yüklenir (summary için yeterli).
     * coverImage @ManyToOne olduğundan JOIN FETCH + pagination güvenlidir.
     */
    @EntityGraph(attributePaths = {"coverImage"})
    Page<Project> findAllByStatus(ProjectStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"coverImage"})
    Page<Project> findAllByStatusAndCategory(
            ProjectStatus status,
            ProjectCategory category,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"coverImage"})
    List<Project> findAllByStatusAndFeaturedTrue(ProjectStatus status);

    // ── Admin ─────────────────────────────────────────────────────────────

    Optional<Project> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

    @EntityGraph(attributePaths = {"coverImage"})
    Page<Project> findAllByStatusIn(List<ProjectStatus> statuses, Pageable pageable);

    /**
     * Admin detail view için id ile tek proje — tags/materials da yüklenir.
     */
    @EntityGraph(attributePaths = {"coverImage", "tags", "materials"})
    @Query("SELECT p FROM Project p WHERE p.id = :id")
    Optional<Project> findByIdWithDetails(@Param("id") Long id);

    // ── View count ────────────────────────────────────────────────────────

    @Modifying
    @Query("UPDATE Project p SET p.viewCount = p.viewCount + 1 WHERE p.id = :id")
    void incrementViewCount(@Param("id") Long id);
}
