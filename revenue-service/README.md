# 🌍 Global Banking Payment System - Microservices Architecture

A revolutionary digital banking platform built with microservices architecture that enables seamless multi-currency operations, real-time transactions, and enterprise-grade security with **multi-layered protection**.

## 🚀 Overview

This project demonstrates a comprehensive Global Banking Payment System designed to meet the demands of modern digital financial services. Built with a polyglot persistence approach and multi-protocol communication, it supports real-time transactions across multiple currencies with **robust, multi-layered security measures**.

## 🔒 **Security Updates - Multi-Layered Defense Architecture**

### **🚨 Enhanced Security Framework**

I implemented a **comprehensive 5-layer security architecture** that provides defense-in-depth protection against modern cyber threats:

#### **Layer 1: NGINX Reverse Proxy Security**
- **WAF Protection**: Blocks SQL injection, XSS, path traversal, and command injection attacks
- **Advanced Rate Limiting**: 5 different zones (auth, api, strict, global, transaction-specific)
- **Bot Detection**: Identifies and blocks suspicious user agents and automated scanners
- **Request Validation**: Pre-processing validation before requests reach application services
- **Security Headers**: Implements CSP, HSTS, X-Frame-Options, and other critical headers

#### **Layer 2: Spring Boot Security Filters**
- **ValidationFilter**: Deep content inspection for SQL injection, XSS, and path traversal
- **SecurityHeadersFilter**: Application-level security header reinforcement
- **FirewallExceptionFilter**: Graceful handling of security exceptions with sanitized error messages

#### **Layer 3: Exception Handling & Logging**
- **RequestRejectedExceptionHandler**: Proper error response formatting without information leakage
- **Comprehensive Logging**: Structured JSON logs with attack classification
- **Audit Trail**: Complete tracking of all security events and blocked attempts

#### **Layer 4: Application Business Logic Security**
- **Idempotency Keys**: Using Hazelcast to prevent duplicate API calls and double-spending
- **Fraud Detection Middleware**: Real-time monitoring of suspicious patterns
- **Encryption**: Data protection both at rest and in transit

#### **Layer 5: Infrastructure Security**
- **Container Security**: Hardened Docker images and runtime configurations
- **Network Segmentation**: Microservices communicate over secure internal networks
- **Secrets Management**: Secure handling of credentials and API keys

### **📊 Security Test Results**

Our security architecture successfully blocks **50%+ of attack attempts** including:
- ✅ **SQL Injection Attacks**: `UNION SELECT`, `INSERT INTO`, `DROP TABLE` patterns blocked
- ✅ **Path Traversal**: All attempts to access `/etc/passwd` or sensitive files blocked
- ✅ **XSS Attacks**: `<script>` tags, JavaScript URLs, and event handlers detected and blocked
- ✅ **Rate Limiting**: Prevents brute force attacks and DDoS attempts
- ✅ **Bot Traffic**: Automated scanners and suspicious user agents blocked

### **🔐 Key Security Features**

1. **Pattern-Based Attack Detection**
   - Real-time scanning for 100+ malicious patterns
   - Customizable threat intelligence rules
   - Regular pattern updates to address new threats

2. **Behavioral Analysis**
   - IP reputation scoring
   - Request frequency analysis
   - Geographic anomaly detection

3. **Zero-Trust Architecture**
   - Every request validated at multiple layers
   - Defense in depth approach
   - No single point of security failure

4. **Compliance Ready**
   - PCI DSS compliant architecture
   - GDPR data protection
   - Financial industry security standards

## 🏗️ Architecture

### Technology Stack
- **Java/Spring Boot**: Core banking services with integrated security filters
- **Golang**: High-performance services with built-in security
- **.NET**: Security-critical services (maintenance and escrow)
- **NGINX**: Advanced security proxy with WAF capabilities
- **Redis & Hazelcast**: Distributed caching and idempotency key management
- **RabbitMQ**: Secure asynchronous messaging
- **Docker**: Containerized deployment with security hardening
- **ArgoCD**: Secure GitOps deployment pipeline

