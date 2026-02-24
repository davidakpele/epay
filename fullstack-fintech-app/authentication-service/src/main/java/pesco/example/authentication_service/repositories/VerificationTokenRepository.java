package pesco.example.authentication_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pesco.example.authentication_service.models.VerificationToken;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    @Query("SELECT v FROM VerificationToken v WHERE v.token=:token")
    VerificationToken findByToken(String token);
}
