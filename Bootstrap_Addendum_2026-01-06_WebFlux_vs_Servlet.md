# Bootstrap Addendum 2026-01-06 - WebFlux vs Servlet Security

## Session Sammanfattning

**Datum:** 2026-01-06  
**Branch:** scg  
**Fokus:** Fixa 401-fel och shop-service krasch  
**Status:** Shop-service SecurityConfig måste bytas från WebFlux till Servlet

---

## Utfört Arbete

### 1. CORS-fix i API Gateway ✅

**Problem:** `allowedOrigins("*")` + `allowCredentials(true)` = konflikt

**Lösning:** Specificerade origins istället för wildcard:
```java
configuration.setAllowedOrigins(List.of(
    "http://localhost:3000",
    "http://localhost:8080",      // Flutter web debug
    "http://127.0.0.1:8080",      // Flutter web (IPv4)
    "http://p8.rantila.com",
    "https://p8.rantila.com"
));
configuration.setAllowCredentials(true);
```

### 2. JWT Gateway Validering ✅

Gateway validerar JWT korrekt:
```
JWT validated - user: cmagnusb@yahoo.se, userId: 12, roles: ROLE_ADMIN,ROLE_USER
```

### 3. Shop-service Krasch ❌ → 🔧

**Symptom:** `NoClassDefFoundError: WebFluxConfigurer`

**Rotorsak:** shop-service SecurityConfig.java använde WebFlux-annotationer:
```java
// FEL - WebFlux (för reaktiva appar som Gateway)
@EnableWebFluxSecurity
public class SecurityConfig {
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {...}
}
```

**Lösning:** Byt till Servlet Security:
```java
// RÄTT - Servlet (för vanliga Spring MVC appar)
@EnableWebSecurity
public class SecurityConfig {
    public SecurityFilterChain filterChain(HttpSecurity http) {...}
}
```

---

## Tekniska Insikter

### WebFlux vs Servlet Security

| Aspekt | WebFlux (Reaktiv) | Servlet (Traditionell) |
|--------|-------------------|------------------------|
| Annotation | `@EnableWebFluxSecurity` | `@EnableWebSecurity` |
| HTTP Security | `ServerHttpSecurity` | `HttpSecurity` |
| Filter Chain | `SecurityWebFilterChain` | `SecurityFilterChain` |
| Används av | api-gateway | admin, blog, email, image, shop |
| Dependency | spring-boot-starter-webflux | spring-boot-starter-web |

### Arkitektur

```
Browser → API Gateway (WebFlux) → Services (Servlet)
              ↓
         CORS här
         JWT validering här
              ↓
         X-Auth-User header
         X-Auth-Roles header
         X-User-Id header
```

### CORS-princip

- **Gateway:** Hanterar CORS för alla externa requests
- **Services:** Ingen CORS behövs - får bara intern trafik från Gateway

---

## Fallgropar Upptäckta

### 1. Fel SecurityConfig-typ
- Shop-service hade `@EnableWebFluxSecurity` men är en Servlet-app
- Spring försöker ladda WebFlux-klasser som inte finns i classpath
- Resultat: `NoClassDefFoundError`

### 2. Exclude räcker inte
- `exclude = {ReactiveSecurityAutoConfiguration.class}` i Application
- Fungerar INTE om SecurityConfig explicit använder `@EnableWebFluxSecurity`
- SecurityConfig laddas innan exclude appliceras

### 3. Fel mapp i projekt
- shop-service hade en `gateway`-mapp med fel SecurityConfig
- Togs bort men JAR:en hade gammal kod cached
- Lösning: `mvn clean` + rebuild

---

## Status Efter Session

| Service | Status | Anteckning |
|---------|--------|------------|
| api-gateway | ✅ Created | Väntar på shop-service |
| admin-service | ✅ Healthy | |
| blog-service | ✅ Healthy | |
| email-service | ✅ Healthy | |
| image-service | ✅ Healthy | |
| shop-service | ❌ Restarting | Väntar på SecurityConfig-fix |

---

## Kvarstående

### Omedelbart
1. Byt shop-service SecurityConfig från WebFlux till Servlet
2. Push → pull → rebuild → verify healthy

### Efter fix
1. Testa Flutter login
2. Verifiera cart-endpoint returnerar data
3. Testa hela flödet

---

## Testdata

```
Email: cmagnusb@yahoo.se
userId: 12
Roller: ROLE_ADMIN, ROLE_USER
```

---

**Author:** Claude & Magnus  
**Date:** 2026-01-06  
**Session Length:** ~2 timmar
