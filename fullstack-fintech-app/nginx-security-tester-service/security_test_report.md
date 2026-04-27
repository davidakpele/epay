# NGINX Security Test Report

## Summary

- **Total Tests**: 168
- **Blocked Attacks**: 168
- **Passed Through**: 0
- **Block Rate**: 100.00%

## Security Effectiveness by Attack Type

| Attack Type | Tests | Blocked | Block Rate |
|-------------|-------|---------|------------|
| SQL Injection | 32 | 32 | 100.0% |
| XSS | 27 | 27 | 100.0% |
| Path Traversal | 5 | 5 | 100.0% |
| Rate Limiting | 90 | 90 | 100.0% |
| Bot Detection | 10 | 10 | 100.0% |
| JWT Validation | 4 | 4 | 100.0% |

## Detailed Results

| Endpoint | Attack Type | Status | Blocked | Response Time | Reason |
|----------|-------------|--------|---------|---------------|--------|
| /api/wallet/ | SQLi: union select 1,2,3 | 403 | ✅ | 28ms | 🛡️ Blocked - Forbidden (W... |
| /api/wallet/ | SQLi: insert into users values('admin','password') | 403 | ✅ | 8ms | 🛡️ Blocked - Forbidden (W... |
| /api/wallet/ | SQLi: delete from users where 1=1 | 403 | ✅ | 8ms | 🛡️ Blocked - Forbidden (W... |
| /api/wallet/ | SQLi: drop table users | 403 | ✅ | 10ms | 🛡️ Blocked - Forbidden (W... |
| /api/wallet/ | SQLi: exec sp_configure | 403 | ✅ | 8ms | 🛡️ Blocked - Forbidden (W... |
| /api/wallet/ | SQLi: 1' OR '1'='1 | 403 | ✅ | 23ms | 🛡️ Blocked - Forbidden (W... |
| /api/wallet/ | SQLi: admin'-- | 403 | ✅ | 10ms | 🛡️ Blocked - Forbidden (W... |
| /api/wallet/ | SQLi: 1' UNION SELECT NULL,NULL-- | 403 | ✅ | 8ms | 🛡️ Blocked - Forbidden (W... |
| /api/deposit/ | SQLi: union select 1,2,3 | 404 | ✅ | 419ms | 🛡️ Blocked - Resource not... |
| /api/deposit/ | SQLi: insert into users values('admin','password') | 404 | ✅ | 28ms | 🛡️ Blocked - Resource not... |
| /api/deposit/ | SQLi: delete from users where 1=1 | 404 | ✅ | 23ms | 🛡️ Blocked - Resource not... |
| /api/deposit/ | SQLi: drop table users | 404 | ✅ | 21ms | 🛡️ Blocked - Resource not... |
| /api/deposit/ | SQLi: exec sp_configure | 404 | ✅ | 20ms | 🛡️ Blocked - Resource not... |
| /api/deposit/ | SQLi: 1' OR '1'='1 | 404 | ✅ | 22ms | 🛡️ Blocked - Resource not... |
| /api/deposit/ | SQLi: admin'-- | 404 | ✅ | 21ms | 🛡️ Blocked - Resource not... |
| /api/deposit/ | SQLi: 1' UNION SELECT NULL,NULL-- | 429 | ✅ | 9ms | ⏱️ Rate Limited - Too many... |
| /api/withdrawals/ | SQLi: union select 1,2,3 | 403 | ✅ | 9ms | 🛡️ Blocked - Forbidden (W... |
| /api/withdrawals/ | SQLi: insert into users values('admin','password') | 403 | ✅ | 7ms | 🛡️ Blocked - Forbidden (W... |
| /api/withdrawals/ | SQLi: delete from users where 1=1 | 403 | ✅ | 8ms | 🛡️ Blocked - Forbidden (W... |
| /api/withdrawals/ | SQLi: drop table users | 401 | ✅ | 196ms | 🛡️ Blocked - Authenticati... |
| /api/withdrawals/ | SQLi: exec sp_configure | 401 | ✅ | 16ms | 🛡️ Blocked - Authenticati... |
| /api/withdrawals/ | SQLi: 1' OR '1'='1 | 429 | ✅ | 8ms | ⏱️ Rate Limited - Too many... |
| /api/withdrawals/ | SQLi: admin'-- | 429 | ✅ | 10ms | ⏱️ Rate Limited - Too many... |
| /api/withdrawals/ | SQLi: 1' UNION SELECT NULL,NULL-- | 403 | ✅ | 8ms | 🛡️ Blocked - Forbidden (W... |
| /api/user/ | SQLi: union select 1,2,3 | 403 | ✅ | 7ms | 🛡️ Blocked - Forbidden (W... |
| /api/user/ | SQLi: insert into users values('admin','password') | 403 | ✅ | 8ms | 🛡️ Blocked - Forbidden (W... |
| /api/user/ | SQLi: delete from users where 1=1 | 403 | ✅ | 8ms | 🛡️ Blocked - Forbidden (W... |
| /api/user/ | SQLi: drop table users | 403 | ✅ | 9ms | 🛡️ Blocked - Forbidden (W... |
| /api/user/ | SQLi: exec sp_configure | 403 | ✅ | 10ms | 🛡️ Blocked - Forbidden (W... |
| /api/user/ | SQLi: 1' OR '1'='1 | 401 | ✅ | 36ms | 🛡️ Blocked - Authenticati... |
| /api/user/ | SQLi: admin'-- | 403 | ✅ | 10ms | 🛡️ Blocked - Forbidden (W... |
| /api/user/ | SQLi: 1' UNION SELECT NULL,NULL-- | 403 | ✅ | 8ms | 🛡️ Blocked - Forbidden (W... |
| /api/wallet/ | XSS: <script>alert('xss')</script> | 403 | ✅ | 9ms | 🛡️ Blocked - Forbidden (W... |
| /api/wallet/ | XSS: javascript:alert('xss') | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (W... |
| /api/wallet/ | XSS: onerror=alert('xss') | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (W... |
| /api/wallet/ | XSS: onload=alert('xss') | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (W... |
| /api/wallet/ | XSS: <img src=x onerror=alert('xss')> | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (W... |
| /api/wallet/ | XSS: <iframe src=javascript:alert('xss')> | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (W... |
| /api/wallet/ | XSS: base64_decode | 403 | ✅ | 10ms | 🛡️ Blocked - Forbidden (W... |
| /api/wallet/ | XSS: eval(String.fromCharCode | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (W... |
| /api/wallet/ | XSS: document.cookie | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (W... |
| /api/deposit/ | XSS: <script>alert('xss')</script> | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (W... |
| /api/deposit/ | XSS: javascript:alert('xss') | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (W... |
| /api/deposit/ | XSS: onerror=alert('xss') | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (W... |
| /api/deposit/ | XSS: onload=alert('xss') | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (W... |
| /api/deposit/ | XSS: <img src=x onerror=alert('xss')> | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (W... |
| /api/deposit/ | XSS: <iframe src=javascript:alert('xss')> | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (W... |
| /api/deposit/ | XSS: base64_decode | 403 | ✅ | 8ms | 🛡️ Blocked - Forbidden (W... |
| /api/deposit/ | XSS: eval(String.fromCharCode | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (W... |
| /api/deposit/ | XSS: document.cookie | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (W... |
| /api/user/ | XSS: <script>alert('xss')</script> | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (W... |
| /api/user/ | XSS: javascript:alert('xss') | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (W... |
| /api/user/ | XSS: onerror=alert('xss') | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (W... |
| /api/user/ | XSS: onload=alert('xss') | 403 | ✅ | 4ms | 🛡️ Blocked - Forbidden (W... |
| /api/user/ | XSS: <img src=x onerror=alert('xss')> | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (W... |
| /api/user/ | XSS: <iframe src=javascript:alert('xss')> | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (W... |
| /api/user/ | XSS: base64_decode | 403 | ✅ | 9ms | 🛡️ Blocked - Forbidden (W... |
| /api/user/ | XSS: eval(String.fromCharCode | 403 | ✅ | 5ms | 🛡️ Blocked - Forbidden (W... |
| /api/user/ | XSS: document.cookie | 403 | ✅ | 4ms | 🛡️ Blocked - Forbidden (W... |
| /api/user/ | Path: ..\..\..\windows\system.ini | 404 | ✅ | 7ms | 🛡️ Blocked - Not found (l... |
| /api/user/ | Path: ....//....//etc/passwd | 403 | ✅ | 2ms | 🛡️ Blocked - Forbidden (W... |
| /api/user/ | Path: .git/config | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (W... |
| /api/user/ | Path: .env | 403 | ✅ | 2ms | 🛡️ Blocked - Forbidden (W... |
| /api/user/ | Path: /proc/self/environ | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (W... |
| /api/auth/login | Rate Limit Test | 403 | ✅ | 11ms | 🛡️ Blocked - Forbidden (m... |
| /api/auth/login | Rate Limit Test | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (m... |
| /api/auth/login | Rate Limit Test | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (m... |
| /api/auth/login | Rate Limit Test | 403 | ✅ | 4ms | 🛡️ Blocked - Forbidden (m... |
| /api/auth/login | Rate Limit Test | 403 | ✅ | 9ms | 🛡️ Blocked - Forbidden (m... |
| /api/auth/login | Rate Limit Test | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (m... |
| /api/auth/login | Rate Limit Test | 403 | ✅ | 5ms | 🛡️ Blocked - Forbidden (m... |
| /api/auth/login | Rate Limit Test | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (m... |
| /api/auth/login | Rate Limit Test | 403 | ✅ | 9ms | 🛡️ Blocked - Forbidden (m... |
| /api/auth/login | Rate Limit Test | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (m... |
| /api/auth/login | Rate Limit Test | 403 | ✅ | 4ms | 🛡️ Blocked - Forbidden (m... |
| /api/auth/login | Rate Limit Test | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (m... |
| /api/auth/login | Rate Limit Test | 403 | ✅ | 9ms | 🛡️ Blocked - Forbidden (m... |
| /api/auth/login | Rate Limit Test | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (m... |
| /api/auth/login | Rate Limit Test | 403 | ✅ | 4ms | 🛡️ Blocked - Forbidden (m... |
| /api/auth/login | Rate Limit Test | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (m... |
| /api/auth/login | Rate Limit Test | 403 | ✅ | 9ms | 🛡️ Blocked - Forbidden (m... |
| /api/auth/login | Rate Limit Test | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (m... |
| /api/auth/login | Rate Limit Test | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (m... |
| /api/auth/login | Rate Limit Test | 403 | ✅ | 4ms | 🛡️ Blocked - Forbidden (m... |
| /api/auth/login | Rate Limit Test | 403 | ✅ | 11ms | 🛡️ Blocked - Forbidden (m... |
| /api/auth/login | Rate Limit Test | 403 | ✅ | 4ms | 🛡️ Blocked - Forbidden (m... |
| /api/auth/login | Rate Limit Test | 403 | ✅ | 4ms | 🛡️ Blocked - Forbidden (m... |
| /api/auth/login | Rate Limit Test | 403 | ✅ | 5ms | 🛡️ Blocked - Forbidden (m... |
| /api/auth/login | Rate Limit Test | 403 | ✅ | 9ms | 🛡️ Blocked - Forbidden (m... |
| /api/auth/login | Rate Limit Test | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (m... |
| /api/auth/login | Rate Limit Test | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (m... |
| /api/auth/login | Rate Limit Test | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (m... |
| /api/auth/login | Rate Limit Test | 403 | ✅ | 9ms | 🛡️ Blocked - Forbidden (m... |
| /api/auth/login | Rate Limit Test | 403 | ✅ | 4ms | 🛡️ Blocked - Forbidden (m... |
| /api/wallet/balance | Rate Limit Test | 403 | ✅ | 15ms | 🛡️ Blocked - Forbidden (m... |
| /api/wallet/balance | Rate Limit Test | 403 | ✅ | 15ms | 🛡️ Blocked - Forbidden (m... |
| /api/wallet/balance | Rate Limit Test | 403 | ✅ | 15ms | 🛡️ Blocked - Forbidden (m... |
| /api/wallet/balance | Rate Limit Test | 403 | ✅ | 14ms | 🛡️ Blocked - Forbidden (m... |
| /api/wallet/balance | Rate Limit Test | 403 | ✅ | 14ms | 🛡️ Blocked - Forbidden (m... |
| /api/wallet/balance | Rate Limit Test | 403 | ✅ | 15ms | 🛡️ Blocked - Forbidden (m... |
| /api/wallet/balance | Rate Limit Test | 403 | ✅ | 15ms | 🛡️ Blocked - Forbidden (m... |
| /api/wallet/balance | Rate Limit Test | 403 | ✅ | 15ms | 🛡️ Blocked - Forbidden (m... |
| /api/wallet/balance | Rate Limit Test | 403 | ✅ | 15ms | 🛡️ Blocked - Forbidden (m... |
| /api/wallet/balance | Rate Limit Test | 403 | ✅ | 16ms | 🛡️ Blocked - Forbidden (m... |
| /api/wallet/balance | Rate Limit Test | 403 | ✅ | 14ms | 🛡️ Blocked - Forbidden (m... |
| /api/wallet/balance | Rate Limit Test | 403 | ✅ | 14ms | 🛡️ Blocked - Forbidden (m... |
| /api/wallet/balance | Rate Limit Test | 429 | ✅ | 8ms | ⏱️ Rate Limited - Too many... |
| /api/wallet/balance | Rate Limit Test | 429 | ✅ | 9ms | ⏱️ Rate Limited - Too many... |
| /api/wallet/balance | Rate Limit Test | 429 | ✅ | 8ms | ⏱️ Rate Limited - Too many... |
| /api/wallet/balance | Rate Limit Test | 429 | ✅ | 9ms | ⏱️ Rate Limited - Too many... |
| /api/wallet/balance | Rate Limit Test | 429 | ✅ | 9ms | ⏱️ Rate Limited - Too many... |
| /api/wallet/balance | Rate Limit Test | 429 | ✅ | 9ms | ⏱️ Rate Limited - Too many... |
| /api/wallet/balance | Rate Limit Test | 429 | ✅ | 10ms | ⏱️ Rate Limited - Too many... |
| /api/wallet/balance | Rate Limit Test | 403 | ✅ | 15ms | 🛡️ Blocked - Forbidden (m... |
| /api/wallet/balance | Rate Limit Test | 429 | ✅ | 10ms | ⏱️ Rate Limited - Too many... |
| /api/wallet/balance | Rate Limit Test | 429 | ✅ | 9ms | ⏱️ Rate Limited - Too many... |
| /api/wallet/balance | Rate Limit Test | 429 | ✅ | 9ms | ⏱️ Rate Limited - Too many... |
| /api/wallet/balance | Rate Limit Test | 429 | ✅ | 9ms | ⏱️ Rate Limited - Too many... |
| /api/wallet/balance | Rate Limit Test | 429 | ✅ | 8ms | ⏱️ Rate Limited - Too many... |
| /api/wallet/balance | Rate Limit Test | 429 | ✅ | 9ms | ⏱️ Rate Limited - Too many... |
| /api/wallet/balance | Rate Limit Test | 429 | ✅ | 8ms | ⏱️ Rate Limited - Too many... |
| /api/wallet/balance | Rate Limit Test | 429 | ✅ | 8ms | ⏱️ Rate Limited - Too many... |
| /api/wallet/balance | Rate Limit Test | 403 | ✅ | 14ms | 🛡️ Blocked - Forbidden (m... |
| /api/wallet/balance | Rate Limit Test | 429 | ✅ | 7ms | ⏱️ Rate Limited - Too many... |
| /api/deposit/ | Rate Limit Test | 429 | ✅ | 9ms | ⏱️ Rate Limited - Too many... |
| /api/deposit/ | Rate Limit Test | 429 | ✅ | 9ms | ⏱️ Rate Limited - Too many... |
| /api/deposit/ | Rate Limit Test | 429 | ✅ | 9ms | ⏱️ Rate Limited - Too many... |
| /api/deposit/ | Rate Limit Test | 429 | ✅ | 9ms | ⏱️ Rate Limited - Too many... |
| /api/deposit/ | Rate Limit Test | 429 | ✅ | 9ms | ⏱️ Rate Limited - Too many... |
| /api/deposit/ | Rate Limit Test | 429 | ✅ | 8ms | ⏱️ Rate Limited - Too many... |
| /api/deposit/ | Rate Limit Test | 429 | ✅ | 8ms | ⏱️ Rate Limited - Too many... |
| /api/deposit/ | Rate Limit Test | 429 | ✅ | 9ms | ⏱️ Rate Limited - Too many... |
| /api/deposit/ | Rate Limit Test | 429 | ✅ | 8ms | ⏱️ Rate Limited - Too many... |
| /api/deposit/ | Rate Limit Test | 429 | ✅ | 9ms | ⏱️ Rate Limited - Too many... |
| /api/deposit/ | Rate Limit Test | 429 | ✅ | 8ms | ⏱️ Rate Limited - Too many... |
| /api/deposit/ | Rate Limit Test | 429 | ✅ | 10ms | ⏱️ Rate Limited - Too many... |
| /api/deposit/ | Rate Limit Test | 429 | ✅ | 10ms | ⏱️ Rate Limited - Too many... |
| /api/deposit/ | Rate Limit Test | 429 | ✅ | 8ms | ⏱️ Rate Limited - Too many... |
| /api/deposit/ | Rate Limit Test | 429 | ✅ | 7ms | ⏱️ Rate Limited - Too many... |
| /api/deposit/ | Rate Limit Test | 429 | ✅ | 8ms | ⏱️ Rate Limited - Too many... |
| /api/deposit/ | Rate Limit Test | 429 | ✅ | 8ms | ⏱️ Rate Limited - Too many... |
| /api/deposit/ | Rate Limit Test | 429 | ✅ | 8ms | ⏱️ Rate Limited - Too many... |
| /api/deposit/ | Rate Limit Test | 429 | ✅ | 8ms | ⏱️ Rate Limited - Too many... |
| /api/deposit/ | Rate Limit Test | 429 | ✅ | 8ms | ⏱️ Rate Limited - Too many... |
| /api/deposit/ | Rate Limit Test | 429 | ✅ | 8ms | ⏱️ Rate Limited - Too many... |
| /api/deposit/ | Rate Limit Test | 429 | ✅ | 8ms | ⏱️ Rate Limited - Too many... |
| /api/deposit/ | Rate Limit Test | 429 | ✅ | 8ms | ⏱️ Rate Limited - Too many... |
| /api/deposit/ | Rate Limit Test | 429 | ✅ | 8ms | ⏱️ Rate Limited - Too many... |
| /api/deposit/ | Rate Limit Test | 429 | ✅ | 9ms | ⏱️ Rate Limited - Too many... |
| /api/deposit/ | Rate Limit Test | 429 | ✅ | 8ms | ⏱️ Rate Limited - Too many... |
| /api/deposit/ | Rate Limit Test | 429 | ✅ | 9ms | ⏱️ Rate Limited - Too many... |
| /api/deposit/ | Rate Limit Test | 429 | ✅ | 19ms | ⏱️ Rate Limited - Too many... |
| /api/deposit/ | Rate Limit Test | 429 | ✅ | 9ms | ⏱️ Rate Limited - Too many... |
| /api/deposit/ | Rate Limit Test | 429 | ✅ | 12ms | ⏱️ Rate Limited - Too many... |
| / | Bot UA: Suspicious | 404 | ✅ | 3ms | 🛡️ Blocked - Resource not... |
| / | Bot UA: Suspicious | 404 | ✅ | 2ms | 🛡️ Blocked - Resource not... |
| / | Bot UA: Suspicious | 404 | ✅ | 4ms | 🛡️ Blocked - Resource not... |
| / | Bot UA: Suspicious | 404 | ✅ | 3ms | 🛡️ Blocked - Resource not... |
| / | Bot UA: Suspicious | 404 | ✅ | 3ms | 🛡️ Blocked - Resource not... |
| / | Bot UA: Suspicious | 404 | ✅ | 3ms | 🛡️ Blocked - Resource not... |
| / | Bot UA: Suspicious | 404 | ✅ | 2ms | 🛡️ Blocked - Resource not... |
| / | Bot UA: Suspicious | 404 | ✅ | 3ms | 🛡️ Blocked - Resource not... |
| / | Bot UA: Suspicious | 404 | ✅ | 3ms | 🛡️ Blocked - Resource not... |
| / | Bot UA: Empty | 404 | ✅ | 3ms | 🛡️ Blocked - Resource not... |
| /api/wallet/balance | Invalid JWT: Invalid | 401 | ✅ | 19ms | 🛡️ Blocked - Authenticati... |
| /api/wallet/balance | Invalid JWT: Invalid | 403 | ✅ | 14ms | 🛡️ Blocked - Forbidden (W... |
| /api/wallet/balance | Invalid JWT: Empty | 403 | ✅ | 13ms | 🛡️ Blocked - Forbidden (W... |
| /api/wallet/balance | Invalid JWT: Invalid | 403 | ✅ | 13ms | 🛡️ Blocked - Forbidden (W... |

## Status Code Analysis

| Status Code | Count | Description |
|-------------|-------|-------------|
| 401 | 4 | Unauthorized |
| 403 | 97 | Forbidden |
| 404 | 18 | Not Found |
| 429 | 49 | Too Many Requests |

## Security Analysis & Recommendations

### Security Issues Found:

⚠️ **Rate Limiting Used as Security Control**: Some SQLi/XSS attacks are returning 429 (rate limited) instead of 403/400 (security blocked). Attackers could bypass this by sending attacks slowly.
  - SQLi: 1' UNION SELECT NULL,NULL-- on /api/deposit/ - Rate limited instead of security blocked
  - SQLi: 1' OR '1'='1 on /api/withdrawals/ - Rate limited instead of security blocked
  - SQLi: admin'-- on /api/withdrawals/ - Rate limited instead of security blocked

✅ All security measures are working correctly!

### Recommendations:

