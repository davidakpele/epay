package models

type TransactionStatus string

const (
    Success TransactionStatus = "Success"
    Failed  TransactionStatus = "Failed"
)