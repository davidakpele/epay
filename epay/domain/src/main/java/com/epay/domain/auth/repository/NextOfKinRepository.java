package com.epay.domain.auth.repository;

import com.epay.domain.auth.entity.NextOfKin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface NextOfKinRepository extends JpaRepository<NextOfKin, Long> {

    @Query("SELECT n FROM NextOfKin n WHERE n.user.id = :userId")
    Optional<NextOfKin> findByUserId(@Param("userId") Long userId);

    @Query("SELECT CASE WHEN COUNT(n) > 0 THEN true ELSE false END " +
           "FROM NextOfKin n WHERE n.user.id = :userId")
    boolean existsByUserId(@Param("userId") Long userId);
}
