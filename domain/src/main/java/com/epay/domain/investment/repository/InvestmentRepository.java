package com.epay.domain.investment.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.epay.domain.investment.entity.Investment;
import com.epay.domain.investment.enums.InvestmentStatus;

import java.time.Instant;
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

    List<Investment> findMaturedUnpaid(Instant now);
}