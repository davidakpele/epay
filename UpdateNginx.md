Setting HTTP cache in NGINX mainly involves two distinct approaches: **browser-side caching** (telling the client's browser to cache assets) and **server-side proxy caching** (making NGINX cache backend responses for faster delivery).

### 📁 Browser Cache (Client-Side)

This uses the `Cache-Control` and `Expires` headers to tell browsers how long to store assets locally. A common strategy is to cache static assets (images, CSS, JS) for long periods, while avoiding caching HTML pages .

**Configuration Example:**

```nginx
# Long-term caching for static assets
location ~* \.(jpg|jpeg|png|gif|ico|css|js)$ {
    expires 30d;
    add_header Cache-Control "public, no-transform";
}

# No caching for HTML (forces revalidation)
location ~* \.html$ {
    expires -1;
    add_header Cache-Control "no-cache, private";
}
```

- **`expires 30d`**: Sets a 30-day expiration, telling the browser to use its local copy.
- **`expires -1`**: Forces the browser to check with the server before using a cached copy (revalidation).

### 🗄️ Proxy Cache (Server-Side)

This configures NGINX to store responses from your backend (like Node.js or Python apps) on disk. This is powerful for reducing backend load.

**Step 1: Define a Cache Zone** (in the `http {}` block)
You must set up the storage location and memory zone for cache keys in the main config .

```nginx
http {
    # Define the cache path and zone
    proxy_cache_path /var/cache/nginx/my_cache
        levels=1:2
        keys_zone=my_cache:10m
        max_size=1g
        inactive=60m
        use_temp_path=off;
}
```

- **`keys_zone=my_cache:10m`**: Creates a 10MB shared memory zone named `my_cache` for storing cache keys (metadata). This can store roughly 80,000 keys .
- **`max_size=1g`**: Limits the cache size on disk to 1GB.
- **`inactive=60m`**: Removes cached items that haven't been accessed in 60 minutes.

**Step 2: Enable Caching in a `server` or `location` Block**

```nginx
server {
    location / {
        # Activate the cache
        proxy_cache my_cache;

        # Set cache key (usually scheme + host + URI)
        proxy_cache_key "$scheme$host$request_uri";

        # Cache successful responses for 10 minutes
        proxy_cache_valid 200 302 10m;
        proxy_cache_valid 404 1m;

        # Optional: Add a header to see cache status (HIT, MISS, etc.)
        add_header X-Cache-Status $upstream_cache_status;

        proxy_pass http://backend_server;
    }
}
```

- **`proxy_cache my_cache`**: Tells NGINX to use the zone you defined earlier.
- **`proxy_cache_valid`**: Sets Time-To-Live (TTL) for specific HTTP status codes. Here, `200` and `302` responses are cached for 10 minutes .
- **`X-Cache-Status`**: This header is invaluable for debugging, showing you whether a request was served from cache (`HIT`) or fetched from the backend (`MISS`).

### 💡 Key Considerations

- **Cache Purging**: To remove an item before it expires, you can configure a `proxy_cache_purge` directive to allow `PURGE` requests .
- **Cache Key**: The default key includes the request method. Be cautious about caching `POST` requests, as they usually shouldn't be cached by default .
- **Compression**: If your backend compresses responses (gzip), you may need to include `Accept-Encoding` in the cache key to avoid serving compressed data to clients that don't support it .

# Question

What about caching user data from the server and hold it for any frequent request for fast execution.

Example

"wallet_balances": [
{
"currency_code": "NGN",
"symbol": "₦",
"balance": "440000.00000000"
},
{
"currency_code": "USD",
"symbol": "$",
"balance": "0.00000000"
},
{
"currency_code": "EUR",
"symbol": "€",
"balance": "0.00000000"
},
{
"currency_code": "GBP",
"symbol": "£",
"balance": "0.00000000"
},
{
"currency_code": "AUD",
"symbol": "A$",
"balance": "0.00000000"
},
{
"currency_code": "JPY",
"symbol": "¥",
"balance": "0.00000000"
},
{
"currency_code": "CAD",
"symbol": "C$",
"balance": "0.00000000"
},
{
"currency_code": "CNY",
"symbol": "¥",
"balance": "0.00000000"
},
{
"currency_code": "CHF",
"symbol": "Fr",
"balance": "0.00000000"
},
{
"currency_code": "GHS",
"symbol": "₵",
"balance": "0.00000000"
}
]

# user wallet and user history data

{
"success": true,
"data": {
"content": [
{
"amount": {
"currency": "NGN",
"fee": 0E-8,
"gross": 50000.00000000,
"net": 50000.00000000,
"tax": 0E-8
},
"balance": {
"available": 440000.00000000,
"change": -50000.00000000,
"currency": "NGN",
"previous": 490000.00000000,
"running": 440000.00000000
},
"debitCredit": "DEBIT",
"description": "Investment locked (YEARLY plan @ 24% p.a.)",
"failure": null,
"historyId": 3,
"ledger": {
"ledgerEntryId": "LED_TXNB5FA09870ED34AD2",
"ledgerStatus": "POSTED"
},
"metadata": {
"accountType": "NGN",
"walletId": 1
},
"provider": {
"name": "SYSTEM",
"providerReference": "INV-2-1788956101"
},
"reference": "INV-2-1788956101",
"relatedTransactions": [],
"settlement": {
"settledAt": "2026-09-09T12:15:01.434792Z",
"settlementReference": "SET_TXNB5FA09870ED34AD2",
"status": "SETTLED"
},
"stateMachine": {
"states": {
"INITIATED": {
"timestamp": "2026-09-09T12:15:01.434777766Z",
"actor": "SYSTEM",
"message": "Transaction created",
"nextState": null,
"onFailure": false,
"failureState": null
},
"SETTLED": {
"timestamp": "2026-09-09T12:15:01.434784567Z",
"actor": "SYSTEM",
"message": "Investment created successfully.",
"nextState": null,
"onFailure": false,
"failureState": null
}
},
"allowedTransitions": []
},
"status": "SETTLED",
"statusHistory": [
{
"status": "INITIATED",
"timestamp": "2026-09-09T12:15:01.434777766Z",
"actor": "SYSTEM",
"message": "Transaction created"
},
{
"status": "SETTLED",
"timestamp": "2026-09-09T12:15:01.434784567Z",
"actor": "SYSTEM",
"message": "Investment created successfully."
}
],
"timestamps": {
"completedAt": "2026-09-09T12:15:01.434792Z",
"createdAt": "2026-09-09T12:15:01.438944Z",
"updatedAt": "2026-09-09T12:15:01.438959Z"
},
"transactionCategory": "DEBITED",
"transactionId": "TXN_B5FA09870ED34AD2",
"transactionType": "DEBITED",
"user": {
"id": 2,
"walletId": 1
}
},
{
"amount": {
"currency": "NGN",
"fee": 0E-8,
"gross": 10000.00000000,
"net": 10000.00000000,
"tax": 0E-8
},
"balance": {
"available": 490000.00000000,
"change": -10000.00000000,
"currency": "NGN",
"previous": 500000.00000000,
"running": 490000.00000000
},
"debitCredit": "DEBIT",
"description": "Investment locked (QUARTERLY plan @ 18% p.a.)",
"failure": null,
"historyId": 2,
"ledger": {
"ledgerEntryId": "LED_TXN2C9AAC4B7E184E7D",
"ledgerStatus": "POSTED"
},
"metadata": {
"accountType": "NGN",
"walletId": 1
},
"provider": {
"name": "SYSTEM",
"providerReference": "INV-2-1788956057"
},
"reference": "INV-2-1788956057",
"relatedTransactions": [],
"settlement": {
"settledAt": "2026-09-09T12:14:17.637015Z",
"settlementReference": "SET_TXN2C9AAC4B7E184E7D",
"status": "SETTLED"
},
"stateMachine": {
"states": {
"INITIATED": {
"timestamp": "2026-09-09T12:14:17.635605197Z",
"actor": "SYSTEM",
"message": "Transaction created",
"nextState": null,
"onFailure": false,
"failureState": null
},
"SETTLED": {
"timestamp": "2026-09-09T12:14:17.636995304Z",
"actor": "SYSTEM",
"message": "Investment created successfully.",
"nextState": null,
"onFailure": false,
"failureState": null
}
},
"allowedTransitions": []
},
"status": "SETTLED",
"statusHistory": [
{
"status": "INITIATED",
"timestamp": "2026-09-09T12:14:17.635605197Z",
"actor": "SYSTEM",
"message": "Transaction created"
},
{
"status": "SETTLED",
"timestamp": "2026-09-09T12:14:17.636995304Z",
"actor": "SYSTEM",
"message": "Investment created successfully."
}
],
"timestamps": {
"completedAt": "2026-09-09T12:14:17.637015Z",
"createdAt": "2026-09-09T12:14:17.692598Z",
"updatedAt": "2026-09-09T12:14:17.692614Z"
},
"transactionCategory": "DEBITED",
"transactionId": "TXN_2C9AAC4B7E184E7D",
"transactionType": "DEBITED",
"user": {
"id": 2,
"walletId": 1
}
},
{
"amount": {
"currency": "NGN",
"fee": 0E-8,
"gross": 500000.00000000,
"net": 500000.00000000,
"symbol": "₦",
"tax": 0E-8
},
"balance": {
"available": 0E-8,
"change": 500000.00000000,
"currency": "NGN",
"previous": 0E-8,
"running": 0E-8
},
"channel": "USSD",
"debitCredit": "CREDIT",
"description": "DEPOSIT//INTO DAVID ALEX NGN ACCOUNT",
"failure": null,
"historyId": 1,
"idempotencyKey": "COMP_DEP_USSD_2_7E0C42A14493",
"ledger": {
"ledgerEntryId": "LED_2bf440a86caa408dbf95def81582421b",
"ledgerStatus": "POSTED"
},
"metadata": {
"accountType": "NGN",
"paymentMethod": "USSD",
"walletId": 1
},
"provider": {
"name": "PAYSTACK",
"providerReference": "DEP_USSD_2_7E0C42A14493"
},
"reference": "DEP_USSD_2_7E0C42A14493",
"relatedTransactions": [],
"settlement": {
"settledAt": "2026-09-09T12:09:29.189666Z",
"settlementReference": "SET_2bf440a86caa408dbf95def81582421b",
"status": "SETTLED"
},
"stateMachine": {
"currentState": "DELIVERED",
"states": {
"INITIATED": {
"timestamp": "2026-09-09T12:09:29.186512Z",
"actor": "SYSTEM",
"message": "Transaction created",
"nextState": "PROCESSING",
"onFailure": false,
"failureState": null
},
"PROCESSING": {
"timestamp": "2026-09-09T12:09:29.189193Z",
"actor": "SYSTEM",
"message": "Transaction validated and processing started",
"nextState": "AUTHORIZED",
"onFailure": false,
"failureState": null
},
"AUTHORIZED": {
"timestamp": "2026-09-09T12:09:29.189219Z",
"actor": "PAYSTACK",
"message": "Payment authorized successfully",
"nextState": "SETTLEMENT_PENDING",
"onFailure": false,
"failureState": null
},
"SETTLEMENT_PENDING": {
"timestamp": "2026-09-09T12:09:29.189222Z",
"actor": "SYSTEM",
"message": "Payment authorized and awaiting settlement",
"nextState": "DELIVERED",
"onFailure": false,
"failureState": null
},
"DELIVERED": {
"timestamp": "2026-09-09T12:09:29.189534Z",
"actor": "SYSTEM",
"message": "Transaction completed successfully",
"nextState": null,
"onFailure": false,
"failureState": null
},
"FAILED": {
"timestamp": null,
"actor": null,
"message": null,
"nextState": null,
"onFailure": false,
"failureState": null
}
},
"allowedTransitions": [
{
"from": "INITIATED",
"to": "PROCESSING"
},
{
"from": "PROCESSING",
"to": "AUTHORIZED"
},
{
"from": "AUTHORIZED",
"to": "SETTLEMENT_PENDING"
},
{
"from": "SETTLEMENT_PENDING",
"to": "DELIVERED"
}
]
},
"status": "DELIVERED",
"statusHistory": [
{
"status": "INITIATED",
"timestamp": "2026-09-09T12:09:29.186512Z",
"actor": "SYSTEM",
"message": "Transaction created"
},
{
"status": "PROCESSING",
"timestamp": "2026-09-09T12:09:29.189193Z",
"actor": "SYSTEM",
"message": "Transaction validated and processing started"
},
{
"status": "AUTHORIZED",
"timestamp": "2026-09-09T12:09:29.189219Z",
"actor": "PAYSTACK",
"message": "Payment authorized successfully"
},
{
"status": "SETTLEMENT_PENDING",
"timestamp": "2026-09-09T12:09:29.189222Z",
"actor": "SYSTEM",
"message": "Payment authorized and awaiting settlement"
},
{
"status": "DELIVERED",
"timestamp": "2026-09-09T12:09:29.189534Z",
"actor": "SYSTEM",
"message": "Transaction completed successfully"
}
],
"timestamps": {
"completedAt": "2026-09-09T12:09:29.189666Z",
"createdAt": "2026-09-09T12:09:29.253943Z",
"updatedAt": "2026-09-09T12:09:29.253965Z"
},
"transactionCategory": "WALLET_FUNDING",
"transactionId": "2bf440a8-6caa-408d-bf95-def81582421b",
"transactionType": "DEPOSIT",
"user": {
"accountHolder": "DAVID ALEX",
"id": 2,
"walletId": 1
}
}
],
"page": 0,
"size": 50,
"totalElements": 3,
"totalPages": 1,
"hasMore": false
}
}

# and user data

{
"success": true,
"data": {
"accountType": "INDIVIDUAL",
"createdAt": "2026-09-09T12:09:08.743203",
"email": "midtechdb@gmail.com",
"emailVerified": false,
"enabled": true,
"id": 2,
"kycDocuments": [],
"kycStatus": "NOT_SUBMITTED",
"kycTier": "TIER_1",
"personal": {
"address": "Flat 77 AQ",
"city": "Chiria",
"country": "India",
"dateOfBirth": "1998-10-13",
"firstName": "David",
"fullName": "David Alex",
"gender": "MALE",
"lastName": "Alex",
"phoneNumber": "+24372879039434",
"profileComplete": true,
"profilePhotoUrl": "/uploads/images/2.png",
"referralCode": "48e97082-adc7-4a87-ad6d-59fd33fe211a",
"state": "Jharkhand",
"totalReferrals": 0
},
"phoneVerified": false,
"role": "USER",
"twoFactorEnabled": false,
"updatedAt": "2026-09-09T12:09:08.755035",
"username": "david"
}
}

# Caching User Data (Wallet Balances & Transaction History) in NGINX

Great question — this is one of the most valuable uses of NGINX caching. Wallet balances and transaction histories are **read-heavy, frequently requested** endpoints, so caching them dramatically reduces backend/database load. But financial data requires **careful cache invalidation** — you must never serve stale balance data after a transaction.

Let me break this down practically.

---

## ⚠️ Important Warning First

For **wallet balances and transaction data**, you should NOT use naive time-based caching like you would for images. If a user deposits ₦50,000 and the balance still shows the old value, that's a critical bug.

You need one of these strategies:

1. **Microcaching** (cache for 1–5 seconds only) — good enough to absorb traffic spikes
2. **Cache + Active Invalidation** (purge cache when a transaction happens)
3. **Cache per-user with short TTL** for history, longer for balances _with_ invalidation

---

## 🏗️ Approach 1: Microcaching Wallet Balances (Simplest, Safe)

Cache for just 2–5 seconds. Under high traffic, this absorbs 90%+ of requests while keeping data nearly real-time.

```nginx
http {
    proxy_cache_path /var/cache/nginx/wallet_cache
        levels=1:2
        keys_zone=wallet_cache:20m
        max_size=500m
        inactive=10m
        use_temp_path=off;
}

server {
    # Wallet balance endpoint — microcache
    location /api/v1/wallet/balances {
        proxy_cache wallet_cache;

        # Cache per user (critical — never share between users!)
        proxy_cache_key "$scheme$host$request_uri$http_authorization";

        # Only cache successful responses
        proxy_cache_valid 200 5s;
        proxy_cache_valid 404 1s;

        # Serve stale if backend is down (graceful degradation)
        proxy_cache_use_stale error timeout updating http_500 http_502 http_503 http_504;

        # Prevent cache stampede (only one request goes to backend)
        proxy_cache_lock on;
        proxy_cache_lock_timeout 3s;

        # Bypass cache for authenticated writes
        proxy_cache_bypass $http_cache_control;
        proxy_no_cache $http_cache_control;

        add_header X-Cache-Status $upstream_cache_status;

        proxy_pass http://backend;
    }
}
```

**Key point:** `proxy_cache_key` includes `$http_authorization` — this ensures **User A never sees User B's balance**. If you don't do this, you have a serious security breach.

---

## 🏗️ Approach 2: Cache Transaction History (Longer TTL + Invalidation)

Transaction history for _past_ transactions rarely changes, so you can cache it longer — but new transactions must invalidate it.

```nginx
    location /api/v1/transactions {
        proxy_cache wallet_cache;

        # Cache per user + query params (page, size, etc.)
        proxy_cache_key "$scheme$host$request_uri$http_authorization";

        # Cache history for 30 seconds
        proxy_cache_valid 200 30s;

        proxy_cache_use_stale updating error timeout;
        proxy_cache_lock on;

        add_header X-Cache-Status $upstream_cache_status;

        proxy_pass http://backend;
    }
```

---

## 🏗️ Approach 3: Active Cache Invalidation (Best for Financial Data)

When a transaction occurs (deposit, investment, withdrawal), the backend should **purge** that user's cached balance and history. This gives you fast reads + always-correct data.

**Step 1: Enable PURGE method in NGINX**

```nginx
http {
    proxy_cache_path /var/cache/nginx/wallet_cache
        levels=1:2
        keys_zone=wallet_cache:20m
        max_size=500m
        inactive=10m;
}

server {
    # Internal purge endpoint — only accessible from localhost/backend
    location ~ ^/api/v1/wallet/balances$ {
        allow 127.0.0.1;
        allow 10.0.0.0/8;   # your backend subnet
        deny all;

        proxy_cache_purge wallet_cache "$scheme$host$request_uri$http_authorization";
    }
}
```

**Step 2: Backend calls purge after any transaction**

After your backend creates a transaction (like the `DEBITED` entries in your data), it calls:

```bash
curl -X PURGE http://nginx/api/v1/wallet/balances \
     -H "Authorization: Bearer <user_token>"
```

This forces NGINX to drop the stale cache entry so the next request fetches fresh data.

---

## 🏗️ Approach 4: Use a Shared Cache Key Instead of Auth Header

Using `$http_authorization` in the cache key works but is inefficient (auth tokens rotate, cache misses increase). A better approach is to have the backend return a `X-User-Id` header and cache on that:

```nginx
    location /api/v1/wallet/balances {
        proxy_cache wallet_cache;

        # Cache per user id returned by backend
        proxy_cache_key "$scheme$host$uri$upstream_http_x_user_id";

        proxy_cache_valid 200 5s;
        proxy_cache_lock on;
        proxy_cache_use_stale updating error timeout;

        add_header X-Cache-Status $upstream_cache_status;

        proxy_pass http://backend;
    }
```

Your backend should return:

```
X-User-Id: 2
```

---

## 🏗️ Approach 5: Cache the Wallet Currencies List (Rarely Changes)

The multi-currency list (NGN, USD, EUR, etc. with symbols) almost never changes — cache it for hours.

```nginx
    location /api/v1/wallet/currencies {
        proxy_cache wallet_cache;
        proxy_cache_key "$scheme$host$request_uri";
        proxy_cache_valid 200 12h;
        proxy_cache_use_stale error timeout updating;

        add_header X-Cache-Status $upstream_cache_status;

        proxy_pass http://backend;
    }
```

---

## 📊 Recommended TTL Strategy for Your Data

| Endpoint                  | Cache TTL | Invalidation         | Reason                       |
| ------------------------- | --------- | -------------------- | ---------------------------- |
| `/wallet/balances`        | 2–5s      | PURGE on transaction | Fast-changing financial data |
| `/wallet/currencies`      | 12h       | Manual/Rare          | Static reference data        |
| `/transactions` (history) | 30s       | PURGE on new txn     | Append-only, mostly stable   |
| `/transactions/{id}`      | 5m        | Never (immutable)    | Individual txns don't change |
| `/user/profile`           | 10m       | PURGE on update      | Rarely changes               |

---

## 🚀 Approach 6: Application-Level Cache (Redis) — Often Better

For user-specific financial data, **Redis is usually a better fit than NGINX caching** because:

- You can invalidate precisely per user/key
- You can store structured data (JSON)
- You control TTL per entry from your app code
- No issues with auth headers in cache keys

**Typical pattern:**

```
Request → Check Redis (key: wallet:balance:user:2)
   ├─ HIT  → Return immediately (< 1ms)
   └─ MISS → Query DB → Store in Redis (TTL 5-10s) → Return
```

Then after a transaction:

```
Transaction created → DEL wallet:balance:user:2 → next read repopulates
```

You can combine this with NGINX as a front-line microcache for extra protection.

---

## ✅ Best Practice Summary

For **wallet balances and user history** in a fintech app:

1. **Don't use long TTLs** — 2–5 seconds max for balances
2. **Always cache per user** (`$http_authorization` or `X-User-Id` in key)
3. **Use `proxy_cache_lock`** to prevent stampede on cache miss
4. **Use `proxy_cache_use_stale`** so users still get data if backend is slow
5. **Actively PURGE** on every transaction (deposit, debit, investment)
6. **Consider Redis** for precise per-user invalidation — it's the industry standard for this
7. **Add `X-Cache-Status`** header for monitoring HIT/MISS rates

Would you like me to show a full **Redis + NGINX combined setup** for your wallet balance endpoint?
