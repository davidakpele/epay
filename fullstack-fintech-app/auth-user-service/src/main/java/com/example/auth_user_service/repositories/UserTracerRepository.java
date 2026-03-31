package com.example.auth_user_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.auth_user_service.models.UserTracer;

import java.util.Optional;

@Repository
public interface UserTracerRepository extends JpaRepository<UserTracer, Long> {

    Optional<UserTracer> findByUserId(Long userId);

    Optional<UserTracer> findBySessionId(String sessionId);

    boolean existsByUserIdAndActiveTrue(Long userId);

    void deleteBySessionId(String sessionId);

    void deleteByUserId(Long userId);
}
