package com.epay.domain.auth.repository;

import org.springframework.stereotype.Repository;

import com.epay.domain.auth.entity.TwoFactorAuthentication;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface TwoFactorOTPRepository extends JpaRepository<TwoFactorAuthentication, Long> {

    @Query("select t from TwoFactorAuthentication t where t.userId = :userId")
    TwoFactorAuthentication findByUserId(@Param("userId") Long userId);

    @Query("select t from TwoFactorAuthentication t where t.token = :token")
    Optional<TwoFactorAuthentication> findByJwt(String token);

    @Query("select t from TwoFactorAuthentication t where t.otp = :otp")
    Optional<TwoFactorAuthentication> findByOTP(String otp);

}