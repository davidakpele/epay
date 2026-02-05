package main

import (
	"beneficiary-service/config"
	"beneficiary-service/controller"
	"beneficiary-service/db"
	"beneficiary-service/internal/handler"
	"beneficiary-service/internal/repository"
	"beneficiary-service/internal/services"
	"beneficiary-service/migrations"
	"beneficiary-service/routers"
	"log"
	"os"
	"time"

	"github.com/gin-contrib/cors"
	"github.com/gin-gonic/gin"
	"github.com/joho/godotenv"
)

func main() {
    // Load .env file
    if err := godotenv.Load(); err != nil {
        log.Fatal("Error loading .env file")
    }

    // Load configuration
    cfg := config.LoadConfig()

    // Connect to the database
    database, err := db.ConnectDatabase(cfg)
    if err != nil {
        log.Fatalf("Failed to connect to database: %v", err)
    }

    // Migrate model
    if err := migrations.MigrateModels(database); err != nil {
        log.Fatalf("Database migration failed: %v", err)
    }

    // Retrieve the JWT secret key from the environment variable
    jwtSecretKey := os.Getenv("JWT_SECRET_KEY")
    if jwtSecretKey == "" {
        log.Fatal("JWT_SECRET_KEY is not set in .env file")
    }

    // Create router
    router := gin.Default()

    // CORS configuration
    router.Use(cors.Default())

    // Custom CORS configuration
	router.Use(cors.New(cors.Config{
		AllowOrigins:     []string{"*"},
		AllowMethods:     []string{"GET", "POST", "PUT", "DELETE", "OPTIONS"},
		AllowHeaders:     []string{"Authorization", "Content-Type"},
		ExposeHeaders:    []string{"Content-Length"},
		AllowCredentials: true,
		MaxAge:           12 * time.Hour,
	}))
	
    router.Static("/static", "./static")  
    // Initialize dependencies

    // Initialize dependencies
    beneficiaryRepo := repository.NewBeneficiaryRepository(database) 
    beneficiaryService := services.NewBeneficiaryService(beneficiaryRepo)
    beneficiaryHandler := handler.NewBeneficiaryHandler(beneficiaryService)
    beneficiaryController := controller.NewBeneficiaryController(beneficiaryHandler)

    // Register all routes by passing the router and dependencies
    routers.RegisterRoutes(router, jwtSecretKey, beneficiaryController) 

    // Start the server
    if err := router.Run(":8084"); err != nil {
        log.Fatalf("Error starting server: %v", err)
    }
    gin.SetMode(gin.ReleaseMode)
}
