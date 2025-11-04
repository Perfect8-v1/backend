# Universal Bootstrap Template - Magnum Opus
**För ADHD-anpassad Java-utveckling med Spring Boot**

---

## 🧠 ADHD-ANPASSADE ARBETSREGLER

### Grundprinciper
1. **EN Java-fil per artifact** - Måste spara innan fokus skiftar
2. **Inga redigeringar** - Alltid kompletta nya filer (eliminerar felkälla)
3. **Max 2 alternativ** - För många val skapar beslutsvånda
4. **Vänta på svar** - En fråga åt gången
5. **Kort arbetsminne** - Allt måste vara explicit och tydligt

### Kommunikationsregler med AI-assistent
- Ge EN fil åt gången
- Ställ max EN fråga per output
- Vänta på bekräftelse innan nästa steg
- Inga långa förklaringar - kort och konkret
- Två alternativ att fortsätta räcker

---

## 🏗️ ARKITEKTURPRINCIPER

### Kodprinciper
1. **Läsbara variabelnamn** - `orderId` inte `id`
2. **Less Strings, More Objects** - Enum över String
3. **Ingen bakåtkompabilitet** - Bygg rätt från början
4. **Inga alias-metoder** - En metod, ett namn
5. **Inga stub-filer** - Fullständig implementation direkt
6. **Version 1.0 fokus** - Kärnfunktionalitet först

### Namnkonventioner
- **Entity-fält**: beskrivande namn (`customerId`, inte `id`)
- **Boolean primitiv**: `isActive()` (Lombok-genererat)
- **Boolean wrapper**: `getActive()` (för nullable)
- **Repository**: `findByOrderId()` (inte `findByOrder_OrderId()`)
- **DTO = Entity**: Samma fältnamn överallt (ingen mappning!)

---

## 📁 PROJEKTSTRUKTUR

### Multi-modul Maven-struktur
```
projekt-namn/
├── pom.xml                 # Parent POM (artifactId: projekt-parent)
├── common/                 # Delade resurser
│   ├── pom.xml
│   └── src/main/java/
│       └── com/företag/common/
│           ├── enums/      # Delade enums
│           ├── utils/      # Verktyg
│           └── constants/  # Konstanter
├── service-1/              # Microservice 1
│   ├── pom.xml
│   └── src/main/java/
│       └── com/företag/service1/
│           ├── entity/     # JPA entities
│           ├── repository/ # Data access
│           ├── service/    # Business logic
│           ├── dto/        # Data Transfer Objects
│           ├── controller/ # REST endpoints
│           ├── exception/  # Custom exceptions
│           ├── security/   # Auth/Authorization
│           └── config/     # Configuration
└── service-2/              # Microservice 2
    └── [samma struktur]
```

---

## 🔨 BYGGORDNING (När vi skapar ny funktionalitet)

1. **enums** - Värdeuppsättningar (Role, Status, PaymentMethod)
2. **entity** - Datamodell med JPA-annoteringar
3. **repository** - JpaRepository interfaces
4. **service** - Affärslogik och transaktioner
5. **dto** - API-kontrakt (SAMMA namn som entity!)
6. **controller** - REST endpoints
7. **exceptions** - Domänspecifika undantag
8. **security** - Autentisering/auktorisering
9. **config** - Övrig konfiguration

---

## 🔧 FELSÖKNINGSORDNING (När vi fixar problem)

1. **config** - Miljöfel, felaktiga properties
2. **security** - JWT, CORS, filterkedja
3. **entity** - Mappning, relationer
4. **repository** - Query-fel, N+1 problem
5. **service** - Affärslogik, transaktioner
6. **dto/mapping** - Konverteringsfel
7. **exceptions** - Felhantering
8. **controllers** - HTTP-lager

---

## 💻 TEKNISK STACK (Rekommenderad)

### Core
- **Spring Boot**: 3.2.x eller senaste LTS
- **Java**: 17 eller 21 (LTS-versioner)
- **Maven**: Multi-modul projekt
- **Database**: MySQL 8.0 / PostgreSQL 15

