use std::time::{Duration, Instant};
use reqwest::blocking::Client;
use reqwest::header::{HeaderMap, HeaderValue, AUTHORIZATION, USER_AGENT, CONTENT_TYPE};
use std::thread;
use std::sync::{Arc, Mutex};
use std::fs::File;
use std::io::Write;
use serde_json::json;

#[derive(Debug, Clone)]
pub struct TestResult {
    pub endpoint: String,
    pub attack_type: String,
    pub status_code: u16,
    pub response_time: Duration,
    pub blocked: bool,
    pub block_reason: String,
}

pub struct SecurityTester {
    client: Client,
    base_url: String,
    results: Arc<Mutex<Vec<TestResult>>>,
}

impl SecurityTester {
    pub fn new(base_url: &str) -> Self {
        let client = Client::builder()
            .timeout(Duration::from_secs(10))
            .danger_accept_invalid_certs(true)
            .build()
            .expect("Failed to create HTTP client");
        
        SecurityTester {
            client,
            base_url: base_url.to_string(),
            results: Arc::new(Mutex::new(Vec::new())),
        }
    }
    
    fn record_result(&self, result: TestResult) {
        let mut results = self.results.lock().unwrap();
        results.push(result);
    }
    
    fn analyze_response(&self, status: u16, attack_type: &str, endpoint: &str) -> (bool, String) {
        let attack_category = if attack_type.starts_with("SQLi:") {
            "SQLi"
        } else if attack_type.starts_with("XSS:") {
            "XSS"
        } else if attack_type.starts_with("Path:") {
            "Path"
        } else if attack_type.starts_with("Bot UA:") {
            "Bot"
        } else if attack_type.starts_with("Invalid JWT:") {
            "JWT"
        } else if attack_type == "Rate Limit Test" {
            "RateLimit"
        } else {
            "Unknown"
        };
        
        match status {
            // SUCCESS CODES (typically NOT blocked)
            200 => {
                if attack_category != "RateLimit" && attack_category != "Unknown" {
                    (false, format!("✅ Request successful - Attack {} was accepted by backend", attack_category))
                } else {
                    (false, "✅ Request successful".to_string())
                }
            }
            201 => (false, "✅ Created successfully".to_string()),
            202 => (false, "✅ Accepted".to_string()),
            204 => (false, "✅ No content".to_string()),
            
            // REDIRECTION CODES (typically NOT blocked, but unusual for attacks)
            301 => (false, "⚠️ Permanent redirect - Attack may have been redirected".to_string()),
            302 => (false, "⚠️ Temporary redirect - Attack may have been redirected".to_string()),
            303 => (false, "⚠️ See other redirect".to_string()),
            304 => (false, "✅ Not modified".to_string()),
            307 => (false, "⚠️ Temporary redirect (preserves method)".to_string()),
            308 => (false, "⚠️ Permanent redirect (preserves method)".to_string()),
            
            // CLIENT ERROR CODES (typically BLOCKED for attacks)
            400 => {
                if attack_category == "RateLimit" {
                    (false, "⚠️ Bad request - Not rate limited but rejected".to_string())
                } else {
                    (true, "🛡️ Blocked - Bad request (WAF/Validation)".to_string())
                }
            }
            401 => {
                if attack_category == "JWT" || attack_type.contains("JWT") {
                    (true, "🛡️ Blocked - Authentication required (JWT validation working)".to_string())
                } else if attack_category == "RateLimit" {
                    (false, "⚠️ Unauthorized - Not rate limited but auth failed".to_string())
                } else {
                    (true, "🛡️ Blocked - Authentication required".to_string())
                }
            }
            403 => {
                if attack_category == "RateLimit" {
                    // For rate limiting, 403 is not the ideal response but still blocks
                    (true, "🛡️ Blocked - Forbidden (may be rate limiting or security rule)".to_string())
                } else {
                    (true, "🛡️ Blocked - Forbidden (WAF/Security rule)".to_string())
                }
            }
            404 => {
                if endpoint.contains("..") || endpoint.contains("etc") || endpoint.contains(".git") || endpoint.contains(".env") {
                    (true, "🛡️ Blocked - Not found (likely path traversal prevented)".to_string())
                } else {
                    (true, "🛡️ Blocked - Resource not found".to_string())
                }
            }
            405 => (true, "🛡️ Blocked - Method not allowed".to_string()),
            406 => (true, "🛡️ Blocked - Not acceptable".to_string()),
            407 => (true, "🛡️ Blocked - Proxy authentication required".to_string()),
            408 => (false, "⚠️ Request timeout".to_string()),
            409 => (false, "⚠️ Conflict".to_string()),
            410 => (true, "🛡️ Blocked - Gone".to_string()),
            411 => (true, "🛡️ Blocked - Length required".to_string()),
            412 => (true, "🛡️ Blocked - Precondition failed".to_string()),
            413 => (true, "🛡️ Blocked - Payload too large".to_string()),
            414 => (true, "🛡️ Blocked - URI too long".to_string()),
            415 => (true, "🛡️ Blocked - Unsupported media type".to_string()),
            416 => (true, "🛡️ Blocked - Range not satisfiable".to_string()),
            417 => (true, "🛡️ Blocked - Expectation failed".to_string()),
            418 => (true, "🛡️ Blocked - I'm a teapot".to_string()), // Easter egg
            421 => (true, "🛡️ Blocked - Misdirected request".to_string()),
            422 => (true, "🛡️ Blocked - Unprocessable entity".to_string()),
            423 => (true, "🛡️ Blocked - Locked".to_string()),
            424 => (true, "🛡️ Blocked - Failed dependency".to_string()),
            425 => (false, "⚠️ Too early".to_string()),
            426 => (true, "🛡️ Blocked - Upgrade required".to_string()),
            428 => (true, "🛡️ Blocked - Precondition required".to_string()),
            429 => {
                if attack_category == "RateLimit" {
                    (true, "⏱️ Rate Limited - Too many requests (correct behavior)".to_string())
                } else {
                    (true, "⏱️ Rate Limited - Too many requests".to_string())
                }
            }
            431 => (true, "🛡️ Blocked - Request header fields too large".to_string()),
            451 => (true, "🛡️ Blocked - Unavailable for legal reasons".to_string()),
            
            // SERVER ERROR CODES (typically NOT blocked - attack reached backend)
            500 => {
                if attack_category != "RateLimit" && attack_category != "Unknown" {
                    (false, format!("💥 Server error - Attack {} caused backend crash!", attack_category))
                } else {
                    (false, "💥 Server error - Backend crash".to_string())
                }
            }
            501 => (false, "⚠️ Not implemented".to_string()),
            502 => (false, "⚠️ Bad gateway".to_string()),
            503 => (false, "⚠️ Service unavailable".to_string()),
            504 => (false, "⚠️ Gateway timeout".to_string()),
            505 => (false, "⚠️ HTTP version not supported".to_string()),
            506 => (false, "⚠️ Variant also negotiates".to_string()),
            507 => (false, "⚠️ Insufficient storage".to_string()),
            508 => (false, "⚠️ Loop detected".to_string()),
            510 => (false, "⚠️ Not extended".to_string()),
            511 => (false, "⚠️ Network authentication required".to_string()),
            
            // UNKNOWN STATUS CODE
            _ => {
                if status >= 100 && status < 200 {
                    (false, format!("ℹ️ Informational status {}", status))
                } else if status >= 200 && status < 300 {
                    (false, format!("✅ Success status {}", status))
                } else if status >= 300 && status < 400 {
                    (false, format!("⚠️ Redirection status {}", status))
                } else if status >= 400 && status < 500 {
                    (true, format!("🛡️ Client error status {} (blocked)", status))
                } else if status >= 500 && status < 600 {
                    (false, format!("💥 Server error status {}", status))
                } else {
                    (false, format!("❓ Unknown status code {}", status))
                }
            }
        }
    }
        
