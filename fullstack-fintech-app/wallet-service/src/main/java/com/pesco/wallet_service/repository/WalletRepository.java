package com.pesco.wallet_service.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import com.pesco.wallet_service.models.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    @Query("SELECT w FROM Wallet w WHERE w.userId = :userId")
    Optional<Wallet> findByUserId(@Param("userId") Long userId);

    @Query("SELECT w FROM Wallet w WHERE w.userId = :userId")
    Wallet findWalletByUserId(@Param("userId") Long userId);

    @Query(value = "SELECT w.* FROM wallet w JOIN wallet_balances wb ON w.id = wb.wallet_id WHERE w.user_id = :userId AND wb.currency_code = :currencyCode", nativeQuery = true)
    Optional<Wallet> findWalletByUserIdAndCurrencyCode(@Param("userId") Long userId,
            @Param("currencyCode") String currencyCode);

    @Modifying
    @Query("DELETE FROM Wallet w WHERE w.userId IN :userIds")
    void deleteUserByIds(@Param("userIds") List<Long> userIds);

    @Query("SELECT w FROM Wallet w WHERE w.userId = :userId")
    List<Wallet> findByUserIdList(@Param("userId") Long userId);

    @Query(value = "SELECT * FROM wallet ORDER BY id ASC LIMIT :size OFFSET :page * :size", nativeQuery = true)
    List<Wallet> findAllWalletsPaginated(
            @Param("page") int page,
            @Param("size") int size
    );

    @Query(value = "SELECT * FROM wallet WHERE id > :lastId ORDER BY id ASC LIMIT :size OFFSET :page * :size", nativeQuery = true)
    List<Wallet> findWalletsAfterId(
            @Param("lastId") Long lastId,
            @Param("page") int page,
            @Param("size") int size
    );

    @Query("SELECT w.id FROM Wallet w WHERE w.updatedOn >= :date ORDER BY w.updatedOn DESC")
    List<Long> findRecentWalletIds(@Param("date") LocalDateTime date);

    @Query("SELECT COALESCE(MIN(w.id), 0) FROM Wallet w")
    Optional<Long> findMinWalletId();
}