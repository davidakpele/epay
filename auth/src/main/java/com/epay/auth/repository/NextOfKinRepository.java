package com.epay.auth.repository;

import com.epay.auth.domain.entity.NextOfKin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NextOfKinRepository extends JpaRepository<NextOfKin, Long> {

    Optional<NextOfKin> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}
