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
@Table(name = "support_faqs", indexes = {
        @Index(name = "idx_faq_category", columnList = "category"),
        @Index(name = "idx_faq_active",   columnList = "active")
})
public class SupportFaq {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "faq_seq")
    @SequenceGenerator(name = "faq_seq", sequenceName = "faq_sequence", allocationSize = 1)
    private Long id;

    @Column(nullable = false, length = 80)
    private String category;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private int sortOrder = 0;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String answer;

    @Column(name = "author_name", length = 100)
    @Builder.Default
    private String authorName = "ePay Support Team";

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;
}
