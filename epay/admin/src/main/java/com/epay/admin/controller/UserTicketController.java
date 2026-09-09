package com.epay.admin.controller;

import com.epay.admin.service.AdminTicketService;
import com.epay.common.exception.ApiResponse;
import com.epay.domain.admin.input.CreateTicketRequest;
import com.epay.domain.admin.input.TicketReplyRequest;
import com.epay.domain.support.dto.TicketDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Support Tickets", description = "Submit and track customer support tickets")
@RestController
@RequestMapping("/tickets")
@PreAuthorize("hasRole('USER')")
@RequiredArgsConstructor
public class UserTicketController {

    private final AdminTicketService ticketService;

    @Operation(
        summary     = "Submit a support ticket",
        description = "Creates a new support ticket from the authenticated user. Returns the generated ticket reference."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<TicketDTO>> submit(
            @Valid @RequestBody CreateTicketRequest request,
            Authentication auth) {
        Long userId = extractUserId(auth);
        request.setUserId(userId);
        TicketDTO ticket = ticketService.submitUserTicket(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Your ticket has been submitted. Reference: " + ticket.getTicketReference(),
                        ticket));
    }

    @Operation(
        summary     = "List my support tickets",
        description = "Returns all support tickets previously submitted by the authenticated user, newest first."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<Page<TicketDTO>>> myTickets(
            Authentication auth,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        Long userId = extractUserId(auth);
        return ResponseEntity.ok(ApiResponse.success(null,
                ticketService.listByUser(userId, pageable)));
    }

    @Operation(
        summary     = "Get a ticket by ID",
        description = "Returns the full details of a single ticket. Only the ticket owner can access it."
    )
    @GetMapping("/{ticketId}")
    public ResponseEntity<ApiResponse<TicketDTO>> getTicket(
            @PathVariable Long ticketId, Authentication auth) {
        TicketDTO ticket = ticketService.getById(ticketId, false);
        if (!ticket.getUserId().equals(extractUserId(auth))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("You are not authorised to view this ticket."));
        }
        return ResponseEntity.ok(ApiResponse.success(null, ticket));
    }

    @Operation(
        summary     = "Get a ticket by reference",
        description = "Looks up a ticket by its reference string (e.g. TKT-20260912-XXXXX). Only the ticket owner can access it."
    )
    @GetMapping("/ref/{reference}")
    public ResponseEntity<ApiResponse<TicketDTO>> getByReference(
            @PathVariable String reference, Authentication auth) {
        TicketDTO ticket = ticketService.getByReference(reference, false);
        if (!ticket.getUserId().equals(extractUserId(auth))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("You are not authorised to view this ticket."));
        }
        return ResponseEntity.ok(ApiResponse.success(null, ticket));
    }

    @Operation(
        summary     = "Reply to a ticket",
        description = "Adds a follow-up message to an open ticket from the authenticated user."
    )
    @PostMapping("/{ticketId}/reply")
    public ResponseEntity<ApiResponse<TicketDTO>> reply(
            @PathVariable Long ticketId,
            @Valid @RequestBody TicketReplyRequest request,
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Reply sent.",
                ticketService.addUserReply(ticketId, request, extractUserId(auth))));
    }

    private Long extractUserId(Authentication auth) {
        if (auth == null) return 0L;
        Object principal = auth.getPrincipal();
        if (principal instanceof org.springframework.security.oauth2.jwt.Jwt jwt) {
            Object uid = jwt.getClaim("userId");
            if (uid instanceof Number n) return n.longValue();
        }
        return 0L;
    }
}
