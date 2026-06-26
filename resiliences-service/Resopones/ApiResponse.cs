using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace resiliences_service.Resopones
{
   public static class ApiResponse
    {
        public static object Success(object? data = null, string message = "Success") => new
        {
            success = true,
            message,
            data
        };

        public static object Error(string message, object? errors = null) => new
        {
            success = false,
            message,
            errors
        };

        // ── Security Errors ──
        public static object Unauthorized(string reason = "Authentication required") => new
        {
            success = false,
            message = "Unauthorized",
            reason
        };

        public static object Forbidden(string reason = "You do not have permission to access this resource") => new
        {
            success = false,
            message = "Forbidden",
            reason
        };

        public static object TokenExpired() => new
        {
            success = false,
            message = "Unauthorized",
            reason = "Token has expired. Please login again"
        };

        public static object TokenInvalid() => new
        {
            success = false,
            message = "Unauthorized",
            reason = "Token is invalid or malformed"
        };

        public static object MissingClaim(string claimName) => new
        {
            success = false,
            message = "Unauthorized",
            reason = $"Required claim '{claimName}' is missing from token"
        };

        public static object NotFound(string resource = "Resource") => new
        {
            success = false,
            message = "Not Found",
            reason = $"{resource} was not found"
        };

        public static object ValidationError(object errors) => new
        {
            success = false,
            message = "Validation Failed",
            errors
        };

        public static object ServerError(string reason = "An unexpected error occurred") => new
        {
            success = false,
            message = "Internal Server Error",
            reason
        };
    }
}