    pub fn generate_report(&self) {
        let results = self.results.lock().unwrap();
        let mut file = File::create("security_test_report.md").expect("Failed to create report file");
        
        writeln!(file, "# NGINX Security Test Report\n").unwrap();
        writeln!(file, "## Summary\n").unwrap();
        
        let total_tests = results.len();
        let blocked_tests = results.iter().filter(|r| r.blocked).count();
        let passed_tests = total_tests - blocked_tests;
        
        writeln!(file, "- **Total Tests**: {}", total_tests).unwrap();
        writeln!(file, "- **Blocked Attacks**: {}", blocked_tests).unwrap();
        writeln!(file, "- **Passed Through**: {}", passed_tests).unwrap();
        writeln!(file, "- **Block Rate**: {:.2}%\n", (blocked_tests as f64 / total_tests as f64) * 100.0).unwrap();
        
        // Calculate statistics by attack type
        let mut sqli_tests = 0;
        let mut sqli_blocked = 0;
        let mut xss_tests = 0;
        let mut xss_blocked = 0;
        let mut path_tests = 0;
        let mut path_blocked = 0;
        let mut rate_tests = 0;
        let mut rate_blocked = 0;
        let mut bot_tests = 0;
        let mut bot_blocked = 0;
        let mut jwt_tests = 0;
        let mut jwt_blocked = 0;
        
        for result in results.iter() {
            if result.attack_type.starts_with("SQLi:") {
                sqli_tests += 1;
                if result.blocked { sqli_blocked += 1; }
            } else if result.attack_type.starts_with("XSS:") {
                xss_tests += 1;
                if result.blocked { xss_blocked += 1; }
            } else if result.attack_type.starts_with("Path:") {
                path_tests += 1;
                if result.blocked { path_blocked += 1; }
            } else if result.attack_type == "Rate Limit Test" {
                rate_tests += 1;
                if result.blocked { rate_blocked += 1; }
            } else if result.attack_type.starts_with("Bot UA:") {
                bot_tests += 1;
                if result.blocked { bot_blocked += 1; }
            } else if result.attack_type.starts_with("Invalid JWT:") {
                jwt_tests += 1;
                if result.blocked { jwt_blocked += 1; }
            }
        }
        
        writeln!(file, "## Security Effectiveness by Attack Type\n").unwrap();
        writeln!(file, "| Attack Type | Tests | Blocked | Block Rate |").unwrap();
        writeln!(file, "|-------------|-------|---------|------------|").unwrap();
        
        if sqli_tests > 0 {
            writeln!(file, "| SQL Injection | {} | {} | {:.1}% |", 
                sqli_tests, sqli_blocked, (sqli_blocked as f64 / sqli_tests as f64) * 100.0).unwrap();
        }
        if xss_tests > 0 {
            writeln!(file, "| XSS | {} | {} | {:.1}% |", 
                xss_tests, xss_blocked, (xss_blocked as f64 / xss_tests as f64) * 100.0).unwrap();
        }
        if path_tests > 0 {
            writeln!(file, "| Path Traversal | {} | {} | {:.1}% |", 
                path_tests, path_blocked, (path_blocked as f64 / path_tests as f64) * 100.0).unwrap();
        }
        if rate_tests > 0 {
            writeln!(file, "| Rate Limiting | {} | {} | {:.1}% |", 
                rate_tests, rate_blocked, (rate_blocked as f64 / rate_tests as f64) * 100.0).unwrap();
        }
        if bot_tests > 0 {
            writeln!(file, "| Bot Detection | {} | {} | {:.1}% |", 
                bot_tests, bot_blocked, (bot_blocked as f64 / bot_tests as f64) * 100.0).unwrap();
        }
        if jwt_tests > 0 {
            writeln!(file, "| JWT Validation | {} | {} | {:.1}% |", 
                jwt_tests, jwt_blocked, (jwt_blocked as f64 / jwt_tests as f64) * 100.0).unwrap();
        }
        
        writeln!(file, "\n## Detailed Results\n").unwrap();
        writeln!(file, "| Endpoint | Attack Type | Status | Blocked | Response Time | Reason |").unwrap();
        writeln!(file, "|----------|-------------|--------|---------|---------------|--------|").unwrap();
        
        for result in results.iter() {
            let status_emoji = if result.blocked { "✅" } else { "❌" };
            let reason_short = if result.block_reason.len() > 30 {
                format!("{}...", &result.block_reason[0..30])
            } else {
                result.block_reason.clone()
            };
            
            writeln!(file, "| {} | {} | {} | {} | {:.2}ms | {} |", 
                result.endpoint, 
                result.attack_type,
                result.status_code,
                status_emoji,
                result.response_time.as_millis(),
                reason_short
            ).unwrap();
        }
        
        writeln!(file, "\n## Status Code Analysis\n").unwrap();
        
        // Count status codes
        let mut status_counts = std::collections::HashMap::new();
        for result in results.iter() {
            *status_counts.entry(result.status_code).or_insert(0) += 1;
        }
        
        writeln!(file, "| Status Code | Count | Description |").unwrap();
        writeln!(file, "|-------------|-------|-------------|").unwrap();
        
        let mut sorted_statuses: Vec<_> = status_counts.iter().collect();
        // Fixed: Added & to destructure the reference
        sorted_statuses.sort_by_key(|&(code, _)| code);
        
        for (&code, &count) in sorted_statuses {
            let description = match code {
                200 => "OK - Success",
                201 => "Created",
                202 => "Accepted",
                204 => "No Content",
                301 => "Moved Permanently",
                302 => "Found",
                304 => "Not Modified",
                307 => "Temporary Redirect",
                308 => "Permanent Redirect",
                400 => "Bad Request",
                401 => "Unauthorized",
                403 => "Forbidden",
                404 => "Not Found",
                405 => "Method Not Allowed",
                408 => "Request Timeout",
                429 => "Too Many Requests",
                500 => "Internal Server Error",
                502 => "Bad Gateway",
                503 => "Service Unavailable",
                504 => "Gateway Timeout",
                _ => "Unknown",
            };
            
            writeln!(file, "| {} | {} | {} |", code, count, description).unwrap();
        }
        
        writeln!(file, "\n## Security Analysis & Recommendations\n").unwrap();
        
        // Analyze specific issues
        let mut issues = Vec::<String>::new();
        
        // Check for endpoints using rate limiting instead of security blocking
        let rate_limited_attacks: Vec<_> = results.iter()
            .filter(|r| r.status_code == 429 && (r.attack_type.starts_with("SQLi:") || r.attack_type.starts_with("XSS:")))
            .collect();
        
        if !rate_limited_attacks.is_empty() {
            issues.push("⚠️ **Rate Limiting Used as Security Control**: Some SQLi/XSS attacks are returning 429 (rate limited) instead of 403/400 (security blocked). Attackers could bypass this by sending attacks slowly.".to_string());
            for attack in &rate_limited_attacks {
                issues.push(format!("  - {} on {} - Rate limited instead of security blocked", 
                    attack.attack_type, attack.endpoint));
            }
        }
        
        // Check for 500 errors on security tests
        let server_error_attacks: Vec<_> = results.iter()
            .filter(|r| r.status_code == 500)
            .collect();
        
        if !server_error_attacks.is_empty() {
            issues.push("🚨 **Server Errors on Attacks**: Some attacks are causing 500 Internal Server Errors, indicating they reached the backend and caused issues:".to_string());
            for attack in &server_error_attacks {
                issues.push(format!("  - {} on {} - Caused server error", 
                    attack.attack_type, attack.endpoint));
            }
        }
        
        // Check for successful attacks (200 status)
        let successful_attacks: Vec<_> = results.iter()
            .filter(|r| r.status_code == 200 && (r.attack_type.starts_with("SQLi:") || 
                                                r.attack_type.starts_with("XSS:") || 
                                                r.attack_type.starts_with("Path:") ||
                                                r.attack_type.starts_with("Invalid JWT:") ||
                                                r.attack_type.starts_with("Bot UA:")))
            .collect();
        
        if !successful_attacks.is_empty() {
            issues.push("🔥 **CRITICAL: Attacks Successfully Processed**: Some attacks returned 200 OK, meaning they were accepted by the backend:".to_string());
            for attack in &successful_attacks {
                issues.push(format!("  - {} on {} - Attack was accepted", 
                    attack.attack_type, attack.endpoint));
            }
        }
        
        // Check rate limiting effectiveness
        let login_rate_limits: Vec<_> = results.iter()
            .filter(|r| r.endpoint == "/api/auth/login" && r.attack_type == "Rate Limit Test")
            .collect();
        
        if !login_rate_limits.is_empty() {
            let blocked_count = login_rate_limits.iter().filter(|r| r.blocked).count();
            let total_count = login_rate_limits.len();
            let block_rate = (blocked_count as f64 / total_count as f64) * 100.0;
            
            if block_rate < 50.0 {
                issues.push(format!("⚠️ **Weak Rate Limiting on Login**: /api/auth/login only blocked {:.1}% of rate limit tests.", block_rate));
            }
        }
        
        // Check for unusual status codes
        let unusual_statuses: Vec<_> = results.iter()
            .filter(|r| {
                matches!(r.status_code, 
                    418 | // I'm a teapot
                    451 | // Unavailable for legal reasons
                    506 | // Variant also negotiates
                    507 | // Insufficient storage
                    508   // Loop detected
                )
            })
            .collect();
        
        if !unusual_statuses.is_empty() {
            issues.push("🔍 **Unusual Status Codes Detected**:".to_string());
            for result in &unusual_statuses {
                issues.push(format!("  - {} {} returned status {}", 
                    result.attack_type, result.endpoint, result.status_code));
            }
        }
        
        if !issues.is_empty() {
            writeln!(file, "### Security Issues Found:\n").unwrap();
            for issue in &issues {
                writeln!(file, "{}", issue).unwrap();
            }
            writeln!(file).unwrap();
        } else {
            writeln!(file, "✅ No critical security issues detected!\n").unwrap();
        }
        
        // List all vulnerabilities that passed through
        let vulnerabilities: Vec<&TestResult> = results.iter().filter(|r| !r.blocked && r.status_code != 404).collect();
        if !vulnerabilities.is_empty() {
            writeln!(file, "### Detailed List of Unblocked Attacks:\n").unwrap();
            for vuln in vulnerabilities {
                writeln!(file, "1. **{}** on `{}` - Status: {} - {}", 
                    vuln.attack_type, vuln.endpoint, vuln.status_code, vuln.block_reason).unwrap();
            }
        } else {
            writeln!(file, "✅ All security measures are working correctly!\n").unwrap();
        }
        
        // Generate recommendations based on findings
        writeln!(file, "### Recommendations:\n").unwrap();
        
        let critical_attacks = results.iter()
            .filter(|r| r.status_code == 200 && !r.attack_type.contains("Rate Limit"))
            .count();
        
        if critical_attacks > 0 {
            writeln!(file, "🚨 **HIGH PRIORITY**: {} critical attacks reached your backend. Implement WAF rules immediately.", critical_attacks).unwrap();
        }
        
        let server_errors = results.iter().filter(|r| r.status_code == 500).count();
        if server_errors > 0 {
            writeln!(file, "⚠️ **MEDIUM PRIORITY**: {} attacks caused server errors. Implement input validation and error handling.", server_errors).unwrap();
        }
        
        let rate_limit_issues = results.iter()
            .filter(|r| r.attack_type == "Rate Limit Test" && !r.blocked)
            .count();
        if rate_limit_issues > 0 {
            writeln!(file, "⚠️ **MEDIUM PRIORITY**: Rate limiting needs improvement. {} requests bypassed rate limits.", rate_limit_issues).unwrap();
        }
        
        println!("✅ Report generated: security_test_report.md");
    }
    
