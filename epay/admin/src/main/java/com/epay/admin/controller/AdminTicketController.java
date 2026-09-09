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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Admin — Support Tickets", description = "Create, assign, resolve, and manage customer support tickets")
@RestController
@RequestMapping("/admin/tickets")
@RequiredArgsConstructor
public class AdminTicketController {

    private final AdminTicketService ticketService;
    private final JwtClaimsHolder     jwtClaims;

    @Operation(
        summary     = "Create a ticket on behalf of a user",
        description = "Allows staff to open a new support ticket manually, e.g. after a phone call from a customer."
    )
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER','CUSTOMER_SERVICE')")
    public ResponseEntity<ApiResponse<TicketDTO>> createTicket(
            @Valid @RequestBody CreateTicketRequest request,
            Authentication auth) {
        TicketDTO ticket = ticketService.createTicket(request, extractUserId(auth));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Ticket created.", ticket));
    }

    @Operation(
        summary     = "Search tickets",
        description = "Multi-criteria ticket search. Filter by status, category, priority, assigned agent, user ID, or date range."
    )
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER','CUSTOMER_SERVICE','EDITOR')")
    public ResponseEntity<ApiResponse<Page<TicketDTO>>> search(
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) TicketCategory category,
            @RequestParam(required = false) TicketPriority priority,
            @RequestParam(required = false) Long assignedTo,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(null,
                ticketService.search(status, category, priority, assignedTo, userId, from, to, pageable)));
    }

    @Operation(
        summary     = "List tickets by status",
        description = "Returns all tickets with the given status (OPEN, IN_PROGRESS, RESOLVED, CLOSED, ESCALATED)."
    )
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER','CUSTOMER_SERVICE','EDITOR')")
    public ResponseEntity<ApiResponse<Page<TicketDTO>>> listByStatus(
            @PathVariable TicketStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(null,
                ticketService.listByStatus(status, pageable)));
    }

    @Operation(
        summary     = "List tickets for a user",
        description = "Returns all support tickets associated with a specific customer."
    )
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER','CUSTOMER_SERVICE')")
    public ResponseEntity<ApiResponse<Page<TicketDTO>>> listByUser(
            @PathVariable Long userId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(null,
                ticketService.listByUser(userId, pageable)));
    }

    @Operation(
        summary     = "List tickets assigned to me",
        description = "Returns all tickets currently assigned to the authenticated staff member."
    )
    @GetMapping("/assigned")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER','CUSTOMER_SERVICE')")
    public ResponseEntity<ApiResponse<Page<TicketDTO>>> myTickets(
            Authentication auth,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(null,
                ticketService.listAssignedTo(extractUserId(auth), pageable)));
    }

    @Operation(
        summary     = "Get a ticket by ID",
        description = "Returns the full ticket details including all replies and status history."
    )
    @GetMapping("/{ticketId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER','CUSTOMER_SERVICE','EDITOR')")
    public ResponseEntity<ApiResponse<TicketDTO>> getTicket(@PathVariable Long ticketId) {
        return ResponseEntity.ok(ApiResponse.success(null,
                ticketService.getById(ticketId, true)));
    }

    @Operation(
        summary     = "Get a ticket by reference",
        description = "Looks up a ticket using its unique reference string (e.g. TKT-20260912-XXXXX)."
    )
    @GetMapping("/ref/{reference}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER','CUSTOMER_SERVICE','EDITOR')")
    public ResponseEntity<ApiResponse<TicketDTO>> getByReference(@PathVariable String reference) {
        return ResponseEntity.ok(ApiResponse.success(null,
                ticketService.getByReference(reference, true)));
    }

    @Operation(
        summary     = "Update a ticket",
        description = "Updates ticket metadata such as priority, category, assignment, or internal notes."
    )
    @PutMapping("/{ticketId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER','CUSTOMER_SERVICE')")
    public ResponseEntity<ApiResponse<TicketDTO>> updateTicket(
            @PathVariable Long ticketId,
            @Valid @RequestBody UpdateTicketRequest request,
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Ticket updated.",
                ticketService.updateTicket(ticketId, request, extractUserId(auth))));
    }

    @Operation(
        summary     = "Resolve a ticket",
        description = "Marks the ticket as RESOLVED, indicating the customer's issue has been addressed."
    )
    @PatchMapping("/{ticketId}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER','CUSTOMER_SERVICE')")
    public ResponseEntity<ApiResponse<TicketDTO>> resolve(
            @PathVariable Long ticketId, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Ticket resolved.",
                ticketService.resolveTicket(ticketId, extractUserId(auth))));
    }

    @Operation(
        summary     = "Close a ticket",
        description = "Permanently closes a resolved ticket. No further replies are accepted."
    )
    @PatchMapping("/{ticketId}/close")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER','CUSTOMER_SERVICE')")
    public ResponseEntity<ApiResponse<TicketDTO>> close(
            @PathVariable Long ticketId, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Ticket closed.",
                ticketService.closeTicket(ticketId, extractUserId(auth))));
    }

    @Operation(
        summary     = "Reopen a closed ticket",
        description = "Re-opens a CLOSED or RESOLVED ticket if the customer's issue recurs."
    )
    @PatchMapping("/{ticketId}/reopen")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER','CUSTOMER_SERVICE')")
    public ResponseEntity<ApiResponse<TicketDTO>> reopen(
            @PathVariable Long ticketId, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Ticket reopened.",
                ticketService.reopenTicket(ticketId, extractUserId(auth))));
    }

    @Operation(
        summary     = "Escalate a ticket",
        description = "Escalates the ticket to a higher-priority queue for senior review."
    )
    @PatchMapping("/{ticketId}/escalate")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER','CUSTOMER_SERVICE')")
    public ResponseEntity<ApiResponse<TicketDTO>> escalate(
            @PathVariable Long ticketId, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Ticket escalated.",
                ticketService.escalateTicket(ticketId, extractUserId(auth))));
    }

    @Operation(
        summary     = "Add a staff reply to a ticket",
        description = "Posts a reply from the authenticated staff member. The reply is visible to the customer."
    )
    @PostMapping("/{ticketId}/reply")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER','CUSTOMER_SERVICE')")
    public ResponseEntity<ApiResponse<TicketDTO>> staffReply(
            @PathVariable Long ticketId,
            @Valid @RequestBody TicketReplyRequest request,
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Reply sent.",
                ticketService.addStaffReply(ticketId, request, extractUserId(auth))));
    }

    @Operation(
        summary     = "Delete a ticket",
        description = "Permanently removes a ticket and all its replies. Admin and SUPER_USER only."
    )
    @DeleteMapping("/{ticketId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER')")
    public ResponseEntity<ApiResponse<Void>> deleteTicket(@PathVariable Long ticketId) {
        ticketService.deleteTicket(ticketId);
        return ResponseEntity.ok(ApiResponse.success("Ticket deleted.", null));
    }

    @Operation(
        summary     = "Get support ticket statistics",
        description = "Returns aggregated counts by status, average resolution time, and ticket volume trends."
    )
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
