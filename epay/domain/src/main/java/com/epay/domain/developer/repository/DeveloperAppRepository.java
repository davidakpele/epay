package com.epay.domain.developer.repository;

import com.epay.domain.developer.entity.DeveloperApp;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeveloperAppRepository extends JpaRepository<DeveloperApp, Long> {
    List<DeveloperApp> findByOwnerUserId(Long ownerUserId);
    Page<DeveloperApp> findAllByOrderByCreatedAtDesc(Pageable pageable);
    boolean existsByOwnerUserIdAndAppName(Long ownerUserId, String appName);
}
