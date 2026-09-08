package com.epay.admin.service;

import com.epay.common.exception.*;
import com.epay.domain.admin.input.*;
import com.epay.domain.auth.entity.User;
import com.epay.domain.auth.entity.UserRecord;
import com.epay.domain.auth.repository.UserRecordRepository;
import com.epay.domain.auth.repository.UserRepository;
import com.epay.domain.support.dto.TicketDTO;
import com.epay.domain.support.entity.SupportTicket;
import com.epay.domain.support.entity.TicketReply;
import com.epay.domain.support.enums.TicketStatus;
import com.epay.domain.support.repository.SupportTicketRepository;
import com.epay.domain.support.repository.TicketReplyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminTicketService {

    private final SupportTicketRepository ticketRepository;
    private final TicketReplyRepository   replyRepository;
    private final UserRepository          userRepository;
    private final UserRecordRepository    userRecordRepository;

    private final AtomicLong ticketCounter = new AtomicLong(0L);

    @Transactional
    public TicketDTO createTicket(CreateTicketRequest request, Long staffId) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserRecord record = userRecordRepository.findByUserId(user.getId()).orElse(null);

        SupportTicket ticket = SupportTicket.builder()
                .ticketReference(generateReference())
                .userId(user.getId())
                .userEmail(user.getEmail())
                .userUsername(user.getUsername())
                .subject(request.getSubject())
                .description(request.getDescription())
                .status(TicketStatus.OPEN)
                .priority(request.getPriority())
                .category(request.getCategory())
                .relatedTransactionId(request.getRelatedTransactionId())
                .assignedTo(staffId)
                .assignedToName(staffName(staffId))
                .build();

        ticketRepository.save(ticket);
        log.info("[Ticket] Created: ref={} userId={} by={}", ticket.getTicketReference(), user.getId(), staffId);
        return toDTO(ticket, true);
    }

    @Transactional
    public TicketDTO submitUserTicket(CreateTicketRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        SupportTicket ticket = SupportTicket.builder()
                .ticketReference(generateReference())
                .userId(user.getId())
                .userEmail(user.getEmail())
                .userUsername(user.getUsername())
                .subject(request.getSubject())
                .description(request.getDescription())
                .status(TicketStatus.OPEN)
                .priority(request.getPriority())
                .category(request.getCategory())
                .relatedTransactionId(request.getRelatedTransactionId())
                .build();

        ticketRepository.save(ticket);
        log.info("[Ticket] User submitted: ref={} userId={}", ticket.getTicketReference(), user.getId());
        return toDTO(ticket, false);
    }

    public Page<TicketDTO> listAll(Pageable pageable) {
        return ticketRepository.findAll(pageable).map(t -> toDTO(t, true));
    }

    public Page<TicketDTO> listByStatus(TicketStatus status, Pageable pageable) {
        return ticketRepository.findByStatus(status, pageable).map(t -> toDTO(t, true));
    }

    public Page<TicketDTO> listByUser(Long userId, Pageable pageable) {
        return ticketRepository.findByUserId(userId, pageable).map(t -> toDTO(t, false));
    }

    public Page<TicketDTO> listAssignedTo(Long staffId, Pageable pageable) {
        return ticketRepository.findByAssignedTo(staffId, pageable).map(t -> toDTO(t, true));
    }

    public Page<TicketDTO> search(
            TicketStatus status,
            com.epay.domain.support.enums.TicketCategory category,
            com.epay.domain.support.enums.TicketPriority priority,
            Long assignedTo, Long userId,
            LocalDateTime from, LocalDateTime to,
            Pageable pageable) {
        return ticketRepository.search(status, category, priority, assignedTo, userId, from, to, pageable)
                .map(t -> toDTO(t, true));
    }

    public TicketDTO getByReference(String reference, boolean includeInternal) {
        SupportTicket ticket = ticketRepository.findByTicketReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + reference));
        return toDTO(ticket, includeInternal);
    }

    public TicketDTO getById(Long id, boolean includeInternal) {
        SupportTicket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));
        return toDTO(ticket, includeInternal);
    }

    @Transactional
    public TicketDTO updateTicket(Long ticketId, UpdateTicketRequest request, Long staffId) {
        SupportTicket ticket = requireTicket(ticketId);

        if (request.getStatus()   != null) {
            applyStatusTransition(ticket, request.getStatus(), staffId);
        }
        if (request.getPriority() != null) ticket.setPriority(request.getPriority());
        if (request.getCategory() != null) ticket.setCategory(request.getCategory());
        if (request.getSubject()  != null) ticket.setSubject(request.getSubject());
        if (request.getInternalNote() != null) ticket.setInternalNote(request.getInternalNote());

        if (request.getAssignedTo() != null) {
            ticket.setAssignedTo(request.getAssignedTo());
            ticket.setAssignedToName(staffName(request.getAssignedTo()));
        }

        ticketRepository.save(ticket);
        log.info("[Ticket] Updated: id={} status={} by={}", ticketId, ticket.getStatus(), staffId);
        return toDTO(ticket, true);
    }

    @Transactional
    public TicketDTO escalateTicket(Long ticketId, Long escalatedBy) {
        SupportTicket ticket = requireTicket(ticketId);
        if (ticket.getStatus() == TicketStatus.RESOLVED || ticket.getStatus() == TicketStatus.CLOSED)
            throw new BadRequestException("Cannot escalate a resolved or closed ticket", ErrorCode.INVALID_INPUT);
        ticket.setStatus(TicketStatus.ESCALATED);
        ticket.setEscalatedAt(LocalDateTime.now());
        ticket.setEscalatedBy(escalatedBy);
        ticketRepository.save(ticket);
        log.info("[Ticket] Escalated: id={} by={}", ticketId, escalatedBy);
        return toDTO(ticket, true);
    }

    @Transactional
    public TicketDTO resolveTicket(Long ticketId, Long staffId) {
        SupportTicket ticket = requireTicket(ticketId);
        ticket.setStatus(TicketStatus.RESOLVED);
        ticket.setResolvedAt(LocalDateTime.now());
        ticketRepository.save(ticket);
        log.info("[Ticket] Resolved: id={} by={}", ticketId, staffId);
        return toDTO(ticket, true);
    }

    @Transactional
    public TicketDTO closeTicket(Long ticketId, Long staffId) {
        SupportTicket ticket = requireTicket(ticketId);
        ticket.setStatus(TicketStatus.CLOSED);
        ticket.setClosedAt(LocalDateTime.now());
        ticketRepository.save(ticket);
        log.info("[Ticket] Closed: id={} by={}", ticketId, staffId);
        return toDTO(ticket, true);
    }

    @Transactional
    public TicketDTO reopenTicket(Long ticketId, Long staffId) {
        SupportTicket ticket = requireTicket(ticketId);
        if (ticket.getStatus() != TicketStatus.RESOLVED && ticket.getStatus() != TicketStatus.CLOSED)
            throw new BadRequestException("Only resolved or closed tickets can be reopened", ErrorCode.INVALID_INPUT);
        ticket.setStatus(TicketStatus.REOPENED);
        ticket.setResolvedAt(null);
        ticket.setClosedAt(null);
        ticketRepository.save(ticket);
        log.info("[Ticket] Reopened: id={} by={}", ticketId, staffId);
        return toDTO(ticket, true);
    }

    @Transactional
    public TicketDTO addStaffReply(Long ticketId, TicketReplyRequest request, Long staffId) {
        SupportTicket ticket = requireTicket(ticketId);

        if (ticket.getStatus() == TicketStatus.CLOSED)
            throw new BadRequestException("Cannot reply to a closed ticket", ErrorCode.INVALID_INPUT);

        if (ticket.getFirstResponseAt() == null) {
            ticket.setFirstResponseAt(LocalDateTime.now());
        }

        if (ticket.getStatus() == TicketStatus.OPEN || ticket.getStatus() == TicketStatus.REOPENED) {
            ticket.setStatus(TicketStatus.IN_PROGRESS);
        }

        TicketReply reply = TicketReply.builder()
                .ticket(ticket)
                .authorId(staffId)
                .authorName(staffName(staffId))
                .staffReply(true)
                .internal(request.isInternal())
                .message(request.getMessage())
                .build();
        replyRepository.save(reply);
        ticketRepository.save(ticket);

        log.info("[Ticket] Staff reply added: ticketId={} internal={} by={}", ticketId, request.isInternal(), staffId);
        return toDTO(ticket, true);
    }

    @Transactional
    public TicketDTO addUserReply(Long ticketId, TicketReplyRequest request, Long userId) {
        SupportTicket ticket = requireTicket(ticketId);

        if (!ticket.getUserId().equals(userId))
            throw new ForbiddenException("You are not authorised to reply to this ticket",
                    ErrorCode.FORBIDDEN_ACCESS);
        if (ticket.getStatus() == TicketStatus.CLOSED)
            throw new BadRequestException("Cannot reply to a closed ticket", ErrorCode.INVALID_INPUT);

        if (ticket.getStatus() == TicketStatus.PENDING_USER) {
            ticket.setStatus(TicketStatus.OPEN);
        }

        UserRecord record = userRecordRepository.findByUserId(userId).orElse(null);
        String authorName = record != null
                ? record.getFirstName() + " " + record.getLastName()
                : "User #" + userId;

        TicketReply reply = TicketReply.builder()
                .ticket(ticket)
                .authorId(userId)
                .authorName(authorName)
                .staffReply(false)
                .internal(false)
                .message(request.getMessage())
                .build();
        replyRepository.save(reply);
        ticketRepository.save(ticket);

        log.info("[Ticket] User reply added: ticketId={} userId={}", ticketId, userId);
        return toDTO(ticket, false);
    }

    @Transactional
    public void deleteTicket(Long ticketId) {
        SupportTicket ticket = requireTicket(ticketId);
        ticketRepository.delete(ticket);
        log.info("[Ticket] Deleted: id={}", ticketId);
    }

    public java.util.Map<String, Object> getTicketStats() {
        java.util.Map<String, Object> stats = new java.util.LinkedHashMap<>();
        stats.put("open",        ticketRepository.countByStatus(TicketStatus.OPEN));
        stats.put("inProgress",  ticketRepository.countByStatus(TicketStatus.IN_PROGRESS));
        stats.put("pendingUser", ticketRepository.countByStatus(TicketStatus.PENDING_USER));
        stats.put("resolved",    ticketRepository.countByStatus(TicketStatus.RESOLVED));
        stats.put("closed",      ticketRepository.countByStatus(TicketStatus.CLOSED));
        stats.put("escalated",   ticketRepository.countByStatus(TicketStatus.ESCALATED));
        stats.put("total",       ticketRepository.count());
        return stats;
    }

    private SupportTicket requireTicket(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));
    }

    private void applyStatusTransition(SupportTicket ticket, TicketStatus newStatus, Long staffId) {
        ticket.setStatus(newStatus);
        switch (newStatus) {
            case RESOLVED    -> ticket.setResolvedAt(LocalDateTime.now());
            case CLOSED      -> ticket.setClosedAt(LocalDateTime.now());
            case ESCALATED   -> { ticket.setEscalatedAt(LocalDateTime.now()); ticket.setEscalatedBy(staffId); }
            default          -> { }
        }
    }

    private String generateReference() {
        long count = ticketRepository.count() + 1 + ticketCounter.incrementAndGet();
        return String.format("TKT-%d%05d", Year.now().getValue(), count);
    }

    private String staffName(Long staffId) {
        if (staffId == null) return null;
        return userRecordRepository.findByUserId(staffId)
                .map(r -> r.getFirstName() + " " + r.getLastName())
                .orElseGet(() -> userRepository.findById(staffId)
                        .map(User::getUsername).orElse("Staff #" + staffId));
    }

    private TicketDTO toDTO(SupportTicket ticket, boolean includeInternal) {
        List<TicketReply> replies = includeInternal
                ? replyRepository.findByTicketId(ticket.getId())
                : replyRepository.findPublicByTicketId(ticket.getId());

        List<TicketDTO.ReplyDTO> replyDTOs = replies.stream()
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
                .id(ticket.getId())
                .ticketReference(ticket.getTicketReference())
                .userId(ticket.getUserId())
                .userEmail(ticket.getUserEmail())
                .userUsername(ticket.getUserUsername())
                .subject(ticket.getSubject())
                .description(ticket.getDescription())
                .status(ticket.getStatus())
                .priority(ticket.getPriority())
                .category(ticket.getCategory())
                .assignedTo(ticket.getAssignedTo())
                .assignedToName(ticket.getAssignedToName())
                .relatedTransactionId(ticket.getRelatedTransactionId())
                .internalNote(includeInternal ? ticket.getInternalNote() : null)
                .resolvedAt(ticket.getResolvedAt())
                .closedAt(ticket.getClosedAt())
                .firstResponseAt(ticket.getFirstResponseAt())
                .escalatedAt(ticket.getEscalatedAt())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .replies(replyDTOs)
                .build();
    }
}
