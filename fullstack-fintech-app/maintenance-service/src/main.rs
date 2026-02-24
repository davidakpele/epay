// main.rs
use std::sync::Arc;
use std::env;
use tokio::time::{self, Duration};
use tokio::signal;
use dotenvy::dotenv;
use sqlx::postgres::PgPoolOptions;
use redis::Client as RedisClient;

mod models;
mod config;
mod middleware;
mod repositories;
mod utils;
mod payloads;
mod services;
mod dto;
mod controllers;
mod router; 

use services::maintenance_service::MaintenanceService;

#[derive(Clone)]
pub struct AppState {
    pub pool: Arc<sqlx::PgPool>,
    pub redis: Arc<RedisClient>,
}

#[tokio::main]
async fn main() -> Result<(), anyhow::Error> {
    dotenv().ok();
    
    // Database setup
    let database_url = env::var("DATABASE_URL").expect("DATABASE_URL must be set");
    let pool = PgPoolOptions::new()
        .max_connections(5)
        .connect(&database_url)
        .await?;
    let pool = Arc::new(pool);
    
    // Redis setup
    let redis = Arc::new(RedisClient::open("redis://127.0.0.1/")?);
    
    // Service URLs
    let user_service_url = env::var("USER_SERVICE_URL").unwrap_or_default();
    let wallet_service_url = env::var("WALLET_SERVICE_URL").unwrap_or_default();
    let history_service_url = env::var("HISTORY_SERVICE_URL").unwrap_or_default();
    let revenue_service_url = env::var("REVENUE_SERVICE_URL").unwrap_or_default();
    let notification_service_url = env::var("NOTIFICATION_SERVICE_URL").unwrap_or_default();

    // Create maintenance service for cron job
    let maintenance_service = MaintenanceService::new(
        Arc::clone(&pool),
        user_service_url,
        wallet_service_url,
        history_service_url,
        revenue_service_url,
        notification_service_url,
    );

    // Create app state for web server
    let app_state = AppState {
        pool: Arc::clone(&pool),
        redis: Arc::clone(&redis),
    };

    // Build the router - FIXED: use router::maintenance_router instead of routers::
    let app = router::maintenance_router::create_router().with_state(app_state);

    // Get port from environment or default to 3000
    let port = env::var("PORT").unwrap_or_else(|_| "3000".to_string());
    let addr = format!("0.0.0.0:{}", port);

    println!("🚀 Maintenance service started on http://{}", addr);

    // Create the server
    let listener = tokio::net::TcpListener::bind(&addr).await
        .unwrap_or_else(|_| panic!("Failed to bind to address {}", addr));
    println!("📡 Server listening on {}", addr);

    // Run the server and cron job concurrently
    tokio::select! {
        server_result = axum::serve(listener, app) => {
            if let Err(e) = server_result {
                eprintln!("❌ Server error: {}", e);
            }
        }
        cron_result = run_maintenance_cron(maintenance_service) => {
            if let Err(e) = cron_result {
                eprintln!("❌ Cron job error: {}", e);
            }
        }
        _ = graceful_shutdown() => {
            println!("🛑 Received shutdown signal, shutting down gracefully...");
        }
    }

    println!("👋 Service shutdown complete");
    Ok(())
}

async fn run_maintenance_cron(maintenance_service: MaintenanceService) -> Result<(), anyhow::Error> {
    println!("⏰ Maintenance cron job started (runs every 5 seconds)");
    let mut interval = time::interval(Duration::from_secs(5));
    
    loop {
        interval.tick().await;

        // Run maintenance task
        if let Err(e) = maintenance_service.run().await {
            eprintln!("❌ Error running maintenance task: {}", e);
        } else {
            println!("✅ Maintenance task completed successfully");
        }
    }
}

async fn graceful_shutdown() {
    let ctrl_c = async {
        signal::ctrl_c()
            .await
            .expect("Failed to install Ctrl+C handler");
    };

    #[cfg(unix)]
    let terminate = async {
        signal::unix::signal(signal::unix::SignalKind::terminate())
            .expect("Failed to install signal handler")
            .recv()
            .await;
    };

    #[cfg(not(unix))]
    let terminate = std::future::pending::<()>();

    tokio::select! {
        _ = ctrl_c => {
            println!("Received Ctrl+C signal");
        },
        _ = terminate => {
            println!("Received TERM signal");
        },
    }
}