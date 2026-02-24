package config

import (
    "log"
    "os"

    "github.com/joho/godotenv"
)

type Config struct {
    DBHost     string
    DBPort     string
    DBUser     string
    DBPassword string
    DBName     string
    DBSSLMode  string
    Port       string
    GinMode    string
}

func LoadConfig() *Config {
    envPaths := []string{
        "./.env",
        // "../.env",
        "../../.env",
        "/root/.env",
        "/app/.env",
    }
    
    var envLoaded bool
    for _, path := range envPaths {
        if err := godotenv.Load(path); err == nil {
            log.Printf("Loaded .env file from: %s", path)
            envLoaded = true
            break
        }
    }
    
    if !envLoaded {
        log.Println("No .env file found, relying on system environment variables")
    }
	dbHost := getEnvWithDefault("DB_HOST", "localhost")
	dbPort := getEnvWithDefault("DB_PORT", "8390")
	dbUser := getEnvWithDefault("DB_USER", "banking_user")
	dbPassword := getEnvWithDefault("DB_PASSWORD", "banking_pass")
	dbName := getEnvWithDefault("DB_NAME", "banking_db")
	dbSSLMode := getEnvWithDefault("DB_SSLMODE", "disable")


    port := getEnvWithDefault("PORT", "8083")
    ginMode := getEnvWithDefault("GIN_MODE", "release")

    return &Config{
        DBHost:     dbHost,
        DBPort:     dbPort,
        DBUser:     dbUser,
        DBPassword: dbPassword,
        DBName:     dbName,
        DBSSLMode:  dbSSLMode,
        Port:       port,
        GinMode:    ginMode,
    }
}

func getEnvOrFail(key string) string {
    value := os.Getenv(key)
    if value == "" {
        log.Fatalf("%s is required but not set in the environment", key)
    }
    return value
}

func getEnvWithDefault(key, defaultValue string) string {
    value := os.Getenv(key)
    if value == "" {
        return defaultValue
    }
    return value
}