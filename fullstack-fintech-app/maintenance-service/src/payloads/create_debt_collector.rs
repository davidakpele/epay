use serde::{Deserialize, Serialize};
use bigdecimal::BigDecimal;
use crate::models::debt_collector::{DebtStatus, CurrencyType};

#[derive(Debug, Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct CreateDebtCollector {
    pub user_id: i64,
    pub amount: BigDecimal,
    pub due_amount: BigDecimal,
    pub debt_status: Option<DebtStatus>,
    pub description: Option<String>,
    pub currency_type: CurrencyType,
}
