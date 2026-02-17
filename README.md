# 💳 KoshPay – Secure UPI-Based Digital Wallet System

KoshPay is a production-style full-stack fintech wallet platform built using:

* ⚛ React (Vite)
* ☕ Spring Boot
* 🛢 MySQL
* 🔐 JWT Authentication
* 📡 WebSocket (Real-Time Balance Updates)
* 🛡 Fraud Detection Engine
* 🔄 Transaction Lifecycle Engine
* ⏳ Scheduled Payment Processor
* 📊 Admin Analytics Dashboard
* 🐳 Dockerized Deployment

---

# 🏗 System Architecture

```
                ┌──────────────────────────┐
                │        React App         │
                │      (Frontend)          │
                │    http://localhost:5173 │
                └──────────────┬───────────┘
                               │ REST API
                               ▼
                ┌──────────────────────────┐
                │     Spring Boot API      │
                │        (Backend)         │
                │    http://localhost:8080 │
                └──────────────┬───────────┘
                               │ JPA
                               ▼
                ┌──────────────────────────┐
                │         MySQL DB         │
                │       Port: 3306         │
                └──────────────────────────┘
```

---

# 🐳 Run With Docker

### 1️⃣ Start Docker Desktop

Make sure Docker is running.

### 2️⃣ From Root Folder Run

```bash
docker compose up --build
```

That’s it ✅

---

## 🌐 Service Ports

| Service  | URL                                            |
| -------- | ---------------------------------------------- |
| Frontend | [http://localhost:5173](http://localhost:5173) |
| Backend  | [http://localhost:8080](http://localhost:8080) |
| MySQL    | localhost:3306                                 |

---

# 📁 Global Project Structure

```
KOSHPAY/
│
├── .gradle/
├── .vscode/
├── backend/
├── frontend/
├── bin/
├── build/
├── Screenshots/
├── docker-compose.yml
├── README.md
└── Video_Overview.mp4
```

---

# ⚛ Frontend Structure

```
frontend/
│
├── Dockerfile
├── package.json
├── vite.config.js
│
└── src/
    │
    ├── App.jsx
    ├── main.jsx
    ├── index.css
    │
    ├── api/
    │   └── axios.js
    │
    ├── assets/
    │   └── react.svg
    │
    ├── auth/
    │   ├── AuthContext.jsx
    │   ├── PrivateRoute.jsx
    │   └── AdminRoute.jsx
    │
    ├── components/
    │   ├── BalanceCard.jsx
    │   ├── Navbar.jsx
    │   ├── TransactionTable.jsx
    │   ├── UpdatePin.jsx
    │   └── admin/
    │       └── AdminSidebar.jsx
    │
    ├── layouts/
    │   └── AdminLayout.jsx
    │
    ├── pages/
    │   ├── Dashboard.jsx
    │   ├── Transfer.jsx
    │   ├── Transactions.jsx
    │   ├── Contacts.jsx
    │   ├── MyQR.jsx
    │   ├── ScanQR.jsx
    │   ├── ScheduledPayments.jsx
    │   ├── Security.jsx
    │   └── admin/
    │       ├── AdminDashboard.jsx
    │       ├── AdminTransactions.jsx
    │       ├── Analytics.jsx
    │       ├── AuditLogs.jsx
    │
    └── websocket/
        └── balanceSocket.js
```

---

# ☕ Backend Structure

```
backend/
│
├── Dockerfile
├── pom.xml
└── src/
    ├── main/
    │   ├── java/com/ewallet/wallet_service/
    │   │
    │   │   ├── WalletServiceApplication.java
    │   │
    │   │   ├── config/
    │   │   ├── controller/
    │   │   │   ├── AuthController.java
    │   │   │   ├── UpiTransferController.java
    │   │   │   ├── ScheduledPaymentController.java
    │   │   │   ├── AdminController.java
    │   │   │   ├── WalletController.java
    │   │   │   └── ContactController.java
    │   │
    │   │   ├── dto/request/
    │   │   ├── dto/response/
    │   │   ├── entity/
    │   │   ├── repository/
    │   │   ├── security/
    │   │   ├── fraud/
    │   │   ├── service/
    │   │   │   ├── impl/
    │   │   │   └── util/
    │   │   └── websocket/
    │   │
    │   └── resources/
    │       ├── application.properties
    │       └── banner.txt
    │
    └── test/java/com/ewallet/wallet_service/
        ├── controller/
        ├── service/
        ├── security/
        ├── entity/
        ├── exception/
        └── config/
```

---

# 👤 User Features

### 🔐 Authentication

* JWT login/register
* Secure password hashing
* Role-based access

### 💸 UPI Transfers

* PIN verification
* Fraud engine risk evaluation
* OTP challenge (> ₹1000 or high risk)
* Self-transfer prevention
* Insufficient balance validation
* ACID-safe transaction updates

### 🔄 Transaction Lifecycle

* INITIATED
* PENDING
* SUCCESS
* FAILED

### 📡 Real-Time Updates

* WebSocket balance updates

### 📱 QR Features

* Generate UPI QR
* Scan QR for instant payment

### 📅 Scheduled Payments

* Schedule future transfer
* Cancel before execution
* Auto-execution via scheduler
* Execution failure handling

### 📜 History

* Transaction list
* Status badges
* Credit/Debit indicators

---

# 👑 Admin Features

### 📊 Dashboard

* Total Transactions
* Successful Volume
* Success Rate
* Fraud Block Count

### 📈 Analytics

* Pie chart (Status Distribution)
* Bar chart (Lifecycle Breakdown)

### 📜 Monitoring

* Full transaction logs
* Audit logs
* Fraud activity tracking

---

# 🛡 Security Features

* JWT authentication
* BCrypt PIN hashing
* Fraud rule engine:

  * High amount rule
  * Transaction velocity rule
  * Wallet drain percentage rule
  * New payee rule
* OTP verification
* Transaction rollback on failure
* Scheduled execution isolation (REQUIRES_NEW)

---

# 🧪 Test Cases

## ✅ Authentication

* Valid login → Success
* Invalid password → Error
* Unauthorized route → Blocked

## ✅ Transfer

* Correct PIN → Processed
* Wrong PIN → Blocked
* Amount > ₹1000 → OTP required
* Invalid OTP → Failed
* Fraud risk high → Blocked
* Self-transfer → Blocked
* Insufficient balance → Failed

## ✅ Scheduled Payments

* Create schedule → Stored as PENDING
* Cancel before execution → Cancelled
* Auto execute on time → SUCCESS
* Insufficient balance at execution → FAILED

## ✅ Admin

* Analytics loads correctly
* Fraud blocks counted
* Audit logs recorded
* Status distribution accurate

---

# 👥 Authors

Shravani Korde
Cloud Engineer – GlideCloud

Gautam Jha
Cloud Engineer – GlideCloud

Siddhant Ghodke
Cloud Engineer – GlideCloud


