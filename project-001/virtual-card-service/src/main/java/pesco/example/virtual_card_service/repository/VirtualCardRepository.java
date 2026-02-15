package pesco.example.virtual_card_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pesco.example.virtual_card_service.enums.CardStatus;
import pesco.example.virtual_card_service.models.VirtualCard;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VirtualCardRepository extends JpaRepository<VirtualCard, String> {

    // Find by cardId (UUID)
    Optional<VirtualCard> findByCardId(String cardId);
    
    Optional<VirtualCard> findByCardIdAndDeletedAtIsNull(String cardId);

    // Find by card number
    Optional<VirtualCard> findByCardNumber(String cardNumber);

    // Find all cards for a user
    List<VirtualCard> findByUserId(Long userId);
    
    List<VirtualCard> findByUserIdAndDeletedAtIsNull(Long userId);
    
    Page<VirtualCard> findByUserIdAndDeletedAtIsNull(Long userId, Pageable pageable);

    // Find active cards for a user
    List<VirtualCard> findByUserIdAndStatusAndDeletedAtIsNull(Long userId, CardStatus status);

    // Find cards by status
    List<VirtualCard> findByStatus(CardStatus status);
    
    List<VirtualCard> findByStatusAndDeletedAtIsNull(CardStatus status);

    // Find expired cards
    @Query("SELECT v FROM VirtualCard v WHERE v.expiresAt < :now AND v.status != 'EXPIRED' AND v.deletedAt IS NULL")
    List<VirtualCard> findExpiredCards(@Param("now") LocalDateTime now);

    // Find cards expiring soon
    @Query("SELECT v FROM VirtualCard v WHERE v.expiresAt BETWEEN :now AND :expiryDate AND v.status = 'ACTIVE' AND v.deletedAt IS NULL")
    List<VirtualCard> findCardsExpiringSoon(
            @Param("now") LocalDateTime now,
            @Param("expiryDate") LocalDateTime expiryDate);

    // Count active cards for a user
    @Query("SELECT COUNT(v) FROM VirtualCard v WHERE v.userId = :userId AND v.status = 'ACTIVE' AND v.deletedAt IS NULL")
    long countActiveCardsByUserId(@Param("userId") Long userId);

    // Find cards by provider ID
    Optional<VirtualCard> findByProviderCardId(String providerCardId);

    // Find cards by last four digits
    List<VirtualCard> findByLastFour(String lastFour);

    // Check if card number exists
    boolean existsByCardNumber(String cardNumber);

    // Delete cards soft deleted before a certain date
    @Query("DELETE FROM VirtualCard v WHERE v.deletedAt < :deletedBefore")
    void deleteCardsPermanentlyDeletedBefore(@Param("deletedBefore") LocalDateTime deletedBefore);
}