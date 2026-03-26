package pesco.example.authentication_service.clients;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.client.inject.GrpcClient;
import pesco.example.authentication_service.dtos.WalletBalanceDTO;
import pesco.example.authentication_service.dtos.WalletSectionDTO;
import pesco.example.authentication_service.exceptions.WalletNotFoundException;
import pesco.wallet_service.grpc.GetWalletByCurrencyRequest;
import pesco.wallet_service.grpc.GetWalletSectionRequest;
import pesco.wallet_service.grpc.WalletBalanceResponse;
import pesco.wallet_service.grpc.WalletDeductionRequest;
import pesco.wallet_service.grpc.WalletSectionResponse;
import pesco.wallet_service.grpc.WalletServiceGrpc;
import pesco.wallet_service.grpc.WithdrawResponse;

@Service
public class WalletServiceClient {

    private final Map<String, CompletableFuture<BigDecimal>> pendingRequests = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private WebSocketSession webSocketSession;

    @GrpcClient("wallet-service")
    private WalletServiceGrpc.WalletServiceBlockingStub walletServiceStub;

    @Value("${wallet-websocket.url}") 
    private String walletSocketUrl;


    public CompletableFuture<Map<String, Object>> createUserWallet(Long userId) {
        String url = walletSocketUrl + "?userId=" + userId;

        Map<String, Object> request = Map.of(
            "type", "create_wallet",
            "userId", String.valueOf(userId)
        );

        CompletableFuture<Map<String, Object>> responseFuture = new CompletableFuture<>();
            try {
                String json = objectMapper.writeValueAsString(request);
                StandardWebSocketClient client = new StandardWebSocketClient();

                client.doHandshake(new TextWebSocketHandler() {
                    @Override
                    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
                        session.sendMessage(new TextMessage(json));
                    }

                    @Override
                    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
                        Map<String, Object> res = objectMapper.readValue(message.getPayload(), new TypeReference<>() {});
                        responseFuture.complete(res);
                        session.close();
                    }

                    @Override
                    public void handleTransportError(WebSocketSession session, Throwable exception) {
                        responseFuture.completeExceptionally(exception);
                    }
                }, url);

            } catch (Exception e) {
                responseFuture.completeExceptionally(e);
            }

        return responseFuture.orTimeout(5, TimeUnit.SECONDS);
    }


    public WalletSectionDTO getWalletSectionByUser(Long userId) {
        GetWalletSectionRequest request = GetWalletSectionRequest.newBuilder()
                .setUserId(userId)
                .build();

        try {
            WalletSectionResponse grpcResponse = walletServiceStub.getWalletSectionByUser(request);

            List<WalletBalanceDTO> balances = grpcResponse.getWalletBalancesList().stream()
                    .map(balance -> new WalletBalanceDTO(
                            balance.getCurrencyCode(),
                            balance.getSymbol(),
                            balance.getBalance()
                    ))
                    .collect(Collectors.toList());

            return new WalletSectionDTO(
                    grpcResponse.getWalletId(),
                    grpcResponse.getHasTransferPin(),
                    balances
            );

        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == io.grpc.Status.Code.NOT_FOUND) {
                throw new WalletNotFoundException("Wallet not found for userId: " + userId);
            }

            throw new RuntimeException("gRPC error while fetching wallet section: " + e.getMessage(), e);
        }
    }

    public WalletBalanceResponse getWalletByCurrency(GetWalletByCurrencyRequest request) {
        try {
            return walletServiceStub.getWalletByCurrency(request);
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == io.grpc.Status.Code.NOT_FOUND) {
                throw new WalletNotFoundException("Wallet or currency not found for userId: " + request.getUserId());
            }

            throw new RuntimeException("gRPC error while fetching balance by currency: " + e.getMessage(), e);
        }
    }

    public WithdrawResponse walletDeduct(WalletDeductionRequest payloads) {
        return walletServiceStub.withdrawIn(payloads);
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
        } catch (JsonProcessingException | ParseException e) {
            // e.printStackTrace();
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
