package com.epay.support.service;

import com.epay.common.exception.BadRequestException;
import com.epay.common.exception.ErrorCode;
import com.epay.common.exception.ResourceNotFoundException;
import com.epay.common.interfaces.UserLookupPort;
import com.epay.domain.support.dto.*;
import com.epay.domain.support.entity.SupportArticle;
import com.epay.domain.support.entity.SupportFaq;
import com.epay.domain.support.entity.SupportTicket;
import com.epay.domain.support.entity.TicketReply;
import com.epay.domain.support.enums.TicketPriority;
import com.epay.domain.support.enums.TicketStatus;
import com.epay.domain.support.input.CreateTicketRequest;
import com.epay.domain.support.repository.SupportArticleRepository;
import com.epay.domain.support.repository.SupportFaqRepository;
import com.epay.domain.support.repository.SupportTicketRepository;
import com.epay.domain.support.repository.TicketReplyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupportService {

    private final SupportArticleRepository articleRepository;
    private final SupportFaqRepository     faqRepository;
    private final SupportTicketRepository  ticketRepository;
    private final TicketReplyRepository    replyRepository;
    private final UserLookupPort           userLookupPort;

    // ── Articles ──────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ArticleDTO> getAllArticles() {
        return articleRepository.findByActiveTrueOrderByCreatedAtDesc()
                .stream().map(this::toArticleDTO).toList();
    }

    @Transactional(readOnly = true)
    public ArticleDTO getArticleBySlug(String slug) {
        return articleRepository.findBySlugAndActiveTrue(slug)
                .map(this::toArticleDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found"));
    }

    // ── FAQs ──────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<FaqCategoryDTO> getAllFaqsGrouped() {
        List<SupportFaq> all = faqRepository.findByActiveTrueOrderByCategoryAscSortOrderAsc();

        // Group by category preserving insertion order
        Map<String, List<FaqDTO>> grouped = new LinkedHashMap<>();
        for (SupportFaq faq : all) {
            grouped.computeIfAbsent(faq.getCategory(), k -> new ArrayList<>())
                   .add(toFaqDTO(faq));
        }

        return grouped.entrySet().stream()
                .map(e -> FaqCategoryDTO.builder()
                        .category(e.getKey())
                        .items(e.getValue())
                        .build())
                .collect(Collectors.toList());
    }

    // ── Tickets ───────────────────────────────────────────────────────────────

    @Transactional
    public TicketDTO createTicket(CreateTicketRequest req) {
        if (!userLookupPort.existsActiveUser(req.getUserId())) {
            throw new ResourceNotFoundException("User not found or account inactive");
        }

        String ref = generateReference();

        // Auto-escalate FRAUD_REPORT to HIGH priority
        TicketPriority priority = req.getPriority() != null ? req.getPriority() : TicketPriority.MEDIUM;
        if (req.getCategory() != null && req.getCategory().name().equals("FRAUD_REPORT")) {
            priority = TicketPriority.HIGH;
        }

        String email    = userLookupPort.findEmailByUserId(req.getUserId()).orElse(null);
        String fullName = userLookupPort.findFullNameByUserId(req.getUserId()).orElse(null);

        SupportTicket ticket = SupportTicket.builder()
                .ticketReference(ref)
                .userId(req.getUserId())
                .userEmail(email)
                .userUsername(fullName)
                .subject(req.getSubject().trim())
                .description(req.getDescription().trim())
                .status(TicketStatus.OPEN)
                .priority(priority)
                .category(req.getCategory())
                .relatedTransactionId(req.getRelatedTransactionId())
                .build();

        SupportTicket saved = ticketRepository.save(ticket);
        log.info("[Support] Ticket created ref={} userId={} category={}",
                ref, req.getUserId(), req.getCategory());

        return toTicketDTO(saved, List.of());
    }

    @Transactional(readOnly = true)
    public Page<TicketDTO> getTicketsByUser(Long userId, int page, int size) {
        return ticketRepository.findByUserId(
                userId,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        ).map(t -> toTicketDTO(t, replyRepository.findPublicByTicketId(t.getId())));
    }

    @Transactional(readOnly = true)
    public TicketDTO getTicketByReference(String reference, Long userId) {
        SupportTicket ticket = ticketRepository.findByTicketReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        if (!ticket.getUserId().equals(userId)) {
            throw new BadRequestException("Ticket does not belong to this user", ErrorCode.UNAUTHORIZED_ACCESS);
        }

        List<TicketReply> replies = replyRepository.findPublicByTicketId(ticket.getId());
        return toTicketDTO(ticket, replies);
    }

    // ── Mapping ───────────────────────────────────────────────────────────────

    private ArticleDTO toArticleDTO(SupportArticle a) {
        return ArticleDTO.builder()
                .id(a.getId())
                .slug(a.getSlug())
                .title(a.getTitle())
                .summary(a.getSummary())
                .content(a.getContent())
                .category(a.getCategory())
                .active(a.isActive())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }

    private FaqDTO toFaqDTO(SupportFaq f) {
        return FaqDTO.builder()
                .id(f.getId())
                .category(f.getCategory())
                .sortOrder(f.getSortOrder())
                .question(f.getQuestion())
                .answer(f.getAnswer())
                .authorName(f.getAuthorName())
                .active(f.isActive())
                .createdAt(f.getCreatedAt())
                .build();
    }

    private TicketDTO toTicketDTO(SupportTicket t, List<TicketReply> replies) {
        List<TicketDTO.ReplyDTO> replyDTOs = replies.stream()
                .filter(r -> !r.isInternal())
                .map(r -> TicketDTO.ReplyDTO.builder()
                        .id(r.getId())
                        .authorId(r.getAuthorId())
                        .authorName(r.getAuthorName())
                        .staffReply(r.isStaffReply())
                        .internal(r.isInternal())
                        .message(r.getMessage())
                        .createdAt(r.getCreatedAt())
                        .build())
                .toList();

        return TicketDTO.builder()
                .id(t.getId())
                .ticketReference(t.getTicketReference())
                .userId(t.getUserId())
                .userEmail(t.getUserEmail())
                .subject(t.getSubject())
                .description(t.getDescription())
                .status(t.getStatus())
                .priority(t.getPriority())
                .category(t.getCategory())
                .relatedTransactionId(t.getRelatedTransactionId())
                .resolvedAt(t.getResolvedAt())
                .closedAt(t.getClosedAt())
                .firstResponseAt(t.getFirstResponseAt())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .replies(replyDTOs)
                .build();
    }

    private String generateReference() {
        return "TKT-" + UUID.randomUUID().toString()
                .replace("-", "").substring(0, 10).toUpperCase();
    }
}
