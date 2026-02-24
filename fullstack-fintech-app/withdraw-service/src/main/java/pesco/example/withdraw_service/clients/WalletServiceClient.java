package pesco.example.withdraw_service.clients;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.client.inject.GrpcClient;
import pesco.example.withdraw_service.dtos.WalletBalanceDTO;
import pesco.example.withdraw_service.dtos.WalletSectionDTO;
import pesco.example.withdraw_service.dtos.WalletSettingsDTO;
import pesco.example.withdraw_service.exceptions.WalletNotFoundException;
import pesco.wallet_service.grpc.CreateWalletRequest;
import pesco.wallet_service.grpc.FindUserWalletPinRequest;
import pesco.wallet_service.grpc.FindUserWalletPinResponse;
import pesco.wallet_service.grpc.GetWalletByCurrencyRequest;
import pesco.wallet_service.grpc.GetWalletSectionRequest;
import pesco.wallet_service.grpc.TransferResponse;
import pesco.wallet_service.grpc.TransferToUserRequest;
import pesco.wallet_service.grpc.WalletBalanceResponse;
import pesco.wallet_service.grpc.WalletDeductionRequest;
import pesco.wallet_service.grpc.WalletSectionResponse;
import pesco.wallet_service.grpc.WalletServiceGrpc;
import pesco.wallet_service.grpc.WithdrawResponse;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;


@Service
public class WalletServiceClient {

    @GrpcClient("wallet-service")
    private WalletServiceGrpc.WalletServiceBlockingStub walletServiceStub;
    
    public Object createUserWallet(Long userId) {
        try {
            CreateWalletRequest request = CreateWalletRequest.newBuilder()
                    .setUserId(userId)
                    .build();

            return walletServiceStub.createWallet(request);
        } catch (StatusRuntimeException e) {
            e.printStackTrace();
            throw new RuntimeException("gRPC call to WalletService failed: " + e.getStatus().getDescription(), e);
        }
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
            return walletServiceStub.withDeadlineAfter(5000, TimeUnit.MILLISECONDS) 
                                   .getWalletByCurrency(request);
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == io.grpc.Status.Code.NOT_FOUND) {
                throw new WalletNotFoundException("Wallet or currency not found for userId: " + request.getUserId());
            } else if (e.getStatus().getCode() == io.grpc.Status.Code.UNAVAILABLE) {
                throw new RuntimeException("Wallet service is unavailable. Please try again later.", e);
            } else if (e.getStatus().getCode() == io.grpc.Status.Code.DEADLINE_EXCEEDED) {
                throw new RuntimeException("Wallet service request timeout. Please try again.", e);
            }

            throw new RuntimeException("gRPC error while fetching balance by currency: " + e.getMessage(), e);
        }
    }

    public WithdrawResponse walletDeduct(WalletDeductionRequest payloads) {
        return walletServiceStub.withdrawIn(payloads);
    }

    public FindUserWalletPinResponse findUserWalletPin(FindUserWalletPinRequest payload) {
        return walletServiceStub.findUserWalletPin(payload);
    }
    
    public WalletSettingsDTO convertToWalletDTO(String response) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            return objectMapper.readValue(response, WalletSettingsDTO.class);
        } catch (Exception e) {
            System.err.println("Error converting response to UserDTO: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error converting response to UserDTO", e);
        }
    }

    public TransferResponse transferMoney(TransferToUserRequest transferPayload) {
        return walletServiceStub.tranferOut(transferPayload);
    }

    public WalletBalanceResponse getWalletByCurrencyWithRetry(GetWalletByCurrencyRequest request, int maxRetries) {
        for (int i = 0; i < maxRetries; i++) {
            try {
                return getWalletByCurrency(request);
            } catch (RuntimeException e) {
                if (i == maxRetries - 1) throw e; // Last attempt
                try {
                    Thread.sleep(1000 * (i + 1)); // Exponential backoff
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Retry interrupted", ie);
                }
            }
        }
        throw new RuntimeException("Failed after " + maxRetries + " retries");
    }

}
