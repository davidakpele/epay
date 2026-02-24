package main

import (
	"bank-collection-service/config"
	"bank-collection-service/controller"
	"bank-collection-service/db"
	"bank-collection-service/internal/handler"
	"bank-collection-service/internal/repository"
	"bank-collection-service/internal/services"
	"bank-collection-service/migrations"
	"bank-collection-service/routers"
	"context"
	"log"
	"os"
	"time"
    "go.opentelemetry.io/contrib/instrumentation/github.com/gin-gonic/gin/otelgin"
	"github.com/gin-contrib/cors"
	"github.com/gin-gonic/gin"
	"github.com/joho/godotenv"
	"go.opentelemetry.io/otel"
	"go.opentelemetry.io/otel/exporters/zipkin"
	sdktrace "go.opentelemetry.io/otel/sdk/trace"
)


func initTracer() func() {
    endpoint := "http://localhost:9411/api/v2/spans"

    exporter, err := zipkin.New(endpoint)
    if err != nil {
        log.Fatalf("failed to create zipkin exporter: %v", err)
    }

    // Set up TracerProvider
    tp := sdktrace.NewTracerProvider(
        sdktrace.WithBatcher(exporter),
    )
    otel.SetTracerProvider(tp)

    // Return cleanup function
    return func() {
        _ = tp.Shutdown(context.Background())
    }
}


func main() {
    if err := godotenv.Load(); err != nil {
        log.Fatal("Error loading .env file")
    }

    // Initialize tracing
    cleanup := initTracer()
    defer cleanup()

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
        log.Fatal("JWT_SECRET_KEY is not set in .env file")
    }

    router := gin.Default()
    router.Use(cors.New(cors.Config{
		AllowOrigins:     []string{"*"},
		AllowMethods:     []string{"GET", "POST", "PUT", "DELETE", "OPTIONS"},
		AllowHeaders:     []string{"Authorization", "Content-Type"},
		ExposeHeaders:    []string{"Content-Length"},
		AllowCredentials: true,
		MaxAge:           12 * time.Hour,
	}))
    router.Use(otelgin.Middleware("bank-collection-service"))

    bankRepo := repository.NewBankRepository(database) 
    virtualCardRepo := repository.NewVirtualCardRepository(database)

    virtualCardService := services.NewCardService(virtualCardRepo)
    bankService := services.NewBankService(bankRepo)

    bankHandler := handler.NewBankHandler(bankService)
    virtualCardHandler := handler.NewCardHandler(virtualCardService)

    virtualCardControll := controller.NewCardController(virtualCardHandler)
    bankController := controller.NewBankController(bankHandler)
    
    routers.RegisterRoutes(router, jwtSecretKey, bankController, virtualCardControll) 

    if err := router.Run(":8040"); err != nil {
        log.Fatalf("Error starting server: %v", err)
    }
    gin.SetMode(gin.ReleaseMode)
}
