// controllers/maintenance_controller.rs
use axum::{
    extract::{Json, State, Query},
    http::StatusCode,
    response::IntoResponse,
};
use bigdecimal::BigDecimal;
use serde::{Deserialize, Serialize};
use sqlx::{Postgres, Transaction};

use crate::{
    models::debt_collector::{CurrencyType, DebtStatus, WalletMaintenance},
    repositories::{
        maintenance_fee_history_repository::MaintenanceFeeHistoryRepository,
        wallet_maintenance_repository::WalletMaintenanceRepository,
    },
    AppState,
};

#[derive(Debug, Deserialize)]
pub struct InitializeWalletRequest {
    pub user_id: i64,
    pub currency_type: CurrencyType,
    pub balance: BigDecimal,
}

#[derive(Debug, Deserialize)]
pub struct PayFeeRequest {
    pub user_id: i64,
    pub currency_type: CurrencyType,
    pub fee_amount: BigDecimal,
}

#[derive(Debug, Deserialize)]
pub struct MarkOverdueRequest {
    pub user_id: i64,
    pub currency_type: CurrencyType,
    pub fee_amount: BigDecimal,
    pub reason: String,
}

#[derive(Debug, Serialize)]
pub struct LastChargedResponse {
    pub last_charged: Option<chrono::DateTime<chrono::Utc>>,
}

#[derive(Debug, Serialize)]
pub struct ApiResponse<T> {
    pub success: bool,
    pub data: Option<T>,
    pub message: String,
}

#[derive(Debug, Deserialize)]
pub struct PaginationParams {
    pub page: Option<i64>,
    pub page_size: Option<i64>,
}

#[derive(Debug, Deserialize)]
pub struct StatusFilterParams {
    pub status: DebtStatus,
    pub page: Option<i64>,
    pub page_size: Option<i64>,
}

#[derive(Debug, Serialize)]
pub struct PaginatedResponse<T> {
    pub data: Vec<T>,
    pub pagination: PaginationMeta,
}

#[derive(Debug, Serialize)]
pub struct PaginationMeta {
    pub page: i64,
    pub page_size: i64,
    pub total_count: i64,
    pub total_pages: i64,
}

pub struct MaintenanceController;

impl MaintenanceController {
    pub async fn initialize_wallet(
        State(state): State<AppState>,
        Json(payload): Json<InitializeWalletRequest>,
    ) -> impl IntoResponse {
        let mut tx = match state.pool.begin().await {
            Ok(tx) => tx,
            Err(e) => {
                return (
                    StatusCode::INTERNAL_SERVER_ERROR,
                    Json(ApiResponse {
                        success: false,
                        data: None::<()>,
                        message: format!("Failed to start transaction: {}", e),
                    }),
                )
            }
        };

        match WalletMaintenanceRepository::initialize_or_update(
            &mut tx,
            payload.user_id,
            &payload.currency_type,
            &payload.balance,
        )
        .await
        {
            Ok(()) => {
                if let Err(e) = tx.commit().await {
                    return (
                        StatusCode::INTERNAL_SERVER_ERROR,
                        Json(ApiResponse {
                            success: false,
                            data: None::<()>,
                            message: format!("Failed to commit transaction: {}", e),
                        }),
                    );
                }

                (
                    StatusCode::OK,
                    Json(ApiResponse {
                        success: true,
                        data: None::<()>,
                        message: "Wallet initialized/updated successfully".to_string(),
                    }),
                )
            }
            Err(e) => {
                let _ = tx.rollback().await;
                (
                    StatusCode::INTERNAL_SERVER_ERROR,
                    Json(ApiResponse {
                        success: false,
                        data: None::<()>,
                        message: format!("Failed to initialize wallet: {}", e),
                    }),
                )
            }
        }
    }

