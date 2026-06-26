package service

import (
	"log"
	"revenue-service/internal/models"
	"revenue-service/internal/repository"

	"github.com/shopspring/decimal"
)

type RevenueService interface {
    GetOrCreateRevenue() (*models.Revenue, error)
    ProcessTransaction(transactionType models.TransactionType, amount decimal.Decimal, currency models.CurrencyTypeStruct, password *string) error
    GetRevenue() (*models.Revenue, error)
    UpdateRevenuePassword(newPassword string) error
    ValidateWithdrawalPassword(password string) (bool, error)
}

type revenueService struct {
    revenueRepo   repository.RevenueRepository
    balanceRepo   repository.CurrencyBalanceRepository
    transactionRepo repository.TransactionRepository
}

func NewRevenueService(
    revenueRepo repository.RevenueRepository,
    balanceRepo repository.CurrencyBalanceRepository,
    transactionRepo repository.TransactionRepository,
) RevenueService {
    return &revenueService{
        revenueRepo:   revenueRepo,
        balanceRepo:   balanceRepo,
        transactionRepo: transactionRepo,
    }
}

func (s *revenueService) GetOrCreateRevenue() (*models.Revenue, error) {
    return s.revenueRepo.GetOrCreate()
}

func (s *revenueService) GetRevenue() (*models.Revenue, error) {
    return s.revenueRepo.GetOrCreate()
}

func (s *revenueService) UpdateRevenuePassword(newPassword string) error {
    revenue, err := s.revenueRepo.GetOrCreate()
    if err != nil {
        return err
    }
    
    revenue.Password = newPassword
    return s.revenueRepo.Update(revenue)
}

func (s *revenueService) ValidateWithdrawalPassword(password string) (bool, error) {
    revenue, err := s.revenueRepo.GetOrCreate()
    if err != nil {
        return false, err
    }
    
    if revenue.Password == "" {
        return true, nil
    }
    
    return revenue.Password == password, nil
}

func (s *revenueService) ProcessTransaction(transactionType models.TransactionType, amount decimal.Decimal, currency models.CurrencyTypeStruct, password *string) error {
    revenue, err := s.revenueRepo.GetOrCreate()
    if err != nil {
        return err
    }

    log.Printf("Processing transaction: Type=%s, Amount=%s, Currency=%s, RevenueID=%d", 
        transactionType, amount.String(), currency, revenue.ID)
    if transactionType == models.WITHDRAW || transactionType == models.DEBITED {
        if revenue.Password != "" {
            if password == nil {
                return &PasswordRequiredError{}
            }
            isValid, err := s.ValidateWithdrawalPassword(*password)
            if err != nil {
                return err
            }
            if !isValid {
                return &InvalidPasswordError{}
            }
        }
    }

    balance, err := s.balanceRepo.FindByRevenueIDAndCurrency(revenue.ID, string(currency))
    if err != nil {
        log.Printf("Wallet not found for currency %s, creating new one", currency)
        
        initialBalance := decimal.NewFromFloat(0)
        if isRevenueTransaction(transactionType) && amount.GreaterThan(decimal.NewFromFloat(0)) {
            initialBalance = amount
            log.Printf("Setting initial balance to transaction amount: %s", initialBalance.String())
        }
        
        balance = &models.CurrencyBalance{
            RevenueID:      revenue.ID,
            CurrencyCode:   string(currency),
            CurrencySymbol: getCurrencySymbol(currency),
            Balance:        initialBalance, 
        }
        if err := s.balanceRepo.Create(balance); err != nil {
            log.Printf("Error creating wallet: %v", err)
            return err
        }
        log.Printf("Created new wallet: Currency=%s, Balance=%s", balance.CurrencyCode, balance.Balance.String())
        if initialBalance.GreaterThan(decimal.NewFromFloat(0)) {
            return s.createTransactionRecord(revenue.ID, transactionType, amount, currency)
        }
    } else {
        log.Printf("Found existing wallet: Currency=%s, Current Balance=%s", balance.CurrencyCode, balance.Balance.String())
    }

    oldBalance := balance.Balance

    switch transactionType {
    case models.DEPOSIT, models.CREDITED, models.MAINTENANCE_FEE, models.TRANSACTION_FEE, models.SERVICE_FEE:
        balance.Balance = balance.Balance.Add(amount)
        log.Printf("Revenue transaction: %s + %s = %s", oldBalance.String(), amount.String(), balance.Balance.String())
        
    case models.WITHDRAW, models.DEBITED:
        if balance.Balance.LessThan(amount) {
            log.Printf("Insufficient balance: %s < %s", balance.Balance.String(), amount.String())
            return &InsufficientBalanceError{}
        }
        balance.Balance = balance.Balance.Sub(amount)
        log.Printf("Withdrawal: %s - %s = %s", oldBalance.String(), amount.String(), balance.Balance.String())
        
    case models.TRANSFER:
        if balance.Balance.LessThan(amount) {
            return &InsufficientBalanceError{}
        }
        balance.Balance = balance.Balance.Sub(amount)
        
    default:
        log.Printf("Unknown transaction type: %s, treating as revenue transaction", transactionType)
        balance.Balance = balance.Balance.Add(amount)
    }

    if err := s.balanceRepo.Update(balance); err != nil {
        log.Printf("Error updating wallet: %v", err)
        return err
    }

    log.Printf("Wallet updated successfully: New Balance=%s", balance.Balance.String())
    return s.createTransactionRecord(revenue.ID, transactionType, amount, currency)
}

func isRevenueTransaction(transactionType models.TransactionType) bool {
    switch transactionType {
    case models.DEPOSIT, models.CREDITED, models.MAINTENANCE_FEE, models.TRANSACTION_FEE, models.SERVICE_FEE:
        return true
    default:
        return false
    }
}

func getCurrencySymbol(currency models.CurrencyTypeStruct) string {
    symbols := map[models.CurrencyTypeStruct]string{
        models.USD: "$",
        models.EUR: "€",
        models.NGN: "₦",
        models.GBP: "£",
        models.JPY: "¥",
        models.AUD: "A$",
        models.CAD: "C$",
        models.CHF: "CHF",
        models.CNY: "¥",
        models.INR: "₹",
    }
    return symbols[currency]
}

func (s *revenueService) createTransactionRecord(revenueID uint, transactionType models.TransactionType, amount decimal.Decimal, currency models.CurrencyTypeStruct) error {
    transaction := &models.RevenueTransaction{
        RevenueID:       revenueID,
        TransactionType: transactionType,
        Amount:          amount,
        Currency:        currency,
    }

    if err := s.transactionRepo.Create(transaction); err != nil {
        log.Printf("Error creating transaction record: %v", err)
        return err
    }

    log.Printf("Transaction record created successfully")
    return nil
}
