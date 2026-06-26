package com.example.admin_api_service.enums;

 
public enum KycTier {
    TIER_0,   // No KYC — limited access
    TIER_1,   // Basic — BVN / phone verified
    TIER_2,   // Intermediate — ID document verified
    TIER_3    // Full — address + face match verified
}
 