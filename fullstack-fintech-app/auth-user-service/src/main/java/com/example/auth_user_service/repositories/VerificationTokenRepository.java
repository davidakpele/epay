package com.example.auth_user_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.auth_user_service.models.VerificationToken;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    @Query("SELECT v FROM VerificationToken v WHERE v.token=:token")
    VerificationToken findByToken(String token);
}
