# ePay — Full-Stack Fintech Payment Platform

A production-grade digital payments platform built with Spring Boot 4 and Next.js. ePay allows users to manage multi-currency wallets, pay bills, transfer funds, invest, manage virtual cards, and more — all behind a hardened, multi-layered security architecture.

---

## Table of Contents

- [About the App](#about-the-app)
- [Problem It Solves](#problem-it-solves)
- [Tech Stack](#tech-stack)
- [Architecture Overview](#architecture-overview)
- [Features](#features)
- [Backend Modules](#backend-modules)
- [API Endpoints](#api-endpoints)
- [Security](#security)
- [Infrastructure & DevOps](#infrastructure--devops)
- [Nginx Configuration](#nginx-configuration)
- [Environment Variables](#environment-variables)
- [Getting Started](#getting-started)
- [Project Structure](#project-structure)
- [Roadmap](#roadmap)

---

## About the App

ePay is a feature-complete digital banking and payments platform. It provides a unified wallet that supports multiple currencies, payment gateways (Paystack and Flutterwave), bills payment for utility services, virtual debit cards, investment plans, escrow, and a full administrative back-office.

The backend is a Java 21 Spring Boot multi-module monolith with 18 Maven modules. The frontend is a Next.js 16 app with server-side rendering and an App Router architecture. Everything runs inside Docker, behind a hardened Nginx reverse proxy with WAF-level protection.

---

## Problem It Solves

Managing money in Africa involves fragmented tools: one app for bank transfers, another for airtime, another for electricity bills, and yet another for investments. ePay consolidates all of these into a single platform:

- A multi-currency wallet you can fund via card or bank transfer
- Instant peer-to-peer transfers using just a username
- All utility bills (airtime, data, cable TV, electricity, betting) in one place
- Fixed-income investment plans with predictable returns
- Virtual dollar/naira cards for online spending
- A full admin back-office for compliance, support, and liquidity management

---

## Tech Stack

### Backend

| Technology            | Version                    | Purpose                                 |
| --------------------- | -------------------------- | --------------------------------------- |
| Java                  | 21                         | Runtime — virtual thread capable        |
| Spring Boot           | 4.1.0                      | Framework                               |
| Spring Security       | 6 + OAuth2 Resource Server | Authentication & authorization          |
| PostgreSQL            | 16                         | Primary relational database             |
| Redis                 | 7                          | Session storage, rate-limiting, caching |
| RabbitMQ              | 3.13                       | Async messaging (notifications, events) |
| JJWT                  | 0.11.5                     | JWT creation and validation             |
| MapStruct             | 1.4.2                      | DTO mapping                             |
| Lombok                | 1.18.40                    | Boilerplate reduction                   |
| Micrometer + Brave    | latest                     | Distributed tracing (Zipkin)            |
| Prometheus Micrometer | latest                     | Metrics export                          |
| springdoc-openapi     | 2.8.5                      | Swagger UI + OpenAPI 3 docs             |
| Paystack SDK          | REST                       | Deposit, withdrawal, bank verification  |
| Flutterwave SDK       | REST                       | Deposit, withdrawal                     |
| Twilio                | 10.5.1                     | SMS OTP delivery                        |
| Spring Mail           | latest                     | Email (Gmail SMTP)                      |
| Thymeleaf             | latest                     | Email templates                         |
| TOTP                  | dev.samstevens 1.7.1       | Google Authenticator-compatible 2FA     |
| Snowflake ID          | 1.0.0                      | Distributed-safe unique ID generation   |
| Caffeine              | latest                     | In-process L1 cache                     |

### Frontend

| Technology   | Version | Purpose                                 |
| ------------ | ------- | --------------------------------------- |
| Next.js      | 16.1.1  | Full-stack React framework (App Router) |
| React        | 19      | UI library                              |
| TypeScript   | 5       | Type safety                             |
| Tailwind CSS | v4      | Styling                                 |
| Lucide React | 0.562   | Icons                                   |
| Font Awesome | 7       | Icons                                   |
| react-select | 5       | Select components                       |
| react-switch | 7       | Toggle switches                         |
| jsonwebtoken | 9       | Client-side JWT decode                  |

### Infrastructure

| Service           | Image                                     | Purpose                                |
| ----------------- | ----------------------------------------- | -------------------------------------- |
| Nginx             | 1.27-alpine                               | Reverse proxy + WAF + load balancer    |
| PostgreSQL        | 16-alpine                                 | Primary relational database            |
| Redis             | 7-alpine                                  | Cache + sessions (LRU, 256 MB cap)     |
| RabbitMQ          | 3.13-management-alpine                    | Async message broker                   |
| Zipkin            | latest                                    | Distributed tracing                    |
| Prometheus        | v2.51.2                                   | Metrics scraping (15-day retention)    |
| Grafana           | 10.4.2                                    | Metrics dashboards (port 3010)         |
| Loki              | custom build (`epay/monitoring/loki`)     | Log aggregation                        |
| Promtail          | custom build (`epay/monitoring/promtail`) | Log shipping (Docker + app logs)       |
| postgres-exporter | prometheuscommunity/postgres-exporter     | PostgreSQL → Prometheus metrics (9187) |
| redis-exporter    | oliver006/redis_exporter:v1.62.0          | Redis → Prometheus metrics (9121)      |
| nginx-exporter    | nginx/nginx-prometheus-exporter:1.1.0     | Nginx stub_status → Prometheus (9113)  |

---

## Architecture Overview

```
Internet
    │
    ▼
┌─────────────────────────────────────┐
│           Nginx 1.27                │
│  • WAF-style attack detection       │
│  • Rate limiting (11 req zones)     │
│  • Security headers                 │
│  • Bot blocking (500+ UA strings)   │
│  • CSP, HSTS, Permissions-Policy    │
│  • Load balancer (least_conn)       │
└──────────┬──────────────────────────┘
           │
    ┌──────┴──────────────────┐
    │                         │
    ▼                         ▼
┌─────────────────┐    ┌─────────────────────────────┐
│  Next.js        │    │  Spring Boot Backend         │
│  frontend-1     │    │  epay-server-1  :8021        │
│  frontend-2     │    │  epay-server-2  :8022        │
│  frontend-3     │    │  epay-server-3  :8023        │
│  :3001–3003     │    │  (all internal port :8029)   │
└─────────────────┘    └──────────────┬──────────────┘
                                      │
               ┌──────────────────────┼──────────────────┐
               │                      │                  │
               ▼                      ▼                  ▼
        ┌──────────┐          ┌──────────┐      ┌──────────────┐
        │PostgreSQL│          │  Redis   │      │  RabbitMQ    │
        │ :5432    │          │  :6379   │      │  :5672       │
        └──────────┘          └──────────┘      └──────────────┘
```

**Traffic routing:**

- `GET /`, `/_next/*` and all non-API paths → Next.js frontend
- `/api/v1/**` → Spring Boot backend (path prefix stripped before reaching controllers)
- `/webhook/**` → Spring Boot (no auth, HMAC-verified at app layer)
- `/actuator/health` → Spring Boot (internal network only)

---

## Features

### User Features

#### Account & Identity

- Register with email + username
- Email and SMS verification (OTP)
- Password reset via email token
- Two-factor authentication (TOTP — Google Authenticator compatible, or OTP via SMS)
- Tiered KYC (Tier 0–3): document upload (National ID, Passport, Driver's License, BVN), admin review workflow, tier-based transaction limits
- Profile management with avatar upload

#### Wallet

- Multi-currency wallet (one wallet per user, multiple currency balances)
- Set a default currency
- Add new currencies to your wallet
- 4-digit wallet PIN for transaction authorization
- Peer-to-peer transfers via ePay username
- In-wallet currency swap
- Balance inquiry per currency

#### Funding & Payments

- Deposit funds via Paystack (card, bank transfer) or Flutterwave
- Withdraw to Nigerian bank accounts via Paystack or Flutterwave
- Webhook-verified payment callbacks for both gateways

#### Bills Payment

| Bill Type    | Details                                     |
| ------------ | ------------------------------------------- |
| Airtime      | All Nigerian networks                       |
| Data Bundles | All Nigerian networks                       |
| Cable TV     | DSTV, GOTV, STARTIMES                       |
| Electricity  | Prepaid token generation + postpaid payment |
| Betting      | Fund wallet at supported betting platforms  |
| Shopping     | Pay shopping orders                         |

All bills payments require `payment:create` permission and an active Redis session.

#### Virtual Cards

- Issue virtual debit cards
- Freeze and unfreeze cards
- Set spending limits and limit periods
- Enable/disable international transactions, ATM, contactless, online spending
- Merchant-binding controls
- Permanent cancellation with audit log

#### Investments

| Duration  | Return Rate     |
| --------- | --------------- |
| Weekly    | 8% annual rate  |
| Monthly   | 12% annual rate |
| Quarterly | 18% annual rate |
| Yearly    | 24% annual rate |

- View portfolio and individual investment details
- Calculate expected returns before investing
- Automatic maturity tracking

#### Beneficiaries

- Save bank beneficiaries (account number + bank)
- Save ePay user beneficiaries (by username)
- Search beneficiaries by name/account
- Soft-delete (recover later)
- Bulk delete

#### Transaction History

- Paginated full transaction history
- Filter by type, status, date range, currency
- Per-transaction status timeline
- Bank statement PDF generation (emailed to user)

#### Bank Accounts

- Link multiple Nigerian bank accounts
- Verify account number via Paystack
- Browse full list of Nigerian banks

#### Support Tickets

- Submit support tickets with subject and description
- Reply to ongoing tickets
- Track ticket status

#### Developer Portal

- Create developer applications
- Generate API keys (sandbox and live modes)
- Register webhooks and configure event subscriptions
- Test webhook delivery
- View webhook delivery logs
- Request live-mode access

---

### Admin Features

#### User Management (`ADMIN`, `SUPER_USER`, `CUSTOMER_SERVICE`)

- Search users with full-text filter
- View full user profile with account details
- Create, update, delete user accounts
- Lock/unlock accounts (failed login protection)
- Block/unblock accounts (compliance action)
- View platform-wide user statistics

#### Wallet Management

- View all wallets with pagination
- Freeze/unfreeze user wallets
- Set/reset wallet PIN on behalf of user
- View wallet transaction history
- View frozen wallet list
- Wallet-level statistics

#### Transaction Management

- Full-text search across all transactions
- Filter by user, type, status, date range
- Add internal admin notes
- Flag transactions for AML review
- Per-user transaction statistics

#### Support Ticket Management

- View and manage all tickets
- Filter by status, assigned agent, user
- Reply to tickets as staff
- Resolve, close, reopen, escalate
- View ticket stats and SLA overview

#### KYC Review

- View all pending KYC submissions
- Approve or reject with reason

#### Liquidity Management

- Monitor Paystack and Flutterwave float balances
- Sync gateway balances
- Record manual top-ups
- View platform ledger
- Check payout availability

#### Virtual Card Fees

- Configure card creation and maintenance fees per currency
- Enable/disable fee tiers
- Update fee amounts

#### Currency Management

- Manage supported currencies (code, symbol, exchange rate)
- Enable/disable currencies per region

#### Blacklist Management

- Blacklist accounts, IPs, or arbitrary identifiers
- Check if an account or IP is blacklisted
- Remove entries

#### Super Admin (SUPER_USER only)

- Create and manage staff accounts (ADMIN, CUSTOMER_SERVICE, EDITOR roles)
- Update staff roles
- Deactivate staff
- View platform-wide statistics

---

## Backend Modules

The backend is a Maven multi-module project with 18 modules:

```
epay/
├── domain/          # Shared JPA entities, DTOs, enums, repository interfaces
├── common/          # Security, JWT, filters, RabbitMQ, caching, exceptions
├── auth/            # Registration, login, KYC, 2FA, password reset, settings
├── wallet/          # Multi-currency wallet, transfers, swaps, PIN management
├── deposit/         # Paystack/Flutterwave deposits + inbound webhooks
├── withdraw/        # Bank withdrawals via Paystack/Flutterwave
├── bills/           # Airtime, data, cable TV, electricity, betting, shopping
├── virtual_card/    # Virtual card issuance and management
├── investment/      # Fixed-income investment plans
├── beneficiary/     # Bank and user beneficiary management
├── bank/            # Bank account management, Paystack bank verification
├── history/         # Transaction history, audit logs, statement generation
├── admin/           # Full admin back-office suite
├── notification/    # Async notifications via RabbitMQ (email + push)
├── blacklist/       # Blacklist service
├── escrow/          # Escrow (domain model complete, controller in progress)
├── savings/         # Savings plans (domain model complete, controller in progress)
└── main/            # Spring Boot application entry point
```

---

## API Endpoints

All user-facing endpoints are prefixed `/api/v1/` at the Nginx layer, which strips the prefix before forwarding to the backend. Controllers receive clean paths (e.g. `/auth/login`, `/wallet/transfer`).

### Authentication (`/auth`)

| Method | Path                        | Access        | Description                       |
| ------ | --------------------------- | ------------- | --------------------------------- |
| POST   | `/auth/register`            | Public        | Create account                    |
| POST   | `/auth/login`               | Public        | Login, returns JWT pair           |
| POST   | `/auth/send-verify-code`    | Public        | Send OTP (EMAIL or SMS)           |
| GET    | `/auth/verify`              | Public        | Verify account by token           |
| GET    | `/auth/resend-verification` | Public        | Resend verification token         |
| POST   | `/auth/forgot-password`     | Public        | Initiate password reset           |
| POST   | `/auth/reset-password`      | Public        | Confirm password reset            |
| POST   | `/auth/forgot-username`     | Public        | Recover username                  |
| GET    | `/auth/logout`              | USER          | Logout, invalidates Redis session |
| POST   | `/auth/refresh`             | Refresh token | Rotate JWT pair                   |

### User & Profile (`/user`, `/settings`, `/kyc`, `/receipt`)

| Method | Path                                  | Access | Description                         |
| ------ | ------------------------------------- | ------ | ----------------------------------- |
| GET    | `/user/profile`                       | USER   | Get own profile                     |
| PUT    | `/user/profile`                       | USER   | Update own profile                  |
| PUT    | `/user/settings/update-password`      | USER   | Change password                     |
| PUT    | `/user/settings/notifications`        | USER   | Update notification preferences     |
| POST   | `/user/2fa/toggle`                    | USER   | Enable/disable 2FA                  |
| DELETE | `/user/account`                       | USER   | Request account deletion            |
| POST   | `/settings/upload-profile-image/{id}` | USER   | Upload avatar                       |
| POST   | `/settings/enable-twofactor`          | USER   | Toggle 2FA via settings             |
| POST   | `/kyc/documents`                      | USER   | Upload KYC document                 |
| POST   | `/kyc/submit/{tier}`                  | USER   | Submit KYC for tier review          |
| GET    | `/kyc/status`                         | USER   | Get KYC status and tier             |
| POST   | `/receipt/generate-pdf/{userId}`      | USER   | Generate + email bank statement PDF |

### Wallet (`/wallet`)

| Method | Path                                             | Access | Description                     |
| ------ | ------------------------------------------------ | ------ | ------------------------------- |
| GET    | `/wallet/userId/{userId}`                        | USER   | Get wallet (ownership enforced) |
| GET    | `/wallet/{userId}/balance/{currency}`            | USER   | Get balance for a currency      |
| POST   | `/wallet`                                        | USER   | Create wallet                   |
| POST   | `/wallet/{userId}/currencies`                    | USER   | Add currency to wallet          |
| PATCH  | `/wallet/{userId}/currencies/{currency}/default` | USER   | Set default currency            |
| POST   | `/wallet/create/{userId}/pin`                    | USER   | Set wallet PIN                  |
| PUT    | `/wallet/{userId}/pin`                           | USER   | Change wallet PIN               |
| POST   | `/wallet/transfer`                               | USER   | Peer-to-peer transfer           |
| POST   | `/wallet/{userId}/swap`                          | USER   | Currency swap                   |

### Deposits & Withdrawals

| Method | Path                           | Access | Description                            |
| ------ | ------------------------------ | ------ | -------------------------------------- |
| POST   | `/deposit/initiate`            | USER   | Start deposit via Paystack/Flutterwave |
| POST   | `/deposit/verify`              | USER   | Verify deposit status                  |
| POST   | `/webhook/deposit/paystack`    | Public | Paystack payment webhook               |
| POST   | `/webhook/deposit/flutterwave` | Public | Flutterwave payment webhook            |
| POST   | `/withdrawals/user`            | USER   | Wallet-to-wallet withdrawal            |
| POST   | `/withdrawals/bank`            | USER   | Bank withdrawal                        |

### Bills Payment (`/bills`)

| Method | Path                 | Access | Description               |
| ------ | -------------------- | ------ | ------------------------- |
| POST   | `/bills/airtime`     | USER   | Purchase airtime          |
| POST   | `/bills/data`        | USER   | Purchase data bundle      |
| POST   | `/bills/cabletv`     | USER   | Pay cable TV subscription |
| POST   | `/bills/electricity` | USER   | Pay electricity bill      |
| POST   | `/bills/betting`     | USER   | Fund betting wallet       |
| POST   | `/bills/shopping`    | USER   | Pay shopping order        |

### Virtual Cards (`/virtual-cards`)

| Method | Path                               | Access | Description             |
| ------ | ---------------------------------- | ------ | ----------------------- |
| POST   | `/virtual-cards`                   | USER   | Issue new virtual card  |
| GET    | `/virtual-cards/{cardId}`          | USER   | Get masked card details |
| GET    | `/virtual-cards/{cardId}/details`  | USER   | Get full card details   |
| GET    | `/virtual-cards/user/{userId}`     | USER   | List user's cards       |
| POST   | `/virtual-cards/{cardId}/freeze`   | USER   | Freeze card             |
| POST   | `/virtual-cards/{cardId}/unfreeze` | USER   | Unfreeze card           |
| POST   | `/virtual-cards/{cardId}/cancel`   | USER   | Cancel card permanently |
| DELETE | `/virtual-cards/{cardId}`          | USER   | Soft-delete card        |

### Investments (`/investments`)

| Method | Path                         | Access | Description                |
| ------ | ---------------------------- | ------ | -------------------------- |
| GET    | `/investments/plans`         | USER   | List available plans       |
| POST   | `/investments/calculate`     | USER   | Calculate expected returns |
| POST   | `/investments/create`        | USER   | Create investment          |
| GET    | `/investments/user/{userId}` | USER   | List user's investments    |
| GET    | `/investments/{id}`          | USER   | Get investment by ID       |

### Beneficiaries (`/beneficiaries`)

| Method | Path                             | Access | Description             |
| ------ | -------------------------------- | ------ | ----------------------- |
| POST   | `/beneficiaries/create`          | USER   | Save beneficiary        |
| GET    | `/beneficiaries/{userId}/all`    | USER   | List all beneficiaries  |
| GET    | `/beneficiaries/{userId}/search` | USER   | Search beneficiaries    |
| PUT    | `/beneficiaries/{id}`            | USER   | Update beneficiary      |
| DELETE | `/beneficiaries/{id}`            | USER   | Soft-delete beneficiary |
| DELETE | `/beneficiaries/bulk`            | USER   | Bulk soft-delete        |

### Transaction History (`/history`)

| Method | Path                                | Access     | Description                                     |
| ------ | ----------------------------------- | ---------- | ----------------------------------------------- |
| GET    | `/history`                          | USER       | Paginated own history                           |
| GET    | `/history/user/{userId}/filter`     | USER/ADMIN | Filtered history (date, type, currency, status) |
| GET    | `/history/{transactionId}`          | USER       | Single transaction                              |
| GET    | `/history/{transactionId}/timeline` | USER       | Status timeline                                 |
| GET    | `/history/summary`                  | USER       | Aggregated totals by type                       |

### Bank Accounts (`/bank`)

| Method | Path                  | Access | Description                        |
| ------ | --------------------- | ------ | ---------------------------------- |
| POST   | `/bank`               | USER   | Save bank account                  |
| GET    | `/bank/user/{userId}` | USER   | List saved bank accounts           |
| DELETE | `/bank`               | USER   | Delete bank accounts by IDs        |
| GET    | `/bank/list`          | USER   | List Nigerian banks (via Paystack) |
| GET    | `/bank/verify`        | USER   | Verify account number + bank code  |

### Support Tickets (`/tickets`)

| Method | Path                        | Access | Description           |
| ------ | --------------------------- | ------ | --------------------- |
| POST   | `/tickets`                  | USER   | Submit support ticket |
| GET    | `/tickets`                  | USER   | List own tickets      |
| GET    | `/tickets/{ticketId}`       | USER   | Get ticket            |
| POST   | `/tickets/{ticketId}/reply` | USER   | Reply to ticket       |

### Admin Panel

#### User Management (`/admin/users`)

| Method | Path                      | Roles                 | Description         |
| ------ | ------------------------- | --------------------- | ------------------- |
| GET    | `/admin/users`            | ADMIN, SUPER_USER, CS | Search/filter users |
| GET    | `/admin/users/stats`      | ADMIN, SUPER_USER     | Dashboard stats     |
| POST   | `/admin/users/{id}/block` | ADMIN, SUPER_USER, CS | Block account       |
| POST   | `/admin/users/{id}/lock`  | ADMIN, SUPER_USER     | Lock account        |
| DELETE | `/admin/users/{id}`       | SUPER_USER            | Delete account      |

#### Wallet Management (`/admin/wallets`)

| Method | Path                                | Roles                 | Description     |
| ------ | ----------------------------------- | --------------------- | --------------- |
| PATCH  | `/admin/wallets/user/{id}/freeze`   | ADMIN, SUPER_USER, CS | Freeze wallet   |
| PATCH  | `/admin/wallets/user/{id}/unfreeze` | ADMIN, SUPER_USER, CS | Unfreeze wallet |
| POST   | `/admin/wallets/user/{id}/pin`      | ADMIN, SUPER_USER, CS | Set wallet PIN  |

#### Transaction Management (`/admin/transactions`)

| Method | Path                            | Roles                 | Description             |
| ------ | ------------------------------- | --------------------- | ----------------------- |
| GET    | `/admin/transactions`           | ADMIN, SUPER_USER, CS | Search all transactions |
| PATCH  | `/admin/transactions/{id}/note` | ADMIN, SUPER_USER, CS | Add admin note          |
| PATCH  | `/admin/transactions/{id}/flag` | ADMIN, SUPER_USER     | Flag for AML review     |

#### Liquidity Management (`/admin/liquidity`)

| Method | Path                                  | Roles             | Description               |
| ------ | ------------------------------------- | ----------------- | ------------------------- |
| GET    | `/admin/liquidity/paystack/status`    | ADMIN, SUPER_USER | Paystack float status     |
| GET    | `/admin/liquidity/flutterwave/status` | ADMIN, SUPER_USER | Flutterwave float status  |
| GET    | `/admin/liquidity/ledger`             | ADMIN, SUPER_USER | Platform ledger           |
| GET    | `/admin/liquidity/payout-check`       | ADMIN, SUPER_USER | Check payout availability |

#### Super Admin (`/admin/super`)

| Method | Path                           | Roles      | Description          |
| ------ | ------------------------------ | ---------- | -------------------- |
| POST   | `/admin/super/staff`           | SUPER_USER | Create staff account |
| GET    | `/admin/super/staff`           | SUPER_USER | List all staff       |
| PATCH  | `/admin/super/staff/{id}/role` | SUPER_USER | Change staff role    |
| DELETE | `/admin/super/staff/{id}`      | SUPER_USER | Deactivate staff     |
| GET    | `/admin/super/stats`           | SUPER_USER | Platform statistics  |

### Developer Portal (`/developer`)

| Method | Path                                        | Access | Description          |
| ------ | ------------------------------------------- | ------ | -------------------- |
| POST   | `/developer/apps`                           | USER   | Create developer app |
| GET    | `/developer/apps`                           | USER   | List my apps         |
| POST   | `/developer/apps/{id}/keys/{mode}/rotate`   | USER   | Rotate API key       |
| POST   | `/developer/apps/{id}/webhooks`             | USER   | Register webhook     |
| POST   | `/developer/apps/{id}/webhooks/{mode}/test` | USER   | Test webhook         |
| GET    | `/developer/apps/{id}/webhooks/{id}/logs`   | USER   | Delivery logs        |
| POST   | `/developer/apps/{id}/live/request`         | USER   | Request live mode    |

---

## Security

ePay implements security at three independent layers: Nginx, Spring Security filters, and method-level annotations.

### Layer 1 — Nginx (Network/Transport)

**Rate Limiting (11 dedicated zones + 2 connection zones):**

- `auth_limit` — 10 req/min per IP on auth endpoints
- `api_limit` — 20 req/s per IP on general API locations
- `strict_limit` — 5 req/s per IP on sensitive endpoints (bills, deposit, withdraw, super admin)
- `global_limit` — 100 req/s per IP catch-all global cap
- `bot_limit` — 2 req/s per IP for detected bot traffic
- `txn_limit` — 5 req/min per IP+URI on transaction endpoints
- `ddos_protect` — 50 req/s per IP DDoS mitigation layer
- `uri_limit` — 10 req/s per URI for hotspot protection
- `user_api_limit` — 5 req/s per IP for user-facing endpoints
- `per_ip_user` — 3 req/s tighter per-IP cap for authenticated users
- `per_user` — 100 req/min per JWT user ID (extracted from Bearer token payload)
- `conn_limit` (connection) — per-IP connection count limit (429 on overflow)
- `serv_limit` (connection) — server-wide connection cap

**Security Headers:**

- `Strict-Transport-Security: max-age=63072000; includeSubDomains; preload` (2-year HSTS)
- `Content-Security-Policy: default-src 'none'` (strict; no inline scripts)
- `X-Frame-Options: DENY`
- `X-Content-Type-Options: nosniff`
- `Referrer-Policy: no-referrer`
- `Permissions-Policy` — disables 20+ browser features including camera, microphone, payment, geolocation
- `Cross-Origin-Embedder-Policy: require-corp`
- `Cross-Origin-Opener-Policy: same-origin-allow-popups`
- Hides `X-Powered-By` and `X-Runtime` headers

**WAF-Style Attack Blocking (location-level regex rules):**

- SQL injection patterns in URI → 403 with JSON error code
- XSS patterns (`<script`, `javascript:`, `onerror=`, `eval(`) → 403
- Path traversal (`../`, `/etc/passwd`, `/.git`, `/.env`) → 403
- Command injection (`wget`, `curl http`, `/bin/bash`, `cmd.exe`, `powershell.exe`) → 403
- Common scanner paths (`wp-admin`, `phpmyadmin`, `adminer`, `jenkins`) → 404
- Backup/temp files (`.bak`, `.tmp`, `.swp`, `.log`) → 404
- Dot files (`.env`, `.git`, `.htaccess`, `Dockerfile`) → 404

**Bot Blocking:**

- Global blacklist of 500+ known bad bot User-Agent strings: sqlmap, nikto, nessus, masscan, Shodan, nuclei, Acunetix, GPTBot, ClaudeBot, AhrefsBot, SemrushBot, and hundreds more
- Missing or blank User-Agent → blocked
- Python/Scrapy/curl/wget scripts in production → blocked
- Good bots (Googlebot, Bingbot, Slackbot) are whitelisted

### Layer 2 — Spring Security Filter Chain

Filters execute in this order on every request:

1. **FirewallExceptionFilter** — wraps `StrictHttpFirewall` rejections into clean JSON errors
2. **BotDetectionFilter** — matches User-Agent against known scanner strings, blocks known attack tools
3. **InputValidationFilter** — sanitizes request input before it reaches business logic
4. **SecurityHeadersFilter** — adds response security headers
5. **RateLimitingFilter** — Redis-backed sliding-window rate limiting per IP and per authenticated user, with exponential back-off penalty and IP blocking on repeated violations. Returns `X-Rate-Limit-Limit`, `X-Rate-Limit-Remaining`, and `Retry-After` headers
6. **JwtAuthenticationFilter** — extracts and validates JWT, enforces token type (ACCESS vs REFRESH), checks account status (LOCKED/INACTIVE blocks before reaching controllers)

**`StrictHttpFirewall` configuration:**

- Blocks: semicolons, backslashes, null bytes, percent-encoded slashes, double slashes, URL-encoded line feeds and carriage returns

**JWT Security:**

- Dual-token model: short-lived ACCESS token and REFRESH token
- REFRESH tokens are explicitly rejected on all non-refresh endpoints at filter level
- JWT claims: `roles`, `userId`, `permissions`, `accountStatus`, `token.type`, `acr` (MFA level), `amr`, `sid` (session ID)
- Signing: HMAC-SHA256 with Base64-encoded secret

**Session Management:**

- Stateless JWT — no server-side HTTP sessions
- Redis session tracking: each login creates a `session:id:{sessionId}` key in Redis
- Logout invalidates the Redis key (true server-side revocation without token blacklisting)

### Layer 3 — Method-Level Authorization (`@PreAuthorize`)

Fine-grained SpEL expressions via the `@security` bean:

```java
// Ownership check — user can only access their own resources
@PreAuthorize("hasRole('USER') and @security.isOwner(#userId)")

// Fine-grained permission
@PreAuthorize("@security.hasPermission('payment:create')")

// Require active Redis session (server-side revocation)
@PreAuthorize("@security.hasValidSession()")

// Require MFA authentication in the JWT
@PreAuthorize("@security.requiresMfa()")

// Combine freely
@PreAuthorize("hasRole('USER') and @security.hasPermission('wallet:read') and @security.hasValidSession()")
```

**Permission matrix:**
| Role | Permissions |
|---|---|
| USER | `payment:create`, `payment:read`, `wallet:read`, `crypto:deposit` |
| ADMIN | + `payment:update`, `wallet:update`, `wallet:freeze`, `user:read`, `user:update`, `admin:access` |
| SUPER_USER | + `payment:delete`, `user:delete`, `crypto:withdraw` |
| CUSTOMER_SERVICE | `payment:read`, `wallet:read`, `wallet:freeze`, `user:read`, `admin:access` |
| EDITOR | `payment:read`, `wallet:read`, `user:read`, `admin:access` |

### Two-Factor Authentication

- TOTP (Time-based One-Time Password) — compatible with Google Authenticator, Authy
- SMS OTP via Twilio for users without an authenticator app
- MFA status encoded in JWT `acr` claim: `urn:epay:auth:mfa`

---

## Infrastructure & DevOps

The entire stack runs from a single `docker-compose.yml` at the project root. Every service uses a custom-built image (not pulled directly) so configuration is baked in at build time.

### Service Topology

```
postgres ──┐
redis    ──┼──► epay-server-1 ─┐
rabbitmq ──┘    epay-server-2 ─┼──► frontend-1 ─┐
                epay-server-3 ─┘    frontend-2 ─┼──► nginx (port 80/443)
                                    frontend-3 ─┘
```

All `depends_on` conditions use `condition: service_healthy` — services only start after their dependencies pass their health checks. Nginx waits for all 3 backend instances and all 3 frontend instances to be healthy before it accepts traffic.

### Services, Images & Ports

| Service             | Image                                            | Host Port(s) | Container Port | Role                                            |
| ------------------- | ------------------------------------------------ | ------------ | -------------- | ----------------------------------------------- |
| `postgres`          | `postgres:16-alpine`                             | 5432         | 5432           | Primary relational database                     |
| `redis`             | `redis:7-alpine`                                 | 6379         | 6379           | Session store, rate-limit state, cache          |
| `rabbitmq`          | `rabbitmq:3.13-management-alpine`                | 5672, 15672  | 5672, 15672    | Async message broker + management UI            |
| `zipkin`            | `openzipkin/zipkin:latest`                       | 9411         | 9411           | Distributed tracing (in-memory storage)         |
| `epay-server-1`     | `epay-server:latest` (built from `./epay`)       | **8021**     | 8029           | Backend replica 1 — `INSTANCE_ID=epay-server-1` |
| `epay-server-2`     | `epay-server:latest`                             | **8022**     | 8029           | Backend replica 2 — `INSTANCE_ID=epay-server-2` |
| `epay-server-3`     | `epay-server:latest`                             | **8023**     | 8029           | Backend replica 3 — `INSTANCE_ID=epay-server-3` |
| `frontend-1`        | `epay-frontend:latest` (built from `./frontend`) | **3001**     | 3000           | Next.js replica 1                               |
| `frontend-2`        | `epay-frontend:latest`                           | **3002**     | 3000           | Next.js replica 2                               |
| `frontend-3`        | `epay-frontend:latest`                           | **3003**     | 3000           | Next.js replica 3                               |
| `nginx`             | `epay-nginx:latest` (built from `./nginx`)       | **80**, 443  | 80, 443        | Reverse proxy + WAF + load balancer             |
| `prometheus`        | `epay-prometheus:latest`                         | 9090         | 9090           | Metrics scraping (15-day retention)             |
| `grafana`           | `epay-grafana:latest`                            | **3010**     | 3000           | Dashboards (provisioned from files)             |
| `loki`              | `epay-loki:latest`                               | 3100         | 3100           | Log aggregation backend                         |
| `promtail`          | `epay-promtail:latest`                           | —            | —              | Ships Docker container logs + app logs to Loki  |
| `postgres-exporter` | `prometheuscommunity/postgres-exporter`          | 9187         | 9187           | PostgreSQL → Prometheus metrics                 |
| `redis-exporter`    | `oliver006/redis_exporter:v1.62.0`               | 9121         | 9121           | Redis → Prometheus metrics                      |
| `nginx-exporter`    | `nginx/nginx-prometheus-exporter:1.1.0`          | 9113         | 9113           | Nginx stub_status → Prometheus metrics          |

### YAML Anchor Pattern (DRY Compose)

Both the backend and frontend use YAML anchors to avoid repeating configuration across replicas:

```yaml
epay-server-base: &server-base
  build: { context: ./epay, dockerfile: Dockerfile }
  image: epay-server:latest
  environment:
    SERVER_PORT: 8029
    INSTANCE_ID: ${HOSTNAME:-unknown}
    # ... all shared env vars
  healthcheck: ...
  networks: [epay-network]

epay-server-1:
  <<: *server-base # inherit everything
  container_name: epay-server-1
  ports: ["8021:8029"]
  environment:
    INSTANCE_ID: epay-server-1 # override only what differs

epay-server-2:
  <<: *server-base
  container_name: epay-server-2
  ports: ["8022:8029"]
  environment:
    INSTANCE_ID: epay-server-2
```

The `INSTANCE_ID` is injected into Spring Boot via `${INSTANCE_ID}` and surfaced on every response as the `X-Instance-ID` header, making it trivial to identify which replica handled a request.

The same `&frontend-base` anchor pattern is used for `frontend-1/2/3`.

### Health Checks

| Service         | Command                                           | Interval | Retries | Start Period |
| --------------- | ------------------------------------------------- | -------- | ------- | ------------ |
| `postgres`      | `pg_isready -U $DB_USERNAME -d epay`              | 10s      | 5       | 20s          |
| `redis`         | `redis-cli ping`                                  | 10s      | 5       | —            |
| `rabbitmq`      | `rabbitmq-diagnostics check_port_connectivity`    | 10s      | 5       | 30s          |
| `epay-server-*` | `wget -qO- http://localhost:8029/actuator/health` | 30s      | 3       | **60s**      |
| `frontend-*`    | `wget -qO- http://localhost:3000`                 | 30s      | 3       | 40s          |
| `nginx`         | `wget --spider http://localhost/nginx-health`     | 30s      | 3       | 40s          |

The 60-second start period on backend instances accounts for JVM startup and Spring context initialization time.

### Backend Runtime Tuning

The shared `epay-server-base` anchor includes tuned environment variables for connection pools and message consumers:

**HikariCP (database connection pool):**
| Parameter | Value | Effect |
| -------------------------- | -------- | --------------------------------------------- |
| `MAXIMUM-POOL-SIZE` | 10 | Max simultaneous DB connections per instance |
| `MINIMUM-IDLE` | 5 | Connections held open even when idle |
| `IDLE-TIMEOUT` | 300000ms | Close idle connections after 5 minutes |
| `CONNECTION-TIMEOUT` | 30000ms | Fail-fast if pool is exhausted for 30s |
| `MAX-LIFETIME` | 1800000ms| Recycle connections every 30 minutes |

**Redis Lettuce pool:**
| Parameter | Value | Effect |
| ------------ | ----- | ----------------------------------- |
| `MAX-ACTIVE` | 20 | Max concurrent Redis connections |
| `MAX-IDLE` | 10 | Max idle connections in pool |
| `MIN-IDLE` | 5 | Connections held open when idle |

**RabbitMQ listener:**
| Parameter | Value | Effect |
| ----------------- | ----- | ----------------------------------------------- |
| `CONCURRENCY` | 3 | Min consumer threads per instance |
| `MAX-CONCURRENCY` | 10 | Max consumer threads under load |
| `PREFETCH` | 5 | Messages fetched ahead per consumer thread |

**Redis configuration flags** (set on the `redis` service command):

```
--appendonly yes            # AOF persistence — survives restarts
--maxmemory 256mb           # Hard cap at 256 MB
--maxmemory-policy allkeys-lru  # Evict least-recently-used keys when full
```

### Docker Network

All services run on a single bridge network `epay-network` with a custom subnet:

```yaml
networks:
  epay-network:
    driver: bridge
    driver_opts:
      com.docker.network.bridge.enable_ip_masquerade: "true"
    ipam:
      config:
        - subnet: 172.28.0.0/16
```

This fixed subnet ensures the Nginx `set_real_ip_from` and `allow/deny` directives always match the Docker bridge range. The `172.16.0.0/12` block in Nginx's real-IP and health-check allow-lists covers this subnet.

### Named Volumes

| Volume            | Mounted By                  | Content                                      |
| ----------------- | --------------------------- | -------------------------------------------- |
| `postgres_data`   | postgres                    | PostgreSQL data files                        |
| `redis_data`      | redis                       | Redis AOF persistence                        |
| `rabbitmq_data`   | rabbitmq                    | RabbitMQ message store                       |
| `app_uploads`     | epay-server-1/2/3           | User-uploaded files (shared across replicas) |
| `epay_logs`       | epay-server-1/2/3, promtail | Application log files shipped by Promtail    |
| `prometheus_data` | prometheus                  | Metrics time-series (15-day retention)       |
| `grafana_data`    | grafana                     | Dashboard state and user config              |
| `loki_data`       | loki                        | Log index and chunks                         |
| `nginx_logs`      | nginx                       | Nginx log files (`/var/log/nginx/`)          |

The `app_uploads` volume is shared read-write across all three backend instances so uploaded files are accessible from any replica.

### Log Rotation (Docker)

Every service sets JSON log rotation via Docker's `json-file` driver to prevent disk exhaustion:

| Service group                          | Max file size | Max files | Max total  |
| -------------------------------------- | ------------- | --------- | ---------- |
| postgres, redis, rabbitmq, epay-server | 50 MB         | 3–5       | 150–250 MB |
| nginx                                  | 10 MB         | 5         | 50 MB      |
| grafana, prometheus, loki, promtail    | 20 MB         | 3         | 60 MB      |
| exporters                              | 10 MB         | 2         | 20 MB      |

### Backend Docker Build

The backend Dockerfile expects a pre-built fat JAR. Build first, then let Docker Compose copy it in:

```bash
cd epay
mvn clean package -DskipTests
cd ..
docker compose up --build
```

The JAR is read from `main/target/*.jar`. You must rebuild the JAR whenever backend code changes.

### Observability Stack

| Tool            | URL                                                             | Purpose                                          |
| --------------- | --------------------------------------------------------------- | ------------------------------------------------ |
| Zipkin          | http://localhost:9411                                           | Distributed traces via Micrometer + Brave        |
| Prometheus      | http://localhost:9090                                           | Scrapes metrics every 15s, 15-day retention      |
| Grafana         | http://localhost:3010                                           | Dashboards auto-provisioned from `provisioning/` |
| Loki            | http://localhost:3100                                           | Log aggregation — queried through Grafana        |
| Spring Actuator | `/actuator/health`, `/actuator/metrics`, `/actuator/prometheus` | Per-instance health and metrics                  |

**Prometheus scrape targets:**

| Target            | Endpoint                           | Port  |
| ----------------- | ---------------------------------- | ----- |
| epay-server-1/2/3 | `/actuator/prometheus`             | 8029  |
| postgres          | via `postgres-exporter`            | 9187  |
| redis             | via `redis-exporter`               | 9121  |
| nginx             | via `nginx-exporter` (stub_status) | 9113  |
| rabbitmq          | built-in Prometheus plugin         | 15692 |

**Promtail** ships two log streams to Loki:

- Docker container logs from `/var/lib/docker/containers` (all services, labelled by container name)
- Application logs from the `epay_logs` shared volume at `/var/log/epay`

### Log Files (Nginx)

| File                   | Contents                                                        |
| ---------------------- | --------------------------------------------------------------- |
| `access.log`           | All requests — JSON combined format                             |
| `security.log`         | All requests with JWT status and suspicious flag                |
| `error.log`            | Nginx errors at `warn` level and above                          |
| `auth.log`             | Auth + receipt endpoint requests                                |
| `kyc.log`              | KYC document upload requests                                    |
| `user.log`             | User, bank, settings, virtual card, support, developer requests |
| `wallet.log`           | Wallet operation requests                                       |
| `transactions.log`     | Bills, deposit, withdrawal, history, beneficiary requests       |
| `admin.log`            | All admin panel requests                                        |
| `webhooks.log`         | Inbound Paystack and Flutterwave webhook calls                  |
| `security_blocked.log` | Every blocked attack attempt                                    |
| `csp-violations.log`   | CSP violation reports from browsers                             |

---

## Nginx Configuration

Config is modular — `nginx.conf` loads global maps and upstream definitions first, then delegates all server-block routing to `06-server-https.conf`, which `include`s the remaining files at the end of the server block.

### Core Settings (`nginx.conf`)

| Setting                       | Value                  | Purpose                                                   |
| ----------------------------- | ---------------------- | --------------------------------------------------------- |
| `worker_processes`            | `auto`                 | One worker per CPU core                                   |
| `worker_rlimit_nofile`        | `65535`                | Max open file descriptors per worker                      |
| `worker_connections`          | `4096`                 | Max simultaneous connections per worker                   |
| `use`                         | `epoll`                | Linux-optimised I/O event model                           |
| `multi_accept`                | `on`                   | Accept all pending connections per event loop tick        |
| `client_max_body_size`        | `50M`                  | Global upload cap (overridden per-location where tighter) |
| `keepalive_timeout`           | `65s`                  | Idle connection keep-alive                                |
| `client_body_timeout`         | `10s`                  | Slow-body attack protection                               |
| `client_header_timeout`       | `10s`                  | Slow-header attack protection                             |
| `proxy_connect_timeout`       | `60s` (global default) | Per-location overrides tighten this to 5s                 |
| `reset_timedout_connection`   | `on`                   | Frees resources from timed-out clients immediately        |
| `server_tokens`               | `off`                  | Hides nginx version from responses                        |
| `sendfile` / `tcp_nopush`     | `on`                   | Zero-copy static file delivery                            |
| `large_client_header_buffers` | `8 × 16k`              | Handles large JWT Authorization headers                   |
| `proxy_buffer_size`           | `32k` / `8 × 32k`      | Buffers upstream response headers and body                |

**Real IP unwrapping:**

```
set_real_ip_from 10.0.0.0/8;
set_real_ip_from 172.16.0.0/12;
set_real_ip_from 192.168.0.0/16;
real_ip_header   X-Forwarded-For;
real_ip_recursive on;
```

Ensures `$remote_addr` reflects the actual client IP even when sitting behind an internal load balancer.

**Circuit breaker:**

```nginx
proxy_next_upstream error timeout invalid_header http_500 http_502 http_503 http_504;
proxy_next_upstream_tries   2;
proxy_next_upstream_timeout 5s;
```

On a 5xx from one backend instance nginx automatically retries on the next healthy replica.

**Proxy caching zones:**
| Zone | Path | Size | Inactive TTL | Used For |
| -------------- | ------------------------------- | ----- | ------------ | ---------------------------- |
| `api_cache` | `/var/cache/nginx/api` | 1 GB | 60 min | API responses (opt-in) |
| `static_cache` | `/var/cache/nginx/static` | 2 GB | 6 hours | Frontend static assets |

Cache key includes `$scheme$request_method$host$request_uri$http_authorization` so authenticated responses are never served to the wrong user.

**Gzip compression:**

- Enabled for JSON, CSS, JS, XML, SVG, fonts
- Minimum length: 1024 bytes
- Compression level: 6 (balanced)
- Disabled for IE6 (`gzip_disable "msie6"`)

**Unique request ID:**
Every request gets `$req_id` injected via `map $request_id $req_id` and forwarded to the backend as `X-Request-ID`. The same ID appears in all log files, making cross-layer trace correlation possible without Zipkin.

---

### Configuration File Reference

Files in `nginx/conf.d/` are included in this order:

| File                              | Included in         | Purpose                                                                                     |
| --------------------------------- | ------------------- | ------------------------------------------------------------------------------------------- |
| `01-security-headers.conf`        | `nginx.conf` (http) | All response security headers (HSTS, CSP, Permissions-Policy…)                              |
| `02-rate-limiting.conf`           | `nginx.conf` (http) | Rate-limit zone definitions and body-size map                                               |
| `03-bot-detection.conf`           | `nginx.conf` (http) | `$is_malicious`, `$is_suspicious`, `$jwt_status`, `$admin_access`, `$maintenance_mode` maps |
| `04-upstreams.conf`               | `nginx.conf` (http) | `epay_backend`, `epay_frontend`, `epay_websocket`, `epay_admin` upstreams                   |
| `globalblacklist.conf`            | `nginx.conf` (http) | 500+ known-bad UA strings — sets `$bad_bot`                                                 |
| `05-server-http.conf`             | `nginx.conf` (http) | HTTP → HTTPS 301 redirect + ACME `/.well-known/` challenge path                             |
| `06-server-https.conf`            | `nginx.conf` (http) | Main server block — all location routing                                                    |
| `07-error-pages.conf`             | inside server block | Named error locations — JSON responses for 400–504                                          |
| `08-security-blocks.conf`         | inside server block | WAF location-level regex deny rules                                                         |
| `09-administrative-security.conf` | inside server block | Extra hardening on `/admin/*` direct paths                                                  |
| `10-health-checks.conf`           | inside server block | `/nginx-health`, `/backend-health`, `/metrics` endpoints                                    |
| `swagger-locations.conf`          | inside server block | Swagger UI proxy locations (`/docs`, `/swagger-ui/`, `/v3/api-docs`)                        |

`deny.d/deny.conf` contains additional hardening rules for dot-files, PHP/CGI scripts in upload directories, Perl/CGI/shell scripts, `.git` repositories, common hack patterns, and WordPress-specific protections. It also handles Let's Encrypt ACME challenges and image anti-hotlinking.

---

### Upstream Load Balancer (`04-upstreams.conf`)

```nginx
upstream epay_backend {
    least_conn;
    server epay-server-1:8029 max_fails=3 fail_timeout=30s weight=1;
    server epay-server-2:8029 max_fails=3 fail_timeout=30s weight=1;
    server epay-server-3:8029 max_fails=3 fail_timeout=30s weight=1;
    keepalive 32;
    keepalive_requests 1000;
    keepalive_timeout  60s;
}

upstream epay_frontend {
    least_conn;
    server frontend-1:3000 max_fails=3 fail_timeout=30s;
    server frontend-2:3000 max_fails=3 fail_timeout=30s;
    server frontend-3:3000 max_fails=3 fail_timeout=30s;
    keepalive 16;
}

upstream epay_websocket {
    ip_hash;   # sticky sessions — WebSocket connections
    server epay-server-1:8029;
    server epay-server-2:8029;
    server epay-server-3:8029;
    keepalive 32;
}

upstream epay_admin {
    least_conn;
    server epay-server-1:8029 weight=2;   # primary admin node
    server epay-server-2:8029 weight=1;
    server epay-server-3:8029 weight=1;
}
```

- `least_conn` — routes each request to the replica with the fewest active connections
- `ip_hash` — used for WebSocket upstreams so a client's connection always lands on the same backend instance
- `max_fails=3 fail_timeout=30s` — a backend is taken out of rotation after 3 consecutive failures and retried after 30 seconds
- `keepalive 32` — maintains a pool of up to 32 persistent connections to the backend, avoiding TCP handshake overhead on every request

---

### HTTP Redirect Server (`05-server-http.conf`)

- Listens on port 80
- Passes `/.well-known/acme-challenge/` to `/var/www/certbot` for Let's Encrypt certificate renewal
- Redirects everything else to HTTPS with `301`
- SSL certificate directives are present but commented out (uncomment for production with a real domain)

---

### Main HTTPS Server (`06-server-https.conf`)

Currently configured for port 80 to support local development. For production, change to `listen 443 ssl http2` and enable the SSL directives in `05-server-http.conf`.

**Per-location routing table:**

| Nginx Location                     | Backend Path                  | Rate Zone(s)                          | Max Body | Methods                       | Log File             |
| ---------------------------------- | ----------------------------- | ------------------------------------- | -------- | ----------------------------- | -------------------- |
| `/api/v1/auth/`                    | `/auth/…`                     | `auth_limit` (10/min) + `per_user`    | 10 KB    | GET, POST                     | `auth.log`           |
| `/api/v1/user/`                    | `/user/…`                     | `user_api_limit` (5/s)                | 10 MB    | GET, POST, PUT, DELETE, PATCH | `user.log`           |
| `/api/v1/bank`                     | `/bank/…`                     | `user_api_limit`                      | 10 KB    | GET, POST, DELETE             | `user.log`           |
| `/api/v1/settings`                 | `/settings/…`                 | `user_api_limit`                      | 10 MB    | GET, POST, DELETE             | `user.log`           |
| `/api/v1/kyc/`                     | `/kyc/…`                      | `api_limit`                           | 15 MB    | GET, POST                     | `kyc.log`            |
| `/api/v1/wallet/`                  | `/wallet/…`                   | `api_limit` + `per_user`              | 10 KB    | GET, POST, PUT, DELETE, PATCH | `wallet.log`         |
| `/api/v1/bills/`                   | `/bills/…`                    | `strict_limit` (5/s) + `per_user`     | 10 KB    | POST only                     | `transactions.log`   |
| `/api/v1/deposit/`                 | `/deposit/…`                  | `strict_limit` + `per_user`           | 10 KB    | GET, POST                     | `transactions.log`   |
| `/api/v1/withdrawals/`             | `/withdrawals/…`              | `strict_limit` + `per_user` (burst=3) | 10 KB    | GET, POST                     | `transactions.log`   |
| `/api/v1/history/`                 | `/history/…`                  | `api_limit` + `per_user`              | —        | GET only                      | `transactions.log`   |
| `/api/v1/beneficiaries/`           | `/beneficiaries/…`            | `api_limit`                           | 10 KB    | GET, POST, PUT, DELETE, PATCH | `transactions.log`   |
| `/api/v1/receipt/`                 | `/receipt/…`                  | `api_limit`                           | 1 MB     | POST only                     | `auth.log`           |
| `/api/v1/virtual-cards`            | `/virtual-cards/…`            | `user_api_limit`                      | 10 KB    | GET, POST, PUT, DELETE, PATCH | `user.log`           |
| `/api/v1/support/`                 | `/support/…`                  | `api_limit`                           | 20 KB    | GET, POST                     | `user.log`           |
| `/api/v1/investments`              | `/investments/…`              | `user_api_limit`                      | 10 KB    | GET, POST, DELETE             | `user.log`           |
| `/api/v1/tickets/`                 | `/tickets/…`                  | `user_api_limit`                      | 50 KB    | GET, POST                     | `user.log`           |
| `/api/v1/developer/`               | `/developer/…`                | `api_limit`                           | 50 KB    | GET, POST, PUT, DELETE        | `user.log`           |
| `/api/v1/admin/users`              | `/admin/users/…`              | `api_limit`                           | 1 MB     | GET, POST, PUT, DELETE, PATCH | `admin.log`          |
| `/api/v1/admin/wallets`            | `/admin/wallets/…`            | `api_limit`                           | 10 KB    | GET, POST, DELETE, PATCH      | `admin.log`          |
| `/api/v1/admin/transactions`       | `/admin/transactions/…`       | `api_limit`                           | 10 KB    | GET, PATCH                    | `admin.log`          |
| `/api/v1/admin/tickets`            | `/admin/tickets/…`            | `api_limit`                           | 50 KB    | GET, POST, PUT, DELETE, PATCH | `admin.log`          |
| `/api/v1/admin/super`              | `/admin/super/…`              | `strict_limit` (burst=5)              | 50 KB    | GET, POST, PUT, DELETE, PATCH | `admin.log`          |
| `/api/v1/admin/developer`          | `/admin/developer/…`          | `api_limit`                           | 50 KB    | GET, POST, DELETE             | `admin.log`          |
| `/api/v1/admin/liquidity`          | `/admin/liquidity/…`          | `api_limit`                           | 10 KB    | GET, POST, PUT                | `admin.log`          |
| `/api/v1/admin/currencies`         | `/admin/currencies/…`         | `api_limit`                           | 10 KB    | GET, POST, PUT, PATCH         | `admin.log`          |
| `/api/v1/admin/blacklist`          | `/admin/blacklist/…`          | `strict_limit`                        | 10 KB    | GET, POST, DELETE             | `admin.log`          |
| `/api/v1/admin/virtual-cards/fees` | `/admin/virtual-cards/fees/…` | `api_limit`                           | 10 KB    | GET, PUT, PATCH               | `admin.log`          |
| `/api/v1/admin/virtual-cards`      | `/admin/virtual-cards/…`      | `api_limit`                           | 10 KB    | GET, POST, PUT, DELETE, PATCH | `admin.log`          |
| `/api/v1/admin/maintenance`        | `/admin/maintenance/…`        | `api_limit`                           | 10 KB    | GET, POST, PUT, DELETE, PATCH | `admin.log`          |
| `/api/v1/uploads/images/`          | `/uploads/images/…`           | —                                     | —        | GET                           | off (cached 1 day)   |
| `/api/v1/ws/`                      | `/ws/…`                       | —                                     | —        | WebSocket upgrade             | —                    |
| `/webhook/`                        | `/webhook/…`                  | —                                     | 50 KB    | POST only                     | `webhooks.log`       |
| `/docs`, `/docs/`                  | Swagger UI                    | —                                     | —        | GET                           | off                  |
| `/swagger-ui/`                     | Swagger static assets         | —                                     | —        | GET                           | off (cached 1h)      |
| `/v3/api-docs`                     | OpenAPI JSON spec             | —                                     | —        | GET                           | off                  |
| `/actuator/health`                 | Spring Boot actuator          | —                                     | —        | GET (internal only)           | off                  |
| `/nginx-health`                    | Nginx only                    | —                                     | —        | GET                           | off                  |
| `/backend-health`                  | Actuator (internal)           | —                                     | —        | GET (internal only)           | off                  |
| `/metrics`                         | Actuator Prometheus           | —                                     | —        | GET (internal only)           | off                  |
| `/nginx_status`                    | `stub_status`                 | —                                     | —        | GET (internal only)           | off                  |
| `/api/csp-report`                  | Internal (204)                | `api_limit` (burst=30)                | —        | POST only                     | `csp-violations.log` |
| `/_next/static/`                   | Next.js static assets         | —                                     | —        | GET                           | off (cached 1 year)  |
| `/_next/`                          | Next.js assets                | —                                     | —        | GET                           | off (cached 1h)      |
| `/`                                | Next.js frontend              | —                                     | —        | all                           | —                    |

**Path rewriting:** Every `/api/v1/` prefix is stripped before forwarding to the backend via `rewrite ^/api/v1/(.*)$ /$1 break;`. Spring Boot controllers receive clean paths (e.g. `/wallet/transfer`, not `/api/v1/wallet/transfer`).

**CORS:** Each location block handles its own `OPTIONS` preflight — returns 204 with the appropriate `Access-Control-Allow-Methods` for that endpoint (e.g. `/bills/` only allows `POST`, `/history/` only allows `GET`). Credentials are always `true`, and `Access-Control-Max-Age: 86400` caches preflight responses for 24 hours.

**WebSocket support:** `/api/v1/ws/` upgrades via `proxy_set_header Upgrade $http_upgrade` and `Connection "upgrade"`, with 3600s read/send timeouts to keep long-lived connections alive. The frontend catch-all also carries WebSocket upgrade headers for Next.js HMR in development.

**Static asset caching:**

- `/_next/static/` → `Cache-Control: public, max-age=31536000, immutable` (1 year, content-hashed)
- `/_next/` → `Cache-Control: public, max-age=3600` (1 hour)
- `/api/v1/uploads/images/` → cached `200` responses for 1 day

---

### Rate Limiting Zones (`02-rate-limiting.conf`)

| Zone             | Key                        | Rate        | Used On                                          |
| ---------------- | -------------------------- | ----------- | ------------------------------------------------ |
| `auth_limit`     | IP                         | 10 req/min  | `/api/v1/auth/`                                  |
| `api_limit`      | IP                         | 20 req/s    | Most API locations                               |
| `strict_limit`   | IP                         | 5 req/s     | Bills, deposit, withdraw, super admin, blacklist |
| `global_limit`   | IP                         | 100 req/s   | Catch-all global cap                             |
| `bot_limit`      | IP                         | 2 req/s     | Detected bot traffic                             |
| `txn_limit`      | IP + URI                   | 5 req/min   | Transaction-level per-endpoint limiting          |
| `ddos_protect`   | IP                         | 50 req/s    | DDoS mitigation layer                            |
| `uri_limit`      | URI                        | 10 req/s    | Per-URI hotspot protection                       |
| `user_api_limit` | IP                         | 5 req/s     | User-facing API endpoints                        |
| `per_ip_user`    | IP                         | 3 req/s     | Tighter per-IP cap for authenticated users       |
| `per_user`       | JWT user ID (from payload) | 100 req/min | User-level cap extracted from Bearer token       |
| `conn_limit`     | IP                         | —           | Connection count limiting (429 on overflow)      |
| `serv_limit`     | server_name                | —           | Server-wide connection cap                       |

The `per_user` zone extracts the middle segment (payload) of the JWT from the `Authorization: Bearer` header using a regex map, so rate limiting tracks the actual authenticated user regardless of IP (shared NAT, VPN, etc.).

**Per-endpoint body size limits** are also controlled via `02-rate-limiting.conf`:
| Path prefix | Max body |
| --------------- | -------- |
| `/api/deposit/` | 5 MB |
| `/api/upload/` | 10 MB |
| `/api/auth/` | 5 MB |
| All others | 1 MB |

---

### Security Headers (`01-security-headers.conf`)

| Header                                | Value                                                                                         |
| ------------------------------------- | --------------------------------------------------------------------------------------------- |
| `X-Frame-Options`                     | `DENY`                                                                                        |
| `X-Content-Type-Options`              | `nosniff`                                                                                     |
| `X-XSS-Protection`                    | `1; mode=block`                                                                               |
| `Referrer-Policy`                     | `no-referrer`                                                                                 |
| `Strict-Transport-Security`           | `max-age=63072000; includeSubDomains; preload` (2-year HSTS)                                  |
| `Content-Security-Policy`             | `default-src 'none'` with explicit allowlists for scripts, styles, images, fonts, connections |
| `Content-Security-Policy-Report-Only` | Same policy with `report-uri /api/csp-report` — violations logged to `csp-violations.log`     |
| `Permissions-Policy`                  | 21 features disabled: camera, microphone, geolocation, payment, USB, gyroscope, etc.          |
| `Cross-Origin-Embedder-Policy`        | `require-corp`                                                                                |
| `Cross-Origin-Opener-Policy`          | `same-origin-allow-popups`                                                                    |
| `Cross-Origin-Resource-Policy`        | `same-origin`                                                                                 |
| `X-Permitted-Cross-Domain-Policies`   | `none`                                                                                        |
| `Cache-Control`                       | `no-store, no-cache, must-revalidate, proxy-revalidate, max-age=0`                            |
| `X-Banking-API-Version`               | `2.0`                                                                                         |
| `X-Powered-By`                        | Hidden (`proxy_hide_header`)                                                                  |
| `X-Runtime`                           | Hidden (`proxy_hide_header`)                                                                  |

---

### Bot Detection Maps (`03-bot-detection.conf`)

| Map Variable            | Source                | Purpose                                                                                        |
| ----------------------- | --------------------- | ---------------------------------------------------------------------------------------------- |
| `$is_malicious`         | `$request_uri`        | Regex matches for SQLi, XSS, path traversal, command injection, PHP/JSP extensions, null bytes |
| `$is_suspicious`        | `$http_user_agent`    | Matches automation tools, scanners, headless browsers, empty UA                                |
| `$jwt_status`           | `$http_authorization` | `"valid"` if the Bearer token is structurally a JWT, `"invalid"` otherwise                     |
| `$admin_access`         | `$remote_addr` (geo)  | `1` for RFC-1918 addresses (Docker internal); `0` for public IPs                               |
| `$maintenance_mode`     | `$remote_addr` (geo)  | `0` for internal networks (bypass), `0` default (maintenance off)                              |
| `$not_browser`          | `$http_accept`        | `1` if the request does not accept `text/html` — flags non-browser clients                     |
| `$require_referer`      | `$request_uri`        | `1` for deposit, withdrawal, and wallet paths — Referer check trigger                          |
| `$invalid_content_type` | `$content_type`       | `0` for JSON, form-urlencoded, multipart; `1` for everything else                              |
| `$api_version`          | `$request_uri`        | Extracts `1.0` or `2.0` from the URI prefix                                                    |

`$is_malicious` pattern categories in `03-bot-detection.conf`:

- **SQL injection** — `union select`, `insert into`, `drop table`, comment sequences (`--`, `/**/`), `OR 1=1` style bypasses
- **XSS** — `<script>`, `<iframe>`, `javascript:`, event handlers (`onerror=`, `onload=`), `eval(`, `document.`, `alert(`
- **Path traversal** — `../`, URL-encoded variants (`%2e%2e%2f`, `%2e%2e%5c`), double-encoded (`%252e`), `/etc/passwd`, `/proc/self`
- **Sensitive file access** — `.git/`, `.env`, `.htaccess`, `wp-config`, `config.php`
- **Command injection** — `wget`, `curl http`, `/bin/bash`, `shell_exec`, `base64_decode`
- **File upload bypass** — `.php`, `.phtml`, `.jsp`, `.asp`, `.cgi` extensions in URIs
- **Null bytes** — `%00`, `\x00`

`globalblacklist.conf` defines `$bad_bot` via a map against `$http_user_agent`. It covers 500+ strings including: `sqlmap`, `nikto`, `nessus`, `masscan`, `Shodan`, `nuclei`, `Acunetix`, `GPTBot`, `ClaudeBot`, `AhrefsBot`, `SemrushBot`, `Python-*`, `Scrapy`, `curl`, `wget` in production UA strings. Good bots (`Googlebot`, `Bingbot`, `Slackbot`) are explicitly whitelisted.

---

### WAF Security Blocks (`08-security-blocks.conf`)

Location-level deny rules that run after all API location blocks. Block patterns return either `403` with a JSON error body or `404` (for scanner/backup paths):

| Pattern                                                                              | Response | Error Code               |
| ------------------------------------------------------------------------------------ | -------- | ------------------------ |
| Any dot-file (`/.*`)                                                                 | 404      | (silent)                 |
| `.env`, `.git`, `.svn`, `Dockerfile`, `package.json`, `yarn.lock`                    | 404      | (silent)                 |
| `.bak`, `.tmp`, `.swp`, `.log` extensions                                            | 404      | (silent)                 |
| Scanner paths: `wp-admin`, `phpmyadmin`, `adminer`, `jenkins`, `solr`, `jmx-console` | 404      | (silent)                 |
| SQL injection in URI                                                                 | 403      | `SQLI_BLOCKED`           |
| XSS patterns in URI                                                                  | 403      | `XSS_BLOCKED`            |
| Path traversal (`../`, `etc/passwd`)                                                 | 403      | `PATH_TRAVERSAL_BLOCKED` |
| Command injection (`wget`, `curl http`, `/bin/bash`, `cmd.exe`)                      | 403      | `CMD_INJECTION_BLOCKED`  |

All blocks log to `/var/log/nginx/security_blocked.log` using the `security_log` JSON format.

---

### Administrative Security (`09-administrative-security.conf`)

Provides additional per-location WAF checks and CORS handling for direct `/admin/*` paths (used alongside the `/api/v1/admin/*` locations in `06-server-https.conf`). Each admin location enforces:

- `$is_malicious` check → 403
- Path traversal and SQLi/XSS inline checks → 403
- `limit_req zone=api_limit burst=20` + `per_user burst=10`
- Method whitelist: `GET, POST, PUT, DELETE, PATCH, OPTIONS`
- Full CORS headers (origin reflection) for the admin frontend
- `client_max_body_size 1M`
- Logged to `admin.log`

Also serves admin SPA static assets (`/css/`, `/js/`, `/fonts/`, `/images/`) through the backend with `static_cache` and 7-day `Cache-Control: public, immutable`.

---

### Health & Metrics Endpoints (`10-health-checks.conf`)

| Endpoint          | Access        | Returns                                                                       |
| ----------------- | ------------- | ----------------------------------------------------------------------------- |
| `/nginx-health`   | Public        | `{"status":"healthy","service":"nginx"}` — used by docker-compose healthcheck |
| `/backend-health` | Internal only | Proxies to `Spring Boot /actuator/health` — 503 JSON on backend failure       |
| `/metrics`        | Internal only | Proxies to `Spring Boot /actuator/prometheus` — Prometheus scrapes this       |
| `/nginx_status`   | Internal only | Nginx `stub_status` — scraped by `nginx-exporter` on port 9113                |

"Internal only" means the `allow` directives restrict access to `127.0.0.1`, `10.0.0.0/8`, `172.16.0.0/12`, and `192.168.0.0/16` — the Docker bridge networks.

---

### Error Responses (`07-error-pages.conf`)

All errors return `application/json`. Every response includes `"request_id":"$req_id"` for tracing.

| Status | Error String        | Extra Header                            |
| ------ | ------------------- | --------------------------------------- |
| 400    | Bad Request         | —                                       |
| 401    | Unauthorized        | `WWW-Authenticate: Bearer realm="epay"` |
| 403    | Forbidden           | —                                       |
| 404    | Not Found           | —                                       |
| 405    | Method Not Allowed  | —                                       |
| 408    | Request Timeout     | —                                       |
| 429    | Too Many Requests   | `Retry-After: 60`                       |
| 503    | Service Unavailable | `Retry-After: 30`                       |

The frontend has its own error handler (`@frontend_unavailable`) that returns a 503 JSON payload with `"code":"FRONTEND_DOWN"` when all Next.js replicas are down.

Custom HTML error pages also exist in `nginx/html/` for browser-facing errors: `400.html`, `401.html`, `403.html`, `404.html`, `405.html`, `429.html`, `50x.html`, and `maintenance.html`.

---

### Logging (`nginx.conf`)

Four named log formats are defined:

| Format          | File(s)                      | Fields                                                                                          |
| --------------- | ---------------------------- | ----------------------------------------------------------------------------------------------- |
| `json_combined` | `access.log`                 | timestamp, IP, method, URI, status, bytes, request time, upstream time, UA, req_id              |
| `security_log`  | `security.log` + per-service | JSON with JWT status (`$jwt_status`), suspicious flag (`$is_suspicious`), full request metadata |
| `auth_failures` | —                            | Plain-text auth failure log                                                                     |
| `sensitive_log` | —                            | Plain-text log for sensitive operations                                                         |

Per-service log files (all using `security_log` JSON format):

| File                   | What it captures                                                |
| ---------------------- | --------------------------------------------------------------- |
| `access.log`           | All requests (json_combined)                                    |
| `security.log`         | All requests with JWT status and suspicious flag                |
| `error.log`            | Nginx errors at `warn` level and above                          |
| `auth.log`             | Auth endpoint + receipt requests                                |
| `kyc.log`              | KYC document upload requests                                    |
| `user.log`             | User, bank, settings, virtual card, support, developer requests |
| `wallet.log`           | Wallet operation requests                                       |
| `transactions.log`     | Bills, deposit, withdrawal, history, beneficiary requests       |
| `admin.log`            | All admin panel requests                                        |
| `webhooks.log`         | Inbound Paystack and Flutterwave webhook calls                  |
| `security_blocked.log` | Every blocked attack attempt                                    |
| `csp-violations.log`   | CSP violation reports from browsers                             |

A `map $status $loggable` block is defined to optionally suppress 2xx/3xx from certain log files, making it easy to filter noise in production without changing access_log directives.

---

## Environment Variables

Create a `.env` file in `epay-app/` (next to `docker-compose.yml`):

```env
# Database
DB_USERNAME=epay
DB_PASSWORD=your_secure_password

# JWT — generate a strong 256-bit Base64-encoded secret
JWT_SECRET=your_base64_encoded_secret_here

# Payment Gateways
PAYSTACK_SECRET_KEY=sk_live_xxxxxxxxxxxx
FLUTTERWAVE_SECRET_KEY=FLWSECK_live-xxxxxxxxxxxx

# Email (Gmail App Password)
GMAIL_USERNAME=your@gmail.com
GMAIL_APP_PASSWORD=your_gmail_app_password

# RabbitMQ
SPRING_RABBITMQ_USERNAME=guest
SPRING_RABBITMQ_PASSWORD=guest

# Grafana
GF_ADMIN_USER=admin
GF_ADMIN_PASSWORD=your_grafana_password

# Frontend API URL (internal Docker network)
NEXT_PUBLIC_API_URL=http://epay-server:8029
```

---

## Getting Started

### Prerequisites

- Docker Desktop (or Docker Engine + Compose v2)
- Java 21 + Maven (for building the backend JAR)
- Node.js 20+ (optional — only needed for local frontend dev)

### 1. Clone the repository

```bash
git clone <repository-url>
cd epay-app
```

### 2. Build the backend JAR

```bash
cd epay
mvn clean package -DskipTests
cd ..
```

### 3. Configure environment

```bash
cp epay/.env .env
# Edit .env with your credentials
```

### 4. Start the stack

```bash
docker compose up --build
```

### 5. Access the services

| Service             | URL                      |
| ------------------- | ------------------------ |
| Frontend            | http://localhost         |
| API (via Nginx)     | http://localhost/api/v1/ |
| Swagger UI          | http://localhost/docs    |
| RabbitMQ Management | http://localhost:15672   |
| Prometheus          | http://localhost:9090    |
| Grafana             | http://localhost:3010    |
| Zipkin              | http://localhost:9411    |
| Loki                | http://localhost:3100    |
| postgres-exporter   | http://localhost:9187    |
| redis-exporter      | http://localhost:9121    |
| nginx-exporter      | http://localhost:9113    |

### Local Development (without Docker)

**Backend:**

```bash
cd epay
# Start postgres, redis, rabbitmq locally (or via docker compose up postgres redis rabbitmq)
mvn spring-boot:run -pl main
```

**Frontend:**

```bash
cd frontend
npm install
npm run dev    # runs on http://localhost:3000
```

---

## Project Structure

```
epay-app/
├── docker-compose.yml
├── .env                            ← environment variables (gitignored)
│
├── nginx/
│   ├── nginx.conf                  ← main nginx config (core settings, gzip, caching, logging)
│   ├── conf.d/
│   │   ├── 01-security-headers.conf        ← HSTS, CSP, Permissions-Policy, CORS
│   │   ├── 02-rate-limiting.conf           ← 11 rate-limit zones + body-size map
│   │   ├── 03-bot-detection.conf           ← $is_malicious, $jwt_status, $admin_access maps
│   │   ├── 04-upstreams.conf               ← backend/frontend/websocket/admin upstreams
│   │   ├── 05-server-http.conf             ← HTTP → HTTPS redirect + ACME challenge
│   │   ├── 06-server-https.conf            ← main server block, all /api/v1/ locations
│   │   ├── 07-error-pages.conf             ← JSON error responses (400–503)
│   │   ├── 08-security-blocks.conf         ← WAF: SQLi, XSS, path traversal, cmd injection
│   │   ├── 09-administrative-security.conf ← extra hardening for /admin/* direct paths
│   │   ├── 10-health-checks.conf           ← /nginx-health, /backend-health, /metrics
│   │   ├── globalblacklist.conf            ← 500+ bad-bot User-Agent strings
│   │   └── swagger-locations.conf          ← /docs, /swagger-ui/, /v3/api-docs
│   ├── deny.d/
│   │   └── deny.conf               ← dot-files, PHP/CGI in uploads, .git, ACME, anti-hotlinking
│   ├── ssl/                        ← TLS certificates (gitignored, for production)
│   └── html/                       ← custom HTML error pages (400, 401, 403, 404, 405, 429, 50x, maintenance)
│
├── epay/                           ← Spring Boot backend (Java 21)
│   ├── Dockerfile
│   ├── pom.xml                     ← parent POM, 18 modules
│   ├── domain/                     ← entities, DTOs, enums, repos
│   ├── common/                     ← security, JWT, filters, config
│   ├── auth/                       ← authentication & KYC
│   ├── wallet/                     ← wallet & transfers
│   ├── deposit/                    ← funding
│   ├── withdraw/                   ← withdrawals
│   ├── bills/                      ← utility payments
│   ├── virtual_card/               ← virtual cards
│   ├── investment/                 ← investment plans
│   ├── beneficiary/                ← saved recipients
│   ├── bank/                       ← bank account management
│   ├── history/                    ← transaction history
│   ├── admin/                      ← admin back-office
│   ├── notification/               ← async notifications
│   ├── blacklist/                  ← blacklist service
│   ├── maintenance/                ← maintenance fee engine + scheduler + debt recovery
│   ├── escrow/                     ← escrow (in progress)
│   ├── savings/                    ← savings plans (in progress)
│   ├── main/                       ← app entry point
│   └── monitoring/
│       ├── prometheus/             ← prometheus.yml + alert.rules.yml
│       ├── grafana/                ← provisioning/dashboards + datasources
│       ├── loki/                   ← log aggregation backend
│       └── promtail/               ← ships Docker + app logs to Loki
│
└── frontend/                       ← Next.js 16 (App Router)
    ├── Dockerfile
    ├── app/
    │   ├── (public)/               ← landing, about, contact
    │   ├── auth/                   ← login, register, reset, verify
    │   ├── dashboard/
    │   ├── wallet/                 ← accounts, beneficiary, deposit, statements
    │   ├── bills/
    │   ├── cards/
    │   ├── exchange/
    │   ├── investments/
    │   ├── settings/
    │   ├── support/
    │   ├── kyc/
    │   ├── banks/
    │   └── payment/
    ├── components/
    └── middleware.ts               ← auth guard + redirect logic
```

```docker
docker compose up --build -d
docker image prune -f
docker compose down -v --remove-orphans
docker system prune -a --volumes -f
docker volume prune -a -f
docker container prune -f
docker compose up -d --no-build --force-recreate epay-server 2>&1
docker compose config --quiet 2>&1
docker compose up -d --build prometheus grafana 2>&1
docker run --rm alpine sh -c "ls /run/desktop/mnt/host/" 2>&1


# Start with 3 instances
docker compose up -d --scale epay-server=3

# Scale to 5 instances
docker compose up -d --scale epay-server=5

# Scale down to 2 instances
docker compose up -d --scale epay-server=2

# Check status of all instances
docker compose ps

# View logs from specific instance
docker compose logs epay-server-1 -f

# View logs from all backend instances
docker compose logs epay-server-1 epay-server-2 epay-server-3 -f

# Test load balancing
for i in {1..20}; do
    curl -s http://localhost/api/v1/health | jq '.instance'
done

# Reload NGINX after scaling
docker compose exec nginx nginx -s reload

# Gracefully stop specific instance
docker compose stop epay-server-2
docker compose start epay-server-2
```

```java build
mvn clean:clean install
mvn clean:clean install
```

---

## Roadmap

| Feature                                                  | Status      |
| -------------------------------------------------------- | ----------- |
| User registration, login, 2FA                            | Done        |
| KYC with document upload and review                      | Done        |
| Multi-currency wallet                                    | Done        |
| Peer-to-peer transfers                                   | Done        |
| Currency swap                                            | Done        |
| Paystack + Flutterwave deposits                          | Done        |
| Bank withdrawals                                         | Done        |
| Airtime, data, cable TV, electricity, betting, shopping  | Done        |
| Virtual cards with spending controls                     | Done        |
| Investment plans (4 tiers)                               | Done        |
| Beneficiary management                                   | Done        |
| Transaction history with filtering                       | Done        |
| Bank statement PDF (emailed)                             | Done        |
| Admin user/wallet/transaction management                 | Done        |
| Admin support ticket workflow                            | Done        |
| Liquidity management (Paystack + Flutterwave floats)     | Done        |
| Developer portal with API keys and webhooks              | Done        |
| Async notifications via RabbitMQ                         | Done        |
| Observability (Prometheus + Grafana + Zipkin)            | Done        |
| Savings plans                                            | In Progress |
| Escrow                                                   | In Progress |
| Referral program                                         | In Progress |
| **Maintenance fee service**                              | **Done**    |
| **Load-balanced multi-instance deployment (3 replicas)** | **Done**    |
| **Nginx load balancer + exporters**                      | **Done**    |
| **@Operation Swagger docs on all endpoints**             | **Done**    |

---

## Changelog

### September 9, 2026

---

#### Maintenance Fee Service (New Module — `epay-maintenance`)

A fully automated, bank-style monthly maintenance fee system was designed and implemented from scratch.

**How it works:**

- Every time a user touches a currency (transfer, deposit, swap, bill payment) the system silently records that currency as "active" for the current calendar month.
- On the 1st of every month at 00:30, the scheduler charges a fee only for the currencies each user actually used — if you only used USD that month, only USD is charged. Unused currencies are never charged.
- If the wallet has enough balance, the fee is deducted immediately and the user is notified.
- If the wallet is empty or has less than the fee, the shortfall is recorded as a **debt** against that wallet. The next time the user deposits into that currency, the debt is automatically recovered first before the remaining balance is credited.

**New domain entities (`epay-domain`):**

| Entity                      | Table                          | Purpose                                           |
| --------------------------- | ------------------------------ | ------------------------------------------------- |
| `MaintenanceFeeConfig`      | `maintenance_fee_config`       | Per-currency fee rules (FIXED or PERCENTAGE)      |
| `UserMonthlyActivity`       | `user_monthly_activity`        | Tracks which currencies each user used each month |
| `MaintenanceFeeTransaction` | `maintenance_fee_transactions` | One charge record per user/currency/month         |
| `UserDebt`                  | `user_debts`                   | Running debt ledger per user/currency             |
| `MaintenanceFeeAuditLog`    | `maintenance_fee_audit_log`    | Immutable audit trail for every action            |

**New enums:** `FeeType` (FIXED / PERCENTAGE), `MaintenanceFeeStatus` (PENDING / DEDUCTED / PARTIAL / DEBT / REPAID / WAIVED / FAILED), `DebtStatus` (ACTIVE / PARTIAL / SETTLED), `MaintenanceAuditAction`.

**Core services (`epay-maintenance`):**

- **`MaintenanceFeeEngine`** — calculates and applies one fee charge: deducts from wallet, records history, updates the debt ledger, writes an audit entry, and sends a user notification. Every user is processed in its own `REQUIRES_NEW` transaction so a single failure never rolls back the rest of the batch.
- **`MaintenanceFeeScheduler`** — chunked cron job (default 00:30 on 1st of every month, configurable via `epay.maintenance.cron`). Processes users in pages of 500 (configurable via `epay.maintenance.batch-size`). **Distributed Redis lock** (`SETNX` with 2-hour TTL scoped per billing month) prevents double-processing across the 3 backend instances.
- **`DebtRecoveryService`** — triggered synchronously inside the wallet credit transaction whenever a deposit or incoming transfer arrives. Deducts outstanding debt FIFO (oldest charge first), notifies the user, and returns the net amount to credit.
- **`MaintenanceFeeConfigService`** — admin CRUD for fee configurations. Supports FIXED (flat fee) and PERCENTAGE (% of volume, with optional min/max clamps).
- **`MaintenanceAdminService`** — rich read views for the admin dashboard: debt aging report (0-30/31-60/61-90/90+ day brackets), monthly status breakdowns, per-user full maintenance profile.
- **`MaintenanceUsageAdapter`** — implements `IMaintenanceUsagePort`. Records activity asynchronously using a JPQL bulk-increment upsert with a concurrent-insert retry to avoid race conditions.

**Wallet service integration (`epay-wallet`):**

Two new port interfaces were added to `epay-common` to avoid circular module dependencies:

- `IMaintenanceUsagePort` — called after every transfer, deposit credit, and swap to record currency activity.
- `IDebtRecoveryPort` — called before crediting any incoming deposit if an active debt exists, to recover the debt atomically in the same transaction.

**Notifications — three new email templates:**

| Event                       | Template                                    | Subject                                                          |
| --------------------------- | ------------------------------------------- | ---------------------------------------------------------------- |
| Fee successfully deducted   | `maintenance/maintenance-fee-deducted.html` | `ePay — Maintenance Fee Deducted`                                |
| Debt created (empty wallet) | `maintenance/maintenance-debt-created.html` | `ePay — Maintenance Fee Debt Created (currency)`                 |
| Debt repaid via deposit     | `maintenance/maintenance-debt-repaid.html`  | `ePay — Maintenance Debt Fully Settled / Partial Debt Repayment` |

Two new RabbitMQ queues wired end-to-end:

- `maintenance.wallet` → fee-deducted notification
- `maintenance.debt` → debt-created and debt-repaid notifications (dispatched on `eventType` field)

**Admin REST API (`/admin/maintenance`):**

| Section              | Endpoints                                                                                               | Who               |
| -------------------- | ------------------------------------------------------------------------------------------------------- | ----------------- |
| Dashboard overview   | `GET /overview`                                                                                         | ADMIN, SUPER_USER |
| Fee config CRUD      | `GET/POST/PUT/DELETE /fee-configs/**`                                                                   | ADMIN, SUPER_USER |
| Charge history       | `GET /transactions/**` (filter by status, month, user, reference)                                       | ADMIN+, CS        |
| Waive a fee          | `POST /transactions/waive`                                                                              | ADMIN, SUPER_USER |
| Debt ledger          | `GET /debts`, `/debts/summary`, `/debts/aging`, `/debts/aging/threshold/{days}`, `/debts/user/{userId}` | ADMIN+, CS        |
| User full profile    | `GET /users/{userId}/profile`                                                                           | ADMIN+, CS        |
| Monthly report       | `GET /reports/month/{YYYY-MM-01}`                                                                       | ADMIN+, CS        |
| Activity tracking    | `GET /activity/user/**`                                                                                 | ADMIN+, CS        |
| Audit log            | `GET /audit` (filter by batchId or userId)                                                              | ADMIN, SUPER_USER |
| Manual batch trigger | `POST /batch/trigger`                                                                                   | SUPER_USER only   |

---

#### Load-Balanced Multi-Instance Deployment

The application was upgraded from a single-instance deployment to a **3-replica load-balanced cluster**.

**`docker-compose.yml` changes:**

- `epay-server-base` YAML anchor (`&server-base`) declared once; `epay-server-1`, `epay-server-2`, `epay-server-3` inherit via `<<: *server-base` and each override a unique `INSTANCE_ID` and host port (`8021`, `8022`, `8023`). This ensures distributed tracing labels are unique per replica.
- Frontend replicated to 3 instances (`frontend-1/2/3` on ports `3001/3002/3003`).
- Grafana port moved from `3001` to `3010` to eliminate the conflict with `frontend-1`.
- Three new monitoring exporters added:
  - `redis-exporter` (`oliver006/redis_exporter:v1.62.0`) on port `9121`
  - `nginx-exporter` (`nginx/nginx-prometheus-exporter:1.1.0`) on port `9113`
  - `postgres-exporter` was already present on port `9187`
- Prometheus volume paths corrected from `./prometheus/...` to `./epay/monitoring/prometheus/...`.
- Grafana provisioning paths corrected from `./grafana/...` to `./epay/monitoring/grafana/provisioning/...`.

**`nginx/conf.d/04-upstreams.conf` — load balancer config:**

```nginx
upstream epay_backend {
    least_conn;
    server epay-server-1:8029 max_fails=3 fail_timeout=30s weight=1;
    server epay-server-2:8029 max_fails=3 fail_timeout=30s weight=1;
    server epay-server-3:8029 max_fails=3 fail_timeout=30s weight=1;
    keepalive 32;
}

upstream epay_frontend { /* same pattern */ }
upstream epay_websocket { ip_hash; /* sticky sessions for WebSocket */ }
upstream epay_admin { least_conn; /* weight=2 on server-1 */ }
```

**`nginx/conf.d/10-health-checks.conf` (new file):**

- `/nginx-health` — returns 200 immediately (used by docker-compose healthcheck)
- `/backend-health` — proxies to `/actuator/health`; restricted to internal subnets only
- `/metrics` — proxies to `/actuator/prometheus`; restricted to internal subnets only

**`nginx/conf.d/06-server-https.conf` — fixes:**

- Renamed `/health` proxy path to `/backend-health` to avoid conflict with the existing static `/health` exact-match.

**`SecurityHeadersFilter` update:**

- Added `X-Instance-ID` response header (populated from `${INSTANCE_ID}`) so clients can see which replica served a request without modifying response bodies.

**`CorsAutoConfiguration` update:**

- `X-Instance-ID` added to `exposedHeaders` so browsers can read it.

---

#### Prometheus Monitoring Fixes

All broken scrape targets in `prometheus.yml` were corrected:

| Target   | Before (broken)                          | After (correct)                               |
| -------- | ---------------------------------------- | --------------------------------------------- |
| Redis    | `redis:6379` (Redis protocol)            | `redis-exporter:9121` (HTTP metrics)          |
| RabbitMQ | `rabbitmq:15672/metrics` (management UI) | `rabbitmq:15692` (built-in prometheus plugin) |
| Nginx    | `nginx:80/nginx_status` (plain text)     | `nginx-exporter:9113` (Prometheus format)     |
| Zipkin   | `zipkin:9411/metrics` (no endpoint)      | Removed                                       |
| cAdvisor | Listed but missing service               | Kept with docker-compose snippet in comment   |

`stub_status` was already correctly configured in `06-server-https.conf` for the nginx exporter to scrape.

---

#### `@Operation` Swagger Documentation

Every REST endpoint across all **30 controllers** (~185 methods) was annotated with:

- `@Tag` at class level (groups endpoints in Swagger UI)
- `@Operation(summary = "...", description = "...")` on every method

Controllers annotated: `AuthController`, `UserController`, `SettingController`, `KycController`, `StatementController`, `WalletController`, `AdminCurrencyController`, `DepositController`, `DepositWebhookController`, `WithdrawController`, `WithdrawWebhookController`, `BeneficiaryController`, `UserBankController`, `BlacklistAdminController`, `HistoryController`, `InvestmentController`, `AdminTransactionController`, `AdminWalletController`, `AdminUserController`, `AdminLiquidityController`, `AdminTicketController`, `UserTicketController`, `SupportController`, `SuperAdminController`, `DeveloperPortalController`, `AdminDeveloperController`, `AdminCardFeeController`, `VirtualCardController`, `AdminMaintenanceFeeController`.

---

#### `@SequenceGenerator` Entity Migration

All 19 entities that previously used `GenerationType.IDENTITY` were migrated to `SEQUENCE` + `@SequenceGenerator` for consistency with PostgreSQL best practices and the rest of the project:

`AuthorizeUserVerification`, `VerificationToken`, `PasswordResetToken`, `TwoFactorAuthentication`, `UserAttempt`, `UserAccountCases`, `UserAccountSettings`, `UserTracer`, `NextOfKin`, `KycDocument`, `KycVerification`, `UserBankList`, `Beneficiary`, `BlacklistEntry`, `TransactionAuditLog`, `Investment`, `CardFeeConfig`, `SupportedCurrency`, `WalletSettings`.

Sequence names follow the `{table_name}_seq` convention with `allocationSize = 1`.

---

#### `InstanceConfig` and Response Interceptor (Rejected)

A proposed `InstanceConfig` bean and `ResponseBodyAdvice` wrapper were reviewed and rejected:

- `InstanceConfig` was duplicate — `spring.instance.id` already handles this in `application.yaml`.
- The `ResponseBodyAdvice` pattern was broken by design (wraps `ResponseEntity` inside another `ResponseEntity`, causing double-serialization).
- Instance identity is now surfaced via the `X-Instance-ID` response header instead, which is invisible to client code and requires zero API contract changes.

---

#### Distributed Lock for Maintenance Scheduler

The in-memory `volatile boolean running` guard in `MaintenanceFeeScheduler` was replaced with a Redis distributed lock:

- Key: `epay:lock:maintenance-batch:{billingMonth}` (month-scoped to prevent August from blocking September)
- Lock value: the instance ID, so `redis-cli GET` shows which node holds the lock
- TTL: 2 hours (auto-expires even if a node crashes without releasing)
- Release: checks the lock still belongs to this instance before deleting (prevents a slow node from releasing a lock re-acquired by another)
- Fail-open: if Redis is unreachable, the scheduler proceeds rather than silently skipping an entire billing cycle

Copyright (c) 2026 Willstone Strategic Industries Limited

All rights reserved.

PROPRIETARY SOFTWARE LICENSE

This software and all associated source code, documentation, designs,
interfaces, algorithms, and other materials are proprietary and confidential
property of Willstone Strategic Industries Limited.

Permission is not granted to copy, modify, distribute, publish, sublicense,
sell, lease, reverse engineer, decompile, disassemble, or otherwise use,
reproduce, or exploit this software or any portion of it without prior
written authorization from Willstone Strategic Industries Limited.

Access to or possession of this source code does not grant any ownership,
license, or other intellectual property rights.

Unauthorized use, reproduction, distribution, modification, or disclosure
of this software is strictly prohibited.

For licensing, commercial use, partnership, or other authorized access,
contact Willstone Strategic Industries Limited.

Copyright © 2026 Willstone Strategic Industries Limited.
All rights reserved.
