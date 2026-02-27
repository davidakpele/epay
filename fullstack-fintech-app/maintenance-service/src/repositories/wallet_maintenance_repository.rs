// repositories/wallet_maintenance_repository.rs

use sqlx::{Postgres, Transaction, Row};
use bigdecimal::BigDecimal;
use chrono::Utc;
use crate::models::debt_collector::{CurrencyType, WalletMaintenance, DebtStatus};


pub struct WalletMaintenanceRepository;

impl WalletMaintenanceRepository {

    pub async fn initialize_or_update(
        tx: &mut Transaction<'_, Postgres>,
        user_id: i64,
        currency_type: &CurrencyType, 
        balance: &BigDecimal,
    ) -> Result<(), sqlx::Error> {
        sqlx::query(
            r#"
            INSERT INTO wallet_maintenance (user_id, currency_type, balance, status, updated_on)
            VALUES ($1, $2, $3, 'PENDING', NOW())
            ON CONFLICT (user_id, currency_type) 
            DO UPDATE SET 
                balance = EXCLUDED.balance,
                updated_on = NOW()
            "#
        )
        .bind(user_id)
        .bind(currency_type)  
        .bind(balance)
        .execute(&mut **tx)
        .await?;

        Ok(())
    }

    pub async fn deduct_fee(
        tx: &mut Transaction<'_, Postgres>,
        user_id: i64,
        currency_type: &CurrencyType,  
        amount: &BigDecimal,
    ) -> Result<(), sqlx::Error> {
        sqlx::query(
            r#"
            UPDATE wallet_maintenance
            SET balance = balance - $1, status = 'PAID', updated_on = NOW(), last_charged = NOW()
            WHERE user_id = $2 AND currency_type = $3
            "#
        )
        .bind(amount)
        .bind(user_id)
        .bind(currency_type)  
        .execute(&mut **tx)
        .await?;

        Ok(())
    }

    pub async fn mark_overdue(
        tx: &mut Transaction<'_, Postgres>,
        user_id: i64,
        currency_type: &CurrencyType,  
    ) -> Result<(), sqlx::Error> {
        sqlx::query(
            r#"
            UPDATE wallet_maintenance
            SET status = 'OVERDUE', updated_on = NOW()
            WHERE user_id = $1 AND currency_type = $2
            "#
        )
        .bind(user_id)
        .bind(currency_type) 
        .execute(&mut **tx)
        .await?;

        Ok(())
    }

    pub async fn get_last_charged_date(
        tx: &mut Transaction<'_, Postgres>,
        user_id: i64,
        currency_type: &CurrencyType,
    ) -> Result<Option<chrono::DateTime<Utc>>, sqlx::Error> {
        let result = sqlx::query(
            r#"
            SELECT last_charged FROM wallet_maintenance 
            WHERE user_id = $1 AND currency_type = $2
            "#
        )
        .bind(user_id)
        .bind(currency_type)
        .fetch_optional(&mut **tx)
        .await?;
        if let Some(row) = result {
            let last_charged: Option<chrono::NaiveDateTime> = row.try_get("last_charged")?;
            Ok(last_charged.map(|dt| chrono::DateTime::from_naive_utc_and_offset(dt, chrono::Utc)))
        } else {
            Ok(None)
        }
    }

    pub async fn get_all_paginated(
        tx: &mut Transaction<'_, Postgres>,
        page: i64,
        page_size: i64,
    ) -> Result<(Vec<WalletMaintenance>, i64), sqlx::Error> {
        let page = page.max(1);
        let page_size = page_size.clamp(1, 100);
        let offset = (page - 1) * page_size;

        let count_row = sqlx::query(
            "SELECT COUNT(*) as total_count FROM wallet_maintenance"
        )
        .fetch_one(&mut **tx)
        .await?;
        let total_count: i64 = count_row.try_get("total_count")?;

        let rows = sqlx::query(
            r#"
            SELECT id, user_id, currency_type, balance, status, last_charged, created_on, updated_on
            FROM wallet_maintenance
            ORDER BY updated_on DESC, user_id ASC
            LIMIT $1 OFFSET $2
            "#
        )
        .bind(page_size)
        .bind(offset)
        .fetch_all(&mut **tx)
        .await?;

        let wallet_maintenances = rows.into_iter().map(|row| {
            Ok(WalletMaintenance {
                id: row.try_get("id")?,
                user_id: row.try_get("user_id")?,
                currency_type: row.try_get("currency_type")?,
                balance: row.try_get("balance")?,
                status: row.try_get("status")?,
                last_charged: row.try_get("last_charged")?,
                created_on: row.try_get("created_on")?,
                updated_on: row.try_get("updated_on")?,
            })
        }).collect::<Result<Vec<WalletMaintenance>, sqlx::Error>>()?;

        Ok((wallet_maintenances, total_count))
    }

    pub async fn get_by_status_paginated(
        tx: &mut Transaction<'_, Postgres>,
        status: DebtStatus,
        page: i64,
        page_size: i64,
    ) -> Result<(Vec<WalletMaintenance>, i64), sqlx::Error> {
        let page = page.max(1);
        let page_size = page_size.clamp(1, 100);
        let offset = (page - 1) * page_size;

        let count_row = sqlx::query(
            "SELECT COUNT(*) as total_count FROM wallet_maintenance WHERE status = $1"
        )
        .bind(&status)
        .fetch_one(&mut **tx)
        .await?;
        let total_count: i64 = count_row.try_get("total_count")?;

        let rows = sqlx::query(
            r#"
            SELECT id, user_id, currency_type, balance, status, last_charged, created_on, updated_on
            FROM wallet_maintenance
            WHERE status = $1
            ORDER BY updated_on DESC, user_id ASC
            LIMIT $2 OFFSET $3
            "#
        )
        .bind(&status)
        .bind(page_size)
        .bind(offset)
        .fetch_all(&mut **tx)
        .await?;

        let wallet_maintenances = rows.into_iter().map(|row| {
            Ok(WalletMaintenance {
                id: row.try_get("id")?,
                user_id: row.try_get("user_id")?,
                currency_type: row.try_get("currency_type")?,
                balance: row.try_get("balance")?,
                status: row.try_get("status")?,
                last_charged: row.try_get("last_charged")?,
                created_on: row.try_get("created_on")?,
                updated_on: row.try_get("updated_on")?,
            })
        }).collect::<Result<Vec<WalletMaintenance>, sqlx::Error>>()?;

        Ok((wallet_maintenances, total_count))
    }

    
}