### Dependencies
```xml
<!-- JWT -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>

<!-- Lombok -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
</dependency>

<!-- Validation -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

### Verktyg-begränsningar
- **IntelliJ Community** - Ingen Thymeleaf/Ultimate features
- **Ingen Swagger UI** - Använd Spring REST Docs eller manuell dokumentation
- **Ingen kod-generering** - Skriv explicit kod

---

## 🐳 DEPLOYMENT

### Docker-struktur
```yaml
version: '3.8'
services:
  database:
    image: mysql:8.0.33
    environment:
      MYSQL_ROOT_PASSWORD: ${DB_PASSWORD}
    volumes:
      - db_data:/var/lib/mysql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      
  service-1:
    build: ./service-1
    depends_on:
      database:
        condition: service_healthy
    environment:
      SPRING_PROFILES_ACTIVE: docker
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      
volumes:
  db_data:
```

### Deployment-principer
- Health checks på alla services
- Restart policies
- Volume mounts för persistens
- Environment-specifika .env filer
- Docker Registry för images
- Enkel backup med Filezilla

---

## 📊 VERSION MANAGEMENT

### Version 1.0 (MVP)
- Kärnfunktionalitet endast
- Grundläggande CRUD
- Enkel autentisering
- Minimal UI
- En betalningsmetod
- Grundläggande email

### Version 2.0 (Utökad)
- Analytics & Metrics
- Avancerad rapportering
- Flera betalningsmetoder
- Dashboard
- Performance monitoring
- Coupons & kampanjer

---

## 🛠️ MAVEN-KOMMANDON

```bash
# Kompilera utan tester, visa bara fel
mvn clean compile -q

# Kompilera specifik modul
mvn clean compile -pl :modul-namn -am -q

# Installera lokalt
mvn clean install -DskipTests

# Kör specifik service
mvn spring-boot:run -pl :service-namn

# Docker build
docker build --target service-namn -t projekt/service-namn .
```

---

## ✅ CHECKLISTA FÖR NY FUNKTIONALITET

- [ ] Enum definierad (om status/typ behövs)
- [ ] Entity skapad med rätt fältnamn
- [ ] Repository interface med Spring Data metoder
- [ ] Service med affärslogik
- [ ] DTO med SAMMA fältnamn som entity
- [ ] Controller med REST endpoints
- [ ] Exception classes för domänfel
- [ ] Security konfiguration uppdaterad
- [ ] Maven kompilerar utan fel
- [ ] Docker-image bygger

---

## 🚫 VANLIGA FALLGROPAR ATT UNDVIKA

### Namngivning
- ❌ `getId()` när fältet heter `customerId`
- ❌ `getProductName()` när fältet heter `name`
- ❌ Olika namn i DTO och Entity
- ✅ Använd EXAKT samma namn överallt

### Metoder
- ❌ Alias-metoder för bakåtkompabilitet
- ❌ Hjälpmetoder som "översätter" mellan namn
- ❌ String-baserade ID:n
- ✅ En metod, ett namn, rätt från början

### Dependencies
- ❌ Onödiga repositories (Spring Milestones när allt är stabilt)
- ❌ För många bibliotek
- ✅ Minimalt med dependencies, lägg till när behov uppstår

---

## 📝 INSTRUKTION FÖR FORTSÄTTNING

**Vid varje ny chat-session:**
1. Be om senaste Bootstrap-addendum
2. Kolla projektets kompileringsstatus
3. Identifiera nästa steg
4. EN fil åt gången
5. Kompilera efter varje ändring

**Kom ihåg att fråga efter:**
- "Kan du skriva ett addendum för denna session?"
- "Vad är nästa prioritet?"
- "Kompilerar projektet nu?"

---

## 🎯 SLUTORD

Denna Bootstrap är designad för:
- **ADHD-vänlig utveckling** - En sak i taget
- **Konsistent kodkvalitet** - Samma principer överallt
- **Snabb felsökning** - Tydlig ordning och struktur
- **Skalbar arkitektur** - Från MVP till fullskalig produkt

**Lycka till med ditt projekt!**

*Version: 1.0 - Universal Bootstrap Template*
*Skapad för utvecklare med ADHD av Magnus*