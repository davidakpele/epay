package services

import (
	"bank-collection-service/internal/domain/model"
	"bank-collection-service/internal/repository"
	"errors"
	"math/rand"
	"strconv"
	"time"
)

type CardService struct {
	cardRepo *repository.VirtualCardRepository
}

func NewCardService(cardRepo *repository.VirtualCardRepository) *CardService {
	return &CardService{
		cardRepo: cardRepo,
	}
}


func (s *CardService) generateCardNumber(cardType string) (string, error) {
	var prefix string
	
	switch cardType {
	case "mastercard":
		prefixes := []string{"51", "52", "53", "54", "55"}
		prefix = prefixes[rand.Intn(len(prefixes))]
	case "visa":
		prefix = "4"
	default:
		prefix = "4"
	}

	for {
		base := prefix
		for i := 0; i < 12; i++ {
			base += strconv.Itoa(rand.Intn(10))
		}

		cardNumber := base + s.calculateLuhnCheckDigit(base)
		
		exists, err := s.cardRepo.CardNumberExists(cardNumber)
		if err != nil {
			return "", err
		}
		if !exists {
			return cardNumber, nil
		}
	}
}

func (s *CardService) calculateLuhnCheckDigit(number string) string {
	var sum int
	alternate := false

	for i := len(number) - 1; i >= 0; i-- {
		digit, _ := strconv.Atoi(string(number[i]))
		
		if alternate {
			digit *= 2
			if digit > 9 {
				digit = (digit % 10) + 1
			}
		}
		
		sum += digit
		alternate = !alternate
	}

	checkDigit := (10 - (sum % 10)) % 10
	return strconv.Itoa(checkDigit)
}

func (s *CardService) generateCVV() string {
	cvv := ""
	for i := 0; i < 3; i++ {
		cvv += strconv.Itoa(rand.Intn(10))
	}
	return cvv
}

func (s *CardService) generateExpiryDate() (int, int) {
	now := time.Now()
	expiry := now.AddDate(3, 0, 0)
	return int(expiry.Month()), expiry.Year()
}

func (s *CardService) CreateVirtualCard(request *model.CreateVirtualCardRequest) (*model.VirtualCard, error) {
	cardNumber, err := s.generateCardNumber(request.CardType)
	if err != nil {
		return nil, err
	}

	cvv := s.generateCVV()
	expiryMonth, expiryYear := s.generateExpiryDate()
	spendingLimit := s.determineSpendingLimit(request.Currency, request.UserID)

	virtualCard := &model.VirtualCard{
		CardID:             request.ID,
		UserID:         request.UserID,
		UserWalletID:   request.UserWalletID,
		CardNumber:     cardNumber,
		CVV:            cvv,
		ExpiryMonth:    expiryMonth,
		ExpiryYear:     expiryYear,
		CardHolderName: request.CardHolderName,
		CardType:       request.CardType,
		CardTheme:      request.CardTheme,
		Status:         "active",
		SpendingLimit:  spendingLimit,
		CurrentBalance: 0,
		Currency:       request.Currency,
		BillingAddress: request.BillingAddress,
		CreatedAt:      time.Now(),
		UpdatedAt:      time.Now(),
	}

	err = s.cardRepo.Create(virtualCard)
	if err != nil {
		return nil, err
	}

	return virtualCard, nil
}


func (s *CardService) determineSpendingLimit(currency string, userID uint) float64 {
    // Get user tier/verification status
    userTier := s.getUserTier(userID)
    
    // Define spending limits based on currency and user tier
    limits := map[string]map[string]float64{
        "USD": {
            "basic":    1000.00,
            "verified": 5000.00,
            "premium":  25000.00,
        },
        "EUR": {
            "basic":    900.00,
            "verified": 4500.00,
            "premium":  22000.00,
        },
        "GBP": {
            "basic":    800.00,
            "verified": 4000.00,
            "premium":  20000.00,
        },
        "NGN": {
            "basic":    500000.00,
            "verified": 2500000.00,
            "premium":  10000000.00,
        },
    }
    
    // Default limits if currency not found
    currencyLimits, exists := limits[currency]
    if !exists {
        currencyLimits = limits["USD"] // Default to USD limits
    }
    
    userLimit, exists := currencyLimits[userTier]
    if !exists {
        userLimit = currencyLimits["basic"] // Default to basic tier
    }
    
    return userLimit
}


func (s *CardService) getUserTier(userID uint) string {
    // Mock implementation - replace with actual logic
    // if s.isUserVerified(userID) {
    //     return "verified"
    // } else if s.isPremiumUser(userID) {
    //     return "premium"
    // }
    
    return "basic" 
}

func (s *CardService) GetVirtualCardByID(cardID string) (*model.VirtualCard, error) {
	if cardID == "" {
		return nil, errors.New("card ID cannot be empty")
	}

	card, err := s.cardRepo.FindByCardID(cardID)
	if err != nil {
		return nil, err
	}

	return card, nil
}

func (s *CardService) GetAllVirtualCardsByUserID(userID uint) ([]model.VirtualCard, error) {
	if userID == 0 {
		return nil, errors.New("user ID cannot be empty")
	}

	cards, err := s.cardRepo.FindAllByUserID(userID)
	if err != nil {
		return nil, err
	}

	return cards, nil
}

func (s *CardService) FindByUserID(userID uint, cardType string) (*model.VirtualCard, error) {
	if cardType == "" {
		return nil, errors.New("Card Type cannot be empty")
	}

	card, err := s.cardRepo.FindByUserID(userID, cardType)
	if err != nil {
		return nil, err
	}

	return card, nil
}