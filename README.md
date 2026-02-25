# 💳 KoshPay – Secure UPI-Based Digital Wallet System

> A production-style full-stack fintech wallet platform with real-time transfers, fraud detection, scheduled payments, and a role-based admin control panel.

---

## 🧰 Tech Stack

| Layer | Technology |
|---|---|
| Frontend | React 18 (Vite), React Router v6, Recharts |
| Backend | Spring Boot 3, Spring Security, Spring Data JPA |
| Database | MySQL (local / Docker) · PostgreSQL (Render) |
| Auth | JWT (HS256) with Role-Based Access Control |
| Real-Time | WebSocket (STOMP) |
| Deployment | Docker, Render (Backend + DB + Frontend) |
| API Docs | Swagger UI / OpenAPI 3 |

---

## 🏗 System Architecture

```
                ┌──────────────────────────────┐
                │          React App           │
                │         (Frontend)           │
                │    http://localhost:5173      │
                └──────────────┬───────────────┘
                               │
                    REST API + WebSocket
                               │
                               ▼
                ┌──────────────────────────────┐
                │       Spring Boot API        │
                │          (Backend)           │
                │    http://localhost:8080      │
                └──────────────┬───────────────┘
                               │
                         JPA / Hibernate
                               │
                               ▼
                ┌──────────────────────────────┐
                │     MySQL / PostgreSQL       │
                │     Port: 3306 / 5432        │
                └──────────────────────────────┘
```

---

## 🐳 Run With Docker

### Prerequisites
- Docker Desktop installed and running

### Start Everything

```bash
docker compose up --build
```

All three services (frontend, backend, database) start automatically. ✅

### Service URLs

| Service  | URL |
|----------|-----|
| Frontend | http://localhost:5173 |
| Backend  | http://localhost:8080 |
| Swagger  | http://localhost:8080/swagger-ui.html |
| MySQL    | localhost:3306 |

---

## 📁 Project Structure

```
KOSHPAY/
├── .gradle/
├── .vscode/
├── backend/
├── frontend/
├── bin/
├── build/
├── Screenshots/
├── docker-compose.yml
├── README.md
└── video_overview.mp4
```

---

## ⚛ Frontend Structure

```
frontend/
│
├── Dockerfile
├── package.json
├── vite.config.js
│
└── src/
    │
    ├── App.jsx                              ← Root router with role-aware admin redirects
    ├── main.jsx                             ← React entry point
    ├── index.css                            ← Global styles
    │
    ├── api/
    │   └── axios.js                         ← Axios instance with JWT interceptor + 401 handler
    │
    ├── assets/
    │   └── react.svg
    │
    ├── auth/
    │   ├── AuthContext.jsx                  ← Global auth state: token, role, login, logout, session timers
    │   ├── PrivateRoute.jsx                 ← Protects user-only routes
    │   └── AdminRoute.jsx                   ← Protects admin routes by allowedRoles prop
    │
    ├── components/
    │   ├── BalanceCard.jsx                  ← Live wallet balance display with WebSocket
    │   ├── BalanceCard.css
    │   ├── Navbar.jsx                       ← Top navigation for regular users
    │   ├── Navbar.css
    │   ├── Toast.jsx                        ← Global toast notification system
    │   ├── Toast.css
    │   ├── TransactionTable.jsx             ← Reusable transaction list component
    │   ├── TransactionTable.css
    │   ├── UpdatePin.jsx                    ← PIN change modal
    │   ├── UpdatePin.css
    │   └── admin/
    │       ├── AdminSidebar.jsx             ← Role-filtered sidebar navigation
    │       └── AdminSidebar.css
    │
    ├── layouts/
    │   ├── AdminLayout.jsx                  ← Admin shell: sidebar + topbar with role label
    │   └── AdminLayout.css
    │
    ├── pages/
    │   ├── Login.jsx                        ← Unified login page (User / Admin toggle)
    │   ├── Login.css
    │   ├── Register.jsx                     ← New user registration
    │   ├── Register.css
    │   ├── Dashboard.jsx                    ← User home: balance, quick actions, recent transactions
    │   ├── Dashboard.css
    │   ├── Transfer.jsx                     ← UPI transfer with PIN + OTP flow
    │   ├── Transfer.css
    │   ├── Transactions.jsx                 ← Full user transaction history
    │   ├── Transactions.css
    │   ├── Contacts.jsx                     ← Saved payees management
    │   ├── Contacts.css
    │   ├── MyQR.jsx                         ← Personal UPI QR code generator
    │   ├── MyQR.css
    │   ├── ScanQR.jsx                       ← QR scanner for instant payments
    │   ├── ScanQR.css
    │   ├── ScheduledPayments.jsx            ← Schedule, view and cancel future payments
    │   ├── ScheduledPayments.css
    │   ├── Security.jsx                     ← PIN management and security settings
    │   ├── Security.css
    │   └── admin/
    │       ├── AdminDashboard.jsx           ← Platform overview (Super Admin only)
    │       ├── AdminDashboard.css
    │       ├── Analytics.jsx                ← Pie + bar charts, success rate metrics
    │       ├── Analytics.css
    │       ├── AdminTransactions.jsx        ← Full transaction log with search and filter
    │       ├── AdminTransactions.css
    │       ├── AuditLogs.jsx                ← User action history with action type filter
    │       └── AuditLogs.css
    │
    └── websocket/
        └── balanceSocket.js                 ← STOMP WebSocket client for live balance updates
```

