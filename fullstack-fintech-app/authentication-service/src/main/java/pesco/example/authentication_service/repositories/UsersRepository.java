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

    @Query("SELECT MAX(u.id) FROM Users u")
    Optional<Long> findMaxId();

     // Total count by role (for USER role only, excluding ADMIN)
    @Query("SELECT COUNT(u) FROM Users u WHERE u.role = 'USER'")
    long countAllRegularUsers();

    // Active users (enabled = true)
    @Query("SELECT COUNT(u) FROM Users u WHERE u.enabled = true AND u.role = 'USER'")
    long countActiveUsers();

    // Inactive users (enabled = false)
    @Query("SELECT COUNT(u) FROM Users u WHERE u.enabled = false AND u.role = 'USER'")
    long countInactiveUsers();

    // New users per period — DAILY (last 30 days)
    @Query("""
        SELECT FUNCTION('DATE', u.createdOn)  AS label,
               COUNT(u)                        AS count
        FROM   Users u
        WHERE  u.createdOn >= :since
        AND    u.role = 'USER'
        GROUP  BY FUNCTION('DATE', u.createdOn)
        ORDER  BY FUNCTION('DATE', u.createdOn) ASC
        """)
    List<Object[]> countDailyRegistrations(@Param("since") LocalDateTime since);

    // New users per period — WEEKLY
    @Query("""
        SELECT FUNCTION('YEAR', u.createdOn)  AS year,
               FUNCTION('WEEK', u.createdOn)  AS week,
               COUNT(u)                        AS count
        FROM   Users u
        WHERE  u.createdOn >= :since
        AND    u.role = 'USER'
        GROUP  BY FUNCTION('YEAR', u.createdOn), FUNCTION('WEEK', u.createdOn)
        ORDER  BY FUNCTION('YEAR', u.createdOn), FUNCTION('WEEK', u.createdOn) ASC
        """)
    List<Object[]> countWeeklyRegistrations(@Param("since") LocalDateTime since);

    // New users per period — MONTHLY
    @Query("""
        SELECT FUNCTION('YEAR', u.createdOn)  AS year,
               FUNCTION('MONTH', u.createdOn) AS month,
               COUNT(u)                        AS count
        FROM   Users u
        WHERE  u.createdOn >= :since
        AND    u.role = 'USER'
        GROUP  BY FUNCTION('YEAR', u.createdOn), FUNCTION('MONTH', u.createdOn)
        ORDER  BY FUNCTION('YEAR', u.createdOn), FUNCTION('MONTH', u.createdOn) ASC
        """)
    List<Object[]> countMonthlyRegistrations(@Param("since") LocalDateTime since);

    // New users per period — YEARLY
    @Query("""
        SELECT FUNCTION('YEAR', u.createdOn) AS year,
               COUNT(u)                       AS count
        FROM   Users u
        WHERE  u.role = 'USER'
        GROUP  BY FUNCTION('YEAR', u.createdOn)
        ORDER  BY FUNCTION('YEAR', u.createdOn) ASC
        """)
    List<Object[]> countYearlyRegistrations();

    // KYC pending — users with incomplete profile
    @Query("""
        SELECT COUNT(r) FROM UserRecord r
        WHERE  r.isProfileComplete = false
        """)
    long countKycPending();
}