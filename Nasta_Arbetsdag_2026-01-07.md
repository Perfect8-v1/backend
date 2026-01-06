# Nästa Arbetsdag 2026-01-07 - Shop-service Fix

**Datum:** Nästa session  
**Branch:** scg  
**Status:** Shop-service väntar på SecurityConfig-fix  
**Prioritet:** HIGH - En fil kvar att fixa

---

## 🎯 Arbetsordning

### Steg 1: Verifiera SecurityConfig är pushad (2 min)

**På servern:**
```bash
cd ~/backend
git pull origin scg
cat ~/backend/shop-service/src/main/java/com/perfect8/shop/config/SecurityConfig.java
```

**Förväntat:** Ska innehålla `@EnableWebSecurity` (INTE `@EnableWebFluxSecurity`)

### Steg 2: Bygg om shop-service (3 min)

```bash
mvn clean package -DskipTests -pl :shop-service
bash copy-jars.sh
docker compose build --no-cache shop-service
docker compose up -d shop-service
```

### Steg 3: Verifiera (2 min)

```bash
# Vänta 60 sekunder
sleep 60
docker compose ps -a
```

**Förväntat:** shop-service = healthy

### Steg 4: Starta Gateway (1 min)

```bash
docker compose up -d api-gateway
docker compose ps -a
```

**Förväntat:** Alla services healthy

### Steg 5: Testa Flutter (5 min)

1. Starta Flutter-appen
2. Logga in
3. Verifiera att cart laddar utan 401

---

## 📋 Korrekt SecurityConfig för shop-service

```java
package com.perfect8.shop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )
                .build();
    }
}
```

---

## 💾 Snabbkommandon

### Om något går fel
```bash
docker compose logs shop-service --tail 50
```

### Full rebuild
```bash
bash new.sh
```

---

## ✅ Definition of Done

- [ ] shop-service healthy
- [ ] api-gateway healthy
- [ ] Flutter login fungerar
- [ ] Cart laddar utan 401
- [ ] Profile laddar utan 401

---

## 🎓 Kom Ihåg

- **WebFlux** = Gateway (reaktiv)
- **Servlet** = Alla andra services (traditionell)
- CORS behövs bara i Gateway
- En fil i taget, verifiera efter varje steg

---

*Skapad: 2026-01-06*  
*Branch: scg*  
*Prioritet: HIGH - En fix kvar*