    pub fn test_sql_injection(&self) {
        println!("\n🔍 Testing SQL Injection Attacks");
        
        let endpoints = vec![
            "/api/wallet/",
            "/api/deposit/",
            "/api/withdrawals/",
            "/api/user/",
        ];
        
        let sql_payloads = vec![
            "union select 1,2,3",
            "insert into users values('admin','password')",
            "delete from users where 1=1",
            "drop table users",
            "exec sp_configure",
            "1' OR '1'='1",
            "admin'--",
            "1' UNION SELECT NULL,NULL--",
        ];
        
        for endpoint in endpoints {
            for payload in &sql_payloads {
                let url = format!("{}{}?id={}", self.base_url, endpoint, payload);
                let start = Instant::now();
                
                let response = self.client.get(&url)
                    .header(USER_AGENT, "Security-Tester/1.0")
                    .send();
                
                let response_time = start.elapsed();
                
                match response {
                    Ok(resp) => {
                        let status = resp.status();
                        let status_code = status.as_u16();
                        
                        let attack_type = format!("SQLi: {}", payload);
                        let (blocked, reason) = self.analyze_response(status_code, &attack_type, endpoint);
                        
                        let result = TestResult {
                            endpoint: endpoint.to_string(),
                            attack_type: attack_type.clone(),
                            status_code,
                            response_time,
                            blocked,
                            block_reason: reason.clone(),
                        };
                        
                        let icon = if blocked { "🛡️" } else { "⚠️" };
                        println!("{} {} - SQLi '{}': Status {} - {}ms - {}",
                            icon, endpoint, payload, status_code,
                            response_time.as_millis(), reason);
                        
                        self.record_result(result);
                    }
                    Err(e) => {
                        println!("❌ {} - SQLi '{}': Failed - {}", endpoint, payload, e);
                    }
                }
                
                thread::sleep(Duration::from_millis(100));
            }
        }
    }
    
