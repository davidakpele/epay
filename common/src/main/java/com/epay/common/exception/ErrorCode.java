package com.epay.common.exception;

public final class ErrorCode {
    
    // Authentication & Authorization (1xxx)
    public static final String INVALID_CREDENTIALS = "AUTH_1001";
    public static final String ACCOUNT_LOCKED = "AUTH_1002";
    public static final String ACCOUNT_DISABLED = "AUTH_1003";
    public static final String TOKEN_EXPIRED = "AUTH_1004";
    public static final String TOKEN_INVALID = "AUTH_1005";
    public static final String UNAUTHORIZED_ACCESS = "AUTH_1006";
    public static final String FORBIDDEN_ACCESS = "AUTH_1007";
    public static final String SESSION_EXPIRED = "AUTH_1008";
    public static final String INVALID_OTP = "AUTH_1009";
    public static final String OTP_EXPIRED = "AUTH_1010";
    public static final String MAX_LOGIN_ATTEMPTS = "AUTH_1011";
    
    // User Management (2xxx)
    public static final String USER_NOT_FOUND = "USER_2001";
    public static final String USER_ALREADY_EXISTS = "USER_2002";
    public static final String INVALID_USER_DATA = "USER_2003";
    public static final String EMAIL_ALREADY_EXISTS = "USER_2004";
    public static final String USERNAME_ALREADY_EXISTS = "USER_2005";
    public static final String PHONE_ALREADY_EXISTS = "USER_2006";
    
    // Wallet Operations (3xxx)
    public static final String WALLET_NOT_FOUND = "WALLET_3001";
    public static final String INSUFFICIENT_BALANCE = "WALLET_3002";
    public static final String WALLET_LOCKED = "WALLET_3003";
    public static final String WALLET_SUSPENDED = "WALLET_3004";
    public static final String INVALID_CURRENCY = "WALLET_3005";
    public static final String CURRENCY_NOT_SUPPORTED = "WALLET_3006";
    public static final String INVALID_AMOUNT = "WALLET_3007";
    public static final String TRANSACTION_LIMIT_EXCEEDED = "WALLET_3008";
    public static final String DAILY_LIMIT_EXCEEDED = "WALLET_3009";
    public static final String INVALID_PIN = "WALLET_3010";
    public static final String PIN_LOCKED = "WALLET_3011";
    public static final String WALLET_ALREADY_EXISTS = "WALLET_3012";
    
    // Deposit Operations (4xxx)
    public static final String DEPOSIT_FAILED = "DEPOSIT_4001";
    public static final String DEPOSIT_NOT_FOUND = "DEPOSIT_4002";
    public static final String DEPOSIT_ALREADY_PROCESSED = "DEPOSIT_4003";
    public static final String INVALID_DEPOSIT_AMOUNT = "DEPOSIT_4004";
    public static final String DEPOSIT_GATEWAY_ERROR = "DEPOSIT_4005";
    public static final String DEPOSIT_VERIFICATION_FAILED = "DEPOSIT_4006";
    
    // Withdrawal Operations (5xxx)
    public static final String WITHDRAWAL_FAILED = "WITHDRAW_5001";
    public static final String WITHDRAWAL_NOT_FOUND = "WITHDRAW_5002";
    public static final String INVALID_WITHDRAWAL_AMOUNT = "WITHDRAW_5003";
    public static final String WITHDRAWAL_LIMIT_EXCEEDED = "WITHDRAW_5004";
    public static final String BANK_ACCOUNT_NOT_FOUND = "WITHDRAW_5005";
    public static final String INVALID_BANK_DETAILS = "WITHDRAW_5006";
    public static final String WITHDRAWAL_GATEWAY_ERROR = "WITHDRAW_5007";
    public static final String WITHDRAWAL_SUSPENDED = "WITHDRAW_5008";
    
