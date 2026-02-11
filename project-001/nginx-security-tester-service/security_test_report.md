# NGINX Security Test Report

## Summary

- **Total Tests**: 168
- **Blocked Attacks**: 156
- **Passed Through**: 12
- **Block Rate**: 92.86%

## Security Effectiveness by Attack Type

| Attack Type | Tests | Blocked | Block Rate |
|-------------|-------|---------|------------|
| SQL Injection | 32 | 32 | 100.0% |
| XSS | 27 | 27 | 100.0% |
| Path Traversal | 5 | 5 | 100.0% |
| Rate Limiting | 90 | 78 | 86.7% |
| Bot Detection | 10 | 10 | 100.0% |
| JWT Validation | 4 | 4 | 100.0% |

## Detailed Results

| Endpoint | Attack Type | Status | Blocked | Response Time | Reason |
|----------|-------------|--------|---------|---------------|--------|
| /api/wallet/ | SQLi: union select 1,2,3 | 403 | ✅ | 118ms | 🛡️ Blocked - Forbidden (W... |
| /api/wallet/ | SQLi: insert into users values('admin','password') | 403 | ✅ | 10ms | 🛡️ Blocked - Forbidden (W... |
| /api/wallet/ | SQLi: delete from users where 1=1 | 403 | ✅ | 12ms | 🛡️ Blocked - Forbidden (W... |
| /api/wallet/ | SQLi: drop table users | 403 | ✅ | 11ms | 🛡️ Blocked - Forbidden (W... |
| /api/wallet/ | SQLi: exec sp_configure | 403 | ✅ | 9ms | 🛡️ Blocked - Forbidden (W... |
| /api/wallet/ | SQLi: 1' OR '1'='1 | 403 | ✅ | 268ms | 🛡️ Blocked - Forbidden (W... |
| /api/wallet/ | SQLi: admin'-- | 403 | ✅ | 9ms | 🛡️ Blocked - Forbidden (W... |
| /api/wallet/ | SQLi: 1' UNION SELECT NULL,NULL-- | 403 | ✅ | 8ms | 🛡️ Blocked - Forbidden (W... |
| /api/deposit/ | SQLi: union select 1,2,3 | 404 | ✅ | 832ms | 🛡️ Blocked - Resource not... |
| /api/deposit/ | SQLi: insert into users values('admin','password') | 404 | ✅ | 47ms | 🛡️ Blocked - Resource not... |
| /api/deposit/ | SQLi: delete from users where 1=1 | 404 | ✅ | 40ms | 🛡️ Blocked - Resource not... |
| /api/deposit/ | SQLi: drop table users | 404 | ✅ | 36ms | 🛡️ Blocked - Resource not... |
| /api/deposit/ | SQLi: exec sp_configure | 404 | ✅ | 35ms | 🛡️ Blocked - Resource not... |
| /api/deposit/ | SQLi: 1' OR '1'='1 | 404 | ✅ | 34ms | 🛡️ Blocked - Resource not... |
| /api/deposit/ | SQLi: admin'-- | 404 | ✅ | 33ms | 🛡️ Blocked - Resource not... |
| /api/deposit/ | SQLi: 1' UNION SELECT NULL,NULL-- | 404 | ✅ | 34ms | 🛡️ Blocked - Resource not... |
| /api/withdrawals/ | SQLi: union select 1,2,3 | 403 | ✅ | 9ms | 🛡️ Blocked - Forbidden (W... |
| /api/withdrawals/ | SQLi: insert into users values('admin','password') | 403 | ✅ | 9ms | 🛡️ Blocked - Forbidden (W... |
| /api/withdrawals/ | SQLi: delete from users where 1=1 | 403 | ✅ | 9ms | 🛡️ Blocked - Forbidden (W... |
| /api/withdrawals/ | SQLi: drop table users | 401 | ✅ | 1181ms | 🛡️ Blocked - Authenticati... |
| /api/withdrawals/ | SQLi: exec sp_configure | 401 | ✅ | 31ms | 🛡️ Blocked - Authenticati... |
| /api/withdrawals/ | SQLi: 1' OR '1'='1 | 401 | ✅ | 32ms | 🛡️ Blocked - Authenticati... |
| /api/withdrawals/ | SQLi: admin'-- | 429 | ✅ | 9ms | ⏱️ Rate Limited - Too many... |
| /api/withdrawals/ | SQLi: 1' UNION SELECT NULL,NULL-- | 403 | ✅ | 11ms | 🛡️ Blocked - Forbidden (W... |
| /api/user/ | SQLi: union select 1,2,3 | 403 | ✅ | 13ms | 🛡️ Blocked - Forbidden (W... |
| /api/user/ | SQLi: insert into users values('admin','password') | 403 | ✅ | 9ms | 🛡️ Blocked - Forbidden (W... |
| /api/user/ | SQLi: delete from users where 1=1 | 403 | ✅ | 9ms | 🛡️ Blocked - Forbidden (W... |
| /api/user/ | SQLi: drop table users | 403 | ✅ | 8ms | 🛡️ Blocked - Forbidden (W... |
| /api/user/ | SQLi: exec sp_configure | 403 | ✅ | 10ms | 🛡️ Blocked - Forbidden (W... |
| /api/user/ | SQLi: 1' OR '1'='1 | 403 | ✅ | 199ms | 🛡️ Blocked - Forbidden (W... |
| /api/user/ | SQLi: admin'-- | 403 | ✅ | 9ms | 🛡️ Blocked - Forbidden (W... |
| /api/user/ | SQLi: 1' UNION SELECT NULL,NULL-- | 403 | ✅ | 9ms | 🛡️ Blocked - Forbidden (W... |
| /api/wallet/ | XSS: <script>alert('xss')</script> | 403 | ✅ | 13ms | 🛡️ Blocked - Forbidden (W... |
| /api/wallet/ | XSS: javascript:alert('xss') | 403 | ✅ | 4ms | 🛡️ Blocked - Forbidden (W... |
| /api/wallet/ | XSS: onerror=alert('xss') | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (W... |
| /api/wallet/ | XSS: onload=alert('xss') | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (W... |
| /api/wallet/ | XSS: <img src=x onerror=alert('xss')> | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (W... |
| /api/wallet/ | XSS: <iframe src=javascript:alert('xss')> | 403 | ✅ | 4ms | 🛡️ Blocked - Forbidden (W... |
| /api/wallet/ | XSS: base64_decode | 403 | ✅ | 9ms | 🛡️ Blocked - Forbidden (W... |
| /api/wallet/ | XSS: eval(String.fromCharCode | 403 | ✅ | 5ms | 🛡️ Blocked - Forbidden (W... |
| /api/wallet/ | XSS: document.cookie | 403 | ✅ | 4ms | 🛡️ Blocked - Forbidden (W... |
| /api/deposit/ | XSS: <script>alert('xss')</script> | 403 | ✅ | 5ms | 🛡️ Blocked - Forbidden (W... |
| /api/deposit/ | XSS: javascript:alert('xss') | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (W... |
| /api/deposit/ | XSS: onerror=alert('xss') | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (W... |
| /api/deposit/ | XSS: onload=alert('xss') | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (W... |
| /api/deposit/ | XSS: <img src=x onerror=alert('xss')> | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (W... |
| /api/deposit/ | XSS: <iframe src=javascript:alert('xss')> | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (W... |
| /api/deposit/ | XSS: base64_decode | 403 | ✅ | 9ms | 🛡️ Blocked - Forbidden (W... |
| /api/deposit/ | XSS: eval(String.fromCharCode | 403 | ✅ | 4ms | 🛡️ Blocked - Forbidden (W... |
| /api/deposit/ | XSS: document.cookie | 403 | ✅ | 4ms | 🛡️ Blocked - Forbidden (W... |
| /api/user/ | XSS: <script>alert('xss')</script> | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (W... |
| /api/user/ | XSS: javascript:alert('xss') | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (W... |
| /api/user/ | XSS: onerror=alert('xss') | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (W... |
| /api/user/ | XSS: onload=alert('xss') | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (W... |
| /api/user/ | XSS: <img src=x onerror=alert('xss')> | 403 | ✅ | 4ms | 🛡️ Blocked - Forbidden (W... |
| /api/user/ | XSS: <iframe src=javascript:alert('xss')> | 403 | ✅ | 6ms | 🛡️ Blocked - Forbidden (W... |
| /api/user/ | XSS: base64_decode | 403 | ✅ | 12ms | 🛡️ Blocked - Forbidden (W... |
| /api/user/ | XSS: eval(String.fromCharCode | 403 | ✅ | 4ms | 🛡️ Blocked - Forbidden (W... |
| /api/user/ | XSS: document.cookie | 403 | ✅ | 5ms | 🛡️ Blocked - Forbidden (W... |
| /api/user/ | Path: ..\..\..\windows\system.ini | 404 | ✅ | 15ms | 🛡️ Blocked - Not found (l... |
| /api/user/ | Path: ....//....//etc/passwd | 403 | ✅ | 2ms | 🛡️ Blocked - Forbidden (W... |
| /api/user/ | Path: .git/config | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (W... |
| /api/user/ | Path: .env | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (W... |
| /api/user/ | Path: /proc/self/environ | 403 | ✅ | 4ms | 🛡️ Blocked - Forbidden (W... |
| /api/auth/login | Rate Limit Test | 400 | ❌ | 56ms | ⚠️ Bad request - Not rate ... |
| /api/auth/login | Rate Limit Test | 400 | ❌ | 19ms | ⚠️ Bad request - Not rate ... |
| /api/auth/login | Rate Limit Test | 400 | ❌ | 31ms | ⚠️ Bad request - Not rate ... |
| /api/auth/login | Rate Limit Test | 400 | ❌ | 23ms | ⚠️ Bad request - Not rate ... |
| /api/auth/login | Rate Limit Test | 400 | ❌ | 22ms | ⚠️ Bad request - Not rate ... |
| /api/auth/login | Rate Limit Test | 400 | ❌ | 22ms | ⚠️ Bad request - Not rate ... |
| /api/auth/login | Rate Limit Test | 400 | ❌ | 21ms | ⚠️ Bad request - Not rate ... |
| /api/auth/login | Rate Limit Test | 400 | ❌ | 20ms | ⚠️ Bad request - Not rate ... |
| /api/auth/login | Rate Limit Test | 400 | ❌ | 20ms | ⚠️ Bad request - Not rate ... |
| /api/auth/login | Rate Limit Test | 400 | ❌ | 41ms | ⚠️ Bad request - Not rate ... |
| /api/auth/login | Rate Limit Test | 400 | ❌ | 22ms | ⚠️ Bad request - Not rate ... |
| /api/auth/login | Rate Limit Test | 400 | ❌ | 21ms | ⚠️ Bad request - Not rate ... |
| /api/auth/login | Rate Limit Test | 429 | ✅ | 8ms | ⏱️ Rate Limited - Too many... |
| /api/auth/login | Rate Limit Test | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (m... |
| /api/auth/login | Rate Limit Test | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (m... |
| /api/auth/login | Rate Limit Test | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (m... |
| /api/auth/login | Rate Limit Test | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (m... |
| /api/auth/login | Rate Limit Test | 403 | ✅ | 4ms | 🛡️ Blocked - Forbidden (m... |
| /api/auth/login | Rate Limit Test | 403 | ✅ | 4ms | 🛡️ Blocked - Forbidden (m... |
| /api/auth/login | Rate Limit Test | 403 | ✅ | 4ms | 🛡️ Blocked - Forbidden (m... |
| /api/auth/login | Rate Limit Test | 403 | ✅ | 5ms | 🛡️ Blocked - Forbidden (m... |
| /api/auth/login | Rate Limit Test | 403 | ✅ | 4ms | 🛡️ Blocked - Forbidden (m... |
| /api/auth/login | Rate Limit Test | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (m... |
| /api/auth/login | Rate Limit Test | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (m... |
| /api/auth/login | Rate Limit Test | 403 | ✅ | 9ms | 🛡️ Blocked - Forbidden (m... |
| /api/auth/login | Rate Limit Test | 403 | ✅ | 5ms | 🛡️ Blocked - Forbidden (m... |
| /api/auth/login | Rate Limit Test | 403 | ✅ | 4ms | 🛡️ Blocked - Forbidden (m... |
| /api/auth/login | Rate Limit Test | 403 | ✅ | 4ms | 🛡️ Blocked - Forbidden (m... |
| /api/auth/login | Rate Limit Test | 403 | ✅ | 6ms | 🛡️ Blocked - Forbidden (m... |
| /api/auth/login | Rate Limit Test | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (m... |
| /api/wallet/balance | Rate Limit Test | 403 | ✅ | 24ms | 🛡️ Blocked - Forbidden (m... |
| /api/wallet/balance | Rate Limit Test | 403 | ✅ | 24ms | 🛡️ Blocked - Forbidden (m... |
| /api/wallet/balance | Rate Limit Test | 429 | ✅ | 10ms | ⏱️ Rate Limited - Too many... |
| /api/wallet/balance | Rate Limit Test | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (m... |
| /api/wallet/balance | Rate Limit Test | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (m... |
| /api/wallet/balance | Rate Limit Test | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (m... |
| /api/wallet/balance | Rate Limit Test | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (m... |
| /api/wallet/balance | Rate Limit Test | 403 | ✅ | 5ms | 🛡️ Blocked - Forbidden (m... |
| /api/wallet/balance | Rate Limit Test | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (m... |
| /api/wallet/balance | Rate Limit Test | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (m... |
| /api/wallet/balance | Rate Limit Test | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (m... |
| /api/wallet/balance | Rate Limit Test | 403 | ✅ | 4ms | 🛡️ Blocked - Forbidden (m... |
| /api/wallet/balance | Rate Limit Test | 403 | ✅ | 10ms | 🛡️ Blocked - Forbidden (m... |
| /api/wallet/balance | Rate Limit Test | 403 | ✅ | 4ms | 🛡️ Blocked - Forbidden (m... |
| /api/wallet/balance | Rate Limit Test | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (m... |
| /api/wallet/balance | Rate Limit Test | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (m... |
| /api/wallet/balance | Rate Limit Test | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (m... |
| /api/wallet/balance | Rate Limit Test | 403 | ✅ | 4ms | 🛡️ Blocked - Forbidden (m... |
| /api/wallet/balance | Rate Limit Test | 403 | ✅ | 4ms | 🛡️ Blocked - Forbidden (m... |
| /api/wallet/balance | Rate Limit Test | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (m... |
| /api/wallet/balance | Rate Limit Test | 403 | ✅ | 4ms | 🛡️ Blocked - Forbidden (m... |
| /api/wallet/balance | Rate Limit Test | 403 | ✅ | 4ms | 🛡️ Blocked - Forbidden (m... |
| /api/wallet/balance | Rate Limit Test | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (m... |
| /api/wallet/balance | Rate Limit Test | 403 | ✅ | 5ms | 🛡️ Blocked - Forbidden (m... |
| /api/wallet/balance | Rate Limit Test | 403 | ✅ | 4ms | 🛡️ Blocked - Forbidden (m... |
| /api/wallet/balance | Rate Limit Test | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (m... |
| /api/wallet/balance | Rate Limit Test | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (m... |
| /api/wallet/balance | Rate Limit Test | 403 | ✅ | 4ms | 🛡️ Blocked - Forbidden (m... |
| /api/wallet/balance | Rate Limit Test | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (m... |
| /api/wallet/balance | Rate Limit Test | 403 | ✅ | 4ms | 🛡️ Blocked - Forbidden (m... |
| /api/deposit/ | Rate Limit Test | 429 | ✅ | 9ms | ⏱️ Rate Limited - Too many... |
| /api/deposit/ | Rate Limit Test | 429 | ✅ | 9ms | ⏱️ Rate Limited - Too many... |
| /api/deposit/ | Rate Limit Test | 429 | ✅ | 8ms | ⏱️ Rate Limited - Too many... |
| /api/deposit/ | Rate Limit Test | 429 | ✅ | 9ms | ⏱️ Rate Limited - Too many... |
| /api/deposit/ | Rate Limit Test | 429 | ✅ | 9ms | ⏱️ Rate Limited - Too many... |
| /api/deposit/ | Rate Limit Test | 429 | ✅ | 9ms | ⏱️ Rate Limited - Too many... |
| /api/deposit/ | Rate Limit Test | 429 | ✅ | 9ms | ⏱️ Rate Limited - Too many... |
| /api/deposit/ | Rate Limit Test | 429 | ✅ | 9ms | ⏱️ Rate Limited - Too many... |
| /api/deposit/ | Rate Limit Test | 429 | ✅ | 11ms | ⏱️ Rate Limited - Too many... |
| /api/deposit/ | Rate Limit Test | 429 | ✅ | 8ms | ⏱️ Rate Limited - Too many... |
| /api/deposit/ | Rate Limit Test | 429 | ✅ | 9ms | ⏱️ Rate Limited - Too many... |
| /api/deposit/ | Rate Limit Test | 429 | ✅ | 8ms | ⏱️ Rate Limited - Too many... |
| /api/deposit/ | Rate Limit Test | 429 | ✅ | 13ms | ⏱️ Rate Limited - Too many... |
| /api/deposit/ | Rate Limit Test | 429 | ✅ | 8ms | ⏱️ Rate Limited - Too many... |
| /api/deposit/ | Rate Limit Test | 429 | ✅ | 8ms | ⏱️ Rate Limited - Too many... |
| /api/deposit/ | Rate Limit Test | 429 | ✅ | 8ms | ⏱️ Rate Limited - Too many... |
| /api/deposit/ | Rate Limit Test | 429 | ✅ | 8ms | ⏱️ Rate Limited - Too many... |
| /api/deposit/ | Rate Limit Test | 429 | ✅ | 16ms | ⏱️ Rate Limited - Too many... |
| /api/deposit/ | Rate Limit Test | 429 | ✅ | 8ms | ⏱️ Rate Limited - Too many... |
| /api/deposit/ | Rate Limit Test | 429 | ✅ | 7ms | ⏱️ Rate Limited - Too many... |
| /api/deposit/ | Rate Limit Test | 429 | ✅ | 8ms | ⏱️ Rate Limited - Too many... |
| /api/deposit/ | Rate Limit Test | 429 | ✅ | 8ms | ⏱️ Rate Limited - Too many... |
| /api/deposit/ | Rate Limit Test | 429 | ✅ | 9ms | ⏱️ Rate Limited - Too many... |
| /api/deposit/ | Rate Limit Test | 429 | ✅ | 10ms | ⏱️ Rate Limited - Too many... |
| /api/deposit/ | Rate Limit Test | 429 | ✅ | 8ms | ⏱️ Rate Limited - Too many... |
| /api/deposit/ | Rate Limit Test | 429 | ✅ | 11ms | ⏱️ Rate Limited - Too many... |
| /api/deposit/ | Rate Limit Test | 404 | ✅ | 135ms | 🛡️ Blocked - Resource not... |
| /api/deposit/ | Rate Limit Test | 429 | ✅ | 13ms | ⏱️ Rate Limited - Too many... |
| /api/deposit/ | Rate Limit Test | 429 | ✅ | 11ms | ⏱️ Rate Limited - Too many... |
| /api/deposit/ | Rate Limit Test | 429 | ✅ | 10ms | ⏱️ Rate Limited - Too many... |
| / | Bot UA: Suspicious | 404 | ✅ | 3ms | 🛡️ Blocked - Resource not... |
| / | Bot UA: Suspicious | 404 | ✅ | 4ms | 🛡️ Blocked - Resource not... |
| / | Bot UA: Suspicious | 404 | ✅ | 3ms | 🛡️ Blocked - Resource not... |
| / | Bot UA: Suspicious | 404 | ✅ | 2ms | 🛡️ Blocked - Resource not... |
| / | Bot UA: Suspicious | 404 | ✅ | 3ms | 🛡️ Blocked - Resource not... |
| / | Bot UA: Suspicious | 404 | ✅ | 3ms | 🛡️ Blocked - Resource not... |
| / | Bot UA: Suspicious | 404 | ✅ | 2ms | 🛡️ Blocked - Resource not... |
| / | Bot UA: Suspicious | 404 | ✅ | 3ms | 🛡️ Blocked - Resource not... |
| / | Bot UA: Suspicious | 404 | ✅ | 3ms | 🛡️ Blocked - Resource not... |
| / | Bot UA: Empty | 404 | ✅ | 2ms | 🛡️ Blocked - Resource not... |
| /api/wallet/balance | Invalid JWT: Invalid | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (W... |
| /api/wallet/balance | Invalid JWT: Invalid | 403 | ✅ | 3ms | 🛡️ Blocked - Forbidden (W... |
| /api/wallet/balance | Invalid JWT: Empty | 403 | ✅ | 2ms | 🛡️ Blocked - Forbidden (W... |
| /api/wallet/balance | Invalid JWT: Invalid | 403 | ✅ | 4ms | 🛡️ Blocked - Forbidden (W... |

## Status Code Analysis

| Status Code | Count | Description |
|-------------|-------|-------------|
| 400 | 12 | Bad Request |
| 401 | 3 | Unauthorized |
| 403 | 101 | Forbidden |
| 404 | 20 | Not Found |
| 429 | 32 | Too Many Requests |

## Security Analysis & Recommendations

### Security Issues Found:

⚠️ **Rate Limiting Used as Security Control**: Some SQLi/XSS attacks are returning 429 (rate limited) instead of 403/400 (security blocked). Attackers could bypass this by sending attacks slowly.
  - SQLi: admin'-- on /api/withdrawals/ - Rate limited instead of security blocked

### Detailed List of Unblocked Attacks:

1. **Rate Limit Test** on `/api/auth/login` - Status: 400 - ⚠️ Bad request - Not rate limited but rejected
1. **Rate Limit Test** on `/api/auth/login` - Status: 400 - ⚠️ Bad request - Not rate limited but rejected
1. **Rate Limit Test** on `/api/auth/login` - Status: 400 - ⚠️ Bad request - Not rate limited but rejected
1. **Rate Limit Test** on `/api/auth/login` - Status: 400 - ⚠️ Bad request - Not rate limited but rejected
1. **Rate Limit Test** on `/api/auth/login` - Status: 400 - ⚠️ Bad request - Not rate limited but rejected
1. **Rate Limit Test** on `/api/auth/login` - Status: 400 - ⚠️ Bad request - Not rate limited but rejected
1. **Rate Limit Test** on `/api/auth/login` - Status: 400 - ⚠️ Bad request - Not rate limited but rejected
1. **Rate Limit Test** on `/api/auth/login` - Status: 400 - ⚠️ Bad request - Not rate limited but rejected
1. **Rate Limit Test** on `/api/auth/login` - Status: 400 - ⚠️ Bad request - Not rate limited but rejected
1. **Rate Limit Test** on `/api/auth/login` - Status: 400 - ⚠️ Bad request - Not rate limited but rejected
1. **Rate Limit Test** on `/api/auth/login` - Status: 400 - ⚠️ Bad request - Not rate limited but rejected
1. **Rate Limit Test** on `/api/auth/login` - Status: 400 - ⚠️ Bad request - Not rate limited but rejected
### Recommendations:

⚠️ **MEDIUM PRIORITY**: Rate limiting needs improvement. 12 requests bypassed rate limits.
