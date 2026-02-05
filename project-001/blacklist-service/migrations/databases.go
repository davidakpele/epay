package migrations

import (
	"log"
	"blacklist-service/internal/domain/model"
	"gorm.io/gorm"
)
func MigrateModels(db *gorm.DB) error {
	log.Println("Starting database migration...")
	err := db.AutoMigrate(
		&model.BlackListedWallet{}, 
	)
	if err == nil {
		log.Println("Database migrated successfully")
	}
	return err
}