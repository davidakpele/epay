package com.epay.domain.liquidity.repository;

import com.epay.domain.liquidity.entity.LiquidityConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LiquidityConfigRepository extends JpaRepository<LiquidityConfig, Long> {
    Optional<LiquidityConfig> findByGateway(String gateway);
}
