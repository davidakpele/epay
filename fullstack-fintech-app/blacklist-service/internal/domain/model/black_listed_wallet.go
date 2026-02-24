package model

import ("time")

type BannedReasons string

const (
	SuspiciousActivity         BannedReasons = "SUSPICIOUS_ACTIVITY"
	TermsOfServiceViolation    BannedReasons = "TERMS_OF_SERVICE_VIOLATION"
	FraudulentActivity         BannedReasons = "FRAUDULENT_ACTIVITY"
	HarassmentOrBullying       BannedReasons = "HARASSMENT_OR_BULLYING"
	InappropriateContent       BannedReasons = "INAPPROPRIATE_CONTENT"
	PlatformManipulation       BannedReasons = "PLATFORM_MANIPULATION"
	IdentityVerificationFailure BannedReasons = "IDENTITY_VERIFICATION_FAILURE"
	SpammingOrSolicitation     BannedReasons = "SPAMMING_OR_SOLICITATION"
	MultipleUserReports        BannedReasons = "MULTIPLE_USER_REPORTS"
	ModeratorAction            BannedReasons = "MODERATOR_ACTION"
)

type BlackListedWallet struct {
	ID              uint           `gorm:"primaryKey;autoIncrement" json:"id"`
	WalletID        uint           `gorm:"not null" json:"wallet_id"`
	BankBannedReason BannedReasons `gorm:"type:varchar(200);not null" json:"bank_banned_reason"`
	IsBlock         bool           `gorm:"not null" json:"is_block"`
	Timestamp       time.Time      `gorm:"type:timestamp;not null" json:"timestamp"`
	CreatedAt       time.Time      `gorm:"autoCreateTime" json:"created_at"`
	UpdatedAt       time.Time      `gorm:"autoUpdateTime" json:"updated_at"`
}

