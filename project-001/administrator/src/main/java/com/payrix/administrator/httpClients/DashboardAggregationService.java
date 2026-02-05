package com.payrix.administrator.httpClients;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.payrix.administrator.dtos.DashboardData;
import com.payrix.administrator.dtos.TransactionStats;
import com.payrix.administrator.responses.HistoryCacheResponse;
import com.payrix.administrator.responses.RevenueResponse;

import reactor.core.publisher.Mono;

@Service
public class DashboardAggregationService {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DashboardAggregationService.class);
    
    private final WebClient authServiceWebClient;
    private final WebClient historyServiceWebClient;
    private final WebClient bankListServiceWebClient;
    private final WebClient revenueServiceWebClient;
    private final WebClient escrowServiceWebClient;

    public DashboardAggregationService(
            @Qualifier("authServiceWebClient") WebClient authServiceWebClient,
            @Qualifier("historyServiceWebClient") WebClient historyServiceWebClient,
            @Qualifier("bankListServiceWebClient") WebClient bankListServiceWebClient,
            @Qualifier("revenueServiceWebClient") WebClient revenueServiceWebClient,
            @Qualifier("escrowServiceWebClient") WebClient escrowServiceWebClient) {
        
        this.authServiceWebClient = authServiceWebClient;
        this.historyServiceWebClient = historyServiceWebClient;
        this.bankListServiceWebClient = bankListServiceWebClient;
        this.revenueServiceWebClient = revenueServiceWebClient;
        this.escrowServiceWebClient = escrowServiceWebClient;
    }
    
    public Mono<DashboardData> fetchDashboardData(String token) {
        logger.info("Starting dashboard data aggregation");
        
        Mono<Long> totalUsers = fetchTotalUsers(token);
        Mono<BigDecimal> totalRevenue = fetchTotalRevenue(token);
        Mono<TransactionStats> transactionStats = fetchTransactionStats(token);
        Mono<TransactionStats> blacklistStats = fetchTotalBlackListedAccount(token);
        Mono<TransactionStats> pendingTransactionStats = fetchTotalPendingTransactions(token);
        
        return Mono.zip(totalUsers, totalRevenue, transactionStats, blacklistStats, pendingTransactionStats)
            .map(tuple -> {
                DashboardData data = new DashboardData(
                    tuple.getT1(),
                    tuple.getT2(),
                    tuple.getT3(),
                    tuple.getT4(),
                    tuple.getT5()
                );
                
                logger.info("Dashboard aggregation completed: Users={}, Revenue={}, Transactions={}/{}, Blacklisted={}, Pending={}",
                    data.getTotalUsers(),
                    data.getTotalRevenue(),
                    data.getTransactionStats().getPending(),
                    data.getTransactionStats().getTotal(),
                    data.getBlacklistStats().getTotal(),
                    data.getPendingTransactionStats().getPending()
                );
                
                return data;
            })
            .timeout(Duration.ofSeconds(5))
            .onErrorResume(e -> {
                logger.error("Dashboard aggregation failed: {}", e.getMessage());
                return Mono.just(DashboardData.getDefault());
            });
    }

    private Mono<Long> fetchTotalUsers(String token) {
        logger.debug("Fetching total users from auth service");
        return authServiceWebClient.get()
            .uri("/user/count") 
            .header("Authorization", "Bearer " + token)
            .retrieve()
            .bodyToMono(Long.class)
            .doOnSuccess(count -> logger.debug("Total users response: {}", count))
            .doOnError(e -> logger.error("Failed to fetch total users: {}", e.getMessage()))
            .onErrorReturn(0L);
    }
    
    private Mono<BigDecimal> fetchTotalRevenue(String token) {
        logger.debug("Fetching total revenue from revenue service");
        return revenueServiceWebClient.get()
            .uri("/api/revenue")
            .header("Authorization", "Bearer " + token)
            .retrieve()
            .bodyToMono(RevenueResponse.class)
            .map(RevenueResponse::getTotalRevenue)
            .doOnSuccess(revenue -> logger.debug("Total revenue response: {}", revenue))
            .doOnError(e -> logger.error("Failed to fetch total revenue: {}", e.getMessage()))
            .onErrorReturn(BigDecimal.ZERO);
    }

    private Mono<TransactionStats> fetchTransactionStats(String token) {
        return historyServiceWebClient.get()
            .uri("/history/cache/all")
            .header("Authorization", "Bearer " + token)
            .retrieve()
            .bodyToMono(HistoryCacheResponse.class)
            .map(response -> {
                long total = response.getCount();
                long pending = response.getData().stream()
                    .filter(d -> "PENDING".equals(d.getStatus()))
                    .count();
                return new TransactionStats(total, pending);
            })
            .onErrorReturn(new TransactionStats(0L, 0L));
    }

    private Mono<TransactionStats> fetchTotalBlackListedAccount(String token) {
        logger.debug("Fetching blacklist stats from banklist service");
        return bankListServiceWebClient.get()
            .uri("/blacklist/count")  
            .header("Authorization", "Bearer " + token)
            .retrieve()
            .bodyToMono(Map.class)  
            .map(response -> {
                Long count = ((Number) response.get("count")).longValue();
                return new TransactionStats(count, 0L); 
            })
            .doOnSuccess(stats -> logger.debug("Blacklist stats response: total={}", stats.getTotal()))
            .doOnError(e -> logger.error("Failed to fetch blacklist stats: {}", e.getMessage()))
            .onErrorReturn(new TransactionStats(0L, 0L));
    }
    
    private Mono<TransactionStats> fetchTotalPendingTransactions(String token) {
        logger.debug("Fetching pending transactions from escrow service");
        return escrowServiceWebClient.get()
            .uri("/api/transactions/stats")
            .header("Authorization", "Bearer " + token)
            .retrieve()
            .bodyToMono(TransactionStats.class)
            .doOnSuccess(stats -> logger.debug("Escrow pending response: total={}, pending={}", 
                stats.getTotal(), stats.getPending()))
            .doOnError(e -> logger.error("Failed to fetch escrow pending: {}", e.getMessage()))
            .onErrorReturn(new TransactionStats(0L, 0L));
    }

}
