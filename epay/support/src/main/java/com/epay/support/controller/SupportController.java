package com.epay.support.controller;

import com.epay.common.exception.ApiResponse;
import com.epay.domain.support.dto.*;
import com.epay.domain.support.input.ChatRequest;
import com.epay.domain.support.input.CreateTicketRequest;
import com.epay.support.service.ChatService;
import com.epay.support.service.SupportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Support", description = "AI chat assistant, help articles, FAQs, and ticket submission")
@Slf4j
@RestController
@RequestMapping("/support")
@RequiredArgsConstructor
public class SupportController {

    private final ChatService    chatService;
    private final SupportService supportService;

    @Operation(
        summary     = "Chat with the AI support assistant",
        description = "Sends a message to the AI assistant and returns a contextual response. Useful for self-service issue resolution before raising a ticket."
    )
    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<ChatResponseDTO>> chat(
            @Valid @RequestBody ChatRequest req) {
        ChatResponseDTO response = chatService.chat(req);
        return ResponseEntity.ok(ApiResponse.success("OK", response));
    }

    @Operation(
        summary     = "List all help articles",
        description = "Returns all published help-centre articles."
    )
    @GetMapping("/articles")
    public ResponseEntity<ApiResponse<List<ArticleDTO>>> getArticles() {
        return ResponseEntity.ok(ApiResponse.success("Articles retrieved successfully",
                supportService.getAllArticles()));
    }

    @Operation(
        summary     = "Get a help article by slug",
        description = "Returns the full content of a single help article identified by its URL slug."
    )
    @GetMapping("/articles/{slug}")
    public ResponseEntity<ApiResponse<ArticleDTO>> getArticleBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success("Article retrieved successfully",
                supportService.getArticleBySlug(slug)));
    }

    @Operation(
        summary     = "List all FAQs grouped by category",
        description = "Returns frequently asked questions grouped into their respective categories."
    )
    @GetMapping("/faqs")
    public ResponseEntity<ApiResponse<List<FaqCategoryDTO>>> getFaqs() {
        return ResponseEntity.ok(ApiResponse.success("FAQs retrieved successfully",
                supportService.getAllFaqsGrouped()));
    }

    @Operation(
        summary     = "Create a support ticket",
        description = "Submits a new support ticket from the authenticated user."
    )
    @PostMapping("/tickets")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<TicketDTO>> createTicket(
            @Valid @RequestBody CreateTicketRequest req) {
        TicketDTO ticket = supportService.createTicket(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Support ticket created successfully", ticket));
    }

    @Operation(
        summary     = "List tickets for a user",
        description = "Returns all support tickets submitted by the specified user, paginated."
    )
    @GetMapping("/tickets/user/{userId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Page<TicketDTO>>> getUserTickets(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Tickets retrieved successfully",
                supportService.getTicketsByUser(userId, page, size)));
    }

    @Operation(
        summary     = "Get a ticket by reference",
        description = "Returns a single support ticket by its reference string. The requesting user must own the ticket."
    )
    @GetMapping("/tickets/{reference}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<TicketDTO>> getTicket(
            @PathVariable String reference,
            @RequestParam Long userId) {
        return ResponseEntity.ok(ApiResponse.success("Ticket retrieved successfully",
                supportService.getTicketByReference(reference, userId)));
    }
}
