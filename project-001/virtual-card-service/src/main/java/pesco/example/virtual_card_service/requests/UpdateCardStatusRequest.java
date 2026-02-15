package pesco.example.virtual_card_service.requests;

import pesco.example.virtual_card_service.enums.CardStatus;

public class UpdateCardStatusRequest {
    private String cardId;
    private CardStatus status;

    public UpdateCardStatusRequest() {
    }

    public UpdateCardStatusRequest(String cardId, CardStatus status) {
        this.cardId = cardId;
        this.status = status;
    }

    public String getCardId() {
        return this.cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public CardStatus getStatus() {
        return this.status;
    }

    public void setStatus(CardStatus status) {
        this.status = status;
    }
    
}