---

## ☕ Backend Structure

```
backend/
│
├── Dockerfile
├── pom.xml
│
└── src/
    ├── main/
    │   ├── java/com/ewallet/wallet_service/
    │   │   │
    │   │   ├── WalletServiceApplication.java               ← Spring Boot entry point
    │   │   │
    │   │   ├── config/
    │   │   │   ├── AdminInitializer.java                   ← Seeds 4 role-based admins on startup
    │   │   │   ├── CorsConfig.java                         ← CORS policy configuration
    │   │   │   ├── OpenApiConfig.java                      ← Swagger / OpenAPI setup
    │   │   │   └── SecurityBeansConfig.java                ← PasswordEncoder bean
    │   │   │
    │   │   ├── controller/
    │   │   │   ├── AuthController.java                     ← Login endpoint, JWT issued with real role
    │   │   │   ├── AdminController.java                    ← Admin endpoints with @PreAuthorize per method
    │   │   │   ├── ContactController.java                  ← Saved contacts CRUD
    │   │   │   ├── ScheduledPaymentController.java         ← Schedule, list, cancel payments
    │   │   │   ├── SetupController.java                    ← Initial wallet and UPI setup
    │   │   │   ├── UpiController.java                      ← UPI ID lookup and management
    │   │   │   ├── UpiTransferController.java              ← Transfer execution with fraud + OTP
    │   │   │   └── WalletController.java                   ← Balance and transaction history
    │   │   │
    │   │   ├── dto/
    │   │   │   ├── request/
    │   │   │   │   ├── ContactCreateRequest.java
    │   │   │   │   ├── LoginRequest.java
    │   │   │   │   ├── SchedulePaymentRequest.java
    │   │   │   │   ├── TransferRequest.java
    │   │   │   │   ├── UpiTransferRequest.java
    │   │   │   │   └── UserCreateRequest.java
    │   │   │   └── response/
    │   │   │       ├── AdminAnalyticsResponse.java
    │   │   │       ├── AdminSummaryResponse.java
    │   │   │       ├── AdminTransactionResponse.java
    │   │   │       ├── AuthResponse.java
    │   │   │       ├── BalanceUpdateResponse.java
    │   │   │       ├── ContactResponse.java
    │   │   │       ├── OtpResponse.java
    │   │   │       ├── QrPayloadResponse.java
    │   │   │       ├── ScheduledPaymentResponse.java
    │   │   │       ├── TransactionResponse.java
    │   │   │       ├── TransactionStatusDistributionResponse.java
    │   │   │       ├── UpiIdResponse.java
    │   │   │       └── WalletResponse.java
    │   │   │
    │   │   ├── entity/
    │   │   │   ├── Admin.java                              ← Admin account with role field
    │   │   │   ├── AuditLog.java                           ← User action audit trail
    │   │   │   ├── Contact.java                            ← Saved payee contact
    │   │   │   ├── ScheduledPayment.java                   ← Pending scheduled transfer
    │   │   │   ├── Transaction.java                        ← Core transaction record
    │   │   │   ├── TransactionStatus.java                  ← Enum: INITIATED, PENDING, SUCCESS, FAILED
    │   │   │   ├── User.java                               ← Platform user account
    │   │   │   ├── VirtualPaymentAddress.java              ← UPI ID record
    │   │   │   └── Wallet.java                             ← User wallet with balance
    │   │   │
    │   │   ├── exception/
    │   │   │   ├── ApiErrorResponse.java                   ← Standard error response shape
    │   │   │   ├── GlobalExceptionHandler.java             ← Centralized @RestControllerAdvice
    │   │   │   ├── InsufficientBalanceException.java
    │   │   │   ├── InvalidRequestException.java
    │   │   │   └── ResourceNotFoundException.java
    │   │   │
    │   │   ├── fraud/
    │   │   │   ├── model/
    │   │   │   │   ├── FraudContext.java                   ← Transaction context passed to all rules
    │   │   │   │   └── FraudResult.java                    ← Risk score and decision output
    │   │   │   ├── rules/
    │   │   │   │   ├── HighAmountRule.java                 ← Flags unusually large transfers
    │   │   │   │   ├── NewPayeeRule.java                   ← Flags first-time payees
    │   │   │   │   ├── TransactionVelocityRule.java        ← Flags rapid repeated transfers
    │   │   │   │   └── WalletDrainPercentageRule.java      ← Flags transfers draining the wallet
    │   │   │   └── service/
    │   │   │       ├── FraudDecision.java                  ← Enum: ALLOW, REVIEW, BLOCK
    │   │   │       ├── FraudDetectionService.java          ← Runs all rules, aggregates risk score
    │   │   │       └── FraudRule.java                      ← Interface implemented by each rule
    │   │   │
    │   │   ├── repository/
    │   │   │   ├── AdminRepository.java
    │   │   │   ├── AuditLogRepository.java
    │   │   │   ├── ContactRepository.java
    │   │   │   ├── ScheduledPaymentRepository.java
    │   │   │   ├── TransactionRepository.java
    │   │   │   ├── UserRepository.java
    │   │   │   ├── VirtualPaymentAddressRepository.java
    │   │   │   └── WalletRepository.java
    │   │   │
    │   │   ├── security/
    │   │   │   ├── JwtFilter.java                          ← Extracts and validates JWT on every request
    │   │   │   ├── JwtUtil.java                            ← Token generation, extraction, validation
    │   │   │   └── SecurityConfig.java                     ← Filter chain, CORS, @EnableMethodSecurity
    │   │   │
    │   │   ├── service/
    │   │   │   ├── AdminAnalyticsService.java              ← Summary and distribution aggregations
    │   │   │   ├── AuditLogService.java                    ← Records user actions to audit log
    │   │   │   ├── BalanceWebSocketService.java            ← Pushes balance updates via WebSocket
    │   │   │   ├── ContactService.java                     ← Saved contacts business logic
    │   │   │   ├── QrService.java                          ← QR code generation from UPI ID
    │   │   │   ├── ScheduledPaymentExecutor.java           ← Triggered by scheduler, runs due payments
    │   │   │   ├── ScheduledPaymentProcessingService.java  ← Core scheduled payment execution logic
    │   │   │   ├── ScheduledPaymentService.java            ← Schedule management (create / cancel)
    │   │   │   ├── TransactionStatusService.java           ← Lifecycle state machine
    │   │   │   ├── UpiResolverService.java                 ← Resolves UPI ID to wallet
    │   │   │   ├── UserService.java                        ← User service interface
    │   │   │   ├── WalletService.java                      ← Wallet service interface
    │   │   │   ├── impl/
    │   │   │   │   ├── UserServiceImpl.java                ← Registration, login, profile
    │   │   │   │   └── WalletServiceImpl.java              ← Balance, transfer, history
    │   │   │   └── util/
    │   │   │       ├── OtpService.java                     ← OTP generation and verification
    │   │   │       └── UpiIdGenerator.java                 ← Generates unique UPI handle
    │   │   │
    │   │   └── websocket/
    │   │       └── WebSocketConfig.java                    ← STOMP endpoint and broker configuration
    │   │
    │   └── resources/
    │       ├── application.properties                      ← All config (DB, JWT, admin, scheduling)
    │       └── banner.txt
    │
    └── test/java/com/ewallet/wallet_service/
        ├── WalletServiceApplicationTests.java
        ├── config/
        │   └── ConfigTest.java
        ├── controller/
        │   ├── AdminControllerTest.java
        │   ├── AuthControllerTest.java
        │   ├── ContactControllerTest.java
        │   ├── ScheduledPaymentControllerTest.java
        │   ├── SetupControllerTest.java
        │   ├── UpiControllerTest.java
        │   ├── UpiTransferControllerTest.java
        │   └── WalletControllerTest.java
        ├── dto/
        │   └── DtoTest.java
        ├── entity/
        │   └── EntityTest.java
        ├── exception/
        │   └── ExceptionTest.java
        ├── fraud/
        │   ├── model/
        │   │   └── ModelCoverageTest.java
        │   ├── rules/
        │   │   └── FraudRulesTest.java
        │   └── service/
        │       └── FraudDetectionServiceTest.java
        ├── repository/
        │   └── RepoTest.java
        ├── security/
        │   ├── JwtFilterTest.java
        │   ├── JwtUtilTest.java
        │   └── SecurityConfigTest.java
        └── service/
            ├── AdminAnalyticsServiceTest.java
            ├── AuditLogServiceTest.java
            ├── BalanceWebSocketServiceTest.java
            ├── ContactServiceTest.java
            ├── QrServiceTest.java
            ├── ScheduledPaymentExecutorTest.java
            ├── ScheduledPaymentProcessingServiceTest.java
            ├── ScheduledPaymentServiceTest.java
            ├── TransactionStatusServiceTest.java
            ├── UpiResolverServiceTest.java
            ├── WalletServiceTest.java
            ├── impl/
            │   ├── UserServiceImplTest.java
            │   └── WalletServiceImplTest.java
            ├── util/
            │   ├── OtpServiceTest.java
            │   └── UpiIdGeneratorTest.java
            └── websocket/
                └── WebSocketConfigTest.java
```

