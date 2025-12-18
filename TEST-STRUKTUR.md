# Test-struktur Perfect8

## Testtyper

### Unit-tester (CI)
Körs automatiskt av GitHub Actions vid push/PR till main och dev.
Kräver INGEN extern server.

**Inkluderade:**
- AuthControllerTest
- HealthCheckTest
- BaseTest

**Kommando:**
```bash
mvn test -DfailIfNoTests=false \
  -Dtest='!*JwtAuthTest,!*LiveTest,!*CrudTest,!EmailSendLiveTest,!BlogPostCrudTest'
```

---

### Integration-tester (Manuella)
Körs manuellt mot live-server (p8.rantila.com).
Kräver att alla tjänster är igång och healthy.

**JWT Auth-tester:**
```bash
mvn test -pl :admin-service -Dtest=AdminJwtAuthTest -DfailIfNoTests=false
mvn test -pl :blog-service -Dtest=BlogJwtAuthTest -DfailIfNoTests=false
mvn test -pl :email-service -Dtest=EmailJwtAuthTest -DfailIfNoTests=false
mvn test -pl :image-service -Dtest=ImageJwtAuthTest -DfailIfNoTests=false
mvn test -pl :shop-service -Dtest=ShopJwtAuthTest -DfailIfNoTests=false
```

**Funktionalitetstester:**
```bash
mvn test -pl :blog-service -Dtest=BlogPostCrudTest -DfailIfNoTests=false
mvn test -pl :email-service -Dtest=EmailSendLiveTest -DfailIfNoTests=false
mvn test -pl :image-service -Dtest=ImageControllerTest -DfailIfNoTests=false
```

---

## Körordning

### 1. Bygg och deploy (Server)
```bash
cd ~/backend
git pull origin dev
mvn clean package -DskipTests
bash copy-jars*.sh
docker compose down
docker compose build
docker compose up -d
```

### 2. Vänta på healthy
```bash
sleep 60
docker compose ps -a
```

### 3. Kör integration-tester (Windows eller Server)
```bash
cd C:\_Perfect8\backend   # Windows
# eller
cd ~/backend              # Server

# JWT-tester först
mvn test -pl :admin-service -Dtest=AdminJwtAuthTest -DfailIfNoTests=false
mvn test -pl :blog-service -Dtest=BlogJwtAuthTest -DfailIfNoTests=false
mvn test -pl :email-service -Dtest=EmailJwtAuthTest -DfailIfNoTests=false
mvn test -pl :image-service -Dtest=ImageJwtAuthTest -DfailIfNoTests=false
mvn test -pl :shop-service -Dtest=ShopJwtAuthTest -DfailIfNoTests=false

# Sedan funktionalitetstester
mvn test -pl :blog-service -Dtest=BlogPostCrudTest -DfailIfNoTests=false
mvn test -pl :image-service -Dtest=ImageControllerTest -DfailIfNoTests=false
```

### 4. Kör ALLA tester för en tjänst
```bash
mvn test -pl :admin-service -DfailIfNoTests=false
```

---

## Testfiler per tjänst

| Tjänst | Unit (CI) | Integration (Manuell) |
|--------|-----------|----------------------|
| admin-service | AuthControllerTest, BaseTest, HealthCheckTest | AdminJwtAuthTest |
| blog-service | - | BlogJwtAuthTest, BlogPostCrudTest |
| email-service | - | EmailJwtAuthTest, EmailSendLiveTest |
| image-service | - | ImageJwtAuthTest, ImageControllerTest |
| shop-service | - | ShopJwtAuthTest |

---

## Namnkonvention
- `*Test.java` - Unit-tester (körs i CI om ej explicit exkluderade)
- `*JwtAuthTest.java` - JWT integration-tester (exkluderas från CI)
- `*LiveTest.java` - Live-tester mot extern server (exkluderas från CI)
- `*CrudTest.java` - CRUD-tester mot extern server (exkluderas från CI)
