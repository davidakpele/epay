package com.epay.admin.controller;

import com.epay.admin.service.AdminTicketService;
import com.epay.common.config.security.JwtClaimsHolder;
import com.epay.common.exception.ApiResponse;
import com.epay.domain.admin.input.CreateTicketRequest;
import com.epay.domain.admin.input.TicketReplyRequest;
import com.epay.domain.admin.input.UpdateTicketRequest;
import com.epay.domain.support.dto.TicketDTO;
import com.epay.domain.support.enums.TicketCategory;
import com.epay.domain.support.enums.TicketPriority;
import com.epay.domain.support.enums.TicketStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;


@RestController
@RequestMapping("/admin/tickets")
@RequiredArgsConstructor
public class AdminTicketController {

    private final AdminTicketService ticketService;
    private final JwtClaimsHolder     jwtClaims;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER','CUSTOMER_SERVICE')")
    public ResponseEntity<ApiResponse<TicketDTO>> createTicket(
            @Valid @RequestBody CreateTicketRequest request,
            Authentication auth) {
        Long staffId = extractUserId(auth);
        TicketDTO ticket = ticketService.createTicket(request, staffId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Ticket created.", ticket));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER','CUSTOMER_SERVICE','EDITOR')")
    public ResponseEntity<ApiResponse<Page<TicketDTO>>> search(
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) TicketCategory category,
            @RequestParam(required = false) TicketPriority priority,
            @RequestParam(required = false) Long assignedTo,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(null,
                ticketService.search(status, category, priority, assignedTo, userId, from, to, pageable)));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER','CUSTOMER_SERVICE','EDITOR')")
    public ResponseEntity<ApiResponse<Page<TicketDTO>>> listByStatus(
            @PathVariable TicketStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(null,
                ticketService.listByStatus(status, pageable)));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER','CUSTOMER_SERVICE')")
    public ResponseEntity<ApiResponse<Page<TicketDTO>>> listByUser(
            @PathVariable Long userId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(null,
                ticketService.listByUser(userId, pageable)));
    }

    @GetMapping("/assigned")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER','CUSTOMER_SERVICE')")
    public ResponseEntity<ApiResponse<Page<TicketDTO>>> myTickets(
            Authentication auth,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(null,
                ticketService.listAssignedTo(extractUserId(auth), pageable)));
    }

    @GetMapping("/{ticketId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER','CUSTOMER_SERVICE','EDITOR')")
    public ResponseEntity<ApiResponse<TicketDTO>> getTicket(@PathVariable Long ticketId) {
        return ResponseEntity.ok(ApiResponse.success(null,
                ticketService.getById(ticketId, true)));
    }

    @GetMapping("/ref/{reference}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER','CUSTOMER_SERVICE','EDITOR')")
    public ResponseEntity<ApiResponse<TicketDTO>> getByReference(@PathVariable String reference) {
        return ResponseEntity.ok(ApiResponse.success(null,
                ticketService.getByReference(reference, true)));
    }

    @PutMapping("/{ticketId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER','CUSTOMER_SERVICE')")
    public ResponseEntity<ApiResponse<TicketDTO>> updateTicket(
            @PathVariable Long ticketId,
            @Valid @RequestBody UpdateTicketRequest request,
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Ticket updated.",
                ticketService.updateTicket(ticketId, request, extractUserId(auth))));
    }

    @PatchMapping("/{ticketId}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER','CUSTOMER_SERVICE')")
    public ResponseEntity<ApiResponse<TicketDTO>> resolve(
            @PathVariable Long ticketId, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Ticket resolved.",
                ticketService.resolveTicket(ticketId, extractUserId(auth))));
    }

    @PatchMapping("/{ticketId}/close")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER','CUSTOMER_SERVICE')")
    public ResponseEntity<ApiResponse<TicketDTO>> close(
            @PathVariable Long ticketId, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Ticket closed.",
                ticketService.closeTicket(ticketId, extractUserId(auth))));
    }

    @PatchMapping("/{ticketId}/reopen")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER','CUSTOMER_SERVICE')")
    public ResponseEntity<ApiResponse<TicketDTO>> reopen(
            @PathVariable Long ticketId, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Ticket reopened.",
                ticketService.reopenTicket(ticketId, extractUserId(auth))));
    }

    @PatchMapping("/{ticketId}/escalate")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER','CUSTOMER_SERVICE')")
    public ResponseEntity<ApiResponse<TicketDTO>> escalate(
            @PathVariable Long ticketId, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Ticket escalated.",
                ticketService.escalateTicket(ticketId, extractUserId(auth))));
    }


    @PostMapping("/{ticketId}/reply")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER','CUSTOMER_SERVICE')")
    public ResponseEntity<ApiResponse<TicketDTO>> staffReply(
            @PathVariable Long ticketId,
            @Valid @RequestBody TicketReplyRequest request,
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Reply sent.",
                ticketService.addStaffReply(ticketId, request, extractUserId(auth))));
    }

    @DeleteMapping("/{ticketId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER')")
    public ResponseEntity<ApiResponse<Void>> deleteTicket(@PathVariable Long ticketId) {
        ticketService.deleteTicket(ticketId);
        return ResponseEntity.ok(ApiResponse.success("Ticket deleted.", null));
    }


    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER','CUSTOMER_SERVICE')")
    public ResponseEntity<ApiResponse<Object>> getStats() {
        return ResponseEntity.ok(ApiResponse.success(null, ticketService.getTicketStats()));
    }

    private Long extractUserId(Authentication auth) {
        Long id = jwtClaims.getUserId();
        return id != null ? id : 0L;
    }
}