    // Virtual Card Operations (6xxx)
    public static final String CARD_NOT_FOUND = "CARD_6001";
    public static final String CARD_CREATION_FAILED = "CARD_6002";
    public static final String CARD_FROZEN = "CARD_6003";
    public static final String CARD_TERMINATED = "CARD_6004";
    public static final String CARD_LIMIT_EXCEEDED = "CARD_6005";
    public static final String CARD_FUNDING_FAILED = "CARD_6006";
    public static final String CARD_PROVIDER_ERROR = "CARD_6007";
    public static final String MAX_CARD_LIMIT_REACHED = "CARD_6008";
    
    // Escrow Operations (7xxx)
    public static final String ESCROW_NOT_FOUND = "ESCROW_7001";
    public static final String ESCROW_ALREADY_RELEASED = "ESCROW_7002";
    public static final String ESCROW_ALREADY_CANCELLED = "ESCROW_7003";
    public static final String ESCROW_CREATION_FAILED = "ESCROW_7004";
    public static final String ESCROW_UNAUTHORIZED = "ESCROW_7005";
    public static final String ESCROW_EXPIRED = "ESCROW_7006";
    public static final String ESCROW_DISPUTED = "ESCROW_7007";
    
    // Beneficiary Operations (8xxx)
    public static final String BENEFICIARY_NOT_FOUND = "BENEF_8001";
    public static final String BENEFICIARY_ALREADY_EXISTS = "BENEF_8002";
    public static final String BENEFICIARY_LIMIT_REACHED = "BENEF_8003";
    public static final String BENEFICIARY_VERIFICATION_FAILED = "BENEF_8004";
    
    // Blacklist Operations (9xxx)
    public static final String ACCOUNT_BLACKLISTED = "BLACK_9001";
    public static final String IP_BLACKLISTED = "BLACK_9002";
    public static final String DEVICE_BLACKLISTED = "BLACK_9003";
    public static final String CARD_BLACKLISTED = "BLACK_9004";
    public static final String BLACKLIST_ENTRY_NOT_FOUND = "BLACK_9005";
    
    // Rate Limiting (10xxx)
    public static final String RATE_LIMIT_EXCEEDED = "RATE_10001";
    public static final String TOO_MANY_REQUESTS = "RATE_10002";
    
    // Validation Errors (11xxx)
    public static final String VALIDATION_ERROR = "VALID_11001";
    public static final String INVALID_INPUT = "VALID_11002";
    public static final String CONFLICT_ON_REQUEST = "VALID_11402";
    public static final String MISSING_REQUIRED_FIELD = "VALID_11003";
    public static final String INVALID_FORMAT = "VALID_11004";
    public static final String CONSTRAINT_VIOLATION = "VALID_11005";
    
    // Business Logic Errors (12xxx)
    public static final String BUSINESS_RULE_VIOLATION = "BIZ_12001";
    public static final String OPERATION_NOT_ALLOWED = "BIZ_12002";
    public static final String DUPLICATE_TRANSACTION = "BIZ_12003";
    public static final String TRANSACTION_CONFLICT = "BIZ_12004";
    
    // External Service Errors (13xxx)
    public static final String EXTERNAL_SERVICE_ERROR = "EXT_13001";
    public static final String GATEWAY_TIMEOUT = "EXT_13002";
    public static final String SERVICE_UNAVAILABLE = "EXT_13003";
    public static final String THIRD_PARTY_API_ERROR = "EXT_13004";
    
    // System Errors (14xxx)
    public static final String INTERNAL_SERVER_ERROR = "SYS_14001";
    public static final String DATABASE_ERROR = "SYS_14002";
    public static final String CACHE_ERROR = "SYS_14003";
    public static final String CONFIGURATION_ERROR = "SYS_14004";
    
    // Resource Errors (15xxx)
    public static final String RESOURCE_NOT_FOUND = "RES_15001";
    public static final String RESOURCE_ALREADY_EXISTS = "RES_15002";
    public static final String RESOURCE_CONFLICT = "RES_15003";
    
    private ErrorCode() {}
}
