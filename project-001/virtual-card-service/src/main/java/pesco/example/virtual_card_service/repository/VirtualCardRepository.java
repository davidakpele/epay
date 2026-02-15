package pesco.example.virtual_card_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pesco.example.virtual_card_service.enums.CardStatus;
import pesco.example.virtual_card_service.models.VirtualCard;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VirtualCardRepository extends JpaRepository<VirtualCard, String> {

    // Find by cardId (UUID)
    @Query("SELECT v FROM VirtualCard v WHERE v.cardId = :cardId")
    Optional<VirtualCard> findByCardId(@Param("cardId") String cardId);
    
    @Query("SELECT v FROM VirtualCard v WHERE v.cardId = :cardId AND v.deletedAt IS NULL")
    Optional<VirtualCard> findByCardIdAndDeletedAtIsNull(@Param("cardId") String cardId);

    // Find by card number
    @Query("SELECT v FROM VirtualCard v WHERE v.cardNumber = :cardNumber")
    Optional<VirtualCard> findByCardNumber(@Param("cardNumber") String cardNumber);

    // Find all cards for a user
    @Query("SELECT v FROM VirtualCard v WHERE v.userId = :userId")
    List<VirtualCard> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT v FROM VirtualCard v WHERE v.userId = :userId AND v.deletedAt IS NULL")
    List<VirtualCard> findByUserIdAndDeletedAtIsNull(@Param("userId") Long userId);
    
    @Query("SELECT v FROM VirtualCard v WHERE v.userId = :userId AND v.deletedAt IS NULL")
    Page<VirtualCard> findByUserIdAndDeletedAtIsNull(@Param("userId") Long userId, Pageable pageable);

    // Find active cards for a user
    @Query("SELECT v FROM VirtualCard v WHERE v.userId = :userId AND v.status = :status AND v.deletedAt IS NULL")
    List<VirtualCard> findByUserIdAndStatusAndDeletedAtIsNull(
            @Param("userId") Long userId, 
            @Param("status") CardStatus status);

    // Find cards by status
    @Query("SELECT v FROM VirtualCard v WHERE v.status = :status")
    List<VirtualCard> findByStatus(@Param("status") CardStatus status);
    
    @Query("SELECT v FROM VirtualCard v WHERE v.status = :status AND v.deletedAt IS NULL")
    List<VirtualCard> findByStatusAndDeletedAtIsNull(@Param("status") CardStatus status);

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
    @Query("SELECT v FROM VirtualCard v WHERE v.providerCardId = :providerCardId")
    Optional<VirtualCard> findByProviderCardId(@Param("providerCardId") String providerCardId);

    // Find cards by last four digits
    @Query("SELECT v FROM VirtualCard v WHERE v.lastFour = :lastFour")
    List<VirtualCard> findByLastFour(@Param("lastFour") String lastFour);

    // Check if card number exists
    @Query("SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END FROM VirtualCard v WHERE v.cardNumber = :cardNumber")
    boolean existsByCardNumber(@Param("cardNumber") String cardNumber);

    // Delete cards soft deleted before a certain date (permanent deletion)
    @Modifying
    @Transactional
    @Query("DELETE FROM VirtualCard v WHERE v.deletedAt < :deletedBefore AND v.deletedAt IS NOT NULL")
    void deleteCardsPermanentlyDeletedBefore(@Param("deletedBefore") LocalDateTime deletedBefore);
    
    // Additional useful queries
    
    // Find cards by BIN (Bank Identification Number)
    @Query("SELECT v FROM VirtualCard v WHERE v.bin = :bin AND v.deletedAt IS NULL")
    List<VirtualCard> findByBin(@Param("bin") String bin);
    
    // Find frozen cards
    @Query("SELECT v FROM VirtualCard v WHERE v.status = 'FROZEN' AND v.deletedAt IS NULL")
    List<VirtualCard> findFrozenCards();
    
    // Find cards by merchant
    @Query("SELECT v FROM VirtualCard v WHERE v.merchantId = :merchantId AND v.deletedAt IS NULL")
    List<VirtualCard> findByMerchantId(@Param("merchantId") String merchantId);
    
    // Find cards with spending limit exceeded
    @Query("SELECT v FROM VirtualCard v WHERE v.currentPeriodSpent >= v.spendingLimit AND v.status = 'ACTIVE' AND v.deletedAt IS NULL")
    List<VirtualCard> findCardsWithLimitExceeded();
    
    // Find cards by user and card type
    @Query("SELECT v FROM VirtualCard v WHERE v.userId = :userId AND v.cardType = :cardType AND v.deletedAt IS NULL")
    List<VirtualCard> findByUserIdAndCardType(
            @Param("userId") Long userId, 
            @Param("cardType") pesco.example.virtual_card_service.enums.CardType cardType);
    
    // Count total cards by user (including deleted)
    @Query("SELECT COUNT(v) FROM VirtualCard v WHERE v.userId = :userId")
    long countTotalCardsByUserId(@Param("userId") Long userId);
    
    // Find recently used cards
    @Query("SELECT v FROM VirtualCard v WHERE v.lastUsedAt IS NOT NULL AND v.deletedAt IS NULL ORDER BY v.lastUsedAt DESC")
    Page<VirtualCard> findRecentlyUsedCards(Pageable pageable);
    
    // Find cards created within date range
    @Query("SELECT v FROM VirtualCard v WHERE v.createdAt BETWEEN :startDate AND :endDate AND v.deletedAt IS NULL")
    List<VirtualCard> findCardsCreatedBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    // Soft delete a card
    @Modifying
    @Transactional
    @Query("UPDATE VirtualCard v SET v.deletedAt = :deletedAt WHERE v.cardId = :cardId")
    void softDeleteCard(@Param("cardId") String cardId, @Param("deletedAt") LocalDateTime deletedAt);
    
    // Update card status
    @Modifying
    @Transactional
    @Query("UPDATE VirtualCard v SET v.status = :status, v.updatedAt = :updatedAt WHERE v.cardId = :cardId")
    void updateCardStatus(
            @Param("cardId") String cardId, 
            @Param("status") CardStatus status, 
            @Param("updatedAt") LocalDateTime updatedAt);
    
    // Freeze card
    @Modifying
    @Transactional
    @Query("UPDATE VirtualCard v SET v.status = 'FROZEN', v.frozenAt = :frozenAt, v.updatedAt = :frozenAt WHERE v.cardId = :cardId")
    void freezeCard(@Param("cardId") String cardId, @Param("frozenAt") LocalDateTime frozenAt);
    
    // Unfreeze card
    @Modifying
    @Transactional
    @Query("UPDATE VirtualCard v SET v.status = 'ACTIVE', v.frozenAt = NULL, v.updatedAt = :updatedAt WHERE v.cardId = :cardId")
    void unfreezeCard(@Param("cardId") String cardId, @Param("updatedAt") LocalDateTime updatedAt);
}