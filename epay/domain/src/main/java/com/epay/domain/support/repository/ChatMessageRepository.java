package com.epay.domain.support.repository;

import com.epay.domain.support.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /** Returns all messages in a session ordered oldest-first — used to build AI context. */
    List<ChatMessage> findBySessionIdOrderByCreatedAtAsc(String sessionId);

    /** Returns the most recent N messages for a session to avoid unbounded context. */
    List<ChatMessage> findTop20BySessionIdOrderByCreatedAtAsc(String sessionId);
}
