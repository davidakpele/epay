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
    "context"
    "fmt"
    "log"
    "os"
    "time"

    "github.com/gin-contrib/cors"
    "github.com/gin-gonic/gin"
    "github.com/hudl/fargo"
    "github.com/joho/godotenv"
    "go.opentelemetry.io/contrib/instrumentation/github.com/gin-gonic/gin/otelgin"
    "go.opentelemetry.io/otel"
    "go.opentelemetry.io/otel/exporters/zipkin"
    sdktrace "go.opentelemetry.io/otel/sdk/trace"
)

func initTracer() func() {
    endpoint := "http://zipkin:9411/"
    exporter, err := zipkin.New(endpoint)
    if err != nil {
        log.Fatalf("failed to create zipkin exporter: %v", err)
    }
    tp := sdktrace.NewTracerProvider(
        sdktrace.WithBatcher(exporter),
    )
    otel.SetTracerProvider(tp)
    return func() {
        _ = tp.Shutdown(context.Background())
    }
}

func registerWithEureka() (*fargo.EurekaConnection, *fargo.Instance) {
    eurekaServer := os.Getenv("EUREKA_SERVER_URL")
    if eurekaServer == "" {
        eurekaServer = "http://service-registry:8761"
    }

    conn := fargo.NewConn(eurekaServer)
    
    hostname := "blacklist-service"
    port := 8013

    instance := &fargo.Instance{
        InstanceId:       fmt.Sprintf("%s:%d", hostname, port),
        HostName:         hostname,
        App:              "BLACKLIST-SERVICE",
        IPAddr:           hostname,
        Port:             port,
        VipAddress:       "blacklist-service",
        Status:           fargo.UP,
        DataCenterInfo: fargo.DataCenterInfo{
            Class: "com.netflix.appinfo.InstanceInfo$DefaultDataCenterInfo",
            Name:  "MyOwn",
        },
        HomePageUrl:    fmt.Sprintf("http://%s:%d/", hostname, port),
        StatusPageUrl:  fmt.Sprintf("http://%s:%d/actuator/info", hostname, port),
        HealthCheckUrl: fmt.Sprintf("http://%s:%d/health", hostname, port),
    }

    var err error
    for i := 0; i < 5; i++ {
        err = conn.RegisterInstance(instance)
        if err == nil {
            log.Println("Successfully registered with Eureka")
            break
        }
        log.Printf("Registration attempt %d failed: %v", i+1, err)
        time.Sleep(time.Duration(i*i) * time.Second)
    }

    if err != nil {
        log.Printf("Failed to register with Eureka after retries: %v", err)
        return &conn, instance
    }

    go func() {
        time.Sleep(10 * time.Second)
        ticker := time.NewTicker(25 * time.Second)
        defer ticker.Stop()
        
        for range ticker.C {
            err := conn.HeartBeatInstance(instance)
            if err != nil {
                log.Printf("Eureka heartbeat failed: %v", err)
                time.Sleep(5 * time.Second)
                conn.RegisterInstance(instance)
            }
        }
    }()

    return &conn, instance
}

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
        log.Fatal("JWT_SECRET_KEY is not set in environment")
    }

    gin.SetMode(gin.ReleaseMode)
    router := gin.Default()
    router.Use(cors.Default())
    router.Use(otelgin.Middleware("blacklist-service"))
    
    setupHealthEndpoint(router)

    // conn, instance := registerWithEureka()
    // defer func() {
    //     log.Println("Deregistering from Eureka...")
    //     if err := conn.DeregisterInstance(instance); err != nil {
    //         log.Printf("Deregistration failed: %v", err)
    //     }
    // }()

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