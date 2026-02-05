package responses

import (
	"net/http"
)

// SuccessResponse represents a structured success response
type SuccessResponse struct {
	Message string      `json:"message"`
	Details string      `json:"details"`
	Data    interface{} `json:"data,omitempty"`
	Status  int         `json:"status"`
}

// CreateSuccessResponse creates a success response object
func CreateSuccessResponse(message, details string, data interface{}) SuccessResponse {
	return SuccessResponse{
		Message: message,
		Details: details,
		Data:    data,
		Status:  http.StatusOK,
	}
}
