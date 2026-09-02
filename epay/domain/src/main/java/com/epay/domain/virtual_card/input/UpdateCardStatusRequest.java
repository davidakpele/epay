package com.epay.domain.virtual_card.input;

import com.epay.domain.virtual_card.enums.CardStatus;

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
