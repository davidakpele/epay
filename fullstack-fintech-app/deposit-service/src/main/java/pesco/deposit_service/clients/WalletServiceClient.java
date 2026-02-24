package pesco.deposit_service.clients;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import pesco.deposit_service.dto.WalletDTO;
import pesco.deposit_service.enums.CurrencyType;
import pesco.deposit_service.exceptions.UserClientNotFoundException;
import pesco.deposit_service.payloads.CreditWalletRequest;
import reactor.core.publisher.Mono;

@Service
public class WalletServiceClient {

    private final WebClient walletServiceWebClient;
    private final Map<String, CompletableFuture<BigDecimal>> pendingRequests = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private WebSocketSession webSocketSession;

    @Value("${wallet-websocket.url}") 
    private String walletSocketUrl;

    @Value("${wallet-service.base-url}") 
    private String walletServiceBaseUrl;

    public WalletServiceClient(WebClient walletServiceWebClient) {
        this.walletServiceWebClient = walletServiceWebClient;
    }

    public WalletDTO findByUserId(Long userId, String token) {
    
        try {
            WalletDTO response = this.walletServiceWebClient.get()
                .uri("/wallet/userId/{userId}", userId)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(WalletDTO.class)
                .block(); 
            return response;
        } catch (Exception e) {
            System.out.println("Error message: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public BigDecimal FetchUserBalance(Long userId, String currencyCode, String token) {
        String url = String.format("/api/v1/wallet/balance/userId/%d/currency/%s", userId, currencyCode);
        Map<String, Object> response = walletServiceWebClient.get()
                .uri(url)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();

        // Extract and return the balance
        if (response != null && response.containsKey("formatted_balance")) {
            try {
                String formattedBalance = response.get("formatted_balance").toString();
                return parseFormattedString(formattedBalance);
            } catch (ParseException e) {
                throw new RuntimeException("Failed to parse balance: " + e.getMessage(), e);
            }
        }

        throw new RuntimeException("Failed to fetch balance for userId: " + userId + " and currencyCode: " + currencyCode);
    }

    public WalletDTO creditUserWallet(BigDecimal amount, CurrencyType currencyType, Long userId, String token) {
        CreditWalletRequest creditWalletRequest = new CreditWalletRequest(amount, currencyType, userId);

        return this.walletServiceWebClient.put()
                .uri("/api/v1/wallet/update")
                .header("Authorization", "Bearer " + token)
                .bodyValue(creditWalletRequest) 
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(errorMessage -> {
                                    if (clientResponse.statusCode().is4xxClientError()) {
                                        String details = extractDetailsFromError(errorMessage);
                                        return Mono.error(new UserClientNotFoundException(
                                                "Client error while crediting wallet", details));
                                    }
                                    return Mono.error(new RuntimeException("Server error while crediting wallet"));
                                }))
                .bodyToMono(WalletDTO.class)
                .block();
    }
    
    private String extractDetailsFromError(String errorMessage) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(errorMessage);
            return rootNode.path("message").asText();
        } catch (JsonProcessingException e) {
            return "No details available";
        }
    }

    public static BigDecimal parseFormattedString(String formattedValue) throws ParseException {
        String pattern = "#,##0.00";
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(',');
        symbols.setDecimalSeparator('.');
        DecimalFormat decimalFormat = new DecimalFormat(pattern, symbols);
        
        Number number = decimalFormat.parse(formattedValue);
        return new BigDecimal(number.toString());
    }

    @SuppressWarnings("removal")
    public CompletableFuture<Map<String, Object>> creditUserWallet(Long userId, String currencySymbol, String amount, String currencyType, String token) {
        if (userId == null || currencySymbol == null || amount == null || currencyType == null) {
            CompletableFuture<Map<String, Object>> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(new IllegalArgumentException("Parameters cannot be null"));
            return failedFuture;
        }
        String url = walletSocketUrl + "/ws/wallet?userId=" + userId;

        Map<String, Object> request = Map.of(
            "type", "credit_wallet",
            "currencyType", currencyType,
            "amount", amount,
            "currencySymbol", currencySymbol,
            "userId", String.valueOf(userId),
            "token", token
        );

        CompletableFuture<Map<String, Object>> responseFuture = new CompletableFuture<>();

        try {
            String json = objectMapper.writeValueAsString(request);
            StandardWebSocketClient client = new StandardWebSocketClient();

            client.doHandshake(new TextWebSocketHandler() {
                private final AtomicBoolean responseReceived = new AtomicBoolean(false);

                @Override
                public void afterConnectionEstablished(WebSocketSession session) throws Exception {
                    session.sendMessage(new TextMessage(json));
                }

                @Override
                protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
                    Map<String, Object> res = objectMapper.readValue(message.getPayload(), new TypeReference<>() {});
                    String type = (String) res.get("type");

                    if ("wallet_update_response".equalsIgnoreCase(type)
                            || "credit_wallet_response".equalsIgnoreCase(type)
                            || "CREDIT_SUCCESS".equalsIgnoreCase(type)) {
                        
                        if (responseReceived.compareAndSet(false, true)) {
                            responseFuture.complete(res);
                            new Thread(() -> {
                                try {
                                    Thread.sleep(500);  
                                    if (session.isOpen()) {
                                        session.close();
                                    }
                                } catch (Exception e) {
                                    // Ignore
                                }
                            }).start();
                        }
                    }
                }

                @Override
                public void handleTransportError(WebSocketSession session, Throwable exception) {
                    responseFuture.completeExceptionally(exception);
                }

            }, url);

        } catch (JsonProcessingException e) {
            responseFuture.completeExceptionally(e);
        }

        return responseFuture.orTimeout(15, TimeUnit.SECONDS);
    }

    public void onWebSocketMessage(String messageJson) {
        try {
            Map<String, Object> message = new ObjectMapper().readValue(messageJson, new TypeReference<>() {
            });
            if ("balance_response".equals(message.get("type"))) {
                String correlationId = (String) message.get("correlationId");
                CompletableFuture<BigDecimal> future = pendingRequests.remove(correlationId);
                if (future != null) {
                    String formatted = message.get("balance").toString();
                    BigDecimal parsedBalance = parseFormattedString(formatted);
                    future.complete(parsedBalance);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendWebSocketMessage(Map<String, Object> payload) {
        try {
            String jsonMessage = new ObjectMapper().writeValueAsString(payload);
            webSocketSession.sendMessage(new TextMessage(jsonMessage));
        } catch (IOException e) {
            throw new RuntimeException("Failed to send WebSocket message", e);
        }
    }  

}
