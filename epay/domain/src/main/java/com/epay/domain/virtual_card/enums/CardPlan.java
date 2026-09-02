package com.epay.domain.virtual_card.enums;

public enum CardPlan {
    SINGLE_USE,      // One-time use card
    MULTI_USE,       // Reusable card
    SUBSCRIPTION,    // For recurring payments
    MERCHANT_LOCKED  // Locked to specific merchant
}