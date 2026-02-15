package pesco.example.virtual_card_service.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pesco.example.virtual_card_service.clients.UserServiceClient;
import pesco.example.virtual_card_service.dto.UserDTO;
import pesco.example.virtual_card_service.enums.BalanceOperation;
import pesco.example.virtual_card_service.enums.CardStatus;
import pesco.example.virtual_card_service.enums.LimitPeriod;
import pesco.example.virtual_card_service.exceptions.*;
import pesco.example.virtual_card_service.models.VirtualCard;
import pesco.example.virtual_card_service.repository.VirtualCardRepository;
import pesco.example.virtual_card_service.requests.CreateVirtualCardRequest;
import pesco.example.virtual_card_service.requests.UpdateBalanceRequest;
import pesco.example.virtual_card_service.requests.UpdateCardStatusRequest;
import pesco.example.virtual_card_service.requests.UpdateVirtualCardRequest;
import pesco.example.virtual_card_service.responses.VirtualCardDetailsResponse;
import pesco.example.virtual_card_service.responses.VirtualCardResponse;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VirtualCardService {

    private final VirtualCardRepository virtualCardRepository;
    private final UserServiceClient userServiceClient;
    private static final SecureRandom random = new SecureRandom();

    // ==================== CREATE ====================
    @Transactional
    public VirtualCardResponse createCard(CreateVirtualCardRequest request) {
        log.info("Creating virtual card for user: {}", request.getUserId());
        
        // Validate user exists (call to User Service)
        validateUserExists(request.getUserId());
        
        // Generate card details
        String cardNumber = generateCardNumber(request.getCardType());
        String cvv = generateCVV();
        String cardId = UUID.randomUUID().toString();
        
        // Calculate expiration (3 years from now)
        LocalDateTime expiresAt = LocalDateTime.now().plusYears(3);
        String expirationMonth = String.format("%02d", expiresAt.getMonthValue());
        String expirationYear = String.valueOf(expiresAt.getYear());
        
        // Build virtual card
        VirtualCard card = VirtualCard.builder()
                .cardId(cardId)
                .userId(request.getUserId())
                .cardNumber(cardNumber)
                .cardHolderName(request.getAccountHolderName().toUpperCase())
                .expirationMonth(expirationMonth)
                .expirationYear(expirationYear)
                .cvv(cvv) 
                .status(CardStatus.PENDING)
                .cardType(request.getCardType())
                .cardPlan(request.getPlan())
                .currency(request.getCurrency())
                .balance(request.getInitialBalance() != null ? request.getInitialBalance() : BigDecimal.ZERO)
                .spendingLimit(request.getSpendingLimit())
                .limitPeriod(request.getLimitPeriod())
                .currentPeriodSpent(BigDecimal.ZERO)
                .allowInternational(request.getAllowInternational())
                .allowOnline(request.getAllowOnline())
                .allowAtm(request.getAllowAtm())
                .allowContactless(request.getAllowContactless())
                .merchantName(request.getMerchantName())
                .merchantId(request.getMerchantId())
                .merchantCategoryCode(request.getMerchantCategoryCode())
                .merchantCountry(request.getMerchantCountry())
                .merchantCity(request.getMerchantCity())
                .bin(cardNumber.substring(0, 6))
                .lastFour(cardNumber.substring(cardNumber.length() - 4))
                .maskedCardNumber(maskCardNumber(cardNumber))
                .expiresAt(expiresAt)
                .activatedAt(LocalDateTime.now())
                .build();
        
        // Set limit reset date if limit period is specified
        if (request.getLimitPeriod() != null) {
            card.setLimitResetDate(calculateLimitResetDate(request.getLimitPeriod()));
        }
        
        VirtualCard savedCard = virtualCardRepository.save(card);
        log.info("Virtual card created successfully: {}", savedCard.getCardId());
        
        return mapToResponse(savedCard);
    }

    // ==================== READ ====================
    @Transactional(readOnly = true)
    public VirtualCardResponse getCardById(String cardId) {
        log.info("Fetching virtual card: {}", cardId);
        VirtualCard card = findCardByIdOrThrow(cardId);
        return mapToResponse(card);
    }

    @Transactional(readOnly = true)
    public VirtualCardDetailsResponse getCardDetails(String cardId) {
        log.info("Fetching virtual card details: {}", cardId);
        VirtualCard card = findCardByIdOrThrow(cardId);
        return mapToDetailsResponse(card);
    }

    @Transactional(readOnly = true)
    public List<VirtualCardResponse> getCardsByUserId(Long userId) {
        log.info("Fetching all cards for user: {}", userId);
        List<VirtualCard> cards = virtualCardRepository.findByUserIdAndDeletedAtIsNull(userId);
        return cards.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<VirtualCardResponse> getCardsByUserId(Long userId, Pageable pageable) {
        log.info("Fetching cards for user: {} with pagination", userId);
        Page<VirtualCard> cards = virtualCardRepository.findByUserIdAndDeletedAtIsNull(userId, pageable);
        return cards.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public List<VirtualCardResponse> getActiveCardsByUserId(Long userId) {
        log.info("Fetching active cards for user: {}", userId);
        List<VirtualCard> cards = virtualCardRepository.findByUserIdAndStatusAndDeletedAtIsNull(
                userId, CardStatus.AUTHORIZED);
        return cards.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ==================== UPDATE ====================
    @Transactional
    public VirtualCardResponse updateCard(String cardId, UpdateVirtualCardRequest request) {
        log.info("Updating virtual card: {}", cardId);
        VirtualCard card = findCardByIdOrThrow(cardId);
        
        // Update only provided fields
        if (request.getAccountHolderName()!= null) {
            card.setCardHolderName(request.getAccountHolderName().toUpperCase());
        }
        
        if (request.getStatus() != null) {
            updateCardStatus(card, request.getStatus());
        }
        
        if (request.getSpendingLimit() != null) {
            card.setSpendingLimit(request.getSpendingLimit());
        }
        
        if (request.getLimitPeriod() != null) {
            card.setLimitPeriod(request.getLimitPeriod());
            card.setLimitResetDate(calculateLimitResetDate(request.getLimitPeriod()));
        }
        
        if (request.getAllowInternational() != null) {
            card.setAllowInternational(request.getAllowInternational());
        }
        
        if (request.getAllowOnline() != null) {
            card.setAllowOnline(request.getAllowOnline());
        }
        
        if (request.getAllowAtm() != null) {
            card.setAllowAtm(request.getAllowAtm());
        }
        
        if (request.getAllowContactless() != null) {
            card.setAllowContactless(request.getAllowContactless());
        }
        
        if (request.getMerchantName() != null) {
            card.setMerchantName(request.getMerchantName());
        }
        
        if (request.getMerchantId() != null) {
            card.setMerchantId(request.getMerchantId());
        }
        
        if (request.getMerchantCategoryCode() != null) {
            card.setMerchantCategoryCode(request.getMerchantCategoryCode());
        }
        
        if (request.getMerchantCountry() != null) {
            card.setMerchantCountry(request.getMerchantCountry());
        }
        
        if (request.getMerchantCity() != null) {
            card.setMerchantCity(request.getMerchantCity());
        }
        
        VirtualCard updatedCard = virtualCardRepository.save(card);
        log.info("Virtual card updated successfully: {}", cardId);
        
        return mapToResponse(updatedCard);
    }

    @Transactional
    public VirtualCardResponse updateCardStatus(String cardId, UpdateCardStatusRequest request) {
        log.info("Updating card status: {} to {}", cardId, request.getStatus());
        VirtualCard card = findCardByIdOrThrow(cardId);
        
        updateCardStatus(card, request.getStatus());
        
        VirtualCard updatedCard = virtualCardRepository.save(card);
        log.info("Card status updated successfully: {}", cardId);
        
        return mapToResponse(updatedCard);
    }

    private void updateCardStatus(VirtualCard card, CardStatus newStatus) {
        card.setStatus(newStatus);
        
        switch (newStatus) {
            case ACTIVE -> {
                card.setActivatedAt(LocalDateTime.now());
                card.setFrozenAt(null);
                card.setCancelledAt(null);
            }
            case PENDING -> {
                card.setActivatedAt(null);
                card.setFrozenAt(null);
            }
            case FROZEN -> card.setFrozenAt(LocalDateTime.now());
            case SUSPENDED -> card.setFrozenAt(LocalDateTime.now());
            case EXPIRED -> card.setFrozenAt(LocalDateTime.now());
            case AUTHORIZED -> card.setActivatedAt(LocalDateTime.now());
            case APPROVED -> card.setActivatedAt(LocalDateTime.now());
            case REJECTED -> card.setActivatedAt(null);
            case CANCELLED -> {
                card.setCancelledAt(LocalDateTime.now());
                card.setFrozenAt(LocalDateTime.now());
            }
            default -> throw new IllegalArgumentException("Unknown card status: " + newStatus);
        }
    }

    @Transactional
    public VirtualCardResponse updateBalance(String cardId, UpdateBalanceRequest request) {
        log.info("Updating balance for card: {}", cardId);
        VirtualCard card = findCardByIdOrThrow(cardId);
        
        validateCardActive(card);
        
        BigDecimal newBalance;
        if (request.getOperation() == BalanceOperation.ADD) {
            newBalance = card.getBalance().add(request.getAmount());
            log.info("Adding {} to card balance", request.getAmount());
        } else {
            if (!card.hasAvailableBalance(request.getAmount())) {
                throw new InsufficientBalanceException(cardId);
            }
            newBalance = card.getBalance().subtract(request.getAmount());
            log.info("Deducting {} from card balance", request.getAmount());
        }
        
        card.setBalance(newBalance);
        card.setLastUsedAt(LocalDateTime.now());
        
        VirtualCard updatedCard = virtualCardRepository.save(card);
        log.info("Balance updated successfully for card: {}", cardId);
        
        return mapToResponse(updatedCard);
    }

    // ==================== DELETE ====================
    @Transactional
    public void deleteCard(String cardId) {
        log.info("Soft deleting virtual card: {}", cardId);
        VirtualCard card = findCardByIdOrThrow(cardId);
        
        card.setDeletedAt(LocalDateTime.now());
        card.setStatus(CardStatus.CANCELLED);
        card.setCancelledAt(LocalDateTime.now());
        
        virtualCardRepository.save(card);
        log.info("Virtual card soft deleted successfully: {}", cardId);
    }

    @Transactional
    public void permanentlyDeleteCard(String cardId) {
        log.info("Permanently deleting virtual card: {}", cardId);
        VirtualCard card = findCardByIdOrThrow(cardId);
        virtualCardRepository.delete(card);
        log.info("Virtual card permanently deleted: {}", cardId);
    }

    // ==================== CARD OPERATIONS ====================
    @Transactional
    public VirtualCardResponse freezeCard(String cardId) {
        log.info("Freezing card: {}", cardId);
        VirtualCard card = findCardByIdOrThrow(cardId);
        
        if (card.getStatus() == CardStatus.FROZEN) {
            throw new InvalidCardOperationException("Card is already frozen");
        }
        
        if (card.getStatus() == CardStatus.CANCELLED) {
            throw new InvalidCardOperationException("Cannot freeze a cancelled card");
        }
        
        card.setStatus(CardStatus.FROZEN);
        card.setFrozenAt(LocalDateTime.now());
        
        VirtualCard updatedCard = virtualCardRepository.save(card);
        log.info("Card frozen successfully: {}", cardId);
        
        return mapToResponse(updatedCard);
    }

    @Transactional
    public VirtualCardResponse unfreezeCard(String cardId) {
        log.info("Unfreezing card: {}", cardId);
        VirtualCard card = findCardByIdOrThrow(cardId);
        
        if (card.getStatus() != CardStatus.FROZEN) {
            throw new InvalidCardOperationException("Card is not frozen");
        }
        
        card.setStatus(CardStatus.ACTIVE);
        card.setFrozenAt(null);
        
        VirtualCard updatedCard = virtualCardRepository.save(card);
        log.info("Card unfrozen successfully: {}", cardId);
        
        return mapToResponse(updatedCard);
    }

    @Transactional
    public VirtualCardResponse cancelCard(String cardId) {
        log.info("Cancelling card: {}", cardId);
        VirtualCard card = findCardByIdOrThrow(cardId);
        
        if (card.getStatus() == CardStatus.CANCELLED) {
            throw new InvalidCardOperationException("Card is already cancelled");
        }
        
        card.setStatus(CardStatus.CANCELLED);
        card.setCancelledAt(LocalDateTime.now());
        
        VirtualCard updatedCard = virtualCardRepository.save(card);
        log.info("Card cancelled successfully: {}", cardId);
        
        return mapToResponse(updatedCard);
    }

    // ==================== HELPER METHODS ====================
    private VirtualCard findCardByIdOrThrow(String cardId) {
        return virtualCardRepository.findByCardIdAndDeletedAtIsNull(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));
    }

    private void validateUserExists(Long userId) {
        UserDTO user = userServiceClient.findById(userId);
        if(!user.getUsername().isEmpty() && !user.getUsername().isBlank());
    }

    private void validateCardActive(VirtualCard card) {
        if (!card.isActive()) {
            throw new CardNotActiveException(card.getCardId());
        }
        
        if (card.isExpired()) {
            throw new CardExpiredException(card.getCardId());
        }
    }

   
    private String generateCardNumber(pesco.example.virtual_card_service.enums.CardType cardType) {
        // Generate a valid card number using Luhn algorithm
        StringBuilder cardNumber = new StringBuilder();
        switch (cardType) {
            case VISA -> cardNumber.append("4"); 
            case MASTER -> cardNumber.append("5"); 
            default -> cardNumber.append("4");
        }
        
        // Generate 14 random digits
        for (int i = 0; i < 14; i++) {
            cardNumber.append(random.nextInt(10));
        }
        
        // Calculate and append Luhn check digit
        int checkDigit = calculateLuhnCheckDigit(cardNumber.toString());
        cardNumber.append(checkDigit);
        
        return cardNumber.toString();
    }

    private int calculateLuhnCheckDigit(String partialCardNumber) {
        int sum = 0;
        boolean alternate = true;
        
        for (int i = partialCardNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(partialCardNumber.charAt(i));
            
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            
            sum += digit;
            alternate = !alternate;
        }
        
        return (10 - (sum % 10)) % 10;
    }

    private String generateCVV() {
        return String.format("%03d", random.nextInt(1000));
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return cardNumber;
        }
        
        String lastFour = cardNumber.substring(cardNumber.length() - 4);
        return "****-****-****-" + lastFour;
    }

    private LocalDate calculateLimitResetDate(LimitPeriod period) {
        LocalDate now = LocalDate.now();
        
        switch (period) {
            case DAILY:
                return now.plusDays(1);
            case WEEKLY:
                return now.plusWeeks(1);
            case MONTHLY:
                return now.plusMonths(1);
            case TRANSACTION:
            default:
                return null;
        }
    }

    private VirtualCardResponse mapToResponse(VirtualCard card) {
        return VirtualCardResponse.builder()
                .id(card.getId())
                .cardId(card.getCardId())
                .userId(card.getUserId())
                .cardHolderName(card.getCardHolderName())
                .maskedCardNumber(card.getMaskedCardNumber())
                .lastFour(card.getLastFour())
                .expirationMonth(card.getExpirationMonth())
                .expirationYear(card.getExpirationYear())
                .status(card.getStatus())
                .cardType(card.getCardType())
                .cardPlan(card.getCardPlan())
                .currency(card.getCurrency())
                .balance(card.getBalance())
                .spendingLimit(card.getSpendingLimit())
                .limitPeriod(card.getLimitPeriod())
                .currentPeriodSpent(card.getCurrentPeriodSpent())
                .allowInternational(card.getAllowInternational())
                .allowOnline(card.getAllowOnline())
                .allowAtm(card.getAllowAtm())
                .allowContactless(card.getAllowContactless())
                .merchantName(card.getMerchantName())
                .merchantId(card.getMerchantId())
                .merchantCategoryCode(card.getMerchantCategoryCode())
                .merchantCountry(card.getMerchantCountry())
                .merchantCity(card.getMerchantCity())
                .expiresAt(card.getExpiresAt())
                .createdAt(card.getCreatedAt())
                .lastUsedAt(card.getLastUsedAt())
                .build();
    }

    private VirtualCardDetailsResponse mapToDetailsResponse(VirtualCard card) {
        return VirtualCardDetailsResponse.builder()
                .id(card.getId())
                .cardId(card.getCardId())
                .userId(card.getUserId())
                .cardNumber(card.getCardNumber()) 
                .cardHolderName(card.getCardHolderName())
                .expirationMonth(card.getExpirationMonth())
                .expirationYear(card.getExpirationYear())
                .cvv(card.getCvv()) 
                .maskedCardNumber(card.getMaskedCardNumber())
                .lastFour(card.getLastFour())
                .status(card.getStatus())
                .cardType(card.getCardType())
                .cardPlan(card.getCardPlan())
                .currency(card.getCurrency())
                .balance(card.getBalance())
                .spendingLimit(card.getSpendingLimit())
                .limitPeriod(card.getLimitPeriod())
                .currentPeriodSpent(card.getCurrentPeriodSpent())
                .allowInternational(card.getAllowInternational())
                .allowOnline(card.getAllowOnline())
                .allowAtm(card.getAllowAtm())
                .allowContactless(card.getAllowContactless())
                .expiresAt(card.getExpiresAt())
                .createdAt(card.getCreatedAt())
                .lastUsedAt(card.getLastUsedAt())
                .build();
    }
}