---

## 👤 User Features

### 🔐 Authentication
* JWT-based login and registration
* BCrypt password hashing
* Token expiry with 5-minute warning toast and automatic logout
* Role-based route protection (User vs Admin)

### 💸 UPI Transfers
* Transfer by UPI ID with PIN verification
* Multi-stage fraud evaluation before every transfer
* OTP challenge triggered for high-risk or high-value transfers (> ₹1000)
* Self-transfer prevention
* Insufficient balance validation
* ACID-safe balance updates with rollback on failure

### 🔄 Transaction Lifecycle
```
INITIATED → PENDING → SUCCESS
                    ↘ FAILED
```
Every stage is tracked and visible to both the user and admin.

### 📡 Real-Time Balance Updates
* WebSocket (STOMP) connection established on login
* Balance card updates instantly after every transfer without a page refresh

### 📱 QR Code Payments
* Generate a personal UPI QR code from your UPI ID
* Scan another user's QR to pre-fill and initiate an instant payment

### 📅 Scheduled Payments
* Schedule a future UPI transfer with amount and execution date
* Cancel any pending scheduled payment before it executes
* Spring scheduler auto-executes due payments in the background
* Execution failure handled gracefully with status tracking

### 📜 Transaction History
* Complete history with status badges per transaction
* Credit (green) and Debit (red) indicators
* Timestamp and UPI ID for every entry

