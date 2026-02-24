use std::sync::Arc;
use sqlx::PgPool;
use reqwest::Client;
use std::str::FromStr;
use bigdecimal::BigDecimal;
use std::collections::HashMap;
use chrono::{Utc, Duration};
use crate::models::debt_collector::{WalletMaintenance, DebtStatus, CurrencyType};
use crate::repositories::wallet_maintenance_repository::WalletMaintenanceRepository;
use crate::repositories::maintenance_fee_history_repository::MaintenanceFeeHistoryRepository;
use crate::dto::dto::{UserResponse, UserDto, WalletResponse, WalletData, WalletBalanceDto, HistoryResponse, HistoryDto};

pub struct MaintenanceService {
    pool: Arc<PgPool>,
    client: Client,
    user_service_url: String,
    wallet_service_url: String,
    history_service_url: String,
    revenue_service_url: String, 
    notification_service_url: String,
}

impl MaintenanceService {
    pub fn new(
        pool: Arc<PgPool>,
        user_service_url: String,
        wallet_service_url: String,
        history_service_url: String,
        revenue_service_url: String,
        notification_service_url: String,
    ) -> Self {
        Self {
            pool,
            client: Client::new(),
            user_service_url,
            wallet_service_url,
            history_service_url,
            revenue_service_url,
            notification_service_url,
        }
    }

    pub async fn run(&self) -> anyhow::Result<()> {
        let users = match self.fetch_users().await {
            Ok(users) => {
                users
            }
            Err(e) => {
                eprintln!("FAILED to fetch users: {}", e);
                return Err(e);
            }
        };

        for user in users {
            let wallet_data = match self.fetch_wallets(user.id).await {
                Ok(wallet) => {
                    wallet
                }
                Err(e) => {
                    eprintln!("FAILED to fetch wallet for user {}: {}", user.id, e);
                    continue;
                }
            };
            
            let user_history = match self.fetch_user_history(user.id).await {
                Ok(history) => {
                    history
                }
                Err(e) => {
                    eprintln!("FAILED to fetch history for user {}: {}", user.id, e);
                    continue;
                }
            };
            let currency_totals = self.calculate_chargeable_totals(&user_history);
            
            if currency_totals.is_empty() {
                continue;
            }
            for (currency_code, total_spent) in currency_totals {
                if let Some(balance) = wallet_data.balances.iter()
                    .find(|b| b.currency_code == currency_code) {
                    
                    match self.charge_fee(&user, &wallet_data, balance, &total_spent).await {
                        Ok(()) => println!("Successfully charged fee for user {} currency {}", user.id, currency_code),
                        Err(e) => eprintln!("FAILED to charge fee for user {} currency {}: {}", user.id, currency_code, e),
                    }
                } else {
                    println!("No wallet balance found for user {} currency {}", user.id, currency_code);
                }
            }
        }
        Ok(())
    }

    fn calculate_chargeable_totals(&self, history: &[HistoryDto]) -> HashMap<String, BigDecimal> {
        let chargeable_types = vec!["WITHDRAW", "TRANSFER", "SWAP", "DEBITED", "EXCHANGE"];
        let mut currency_totals = HashMap::new();

        for record in history {
            if chargeable_types.contains(&record.transaction_type.as_str()) {
                let total = currency_totals
                    .entry(record.currency_type.clone())
                    .or_insert(BigDecimal::from(0));
                *total += &record.amount;
            }
        }
        currency_totals
    }