    pub async fn pay_fee(
        State(state): State<AppState>,
        Json(payload): Json<PayFeeRequest>,
    ) -> impl IntoResponse {
        let mut tx = match state.pool.begin().await {
            Ok(tx) => tx,
            Err(e) => {
                return (
                    StatusCode::INTERNAL_SERVER_ERROR,
                    Json(ApiResponse {
                        success: false,
                        data: None::<()>,
                        message: format!("Failed to start transaction: {}", e),
                    }),
                )
            }
        };

        match WalletMaintenanceRepository::deduct_fee(
            &mut tx,
            payload.user_id,
            &payload.currency_type,
            &payload.fee_amount,
        )
        .await
        {
            Ok(()) => {
                match MaintenanceFeeHistoryRepository::record_paid(
                    &mut tx,
                    payload.user_id,
                    &payload.currency_type,
                    &payload.fee_amount,
                )
                .await
                {
                    Ok(()) => {
                        if let Err(e) = tx.commit().await {
                            return (
                                StatusCode::INTERNAL_SERVER_ERROR,
                                Json(ApiResponse {
                                    success: false,
                                    data: None::<()>,
                                    message: format!("Failed to commit transaction: {}", e),
                                }),
                            );
                        }

                        (
                            StatusCode::OK,
                            Json(ApiResponse {
                                success: true,
                                data: None::<()>,
                                message: "Fee paid successfully".to_string(),
                            }),
                        )
                    }
                    Err(e) => {
                        let _ = tx.rollback().await;
                        (
                            StatusCode::INTERNAL_SERVER_ERROR,
                            Json(ApiResponse {
                                success: false,
                                data: None::<()>,
                                message: format!("Failed to record payment history: {}", e),
                            }),
                        )
                    }
                }
            }
            Err(e) => {
                let _ = tx.rollback().await;
                (
                    StatusCode::INTERNAL_SERVER_ERROR,
                    Json(ApiResponse {
                        success: false,
                        data: None::<()>,
                        message: format!("Failed to deduct fee: {}", e),
                    }),
                )
            }
        }
    }

    pub async fn mark_overdue(
        State(state): State<AppState>,
        Json(payload): Json<MarkOverdueRequest>,
    ) -> impl IntoResponse {
        let mut tx = match state.pool.begin().await {
            Ok(tx) => tx,
            Err(e) => {
                return (
                    StatusCode::INTERNAL_SERVER_ERROR,
                    Json(ApiResponse {
                        success: false,
                        data: None::<()>,
                        message: format!("Failed to start transaction: {}", e),
                    }),
                )
            }
        };

        match WalletMaintenanceRepository::mark_overdue(
            &mut tx,
            payload.user_id,
            &payload.currency_type,
        )
        .await
        {
            Ok(()) => {
                match MaintenanceFeeHistoryRepository::record_overdue(
                    &mut tx,
                    payload.user_id,
                    &payload.currency_type,
                    &payload.fee_amount,
                    &payload.reason,
                )
                .await
                {
                    Ok(()) => {
                        if let Err(e) = tx.commit().await {
                            return (
                                StatusCode::INTERNAL_SERVER_ERROR,
                                Json(ApiResponse {
                                    success: false,
                                    data: None::<()>,
                                    message: format!("Failed to commit transaction: {}", e),
                                }),
                            );
                        }

                        (
                            StatusCode::OK,
                            Json(ApiResponse {
                                success: true,
                                data: None::<()>,
                                message: "Marked as overdue successfully".to_string(),
                            }),
                        )
                    }
                    Err(e) => {
                        let _ = tx.rollback().await;
                        (
                            StatusCode::INTERNAL_SERVER_ERROR,
                            Json(ApiResponse {
                                success: false,
                                data: None::<()>,
                                message: format!("Failed to record overdue history: {}", e),
                            }),
                        )
                    }
                }
            }
            Err(e) => {
                let _ = tx.rollback().await;
                (
                    StatusCode::INTERNAL_SERVER_ERROR,
                    Json(ApiResponse {
                        success: false,
                        data: None::<()>,
                        message: format!("Failed to mark wallet as overdue: {}", e),
                    }),
                )
            }
        }
    }

