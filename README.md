<h1 align="center">🌾 Farmer to Retailer Trading Platform 🛒</h1>

<p align="center">
  <img src="https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white" alt="Spring Boot">
  <img src="https://img.shields.io/badge/React-20232A?style=for-the-badge&logo=react&logoColor=61DAFB" alt="React">
  <img src="https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white" alt="PostgreSQL">
  <img src="https://img.shields.io/badge/Razorpay-02042B?style=for-the-badge&logo=razorpay&logoColor=3395FF" alt="Razorpay">
  <img src="https://img.shields.io/badge/Cloudinary-3448C5?style=for-the-badge&logo=cloudinary&logoColor=white" alt="Cloudinary">
  <img src="https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=JSON%20web%20tokens&logoColor=white" alt="JWT">
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Status-Completed-brightgreen?style=flat-square" alt="Status">
  <img src="https://img.shields.io/badge/Infosys-Virtual_Internship-orange?style=flat-square" alt="Infosys">
</p>



> 🚀 **Direct farmer-to-retailer trading built during the Infosys Virtual Internship.**  
> Removes middlemen, boosts farmer margins, and gives retailers transparent pricing with bidding or instant buy.

---

## 🔗 Quick Links

| 📌 Section | 🔗 Jump To |
|------------|------------|
| ✨ Features | [View](#-features) |
| 🏗️ Architecture | [View](#-architecture) |
| 💻 Tech Stack | [View](#-tech-stack) |
| ⚙️ Setup | [View](#%EF%B8%8F-setup) |
| 🔒 Security | [View](#-security) |
| 🗺️ Roadmap | [View](#%EF%B8%8F-roadmap) |

---

## ⭐ Highlights

| Feature | Description |
|---------|-------------|
| 🎯 **Dual Selling Modes** | Auction bidding or direct buy |
| 💳 **Secure Payments** | Razorpay integration with webhook validation |
| 🖼️ **Cloud Media** | Images served via Cloudinary CDN |
| 🔔 **Smart Notifications** | Email + in-app; schedulers handle expiries and digests |
| ⭐ **Trust System** | Ratings and feedback across trades |
| 🔐 **Stateless Security** | JWT + role-based access (Farmer/Retailer/Admin) |

---

## ✨ Features

### 👨‍🌾 Farmer Portal
| Feature | Description |
|---------|-------------|
| 📝 | Create and manage crop listings with photos, quantity, and pricing |
| 🏷️ | Pick selling mode: bidding or fixed-price |
| 📊 | Track bids, accept/decline offers, and trigger settlement |
| 📧 | Receive email and in-app notifications |
| ⭐ | Build reputation through ratings and feedback |

### 🛒 Retailer Portal
| Feature | Description |
|---------|-------------|
| 🔍 | Browse and search crops with filters |
| 💰 | Place bids or buy now; pay securely via Razorpay |
| 📦 | Follow order status with notifications |
| ✍️ | Rate and review farmers after fulfillment |

### 🌐 Platform-wide
| Feature | Description |
|---------|-------------|
| 🔑 | JWT auth (stateless) and role-based authorization |
| ⏰ | Scheduler-driven jobs (bid expiry, payout reminders, email digests) |
| 🔄 | CI for the Spring Boot backend |

---

## 🏗️ Architecture

```
┌─────────────┐     ┌──────────────────┐     ┌─────────────────┐
│   React     │────▶│   Spring Boot    │────▶│   PostgreSQL    │
│   Frontend  │◀────│   REST APIs      │◀────│   Database      │
└─────────────┘     └────────┬─────────┘     └─────────────────┘
                             │
              ┌──────────────┼──────────────┐
              ▼              ▼              ▼
        ┌──────────┐  ┌──────────┐  ┌──────────┐
        │ Razorpay │  │Cloudinary│  │  SMTP    │
        │ Payments │  │  Media   │  │  Email   │
        └──────────┘  └──────────┘  └──────────┘
```

![Architecture diagram](https://via.placeholder.com/1280x360?text=React+SPA+%E2%86%92+Spring+Boot+%E2%86%92+PostgreSQL+%7C+Razorpay+%7C+Cloudinary)  

> 💡 *React SPA calls Spring Boot REST; Spring integrates Razorpay (payments), Cloudinary (media), PostgreSQL (data), SMTP (email), and a scheduler for timed jobs. JWT secures APIs and stays stateless for scaling.*

---

## 💻 Tech Stack

| Layer | Technology | Badge |
|-------|------------|-------|
| 🎨 **Frontend** | React | ![React](https://img.shields.io/badge/React-20232A?style=flat-square&logo=react&logoColor=61DAFB) |
| ⚙️ **Backend** | Spring Boot, Spring Security (JWT), Schedulers | ![Spring](https://img.shields.io/badge/Spring_Boot-6DB33F?style=flat-square&logo=spring-boot&logoColor=white) |
| 💳 **Payments** | Razorpay | ![Razorpay](https://img.shields.io/badge/Razorpay-02042B?style=flat-square&logo=razorpay&logoColor=3395FF) |
| 🖼️ **Media** | Cloudinary | ![Cloudinary](https://img.shields.io/badge/Cloudinary-3448C5?style=flat-square&logo=cloudinary&logoColor=white) |
| 🗄️ **Database** | PostgreSQL | ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=flat-square&logo=postgresql&logoColor=white) |
| 🔔 **Notifications** | Email + In-app | ![Email](https://img.shields.io/badge/Email-D14836?style=flat-square&logo=gmail&logoColor=white) |
| 🔄 **CI/CD** | Backend Pipeline | ![CI](https://img.shields.io/badge/CI-passing-brightgreen?style=flat-square) |

---

## ⚙️ Setup

### 🖥️ Backend

1️⃣ Configure environment (Spring profile or `.env`):
```env
# Database
DB_URL=jdbc:postgresql://localhost:5432/farmretail
DB_USER=your_user
DB_PASSWORD=your_password

# Cloudinary
CLOUDINARY_URL=cloudinary://api_key:api_secret@cloud_name

# Razorpay
RAZORPAY_KEY=your_key
RAZORPAY_SECRET=your_secret

# JWT
JWT_SECRET=your_secret_key
JWT_EXPIRY=86400000

# Mail
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USER=your_email
MAIL_PASSWORD=your_app_password
```

2️⃣ Start PostgreSQL locally or point to managed instance  
3️⃣ Run API:
```bash
./mvnw spring-boot:run
```

### 🎨 Frontend

1️⃣ Set environment variables:
```env
REACT_APP_API_BASE=http://localhost:8080/api
REACT_APP_RAZORPAY_KEY=your_publishable_key
```

2️⃣ Install dependencies:
```bash
npm install
```

3️⃣ Start dev server:
```bash
npm start
```

---

## 🔒 Security

| Aspect | Implementation |
|--------|----------------|
| 🔑 **Authentication** | JWT tokens with expiry/refresh; strong secrets; HTTPS in production |
| ✅ **Payment Validation** | Razorpay signature verification on callbacks and server-side captures |
| 🛡️ **Upload Security** | Sanitize uploads before Cloudinary; presets for size/type limits |
| 👥 **Authorization** | Role-based access control on every protected endpoint |

---

<p align="center">
  <b>🎓 Developed during Infosys Virtual Internship</b>
</p>

<p align="center">
  ⭐ Star this repo if you found it helpful!
</p>
