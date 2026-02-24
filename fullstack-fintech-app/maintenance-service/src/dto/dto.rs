use serde::{Serialize, Deserialize};
use bigdecimal::BigDecimal;

// User Service Response DTOs
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct UserResponse {
    pub success: bool,
    pub message: Option<String>,
    pub data: Vec<UserDto>,
    pub cached: bool,
    pub count: i32,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct UserDto {
    pub id: i64,
    pub email: String,
    pub username: String,
    #[serde(rename = "createdOn")]
    pub created_on: String,
    #[serde(rename = "updatedOn")]
    pub updated_on: String,
    pub enabled: bool,
    pub records: Vec<UserRecord>,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct UserRecord {
    pub id: i64,
    #[serde(rename = "firstName")]
    pub first_name: Option<String>,  
    #[serde(rename = "lastName")]
    pub last_name: Option<String>,   
    pub telephone: Option<String>,   
    pub gender: Option<String>,      
    pub country: Option<String>,     
    pub city: Option<String>,        
    #[serde(rename = "nextOfKing")]
    pub next_of_king: Option<String>, 
    #[serde(rename = "isTransferPinSet")]
    pub is_transfer_pin_set: bool,
    pub locked: bool,
    #[serde(rename = "lockedAt")]
    pub locked_at: Option<String>,   
    #[serde(rename = "isBlocked")]
    pub is_blocked: bool,
    #[serde(rename = "blockedDuration")]
    pub blocked_duration: Option<String>, 
    #[serde(rename = "blockedUntil")]
    pub blocked_until: Option<String>, 
    #[serde(rename = "blockedReason")]
    pub blocked_reason: Option<String>, 
    #[serde(rename = "referralCode")]
    pub referral_code: String,
    #[serde(rename = "totalReferers")]
    pub total_referers: Option<i32>, 
    #[serde(rename = "referralLink")]
    pub referral_link: String,
    pub photo: Option<String>,       
}

// Wallet Service Response DTOs
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct WalletResponse {
    pub wallet: WalletData,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct WalletData {
    pub id: i64,
    #[serde(rename = "userId")]
    pub user_id: i64,
    pub balances: Vec<WalletBalanceDto>,
    #[serde(rename = "createdOn")]
    pub created_on: String,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct WalletBalanceDto {
    #[serde(rename = "currencyCode")]
    pub currency_code: String,
    #[serde(rename = "currencySymbol")] 
    pub currency_symbol: String,
    #[serde(with = "bigdecimal_serde")]
    pub balance: BigDecimal,
}

// History Service Response DTOs
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct HistoryResponse {
    pub cached: bool,
    pub count: i32,
    pub data: Vec<HistoryDto>,
    pub source: String,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct HistoryDto {
    pub id: String,
    #[serde(rename = "walletId")]
    pub wallet_id: i64,
    #[serde(rename = "userId")]
    pub user_id: i64,
    #[serde(rename = "sessionId")]
    pub session_id: String,
    #[serde(rename = "transactionId")]
    pub transaction_id: String,
    #[serde(rename = "referenceNo")]
    pub reference_no: String,
    #[serde(rename = "terminalId")]
    pub terminal_id: String,
    #[serde(rename = "erId")]
    pub er_id: String,
    #[serde(rename = "accountHolder")]
    pub account_holder: String,
    #[serde(rename = "previousBalance")]
    #[serde(with = "bigdecimal_serde")]
    pub previous_balance: BigDecimal,
    #[serde(rename = "availableBalance")]
    #[serde(with = "bigdecimal_serde")]
    pub available_balance: BigDecimal,
    #[serde(with = "bigdecimal_serde")]
    pub amount: BigDecimal,
    #[serde(rename = "type")]
    pub transaction_type: String,
    pub description: String,
    pub message: String,
    #[serde(rename = "currencyType")]
    pub currency_type: String,
    pub status: String,
    #[serde(rename = "ipAddress")]
    pub ip_address: String,
    pub timestamp: String,
}

mod bigdecimal_serde {
    use bigdecimal::BigDecimal;
    use serde::{Serializer, Deserializer, Deserialize};
    use std::str::FromStr;
    use serde_json::Value;

    pub fn serialize<S>(value: &BigDecimal, serializer: S) -> Result<S::Ok, S::Error>
    where
        S: Serializer,
    {
        serializer.serialize_str(&value.to_string())
    }

    pub fn deserialize<'de, D>(deserializer: D) -> Result<BigDecimal, D::Error>
    where
        D: Deserializer<'de>,
    {
        let value = Value::deserialize(deserializer)?;
        
        match value {
            Value::String(s) => {
                BigDecimal::from_str(&s).map_err(serde::de::Error::custom)
            }
            Value::Number(num) => {
                let num_str = num.to_string();
                BigDecimal::from_str(&num_str).map_err(serde::de::Error::custom)
            }
            _ => Err(serde::de::Error::custom("Expected string or number for BigDecimal")),
        }
    }
}