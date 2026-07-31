package com.epay.domain.investment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.epay.domain.investment.entity.Investment;
import com.epay.domain.investment.enums.InvestmentStatus;
import java.time.LocalDateTime;
import java.util.List;

public interface InvestmentRepository extends JpaRepository<Investment, Long> {

    List<Investment> findByUserIdOrderByCreatedOnDesc(Long userId);

    List<Investment> findByStatus(InvestmentStatus status);

    List<Investment> findByStatusAndMaturityDateLessThanEqual(
            InvestmentStatus status,
            LocalDateTime maturityDate
    );

    List<Investment> findByUserId(Long userId);

    @Query("""
        SELECT i
        FROM Investment i
        WHERE i.status = com.epay.domain.investment.enums.InvestmentStatus.ACTIVE
          AND i.maturityDate <= :now
    """)
    List<Investment> findMaturedUnpaid(@Param("now") LocalDateTime now);
}