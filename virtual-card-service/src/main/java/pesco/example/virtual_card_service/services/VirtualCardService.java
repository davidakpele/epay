package pesco.example.virtual_card_service.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pesco.example.virtual_card_service.clients.UserServiceClient;
import pesco.example.virtual_card_service.enums.BalanceOperation;
import pesco.example.virtual_card_service.enums.CardStatus;
import pesco.example.virtual_card_service.enums.LimitPeriod;
import pesco.example.virtual_card_service.exceptions.*;
import pesco.example.virtual_card_service.models.CardLimit;
import pesco.example.virtual_card_service.models.VirtualCard;
import pesco.example.virtual_card_service.repository.CardLimitRepository;
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
    private final CardLimitRepository cardLimitRepository;
    private final UserServiceClient userServiceClient;
    
    private static final SecureRandom random = new SecureRandom();

    @Transactional
    public VirtualCardResponse createCard(CreateVirtualCardRequest request) {
        validateUserExists(request.getUserId());
        
        String cardNumber = generateCardNumber(request.getCardType());
        String cvv = generateCVV();
        String cardId = UUID.randomUUID().toString();
        
        LocalDateTime expiresAt = LocalDateTime.now().plusYears(3);
        String expirationMonth = String.format("%02d", expiresAt.getMonthValue());
        String expirationYear = String.valueOf(expiresAt.getYear());
        
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
                .firstFour(cardNumber.substring(0, 4))
                .lastFour(cardNumber.substring(cardNumber.length() - 4))
                .maskedCardNumber(maskCardNumber(cardNumber))
                .hashedCardNumber(formatCardNumberForDisplay(cardNumber))
                .expiresAt(expiresAt)
                .activatedAt(LocalDateTime.now())
                .build();
        
        if (request.getLimitPeriod() != null) {
            card.setLimitResetDate(calculateLimitResetDate(request.getLimitPeriod()));
        }
        
        VirtualCard savedCard = virtualCardRepository.save(card);

        LocalDateTime now = LocalDateTime.now();
        CardLimit cardLimit = CardLimit.builder()
                .cardId(savedCard.getCardId())
                .maxTransactionAmount(new BigDecimal("10000.00"))
                .minTransactionAmount(new BigDecimal("1.00"))
                .dailyLimit(new BigDecimal("5000.00"))
                .dailySpent(BigDecimal.ZERO)
                .dailyResetAt(now.plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0))
                .weeklyLimit(new BigDecimal("20000.00"))
                .weeklySpent(BigDecimal.ZERO)
                .weeklyResetAt(now.plusWeeks(1).with(java.time.DayOfWeek.MONDAY)
                        .withHour(0).withMinute(0).withSecond(0).withNano(0))
                .monthlyLimit(new BigDecimal("50000.00"))
                .monthlySpent(BigDecimal.ZERO)
                .monthlyResetAt(now.plusMonths(1).withDayOfMonth(1)
                        .withHour(0).withMinute(0).withSecond(0).withNano(0))
                .dailyTransactionCountLimit(50)
                .dailyTransactionCount(0)
                .build();
        
        cardLimitRepository.save(cardLimit);
        return mapToResponse(savedCard, cardLimit);
    }

    @Transactional(readOnly = true)
    public VirtualCardResponse getCardById(String cardId) {
        VirtualCard card = findCardByIdOrThrow(cardId);
        CardLimit cardLimit = findCardLimitByCardId(cardId);
        return mapToResponse(card, cardLimit);
    }

    @Transactional(readOnly = true)
    public VirtualCardDetailsResponse getCardDetails(String cardId) {
        VirtualCard card = findCardByIdOrThrow(cardId);
        CardLimit cardLimit = findCardLimitByCardId(cardId);
        return mapToDetailsResponse(card, cardLimit);
    }

    @Transactional(readOnly = true)
    public List<VirtualCardResponse> getCardsByUserId(Long userId) {
        List<VirtualCard> cards = virtualCardRepository.findByUserIdAndDeletedAtIsNull(userId);
        return cards.stream()
                .map(card -> mapToResponse(card, findCardLimitByCardId(card.getCardId())))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<VirtualCardResponse> getCardsByUserId(Long userId, Pageable pageable) {
        Page<VirtualCard> cards = virtualCardRepository.findByUserIdAndDeletedAtIsNull(userId, pageable);
        return cards.map(card -> mapToResponse(card, findCardLimitByCardId(card.getCardId())));
    }

    @Transactional(readOnly = true)
    public List<VirtualCardResponse> getActiveCardsByUserId(Long userId) {
        List<VirtualCard> cards = virtualCardRepository.findByUserIdAndStatusAndDeletedAtIsNull(userId, CardStatus.AUTHORIZED);
        return cards.stream()
                .map(card -> mapToResponse(card, findCardLimitByCardId(card.getCardId())))
                .collect(Collectors.toList());
    }

    @Transactional
    public VirtualCardResponse updateCard(String cardId, UpdateVirtualCardRequest request) {
        VirtualCard card = findCardByIdOrThrow(cardId);
        
        if (request.getAccountHolderName() != null) {
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
        return mapToResponse(updatedCard, findCardLimitByCardId(cardId));
    }

    @Transactional
    public VirtualCardResponse updateCardStatus(String cardId, UpdateCardStatusRequest request) {
        VirtualCard card = findCardByIdOrThrow(cardId);
        updateCardStatus(card, request.getStatus());
        VirtualCard updatedCard = virtualCardRepository.save(card);
        return mapToResponse(updatedCard, findCardLimitByCardId(cardId));
    }

    @Transactional
    public VirtualCardResponse updateBalance(String cardId, UpdateBalanceRequest request) {
        VirtualCard card = findCardByIdOrThrow(cardId);
        validateCardActive(card);
        
        BigDecimal newBalance;
        if (request.getOperation() == BalanceOperation.ADD) {
            newBalance = card.getBalance().add(request.getAmount());
        } else {
            if (!card.hasAvailableBalance(request.getAmount())) {
                throw new InsufficientBalanceException(cardId);
            }
            newBalance = card.getBalance().subtract(request.getAmount());
        }
        
        card.setBalance(newBalance);
        card.setLastUsedAt(LocalDateTime.now());
        
        VirtualCard updatedCard = virtualCardRepository.save(card);
        return mapToResponse(updatedCard, findCardLimitByCardId(cardId));
    }

    @Transactional
    public void deleteCard(String cardId) {
        VirtualCard card = findCardByIdOrThrow(cardId);
        if(card !=null){
           cardLimitRepository.deleteByCardId(cardId);
            virtualCardRepository.deleteByCardId(cardId); 
        }
    }

    @Transactional
    public void deleteAllCardsByUserId(Long userId) {
        List<VirtualCard> cards = virtualCardRepository.findByUserId(userId);
        cards.forEach(card -> cardLimitRepository.deleteByCardId(card.getCardId()));
        virtualCardRepository.deleteAllByUserId(userId);
    }

    @Transactional
    public VirtualCardResponse freezeCard(String cardId) {
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
        return mapToResponse(updatedCard, findCardLimitByCardId(cardId));
    }

    @Transactional
    public VirtualCardResponse unfreezeCard(String cardId) {
        VirtualCard card = findCardByIdOrThrow(cardId);
        
        if (card.getStatus() != CardStatus.FROZEN) {
            throw new InvalidCardOperationException("Card is not frozen");
        }
        
        card.setStatus(CardStatus.ACTIVE);
        card.setFrozenAt(null);
        
        VirtualCard updatedCard = virtualCardRepository.save(card);
        return mapToResponse(updatedCard, findCardLimitByCardId(cardId));
    }

    @Transactional
    public VirtualCardResponse cancelCard(String cardId) {
        VirtualCard card = findCardByIdOrThrow(cardId);
        
        if (card.getStatus() == CardStatus.CANCELLED) {
            throw new InvalidCardOperationException("Card is already cancelled");
        }
        
        card.setStatus(CardStatus.CANCELLED);
        card.setCancelledAt(LocalDateTime.now());
        
        VirtualCard updatedCard = virtualCardRepository.save(card);
        return mapToResponse(updatedCard, findCardLimitByCardId(cardId));
    }

    private VirtualCard findCardByIdOrThrow(String cardId) {
        return virtualCardRepository.findByCardIdAndDeletedAtIsNull(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));
    }

    private CardLimit findCardLimitByCardId(String cardId) {
        return cardLimitRepository.findByCardId(cardId)
                .orElseThrow(() -> new RuntimeException("Card limit not found for card: " + cardId));
    }

    private void validateUserExists(Long userId) {
        userServiceClient.findById(userId);
    }

    private void validateCardActive(VirtualCard card) {
        if (!card.isActive()) {
            throw new CardNotActiveException(card.getCardId());
        }
        if (card.isExpired()) {
            throw new CardExpiredException(card.getCardId());
        }
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
        }
    }

    private String generateCardNumber(pesco.example.virtual_card_service.enums.CardType cardType) {
        StringBuilder cardNumber = new StringBuilder();
        cardNumber.append(cardType == pesco.example.virtual_card_service.enums.CardType.VISA ? "4" : "5");
        
        for (int i = 0; i < 14; i++) {
            cardNumber.append(random.nextInt(10));
        }
        
        cardNumber.append(calculateLuhnCheckDigit(cardNumber.toString()));
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

    private String formatCardNumberForDisplay(String cardNumber) {
        if (cardNumber == null || cardNumber.length() != 16) {
            return cardNumber;
        }
        
        return cardNumber.substring(0, 4) + "-" + 
               cardNumber.substring(4, 8) + "-" + 
               cardNumber.substring(8, 12) + "-" + 
               cardNumber.substring(12, 16);
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return cardNumber;
        }
        
        return "****-****-****-" + cardNumber.substring(cardNumber.length() - 4);
    }

    private LocalDate calculateLimitResetDate(LimitPeriod period) {
        LocalDate now = LocalDate.now();
        
        return switch (period) {
            case DAILY -> now.plusDays(1);
            case WEEKLY -> now.plusWeeks(1);
            case MONTHLY -> now.plusMonths(1);
            case TRANSACTION -> null;
        };
    }

    private VirtualCardResponse mapToResponse(VirtualCard card, CardLimit cardLimit) {
        return VirtualCardResponse.builder()
                .id(card.getId())
                .cardId(card.getCardId())
                .userId(card.getUserId())
                .cardHolderName(card.getCardHolderName())
                .maskedCardNumber(card.getMaskedCardNumber())
                .firstFour(card.getFirstFour())
                .lastFour(card.getLastFour())
                .hashedCardNumber(card.getHashedCardNumber())
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
                .cardLimit(cardLimit)
                .build();
    }

    private VirtualCardDetailsResponse mapToDetailsResponse(VirtualCard card, CardLimit cardLimit) {
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
                .firstFour(card.getFirstFour())
                .lastFour(card.getLastFour())
                .hashedCardNumber(card.getHashedCardNumber())
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
                .cardLimit(cardLimit)
                .build();
    }

    @Transactional(readOnly = true)
    public long getTotalCards() {
        return virtualCardRepository.countByDeletedAtIsNull();
    }

    
}