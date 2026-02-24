package exceptions

import (
	"log"
	"net/http"
)

func (e ErrorResponse) Error() string {
    return e.Message
}

// ErrorResponse represents a structured error response
type ErrorResponse struct {
	Message string `json:"message"`
	Details string `json:"details"`
	Status  int    `json:"status"`
}


// CreateErrorResponse creates an error response object
func CreateErrorResponse(message, details string) ErrorResponse {
	return ErrorResponse{
		Message: message,
		Details: details,
		Status:  http.StatusBadRequest,
	}
}

// LogError logs error messages with context
func LogError(context string, err error) {
	log.Printf("[ERROR] %s: %v\n", context, err)
}

// LogInfo logs informational messages
func LogInfo(context string) {
	log.Printf("[INFO] %s\n", context)
}