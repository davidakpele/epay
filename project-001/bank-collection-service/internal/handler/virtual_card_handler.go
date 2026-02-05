package handler

import (
	"bank-collection-service/internal/domain/model"
	"bank-collection-service/internal/services"
	"errors"
	"strconv"
)

type CardHandler struct {
	cardService *services.CardService
}

func NewCardHandler(cardService *services.CardService) *CardHandler {
	return &CardHandler{cardService: cardService}
}

func (ch *CardHandler) CreateVirtualCard(request *model.CreateVirtualCardRequest) (*model.VirtualCardResponse, error) {

	if request.CardHolderName == "" {
		return nil, errors.New("card holder name is required")
	}

	card, _ := ch.cardService.FindByUserID(uint(request.UserID), request.CardType)
	if card != nil {
		return nil, errors.New("User already have "+request.CardType+" Card with our platform.")
	}

	virtualCard, err := ch.cardService.CreateVirtualCard(request)
	if err != nil {
		return nil, err
	}
	response := &model.VirtualCardResponse{
		Message: "Virtual card created successfully",
		Card:    virtualCard,
		Status: "success",
	}

	return response, nil
}

func (ch *CardHandler) GetVirtualCard(cardID string) (*model.VirtualCardResponse, error) {
	if cardID == "" {
		return nil, errors.New("card ID is required")
	}

	card, err := ch.cardService.GetVirtualCardByID(cardID)
	if err != nil {
		return nil, err
	}

	response := &model.VirtualCardResponse{
		Message: "Virtual card retrieved successfully",
		Card:    card,
	}

	return response, nil
}

func (ch *CardHandler) GetAllUserVirtualCards(userIDStr string) (*model.VirtualCardsListResponse, error) {
	if userIDStr == "" {
		return nil, errors.New("user ID is required")
	}

	userID, err := strconv.ParseUint(userIDStr, 10, 32)
	if err != nil {
		return nil, errors.New("invalid user ID format")
	}

	cards, err := ch.cardService.GetAllVirtualCardsByUserID(uint(userID))
	if err != nil {
		return nil, err
	}

	response := &model.VirtualCardsListResponse{
		Message: "Virtual cards retrieved successfully",
		Cards:   cards,
		Count:   len(cards),
	}

	return response, nil
}
