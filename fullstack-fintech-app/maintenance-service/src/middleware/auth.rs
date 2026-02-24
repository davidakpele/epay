use axum::{
    async_trait,
    extract::{FromRequestParts},
    http::{request::Parts, StatusCode, header},
    response::{IntoResponse, Response},
    Json,
};
use serde_json::json;

use crate::utils::jwt::{decode_token, Claims};

#[derive(Debug, Clone)]
pub struct AuthUser(pub Claims);

#[async_trait]
impl<S> FromRequestParts<S> for AuthUser
where
    S: Send + Sync,
{
    type Rejection = Response;

    async fn from_request_parts(parts: &mut Parts, _state: &S) -> Result<Self, Self::Rejection> {
        let token = parts
            .headers
            .get(header::AUTHORIZATION)
            .and_then(|h| h.to_str().ok())
            .and_then(|s| s.strip_prefix("Bearer "))
            .ok_or_else(|| {
                (
                    StatusCode::UNAUTHORIZED,
                    Json(json!(
                        { 
                            "title": "Authentication Error",
                            "details":"Something went wrong with authentication.",
                            "code": "generic_authentication_error",
                            "error": "Missing authorization token" 
                        }
                    )),
                )
                    .into_response()
            })?;

        match decode_token(token) {
            Ok(claims) => Ok(AuthUser(claims)),
            Err(e) => {
                let err_msg = e.to_string();
                if err_msg.contains("ExpiredSignature") {
                    Err((
                        StatusCode::UNAUTHORIZED,
                        Json(json!(
                            { 
                                "title": "Authentication Error",
                                "details":"Something went wrong with authentication.",
                                "code": "generic_authentication_error",
                                "error": "Token has expired" 
                            }
                        )),
                    )
                        .into_response())
                } else {
                    Err((
                        StatusCode::UNAUTHORIZED,
                        Json(json!(
                            { 
                                "title": "Authentication Error",
                                "details":"Something went wrong with authentication.",
                                "code": "generic_authentication_error",
                                "error": "Invalid token" 
                            }
                        )),
                    )
                        .into_response())
                }
            }
        }
    }
}

#[allow(dead_code)]
#[derive(Debug, Clone)]
pub struct AdminUser(pub Claims);

#[async_trait]
impl<S> FromRequestParts<S> for AdminUser
where
    S: Send + Sync,
{
    type Rejection = Response;

    async fn from_request_parts(parts: &mut Parts, state: &S) -> Result<Self, Self::Rejection> {
        let AuthUser(claims) = AuthUser::from_request_parts(parts, state).await?;

        if claims.is_admin {
            Ok(AdminUser(claims))
        } else {
            Err((
                StatusCode::FORBIDDEN,
                Json(json!(
                    { 
                        "title": "Authentication Error",
                        "details":"Something went wrong with authentication.",
                        "code": "generic_authentication_error",
                        "error": "Access denied: Admins only" 
                    }
                )),
            )
                .into_response())
        }
    }
}