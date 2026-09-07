package com.epay.domain.support.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Persists each turn of a user ↔ AI support conversation.
 * Grouped by {@code sessionId} so the full history can be
 * replayed as context for the AI on the next message.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "chat_messages", indexes = {
        @Index(name = "idx_chat_session",    columnList = "session_id"),
        @Index(name = "idx_chat_user_id",    columnList = "user_id"),
        @Index(name = "idx_chat_created_at", columnList = "created_at")
})
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "chat_msg_seq")
    @SequenceGenerator(name = "chat_msg_seq", sequenceName = "chat_message_sequence", allocationSize = 1)
    private Long id;

    /** UUID supplied by the frontend to group messages into one chat session. */
    @Column(name = "session_id", nullable = false, length = 64)
    private String sessionId;

    /** Nullable — guest users (not logged in) will have null here. */
    @Column(name = "user_id")
    private Long userId;

    /** "user" or "assistant" — mirrors the OpenAI / Anthropic convention. */
    @Column(nullable = false, length = 20)
    private String role;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;
}
