// router/maintenance_router.rs
use axum::{
    Router,
    routing::{get, post},
    response::IntoResponse,
    http::StatusCode,
    Json, 
};
use serde_json::json;
use crate::controllers::maintenance_controller::MaintenanceController;
use crate::AppState;

async fn default_handler() -> impl IntoResponse {
    "Maintenance Service is up!"
}

async fn handler_404() -> impl IntoResponse {
    (
        StatusCode::NOT_FOUND,
        Json(json!({
            "error": "Not Found",
            "message": "The requested resource was not found"
        })),
    )
}

pub fn create_router() -> Router<AppState> {
    let maintenance_routes = Router::new()
        .route("/wallet/initialize", post(MaintenanceController::initialize_wallet))
        .route("/fee/pay", post(MaintenanceController::pay_fee))
        .route("/fee/overdue", post(MaintenanceController::mark_overdue))
        .route("/wallet/:user_id/:currency_type/last-charged", get(MaintenanceController::get_last_charged_date))
        .route("/wallet-maintenance", get(MaintenanceController::get_all_wallet_maintenance))
        .route("/wallet-maintenance/filter", get(MaintenanceController::get_wallet_maintenance_by_status));

    Router::new()
        .nest("/api/maintenance", maintenance_routes)
        .route("/health", get(|| async { "Maintenance Service OK" }))
        .route("/", get(default_handler))
        .fallback(handler_404)
}