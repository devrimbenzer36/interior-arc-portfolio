package com.portfolio.domain.contact.controller;

import com.portfolio.common.exception.BusinessException;
import com.portfolio.common.response.ApiResponse;
import com.portfolio.domain.contact.dto.request.SendContactMessageRequest;
import com.portfolio.domain.contact.dto.response.ContactMessageResponse;
import com.portfolio.domain.contact.enums.ContactStatus;
import com.portfolio.domain.contact.service.ContactService;
import com.portfolio.security.ratelimit.RateLimiterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Contact", description = "Contact form and message management")
public class ContactController {

    private final ContactService contactService;
    private final RateLimiterService rateLimiter;

    // ── Public ────────────────────────────────────────────────────────────

    @PostMapping("/api/v1/contact")
    @Operation(summary = "Send contact message")
    public ResponseEntity<ApiResponse<Void>> sendMessage(
            @Valid @RequestBody SendContactMessageRequest request,
            HttpServletRequest httpRequest
    ) {
        String ip = httpRequest.getRemoteAddr();
        if (!rateLimiter.isContactAllowed(ip)) {
            throw new BusinessException(
                "Too many requests. Please try again later.",
                HttpStatus.TOO_MANY_REQUESTS
            );
        }
        contactService.sendMessage(request, ip);
        return ResponseEntity.ok(ApiResponse.ok("Your message has been received. We will get back to you soon."));
    }

    // ── Admin ─────────────────────────────────────────────────────────────

    @GetMapping("/api/v1/admin/contact")
    @Operation(summary = "[Admin] List all contact messages")
    public ResponseEntity<ApiResponse<Page<ContactMessageResponse>>> getAllMessages(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.ok(contactService.getAllMessages(pageable)));
    }

    @GetMapping("/api/v1/admin/contact/status/{status}")
    @Operation(summary = "[Admin] List messages by status")
    public ResponseEntity<ApiResponse<Page<ContactMessageResponse>>> getByStatus(
            @PathVariable ContactStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.ok(contactService.getMessagesByStatus(status, pageable)));
    }

    @GetMapping("/api/v1/admin/contact/count/new")
    @Operation(summary = "[Admin] Count unread messages")
    public ResponseEntity<ApiResponse<Long>> countNewMessages() {
        return ResponseEntity.ok(ApiResponse.ok(contactService.countNewMessages()));
    }

    @PatchMapping("/api/v1/admin/contact/{id}/status")
    @Operation(summary = "[Admin] Update message status")
    public ResponseEntity<ApiResponse<ContactMessageResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam ContactStatus status
    ) {
        return ResponseEntity.ok(ApiResponse.ok(contactService.updateStatus(id, status)));
    }

    @DeleteMapping("/api/v1/admin/contact/{id}")
    @Operation(summary = "[Admin] Delete message")
    public ResponseEntity<ApiResponse<Void>> deleteMessage(@PathVariable Long id) {
        contactService.deleteMessage(id);
        return ResponseEntity.ok(ApiResponse.ok("Message deleted"));
    }

}
