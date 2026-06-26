package com.example.admin_api_service.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import com.example.admin_api_service.Interfaces.IDashboardAnalyticsService;
import com.example.admin_api_service.clients.HistoryServiceClient;
import com.example.admin_api_service.clients.UserServiceClient;
import com.example.admin_api_service.dto.EnrichedTransactionDTO;
import com.example.admin_api_service.enums.AnalyticsPeriod;
import com.example.admin_api_service.responses.AnalyticsDataPoint;
import com.example.admin_api_service.responses.DashboardAnalyticsResponse;
import com.example.admin_api_service.responses.TransactionHistory;
import com.example.admin_api_service.responses.UserAccount;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardAnalyticsService implements IDashboardAnalyticsService{

    private final HistoryServiceClient historyServiceClient;
    private final UserServiceClient userServiceClient;

    @Override
    public DashboardAnalyticsResponse getAnalytics(String token, AnalyticsPeriod period) {
        List<AnalyticsDataPoint> transactions = historyServiceClient.getTransactionAnalytics(token, period.name());
        return DashboardAnalyticsResponse.of(period, transactions);
    }

    public List<EnrichedTransactionDTO> getEnrichedHistory(String token) {
        List<TransactionHistory> histories = historyServiceClient.getRecentHistory(token, 50);

        if (histories.isEmpty()) return Collections.emptyList();

        Set<Long> userIds = histories.stream()
            .flatMap(h -> Stream.of(h.getUserId(), h.getCounterpartyUserId()))
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        Map<Long, UserAccount> userMap = userServiceClient
            .getUsersByIds(new ArrayList<>(userIds), token)
            .stream()
            .collect(Collectors.toMap(
                UserAccount::getId,
                Function.identity(),
                (existing, replacement) -> existing
            ));

        return histories.stream()
            .map(h -> {
                EnrichedTransactionDTO enriched = new EnrichedTransactionDTO();
                enriched.setHistory(h);
                enriched.setInitiator(userMap.getOrDefault(h.getUserId(), null));
                enriched.setCounterparty(userMap.getOrDefault(h.getCounterpartyUserId(), null));
                return enriched;
            })
            .collect(Collectors.toList());
    }


}
