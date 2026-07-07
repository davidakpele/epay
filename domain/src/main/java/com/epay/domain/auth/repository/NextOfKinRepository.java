package com.epay.domain.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.epay.domain.auth.entity.NextOfKin;

import java.util.Optional;

@Repository
public interface NextOfKinRepository extends JpaRepository<NextOfKin, Long> {

    Optional<NextOfKin> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}
