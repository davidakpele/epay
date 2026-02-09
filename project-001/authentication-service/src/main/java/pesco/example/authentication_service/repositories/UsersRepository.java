package pesco.example.authentication_service.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pesco.example.authentication_service.models.Users;

@Repository
public interface UsersRepository extends JpaRepository<Users, Long> {

    @Query("SELECT u FROM Users u WHERE u.username = :username")
    Optional<Users> findByUsername(@Param("username") String username);

    Optional<Users> findByEmail(String email);

    @Query("SELECT u FROM Users u LEFT JOIN FETCH u.records WHERE u.id = :id")
    Users findUserWithRecordById(@Param("id") Long id);

    @Query("SELECT u FROM Users u WHERE u.username IN :usernames")
    List<Users> findByUsernameIn(@Param("usernames") List<String> usernames);

    @Modifying
    @Transactional
    @Query("DELETE FROM Users u WHERE u.id IN :ids")
    void deleteUserByIds(@Param("ids") List<Long> ids);

    @Query("SELECT u FROM Users u ORDER BY u.createdOn DESC")
    List<Users> findAllWithPagination(Pageable pageable);

    @Query("SELECT u FROM Users u WHERE u.id > :lastId ORDER BY u.id ASC")
    List<Users> findUsersAfterId(@Param("lastId") Long lastId, Pageable pageable);

    @Query("SELECT u FROM Users u ORDER BY u.id ASC")
    List<Users> findAllUsersPaginated(Pageable pageable);

    @Query("SELECT u.id FROM Users u WHERE u.createdOn >= :date ORDER BY u.createdOn DESC")
    List<Long> findRecentUserIds(@Param("date") LocalDateTime date);

    @Query("SELECT COALESCE(MIN(u.id), 0) FROM Users u")
    Optional<Long> findMinUserId();
}