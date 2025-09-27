Bootstrap.docx - Addendum Session 10
2025-01-20

SESSION 10: API GATEWAY & FRONTEND INTEGRATION

🎉 MILESTONE: BUILD SUCCESS!
Hela backend kompilerar utan fel!

📋 VAD VI SKAPAT FÖR FRONTEND-INTEGRATION

✅ Nginx API Gateway (nginx.conf)
- Single entry point: http://localhost:8080
- Routing till alla microservices
- CORS headers för frontend
- Health check endpoint för monitoring
- API versionering (/api/v1/ för version 1.0)

✅ API Contract Documentation (API_CONTRACT_V1.md)
- Komplett endpoint-dokumentation
- Request/response exempel i JSON
- HTTP status codes
- Authentication med JWT Bearer tokens
- Pagination och sortering

✅ CORS Configuration (CorsConfig.java)
- Spring Boot CORS för varje service
- Tillåter localhost:3000, 5173, 4200, 8080
- Stöd för Authorization headers
- Credentials tillåtna

✅ Docker Compose (docker-compose.yml)
- Hela stacken i en fil
- MySQL + 5 services + Nginx
- Health checks på alla services
- Automatisk nätverkskonfiguration
- Volumes för persistens

✅ Start/Stop Scripts
- start-backend.sh - Bygger och startar allt
- stop-backend.sh - Stoppar och rensar
- Automatisk verifiering av services

🏗️ ARKITEKTUR FÖR FRONTEND

Frontend App (React/Vue/Angular)
         ↓
   Nginx Gateway (:8080)
         ↓
    /api/v1/*
    ↙    ↓    ↘
Admin  Shop  Blog  (etc)
(:8081)(:8085)(:8082)

📊 API ROUTING STRUKTUR

/api/v1/auth/*      → admin-service:8081
/api/v1/products/*  → shop-service:8085
/api/v1/posts/*     → blog-service:8082
/api/v1/cart/*      → shop-service:8085
/api/v1/orders/*    → shop-service:8085
/api/v1/images/*    → image-service:8084

🔐 AUTHENTICATION FLOW

1. POST /api/v1/auth/login
   → Får JWT token
2. Inkludera token i headers:
   Authorization: Bearer <token>
3. Token valideras av varje service
4. 401 om token saknas/ogiltig

📦 VERSION 1.0 SCOPE (Det som ÄR med)

✅ Authentication & User management
✅ Product catalog
✅ Shopping cart
✅ Order & checkout
✅ PayPal payment
✅ Blog system
✅ Image upload/serving
✅ Email notifications

❌ VERSION 2.0 (INTE med än)

- Analytics & Metrics
- Admin Dashboard
- Coupons & Discounts
- Product Reviews
- Wishlists
- Recommendations
- Performance monitoring

🚀 QUICK START FÖR FRONTEND-UTVECKLARE

1. Clone repo:
   git clone https://github.com/Perfect8-v1/backend.git

2. Starta backend:
   ./start-backend.sh

3. Vänta tills alla services är gröna

4. Testa API Gateway:
   curl http://localhost:8080/health

5. Börja använda APIer:
   Base URL: http://localhost:8080/api/v1

📝 EXEMPEL: Första API-anropet från Frontend

// Login
fetch('http://localhost:8080/api/v1/auth/login', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    email: 'user@example.com',
    password: 'password123'
  })
})
.then(res => res.json())
.then(data => {
  localStorage.setItem('token', data.token);
});

// Hämta produkter
fetch('http://localhost:8080/api/v1/products', {
  headers: {
    'Authorization': `Bearer ${localStorage.getItem('token')}`
  }
})
.then(res => res.json())
.then(products => console.log(products));

🔧 FELSÖKNING

Problem: CORS error i browser
Lösning: Kontrollera att frontend kör på tillåten port

Problem: 404 på API-anrop
Lösning: Kontrollera att du använder /api/v1/ prefix

Problem: 401 Unauthorized
Lösning: Kontrollera JWT token i Authorization header

Problem: Services startar inte
Lösning: Kör `docker-compose logs <service-name>`

📊 PORTAR ÖVERSIKT

8080 - API Gateway (Nginx)
8081 - Admin Service
8082 - Blog Service
8083 - Email Service
8084 - Image Service
8085 - Shop Service
3306 - MySQL Database

💡 NÄSTA STEG

1. Testa full stack med en enkel frontend
2. Implementera health checks i alla services
3. Sätta upp Uptime Kuma monitoring
4. Konfigurera production deployment

⚠️ SÄKERHET PÅMINNELSER

- Ändra default lösenord i docker-compose.yml
- Sätt JWT_SECRET som miljövariabel
- Använd HTTPS i produktion
- Begränsa CORS origins i produktion
- Aldrig committa .env filer

✅ REDO FÖR FRONTEND!
Backend är nu helt förberedd för frontend-integration.
Alla APIer är dokumenterade och tillgängliga via gateway.