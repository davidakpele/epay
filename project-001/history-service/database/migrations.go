package database

import (
	"log"
	"history-service/models"
	"gorm.io/gorm"
)

// MigrateModels handles database migrations
func MigrateModels(db *gorm.DB) error {
	log.Println("Starting database migration...")
	err := db.AutoMigrate(
		&models.History{},
	)
	if err == nil {
		log.Println("Database migrated successfully")
	}
	return err
}