    pub async fn get_last_charged_date(
        State(state): State<AppState>,
        axum::extract::Path((user_id, currency_type)): axum::extract::Path<(i64, CurrencyType)>,
    ) -> impl IntoResponse {
        let mut tx = match state.pool.begin().await {
            Ok(tx) => tx,
            Err(e) => {
                return (
                    StatusCode::INTERNAL_SERVER_ERROR,
                    Json(ApiResponse {
                        success: false,
                        data: None::<LastChargedResponse>,
                        message: format!("Failed to start transaction: {}", e),
                    }),
                )
            }
        };

        match WalletMaintenanceRepository::get_last_charged_date(
            &mut tx,
            user_id,
            &currency_type,
        )
        .await
        {
            Ok(last_charged) => {
                let _ = tx.rollback().await; 

                (
                    StatusCode::OK,
                    Json(ApiResponse {
                        success: true,
                        data: Some(LastChargedResponse { last_charged }),
                        message: "Last charged date retrieved successfully".to_string(),
                    }),
                )
            }
            Err(e) => {
                let _ = tx.rollback().await;
                (
                    StatusCode::INTERNAL_SERVER_ERROR,
                    Json(ApiResponse {
                        success: false,
                        data: None::<LastChargedResponse>,
                        message: format!("Failed to get last charged date: {}", e),
                    }),
                )
            }
        }
    }

     pub async fn get_all_wallet_maintenance(
        State(state): State<AppState>,
        Query(params): Query<PaginationParams>,
    ) -> impl IntoResponse {
        let page = params.page.unwrap_or(1);
        let page_size = params.page_size.unwrap_or(20);

        let mut tx = match state.pool.begin().await {
            Ok(tx) => tx,
            Err(e) => {
                return (StatusCode::INTERNAL_SERVER_ERROR, Json(ApiResponse {
                    success: false,
                    data: None::<PaginatedResponse<WalletMaintenance>>,
                    message: format!("Failed to start transaction: {}", e),
                }))
            }
        };

        match WalletMaintenanceRepository::get_all_paginated(&mut tx, page, page_size).await {
            Ok((data, total_count)) => {
                let _ = tx.rollback().await;
                let total_pages = (total_count as f64 / page_size as f64).ceil() as i64;
                
                let response = PaginatedResponse {
                    data,
                    pagination: PaginationMeta { page, page_size, total_count, total_pages },
                };

                (StatusCode::OK, Json(ApiResponse {
                    success: true,
                    data: Some(response),
                    message: "Wallet maintenance records retrieved successfully".to_string(),
                }))
            }
            Err(e) => {
                let _ = tx.rollback().await;
                (StatusCode::INTERNAL_SERVER_ERROR, Json(ApiResponse {
                    success: false,
                    data: None::<PaginatedResponse<WalletMaintenance>>,
                    message: format!("Failed to retrieve records: {}", e),
                }))
            }
        }
    }

    pub async fn get_wallet_maintenance_by_status(
        State(state): State<AppState>,
        Query(params): Query<StatusFilterParams>,
    ) -> impl IntoResponse {
        let page = params.page.unwrap_or(1);
        let page_size = params.page_size.unwrap_or(20);

        let mut tx = match state.pool.begin().await {
            Ok(tx) => tx,
            Err(e) => {
                return (StatusCode::INTERNAL_SERVER_ERROR, Json(ApiResponse {
                    success: false,
                    data: None::<PaginatedResponse<WalletMaintenance>>,
                    message: format!("Failed to start transaction: {}", e),
                }))
            }
        };

        match WalletMaintenanceRepository::get_by_status_paginated(&mut tx, params.status, page, page_size).await {
            Ok((data, total_count)) => {
                let _ = tx.rollback().await;
                let total_pages = (total_count as f64 / page_size as f64).ceil() as i64;
                
                let response = PaginatedResponse {
                    data,
                    pagination: PaginationMeta { page, page_size, total_count, total_pages },
                };

                (StatusCode::OK, Json(ApiResponse {
                    success: true,
                    data: Some(response),
                    message: "Wallet maintenance records retrieved successfully".to_string(),
                }))
            }
            Err(e) => {
                let _ = tx.rollback().await;
                (StatusCode::INTERNAL_SERVER_ERROR, Json(ApiResponse {
                    success: false,
                    data: None::<PaginatedResponse<WalletMaintenance>>,
                    message: format!("Failed to retrieve records: {}", e),
                }))
            }
        }
    }
}