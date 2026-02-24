package dto

import (
	"history-service/models"
	"time"
)

type HistoryDTO struct {
    ID           string  `json:"id"`
    WalletID     uint64  `json:"walletId"`
    UserID       uint64  `json:"userId"`
    SessionID    string  `json:"sessionId"`
    Amount       float64 `json:"amount"`
    Type         string  `json:"type"`        
    Description  string  `json:"description"`
    Message      string  `json:"message"`
    CurrencyType string  `json:"currencyType"`
    Status       string  `json:"status"`
    IPAddress    string  `json:"ipAddress"`
    Timestamp    string  `json:"timestamp"`
    CreatedOn    string  `json:"createdOn"`
    UpdatedOn    string  `json:"updatedOn"`
}

// Convert History to HistoryDTO
func ToHistoryDTO(history *models.History) *HistoryDTO {
    return &HistoryDTO{
        ID:           history.ID,
        WalletID:     history.WalletID,
        UserID:       history.UserID,
        SessionID:    history.SessionID,
        Amount:       history.Amount,
        Type:         history.Type,         
        Description:  history.Description,
        Message:      history.Message,
        CurrencyType: history.CurrencyType, 
        Status:       history.Status,
        IPAddress:    history.IPAddress,
        Timestamp:    formatTime(history.Timestamp),
        CreatedOn:    formatTime(&history.CreatedOn),
        UpdatedOn:    formatTime(&history.UpdatedOn),
    }
}

// Convert slice of History to slice of HistoryDTO
func ToHistoryDTOs(histories []models.History) []HistoryDTO {
    dtos := make([]HistoryDTO, len(histories))
    for i, history := range histories {
        dtos[i] = *ToHistoryDTO(&history)
    }
    return dtos
}

// Helper function to format time as RFC3339 string
func formatTime(t *time.Time) string {
    if t == nil {
        return ""
    }
    return t.Format(time.RFC3339Nano)
}