    async fn fetch_users(&self) -> anyhow::Result<Vec<UserDto>> {
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

    async fn fetch_wallets(&self, user_id: i64) -> anyhow::Result<WalletData> {
        let url = format!("{}/wallet/cache/{}", self.wallet_service_url, user_id);
        let response = match self.client
            .get(&url)
            .timeout(std::time::Duration::from_secs(10))
            .send()
            .await {
                Ok(resp) => {
                    resp
                }
                Err(e) => {
                    eprintln!("HTTP REQUEST FAILED: {}", e);
                    eprintln!("Error type: {:?}", e);
                    eprintln!("URL attempted: {}", url);
                    return Err(anyhow::anyhow!("HTTP request failed: {}", e));
                }
            };
            
        let status = response.status();
        if !status.is_success() {
            let error_text = response.text().await.unwrap_or_default();
            eprintln!("Wallet service error response: {}", error_text);
            return Err(anyhow::anyhow!("Wallet service returned status: {} - {}", status, error_text));
        }
        let wallet_response = response.json::<WalletResponse>().await?;
        Ok(wallet_response.wallet)
    }
   
    async fn fetch_user_history(&self, user_id: i64) -> anyhow::Result<Vec<HistoryDto>> {
        let from = Utc::now() - Duration::days(30);
        let url = format!(
            "{}/history/cache/user/{}?from={}",
            self.history_service_url,
            user_id,
            from.to_rfc3339()
        );
        
        let response = self.client
            .get(&url)
            .send()
            .await?;
            
        let status = response.status();
        if !status.is_success() {
            let error_text = response.text().await.unwrap_or_default();
            eprintln!("History service error response: {}", error_text);
            return Err(anyhow::anyhow!("History service returned status: {} - {}", status, error_text));
        }
        
        let history_response = response.json::<HistoryResponse>().await?;
        Ok(history_response.data)
    }

    async fn charge_fee(
        &self, 
        user: &UserDto, 
        wallet_data: &WalletData, 
        balance: &WalletBalanceDto,
        total_spent: &BigDecimal
    ) -> anyhow::Result<()> {
        let fee_rate = BigDecimal::from_str("0.005")?; 
        let fee_amount = total_spent * &fee_rate;
        let previous_balance = balance.balance.clone();
        let available_balance_after_fee = &previous_balance - &fee_amount;
        
        let currency_type = match balance.currency_code.as_str() {
            "USD" => CurrencyType::USD,
            "EUR" => CurrencyType::EUR,
            "NGN" => CurrencyType::NGN,
            "GBP" => CurrencyType::GBP,
            "JPY" => CurrencyType::JPY,
            "AUD" => CurrencyType::AUD,
            "CAD" => CurrencyType::CAD,
            "CHF" => CurrencyType::CHF,
            "CNY" => CurrencyType::CNY,
            "INR" => CurrencyType::INR,
            _ => return Err(anyhow::anyhow!("Invalid currency code: {}", balance.currency_code)),
        };

        let mut tx = self.pool.begin().await?;
        let last_charged = WalletMaintenanceRepository::get_last_charged_date(&mut tx, user.id, &currency_type).await?;
        
        if let Some(last_charge) = last_charged {
            let days_since_last_charge = Utc::now().signed_duration_since(last_charge).num_days();
            if days_since_last_charge < 30 {
                return Ok(());
            }
        }

        let minimum_fee = BigDecimal::from_str("0.01")?;
        if fee_amount <= minimum_fee {
            return Ok(());
        }
        if total_spent == &BigDecimal::from(0) {
            return Ok(());
        }
        WalletMaintenanceRepository::initialize_or_update(
            &mut tx, 
            user.id, 
            &currency_type,
            &balance.balance
        ).await?;

        if balance.balance >= fee_amount && balance.balance > BigDecimal::from(0) {
            let wallet_result = self.deduct_from_wallet(user.id, wallet_data.id, &balance.currency_code, &fee_amount).await;
            match wallet_result {
                Ok(()) => {
                    let revenue_result = self.record_revenue(&balance.currency_code, &fee_amount).await;
                    
                    match revenue_result {
                        Ok(()) => {
                            WalletMaintenanceRepository::deduct_fee(&mut tx, user.id, &currency_type, &fee_amount).await?;
                            MaintenanceFeeHistoryRepository::record_paid(&mut tx, user.id, &currency_type, &fee_amount).await?;
                            if let Err(e) = self.send_notification(
                                user,
                                "MAINTENANCE_FEE_PAID",
                                &balance.currency_code,
                                total_spent,
                                &fee_amount,
                                &previous_balance,
                                &available_balance_after_fee,
                                "Monthly maintenance fee charged on your transaction activities",
                                true
                            ).await {
                                eprintln!("Failed to send success notification: {}", e);
                            }
                            
                            tx.commit().await?;
                            Ok(())
                        }
                        Err(revenue_error) => {
                        
                            if let Err(rollback_error) = self.reverse_wallet_deduction(user.id, wallet_data.id, &balance.currency_code, &fee_amount).await {
                                eprintln!("CRITICAL: Failed to reverse wallet deduction: {}", rollback_error);
                            }
    
                            WalletMaintenanceRepository::mark_overdue(&mut tx, user.id, &currency_type).await?;
                            MaintenanceFeeHistoryRepository::record_overdue(
                                &mut tx, 
                                user.id, 
                                &currency_type, 
                                &fee_amount, 
                                &format!("Revenue recording failed: {}", revenue_error)
                            ).await?;
                            
                            tx.commit().await?;
                            Err(anyhow::anyhow!("Revenue recording failed: {}", revenue_error))
                        }
                    }
                }
                Err(wallet_error) => {
                    eprintln!("Wallet deduction failed: {}", wallet_error);
                    
                    WalletMaintenanceRepository::mark_overdue(&mut tx, user.id, &currency_type).await?;
                    MaintenanceFeeHistoryRepository::record_overdue(
                        &mut tx, 
                        user.id, 
                        &currency_type, 
                        &fee_amount, 
                        &format!("Wallet deduction failed: {}", wallet_error)
                    ).await?;
                    
                    tx.commit().await?;
                    Err(anyhow::anyhow!("Wallet deduction failed: {}", wallet_error))
                }
            }
        } else {
            WalletMaintenanceRepository::mark_overdue(&mut tx, user.id, &currency_type).await?;
            MaintenanceFeeHistoryRepository::record_overdue(&mut tx, user.id, &currency_type, &fee_amount, "Insufficient balance").await?;
            
            tx.commit().await?;
            println!("Insufficient balance for fee charge on user {} currency {} (balance: {}, fee: {}, total_spent: {})", 
                user.id, balance.currency_code, balance.balance, fee_amount, total_spent);
            Ok(())
        }
    }

    async fn deduct_from_wallet(&self, user_id: i64, wallet_id: i64, currency_code: &str, fee_amount: &BigDecimal) -> anyhow::Result<()> {
        let wallet_url = format!("{}/wallet/internal/debit/maintenance", self.wallet_service_url);
        
        let wallet_request_body = serde_json::json!({
            "userId": user_id,
            "walletId": wallet_id,
            "currencyType": currency_code,
            "amount": fee_amount.to_string(),
            "description": "Monthly maintenance fee on transaction activities",
            "referenceNo": format!("MAINT-{}-{}", user_id, Utc::now().timestamp())
        });
        let wallet_response = self.client
            .post(&wallet_url)
            .json(&wallet_request_body)
            .timeout(std::time::Duration::from_secs(10))
            .send()
            .await?;

        let wallet_status = wallet_response.status();
        if !wallet_status.is_success() {
            let error_text = wallet_response.text().await.unwrap_or_default();
            eprintln!("Wallet deduction error: {}", error_text);
            return Err(anyhow::anyhow!("Wallet service returned status: {} - {}", wallet_status, error_text));
        }

        println!("SUCCESS: Fee deducted from wallet");
        Ok(())
    }

    async fn record_revenue(&self, currency_code: &str, fee_amount: &BigDecimal) -> anyhow::Result<()> {
        let revenue_url = format!("{}/api/revenue/transactions", self.revenue_service_url);
        let fee_amount_f64 = fee_amount.to_string().parse::<f64>()
            .map_err(|e| anyhow::anyhow!("Failed to convert BigDecimal to f64: {}", e))?;
        let fee_amount_rounded = (fee_amount_f64 * 100.0).round() / 100.0;
        
        let revenue_request_body = serde_json::json!({
            "transactionType": "MAINTENANCE_FEE",
            "amount": fee_amount_rounded,
            "currency": currency_code,
        });
        let revenue_response = self.client
            .post(&revenue_url)
            .json(&revenue_request_body)
            .timeout(std::time::Duration::from_secs(10))
            .send()
            .await?;

        let revenue_status = revenue_response.status();
        if !revenue_status.is_success() {
            let error_text = revenue_response.text().await.unwrap_or_default();
            eprintln!("Revenue recording error: {}", error_text);
            return Err(anyhow::anyhow!("Revenue service returned status: {} - {}", revenue_status, error_text));
        }
        Ok(())
    }

    async fn reverse_wallet_deduction(&self, user_id: i64, wallet_id: i64, currency_code: &str, fee_amount: &BigDecimal) -> anyhow::Result<()> {
        let wallet_url = format!("{}/wallet/internal/credit/maintenance", self.wallet_service_url);
        
        let wallet_request_body = serde_json::json!({
            "userId": user_id,
            "walletId": wallet_id,
            "currencyType": currency_code,
            "amount": fee_amount.to_string(),
            "description": "Reversal: Maintenance fee deduction failed",
            "referenceNo": format!("REV-MAINT-{}-{}", user_id, Utc::now().timestamp())
        });
        let wallet_response = self.client
            .post(&wallet_url)
            .json(&wallet_request_body)
            .timeout(std::time::Duration::from_secs(10))
            .send()
            .await?;

        let wallet_status = wallet_response.status();
        if !wallet_status.is_success() {
            let error_text = wallet_response.text().await.unwrap_or_default();
            eprintln!("Wallet reversal error: {}", error_text);
            return Err(anyhow::anyhow!("Wallet service returned status: {} - {}", wallet_status, error_text));
        }
        Ok(())
    }

    async fn send_notification(
        &self,
        user: &UserDto,
        action_type: &str,
        currency_code: &str,
        total_amount_spent: &BigDecimal,
        fee_amount: &BigDecimal,
        previous_balance: &BigDecimal,
        available_balance: &BigDecimal,
        reason: &str,
        success: bool
    ) -> anyhow::Result<()> {
        let url = format!("{}/notifications/maintenance-fee", self.notification_service_url);
        let (first_name, last_name) = self.extract_user_names(user);
        let total_amount_spent_f64 = total_amount_spent.to_string().parse::<f64>()
            .map(|v| (v * 100.0).round() / 100.0)
            .unwrap_or(0.0);
        let fee_amount_f64 = fee_amount.to_string().parse::<f64>()
            .map(|v| (v * 100.0).round() / 100.0)
            .unwrap_or(0.0);
        let previous_balance_f64 = previous_balance.to_string().parse::<f64>()
            .map(|v| (v * 100.0).round() / 100.0)
            .unwrap_or(0.0);
        let available_balance_f64 = available_balance.to_string().parse::<f64>()
            .map(|v| (v * 100.0).round() / 100.0)
            .unwrap_or(0.0);

        let notification_request = serde_json::json!({
            "userId": user.id,
            "userEmail": user.email,
            "userFirstName": first_name,
            "userLastName": last_name,
            "actionType": action_type,
            "currency": currency_code,
            "totalAmountSpent": total_amount_spent_f64,
            "feeAmount": fee_amount_f64,
            "previousBalance": previous_balance_f64,
            "availableBalance": available_balance_f64,
            "reason": reason,
            "success": success,
            "timestamp": Utc::now().to_rfc3339()
        });
        let response = match self.client
            .post(&url)
            .json(&notification_request)
            .timeout(std::time::Duration::from_secs(10))
            .send()
            .await {
                Ok(resp) => resp,
                Err(e) => {
                    eprintln!("Notification service request failed: {}", e);
                    return Ok(());
                }
            };

        let status = response.status();
        if !status.is_success() {
            let error_text = response.text().await.unwrap_or_default();
            eprintln!("Notification service error: {}", error_text);
            println!("Continuing despite notification service error");
        }

        Ok(())
    }
    
    fn extract_user_names(&self, user: &UserDto) -> (String, String) {
        if let Some(record) = user.records.first() {
            let first_name = record.first_name
                .clone()
                .unwrap_or_else(|| "User".to_string());
            let last_name = record.last_name
                .clone()
                .unwrap_or_else(|| user.id.to_string());
            (first_name, last_name)
        } else {
            ("User".to_string(), user.id.to_string())
        }
    }
}