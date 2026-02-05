package pesco.example.authentication_service.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import pesco.example.authentication_service.models.UserRecord;

@Repository
public interface UserRecordRepository extends JpaRepository<UserRecord, Long> {
    
    @Query("SELECT r FROM UserRecord r WHERE r.user.id = :userId")
    Optional<UserRecord> findByUserId(@Param("userId") Long userId);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM UserRecord r WHERE r.user.id = :userId AND r.locked = true")
    boolean isUserAccountLocked(@Param("userId") Long userId);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM UserRecord r WHERE r.user.id = :userId AND r.isBlocked = true")
    boolean isUserAccountBlocked(@Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserRecord r WHERE r.user.id IN :ids")
    void deleteUserRecordByIds(@Param("ids") List<Long> ids);

    @Query("SELECT r FROM UserRecord r WHERE r.telephone = :telephone")
    Optional<UserRecord> findByTelephone(String telephone);
}