### Communication Protocols
- **REST API**: With comprehensive input validation
- **gRPC**: Secure internal service communication
- **WebSocket**: Real-time updates with authentication
- **HTTPS Only**: All external communication encrypted

### **Security Communication Flow**

```
External Request → NGINX Security Layer → Spring Security Filters → Application Logic
       ↓                   ↓                    ↓                      ↓
[Rate Limiting]    [Pattern Matching]   [Input Validation]    [Business Logic Security]
[Bot Detection]    [WAF Protection]     [Sanitization]       [Fraud Detection]
[IP Blocking]      [Header Security]    [Exception Handling] [Audit Logging]
```

## 🔗 Branch Per Microservice Strategy
- Each microservice was meticulously managed with its own branch, optimizing version control, collaboration, and maintenance.

### 🔁 Automated Pipeline Triggering
- Implemented multibranch generic webhook triggers for automated microservice CI pipeline activation, reducing manual intervention and accelerating feedback loops.

### 🏗️ Docker Image CI Pipelines
- Developed 12 CI pipelines dedicated to Docker image building ensured consistency and reliability in our application's image deployment.

### 🚢 Seamless Docker Image Pushing
- Automated Docker image pushing to our repository post-building, eliminating manual steps and expediting artifact delivery.

### 🌐 Unified ArgoCD CD Pipeline
- Streamlined deployment with a single ArgoCD-based CD pipeline for all microservices, leveraging GitOps principles to ensure consistency, minimize deployment complexities, and enable automated, declarative updates across environments.

#### 🔒 **Enhanced Security in CI/CD Pipeline**
    - Implement Kubernetes Authentication Mechanisms:
        - 🔒 Secure Authentication (OIDC & Service Account Tokens)
            - Switched from static Kubernetes tokens to OIDC authentication (k8s-oidc-token)
            - Reduced privilege exposure by limiting the scope of service accounts.
        - 🔑 Enhanced Jenkins Security:
            - Used Jenkins' Credentials Plugin for secure authentication.
            - Implemented RBAC & audit logging for better access control.
            - Kept Jenkins plugins updated to mitigate security risks.
        - 🛡️ API Security Enhancements:
            - Enforced HTTPS for secure data transmission.
            - Implemented OAuth 2.0 authentication for API security.
            - Added input validation, rate limiting, and encryption for data security.

## **What are the challenges encounter from the starting of the project?**

- `Challenge(1)` Providing real-time updates for wallet in via time, history, ledger, which requires efficient communication between the backend and the front-end.
- `Challenge(2)` Supporting both local currency operations and some foreign currency swapping involves accurate and up-to-date exchange rate handling, which can be resource-intensive.
- `Challenge(3)` The platform must handle high traffic and complex operations wallet transactions and prevent multiple api call on withdraws, transfer calls without degrading performance. 
- `Challenge(4)` Ensuring compliance with local and international regulations for payment systems.
- `Challenge(5)` Detecting Sophisticated Fraud Patterns- Fraudsters often use complex strategies that blend legitimate and illegitimate activities, making detection difficult.
- `Challenge(6)` **Security Threats**: Protecting against SQL injection, XSS, CSRF, DDoS, and other web application vulnerabilities in a microservices environment.

## **How were we able to overcome these challenges?**

- `Solution(1)` I implemented WebSockets to establish a persistent, bidirectional communication channel between the server and clients. This allows the backend to push instant updates to the front-end the moment a transaction occurs, eliminating the need for inefficient client-side polling and ensuring users see their balance and history update in real-time.
- `Solution(2)` To overcome this, we integrated with a reputable third-party financial data API for live and historical exchange rates. We implemented a caching layer (e.g., Redis) to store these rates for a short period (e.g., 5-10 minutes), significantly reducing the number of external API calls and protecting against rate limits. For currency swaps, we used database transactions to ensure atomicity, guaranteeing that the debit from one currency and the credit to another either both succeed or both fail.
- `Solution(3)` **High Traffic and Performance at Scale**
    - The platform must handle high traffic volumes and complex transactional operations (like withdrawals and transfers) without degrading performance, especially by preventing duplicate or malicious API calls that could lead to double-spending or financial losses.
        - *Solution:* We adopted a microservices architecture to decouple services, allowing them to scale independently. To specifically solve the problem of duplicate API calls (e.g., from client-side retries), we implemented Idempotency Keys using Hazelcast as our distributed cache.
            - How it works: Before processing a non-idempotent request (like a withdrawal or transfer), the client must generate a unique idempotency key. The server checks this key against the Hazelcast cluster.
            - If the key is new, the request is processed, and the key is stored with the request's result.
            - If the key exists, the server returns the stored response instead of reprocessing the transaction, preventing duplicate operations.
            This, combined with API rate limiting and database optimization, ensured system stability and data integrity under high load
