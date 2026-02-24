use sqlx::{PgPool, postgres::PgPoolOptions};
use std::env;

pub async fn establish_connection() -> Result<PgPool, sqlx::Error> {
    let database_url = env::var("DATABASE_URL")
        .expect("DATABASE_URL must be set in environment or .env file");

    PgPoolOptions::new()
        .max_connections(10)
        .connect(&database_url)
        .await
}