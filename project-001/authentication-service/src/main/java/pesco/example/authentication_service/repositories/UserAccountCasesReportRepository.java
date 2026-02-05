package pesco.example.authentication_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pesco.example.authentication_service.models.UserAccountCases;

@Repository
public interface UserAccountCasesReportRepository extends JpaRepository<UserAccountCases, Long> {

    @Query("SELECT r FROM UserAccountCases r WHERE r.user.id = :userId")
    UserAccountCases findByUserId(Long userId);

}