    pub fn test_xss_attacks(&self) {
        println!("\n🔍 Testing XSS Attacks");
        
        let endpoints = vec![
            "/api/wallet/",
            "/api/deposit/",
            "/api/user/",
        ];
        
        let xss_payloads = vec![
            "<script>alert('xss')</script>",
            "javascript:alert('xss')",
            "onerror=alert('xss')",
            "onload=alert('xss')",
            "<img src=x onerror=alert('xss')>",
            "<iframe src=javascript:alert('xss')>",
            "base64_decode",
            "eval(String.fromCharCode",
            "document.cookie",
        ];
        
        for endpoint in endpoints {
            for payload in &xss_payloads {
                let url = format!("{}{}?search={}", self.base_url, endpoint, payload);
                let start = Instant::now();
                
                let response = self.client.get(&url)
                    .header(USER_AGENT, "Security-Tester/1.0")
                    .send();
                
                let response_time = start.elapsed();
                
                match response {
                    Ok(resp) => {
                        let status = resp.status();
                        let status_code = status.as_u16();
                        
                        let attack_type = format!("XSS: {}", payload);
                        let (blocked, reason) = self.analyze_response(status_code, &attack_type, endpoint);
                        
                        let result = TestResult {
                            endpoint: endpoint.to_string(),
                            attack_type: attack_type.clone(),
                            status_code,
                            response_time,
                            blocked,
                            block_reason: reason.clone(),
                        };
                        
                        let icon = if blocked { "🛡️" } else { "⚠️" };
                        println!("{} {} - XSS '{}': Status {} - {}ms - {}",
                            icon, endpoint, payload, status_code,
                            response_time.as_millis(), reason);
                        
                        self.record_result(result);
                    }
                    Err(e) => {
                        println!("❌ {} - XSS '{}': Failed - {}", endpoint, payload, e);
                    }
                }
                
                thread::sleep(Duration::from_millis(100));
            }
        }
    }
    
