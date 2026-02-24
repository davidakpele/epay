package pesco.deposit_service.payloads;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentRequest {
    private String email;
    private int amount;

    public PaymentRequest() {
    }

    public PaymentRequest(String email, int amount) {
        this.email = email;
        this.amount = amount;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getAmount() {
        return this.amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }



}