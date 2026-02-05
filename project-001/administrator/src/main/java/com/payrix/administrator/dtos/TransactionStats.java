package com.payrix.administrator.dtos;

import java.math.BigDecimal;

public class TransactionStats {
    
    private Long totalTransactions;
    private Long pendingTransactions;
    private Long completedTransactions;
    private Long failedTransactions;
    private BigDecimal totalTransactionValue;
    private BigDecimal pendingTransactionValue;
    
    // Constructors
    public TransactionStats() {
    }
    
    public TransactionStats(Long totalTransactions, Long pendingTransactions) {
        this.totalTransactions = totalTransactions;
        this.pendingTransactions = pendingTransactions;
        this.completedTransactions = 0L;
        this.failedTransactions = 0L;
        this.totalTransactionValue = BigDecimal.ZERO;
        this.pendingTransactionValue = BigDecimal.ZERO;
    }
    
    public TransactionStats(Long totalTransactions, Long pendingTransactions, 
                           Long completedTransactions, Long failedTransactions,
                           BigDecimal totalTransactionValue, BigDecimal pendingTransactionValue) {
        this.totalTransactions = totalTransactions;
        this.pendingTransactions = pendingTransactions;
        this.completedTransactions = completedTransactions;
        this.failedTransactions = failedTransactions;
        this.totalTransactionValue = totalTransactionValue;
        this.pendingTransactionValue = pendingTransactionValue;
    }
    
    // Factory method for default/error state
    public static TransactionStats getDefault() {
        return new TransactionStats(0L, 0L, 0L, 0L, BigDecimal.ZERO, BigDecimal.ZERO);
    }
    
    // Getters and Setters
    public Long getTotalTransactions() {
        return totalTransactions;
    }
    
    public void setTotalTransactions(Long totalTransactions) {
        this.totalTransactions = totalTransactions;
    }
    
    public Long getPendingTransactions() {
        return pendingTransactions;
    }
    
    public void setPendingTransactions(Long pendingTransactions) {
        this.pendingTransactions = pendingTransactions;
    }
    
    public Long getCompletedTransactions() {
        return completedTransactions;
    }
    
    public void setCompletedTransactions(Long completedTransactions) {
        this.completedTransactions = completedTransactions;
    }
    
    public Long getFailedTransactions() {
        return failedTransactions;
    }
    
    public void setFailedTransactions(Long failedTransactions) {
        this.failedTransactions = failedTransactions;
    }
    
    public BigDecimal getTotalTransactionValue() {
        return totalTransactionValue;
    }
    
    public void setTotalTransactionValue(BigDecimal totalTransactionValue) {
        this.totalTransactionValue = totalTransactionValue;
    }
    
    public BigDecimal getPendingTransactionValue() {
        return pendingTransactionValue;
    }
    
    public void setPendingTransactionValue(BigDecimal pendingTransactionValue) {
        this.pendingTransactionValue = pendingTransactionValue;
    }
    
    // Utility methods
    public Long getTotal() {
        return totalTransactions != null ? totalTransactions : 0L;
    }
    
    public Long getPending() {
        return pendingTransactions != null ? pendingTransactions : 0L;
    }
    
    public double getSuccessRate() {
        if (totalTransactions == null || totalTransactions == 0) {
            return 0.0;
        }
        Long completed = completedTransactions != null ? completedTransactions : 0L;
        return (completed * 100.0) / totalTransactions;
    }
    
    @Override
    public String toString() {
        return "TransactionStats{" +
                "totalTransactions=" + totalTransactions +
                ", pendingTransactions=" + pendingTransactions +
                ", completedTransactions=" + completedTransactions +
                ", failedTransactions=" + failedTransactions +
                ", totalTransactionValue=" + totalTransactionValue +
                ", pendingTransactionValue=" + pendingTransactionValue +
                '}';
    }
}