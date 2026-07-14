package com.epay.domain.auth.repository;

import com.epay.domain.auth.entity.UserAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserAttemptRepository extends JpaRepository<UserAttempt, Long> {

    @Query("SELECT b FROM UserAttempt b WHERE b.userId = :id " +
           "ORDER BY b.createdOn DESC LIMIT 1")
    Optional<UserAttempt> findFirstByUserIdOrderByCreatedOnDesc(@Param("id") Long id);

    @Query("SELECT b FROM UserAttempt b WHERE b.userId = :id ORDER BY b.createdOn DESC")
    List<UserAttempt> findByUserId(@Param("id") Long id);
}