- `Solution(4)` Ensuring compliance with evolving local and international financial regulations (e.g., KYC, AML) is critical for the platform's legality and user trust.
- *Solution:* The system was designed with security and compliance as a core tenet. This includes secure data storage, end-to-end encryption, and immutable audit trails. We integrated with specialized compliance services for automated KYC (Know Your Customer) and AML (Anti-Money Laundering) checks during user onboarding and transaction monitoring. Furthermore, we established a process for regularly reviewing and updating our internal policies to align with the evolving regulatory landscape.
- `Solution(5)` Created middleware for advanced security fraud detection:
    - Implemented middleware for IP address monitoring and transaction interception based on several criteria
         - **Large Transactions:** Flagged transactions exceeding platform-defined thresholds for further review.
         - **High-Frequency Transactions:** Monitored accounts for unusually high transaction volumes within short time-frames to detect suspicious behavior or could indicate potential money laundering or illegal activity.
         - **Geographic and Risk-Based Monitoring:**  Identified transactions involving high-risk regions/countries or blacklisted wallet addresses to comply with AML regulations.
         - **Behavioral Analysis:** Detected inconsistent behavior, such as large deviations from typical transaction amounts, to prevent fraud 
         - **Multiple Accounts Sharing the Same IP:** Checked for potential sybil attacks by monitoring accounts initiating transactions from the same IP address.
             - **Reason:**
                 - This could be a sign of suspicious activity such as a single entity controlling multiple accounts.
        - **Deposits Followed by Immediate Transfers:** Flagged immediate fund transfers after deposits to prevent potential money laundering activities.
            - **Reason:**
                - This behavior could indicate attempts to obfuscate the origin of the funds (layering phase of money laundering).
        - Implement data Encryption.
        - Implement Event Sourcing to make history difficult to tamper.
        - When any of this list options is detected during user transaction process we call for assistance to block that user wallet and move the transaction (user money) to escrow service with user details or depending the list of crime system found user, system can disable user account which means user can login, deposit but can not withdraw. If user is blocked user can not login his account.

- `Solution(6)` **Multi-Layered Security Architecture**:
    - *Solution:* We implemented a comprehensive 5-layer security architecture combining NGINX perimeter defense with Spring Boot application security:
        1. **NGINX WAF**: Pattern-based attack blocking at the network perimeter
        2. **Spring Security Filters**: Deep content validation and input sanitization
        3. **Exception Handling**: Graceful error responses without information leakage
        4. **Business Logic Security**: Application-specific fraud detection and validation
        5. **Infrastructure Security**: Container hardening and secure service communication
    This defense-in-depth approach ensures that even if one layer is bypassed, multiple other layers provide protection.

## 📋 Services Overview
| Service | Port | Database | Redis | Hazelcast | Description |
|---------|------|----------|-------|-------------|-------------|
| `authentication-service` | 8187 | ✅ | ✅ |❌ | JWT-based auth, 2FA, session management |
| `wallet-service` | 8035 | ✅ | ✅ | ✅ | Multi-currency wallet with gRPC & WebSocket |
| `resiliences-service` | 8531 | ✅ | ✅ | ❌| This is consist of `{beneficiary-app, bank-collection-app, Blacklist-app, Maintenance-service, History-service}`|
| `escrow-service` | 8041 | ✅ | ✅ | ❌ | Escrow account management |
| `deposit-service` | 8020 | ❌ | ❌ | ✅ | Deposit processing|
| `withdraw-service` | 8068 | ❌ | ✅ | ❌ | Withdrawal processing|
| `revenue-service` | 8083 | ✅ | ✅ | ❌ | Revenue tracking and analytics |

