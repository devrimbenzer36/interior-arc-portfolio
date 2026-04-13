package com.portfolio.domain.contact.service;

import com.portfolio.common.exception.ResourceNotFoundException;
import com.portfolio.domain.contact.dto.request.SendContactMessageRequest;
import com.portfolio.domain.contact.dto.response.ContactMessageResponse;
import com.portfolio.domain.contact.entity.ContactMessage;
import com.portfolio.domain.contact.enums.ContactStatus;
import com.portfolio.domain.contact.repository.ContactMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContactService {

    private final ContactMessageRepository contactMessageRepository;

    // ── Public ────────────────────────────────────────────────────────────

    @Transactional
    public void sendMessage(SendContactMessageRequest request, String ipAddress) {
        ContactMessage message = ContactMessage.of(
                request.getFullName(),
                request.getEmail(),
                request.getPhone(),
                request.getSubject(),
                request.getMessage(),
                ipAddress
        );
        contactMessageRepository.save(message);
        log.info("Contact message received from: {}", request.getEmail());
    }

    // ── Admin ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<ContactMessageResponse> getAllMessages(Pageable pageable) {
        return contactMessageRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ContactMessageResponse> getMessagesByStatus(ContactStatus status, Pageable pageable) {
        return contactMessageRepository.findAllByStatus(status, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public long countNewMessages() {
        return contactMessageRepository.countByStatus(ContactStatus.NEW);
    }

    @Transactional
    public ContactMessageResponse updateStatus(Long id, ContactStatus status) {
        ContactMessage message = contactMessageRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("ContactMessage", id));
        message.updateStatus(status);
        log.info("Contact message status updated: id={}, status={}", id, status);
        return toResponse(message);
    }

    @Transactional
    public void deleteMessage(Long id) {
        ContactMessage message = contactMessageRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("ContactMessage", id));
        contactMessageRepository.delete(message);
        log.info("Contact message deleted: id={}", id);
    }

    // ── Mapping ────────────────────────────────────────────────────────────

    private ContactMessageResponse toResponse(ContactMessage m) {
        return ContactMessageResponse.builder()
                .id(m.getId())
                .fullName(m.getFullName())
                .email(m.getEmail())
                .phone(m.getPhone())
                .subject(m.getSubject())
                .message(m.getMessage())
                .status(m.getStatus())
                .createdAt(m.getCreatedAt())
                .build();
    }
}
