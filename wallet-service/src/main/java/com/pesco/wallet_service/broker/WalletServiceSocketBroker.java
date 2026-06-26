package com.pesco.wallet_service.broker;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pesco.wallet_service.bootstrap.DataSection;
import com.pesco.wallet_service.bootstrap.HistorySection;
import com.pesco.wallet_service.bootstrap.TransactionRecord;
import com.pesco.wallet_service.bootstrap.UserSessionData;
import com.pesco.wallet_service.client.HistoryClient;
import com.pesco.wallet_service.client.NotificationServiceClient;
import com.pesco.wallet_service.client.UserServiceClient;
import com.pesco.wallet_service.dtos.CurrencyBalanceDTO;
import com.pesco.wallet_service.dtos.HistoryDTO;
import com.pesco.wallet_service.dtos.UserDTO;
import com.pesco.wallet_service.dtos.WalletResponseDTO;
import com.pesco.wallet_service.encryptions.battery.SEC46;
import com.pesco.wallet_service.enums.Currency;
import com.pesco.wallet_service.handler.WalletHandler;
import com.pesco.wallet_service.models.CurrencyBalanceMapStruct;
import com.pesco.wallet_service.models.Wallet;
import com.pesco.wallet_service.payloads.SwapHistoryRequest;
import com.pesco.wallet_service.repository.WalletRepository;
import com.pesco.wallet_service.response.HistoryResponse;
import com.pesco.wallet_service.services.WalletService;
import net.devh.boot.grpc.client.inject.GrpcClient;
import pesco.wallet_service.grpc.WalletServiceGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.collection.ISet;
import com.hazelcast.map.IMap;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class WalletServiceSocketBroker extends AbstractWebSocketHandler {

    private final WalletRepository walletRepository;
    private final UserServiceClient userServiceClient;
    private final WalletService walletService;
    private final NotificationServiceClient notificationServiceClient;
    private final HistoryClient historyClient;
    private final SEC46 sec46;
    private final WalletHandler walletHandler;
    private final ObjectMapper objectMapper;
    private final HazelcastInstance hazelcastInstance;

    public WalletServiceSocketBroker(
            WalletRepository walletRepository,
            UserServiceClient userServiceClient,
            WalletService walletService,
            NotificationServiceClient notificationServiceClient,
            HistoryClient historyClient,
            SEC46 sec46,
            WalletHandler walletHandler,
            HazelcastInstance hazelcastInstance, ObjectMapper objectMapper) {
        this.walletRepository = walletRepository;
        this.userServiceClient = userServiceClient;
        this.walletService = walletService;
        this.notificationServiceClient = notificationServiceClient;
        this.historyClient = historyClient;
        this.sec46 = sec46;
        this.walletHandler = walletHandler;
        this.objectMapper = objectMapper;
        this.hazelcastInstance = hazelcastInstance;
    }

    private static final Logger log = LoggerFactory.getLogger(WalletServiceSocketBroker.class);

    @GrpcClient("wallet-service")
    private WalletServiceGrpc.WalletServiceBlockingStub walletServiceStub;

    

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        String sessionId = session.getId();
        MDC.put("sessionId", sessionId);
        try {
            URI uri = session.getUri();
            log.info("WS connection opened | sessionId={} | uri={}", sessionId, uri);

            if (uri == null) {
                log.warn("Connection rejected: missing URI | sessionId={}", sessionId);
                session.close();
                return;
            }

            Map<String, String> queryParams = extractQueryParams(uri.getQuery());
            log.debug("Query params extracted | sessionId={} | params={}", sessionId, sanitizeParamsForLogging(queryParams));

            String userIdRaw = queryParams.get("userId");
            long userId;
            try {
                userId = Long.parseLong(userIdRaw);
            } catch (NumberFormatException | NullPointerException e) {
                log.warn("Connection rejected: invalid userId | sessionId={} | rawUserId={}", sessionId, userIdRaw);
                sendErrorAndClose(session, "Bad Request", "Invalid or missing userId");
                return;
            }
            MDC.put("userId", String.valueOf(userId));

            String token = queryParams.get("token");
            if (token == null || token.isBlank() || token.equals("null")) {
                log.warn("Connection rejected: missing/invalid token | sessionId={} | userId={}", sessionId, userId);
                sendErrorAndClose(session, "Unauthorized", "Missing or invalid token");
                return;
            }

            log.info("Connection authenticated | sessionId={} | userId={} | token={}", sessionId, userId, maskToken(token));
            handleUserConnection(session, userId, token);

        } finally {
            MDC.clear();
        }
    }

    private void handleUserConnection(WebSocketSession session, Long userId, String token) throws IOException {
        String sessionId = UUID.randomUUID().toString();
        String sessionKey = "user_session:" + userId + ":" + sessionId;
        String sessionsIndexKey = "user_sessions:" + userId;

        log.info("Initializing user session | userId={} | sessionId={}", userId, sessionId);

        try {
            UserDTO user = userServiceClient.findById(userId);
            log.debug("User lookup succeeded | userId={} | email={}", userId, user != null ? user.getEmail() : null);

            historyClient.findByUserId(user.getId(), token)
                .thenAccept(historyResponseMap -> {
                    MDC.put("userId", String.valueOf(userId));
                    MDC.put("sessionId", sessionId);
                    try {
                        log.info("History fetched successfully | userId={} | sessionId={}", userId, sessionId);

                        HistorySection historySection = buildHistorySection(historyResponseMap);

                        DataSection dataSection = new DataSection();
                        dataSection.setSession_date(Instant.now().toString());
                        dataSection.setSessionId(sessionId);
                        dataSection.setUserDetails(user);

                        UserSessionData sessionData = new UserSessionData();
                        sessionData.setData(dataSection);
                        sessionData.setWallet(walletHandler.buildWalletSection(user));
                        sessionData.setHistory(historySection);
                        sessionData.setEncrypted_signature(sec46.data_encryption(userId));

                        String sessionJson = objectMapper.writeValueAsString(sessionData);

                        IMap<String, String> userSessionMap = hazelcastInstance.getMap("user-sessions");
                        userSessionMap.put(sessionKey, sessionJson);
                        ISet<String> userSessionsSet = hazelcastInstance.getSet(sessionsIndexKey);
                        userSessionsSet.add(sessionId);
                        log.debug("Session cached in Hazelcast | sessionKey={} | userId={}", sessionKey, userId);

                        sendMessage(session, Map.of(
                            "status", "success",
                            "type", "SESSION_INITIALIZED",
                            "data", sessionData
                        ));
                        log.info("SESSION_INITIALIZED sent | userId={} | sessionId={}", userId, sessionId);

                    } catch (IOException e) {
                        log.error("Session build failed | userId={} | sessionId={} | error={}",
                                userId, sessionId, e.getMessage(), e);
                        try {
                            sendMessage(session, Map.of(
                                "status", "error",
                                "message", "Session build failed: " + e.getMessage()
                            ));
                        } catch (IOException ignored) {}
                    } finally {
                        MDC.clear();
                    }
                })
                .exceptionally(ex -> {
                    MDC.put("userId", String.valueOf(userId));
                    MDC.put("sessionId", sessionId);
                    try {
                        log.warn("History fetch failed, falling back to empty history | userId={} | sessionId={} | error={}",
                                userId, sessionId, ex.getMessage());

                        HistorySection emptyHistory = buildEmptyHistorySection();

                        DataSection dataSection = new DataSection();
                        dataSection.setSession_date(Instant.now().toString());
                        dataSection.setSessionId(sessionId);
                        dataSection.setUserDetails(user);

                        UserSessionData sessionData = new UserSessionData();
                        sessionData.setData(dataSection);
                        sessionData.setWallet(walletHandler.buildWalletSection(user));
                        sessionData.setHistory(emptyHistory);
                        sessionData.setEncrypted_signature(sec46.data_encryption(userId));

                        sendMessage(session, Map.of(
                            "status", "success",
                            "type", "SESSION_INITIALIZED",
                            "data", sessionData
                        ));
                        log.info("SESSION_INITIALIZED sent (empty history fallback) | userId={} | sessionId={}", userId, sessionId);

                    } catch (IOException ioEx) {
                        log.error("Failed to send fallback session response | userId={} | sessionId={} | error={}",
                                userId, sessionId, ioEx.getMessage(), ioEx);
                    } finally {
                        MDC.clear();
                    }
                    return null;
                });

        } catch (Exception e) {
            log.error("Failed to initialize user session | userId={} | sessionId={} | error={}",
                    userId, sessionId, e.getMessage(), e);
            sendErrorAndClose(session, "Error", e.getMessage());
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String requestId = UUID.randomUUID().toString();
        MDC.put("sessionId", session.getId());
        MDC.put("requestId", requestId);

        try {
            Map<String, Object> payload = objectMapper.readValue(message.getPayload(),
                new TypeReference<Map<String, Object>>() {});

            Map<String, Object> responseMessageObject = new LinkedHashMap<>();
            String type = (String) payload.get("type");

            log.info("WS message received | sessionId={} | requestId={} | type={} | payload={}",
                    session.getId(), requestId, type, sanitizePayloadForLogging(payload));

            if (type == null || type.isEmpty()) {
                log.warn("Rejected message: missing type | sessionId={} | requestId={}", session.getId(), requestId);
                responseMessageObject.put("status", "error");
                responseMessageObject.put("type", "message");
                responseMessageObject.put("message", "Missing Type param..! Type parameter must be provided.");
                sendMessage(session, responseMessageObject);
                return;
            }

            switch (type) {
                case "create_wallet" -> handleCreateWallet(session, payload, responseMessageObject, requestId);
                case "credit_wallet" -> handleCreditWallet(session, payload, requestId);
                case "swap_currency" -> handleSwapCurrency(session, payload, responseMessageObject, requestId);
                default -> log.warn("Unhandled message type | sessionId={} | requestId={} | type={}",
                        session.getId(), requestId, type);
            }

        } catch (IOException | NumberFormatException e) {
            log.error("Failed to process WS message | sessionId={} | requestId={} | error={}",
                    session.getId(), requestId, e.getMessage(), e);
            sendErrorAndClose(session, "Error", e.getMessage());
        } finally {
            MDC.clear();
        }
    }

    private void handleCreateWallet(WebSocketSession session, Map<String, Object> payload, Map<String, Object> responseMessageObject, String requestId) {
        String userStringId = String.valueOf(payload.get("userId"));
        Long userId = Long.valueOf(userStringId);
        MDC.put("userId", userStringId);

        log.info("Processing create_wallet | userId={} | requestId={}", userId, requestId);

        synchronized (("create_new_account-" + userId).intern()) {
            try {
                Wallet wallet = walletRepository.findWalletByUserId(userId);
                if (wallet != null) {
                    log.warn("create_wallet rejected: wallet already exists | userId={} | walletId={}",
                            userId, wallet.getId());
                    sendErrorAndClose(session, "User already have account.",
                            "This user already have an account.");
                    return;
                }

                walletHandler.createAccount(userId);
                log.info("Wallet account created | userId={}", userId);

                responseMessageObject.put("status", "success");
                responseMessageObject.put("type", "account_created");
                responseMessageObject.put("message", "Wallet account successfully created for user.");
                sendMessage(session, responseMessageObject);

            } catch (IOException e) {
                log.error("Failed to create wallet | userId={} | error={}", userId, e.getMessage(), e);
                try {
                    sendMessage(session, Map.of(
                        "status", "error",
                        "type", "account_creation_failed",
                        "message", "Failed to create wallet: " + e.getMessage()
                    ));
                } catch (IOException ignored) {}
            }
        }
    }

    private void handleCreditWallet(WebSocketSession session, Map<String, Object> payload, String requestId) {
        String userStringId = String.valueOf(payload.get("userId"));
        Long userId = Long.valueOf(userStringId);

        String currency = (String) payload.get("currencyType");
        BigDecimal amount = new BigDecimal(((String) payload.get("amount")).replace(",", "."));
        Currency currencyType = Currency.valueOf(currency.toUpperCase());

        long startTime = System.currentTimeMillis();
        MDC.put("userId", userStringId);
        MDC.put("operation", "credit_wallet");

        log.info("Processing credit_wallet | userId={} | requestId={}", userId, requestId);

        synchronized (("credit_wallet_" + userId).intern()) {
            try {
                Wallet wallet = walletRepository.findWalletByUserId(userId);

                if (wallet == null) {
                    log.warn("Wallet not found | userId={}", userId);
                    sendCreditWalletResponse(session, userId, null, false, "Wallet not found");
                    return;
                }

                List<CurrencyBalanceDTO> balances = wallet.getBalances().stream()
                        .map(b -> new CurrencyBalanceDTO(b.getCurrencyCode(), b.getCurrencySymbol(), b.getBalance()))
                        .toList();

                this.updateWallet(wallet.getId(), userId, currencyType, amount);

                WalletResponseDTO walletResponse = new WalletResponseDTO(
                        wallet.getId(), wallet.getUserId(), balances, wallet.getCreatedOn(), wallet.getUpdatedOn());

                long duration = System.currentTimeMillis() - startTime;
                log.info("Wallet fetched | userId={} | walletId={} | balances={} | durationMs={}",
                        userId, wallet.getId(), balances.size(), duration);

                if (session.isOpen()) {
                    sendCreditWalletResponse(session, userId, walletResponse, true, "Wallet fetched successfully");
                }

            } catch (Exception e) {
                long duration = System.currentTimeMillis() - startTime;
                log.error("Failed to fetch wallet | userId={} | durationMs={} | error={}",
                        userId, duration, e.getMessage(), e);

                if (session.isOpen()) {
                    sendCreditWalletResponse(session, userId, null, false, "Failed to fetch wallet: " + e.getMessage());
                }
            } finally {
                MDC.remove("operation");
            }
        }
    }
  
    private void handleSwapCurrency(WebSocketSession session, Map<String, Object> payload,
                                     Map<String, Object> responseMessageObject, String requestId) throws IOException {
        Boolean acceptRate  = (Boolean) payload.get("acceptRate");
        String fromCurrency = (String)  payload.get("fromCurrency");
        String toCurrency   = (String)  payload.get("toCurrency");
        String jwt          = String.valueOf(payload.get("token"));
        Long userId         = payload.get("userId") != null
                                ? ((Number) payload.get("userId")).longValue()
                                : null;

        MDC.put("operation", "swap_currency");
        if (userId != null) MDC.put("userId", String.valueOf(userId));

        log.info("Processing swap_currency | userId={} | from={} | to={} | acceptRate={} | requestId={}",
                userId, fromCurrency, toCurrency, acceptRate, requestId);

        BigDecimal amount = null;
        try {
            String rawAmount = (String) payload.get("amount");
            if (rawAmount != null && !rawAmount.isBlank()) {
                amount = new BigDecimal(rawAmount.replace(",", "."));
            }
        } catch (NumberFormatException e) {
            log.warn("swap_currency rejected: invalid amount format | userId={} | rawAmount={}",
                    userId, payload.get("amount"));
            sendMessage(session, Map.of("type", "error", "message", "Invalid amount format"));
            return;
        }

        if (userId == null)                                      { log.warn("swap_currency rejected: missing userId"); sendMessage(session, Map.of("type", "error", "message", "User ID is required"));        return; }
        if (fromCurrency == null || fromCurrency.isBlank())      { log.warn("swap_currency rejected: missing fromCurrency | userId={}", userId); sendMessage(session, Map.of("type", "error", "message", "From currency is required"));   return; }
        if (toCurrency   == null || toCurrency.isBlank())        { log.warn("swap_currency rejected: missing toCurrency | userId={}", userId); sendMessage(session, Map.of("type", "error", "message", "To currency is required"));     return; }
        if (jwt == null  || jwt.isBlank() || jwt.equals("null")) { log.warn("swap_currency rejected: missing token | userId={}", userId); sendMessage(session, Map.of("type", "error", "message", "Token is required"));           return; }
        if (acceptRate   == null)                                { log.warn("swap_currency rejected: missing acceptRate | userId={}", userId); sendMessage(session, Map.of("type", "error", "message", "Accept rate is required"));     return; }
        if (amount == null)                                      { log.warn("swap_currency rejected: missing amount | userId={}", userId); sendMessage(session, Map.of("type", "error", "message", "Amount is required"));          return; }
        if (amount.compareTo(BigDecimal.ZERO) <= 0)              { log.warn("swap_currency rejected: non-positive amount | userId={} | amount={}", userId, amount); sendMessage(session, Map.of("type", "error", "message", "Amount must be greater than zero")); return; }

        log.debug("swap_currency validated, processing | userId={} | from={} | to={} | amount={} | token={}",
                userId, fromCurrency, toCurrency, amount, maskToken(jwt));

        Map<String, Object> swapResult = processCurrencySwap(userId, fromCurrency, toCurrency, amount, acceptRate, session, jwt);

        log.info("swap_currency result | userId={} | status={} | result={}", userId, swapResult.get("status"), swapResult);

        responseMessageObject.putAll(swapResult);
        responseMessageObject.put("type", "swap_response");
        sendMessage(session, responseMessageObject);
    }

    private void sendCreditWalletResponse(WebSocketSession session, Long userId, WalletResponseDTO wallet, boolean success, String message) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("type", "credit_wallet_response");
            response.put("success", success);
            response.put("userId", userId);
            response.put("message", message);

            if (wallet != null) {
                response.put("wallet", wallet);
            }

            synchronized (session) {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
                    log.debug("credit_wallet_response sent | userId={} | success={}", userId, success);
                }
            }
        } catch (IOException e) {
            log.error("Failed to send credit_wallet_response | userId={} | error={}", userId, e.getMessage(), e);
            sendErrorAndClose(session, "Error", e.getMessage());
        }
    }

    private WalletResponseDTO updateWallet(Long walletId, Long userId, Currency currencyType, BigDecimal amount) {
        log.debug("Calling walletService.updateBalance | walletId={} | userId={} | currency={} | amount={}",
                walletId, userId, currencyType, amount);
        try {
            ResponseEntity<?> responseEntity = walletService.updateBalance(currencyType.toString(), amount, userId, walletId);
            Object body = responseEntity.getBody();
            log.debug("updateBalance responded | walletId={} | userId={} | status={}",
                    walletId, userId, responseEntity.getStatusCode());

            if (body instanceof WalletResponseDTO dto) {
                return dto;
            }

            if (body instanceof Map<?, ?> mapBody) {
                Long id = mapBody.get("id") != null ? Long.valueOf(mapBody.get("id").toString()) : null;
                Long uid = mapBody.get("user_id") != null ? Long.valueOf(mapBody.get("user_id").toString()) : null;

                List<CurrencyBalanceDTO> balances = new ArrayList<>();
                Object balancesObj = mapBody.get("balances");
                if (balancesObj instanceof List<?>) {
                    for (Object b : (List<?>) balancesObj) {
                        if (b instanceof Map<?, ?> balanceMap) {
                            String code = (String) balanceMap.get("currency_code");
                            String symbol = (String) balanceMap.get("symbol");
                            BigDecimal bal = new BigDecimal(balanceMap.get("balance").toString());
                            balances.add(new CurrencyBalanceDTO(code, symbol, bal));
                        }
                    }
                }

                LocalDateTime createdOn = mapBody.get("created_on") != null
                        ? LocalDateTime.parse(mapBody.get("created_on").toString()) : null;
                LocalDateTime updatedOn = mapBody.get("updated_on") != null
                        ? LocalDateTime.parse(mapBody.get("updated_on").toString()) : null;

                return new WalletResponseDTO(id, uid, balances, createdOn, updatedOn);
            }

            log.warn("updateBalance returned unrecognized body type | walletId={} | userId={} | bodyType={}",
                    walletId, userId, body != null ? body.getClass() : "null");
            return new WalletResponseDTO();

        } catch (NumberFormatException e) {
            log.error("updateWallet failed to parse balance response | walletId={} | userId={} | error={}",
                    walletId, userId, e.getMessage(), e);
            return new WalletResponseDTO();
        }
    }

    private void sendWalletUpdateResponse(WebSocketSession session, Long userId, String token) {
        log.debug("Building wallet_update_response | userId={}", userId);
        try {
            UserDTO user = userServiceClient.findById(userId);

            HistoryResponse historyResponse = historyClient.findByUserId(userId, token)
                .get(10, TimeUnit.SECONDS);
            HistorySection historySection = buildHistorySection(historyResponse);

            DataSection dataSection = new DataSection();
            dataSection.setSession_date(Instant.now().toString());
            dataSection.setSessionId(UUID.randomUUID().toString());
            dataSection.setUserDetails(user);

            UserSessionData sessionData = new UserSessionData();
            sessionData.setData(dataSection);
            sessionData.setWallet(walletHandler.buildWalletSection(user));
            sessionData.setHistory(historySection);
            sessionData.setEncrypted_signature(sec46.data_encryption(userId));

            Map<String, Object> response = new HashMap<>();
            response.put("type", "wallet_update_response");
            response.put("success", true);
            response.put("userId", userId);
            response.put("data", sessionData);
            response.put("message", "Wallet updated successfully");

            synchronized (session) {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
                    log.info("wallet_update_response sent | userId={}", userId);
                }
            }

        } catch (IOException | InterruptedException | ExecutionException | TimeoutException e) {
            log.error("Failed to build/send wallet_update_response | userId={} | error={}", userId, e.getMessage(), e);
            try {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("type", "wallet_update_error");
                errorResponse.put("success", false);
                errorResponse.put("message", "Failed to refresh data: " + e.getMessage());

                synchronized (session) {
                    if (session.isOpen()) {
                        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(errorResponse)));
                    }
                }
            } catch (IOException ex) {
                log.error("Failed to send wallet_update_error | userId={} | error={}", userId, ex.getMessage(), ex);
            }
        }
    }

    private Map<String, String> extractQueryParams(String query) {
        Map<String, String> queryParams = new HashMap<>();
        if (query == null || query.isEmpty())
            return queryParams;

        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                queryParams.put(keyValue[0], keyValue[1]);
            } else {
                log.debug("Skipped malformed query param pair: {}", pair);
            }
        }
        return queryParams;
    }

    private void sendErrorAndClose(WebSocketSession session, String message, String details) {
        log.warn("Sending error and closing session | sessionId={} | message={} | details={}",
                session.getId(), message, details);
        try {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", message);
            error.put("details", details);

            String errorJson = objectMapper.writeValueAsString(error);
            session.sendMessage(new TextMessage(errorJson));
            try {
                Thread.sleep(50);
            } catch (InterruptedException ignored) {
            }

            if (session.isOpen()) {
                session.close(CloseStatus.NOT_ACCEPTABLE.withReason(message));
            }
        } catch (IOException e) {
            log.error("Failed to send error response, forcing close | sessionId={} | error={}",
                    session.getId(), e.getMessage(), e);
            try {
                if (session.isOpen()) {
                    session.close(CloseStatus.SERVER_ERROR.withReason("Internal server error"));
                }
            } catch (IOException ignored) {}
        }
    }

    private void sendMessage(WebSocketSession session, Object message) throws IOException {
        try {
            String json = objectMapper
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(message);

            session.sendMessage(new TextMessage(json));
            log.debug("Message sent | sessionId={} | bytes={}", session.getId(), json.length());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize outgoing message | sessionId={} | error={}", session.getId(), e.getMessage(), e);
            throw new IOException("Failed to send message", e);
        }
    }

    private String maskToken(String token) {
        if (token == null || token.isBlank() || token.equals("null")) return "[absent]";
        int visible = Math.min(6, token.length());
        return token.substring(0, visible) + "..." + "[" + token.length() + " chars]";
    }

    private Map<String, Object> sanitizePayloadForLogging(Map<String, Object> payload) {
        Map<String, Object> safe = new LinkedHashMap<>(payload);
        if (safe.containsKey("token")) {
            safe.put("token", maskToken(String.valueOf(safe.get("token"))));
        }
        return safe;
    }

    private Map<String, String> sanitizeParamsForLogging(Map<String, String> params) {
        Map<String, String> safe = new LinkedHashMap<>(params);
        if (safe.containsKey("token")) {
            safe.put("token", maskToken(safe.get("token")));
        }
        return safe;
    }

    public static String FormatBigDecimal(BigDecimal amount) {
        String pattern = "#,##0.00";
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(',');
        symbols.setDecimalSeparator('.');
        DecimalFormat decimalFormat = new DecimalFormat(pattern, symbols);
        return decimalFormat.format(amount);
    }

    private HistorySection buildHistorySection(HistoryResponse historyResponse) {
        if (!historyResponse.isSuccess() || historyResponse.getData() == null) {
            log.debug("History response empty/unsuccessful, building empty section");
            return buildEmptyHistorySection();
        }

        List<HistoryDTO> transactions = historyResponse.getData();
        log.debug("Building history section from {} transactions", transactions.size());

        List<TransactionRecord> deposits = new ArrayList<>();
        List<TransactionRecord> withdraws = new ArrayList<>();
        List<TransactionRecord> swaps = new ArrayList<>();
        List<TransactionRecord> services = new ArrayList<>();
        List<TransactionRecord> transfers = new ArrayList<>();
        List<TransactionRecord> maintenances = new ArrayList<>();

        for (HistoryDTO item : transactions) {
            TransactionRecord record = toTransactionRecord(item);
            switch (record.getType().toUpperCase()) {
                case "DEPOSIT" -> deposits.add(record);
                case "WITHDRAW" -> withdraws.add(record);
                case "SWAP" -> swaps.add(record);
                case "CREDITED", "DEBITED", "SERVICE" -> services.add(record);
                case "TRANSFER" -> transfers.add(record);
                case "MAINTENANCE" -> maintenances.add(record);
                default -> log.debug("Ignoring unknown history transaction type: {}", record.getType());
            }
        }

        HistorySection historySection = new HistorySection();
        historySection.setDeposit_container(deposits);
        historySection.setWithdraws_container(withdraws);
        historySection.setSwap_container(swaps);
        historySection.setServices_container(services);
        historySection.setTransfer_container(transfers);
        historySection.setMaintenances_container(maintenances);

        return historySection;
    }

    private HistorySection buildEmptyHistorySection() {
        HistorySection history = new HistorySection();
        history.setDeposit_container(new ArrayList<>());
        history.setWithdraws_container(new ArrayList<>());
        history.setSwap_container(new ArrayList<>());
        history.setServices_container(new ArrayList<>());
        history.setTransfer_container(new ArrayList<>());
        history.setMaintenances_container(new ArrayList<>());
        return history;
    }

    private TransactionRecord toTransactionRecord(HistoryDTO item) {
        TransactionRecord record = new TransactionRecord();
        record.setId(item.getId());
        record.setType(item.getType());
        record.setAmount(item.getAmount());
        record.setCurrency(item.getCurrencyType());
        record.setStatus(item.getStatus());
        record.setTimestamp(item.getCreatedOn() != null
            ? item.getCreatedOn().toString()
            : Instant.now().toString());
        record.setDescription(item.getDescription());
        return record;
    }

    private Map<String, Object> processCurrencySwap(Long userId, String fromCurrency, String toCurrency, BigDecimal amount, Boolean acceptRate, WebSocketSession session, String token) {
        try {
            Map<String, Object> validationResult = validateSwapRequest(userId, fromCurrency, toCurrency, amount);
            if (!(Boolean) validationResult.get("valid")) {
                log.warn("Swap validation failed | userId={} | reason={}", userId, validationResult.get("message"));
                return Map.of(
                    "status", "FAILED",
                    "message", validationResult.get("message")
                );
            }

            BigDecimal exchangeRate = getExchangeRate(fromCurrency, toCurrency);
            log.debug("Resolved exchange rate | userId={} | {}->{} | rate={}", userId, fromCurrency, toCurrency, exchangeRate);

            Map<String, Object> calculation = calculateSwapAmounts(amount, exchangeRate);
            log.debug("Swap calculation | userId={} | calculation={}", userId, calculation);

            return executeSwapTransaction(userId, fromCurrency, toCurrency, amount, exchangeRate, calculation, session, token);
        } catch (Exception e) {
            log.error("Swap processing failed | userId={} | error={}", userId, e.getMessage(), e);
            return Map.of(
                "status", "FAILED",
                "message", "Swap processing failed: " + e.getMessage()
            );
        }
    }

    private Map<String, Object> validateSwapRequest(Long userId, String fromCurrency, String toCurrency, BigDecimal amount) {
        if (fromCurrency.equals(toCurrency)) {
            return Map.of("valid", false, "message", "Source and target currencies cannot be the same");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Map.of("valid", false, "message", "Amount must be greater than zero");
        }

        Wallet wallet = walletRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Wallet not found"));

        boolean sourceCurrencyExists = wallet.getBalances().stream()
            .anyMatch(balance -> balance.getCurrencyCode().equals(fromCurrency));

        if (!sourceCurrencyExists) {
            return Map.of("valid", false, "message", "Source currency not found in wallet");
        }

        boolean targetCurrencyExists = wallet.getBalances().stream()
            .anyMatch(balance -> balance.getCurrencyCode().equals(toCurrency));

        if (!targetCurrencyExists) {
            return Map.of("valid", false, "message", "Target currency not supported");
        }

        BigDecimal sourceBalance = getBalanceForCurrency(wallet, fromCurrency);
        if (sourceBalance.compareTo(amount) < 0) {
            log.warn("Insufficient balance for swap | userId={} | available={} | requested={}",
                    userId, sourceBalance, amount);
            return Map.of("valid", false, "message", "Insufficient balance in source currency");
        }

        return Map.of("valid", true);
    }

    private BigDecimal getExchangeRate(String fromCurrency, String toCurrency) {
        Map<String, Map<String, BigDecimal>> exchangeRates = Map.of(
            "USD", Map.of("EUR", new BigDecimal("0.92"), "GBP", new BigDecimal("0.80"), "NGN", new BigDecimal("1500.50"), "JPY", new BigDecimal("147.11"), "AUD", new BigDecimal("1.52"), "CAD", new BigDecimal("1.35"), "CHF", new BigDecimal("0.88"), "CNY", new BigDecimal("7.25"), "INR", new BigDecimal("83.12")),
            "EUR", Map.of("USD", new BigDecimal("1.09"), "GBP", new BigDecimal("0.87"), "NGN", new BigDecimal("1630.75"), "JPY", new BigDecimal("159.25"), "AUD", new BigDecimal("1.65"), "CAD", new BigDecimal("1.47"), "CHF", new BigDecimal("0.96"), "CNY", new BigDecimal("7.88"), "INR", new BigDecimal("90.35")),
            "GBP", Map.of("USD", new BigDecimal("1.25"), "EUR", new BigDecimal("1.15"), "NGN", new BigDecimal("1875.30"), "JPY", new BigDecimal("184.22"), "AUD", new BigDecimal("1.90"), "CAD", new BigDecimal("1.69"), "CHF", new BigDecimal("1.10"), "CNY", new BigDecimal("9.06"), "INR", new BigDecimal("103.89")),
            "NGN", Map.of("USD", new BigDecimal("0.00067"), "EUR", new BigDecimal("0.00061"), "GBP", new BigDecimal("0.00053"), "JPY", new BigDecimal("0.098"), "AUD", new BigDecimal("0.00101"), "CAD", new BigDecimal("0.00090"), "CHF", new BigDecimal("0.00059"), "CNY", new BigDecimal("0.0048"), "INR", new BigDecimal("0.055")),
            "JPY", Map.of("USD", new BigDecimal("0.0068"), "EUR", new BigDecimal("0.0063"), "GBP", new BigDecimal("0.0054"), "NGN", new BigDecimal("10.20"), "AUD", new BigDecimal("0.0103"), "CAD", new BigDecimal("0.0092"), "CHF", new BigDecimal("0.0060"), "CNY", new BigDecimal("0.0493"), "INR", new BigDecimal("0.565")),
            "AUD", Map.of("USD", new BigDecimal("0.66"), "EUR", new BigDecimal("0.61"), "GBP", new BigDecimal("0.53"), "NGN", new BigDecimal("987.45"), "JPY", new BigDecimal("97.10"), "CAD", new BigDecimal("0.89"), "CHF", new BigDecimal("0.58"), "CNY", new BigDecimal("4.77"), "INR", new BigDecimal("54.68")),
            "CAD", Map.of("USD", new BigDecimal("0.74"), "EUR", new BigDecimal("0.68"), "GBP", new BigDecimal("0.59"), "NGN", new BigDecimal("1111.11"), "JPY", new BigDecimal("108.75"), "AUD", new BigDecimal("1.12"), "CHF", new BigDecimal("0.65"), "CNY", new BigDecimal("5.37"), "INR", new BigDecimal("61.55")),
            "CHF", Map.of("USD", new BigDecimal("1.14"), "EUR", new BigDecimal("1.04"), "GBP", new BigDecimal("0.91"), "NGN", new BigDecimal("1705.88"), "JPY", new BigDecimal("166.67"), "AUD", new BigDecimal("1.72"), "CAD", new BigDecimal("1.54"), "CNY", new BigDecimal("8.24"), "INR", new BigDecimal("94.50")),
            "CNY", Map.of("USD", new BigDecimal("0.14"), "EUR", new BigDecimal("0.13"), "GBP", new BigDecimal("0.11"), "NGN", new BigDecimal("206.90"), "JPY", new BigDecimal("20.28"), "AUD", new BigDecimal("0.21"), "CAD", new BigDecimal("0.19"), "CHF", new BigDecimal("0.12"), "INR", new BigDecimal("11.46")),
            "INR", Map.of("USD", new BigDecimal("0.012"), "EUR", new BigDecimal("0.011"), "GBP", new BigDecimal("0.0096"), "NGN", new BigDecimal("18.05"), "JPY", new BigDecimal("1.77"), "AUD", new BigDecimal("0.018"), "CAD", new BigDecimal("0.016"), "CHF", new BigDecimal("0.0106"), "CNY", new BigDecimal("0.087"))
        );

        if (!exchangeRates.containsKey(fromCurrency)) {
            throw new RuntimeException("Unsupported source currency: " + fromCurrency);
        }

        Map<String, BigDecimal> fromRates = exchangeRates.get(fromCurrency);
        if (!fromRates.containsKey(toCurrency)) {
            throw new RuntimeException("Unsupported target currency: " + toCurrency + " for source: " + fromCurrency);
        }

        BigDecimal rate = fromRates.get(toCurrency);
        BigDecimal margin = new BigDecimal("0.005");
        return rate.multiply(BigDecimal.ONE.subtract(margin));
    }

    private Map<String, Object> calculateSwapAmounts(BigDecimal amount, BigDecimal exchangeRate) {
        BigDecimal baseConvertedAmount = amount.multiply(exchangeRate);
        BigDecimal feePercentage = calculateSwapFee(amount);
        BigDecimal feeAmount = baseConvertedAmount.multiply(feePercentage);
        BigDecimal finalAmount = baseConvertedAmount.subtract(feeAmount);

        return Map.of(
            "baseConvertedAmount", baseConvertedAmount,
            "feePercentage", feePercentage,
            "feeAmount", feeAmount,
            "finalAmount", finalAmount,
            "exchangeRate", exchangeRate
        );
    }

    private BigDecimal calculateSwapFee(BigDecimal amount) {
        if (amount.compareTo(new BigDecimal("1000")) > 0) {
            return new BigDecimal("0.005");
        } else if (amount.compareTo(new BigDecimal("100")) > 0) {
            return new BigDecimal("0.01");
        } else {
            return new BigDecimal("0.015");
        }
    }

    private Map<String, Object> executeSwapTransaction(Long userId, String fromCurrency, String toCurrency, BigDecimal amount, BigDecimal exchangeRate, Map<String, Object> calculation, WebSocketSession session, String token) {
        try {
            Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

            Long walletId = wallet.getId();
            BigDecimal finalAmount = (BigDecimal) calculation.get("finalAmount");

            UserDTO user = userServiceClient.findById(userId);

            String userFullName = user.getRecords().get(0).getFirstName().toUpperCase() + " " + user.getRecords().get(0).getLastName().toUpperCase();
            String userEmail = user.getEmail();

            BigDecimal previousBalanceOnFromWallet;
            BigDecimal availableBalance;

            synchronized (("swap_currency_" + userId).intern()) {
                log.debug("Acquired swap lock | userId={} | walletId={}", userId, walletId);

                previousBalanceOnFromWallet = wallet.getBalances().stream()
                    .filter(b -> fromCurrency.equalsIgnoreCase(b.getCurrencyCode()))
                    .map(b -> b.getBalance())
                    .findFirst()
                    .orElse(BigDecimal.ZERO);

                BigDecimal negativeAmount = amount.negate();

                log.info("Debiting source currency | userId={} | currency={} | amount={}", userId, fromCurrency, negativeAmount);
                updateWallet(walletId, userId, Currency.valueOf(fromCurrency.toUpperCase()), negativeAmount);

                log.info("Crediting target currency | userId={} | currency={} | amount={}", userId, toCurrency, finalAmount);
                updateWallet(walletId, userId, Currency.valueOf(toCurrency.toUpperCase()), finalAmount);

                sendWalletUpdateResponse(session, userId, token);

                Wallet new_wallet_request = walletRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("Wallet not found"));

                availableBalance = new_wallet_request.getBalances().stream()
                    .filter(b -> fromCurrency.equalsIgnoreCase(b.getCurrencyCode()))
                    .map(b -> b.getBalance())
                    .findFirst()
                    .orElse(BigDecimal.ZERO);
            }

            String transactionId = "SWAP_" + System.currentTimeMillis() + "_" + userId;
            String swapCurrency = fromCurrency + "/" + toCurrency;
            BigDecimal feeAmount = (BigDecimal) calculation.get("feeAmount");

            log.info("Swap executed | transactionId={} | userId={} | pair={} | amount={} | finalAmount={} | fee={} | previousBalance={} | newBalance={}",
                    transactionId, userId, swapCurrency, amount, finalAmount, feeAmount, previousBalanceOnFromWallet, availableBalance);

            SwapHistoryRequest historyPayload = new SwapHistoryRequest();
            historyPayload.setAmount(amount);
            historyPayload.setWalletId(walletId);
            historyPayload.setCurrencyType(toCurrency);
            historyPayload.setTransactionType("SWAP");
            historyPayload.setDescription("SWAP " + swapCurrency + " : cut " + exchangeRate);
            historyPayload.setAccountHolder(userFullName);
            historyPayload.setPreviousBalance(previousBalanceOnFromWallet);
            historyPayload.setAvailable(availableBalance);
            historyPayload.setUserId(user.getId());
            historyPayload.setExchangeRate(exchangeRate);
            historyPayload.setOriginalCurrency(fromCurrency);
            historyPayload.setFeeAmount(feeAmount);
            historyPayload.setNetAmount(finalAmount);
            historyPayload.setStatus("COMPLETED");

            CompletableFuture<Void> saveHistory = createHistory(historyPayload, token);

            log.debug("Sending swap alert notification | userId={} | email={} | transactionId={}",
                    userId, userEmail, transactionId);
            notificationServiceClient.sendSwapAlert(
                userEmail,
                userFullName,
                amount,
                fromCurrency,
                availableBalance,
                transactionId,
                previousBalanceOnFromWallet,
                swapCurrency
            );

            CompletableFuture.allOf(saveHistory).join();
            log.debug("Swap history saved | transactionId={} | userId={}", transactionId, userId);

            return Map.of(
                "status", "COMPLETED",
                "transactionId", transactionId,
                "fromAmount", amount,
                "toAmount", finalAmount,
                "exchangeRate", exchangeRate,
                "feeAmount", calculation.get("feeAmount"),
                "feePercentage", calculation.get("feePercentage"),
                "timestamp", LocalDateTime.now().toString(),
                "message", "Currency swap completed successfully"
            );
        } catch (Exception e) {
            log.error("Swap execution failed | userId={} | from={} | to={} | error={}",
                    userId, fromCurrency, toCurrency, e.getMessage(), e);
            return Map.of(
                "status", "FAILED",
                "message", "Swap failed: " + e.getMessage()
            );
        }
    }

    private BigDecimal getBalanceForCurrency(Wallet wallet, String currencyCode) {
        return wallet.getBalances().stream()
            .filter(balance -> balance.getCurrencyCode().equals(currencyCode))
            .findFirst()
            .map(CurrencyBalanceMapStruct::getBalance)
            .orElse(BigDecimal.ZERO);
    }

    private CompletableFuture<Void> createHistory(SwapHistoryRequest request, String token) {
        log.debug("Creating swap history record | walletId={} | userId={}", request.getWalletId(), request.getUserId());
        historyClient.createUserHistory(request, token);
        return CompletableFuture.completedFuture(null);
    }
}