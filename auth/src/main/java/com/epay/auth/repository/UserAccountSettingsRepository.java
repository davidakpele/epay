package com.epay.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.epay.domain.auth.entity.UserAccountSettings;

import java.util.Optional;

@Repository
public interface UserAccountSettingsRepository extends JpaRepository<UserAccountSettings, Long> {
        
    @Query("SELECT r FROM UserAccountSettings r WHERE r.user.id = :userId")
    Optional<UserAccountSettings> findByUserId(@Param("userId") Long userId);
    @Query("SELECT r FROM UserAccountSettings r WHERE r.user.id = :userId")
    boolean existsByUserId(Long userId);
}