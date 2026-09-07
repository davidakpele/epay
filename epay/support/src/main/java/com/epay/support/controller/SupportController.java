package com.epay.support.controller;

import com.epay.common.exception.ApiResponse;
import com.epay.domain.support.dto.*;
import com.epay.domain.support.input.ChatRequest;
import com.epay.domain.support.input.CreateTicketRequest;
import com.epay.support.service.ChatService;
import com.epay.support.service.SupportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API for ePay customer support.
 *
 * <table border="1">
 *   <tr><th>Method</th><th>Path</th><th>Auth</th><th>Description</th></tr>
 *   <tr><td>POST</td><td>/support/chat</td><td>permitAll</td><td>Send a message to the AI support bot</td></tr>
 *   <tr><td>GET</td> <td>/support/articles</td><td>permitAll</td><td>List all active help articles</td></tr>
 *   <tr><td>GET</td> <td>/support/articles/{slug}</td><td>permitAll</td><td>Get a single article by slug</td></tr>
 *   <tr><td>GET</td> <td>/support/faqs</td><td>permitAll</td><td>List FAQs grouped by category</td></tr>
 *   <tr><td>POST</td><td>/support/tickets</td><td>USER</td><td>Create a support ticket</td></tr>
 *   <tr><td>GET</td> <td>/support/tickets/user/{userId}</td><td>USER</td><td>Get tickets for a user (paged)</td></tr>
 *   <tr><td>GET</td> <td>/support/tickets/{reference}</td><td>USER</td><td>Get a single ticket by reference</td></tr>
 * </table>
 */
@Slf4j
@RestController
@RequestMapping("/support")
@RequiredArgsConstructor
public class SupportController {

    private final ChatService    chatService;
    private final SupportService supportService;

    // ── Chat ──────────────────────────────────────────────────────────────────

    /**
     * POST /support/chat
     * Accepts a user message, calls the AI with full conversation context,
     * persists both turns, and returns the assistant reply.
     * Open to everyone (logged-in and guest users).
     */
    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<ChatResponseDTO>> chat(
            @Valid @RequestBody ChatRequest req) {

        ChatResponseDTO response = chatService.chat(req);
        return ResponseEntity.ok(ApiResponse.success("OK", response));
    }

    // ── Articles ──────────────────────────────────────────────────────────────

    /**
     * GET /support/articles
     * Returns all active help articles, newest first.
     */
    @GetMapping("/articles")
    public ResponseEntity<ApiResponse<List<ArticleDTO>>> getArticles() {
        List<ArticleDTO> articles = supportService.getAllArticles();
        return ResponseEntity.ok(ApiResponse.success("Articles retrieved successfully", articles));
    }

    /**
     * GET /support/articles/{slug}
     * Returns a single article by its URL slug.
     */
    @GetMapping("/articles/{slug}")
    public ResponseEntity<ApiResponse<ArticleDTO>> getArticleBySlug(
            @PathVariable String slug) {

        ArticleDTO article = supportService.getArticleBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success("Article retrieved successfully", article));
    }

    // ── FAQs ──────────────────────────────────────────────────────────────────

    /**
     * GET /support/faqs
     * Returns all active FAQs grouped by category, ordered by category name
     * then sort_order within each category.
     */
    @GetMapping("/faqs")
    public ResponseEntity<ApiResponse<List<FaqCategoryDTO>>> getFaqs() {
        List<FaqCategoryDTO> faqs = supportService.getAllFaqsGrouped();
        return ResponseEntity.ok(ApiResponse.success("FAQs retrieved successfully", faqs));
    }

    // ── Tickets ───────────────────────────────────────────────────────────────

    /**
     * POST /support/tickets
     * Creates a new support ticket for an authenticated user.
     */
    @PostMapping("/tickets")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<TicketDTO>> createTicket(
            @Valid @RequestBody CreateTicketRequest req) {

        TicketDTO ticket = supportService.createTicket(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Support ticket created successfully", ticket));
    }

    /**
     * GET /support/tickets/user/{userId}?page=0&size=10
     * Returns paginated tickets for the given user, newest first.
     */
    @GetMapping("/tickets/user/{userId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Page<TicketDTO>>> getUserTickets(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<TicketDTO> tickets = supportService.getTicketsByUser(userId, page, size);
        return ResponseEntity.ok(ApiResponse.success("Tickets retrieved successfully", tickets));
    }

    /**
     * GET /support/tickets/{reference}?userId=
     * Returns a single ticket by reference, scoped to the requesting user.
     */
    @GetMapping("/tickets/{reference}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<TicketDTO>> getTicket(
            @PathVariable String reference,
            @RequestParam Long userId) {

        TicketDTO ticket = supportService.getTicketByReference(reference, userId);
        return ResponseEntity.ok(ApiResponse.success("Ticket retrieved successfully", ticket));
    }
}
