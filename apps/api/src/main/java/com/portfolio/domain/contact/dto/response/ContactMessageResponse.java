package com.portfolio.domain.contact.dto.response;

import com.portfolio.domain.contact.enums.ContactStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * Admin paneli için iletişim mesajı response DTO'su.
 */
@Getter
@Builder
public class ContactMessageResponse {

    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String subject;
    private String message;
    private ContactStatus status;
    private Instant createdAt;
}
