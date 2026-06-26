package pesco.example.virtual_card_service.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CardLimitInfo {
    private BigDecimal dailyLimit;
    private BigDecimal dailySpent;
    private BigDecimal dailyRemaining;
    private BigDecimal monthlyLimit;
    private BigDecimal monthlySpent;
    private BigDecimal monthlyRemaining;

    public CardLimitInfo() {
    }

    public CardLimitInfo(BigDecimal dailyLimit, BigDecimal dailySpent, BigDecimal dailyRemaining, BigDecimal monthlyLimit, BigDecimal monthlySpent, BigDecimal monthlyRemaining) {
        this.dailyLimit = dailyLimit;
        this.dailySpent = dailySpent;
        this.dailyRemaining = dailyRemaining;
        this.monthlyLimit = monthlyLimit;
        this.monthlySpent = monthlySpent;
        this.monthlyRemaining = monthlyRemaining;
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

    public BigDecimal getDailyRemaining() {
        return this.dailyRemaining;
    }

    public void setDailyRemaining(BigDecimal dailyRemaining) {
        this.dailyRemaining = dailyRemaining;
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

    public BigDecimal getMonthlyRemaining() {
        return this.monthlyRemaining;
    }

    public void setMonthlyRemaining(BigDecimal monthlyRemaining) {
        this.monthlyRemaining = monthlyRemaining;
    }

    
}
