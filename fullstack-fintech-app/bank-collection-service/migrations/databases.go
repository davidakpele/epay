package migrations

import (
	"log"
	"bank-collection-service/internal/domain/model"
	"gorm.io/gorm"
)

// MigrateModels is an exported function to handle database migrations
func MigrateModels(db *gorm.DB) error {
	log.Println("Starting database migration...")
	err := db.AutoMigrate(
		&model.UserBankList{},
		&model.VirtualCard{},
	)
	if err == nil {
		log.Println("Database migrated successfully")
	}
	return err
}
