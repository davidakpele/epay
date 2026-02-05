package controller

import (
	"net/http"
	"bank-collection-service/internal/domain/model"
	"bank-collection-service/internal/handler"
	"github.com/gin-gonic/gin"
)

type CardController struct {
	cardHandler *handler.CardHandler
}

func NewCardController(cardHandler *handler.CardHandler) *CardController {
	return &CardController{cardHandler: cardHandler}
}

func (cc *CardController) CreateVirtualCard(c *gin.Context) {
	var request model.CreateVirtualCardRequest
	
	if err := c.ShouldBindJSON(&request); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	response, err := cc.cardHandler.CreateVirtualCard(&request)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusCreated, response)
}

func (cc *CardController) GetVirtualCardByID(c *gin.Context) {
	cardID := c.Param("cardId")
	if cardID == "" {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Card ID is required"})
		return
	}

	response, err := cc.cardHandler.GetVirtualCard(cardID)
	if err != nil {
		c.JSON(http.StatusNotFound, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, response)
}

func (cc *CardController) GetAllUserVirtualCards(c *gin.Context) {
	userID := c.Param("userId")
	if userID == "" {
		c.JSON(http.StatusBadRequest, gin.H{"error": "User ID is required"})
		return
	}

	response, err := cc.cardHandler.GetAllUserVirtualCards(userID)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, response)
}