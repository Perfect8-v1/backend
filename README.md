# Perfect8 - Modern E-commerce & Blog Platform

[![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)](https://github.com/Perfect8-v1/backend)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)
[![Status](https://img.shields.io/badge/status-Production%20Ready-success.svg)](https://github.com/Perfect8-v1/backend)

> A production-ready microservices backend for e-commerce and blogging, built with Spring Boot 3.4.1 and Java 21.

**Live Demo:** [p8.rantila.com](http://p8.rantila.com)  
**Author:** C Magnus Berglund and Jonatan Håkansson
**Portfolio Project:** ✅ Production Ready

---

## 🎯 Project Overview

Perfect8 is a full-stack e-commerce and blog platform built with modern microservices architecture. The backend consists of 5 independent services, each with its own database, containerized with Docker, and deployed to production.

**Key Highlights:**
- ✅ 5 microservices running in production
- ✅ JWT authentication & authorization
- ✅ RESTful API design
- ✅ MySQL database per service
- ✅ Docker containerization
- ✅ Health monitoring
- ✅ PayPal payment integration
- ✅ Email notification system
- ✅ Image upload & processing

---

## 🏗️ Architecture

### Microservices Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         Perfect8 Platform                       │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐           │
│  │   Admin      │  │    Blog      │  │    Shop      │           │
│  │   Service    │  │   Service    │  │   Service    │           │
│  │   :8081      │  │   :8082      │  │   :8085      │           │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘           │
│         │                 │                 │                   │
│  ┌──────▼──────┐   ┌──────▼──────┐   ┌──────▼──────┐            │
│  │   adminDB   │   │   blogDB    │   │   shopDB    │            │
│  └─────────────┘   └─────────────┘   └─────────────┘            │
│                                                                 │
│  ┌──────────────┐  ┌──────────────┐                             │
│  │   Email      │  │   Image      │                             │
│  │   Service    │  │   Service    │                             │
│  │   :8083      │  │   :8084      │                             │
│  └──────┬───────┘  └──────┬───────┘                             │
│         │                  │                                    │
│  ┌──────▼──────┐   ┌──────▼──────┐                              │
│  │  emailDB    │   │  imageDB    │                              │
│  └─────────────┘   └─────────────┘                              │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### Services & Responsibilities

| Service | Port | Database | Purpose |
|---------|------|----------|---------|
| **admin-service** | 8081 | adminDB | Authentication, authorization, admin operations |
| **blog-service** | 8082 | blogDB | Blog posts, content management |
| **email-service** | 8083 | emailDB | Email notifications, templates |
| **image-service** | 8084 | imageDB | Image upload, processing, storage |
| **shop-service** | 8085 | shopDB | Products, orders, payments, inventory |

---

## 🚀 Tech Stack

### Backend
- **Java 21** (LTS)
- **Spring Boot 3.4.1**
- **Spring Security** with JWT (jjwt 0.12.3)
- **Spring Data JPA** with Hibernate
- **MySQL 8.0** (separate database per service)
- **Maven** (multi-module project)
- **Lombok** (reduce boilerplate)

### DevOps
- **Docker & Docker Compose**
- **Ubuntu Server 24.04 LTS**
- **Nginx** (reverse proxy)
- **Git** (version control)

### Frontend (Planned - v1.2)
- **Flutter (Dart)** - Cross-platform UI
- **Platforms:** Web, iOS, Android, Windows

### Third-Party Integrations (Greater flexibility will come with Version 2.0)
- **PayPal SDK** - Payment processing
- **Gmail SMTP** - Email delivery
- **Thumbnailator** - Image processing

---

## ✨ Features

### Version 1.0 (Current - Production Ready)

#### 🔐 Authentication & Authorization
- JWT-based authentication
- Role-based access control (RBAC)
- Admin & Customer user types
- Password hashing (BCrypt)
- Token expiration & refresh

#### 🛒 E-commerce
- Product catalog with categories
- Shopping cart (guest & logged-in users)
- Order management
- Inventory tracking
- Payment processing (PayPal)
- Shipment tracking
- Customer address management

#### 📝 Blog & Content
- Create, read, update, delete blog posts
- Image integration
- Publish/unpublish functionality
- Slug-based URLs
- View count tracking

#### 📧 Email System
- 7 HTML email templates
- Order confirmations
- Shipping notifications
- Welcome emails
- Password reset
- Newsletter support

#### 🖼️ Image Management
- Image upload (JPG, PNG, GIF, WEBP)
- Automatic thumbnail generation (4 sizes)
- Image optimization
- Metadata storage

#### 🏥 Health & Monitoring
- Health check endpoints (`/actuator/health`)
- Database connection validation
- Service status monitoring
- Uptime tracking

---

## 📦 Installation & Setup

### Prerequisites
- **Java 21** ([Download](https://openjdk.org/))
- **Maven 3.9+** ([Download](https://maven.apache.org/))
- **Docker & Docker Compose** ([Download](https://www.docker.com/))
- **Git** ([Download](https://git-scm.com/))

### Clone Repository
```bash
git clone https://github.com/Perfect8-v1/backend.git
cd backend
```

### Environment Variables
Create a `.env` file in the root directory:

```env
# Database
DB_ROOT_PASSWORD=your_root_password
DB_USER=perfect8user
DB_PASSWORD=your_database_password

# JPA
JPA_DDL_AUTO=validate

# JWT
JWT_SECRET=your_jwt_secret_key_minimum_256_bits

# Email (Gmail SMTP)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_app_password
MAIL_PROTOCOL=smtp

# PayPal
PAYPAL_CLIENT_ID=your_paypal_client_id
PAYPAL_CLIENT_SECRET=your_paypal_client_secret
```

### Build & Run with Docker

```bash
# Build all services
mvn clean package -DskipTests

# Copy JAR files to Docker contexts
bash copy-jars.sh

# Start all services with Docker Compose
docker compose up -d

# Check service status
docker compose ps

# View logs
docker compose logs -f
```

### Verify Installation

All services should be **healthy** after 2-3 minutes:

```bash
# Check health endpoints
curl http://localhost:8081/actuator/health  # admin-service
curl http://localhost:8082/actuator/health  # blog-service
curl http://localhost:8083/actuator/health  # email-service
curl http://localhost:8084/actuator/health  # image-service
curl http://localhost:8085/actuator/health  # shop-service
```

Expected response:
```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" }
  }
}
```

---

## 🔌 API Documentation

### Base URLs (Production)
- **Admin:** `http://p8.rantila.com:8081`
- **Blog:** `http://p8.rantila.com:8082`
- **Email:** `http://p8.rantila.com:8083`
- **Image:** `http://p8.rantila.com:8084`
- **Shop:** `http://p8.rantila.com:8085`

### Authentication

#### Admin Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "your_password"
}

Response:
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "role": "ROLE_ADMIN"
}
```

#### Customer Registration
```http
POST /api/v1/customers/register
Content-Type: application/json

{
  "email": "customer@example.com",
  "password": "securePassword123",
  "firstName": "John",
  "lastName": "Doe"
}
```

### Shop API Examples

#### Get Products
```http
GET /api/v1/products
Authorization: Bearer <token>

Response:
[
  {
    "productId": 1,
    "name": "Product Name",
    "price": 299.99,
    "stockQuantity": 50,
    "imageId": 1
  }
]
```

#### Add to Cart
```http
POST /api/v1/cart/add
Authorization: Bearer <token>
Content-Type: application/json

{
  "productId": 1,
  "quantity": 2
}
```

#### Create Order
```http
POST /api/v1/orders/create
Authorization: Bearer <token>
Content-Type: application/json

{
  "shippingAddressId": 1,
  "billingAddressId": 1,
  "paymentMethod": "PAYPAL"
}
```

### Blog API Examples

#### Get Posts
```http
GET /api/posts
Response:
[
  {
    "postId": 1,
    "title": "Blog Post Title",
    "slug": "blog-post-title",
    "publishedDate": "2025-11-14T10:00:00",
    "viewCount": 42
  }
]
```

#### Create Post (Admin)
```http
POST /api/posts
Authorization: Bearer <admin_token>
Content-Type: application/json

{
  "title": "New Blog Post",
  "content": "Post content here...",
  "slug": "new-blog-post"
}
```

---

## 🗃️ Database Schema

Each service has its own MySQL database with the following key entities:

### Admin Service
- `admin_users` - Admin user accounts

### Blog Service
- `users` - Blog user accounts
- `roles` - User roles
- `posts` - Blog posts
- `image_references` - Post images

### Shop Service
- `products` - Product catalog
- `categories` - Product categories
- `customers` - Customer accounts
- `addresses` - Customer addresses
- `carts` - Shopping carts
- `cart_items` - Cart contents
- `orders` - Customer orders
- `order_items` - Order details
- `payments` - Payment records
- `shipments` - Shipment tracking
- `inventory_transactions` - Stock movements

### Email Service
- `email_templates` - Email templates

### Image Service
- `images` - Image metadata & storage

---

## 🧪 Testing

### Manual API Testing

Use **Postman**, **Insomnia**, or **curl**:

```bash
# Test admin login
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# Test product listing
curl -X GET http://localhost:8085/api/v1/products \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Health Check Monitoring

```bash
# Check all services in one command
for port in 8081 8082 8083 8084 8085; do
  echo "Service on port $port:"
  curl -s http://localhost:$port/actuator/health | grep -o '"status":"[^"]*"'
done
```

Expected output:
```
Service on port 8081: "status":"UP"
Service on port 8082: "status":"UP"
Service on port 8083: "status":"UP"
Service on port 8084: "status":"UP"
Service on port 8085: "status":"UP"
```

---

## 📂 Project Structure

```
backend/
├── common/                      # Shared code (enums, utils)
│   └── src/main/java/com/perfect8/common/
├── admin-service/               # Authentication & admin
│   ├── src/main/java/com/perfect8/admin/
│   ├── Dockerfile
│   └── pom.xml
├── blog-service/                # Blog & content
│   ├── src/main/java/com/perfect8/blog/
│   ├── Dockerfile
│   └── pom.xml
├── email-service/               # Email notifications
│   ├── src/main/java/com/perfect8/email/
│   ├── src/main/resources/templates/  # HTML email templates
│   ├── Dockerfile
│   └── pom.xml
├── image-service/               # Image management
│   ├── src/main/java/com/perfect8/image/
│   ├── Dockerfile
│   └── pom.xml
├── shop-service/                # E-commerce core
│   ├── src/main/java/com/perfect8/shop/
│   ├── Dockerfile
│   └── pom.xml
├── SQL/                         # Database schemas
│   ├── admin-CREATE-TABLE.sql
│   ├── blog-CREATE-TABLE.sql
│   ├── email-CREATE-TABLE.sql
│   ├── image-CREATE-TABLE.sql
│   └── shop-CREATE-TABLE.sql
├── docker-compose.yml           # Service orchestration
├── copy-jars.sh                 # JAR deployment script
├── pom.xml                      # Parent POM
└── README.md                    # This file
```

---

## 🚢 Deployment

### Production Environment
- **Server:** Ubuntu Server 24.04 LTS
- **Domain:** p8.rantila.com
- **Containerization:** Docker & Docker Compose
- **Reverse Proxy:** Nginx
- **SSL/TLS:** (Planned)

### Deployment Workflow

```bash
# On server
cd ~/backend
git pull origin main

# Build JAR files
mvn clean package -DskipTests

# Copy JARs to Docker contexts
bash copy-jars.sh

# Rebuild Docker images
docker compose build

# Restart services
docker compose up -d

# Monitor logs
docker compose logs -f
```

---

## 📊 Development Status

### Version 1.0 ✅ (Current - Production Ready)
- [x] 5 microservices architecture
- [x] JWT authentication & authorization
- [x] Product catalog & categories
- [x] Shopping cart & checkout
- [x] Order management
- [x] PayPal payment integration
- [x] Blog post management
- [x] Email notification system
- [x] Image upload & processing
- [x] Docker containerization
- [x] Production deployment

### Version 1.1 📅 (Planned - Q1 2025)
- [ ] Unit testing (JUnit 5)
- [ ] Integration testing
- [ ] API documentation (OpenAPI/Swagger)
- [ ] English translation
- [ ] SSL/TLS certificates
- [ ] Automated CI/CD pipeline

### Version 1.2 📅 (Planned - Q2 2025)
- [ ] Flutter frontend (Web, iOS, Android, Windows)
- [ ] User dashboard
- [ ] Admin panel UI
- [ ] Product search & filters
- [ ] Order tracking UI
- [ ] Blog reader interface

### Version 2.0 🔮 (Future)
- [ ] Analytics & metrics dashboard
- [ ] Performance monitoring
- [ ] Advanced reporting
- [ ] Coupon & discount system
- [ ] Product reviews & ratings
- [ ] Wishlist functionality
- [ ] Recommendation engine
- [ ] Multiple payment methods (Stripe, etc.)

---

## 🤝 Contributing

This is a portfolio project, but feedback and suggestions are welcome!

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👤 Authors

**C Magnus Berglund and Jonathan Håkansson**

- **GitHub:** [@Perfect8-v1](https://github.com/Perfect8-v1)
- **LinkedIn:** [C Magnus Berglund](https://www.linkedin.com/in/cmagnusberglund/)
- **LinkedIn:** [Jonathan Håkansson](https://www.linkedin.com/in/jonathan-h%C3%A5kansson-a01618191/)
- **Email:** cmagnusb@gmail.com

---

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- Docker for containerization platform
- MySQL for reliable database
- PayPal for payment integration
- Open source community

---

## 📸 Screenshots

### Service Health Check
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "MySQL",
        "validationQuery": "isValid()"
      }
    },
    "healthController": {
      "status": "UP",
      "details": {
        "service": "shop-service",
        "port": "8085",
        "uptime": "16 hours",
        "database": "connected"
      }
    }
  }
}
```

---

## 🔗 Related Repositories

- **Backend:** [github.com/Perfect8-v1/backend](https://github.com/Perfect8-v1/backend) (this repo)
- **Frontend:** [github.com/Perfect8-v1/frontend](https://github.com/Perfect8-v1/frontend) (planned)
- **Documentation:** [github.com/Perfect8-v1/docs](https://github.com/Perfect8-v1/docs) (planned)

---

## 📈 Project Statistics

- **Lines of Code:** ~15,000
- **Services:** 5
- **Databases:** 5 (MySQL)
- **REST Endpoints:** 150+
- **JPA Entities:** 35+
- **Development Time:** 3 months
- **Status:** Production Ready ✅

---

<div align="center">

### ⭐ Star this repository if you find it helpful!

**Perfect8 - Built with Claude by C Magnus Berglund and Jonathan Håkansson**

</div>
