package com.portfolio.domain.project.repository;

import com.portfolio.domain.project.entity.ProjectImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * ProjectImage veri erişim katmanı.
 */
public interface ProjectImageRepository extends JpaRepository<ProjectImage, Long> {

    /**
     * Proje görsellerini MediaFile ile birlikte tek sorguda getirir.
     * JOIN FETCH ile N+1 engellenir: 10 görsel = 1 sorgu.
     */
    @Query("SELECT pi FROM ProjectImage pi JOIN FETCH pi.mediaFile WHERE pi.project.id = :projectId ORDER BY pi.displayOrder ASC")
    List<ProjectImage> findAllWithMediaByProjectId(@Param("projectId") Long projectId);

    List<ProjectImage> findAllByProjectIdOrderByDisplayOrderAsc(Long projectId);

    Optional<ProjectImage> findByIdAndProjectId(Long id, Long projectId);

    /**
     * Projenin mevcut en yüksek display_order değerini döner.
     * Yeni görsel eklenirken sona yerleştirmek için kullanılır.
     */
    @Query("SELECT COALESCE(MAX(pi.displayOrder), -1) FROM ProjectImage pi WHERE pi.project.id = :projectId")
    int findMaxDisplayOrderByProjectId(@Param("projectId") Long projectId);

    void deleteByIdAndProjectId(Long id, Long projectId);

    /**
     * Aynı görselin projeye iki kez eklenmesini önlemek için kullanılır.
     */
    boolean existsByProjectIdAndMediaFileId(Long projectId, Long mediaFileId);
}
