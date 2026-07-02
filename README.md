# epay-server

Maven multi-module skeleton for the ePay / Pesco distributed payments platform.

## Structure

- `domain/`        - Pure Java DTOs, inputs, enums. No Spring beans. Depended on by every module.
- `common/`         - Shared Spring configuration (security, cache, cors, logging, rate-limit, events, util).
- `auth/`           - Authentication & user management.
- `wallet/`         - Wallet, balances, transfers, currency.
- `deposit/`        - Deposits via payment gateways (Paystack, Flutterwave).
- `withdraw/`       - Withdrawals - in-house + bank payouts.
- `virtual_card/`   - Virtual debit card issuance and management.
- `escrow/`         - Held funds pending release or dispute.
- `beneficiary/`    - Saved payees for fast repeat transfers.
- `blacklist/`      - Fraud prevention - blocks accounts, IPs, devices, cards.
- `maintenance/`    - Periodic wallet maintenance fee deduction.
- `savings/`        - Target savings goals.
- `investment/`     - Investment plans & maturity payouts.
- `notification/`   - Email/SMS/push notifications via event listeners.
- `history/`        - Unified transaction history & audit trail.
- `admin/`          - Admin dashboard API, aggregates every module.
- `main/`           - Single Spring Boot entry point (`EpayApplication.java` + `application.yaml`).

Every module is registered in the root `pom.xml` `<modules>` section and wired
into `main/pom.xml` as a dependency. Only `main` carries `application.yaml`;
every other module is config-free and gets its properties injected at runtime
through the main module's classpath.

This is a structural skeleton only - packages are scaffolded (with `.gitkeep`
placeholders) but contain no classes or business logic beyond
`EpayApplication.java`.

## Build

```bash
mvn clean install
```

## Run

```bash
cd main
mvn spring-boot:run
```
