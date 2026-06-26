package pesco.example.virtual_card_service.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.hibernate.annotations.UpdateTimestamp;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Entity
@Table(name = "card_limits")
public class CardLimit {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(name = "card_id", nullable = false, unique = true)
    private String cardId;
    
    // Transaction Limits
    @Column(name = "max_transaction_amount", precision = 19, scale = 2)
    private BigDecimal maxTransactionAmount;
    
    @Column(name = "min_transaction_amount", precision = 19, scale = 2)
    private BigDecimal minTransactionAmount;
    
    // Period Limits
    @Column(name = "daily_limit", precision = 19, scale = 2)
    private BigDecimal dailyLimit;
    
    @Column(name = "daily_spent", precision = 19, scale = 2)
    private BigDecimal dailySpent;
    
    @Column(name = "daily_reset_at")
    private LocalDateTime dailyResetAt;
    
    @Column(name = "weekly_limit", precision = 19, scale = 2)
    private BigDecimal weeklyLimit;
    
    @Column(name = "weekly_spent", precision = 19, scale = 2)
    private BigDecimal weeklySpent;
    
    @Column(name = "weekly_reset_at")
    private LocalDateTime weeklyResetAt;
    
    @Column(name = "monthly_limit", precision = 19, scale = 2)
    private BigDecimal monthlyLimit;
    
    @Column(name = "monthly_spent", precision = 19, scale = 2)
    private BigDecimal monthlySpent;
    
    @Column(name = "monthly_reset_at")
    private LocalDateTime monthlyResetAt;
    
    // Transaction Count Limits
    @Column(name = "daily_transaction_count_limit")
    private Integer dailyTransactionCountLimit;
    
    @Column(name = "daily_transaction_count")
    private Integer dailyTransactionCount;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Version
    private Long version;


    public CardLimit() {
    }

    public CardLimit(String id, String cardId, BigDecimal maxTransactionAmount, BigDecimal minTransactionAmount, BigDecimal dailyLimit, BigDecimal dailySpent, LocalDateTime dailyResetAt, BigDecimal weeklyLimit, BigDecimal weeklySpent, LocalDateTime weeklyResetAt, BigDecimal monthlyLimit, BigDecimal monthlySpent, LocalDateTime monthlyResetAt, Integer dailyTransactionCountLimit, Integer dailyTransactionCount, LocalDateTime updatedAt, Long version) {
        this.id = id;
        this.cardId = cardId;
        this.maxTransactionAmount = maxTransactionAmount;
        this.minTransactionAmount = minTransactionAmount;
        this.dailyLimit = dailyLimit;
        this.dailySpent = dailySpent;
        this.dailyResetAt = dailyResetAt;
        this.weeklyLimit = weeklyLimit;
        this.weeklySpent = weeklySpent;
        this.weeklyResetAt = weeklyResetAt;
        this.monthlyLimit = monthlyLimit;
        this.monthlySpent = monthlySpent;
        this.monthlyResetAt = monthlyResetAt;
        this.dailyTransactionCountLimit = dailyTransactionCountLimit;
        this.dailyTransactionCount = dailyTransactionCount;
        this.updatedAt = updatedAt;
        this.version = version;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCardId() {
        return this.cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public BigDecimal getMaxTransactionAmount() {
        return this.maxTransactionAmount;
    }

    public void setMaxTransactionAmount(BigDecimal maxTransactionAmount) {
        this.maxTransactionAmount = maxTransactionAmount;
    }

    public BigDecimal getMinTransactionAmount() {
        return this.minTransactionAmount;
    }

    public void setMinTransactionAmount(BigDecimal minTransactionAmount) {
        this.minTransactionAmount = minTransactionAmount;
    }

    public BigDecimal getDailyLimit() {
        return this.dailyLimit;
    }

    public void setDailyLimit(BigDecimal dailyLimit) {
        this.dailyLimit = dailyLimit;
    }

    public BigDecimal getDailySpent() {
        return this.dailySpent;
    }

    public void setDailySpent(BigDecimal dailySpent) {
        this.dailySpent = dailySpent;
    }

    public LocalDateTime getDailyResetAt() {
        return this.dailyResetAt;
    }

    public void setDailyResetAt(LocalDateTime dailyResetAt) {
        this.dailyResetAt = dailyResetAt;
    }

    public BigDecimal getWeeklyLimit() {
        return this.weeklyLimit;
    }

    public void setWeeklyLimit(BigDecimal weeklyLimit) {
        this.weeklyLimit = weeklyLimit;
    }

    public BigDecimal getWeeklySpent() {
        return this.weeklySpent;
    }

    public void setWeeklySpent(BigDecimal weeklySpent) {
        this.weeklySpent = weeklySpent;
    }

    public LocalDateTime getWeeklyResetAt() {
        return this.weeklyResetAt;
    }

    public void setWeeklyResetAt(LocalDateTime weeklyResetAt) {
        this.weeklyResetAt = weeklyResetAt;
    }

    public BigDecimal getMonthlyLimit() {
        return this.monthlyLimit;
    }

    public void setMonthlyLimit(BigDecimal monthlyLimit) {
        this.monthlyLimit = monthlyLimit;
    }

    public BigDecimal getMonthlySpent() {
        return this.monthlySpent;
    }

    public void setMonthlySpent(BigDecimal monthlySpent) {
        this.monthlySpent = monthlySpent;
    }

    public LocalDateTime getMonthlyResetAt() {
        return this.monthlyResetAt;
    }

    public void setMonthlyResetAt(LocalDateTime monthlyResetAt) {
        this.monthlyResetAt = monthlyResetAt;
    }

    public Integer getDailyTransactionCountLimit() {
        return this.dailyTransactionCountLimit;
    }

    public void setDailyTransactionCountLimit(Integer dailyTransactionCountLimit) {
        this.dailyTransactionCountLimit = dailyTransactionCountLimit;
    }

    public Integer getDailyTransactionCount() {
        return this.dailyTransactionCount;
    }

    public void setDailyTransactionCount(Integer dailyTransactionCount) {
        this.dailyTransactionCount = dailyTransactionCount;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getVersion() {
        return this.version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

}
