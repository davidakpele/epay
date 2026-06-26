package pesco.example.virtual_card_service.enums;

public enum CardStatus {
    ACTIVE,       // Card is active and can be used
    PENDING,      // Card created but not activated
    FROZEN,       // Temporarily blocked by user
    SUSPENDED,    // Blocked by system (fraud, limits, etc)
    EXPIRED,      // Past expiration date
    AUTHORIZED,    // Funds held
    APPROVED,       // Approved for use
    REJECTED,
    CANCELLED
}