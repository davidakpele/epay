package main

import (
    "log"
    "revenue-service/internal/handlers"
    "revenue-service/internal/repository"
    "revenue-service/internal/service"
    "revenue-service/pkg/database"
    "revenue-service/config" 
    "github.com/gin-gonic/gin"
)

func main() {
    // Load configuration
    cfg := config.LoadConfig()

    // Set gin mode from config
    gin.SetMode(cfg.GinMode)

    // Build DSN from configuration
    dsn := buildDSN(cfg)
    
    db := database.ConnectDB(dsn)

    // Initialize repositories
    revenueRepo := repository.NewRevenueRepository(db)
    balanceRepo := repository.NewCurrencyBalanceRepository(db)
    transactionRepo := repository.NewTransactionRepository(db)

    // Initialize services
    revenueService := service.NewRevenueService(revenueRepo, balanceRepo, transactionRepo)

    // Initialize handlers
    revenueHandler := handlers.NewRevenueHandler(revenueService)

    // Setup router
    router := gin.Default()

    // Revenue routes
    revenueRoutes := router.Group("/api/revenue")
    {
        revenueRoutes.GET("", revenueHandler.GetRevenue)
        revenueRoutes.PUT("/password", revenueHandler.UpdateRevenuePassword)
        revenueRoutes.POST("/transactions", revenueHandler.ProcessTransaction)
    }
    
    // Start server
    log.Printf("Server starting on port %s", cfg.Port)
    if err := router.Run(":" + cfg.Port); err != nil {
        log.Fatal("Failed to start server:", err)
    }
}

func buildDSN(cfg *config.Config) string {
    return cfg.DBUser + ":" + cfg.DBPassword + "@tcp(" + cfg.DBHost + ":" + cfg.DBPort + ")/" + cfg.DBName + "?charset=utf8mb4&parseTime=True&loc=Local"
}