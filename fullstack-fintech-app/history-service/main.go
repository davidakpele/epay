package main

import (
	"context"
	"history-service/config"
	"history-service/database"
	"history-service/handlers"
	"history-service/repository"
	"history-service/router"
	"history-service/services"
	"log"
	"os"

	"github.com/redis/go-redis/v9"
	"go.uber.org/zap"
)

func main() {
	// Load configuration
	cfg := config.LoadConfig()
	if cfg.GinMode != "" {
		os.Setenv("GIN_MODE", cfg.GinMode)
	}
	logger, err := zap.NewProduction()
	if err != nil {
		log.Fatal("Failed to create logger:", err)
	}
	defer logger.Sync()
	db, err := database.ConnectDatabase(cfg)
	if err != nil {
		logger.Fatal("Failed to connect to database", zap.Error(err))
	}

	err = database.MigrateModels(db)
	if err != nil {
		logger.Fatal("Failed to migrate database", zap.Error(err))
	}

	redisClient := redis.NewClient(&redis.Options{
		Addr:     cfg.Redis.Address,
		Password: cfg.Redis.Password,
		DB:       cfg.Redis.DB,
	})

	// Test Redis connection
	ctx := context.Background()
	if err := redisClient.Ping(ctx).Err(); err != nil {
		logger.Warn("Failed to connect to Redis, cache will be disabled", zap.Error(err))
	} else {
		logger.Info("Successfully connected to Redis", zap.String("address", cfg.Redis.Address))
	}
	historyRepo := repository.NewHistoryRepository(db)
	
	cacheService := services.NewHistoryCacheService(
		redisClient,
		historyRepo,
		logger,                    
		services.NewNoopMetrics(), 
		services.DefaultCacheConfig(), 
	)
	cacheService.StartCacheWarmup(ctx)

	historyHandler := handlers.NewHistoryHandler(historyRepo, cacheService)

	router := router.SetupRouter(historyHandler)
	
	port := ":" + cfg.Port
	logger.Info("Server starting", 
		zap.String("port", port), 
		zap.String("mode", cfg.GinMode),
		zap.String("redis_addr", cfg.Redis.Address),
		zap.Int("redis_db", cfg.Redis.DB),
	)
	
	if err := router.Run(port); err != nil {
		logger.Fatal("Failed to start server", zap.Error(err))
	}
}