    pub fn test_path_traversal(&self) {
        println!("\n🔍 Testing Path Traversal Attacks");
        
        let traversal_payloads = vec![
            "..\\..\\..\\windows\\system.ini",
            "....//....//etc/passwd",
            ".git/config",
            ".env",
            "/proc/self/environ",
        ];
        
        for payload in &traversal_payloads {
            let url = format!("{}/api/user/{}", self.base_url, payload);
            let start = Instant::now();
            
            let response = self.client.get(&url)
                .header(USER_AGENT, "Security-Tester/1.0")
                .send();
            
            let response_time = start.elapsed();
            
            match response {
                Ok(resp) => {
                    let status = resp.status();
                    let status_code = status.as_u16();
                    
                    let attack_type = format!("Path: {}", payload);
                    let (blocked, reason) = self.analyze_response(status_code, &attack_type, &url);
                    
                    let result = TestResult {
                        endpoint: "/api/user/".to_string(),
                        attack_type: attack_type.clone(),
                        status_code,
                        response_time,
                        blocked,
                        block_reason: reason.clone(),
                    };
                    
                    let icon = if blocked { "🛡️" } else { "⚠️" };
                    println!("{} Path '{}': Status {} - {}ms - {}",
                        icon, payload, status_code,
                        response_time.as_millis(), reason);
                    
                    self.record_result(result);
                }
                Err(e) => {
                    println!("❌ Path '{}': Failed - {}", payload, e);
                }
            }
        }
    }
    