### 🔒 Security Settings
* Change wallet PIN from the Security page
* Old PIN verification required before update

---

## 👑 Admin Panel

KoshPay has a centralized role-based admin system. One Super Admin has full access across all four tabs. Three specialized sub-admins each have access to exactly one tab — enforced at both the API layer (Spring `@PreAuthorize`) and the UI layer (React `AdminRoute` and `AdminSidebar`).

### 🔑 Admin Roles

| Role | Tab Access | API Access |
|---|---|---|
| `ROLE_SUPER_ADMIN` | Dashboard, Analytics, Transactions, Audit Logs | All `/api/admin/**` endpoints |
| `ROLE_ANALYTICS` | Analytics only
| `ROLE_TRANSACTIONS` | Transactions only
| `ROLE_AUDIT_LOGS` | Audit Logs only 

### 🔒 How Security Is Enforced

```
Admin Login
    │
    ▼
AuthController reads admin.getRole() from DB
    │
    ▼
JWT issued with real role claim (e.g. "ROLE_ANALYTICS")
    │
    ├── Frontend: AuthContext decodes role from token
    │       │
    │       ├── AdminSidebar renders only permitted tab links
    │       ├── AdminRoute blocks wrong-tab URLs → Access Denied page
    │       └── App.jsx redirects each role to correct landing tab on login
    │
    └── Backend: JwtFilter extracts role from token on every request
            │
            └── @PreAuthorize on each AdminController method
                    → 403 Forbidden returned if role does not match
```

Double protection — the UI hides tabs and the backend blocks API calls independently of each other.

### 📊 Admin Tab Details

**Dashboard** *(Super Admin only)*
* Total transaction count across all wallets
* Total successful transfer volume (₹)
* Platform-wide success rate percentage
* Fraud block count

