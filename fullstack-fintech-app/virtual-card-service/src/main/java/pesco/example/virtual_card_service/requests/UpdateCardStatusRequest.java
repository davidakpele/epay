package pesco.example.virtual_card_service.requests;

import pesco.example.virtual_card_service.enums.CardStatus;

public class UpdateCardStatusRequest {
    private CardStatus status;

    public UpdateCardStatusRequest() {
    }

    public UpdateCardStatusRequest(CardStatus status) {
        this.status = status;
    }

    public CardStatus getStatus() {
        return this.status;
    }

    public void setStatus(CardStatus status) {
        this.status = status;
    }

    
}
