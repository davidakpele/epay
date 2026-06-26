package pesco.example.virtual_card_service.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;
import pesco.example.virtual_card_service.models.CardLimit;

@Repository
public interface CardLimitRepository extends JpaRepository<CardLimit, String> {

    @Query("SELECT cl FROM CardLimit cl WHERE cl.cardId = :cardId")
    Optional<CardLimit> findByCardId(@Param("cardId") String cardId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM CardLimit c WHERE c.cardId = :cardId")
    void deleteByCardId(@Param("cardId") String cardId);

}