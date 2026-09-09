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

| Service    | Image                  | Purpose             |
| ---------- | ---------------------- | ------------------- |
| Nginx      | 1.27-alpine            | Reverse proxy + WAF |
| PostgreSQL | 16-alpine              | Database            |
| Redis      | 7-alpine               | Cache + sessions    |
| RabbitMQ   | 3.13-management-alpine | Message broker      |
| Zipkin     | latest                 | Distributed tracing |
| Prometheus | v2.51.2                | Metrics collection  |
| Grafana    | 10.4.2                 | Metrics dashboards  |

---

## Architecture Overview

```
Internet
    │
    ▼
┌─────────────────────────────────────┐
│           Nginx 1.27                │
│  • WAF-style attack detection       │
│  • Rate limiting (9 zones)          │
│  • Security headers                 │
│  • Bot blocking (500+ UA strings)   │
│  • CSP, HSTS, Permissions-Policy    │
└─────────────┬───────────────────────┘
              │
    ┌─────────┴──────────┐
    │                    │
    ▼                    ▼
┌─────────────┐    ┌──────────────┐
│  Next.js    │    │ Spring Boot  │
│  Frontend   │    │   Backend    │
│  :3000      │    │   :8029      │
│             │    │              │
│  /          │    │  /api/v1/**  │
│  /_next/**  │    │  /webhook/** │
└─────────────┘    └──────┬───────┘
                          │
          ┌───────────────┼────────────────┐
          │               │                │
          ▼               ▼                ▼
    ┌──────────┐   ┌──────────┐   ┌──────────────┐
    │PostgreSQL│   │  Redis   │   │  RabbitMQ    │
    │ :5432    │   │  :6379   │   │  :5672       │
    └──────────┘   └──────────┘   └──────────────┘
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

**Rate Limiting (9 dedicated zones):**

- `auth_limit` — 10 requests/min per IP on auth endpoints
- `api_limit` — 20 requests/sec per IP on general API
- `strict_limit` — 5 requests/sec per IP on sensitive endpoints
- `txn_limit` — 5 requests/min per IP+URI on transaction endpoints
- `per_user` — 100 requests/min per JWT user ID (extracted from Bearer token)
- `bot_limit`, `ddos_protect`, `global_limit`, `conn_limit` — network-layer DDoS protection

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

The entire stack runs from a single `docker-compose.yml` at the project root.

```
epay-app/
├── docker-compose.yml       ← single compose for everything
├── nginx/                   ← shared reverse proxy config
│   ├── nginx.conf
│   ├── conf.d/
│   ├── deny.d/
│   └── html/                ← custom error pages
├── epay/                    ← Spring Boot backend
│   ├── Dockerfile
│   ├── pom.xml
│   └── [18 Maven modules]
└── frontend/                ← Next.js frontend
    ├── Dockerfile
    └── [App Router pages]
```

### Services & Startup Order

```
postgres ──┐
redis    ──┼──► epay-server ──► frontend ──► nginx
rabbitmq ──┘
```

All `depends_on` use `condition: service_healthy` — services only start after their dependencies pass health checks.

### Health Checks

- **postgres** — `pg_isready` every 10s
- **redis** — `redis-cli ping` every 10s
- **rabbitmq** — `rabbitmq-diagnostics check_port_connectivity` every 10s
- **epay-server** — `wget /actuator/health` every 30s (60s start grace period)
- **frontend** — `wget http://localhost:3000` every 30s
- **nginx** — `wget http://localhost/health` every 30s

### Backend Docker Build

The backend Dockerfile expects a pre-built JAR from `main/target/*.jar`. Build the project with Maven before running Docker Compose:

```bash
cd epay
mvn clean package -DskipTests
```

### Observability

- **Zipkin** (`:9411`) — distributed tracing via Micrometer + Brave
- **Prometheus** (`:9090`) — scrapes `/actuator/prometheus` every 15s, 15-day retention
- **Grafana** (`:3001`) — dashboards connected to Prometheus
- **Spring Actuator** — `/actuator/health`, `/actuator/metrics`, `/actuator/prometheus`
- **Structured JSON logging** — Nginx logs all requests in JSON format to `/var/log/nginx/`

### Log Files (Nginx)

| File                   | Contents                                                        |
| ---------------------- | --------------------------------------------------------------- |
| `access.log`           | All requests (JSON combined format)                             |
| `security.log`         | Full security metadata including JWT status and suspicious flag |
| `auth.log`             | Auth endpoint requests                                          |
| `admin.log`            | Admin panel requests                                            |
| `wallet.log`           | Wallet requests                                                 |
| `transactions.log`     | Transaction requests (deposit, withdraw, bills, history)        |
| `security_blocked.log` | Blocked attack attempts                                         |
| `csp-violations.log`   | CSP violation reports                                           |
| `webhooks.log`         | Inbound webhook calls                                           |

---

## Nginx Configuration

Config is modular — `nginx.conf` includes files from `nginx/conf.d/` in order:

| File                              | Purpose                                                          |
| --------------------------------- | ---------------------------------------------------------------- |
| `01-security-headers.conf`        | All security response headers                                    |
| `02-rate-limiting.conf`           | Rate limit zone definitions                                      |
| `03-bot-detection.conf`           | `$is_malicious`, `$is_suspicious`, `$admin_access` maps          |
| `04-upstreams.conf`               | `epay_backend` (`:8029`) and `epay_frontend` (`:3000`) upstreams |
| `05-server-http.conf`             | HTTP → HTTPS 301 redirect + ACME challenge path                  |
| `06-server-https.conf`            | Main server block — all location routing                         |
| `11-error-pages.conf`             | Custom error page mappings                                       |
| `12-security-blocks.conf`         | WAF location-level regex deny rules                              |
| `13-administrative-security.conf` | Extra hardening on `/admin/*` paths                              |
| `globalblacklist.conf`            | 500+ bad bot UA strings                                          |
| `swagger-locations.conf`          | Swagger UI proxy locations                                       |

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

| Service             | URL                         |
| ------------------- | --------------------------- |
| Frontend            | http://localhost            |
| API (via Nginx)     | http://localhost/api/v1/    |
| Swagger UI          | http://localhost/swagger-ui |
| RabbitMQ Management | http://localhost:15672      |
| Prometheus          | http://localhost:9090       |
| Grafana             | http://localhost:3001       |
| Zipkin              | http://localhost:9411       |

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
│   ├── nginx.conf                  ← main nginx config
│   ├── conf.d/
│   │   ├── 01-security-headers.conf
│   │   ├── 02-rate-limiting.conf
│   │   ├── 03-bot-detection.conf
│   │   ├── 04-upstreams.conf
│   │   ├── 05-server-http.conf
│   │   ├── 06-server-https.conf
│   │   ├── 11-error-pages.conf
│   │   ├── 12-security-blocks.conf
│   │   ├── 13-administrative-security.conf
│   │   ├── globalblacklist.conf
│   │   └── swagger-locations.conf
│   ├── deny.d/
│   └── html/                       ← custom error pages (400, 401, 403, 404, 429, 50x)
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
│   ├── escrow/                     ← escrow (in progress)
│   ├── savings/                    ← savings plans (in progress)
│   └── main/                       ← app entry point
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
