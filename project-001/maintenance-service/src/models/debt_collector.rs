use serde::{Deserialize, Serialize};
use sqlx::FromRow;
use chrono::NaiveDateTime;
use bigdecimal::BigDecimal;

#[derive(Debug, Clone, Serialize, Deserialize, FromRow)]
#[serde(rename_all = "camelCase")]
pub struct DebtCollector {
    pub id: i64,                  
    pub user_id: i64,
    pub amount: BigDecimal,
    pub due_amount: BigDecimal,
    pub debt_status: Option<DebtStatus>,
    pub description: Option<String>,
    pub currency_type: CurrencyType,
    pub created_on: Option<NaiveDateTime>,
    pub updated_on: Option<NaiveDateTime>,
}

#[derive(Debug, Clone, Serialize, Deserialize, sqlx::Type)]
#[sqlx(type_name = "debt_status", rename_all = "SCREAMING_SNAKE_CASE")]
#[serde(rename_all = "SCREAMING_SNAKE_CASE")]
pub enum DebtStatus {
    Pending,
    Paid,
    Overdue,
}

#[derive(Debug, Clone, Serialize, Deserialize, sqlx::Type)]
#[sqlx(type_name = "currency_type", rename_all = "UPPERCASE")]
#[serde(rename_all = "UPPERCASE")]
pub enum CurrencyType {
    USD,
    EUR,
    NGN,
    GBP,
    JPY,
    AUD,
    CAD,
    CHF,
    CNY,
    INR,
}

impl CurrencyType {
    pub fn as_str(&self) -> &'static str {
        match self {
            CurrencyType::USD => "USD",
            CurrencyType::EUR => "EUR",
            CurrencyType::NGN => "NGN",
            CurrencyType::GBP => "GBP",
            CurrencyType::JPY => "JPY",
            CurrencyType::AUD => "AUD",
            CurrencyType::CAD => "CAD",
            CurrencyType::CHF => "CHF",
            CurrencyType::CNY => "CNY",
            CurrencyType::INR => "INR",
        }
    }
}

#[derive(Debug, Clone, Serialize, Deserialize, FromRow)]
#[serde(rename_all = "camelCase")]
pub struct WalletMaintenance {
    pub id: i64,
    pub user_id: i64,
    pub currency_type: CurrencyType,
    pub balance: BigDecimal,
    pub status: DebtStatus,            
    pub last_charged: Option<NaiveDateTime>,
    pub created_on: Option<NaiveDateTime>,
    pub updated_on: Option<NaiveDateTime>,
}

#[derive(Debug, Clone, Serialize, Deserialize, FromRow)]
#[serde(rename_all = "camelCase")]
pub struct MaintenanceFeeHistory {
    pub id: i64,
    pub user_id: i64,
    pub currency_type: CurrencyType,
    pub fee_amount: BigDecimal,
    pub status: DebtStatus,
    pub attempted_on: Option<NaiveDateTime>,
    pub paid_on: Option<NaiveDateTime>,
    pub reason: Option<String>,
}
