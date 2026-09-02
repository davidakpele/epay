package com.epay.domain.support.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ticket_replies", indexes = {
        @Index(name = "idx_reply_ticket_id",  columnList = "ticket_id"),
        @Index(name = "idx_reply_author_id",  columnList = "author_id"),
        @Index(name = "idx_reply_created_at", columnList = "created_at")
})
public class TicketReply {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ticket_reply_seq")
    @SequenceGenerator(name = "ticket_reply_seq", sequenceName = "ticket_reply_sequence", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false)
    private SupportTicket ticket;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(name = "author_name", length = 200)
    private String authorName;

    @Column(name = "is_staff_reply", nullable = false)
    private boolean staffReply;

    @Column(name = "is_internal", nullable = false)
    @Builder.Default
    private boolean internal = false;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;
}
