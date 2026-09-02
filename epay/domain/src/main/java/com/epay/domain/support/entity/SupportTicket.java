package com.epay.domain.support.entity;

import com.epay.domain.support.enums.TicketCategory;
import com.epay.domain.support.enums.TicketPriority;
import com.epay.domain.support.enums.TicketStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "support_tickets", indexes = {
        @Index(name = "idx_ticket_user_id",     columnList = "user_id"),
        @Index(name = "idx_ticket_status",       columnList = "status"),
        @Index(name = "idx_ticket_category",     columnList = "category"),
        @Index(name = "idx_ticket_assigned_to",  columnList = "assigned_to"),
        @Index(name = "idx_ticket_created_at",   columnList = "created_at"),
        @Index(name = "idx_ticket_reference",    columnList = "ticket_reference", unique = true)
})
public class SupportTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ticket_seq")
    @SequenceGenerator(name = "ticket_seq", sequenceName = "ticket_sequence", allocationSize = 1)
    private Long id;

    @Column(name = "ticket_reference", nullable = false, unique = true, length = 30)
    private String ticketReference;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "user_email", length = 255)
    private String userEmail;

    @Column(name = "user_username", length = 100)
    private String userUsername;

    @Column(nullable = false, length = 200)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TicketStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TicketCategory category;

    @Column(name = "assigned_to")
    private Long assignedTo;

    @Column(name = "assigned_to_name", length = 200)
    private String assignedToName;

    @Column(name = "related_transaction_id", length = 50)
    private String relatedTransactionId;

    @Column(name = "internal_note", columnDefinition = "TEXT")
    private String internalNote;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "first_response_at")
    private LocalDateTime firstResponseAt;

    @Column(name = "escalated_at")
    private LocalDateTime escalatedAt;

    @Column(name = "escalated_by")
    private Long escalatedBy;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<TicketReply> replies = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Long version;
}
