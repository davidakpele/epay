use sqlx::{Postgres, Transaction};
use bigdecimal::BigDecimal;
use crate::models::debt_collector::{CurrencyType, DebtStatus};

pub struct MaintenanceFeeHistoryRepository;

impl MaintenanceFeeHistoryRepository {
    pub async fn record_paid(
        tx: &mut Transaction<'_, Postgres>,
        user_id: i64,
        currency_type: &CurrencyType, 
        fee_amount: &BigDecimal,
    ) -> sqlx::Result<()> {
        sqlx::query(
            r#"
            INSERT INTO maintenance_fee_history
                (user_id, currency_type, fee_amount, status, paid_on)
            VALUES ($1, $2, $3, 'PAID', NOW())
            "#
        )
        .bind(user_id)
        .bind(currency_type) 
        .bind(fee_amount)
        .execute(&mut **tx)
        .await?;

        Ok(())
    }

    pub async fn record_overdue(
        tx: &mut Transaction<'_, Postgres>,
        user_id: i64,
        currency_type: &CurrencyType,  
        fee_amount: &BigDecimal,
        reason: &str,
    ) -> sqlx::Result<()> {
        sqlx::query(
            r#"
            INSERT INTO maintenance_fee_history
                (user_id, currency_type, fee_amount, status, reason)
            VALUES ($1, $2, $3, 'OVERDUE', $4)
            "#
        )
        .bind(user_id)
        .bind(currency_type)  
        .bind(fee_amount)
        .bind(reason)
        .execute(&mut **tx)
        .await?;

        Ok(())
    }
}