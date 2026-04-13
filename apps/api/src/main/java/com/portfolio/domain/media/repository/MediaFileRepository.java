package com.portfolio.domain.media.repository;

import com.portfolio.domain.media.entity.MediaFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * MediaFile veri erişim katmanı.
 */
public interface MediaFileRepository extends JpaRepository<MediaFile, Long> {

    Optional<MediaFile> findByStoredName(String storedName);

    boolean existsByStoredName(String storedName);
}
