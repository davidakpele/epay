package com.epay.wallet.repository;

import com.epay.wallet.domain.entity.Wallet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    @Query("SELECT w FROM Wallet w WHERE w.userId = :userId")
    Optional<Wallet> findByUserId(@Param("userId") Long userId);

    @Query("SELECT w FROM Wallet w JOIN FETCH w.balances WHERE w.userId = :userId")
    Optional<Wallet> findByUserIdWithBalances(@Param("userId") Long userId);

    @Query(value = "SELECT w.* FROM wallets w " +
                   "JOIN wallet_balances wb ON w.id = wb.wallet_id " +
                   "WHERE w.user_id = :userId AND UPPER(wb.currency_code) = UPPER(:currencyCode) " +
                   "LIMIT 1",
           nativeQuery = true)
    Optional<Wallet> findByUserIdAndCurrencyCode(@Param("userId") Long userId,
                                                  @Param("currencyCode") String currencyCode);

    @Query(value = "SELECT w.* FROM wallets w " +
                   "JOIN users u ON w.user_id = u.id " +
                   "WHERE u.username = :username LIMIT 1",
           nativeQuery = true)
    Optional<Wallet> findByRecipientUsername(@Param("username") String username);
    @Query("SELECT COUNT(w) > 0 FROM Wallet w WHERE w.userId = :userId")
    boolean existsByUserId(@Param("userId") Long userId);

    @Query(value = "SELECT COUNT(*) > 0 FROM wallet_balances WHERE UPPER(currency_code) = UPPER(:code)",
           nativeQuery = true)
    boolean existsByCurrencyCode(@Param("code") String code);


    @Query("SELECT w FROM Wallet w ORDER BY w.createdAt DESC")
    Page<Wallet> findAllPaginated(Pageable pageable);

    @Query("SELECT w FROM Wallet w WHERE w.active = :active ORDER BY w.createdAt DESC")
    Page<Wallet> findByActive(@Param("active") boolean active, Pageable pageable);

    @Query("SELECT w FROM Wallet w WHERE w.updatedAt >= :since ORDER BY w.updatedAt DESC")
    List<Wallet> findRecentlyUpdated(@Param("since") LocalDateTime since);

    @Query("SELECT COUNT(w) FROM Wallet w WHERE w.active = true")
    long countActive();

    @Query("SELECT COUNT(w) FROM Wallet w WHERE w.active = false")
    long countFrozen();

    @Modifying
    @Query("UPDATE Wallet w SET w.active = false WHERE w.userId IN :userIds")
    void freezeByUserIds(@Param("userIds") List<Long> userIds);

    @Modifying
    @Query("DELETE FROM Wallet w WHERE w.userId IN :userIds")
    void deleteByUserIds(@Param("userIds") List<Long> userIds);
}
