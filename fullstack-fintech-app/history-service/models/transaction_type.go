package models

type TransactionType string

const (
    DEPOSIT                 TransactionType = "DEPOSIT"
    WITHDRAW                TransactionType = "WITHDRAW"
    TRANSFER                TransactionType = "TRANSFER"
    CREDITED                TransactionType = "CREDITED"
    DEBITED                 TransactionType = "DEBITED"
    BUY                     TransactionType = "BUY"
    SELL                    TransactionType = "SELL"
    SEND                    TransactionType = "SEND"
    RECEIVE                 TransactionType = "RECEIVE"
    SWAP                    TransactionType = "SWAP"
    BANK_TO_WALLET_DEPOSIT  TransactionType = "BANK_TO_WALLET_DEPOSIT"
    WALLET_TO_BANK_TRANSFER TransactionType = "WALLET_TO_BANK_TRANSFER"
    WALLET_TO_WALLET_TRANSFER TransactionType = "WALLET_TO_WALLET_TRANSFER"
    LOAD_AIRTIME            TransactionType = "LOAD_AIRTIME"
    LOAD_DATA_BUNDLE        TransactionType = "LOAD_DATA_BUNDLE"
    SUBSCRIBE_CABLE         TransactionType = "SUBSCRIBE_CABLE"
    BOOKED_FLIGHT           TransactionType = "BOOKED_FLIGHT"
    BOOKED_HOTEL            TransactionType = "BOOKED_HOTEL"
)