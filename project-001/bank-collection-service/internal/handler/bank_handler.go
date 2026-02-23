package handler

import (
	"bank-collection-service/exceptions"
	"bank-collection-service/internal/domain/model"
	"bank-collection-service/internal/services"
	"bank-collection-service/payloads"
	"bank-collection-service/responses"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"os"

	"github.com/gin-gonic/gin"
)

type BankHandler struct {
	service         services.BankService
	paystackBaseURL string
	paystackAPIKey  string
}

func NewBankHandler(service services.BankService) *BankHandler {
	return &BankHandler{
		service:         service,
		paystackBaseURL: os.Getenv("PAYSTACK_BASE_URL"),
		paystackAPIKey:  os.Getenv("PAYSTACK_API_KEY"),
	}
}

func (h *BankHandler) CreateBank(c *gin.Context, bank *model.UserBankList) {
	if bank.BankCode == "" {
		c.JSON(http.StatusBadRequest, exceptions.ErrorResponse{
			Message: "Validation Error",
			Details: "Bank code is required",
		})
		return
	}
	if bank.BankName == "" {
		c.JSON(http.StatusBadRequest, exceptions.ErrorResponse{
			Message: "Validation Error",
			Details: "Bank name is required",
		})
		return
	}
	if bank.AccountHolderName == "" {
		c.JSON(http.StatusBadRequest, exceptions.ErrorResponse{
			Message: "Validation Error",
			Details: "Account holder name is required",
		})
		return
	}
	if bank.AccountNumber == "" {
		c.JSON(http.StatusBadRequest, exceptions.ErrorResponse{
			Message: "Validation Error",
			Details: "Account number is required",
		})
		return
	}

	exists, err := h.service.FindByAccountNumberAndBankNameForUser(bank.AccountNumber, bank.BankName)
	if err != nil {
		c.JSON(http.StatusInternalServerError, exceptions.ErrorResponse{
			Message: "Failed to check bank record",
			Details: err.Error(),
		})
		return
	}
	if exists {
		c.JSON(http.StatusBadRequest, exceptions.ErrorResponse{
			Message: "This bank details is already registered to a user in this platform",
			Details: "This bank details is already registered to a user in this platform",
			Status:  400,
		})
		return
	}

	if err := h.service.CreateBank(bank); err != nil {
		c.JSON(http.StatusInternalServerError, exceptions.ErrorResponse{
			Message: "Failed to create bank",
			Details: err.Error(),
		})
		return
	}

	c.JSON(http.StatusCreated, responses.SuccessResponse{
		Message: "Bank created successfully",
		Data:    nil,
		Status:  201,
	})
}

func (h *BankHandler) GetBankById(c *gin.Context, id uint) (*model.UserBankList, error) {
	if id == 0 {
		return nil, fmt.Errorf("invalid bank ID")
	}
	bank, err := h.service.FindById(id)
	if err != nil {
		return nil, fmt.Errorf("bank not found")
	}
	return bank, nil
}

func (h *BankHandler) GetBankByAccountNumber(c *gin.Context, accountNumber string) (*model.UserBankList, error) {
	bank, err := h.service.FindByAccountNumber(accountNumber)
	if err != nil {
		return nil, fmt.Errorf("bank with given account number not found")
	}
	return bank, nil
}

func (h *BankHandler) FetchAllBanksHandler(c *gin.Context) {
	banks, err := h.FetchAllBanks()
	if err != nil {
		c.JSON(http.StatusInternalServerError, exceptions.ErrorResponse{
			Message: "Error fetching bank list",
			Details: err.Error(),
		})
		return
	}

	c.JSON(http.StatusOK, responses.SuccessResponse{
		Message: "Success",
		Data:    banks,
	})
}

func (h *BankHandler) GetBanksByUserId(c *gin.Context, userId uint) ([]*model.UserBankList, error) {
	banks, err := h.service.FindByUserId(userId)
	if err != nil {
		return nil, err
	}

	if len(banks) == 0 {
		return []*model.UserBankList{}, nil
	}

	bankPointers := make([]*model.UserBankList, 0, len(banks))
	for i := range banks {
		bankPointers = append(bankPointers, &banks[i])
	}
	return bankPointers, nil
}

func (h *BankHandler) DeleteBanksByIds(c *gin.Context, ids []uint) error {
	if len(ids) == 0 {
		return fmt.Errorf("provide a valid list of IDs for deletion")
	}

	if err := h.service.DeleteByIds(ids); err != nil {
		return fmt.Errorf("failed to delete banks: %v", err)
	}
	return nil
}

func (h *BankHandler) FetchAllBanks() ([]payloads.PayStackBankList, error) {
	url := h.paystackBaseURL + "/bank"

	req, err := http.NewRequest("GET", url, nil)
	if err != nil {
		return nil, fmt.Errorf("failed to create request: %v", err)
	}

	req.Header.Set("Authorization", "Bearer "+h.paystackAPIKey)
	req.Header.Set("Content-Type", "application/json")

	client := &http.Client{}
	resp, err := client.Do(req)
	if err != nil {
		return nil, fmt.Errorf("failed to make request: %v", err)
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, fmt.Errorf("failed to read response: %v", err)
	}

	var paystackResponse payloads.PaystackBankResponse
	if err := json.Unmarshal(body, &paystackResponse); err != nil {
		return nil, fmt.Errorf("failed to parse response: %v", err)
	}

	if !paystackResponse.Status {
		return nil, fmt.Errorf("paystack API error: %s", paystackResponse.Message)
	}

	return paystackResponse.Data, nil
}

func (h *BankHandler) VerifyInternal(accountNumber string, bankCode string) (*payloads.PaystackAccountResponse, error) {
	accountData, err := h.service.GetAccountData(accountNumber, bankCode)
	if err != nil {
		return nil, fmt.Errorf("error checking internal bank details: %v", err)
	}

	if accountData != nil {
		response := &payloads.PaystackAccountResponse{
			Status:  true,
			Message: "Account found in internal records",
			Data: payloads.PaystackAccountData{
				AccountNumber: accountData.AccountNumber,
				AccountName:   accountData.AccountHolderName,
				BankID:        int(accountData.ID),
			},
		}
		return response, nil
	}
	return nil, nil
}

func (h *BankHandler) VerifyExternal(accountNumber string, bankCode string) (*payloads.PaystackAccountData, error) {
	url := fmt.Sprintf("%s/bank/resolve?account_number=%s&bank_code=%s", h.paystackBaseURL, accountNumber, bankCode)

	req, err := http.NewRequest("GET", url, nil)
	if err != nil {
		return nil, fmt.Errorf("failed to create request: %v", err)
	}

	req.Header.Set("Authorization", "Bearer "+h.paystackAPIKey)
	req.Header.Set("Content-Type", "application/json")

	client := &http.Client{}
	resp, err := client.Do(req)
	if err != nil {
		return nil, fmt.Errorf("failed to make request: %v", err)
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, fmt.Errorf("failed to read response: %v", err)
	}

	var paystackResponse payloads.PaystackAccountResponse
	if err := json.Unmarshal(body, &paystackResponse); err != nil {
		return nil, fmt.Errorf("failed to parse response: %v", err)
	}

	if !paystackResponse.Status {
		return nil, fmt.Errorf("paystack API error: %s", paystackResponse.Message)
	}

	return &paystackResponse.Data, nil
}