    pub fn test_rate_limiting(&self) {
        println!("\n🔍 Testing Rate Limiting");
        
        let endpoints = vec![
            "/api/auth/login",
            "/api/wallet/balance",
            "/api/deposit/",
        ];
        
        for endpoint in endpoints {
            println!("\n📊 Testing rate limit on {}:", endpoint);
            
            let mut success_count = 0;
            let mut blocked_count = 0;
            let mut rate_limit_count = 0;
            let mut other_block_count = 0;
            
            for i in 1..=30 {
                let url = format!("{}{}", self.base_url, endpoint);
                let start = Instant::now();
                
                let response = if endpoint == "/api/auth/login" {
                    let payload = json!({
                        "username": "testuser",
                        "password": "testpass"
                    });
                    
                    self.client.post(&url)
                        .header(USER_AGENT, "Security-Tester/1.0")
                        .header(CONTENT_TYPE, "application/json")
                        .json(&payload)
                        .send()
                } else {
                    self.client.get(&url)
                        .header(USER_AGENT, "Security-Tester/1.0")
                        .send()
                };
                
                let response_time = start.elapsed();
                
                match response {
                    Ok(resp) => {
                        let status = resp.status();
                        let status_code = status.as_u16();
                        
                        let attack_type = "Rate Limit Test".to_string();
                        let (blocked, reason) = self.analyze_response(status_code, &attack_type, endpoint);
                        
                        if blocked {
                            blocked_count += 1;
                            if status_code == 429 {
                                rate_limit_count += 1;
                                println!("⏱️  Request {}: RATE LIMITED (Status: {}) - {}", i, status, reason);
                            } else {
                                other_block_count += 1;
                                println!("🛡️  Request {}: BLOCKED (Status: {}) - {}", i, status, reason);
                            }
                        } else {
                            success_count += 1;
                            println!("⚠️  Request {}: PASSED THROUGH (Status: {}) - {}", i, status, reason);
                        }
                        
                        let result = TestResult {
                            endpoint: endpoint.to_string(),
                            attack_type,
                            status_code,
                            response_time,
                            blocked,
                            block_reason: reason,
                        };
                        
                        self.record_result(result);
                    }
                    Err(e) => {
                        println!("❌ Request {}: Error - {}", i, e);
                        
                        // Record error as a test result
                        let result = TestResult {
                            endpoint: endpoint.to_string(),
                            attack_type: "Rate Limit Test".to_string(),
                            status_code: 0,
                            response_time,
                            blocked: false,
                            block_reason: format!("Request failed: {}", e),
                        };
                        self.record_result(result);
                    }
                }
                
                thread::sleep(Duration::from_millis(50));
            }
            
            println!("\n📈 Results for {}:", endpoint);
            println!("  ✅ Passed through: {}", success_count);
            println!("  ⏱️  Rate limited: {}", rate_limit_count);
            println!("  🛡️  Other blocks: {}", other_block_count);
            println!("  📊 Total blocked: {}", blocked_count);
        }
    }
    
