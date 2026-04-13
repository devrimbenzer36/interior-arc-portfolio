package com.portfolio.domain.contact.repository;

import com.portfolio.domain.contact.entity.ContactMessage;
import com.portfolio.domain.contact.enums.ContactStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * ContactMessage veri erişim katmanı.
 */
public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {

    Page<ContactMessage> findAllByStatus(ContactStatus status, Pageable pageable);

    long countByStatus(ContactStatus status);
}
