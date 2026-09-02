package com.epay.domain.support.repository;

import com.epay.domain.support.entity.SupportTicket;
import com.epay.domain.support.enums.TicketCategory;
import com.epay.domain.support.enums.TicketPriority;
import com.epay.domain.support.enums.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {

    Optional<SupportTicket> findByTicketReference(String ticketReference);

    Page<SupportTicket> findByUserId(Long userId, Pageable pageable);

    Page<SupportTicket> findByStatus(TicketStatus status, Pageable pageable);

    Page<SupportTicket> findByAssignedTo(Long assignedTo, Pageable pageable);

    Page<SupportTicket> findByStatusAndCategory(TicketStatus status, TicketCategory category, Pageable pageable);

    Page<SupportTicket> findByPriority(TicketPriority priority, Pageable pageable);

    @Query("""
           SELECT t FROM SupportTicket t
           WHERE (:status IS NULL OR t.status = :status)
             AND (:category IS NULL OR t.category = :category)
             AND (:priority IS NULL OR t.priority = :priority)
             AND (:assignedTo IS NULL OR t.assignedTo = :assignedTo)
             AND (:userId IS NULL OR t.userId = :userId)
             AND (:from IS NULL OR t.createdAt >= :from)
             AND (:to IS NULL OR t.createdAt <= :to)
           ORDER BY t.createdAt DESC
           """)
    Page<SupportTicket> search(
            @Param("status")     TicketStatus status,
            @Param("category")   TicketCategory category,
            @Param("priority")   TicketPriority priority,
            @Param("assignedTo") Long assignedTo,
            @Param("userId")     Long userId,
            @Param("from")       LocalDateTime from,
            @Param("to")         LocalDateTime to,
            Pageable pageable);

    long countByStatus(TicketStatus status);

    long countByAssignedToAndStatus(Long assignedTo, TicketStatus status);

    @Query("SELECT COUNT(t) FROM SupportTicket t WHERE t.status NOT IN ('RESOLVED','CLOSED') AND t.createdAt < :before")
    long countOverdue(@Param("before") LocalDateTime before);
}
