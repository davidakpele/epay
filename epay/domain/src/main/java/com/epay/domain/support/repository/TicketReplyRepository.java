package com.epay.domain.support.repository;

import com.epay.domain.support.entity.TicketReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketReplyRepository extends JpaRepository<TicketReply, Long> {

    @Query("SELECT r FROM TicketReply r WHERE r.ticket.id = :ticketId ORDER BY r.createdAt ASC")
    List<TicketReply> findByTicketId(@Param("ticketId") Long ticketId);

    @Query("SELECT r FROM TicketReply r WHERE r.ticket.id = :ticketId AND r.internal = false ORDER BY r.createdAt ASC")
    List<TicketReply> findPublicByTicketId(@Param("ticketId") Long ticketId);
}
