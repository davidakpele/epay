package com.example.admin_api_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.example.admin_api_service.models.accessAndSecurity.AdminPasswordHistory;

@Repository
public interface AdminPasswordHistoryRepository extends JpaRepository<AdminPasswordHistory, String> {
    List<AdminPasswordHistory> findAllByAdminUserIdOrderByCreatedOnDesc(String adminUserId);
    Optional<AdminPasswordHistory> findTopByAdminUserIdOrderByCreatedOnDesc(String adminUserId);
 
    @Query(value = "SELECT * FROM admin_password_histories " +
                   "WHERE admin_user_id = :adminUserId " +
                   "ORDER BY created_on DESC LIMIT :limit", nativeQuery = true)
    List<AdminPasswordHistory> findTopNByAdminUserIdOrderByCreatedOnDesc(
            @Param("adminUserId") String adminUserId,
            @Param("limit") int limit);
}