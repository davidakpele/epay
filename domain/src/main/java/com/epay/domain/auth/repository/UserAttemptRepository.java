package com.epay.domain.auth.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.epay.domain.auth.entity.UserAttempt;

@Repository
public interface UserAttemptRepository extends JpaRepository<UserAttempt, Long> {

    List<UserAttempt> findByTimestampAfter(Instant minus);

    @Query("SELECT b FROM UserAttempt b WHERE b.userId=:id")
    Optional<UserAttempt> findFirstByUserIdOrderByCreatedOnDesc(Long id);
 
    @Query("SELECT b FROM UserAttempt b WHERE b.userId=:id ORDER BY b.createdOn DESC")
    List<UserAttempt> findByUserId(Long id);
    
    @Query("SELECT b FROM UserAttempt b WHERE b.userId=:id")
    List<UserAttempt> findByUserIdInList(Long id);
}
