package com.epay.domain.auth.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.epay.domain.auth.entity.AuthorizeUserVerification;

@Repository
public interface AuthorizeUserVerificationRepository extends JpaRepository<AuthorizeUserVerification, Long> {

    @Query("""
    SELECT COUNT(a) > 0
    FROM AuthorizeUserVerification a, User u
    WHERE a.userId = u.id
    AND u.enabled = true
    AND a.id = :id
    """)
    boolean findUserById(Long id);

    @Query("select t from AuthorizeUserVerification t where t.userId = :id")
    Optional<AuthorizeUserVerification> findUserByIdOptional(Long id);

}