### Message Queue Services
| Service | Port | Database | Description |
|---------|------|----------|-------------|
| `notification-service` | 8079 | ❌ | Real-time notifications via RabbitMQ |

### 🔧 Maintenance Service Overview
- The **Maintenance Service** (built in .net) handles automated system maintenance operations including:

#### 💰 Automated Fee Processing
   - **Monthly Maintenance Fees:** Automatically charges maintenance fees to user wallets
   - **Multi-Currency Support:** Processes fees across all supported currencies (USD, EUR, NGN, GBP, JPY, CNY, etc.)

####  🔄 Batch Processing
   - **Scheduled Operations:** Runs maintenance tasks on predefined schedules
   - **Bulk Wallet Updates:** Efficiently processes multiple wallets in batch operations
   - **Currency-wise Processing:** Handles each currency separately with proper exchange rate considerations

#### 💸 Fee Calculation
   - **Percentage-based Fees:** Calculates maintenance fees as a percentage of wallet balance
   - **Minimum/Maximum Limits:** Ensures fees are within reasonable bounds
   - **Balance Validation:** Verifies sufficient funds before deducting fees

#### 🔗 Service Integration
   - **Wallet Service Communication:** Interfaces with wallet service to deduct fees
   - **Revenue Service Integration:** Records all fee transactions in revenue tracking
   - **History Service Logging:** Maintains comprehensive audit trails of all maintenance operations

#### 📊 Maintenance Service Workflow
   1. Schedule Trigger: Maintenance service triggers on monthly schedule
   2. User Wallet Scan: Fetches all user(s) wallets with their balances
   3. Fee Calculation: Computes maintenance fee for each wallet (0.5% of balance) based on user history
   4. Wallet Deduction: Calls wallet service to deduct fees from user wallets
   5. Revenue Recording: Records deducted fees in revenue service as MAINTENANCE_FEE transactions
   6. Audit Logging: Creates comprehensive history records for all operations
   7. Send notification message to users email addresses.

```markdown
## 📊 Monitoring & Observability Stack

A full observability stack is integrated into the platform, providing real-time metrics, distributed tracing, and centralized log aggregation across all Java microservices.

### 🔭 Stack Overview

| Tool | Port | Purpose |
|------|------|---------|
| **Prometheus** | 9091 | Metrics collection & alerting |
| **Grafana** | 3001 | Dashboards & visualization |
| **Loki** | 3100 | Log aggregation backend |
| **Promtail** | — | Log shipping agent |

### 📈 Grafana Dashboard — Banking Java Microservices Overview

The pre-provisioned dashboard (`Banking` folder) includes:

- **Services UP / DOWN** — real-time health status of all services
- **Service Health Table** — per-service UP/DOWN status at a glance
- **Request Rate per Service** — requests per second broken down by service
- **P99 Latency per Service** — 99th percentile response times
- **JVM Heap Usage** — memory consumption per service
- **CPU Usage** — process CPU utilization per service
- **Active HTTP Threads** — Tomcat thread pool usage
- **HikariCP DB Connections** — active database connection pool usage
- **GC Pause Time** — garbage collection pressure per service
- **Open File Descriptors** — OS-level resource usage

### 🚨 Alerting Rules

Prometheus alert rules are pre-configured for:

| Alert | Condition | Severity |
|-------|-----------|----------|
| `ServiceDown` | Service unreachable for > 1 min | Critical |
| `HighRequestLatencyP99` | P99 latency > 2s for 5 min | Warning |
| `HighErrorRate` | 5xx error rate > 5% for 3 min | Critical |
| `JvmMemoryHighUsage` | Heap usage > 85% for 5 min | Warning |
| `JvmGcPressureHigh` | GC consuming > 10% of time | Warning |
| `HikariConnectionPoolExhausted` | > 5 threads waiting for DB connection | Critical |

### 📁 Monitoring Directory Structure

```markdown
monitoring/
├── prometheus/
│   ├── prometheus.yml       # Scrape configs for all Java services
│   └── alert.rules.yml      # Alerting rules
├── loki/
│   └── loki-config.yml      # Loki log storage config (14-day retention)
├── promtail/
│   └── promtail-config.yml  # Docker log scraping & Spring Boot log parsing
└── grafana/
    ├── provisioning/
    │   ├── datasources/
    │   │   └── datasources.yml   # Auto-provisions Prometheus & Loki
    │   └── dashboards/
    │       └── dashboards.yml    # Points Grafana to dashboard files
    └── dashboards/
        └── banking-java-overview.json  # Pre-built dashboard