    pub fn test_bot_detection(&self) {
        println!("\n🔍 Testing Bot Detection");
        
        let bot_user_agents = vec![
            "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)",
            "Mozilla/5.0 (compatible; Bingbot/2.0; +http://www.bing.com/bingbot.htm)",
            "python-requests/2.28.1",
            "curl/7.88.1",
            "wget/1.21.3",
            "sqlmap/1.7",
            "nikto",
            "nessus",
            "hydra",
            "", // Empty UA
        ];
        
        for ua in bot_user_agents {
            let url = format!("{}/", self.base_url);
            let start = Instant::now();
            
            let mut headers = HeaderMap::new();
            if !ua.is_empty() {
                headers.insert(USER_AGENT, HeaderValue::from_str(ua).unwrap());
            }
            
            let response = self.client.get(&url)
                .headers(headers)
                .send();
            
            let response_time = start.elapsed();
            
            match response {
                Ok(resp) => {
                    let status = resp.status();
                    let status_code = status.as_u16();
                    
                    let ua_label = if ua.is_empty() { "Empty" } else { "Suspicious" };
                    let attack_type = format!("Bot UA: {}", ua_label);
                    let (blocked, reason) = self.analyze_response(status_code, &attack_type, "/");
                    
                    let result = TestResult {
                        endpoint: "/".to_string(),
                        attack_type: attack_type.clone(),
                        status_code,
                        response_time,
                        blocked,
                        block_reason: reason.clone(),
                    };
                    
                    let icon = if blocked { "🛡️" } else { "⚠️" };
                    println!("{} UA '{}': Status {} - {}ms - {}",
                        icon, 
                        ua_label, 
                        status_code,
                        response_time.as_millis(),
                        reason);
                    
                    self.record_result(result);
                }
                Err(e) => {
                    println!("❌ UA '{}': Failed - {}", ua, e);
                }
            }
            
            thread::sleep(Duration::from_millis(200));
        }
    }
    
