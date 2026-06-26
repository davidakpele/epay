package pesco.deposit_service.clients;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
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

    private static final Logger log = LoggerFactory.getLogger(WalletServiceClient.class);

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
        log.debug("Fetching wallet | userId={}", userId);
        try {
            WalletDTO response = this.walletServiceWebClient.get()
                .uri("/wallet/userId/{userId}", userId)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(WalletDTO.class)
                .block();
            log.debug("Wallet fetched successfully | userId={}", userId);
            return response;
        } catch (Exception e) {
            log.error("Failed to fetch wallet | userId={} | error={}", userId, e.getMessage(), e);
            return null;
        }
    }

    public BigDecimal FetchUserBalance(Long userId, String currencyCode, String token) {
        String url = String.format("/api/v1/wallet/balance/userId/%d/currency/%s", userId, currencyCode);
        log.debug("Fetching balance | userId={} | currencyCode={}", userId, currencyCode);

        Map<String, Object> response = walletServiceWebClient.get()
                .uri(url)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();

        if (response != null && response.containsKey("formatted_balance")) {
            try {
                String formattedBalance = response.get("formatted_balance").toString();
                BigDecimal balance = parseFormattedString(formattedBalance);
                log.debug("Balance fetched | userId={} | currencyCode={} | balance={}", userId, currencyCode, balance);
                return balance;
            } catch (ParseException e) {
                log.error("Failed to parse balance | userId={} | currencyCode={} | error={}",
                        userId, currencyCode, e.getMessage(), e);
                throw new RuntimeException("Failed to parse balance: " + e.getMessage(), e);
            }
        }

        log.warn("No formatted_balance in response | userId={} | currencyCode={}", userId, currencyCode);
        throw new RuntimeException("Failed to fetch balance for userId: " + userId + " and currencyCode: " + currencyCode);
    }

    public WalletDTO creditUserWallet(BigDecimal amount, CurrencyType currencyType, Long userId, String token) {
        log.info("Crediting wallet via REST | userId={} | amount={} | currency={}", userId, amount, currencyType);
        CreditWalletRequest creditWalletRequest = new CreditWalletRequest(amount, currencyType, userId);

        try {
            WalletDTO result = this.walletServiceWebClient.put()
                    .uri("/api/v1/wallet/update")
                    .header("Authorization", "Bearer " + token)
                    .bodyValue(creditWalletRequest)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(errorMessage -> {
                                        log.warn("Credit wallet REST call failed | userId={} | status={} | body={}",
                                                userId, clientResponse.statusCode(), errorMessage);
                                        if (clientResponse.statusCode().is4xxClientError()) {
                                            String details = extractDetailsFromError(errorMessage);
                                            return Mono.error(new UserClientNotFoundException(
                                                    "Client error while crediting wallet", details));
                                        }
                                        return Mono.error(new RuntimeException("Server error while crediting wallet"));
                                    }))
                    .bodyToMono(WalletDTO.class)
                    .block();

            log.info("Credit wallet REST call succeeded | userId={}", userId);
            return result;

        } catch (Exception e) {
            log.error("Credit wallet REST call threw exception | userId={} | error={}", userId, e.getMessage(), e);
            throw e;
        }
    }

    private String extractDetailsFromError(String errorMessage) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(errorMessage);
            return rootNode.path("message").asText();
        } catch (JsonProcessingException e) {
            log.debug("Failed to extract details from error body | raw={}", errorMessage);
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
            log.warn("creditUserWallet rejected: null parameter(s) | userId={} | currencySymbol={} | amount={} | currencyType={}",
                    userId, currencySymbol, amount, currencyType);
            CompletableFuture<Map<String, Object>> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(new IllegalArgumentException("Parameters cannot be null"));
            return failedFuture;
        }

        String correlationId = UUID.randomUUID().toString();
        MDC.put("userId", String.valueOf(userId));
        MDC.put("correlationId", correlationId);

        String url = walletSocketUrl + "/ws/wallet?userId=" + userId
                + "&token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);

        Map<String, Object> request = Map.of(
            "type", "credit_wallet",
            "currencyType", currencyType,
            "amount", amount,
            "currencySymbol", currencySymbol,
            "userId", String.valueOf(userId),
            "token", token
        );
        log.info("Initiating credit_wallet over WebSocket | userId={} | amount={} | currency={} | correlationId={}",
                userId, amount, currencyType, correlationId);

        CompletableFuture<Map<String, Object>> responseFuture = new CompletableFuture<>();
        long startTime = System.currentTimeMillis();

        try {
            String json = objectMapper.writeValueAsString(request);
            StandardWebSocketClient client = new StandardWebSocketClient();

            client.getUserProperties().put("org.apache.tomcat.websocket.IO_TIMEOUT_MS", "14000");

            client.doHandshake(new TextWebSocketHandler() {
                private final AtomicBoolean responseReceived = new AtomicBoolean(false);

                @Override
                public void afterConnectionEstablished(WebSocketSession session) throws Exception {
                    log.debug("WS handshake established | correlationId={} | sessionId={} | userId={}",
                            correlationId, session.getId(), userId);
                    session.sendMessage(new TextMessage(json));
                    log.debug("credit_wallet message sent | correlationId={} | sessionId={}", correlationId, session.getId());
                }

                @Override
                protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
                    Map<String, Object> res = objectMapper.readValue(message.getPayload(), new TypeReference<>() {});
                    String type = (String) res.get("type");

                    log.debug("WS message received | correlationId={} | sessionId={} | type={}",
                            correlationId, session.getId(), type);

                    if ("wallet_update_response".equalsIgnoreCase(type)
                            || "credit_wallet_response".equalsIgnoreCase(type)
                            || "CREDIT_SUCCESS".equalsIgnoreCase(type)) {

                        if (responseReceived.compareAndSet(false, true)) {
                            long duration = System.currentTimeMillis() - startTime;
                            log.info("credit_wallet response received | correlationId={} | userId={} | success={} | durationMs={}",
                                    correlationId, userId, res.get("success"), duration);

                            responseFuture.complete(res);
                            new Thread(() -> {
                                try {
                                    Thread.sleep(500);
                                    if (session.isOpen()) {
                                        session.close();
                                        log.debug("WS session closed after response | correlationId={} | sessionId={}",
                                                correlationId, session.getId());
                                    }
                                } catch (IOException | InterruptedException e) {
                                    log.debug("Error while closing WS session post-response | correlationId={} | error={}",
                                            correlationId, e.getMessage());
                                }
                            }).start();
                        }
                    } else if ("error".equalsIgnoreCase(type) || "Unauthorized".equalsIgnoreCase(String.valueOf(res.get("message")))) {
                        log.warn("WS rejected/error response received | correlationId={} | userId={} | response={}",
                                correlationId, userId, res);
                    }
                }

                @Override
                public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus closeStatus) {
                    if (!responseReceived.get()) {
                        log.warn("WS session closed before credit_wallet response arrived | correlationId={} | userId={} | closeStatus={}",
                                correlationId, userId, closeStatus);
                    }
                }

                @Override
                public void handleTransportError(WebSocketSession session, Throwable exception) {
                    log.error("WS transport error | correlationId={} | userId={} | error={}",
                            correlationId, userId, exception.getMessage(), exception);
                    responseFuture.completeExceptionally(exception);
                }

            }, url);

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize credit_wallet request | correlationId={} | userId={} | error={}",
                    correlationId, userId, e.getMessage(), e);
            responseFuture.completeExceptionally(e);
        } finally {
            MDC.remove("userId");
            MDC.remove("correlationId");
        }

        return responseFuture.orTimeout(15, TimeUnit.SECONDS)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("credit_wallet ultimately failed | correlationId={} | userId={} | error={}",
                                correlationId, userId, ex.getMessage());
                    }
                });
    }

    public void onWebSocketMessage(String messageJson) {
        try {
            Map<String, Object> message = new ObjectMapper().readValue(messageJson, new TypeReference<>() {});
            if ("balance_response".equals(message.get("type"))) {
                String correlationId = (String) message.get("correlationId");
                CompletableFuture<BigDecimal> future = pendingRequests.remove(correlationId);
                if (future != null) {
                    String formatted = message.get("balance").toString();
                    BigDecimal parsedBalance = parseFormattedString(formatted);
                    future.complete(parsedBalance);
                    log.debug("balance_response resolved | correlationId={} | balance={}", correlationId, parsedBalance);
                } else {
                    log.warn("balance_response received with no matching pending request | correlationId={}", correlationId);
                }
            }
        } catch (JsonProcessingException | ParseException e) {
            log.error("Failed to process incoming WS message | error={}", e.getMessage(), e);
        }
    }

    public void sendWebSocketMessage(Map<String, Object> payload) {
        try {
            String jsonMessage = new ObjectMapper().writeValueAsString(payload);
            webSocketSession.sendMessage(new TextMessage(jsonMessage));
            log.debug("WS message sent | payloadType={}", payload.get("type"));
        } catch (IOException e) {
            log.error("Failed to send WS message | error={}", e.getMessage(), e);
            throw new RuntimeException("Failed to send WebSocket message", e);
        }
    }
}