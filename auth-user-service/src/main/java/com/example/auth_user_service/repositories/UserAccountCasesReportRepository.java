package com.example.auth_user_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.auth_user_service.models.UserAccountCases;

@Repository
public interface UserAccountCasesReportRepository extends JpaRepository<UserAccountCases, Long> {
 
    @Query("SELECT r FROM UserAccountCases r WHERE r.userId = :userId")
    UserAccountCases findByUserId(Long userId);

}