    pub fn test_jwt_validation(&self) {
        println!("\n🔍 Testing JWT Validation");
        
        let invalid_tokens = vec![
            "Bearer invalid.token.here",
            "Bearer ",
            "", // No token
            "InvalidFormat",
        ];
        
        for token in invalid_tokens {
            let url = format!("{}/api/wallet/balance", self.base_url);
            let start = Instant::now();
            
            let mut headers = HeaderMap::new();
            if !token.is_empty() {
                headers.insert(AUTHORIZATION, HeaderValue::from_str(token).unwrap());
            }
            
            let response = self.client.get(&url)
                .headers(headers)
                .header(USER_AGENT, "Security-Tester/1.0")
                .send();
            
            let response_time = start.elapsed();
            
            match response {
                Ok(resp) => {
                    let status = resp.status();
                    let status_code = status.as_u16();
                    
                    let token_label = if token.is_empty() { "Empty" } else { "Invalid" };
                    let attack_type = format!("Invalid JWT: {}", token_label);
                    let (blocked, reason) = self.analyze_response(status_code, &attack_type, "/api/wallet/balance");
                    
                    let result = TestResult {
                        endpoint: "/api/wallet/balance".to_string(),
                        attack_type: attack_type.clone(),
                        status_code,
                        response_time,
                        blocked,
                        block_reason: reason.clone(),
                    };
                    
                    let icon = if blocked { "🛡️" } else { "⚠️" };
                    println!("{} JWT '{}': Status {} - {}ms - {}",
                        icon,
                        token_label, 
                        status_code,
                        response_time.as_millis(),
                        reason);
                    
                    self.record_result(result);
                }
                Err(e) => {
                    println!("❌ JWT '{}': Failed - {}", token, e);
                }
            }
            
            thread::sleep(Duration::from_millis(200));
        }
    }
    
    pub fn test_all(&self) {
        println!("\n🚀 Starting comprehensive security tests");
        println!("🎯 Target: {}", self.base_url);
        println!("📋 Testing all security controls...\n");
        
        self.test_sql_injection();
        self.test_xss_attacks();
        self.test_path_traversal();
        self.test_rate_limiting();
        self.test_bot_detection();
        self.test_jwt_validation();
        
        println!("\n✅ All Tests Complete");
        self.generate_report();
    }
}

// Main function to run the tests
fn main() {
    println!("🔒 NGINX Security Tester");
    println!("========================");
    
    // You can change the target URL here
    let target_url = "http://localhost:8080";
    
    let tester = SecurityTester::new(target_url);
    tester.test_all();
}