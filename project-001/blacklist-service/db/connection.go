package db

import (
	"fmt"
	"log"
	"blacklist-service/config"
	"gorm.io/driver/mysql"
	"gorm.io/gorm"
	"gorm.io/gorm/logger"
)

// ConnectDatabase establishes a connection to the database and returns the DB instance
func ConnectDatabase(cfg *config.Config) (*gorm.DB, error) {
	 dsn := fmt.Sprintf("%s:%s@tcp(%s:%s)/%s?parseTime=true&loc=Local",
		cfg.DBUser, cfg.DBPassword, cfg.DBHost, cfg.DBPort, cfg.DBName)

	// Open the connection
	db, err := gorm.Open(mysql.Open(dsn), &gorm.Config{
		Logger: logger.Default.LogMode(logger.Info), 
	})
	if err != nil {
		return nil, fmt.Errorf("failed to connect to the database: %w", err)
	}

	// Test the connection
	sqlDB, err := db.DB()
	if err != nil {
		return nil, fmt.Errorf("failed to get SQL DB object: %w", err)
	}

	// Ping the database to check if the connection is valid
	if err := sqlDB.Ping(); err != nil {
		return nil, fmt.Errorf("failed to ping the database: %w", err)
	}

	log.Println("Database connection established successfully")
	return db, nil
}