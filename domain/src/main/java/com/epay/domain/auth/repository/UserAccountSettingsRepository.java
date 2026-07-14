package com.epay.domain.auth.repository;

import com.epay.domain.auth.entity.UserAccountSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserAccountSettingsRepository extends JpaRepository<UserAccountSettings, Long> {

    @Query("SELECT s FROM UserAccountSettings s WHERE s.user.id = :userId")
    Optional<UserAccountSettings> findByUserId(@Param("userId") Long userId);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " +
           "FROM UserAccountSettings s WHERE s.user.id = :userId")
    boolean existsByUserId(@Param("userId") Long userId);
}
