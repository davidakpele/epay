package config

import (
	"log"
	"os"
	"strconv"

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
	Redis      RedisConfig
}

type RedisConfig struct {
	Address  string
	Password string
	DB       int
}

func LoadConfig() *Config {
	envPaths := []string{
		"./.env",
		"../.env",
		"../../.env",
		"/root/.env",
		"/app/.env",
		// "../.env",
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

	// Database configuration
	dbHost := getEnvWithDefault("DB_HOST", "localhost")
	dbPort := getEnvWithDefault("DB_PORT", "8390")
	dbUser := getEnvWithDefault("DB_USER", "banking_user")
	dbPassword := getEnvWithDefault("DB_PASSWORD", "banking_pass")
	dbName := getEnvWithDefault("DB_NAME", "banking_db")
	dbSSLMode := getEnvWithDefault("DB_SSLMODE", "disable")


	port := getEnvWithDefault("PORT", "8390")
	ginMode := getEnvWithDefault("GIN_MODE", "production")

	// Redis configuration
	redisAddress := getEnvWithDefault("REDIS_ADDRESS", "localhost:6379")
	redisPassword := getEnvWithDefault("REDIS_PASSWORD", "")
	redisDB := getEnvWithDefaultInt("REDIS_DB", 0)

	return &Config{
		DBHost:     dbHost,
		DBPort:     dbPort,
		DBUser:     dbUser,
		DBPassword: dbPassword,
		DBName:     dbName,
		DBSSLMode:  dbSSLMode,
		Port:       port,
		GinMode:    ginMode,
		Redis: RedisConfig{
			Address:  redisAddress,
			Password: redisPassword,
			DB:       redisDB,
		},
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

func getEnvWithDefaultInt(key string, defaultValue int) int {
	value := os.Getenv(key)
	if value == "" {
		return defaultValue
	}

	intValue, err := strconv.Atoi(value)
	if err != nil {
		log.Printf("Invalid integer value for %s: %s, using default: %d", key, value, defaultValue)
		return defaultValue
	}

	return intValue
}