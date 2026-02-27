// services/users_clearance_service.rs
use std::sync::Arc;
use sqlx::PgPool;
use reqwest::Client;
use std::str::FromStr;
use std::collections::HashMap;
use chrono::{Utc, Duration};
use crate::dto::dto::{UserResponse, UserDto, WalletResponse, WalletData, WalletBalanceDto, HistoryResponse, HistoryDto};

pub struct UsersClearanceService {
    pool: Arc<PgPool>,
    client: Client,
    user_service_url: String,
    notification_service_url: String,
}

impl UsersClearanceService {
    pub fn new(
        pool: Arc<PgPool>,
        user_service_url: String,
        notification_service_url: String,
    ) -> Self {
        Self {
            pool,
            client: Client::new(),
            user_service_url,
            notification_service_url,
        }
    }

    pub async fn run(&self) -> anyhow::Result<()> {
        let users = match self.fetch_all_users().await {
            Ok(users) => {
                users
            }
            Err(e) => {
                eprintln!("FAILED to fetch users: {}", e);
                return Err(e);
            }
        };
        Ok(())
    }

    async fn fetch_all_users(&self) -> anyhow::Result<Vec<UserDto>> {
        let url = format!("{}/cache/users/all", self.user_service_url);

        let response = self.client
            .get(&url)
            .send()
            .await?;
        let status = response.status();

        if !status.is_success() {
            let error_text = response.text().await.unwrap_or_default();
            eprintln!("User service error response: {}", error_text);
            return Err(anyhow::anyhow!("User service returned status: {} - {}", status, error_text));
        }
        
        let user_response = response.json::<UserResponse>().await?;
        Ok(user_response.data)
    }
}