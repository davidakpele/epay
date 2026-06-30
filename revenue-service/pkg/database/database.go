package database

import (
	"log"
	"revenue-service/internal/models"
	"gorm.io/driver/mysql"
	"gorm.io/gorm"
	"gorm.io/gorm/logger"
)

func ConnectDB(dsn string) *gorm.DB {
    db, err := gorm.Open(mysql.Open(dsn), &gorm.Config{
        Logger: logger.Default.LogMode(logger.Info), 
    })
    if err != nil {
        log.Fatal("Failed to connect to database:", err)
    }

    err = db.AutoMigrate(
        &models.Revenue{},
        &models.CurrencyBalance{},
        &models.RevenueTransaction{},
    )
    if err != nil {
        log.Fatal("Failed to migrate database:", err)
    }

    log.Println("Database connection established successfully")
    return db
}