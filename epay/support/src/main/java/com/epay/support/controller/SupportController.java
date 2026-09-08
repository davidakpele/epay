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


@Slf4j
@RestController
@RequestMapping("/support")
@RequiredArgsConstructor
public class SupportController {

    private final ChatService    chatService;
    private final SupportService supportService;


    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<ChatResponseDTO>> chat(
            @Valid @RequestBody ChatRequest req) {

        ChatResponseDTO response = chatService.chat(req);
        return ResponseEntity.ok(ApiResponse.success("OK", response));
    }

    @GetMapping("/articles")
    public ResponseEntity<ApiResponse<List<ArticleDTO>>> getArticles() {
        List<ArticleDTO> articles = supportService.getAllArticles();
        return ResponseEntity.ok(ApiResponse.success("Articles retrieved successfully", articles));
    }

    @GetMapping("/articles/{slug}")
    public ResponseEntity<ApiResponse<ArticleDTO>> getArticleBySlug(
            @PathVariable String slug) {

        ArticleDTO article = supportService.getArticleBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success("Article retrieved successfully", article));
    }


    @GetMapping("/faqs")
    public ResponseEntity<ApiResponse<List<FaqCategoryDTO>>> getFaqs() {
        List<FaqCategoryDTO> faqs = supportService.getAllFaqsGrouped();
        return ResponseEntity.ok(ApiResponse.success("FAQs retrieved successfully", faqs));
    }


    @PostMapping("/tickets")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<TicketDTO>> createTicket(
            @Valid @RequestBody CreateTicketRequest req) {

        TicketDTO ticket = supportService.createTicket(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Support ticket created successfully", ticket));
    }

    @GetMapping("/tickets/user/{userId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Page<TicketDTO>>> getUserTickets(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<TicketDTO> tickets = supportService.getTicketsByUser(userId, page, size);
        return ResponseEntity.ok(ApiResponse.success("Tickets retrieved successfully", tickets));
    }

    @GetMapping("/tickets/{reference}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<TicketDTO>> getTicket(
            @PathVariable String reference,
            @RequestParam Long userId) {

        TicketDTO ticket = supportService.getTicketByReference(reference, userId);
        return ResponseEntity.ok(ApiResponse.success("Ticket retrieved successfully", ticket));
    }
}