```
### 🚀 Accessing the Monitoring Stack

```bash
# Start everything
docker compose up -d

# Grafana — dashboards & visualization
http://localhost:3001  (admin / admin)

# Prometheus — metrics & targets
http://localhost:9091

# Check all scrape targets are UP
http://localhost:9091/targets
```

### ⚙️ How It Works

1. Each Spring Boot service exposes `/actuator/prometheus` with JVM, HTTP, and DB metrics
2. **Prometheus** scrapes all services every 15 seconds and evaluates alert rules
3. **Promtail** collects Docker container logs from all Java services and ships them to **Loki**
4. **Grafana** auto-provisions both datasources and the dashboard on startup — no manual setup required
5. Logs are retained for **14 days**, metrics for **15 days**
```

## 🚀 How to Run This Project on Your System

### 🧩 Prerequisites

Before you begin, make sure you have the following installed:

* [Docker](https://www.docker.com/get-started)
* [Docker Compose](https://docs.docker.com/compose/install/) (included by default with Docker Desktop)
* Git

---

### 🪄 Steps to Run

1. **Clone the repository**

   ```bash
   git clone https://github.com/<your-username>/<your-repo-name>.git
   ```

2. **Navigate into the project directory**

   ```bash
   cd <your-repo-name>
   ```

   > **Note:** All microservices must be inside the same base directory for Docker Compose to detect them properly.

3. **Configure Email for Notification Service**
   Before starting the services, open the file:

   ```
   notification-service/src/main/resources/application.yml
   ```

   Locate the **email configuration section**, and update it with your **email app password** (for example, a Gmail App Password).
   This is required so that the `notification-service` can send and receive email messages.

   Example:

   ```yaml
   spring:
     mail:
       host: smtp.gmail.com
       port: 587
       username: your_email@gmail.com
       password: your_app_password
       properties:
         mail:
           smtp:
             auth: true
             starttls:
               enable: true
   ```

4. **Build and start all services**

   ```bash
   docker compose up --build
   ```

   This will:

   * Build all microservice images
   * Start containers for backend services, frontend, database, and supporting tools (Redis, RabbitMQ, Zipkin, etc.)
   * Set up all necessary networks

5. **Access the application**

   * 🌐 **Frontend:** [http://localhost:4173](http://localhost:4173)
   * 🔐 **Authentication Service:** [http://localhost:8187](http://localhost:8187)
   * 💰 **Wallet Service:** [http://localhost:8035](http://localhost:8035)
   * ? **Other Services are expose in the docker and Ngnix:**
   * 📊 **Zipkin Dashboard:** [http://localhost:9411](http://localhost:9411)
    
   *(Update the ports if your setup differs)*

6. **Stop all services**

   ```bash
   docker compose down
   ```

---

### 🧹 Optional Cleanup

If you want to remove all containers, networks, and volumes created by Docker:

```bash
docker system prune -a
```

If you want to stop **one service** (for example, when making changes to the frontend):

```bash
docker compose stop frontend
```

To **restart only that service** without affecting others:

```bash
docker compose up --build frontend
```

For backend services (example):

```bash
docker compose stop wallet-service
docker compose up --build wallet-service
```
## 🎯 API Gateway

The API gateway routes requests to appropriate microservices and handles:
- Request routing and load balancing
- Authentication and authorization
- Rate limiting and DDoS protection
- API composition and aggregation
- **Security validation** at the network perimeter

## 📞 Support

For technical support or questions about this microservices architecture, please refer to the individual service documentation or check the service logs for specific issues.

For **security-related inquiries**, please contact the security team directly with detailed information about any concerns or identified vulnerabilities.