**Analytics** *(Super Admin + Analytics Admin)*
* Pie chart — live transaction status distribution (Success / Failed / Pending / Initiated)
* Bar chart — lifecycle breakdown with color coding per status
* Success rate metric card
* Successful volume card (Super Admin only — requires summary API access that Analytics Admin does not have)

**Transactions** *(Super Admin + Transactions Admin)*
* Full transaction log with From UPI, To UPI, Amount, Status, Timestamp
* Search by transaction ID or UPI handle
* Filter by status (All / Success / Failed / Pending)
* Live record count display

**Audit Logs** *(Super Admin + Audit Logs Admin)*
* Complete user action trail — Login, Transfer, PIN change and more
* Filter by action type
* Status badges per action
* Old balance shown for transfer events

### 🌱 Admin Seeding

All four admins are created automatically on first backend startup by `AdminInitializer`. The table is only seeded when it is empty — restarting the server never creates duplicates.

| Role | Email | Password |
|---|---|---|
| Super Admin | Set via `ADMIN_EMAIL` env var | Set via `ADMIN_PASSWORD` env var |
| Analytics Admin | analytics@koshpay.com | ************* |
| Transactions Admin | transactions@koshpay.com | ************* |
| Audit Logs Admin | auditlogs@koshpay.com | ************* |

---

## 🛡 Fraud Detection Engine

The fraud engine runs before every transfer and evaluates four independent rules:

| Rule | What It Checks |
|---|---|
| `HighAmountRule` | Transfer amount exceeds a configured threshold |
| `TransactionVelocityRule` | Too many transfers made in a short time window |
| `WalletDrainPercentageRule` | Transfer would drain a large percentage of wallet balance |
| `NewPayeeRule` | Recipient has never been paid before by this sender |

Each rule returns a risk score. `FraudDetectionService` aggregates all scores into a final decision:

```
ALLOW  → Transaction proceeds normally
REVIEW → OTP challenge required before proceeding
BLOCK  → Transaction rejected immediately
```

---

## 🧪 Test Coverage

### Authentication
* Valid login → JWT returned with correct role
* Invalid password → 401 error
* Expired token → Rejected by JWT filter
* Unauthorized route → Redirected to login

### Transfers
* Correct PIN → Transaction processed
* Wrong PIN → Blocked at validation layer
* Amount > ₹1000 → OTP screen triggered
* Invalid OTP → Transfer rejected
* Fraud score high → Blocked by engine
* Self-transfer → Blocked by business rule
* Insufficient balance → Rejected before any debit

### Scheduled Payments
* Create schedule → Stored as PENDING
* Cancel before execution → Status set to CANCELLED
* Auto-execute on due date → SUCCESS
* Insufficient balance at execution time → FAILED with status recorded

### Admin — Role-Based Access
* Super Admin login → All 4 tabs visible, all APIs return 200
* Analytics Admin login → Only Analytics tab visible, other API calls return 403
* Transactions Admin login → Only Transactions tab visible, other API calls return 403
* Audit Logs Admin login → Only Audit Logs tab visible, other API calls return 403
* Any sub-admin manually typing a restricted URL → Redirected to Access Denied page
* Analytics page for Analytics Admin → Charts load correctly, volume card hidden
* All admins auto-redirected to their correct landing tab immediately on login

---

## 🌍 Deployment — Render

KoshPay is deployed on Render with three services:

| Service | Type |
|---|---|
| Backend | Web Service (Spring Boot JAR) |
| Database | Managed PostgreSQL |
| Frontend | Static Site (Vite build) |

### Environment Variables (set on Render backend service)

| Variable | Description |
|---|---|
| `ADMIN_EMAIL` | Super Admin login email |
| `ADMIN_PASSWORD` | Super Admin login password |
| `DB_HOST` | PostgreSQL host from Render |
| `DB_PORT` | PostgreSQL port (default 5432) |
| `DB_NAME` | Database name |
| `DB_USER` | Database user |
| `DB_PASSWORD` | Database password |

Sub-admin accounts are seeded automatically by `AdminInitializer` on first deploy. No manual SQL required.

---

## 👥 Authors

**Shravani Korde** — Cloud Engineer, GlideCloud Solutions

**Gautam Jha** — Cloud Engineer, GlideCloud Solutions

**Siddhant Ghodke** — Cloud Engineer, GlideCloud Solutions

## 👥 Mentor

**Vikrant Kulkarni** — Generative AI Engineer, GlideCloud Solutions