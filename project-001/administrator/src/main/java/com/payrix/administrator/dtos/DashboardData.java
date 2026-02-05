package com.payrix.administrator.dtos;


import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DashboardData {
    
    private Long totalUsers;
    private BigDecimal totalRevenue;
    private TransactionStats transactionStats;
    private TransactionStats blacklistStats;
    private TransactionStats pendingTransactionStats;
    private LocalDateTime lastUpdated;
    
    // Default Constructor
    public DashboardData() {
        this.lastUpdated = LocalDateTime.now();
    }
    
    // Constructor matching your Mono.zip call
    public DashboardData(Long totalUsers, BigDecimal totalRevenue, 
                        TransactionStats transactionStats,
                        TransactionStats blacklistStats,
                        TransactionStats pendingTransactionStats) {
        this.totalUsers = totalUsers;
        this.totalRevenue = totalRevenue;
        this.transactionStats = transactionStats;
        this.blacklistStats = blacklistStats;
        this.pendingTransactionStats = pendingTransactionStats;
        this.lastUpdated = LocalDateTime.now();
    }
    
    // Legacy constructor for backward compatibility
    public DashboardData(Long totalUsers, BigDecimal totalRevenue, TransactionStats transactionStats) {
        this.totalUsers = totalUsers;
        this.totalRevenue = totalRevenue;
        this.transactionStats = transactionStats;
        this.blacklistStats = TransactionStats.getDefault();
        this.pendingTransactionStats = TransactionStats.getDefault();
        this.lastUpdated = LocalDateTime.now();
    }
    
    // Factory method for default/error state
    public static DashboardData getDefault() {
        return new DashboardData(
            0L, 
            BigDecimal.ZERO, 
            TransactionStats.getDefault(),
            TransactionStats.getDefault(),
            TransactionStats.getDefault()
        );
    }
    
    // Getters and Setters
    public Long getTotalUsers() {
        return totalUsers;
    }
    
    public void setTotalUsers(Long totalUsers) {
        this.totalUsers = totalUsers;
    }
    
    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }
    
    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
    
    public TransactionStats getTransactionStats() {
        return transactionStats;
    }
    
    public void setTransactionStats(TransactionStats transactionStats) {
        this.transactionStats = transactionStats;
    }
    
    public TransactionStats getBlacklistStats() {
        return blacklistStats;
    }
    
    public void setBlacklistStats(TransactionStats blacklistStats) {
        this.blacklistStats = blacklistStats;
    }
    
    public TransactionStats getPendingTransactionStats() {
        return pendingTransactionStats;
    }
    
    public void setPendingTransactionStats(TransactionStats pendingTransactionStats) {
        this.pendingTransactionStats = pendingTransactionStats;
    }
    
    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }
    
    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    
    @Override
    public String toString() {
        return "DashboardData{" +
                "totalUsers=" + totalUsers +
                ", totalRevenue=" + totalRevenue +
                ", transactionStats=" + transactionStats +
                ", blacklistStats=" + blacklistStats +
                ", pendingTransactionStats=" + pendingTransactionStats +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}