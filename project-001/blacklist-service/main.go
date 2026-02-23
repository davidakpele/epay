package main

import (
	"blacklist-service/config"
	"blacklist-service/controller"
	"blacklist-service/db"
	"blacklist-service/internal/handler"
	"blacklist-service/internal/repository"
	"blacklist-service/internal/services"
	"blacklist-service/migrations"
	"blacklist-service/routers"
	"log"
	"os"
	"github.com/gin-contrib/cors"
	"github.com/gin-gonic/gin"
	"github.com/joho/godotenv"
)


func setupHealthEndpoint(router *gin.Engine) {
	router.GET("/health", func(c *gin.Context) {
		c.JSON(200, gin.H{
			"status": "UP",
		})
	})
}

func main() {
	if err := godotenv.Load(); err != nil {
		log.Println("No .env file found, using environment variables")
	}

	cfg := config.LoadConfig()
	database, err := db.ConnectDatabase(cfg)
	if err != nil {
		log.Fatalf("Failed to connect to database: %v", err)
	}

	if err := migrations.MigrateModels(database); err != nil {
		log.Fatalf("Database migration failed: %v", err)
	}

	jwtSecretKey := os.Getenv("JWT_SECRET_KEY")
	if jwtSecretKey == "" {
		log.Fatal("JWT_SECRET_KEY is not set in environment")
	}

	gin.SetMode(gin.ReleaseMode)
	router := gin.Default()
	router.Use(cors.Default())

	setupHealthEndpoint(router)

	blacklistedWalletRepo := repository.NewBlackListedWalletRepository(database)
	blacklistedWalletService := services.NewBlackListedWalletService(blacklistedWalletRepo)
	blacklistedWalletHandler := handler.NewBlackListedWalletHandler(blacklistedWalletService)
	bankController := controller.NewBlackListedWalletController(blacklistedWalletHandler)
	routers.RegisterRoutes(router, jwtSecretKey, bankController)

	port := ":8013"
	log.Printf("Starting blacklist-service on port %s", port)
	if err := router.Run(port); err != nil {
		log.Fatalf("Error starting server: %v", err)
	}
}