package com.epay.maintenance.repository;

import com.epay.domain.maintenance.entity.MaintenanceFeeConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MaintenanceFeeConfigRepository extends JpaRepository<MaintenanceFeeConfig, Long> {

    @Query("SELECT c FROM MaintenanceFeeConfig c WHERE UPPER(c.currencyCode) = UPPER(:code) AND c.active = true")
    Optional<MaintenanceFeeConfig> findActiveByCurrencyCode(@Param("code") String currencyCode);

    @Query("SELECT c FROM MaintenanceFeeConfig c WHERE c.active = true ORDER BY c.currencyCode")
    List<MaintenanceFeeConfig> findAllActive();

    @Query("SELECT c FROM MaintenanceFeeConfig c WHERE UPPER(c.currencyCode) = UPPER(:code) ORDER BY c.createdAt DESC")
    List<MaintenanceFeeConfig> findAllByCurrencyCode(@Param("code") String currencyCode);

    @Modifying
    @Query("UPDATE MaintenanceFeeConfig c SET c.active = false WHERE UPPER(c.currencyCode) = UPPER(:code)")
    void deactivateAllByCurrencyCode(@Param("code") String currencyCode);

    boolean existsByCurrencyCodeIgnoreCaseAndActiveTrue(String currencyCode);
}
