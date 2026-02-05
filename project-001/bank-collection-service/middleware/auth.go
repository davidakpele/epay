package middleware

import (
	"encoding/base64"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"strings"
	"sync"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/golang-jwt/jwt/v4"
)

type ErrorResponse struct {
	Code   string `json:"code"`
	Detail string `json:"detail"`
	Title  string `json:"title"`
	Status int    `json:"status"`
}

type UserDTO struct {
	ID        int      `json:"id"`
	Email     string   `json:"email"`
	Username  string   `json:"username"`
	CreatedOn string   `json:"createdOn"`
	UpdatedOn *string  `json:"updatedOn"`
	Enabled   bool     `json:"enabled"`
	Records   []Record `json:"records"`
}

type Record struct {
	ID           int    `json:"id"`
	FirstName    string `json:"firstName"`
	LastName     string `json:"lastName"`
	Gender       string `json:"gender"`
	Locked       bool   `json:"locked"`
	LockedAt     string `json:"lockedAt"`
	ReferralCode string `json:"referralCode"`
	ReferralLink string `json:"referralLink"`
	TransferPin  bool   `json:"_transfer_pin"`
	Blocked      bool   `json:"blocked"`
}

// Global mutex map to track user locks
var userMutexMap = sync.Map{}

func AuthenticationMiddleware(base64Secret string) gin.HandlerFunc {
	return func(c *gin.Context) {
		authHeader := c.GetHeader("Authorization")
		if authHeader == "" {
			errorResponse := ErrorResponse{
				Code:   "Unauthorized Access",
				Detail: "FORBIDDEN ACCESS",
				Title:  "Authentication Error",
				Status: http.StatusUnauthorized,
			}
			c.JSON(http.StatusUnauthorized, gin.H{"error": errorResponse})
			c.Abort()
			return
		}

		decodedKey, err := base64.StdEncoding.DecodeString(base64Secret)
		if err != nil {
			errorResponse := ErrorResponse{
				Code:   "invalid_secret_key",
				Detail: "Failed to decode secret key",
				Title:  "Authentication Error",
				Status: http.StatusUnauthorized,
			}
			c.JSON(http.StatusUnauthorized, gin.H{"error": errorResponse})
			c.Abort()
			return
		}

		tokenString := strings.Split(authHeader, " ")[1]
		token, err := jwt.Parse(tokenString, func(token *jwt.Token) (interface{}, error) {
			if _, ok := token.Method.(*jwt.SigningMethodHMAC); !ok {
				return nil, fmt.Errorf("unexpected signing method: %v", token.Header["alg"])
			}
			return decodedKey, nil
		})

		if err != nil {
			errorResponse := ErrorResponse{
				Code:   "invalid_token",
				Detail: fmt.Sprintf("Token error: %v", err),
				Title:  "Authentication Error",
				Status: http.StatusUnauthorized,
			}
			c.JSON(http.StatusUnauthorized, gin.H{"error": errorResponse})
			c.Abort()
			return
		}

		if !token.Valid {
			errorResponse := ErrorResponse{
				Code:   "invalid_token",
				Detail: "Invalid token",
				Title:  "Authentication Error",
				Status: http.StatusUnauthorized,
			}
			c.JSON(http.StatusUnauthorized, gin.H{"error": errorResponse})
			c.Abort()
			return
		}

		if claims, ok := token.Claims.(jwt.MapClaims); ok && token.Valid {
			username, ok := claims["sub"].(string)
			if !ok {
				errorResponse := ErrorResponse{
					Code:   "invalid_token_claims",
					Detail: "Username not found in token claims",
					Title:  "Authentication Error",
					Status: http.StatusUnauthorized,
				}
				c.JSON(http.StatusUnauthorized, gin.H{"error": errorResponse})
				c.Abort()
				return
			}

			// Check if there's already a lock for this user
			mutexInterface, _ := userMutexMap.LoadOrStore(username, &sync.Mutex{})
			mutex := mutexInterface.(*sync.Mutex)

			// Lock the user's mutex to prevent concurrent access
			mutex.Lock()
			defer mutex.Unlock()

			// Start async goroutine to fetch user data
			var user UserDTO
			var wg sync.WaitGroup
			wg.Add(1)

			go func() {
				defer wg.Done()

				springBootURL := fmt.Sprintf(
					"http://authentication-service:8187/user/username/%s",
					username,
				)

				// Create HTTP client
				client := &http.Client{
					Timeout: 10 * time.Second,
				}

				// Create request
				req, err := http.NewRequest(http.MethodGet, springBootURL, nil)
				if err != nil {
					c.JSON(http.StatusInternalServerError, gin.H{
						"error": ErrorResponse{
							Code:   "request_creation_error",
							Title:  "Authentication Error",
							Detail: err.Error(),
							Status: http.StatusInternalServerError,
						},
					})
					return
				}

				// Attach Bearer token
				req.Header.Set("Authorization", "Bearer "+tokenString)
				req.Header.Set("Accept", "application/json")

				// Execute request
				resp, err := client.Do(req)
				if err != nil {
					c.JSON(http.StatusInternalServerError, gin.H{
						"error": ErrorResponse{
							Code:   "network_error",
							Title:  "Authentication Error",
							Detail: err.Error(),
							Status: http.StatusInternalServerError,
						},
					})
					return
				}
				defer resp.Body.Close()

				body, err := io.ReadAll(resp.Body)
				if err != nil {
					c.JSON(http.StatusInternalServerError, gin.H{
						"error": ErrorResponse{
							Code:   "read_error",
							Title:  "Authentication Error",
							Detail: err.Error(),
							Status: http.StatusInternalServerError,
						},
					})
					return
				}

				if resp.StatusCode != http.StatusOK {
					c.JSON(resp.StatusCode, gin.H{
						"error": ErrorResponse{
							Code:   "unexpected_status",
							Title:  "Authentication Error",
							Detail: string(body),
							Status: resp.StatusCode,
						},
					})
					return
				}

				if err := json.Unmarshal(body, &user); err != nil {
					c.JSON(http.StatusInternalServerError, gin.H{
						"error": ErrorResponse{
							Code:   "unmarshal_error",
							Title:  "Authentication Error",
							Detail: err.Error(),
							Status: http.StatusInternalServerError,
						},
					})
					return
				}
			}()
			// Wait for the goroutine to finish before continuing
			wg.Wait()

			if user.Username != username {
				errorResponse := ErrorResponse{
					Code:   "user_not_found",
					Detail: "User not found in Authentication Service",
					Title:  "Authentication Error",
					Status: http.StatusUnauthorized,
				}
				c.JSON(http.StatusUnauthorized, gin.H{"error": errorResponse})
				c.Abort()
				return
			}

			c.Set("user", user)
		} else {
			errorResponse := ErrorResponse{
				Code:   "invalid_token_claims",
				Detail: "Invalid token claims",
				Title:  "Authentication Error",
				Status: http.StatusUnauthorized,
			}
			c.JSON(http.StatusUnauthorized, gin.H{"error": errorResponse})
			c.Abort()
			return
		}
		c.Next()
	}
}