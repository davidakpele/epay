package pesco.example.authentication_service.repositories;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pesco.example.authentication_service.models.UserAttempt;

@Repository
public interface UserAttemptRepository extends JpaRepository<UserAttempt, Long> {

    List<UserAttempt> findByTimestampAfter(Instant minus);

    @Query("SELECT b FROM UserAttempt b WHERE b.userId=:id")
    UserAttempt findByUserId(Long id);

    @Query("SELECT b FROM UserAttempt b WHERE b.userId=:id")
    List<UserAttempt> findByUserIdInList(Long id);
}
