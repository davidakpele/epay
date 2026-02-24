package pesco.example.withdraw_service.payloads;

public class CreateWalletRequest {
    private Long userId;

    // Constructor
    public CreateWalletRequest(Long userId) {
        this.userId = userId;
    }

    // Getters and setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
