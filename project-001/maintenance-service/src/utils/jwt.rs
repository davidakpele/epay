use chrono::{Utc, Duration};
use jsonwebtoken::{decode, encode, Algorithm, DecodingKey, EncodingKey, Header, Validation};
use serde::{Deserialize, Serialize};
use anyhow::{Result, anyhow};
use dotenv::dotenv;
use std::env;

#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct Claims {
    pub sub: i64,
    pub email: String,
    pub roles: Vec<String>,
    pub is_admin: bool,
    pub is_user: bool,
    pub exp: usize,
}

pub fn get_secret() -> Result<Vec<u8>> {
    dotenv().ok();
    env::var("JWT_SECRET")
        .map(|s| s.into_bytes())
        .map_err(|_| anyhow!("JWT_SECRET must be set in .env"))
}

pub fn generate_token(user_id: i64, email: String, is_admin: bool) -> Result<String> {
    let secret = get_secret()?;
    let expiration = Utc::now()
        .checked_add_signed(Duration::hours(24)) 
        .ok_or_else(|| anyhow!("Invalid expiration time"))?
        .timestamp() as usize;

    let claims = Claims {
        sub: user_id,
        email,
        roles: vec!["USER".to_string()],
        is_user: true,
        is_admin,
        exp: expiration,
    };

    let token = encode(
        &Header::default(),
        &claims,
        &EncodingKey::from_secret(&secret),
    )?;

    Ok(token)
}


pub fn decode_token(token: &str) -> Result<Claims> {
    let secret = get_secret()?;

    let token_data = decode::<Claims>(
        token,
        &DecodingKey::from_secret(&secret),
        &Validation::new(Algorithm::HS256),
    )?;
    
    Ok(token_data.claims)
}