use serde::Deserialize;
use dotenv::dotenv;
use std::env;

#[allow(dead_code)]
#[derive(Debug, Deserialize, Clone)]
pub struct Settings {
    pub database_url: String,
    pub db_max_connections: u32,
    pub port: u16,
    pub user_service_url: String,
    pub wallet_service_url: String,
    pub history_service_url: String,
    pub revenue_service_url: String,
    pub notification_service_url: String,
}

impl Settings {
    pub fn new() -> Self {
        dotenv().ok();

        Settings {
            database_url: env::var("DATABASE_URL").expect("DATABASE_URL must be set"),
            db_max_connections: env::var("DB_MAX_CONNECTIONS")
                .unwrap_or_else(|_| "10".to_string())
                .parse()
                .expect("DB_MAX_CONNECTIONS must be a number"),
            port: env::var("PORT")
                .unwrap_or_else(|_| "8086".to_string())
                .parse()
                .expect("PORT must be a number"),
            user_service_url: env::var("USER_SERVICE_URL")
                .unwrap_or_else(|_| "http://authentication-service:8187".to_string()),
            wallet_service_url: env::var("WALLET_SERVICE_URL")
                .unwrap_or_else(|_| "http://wallet-service:8035".to_string()),
            history_service_url: env::var("HISTORY_SERVICE_URL")
                .unwrap_or_else(|_| "http://history-service:8390".to_string()),
            revenue_service_url: env::var("REVENUE_SERVICE_URL") 
                .unwrap_or_else(|_| "http://revenue-service:8083".to_string()),
            notification_service_url: env::var("NOTIFICATION_SERVICE_URL") 
                .unwrap_or_else(|_| "http://notification-service:8079".to_string()),
        }
    }
}