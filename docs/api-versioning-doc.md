# API Versioning & Deprecation Policy
**För REST APIs i Spring Boot Microservices**

---

## 📋 GRUNDPRINCIPER

### Versioneringsstrategi
- **URL Path Versioning** - `/api/v1/`, `/api/v2/`
- **Ingen bakåtkompatibilitet inom major version** - Breaking changes = ny version
- **Tydlig deprecation** - Minst 3 månader varning
- **Max 2 aktiva versioner** - Gamla versioner fasas ut

### Versionsnummer (Semantic Versioning)
- **v1** - Major version (breaking changes)
- **v1.2** - Minor version (nya features, bakåtkompatibel)
- **v1.2.3** - Patch version (bugfixar, intern användning)

---

## 🏗️ IMPLEMENTATION I SPRING BOOT

### Controller-struktur
```java
package com.perfect8.shop.controller.v1;

@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Product API v1")
public class ProductControllerV1 {
    
    // Version 1.0 endpoints
    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }
}
```

### Parallella versioner
```java
// Version 1 - Original
@RestController
@RequestMapping("/api/v1/orders")
public class OrderControllerV1 {
    @PostMapping
    public OrderResponseV1 createOrder(@RequestBody OrderRequestV1 request) {
        // v1 logic: simple order
    }
}

// Version 2 - Utökad funktionalitet
@RestController
@RequestMapping("/api/v2/orders")
public class OrderControllerV2 {
    @PostMapping
    public OrderResponseV2 createOrder(@RequestBody OrderRequestV2 request) {
        // v2 logic: order with shipping options
    }
}
```

---

## 🔄 VERSIONSHANTERING

### DTOs per version
```java
// DTOs för v1
package com.perfect8.shop.dto.v1;

@Data
public class ProductDTOv1 {
    private Long productId;
    private String name;
    private BigDecimal price;
}

// DTOs för v2 (utökad)
package com.perfect8.shop.dto.v2;

@Data  
public class ProductDTOv2 {
    private Long productId;
    private String name;
    private BigDecimal price;
    private List<String> categories;  // Nytt i v2
    private Integer stockLevel;        // Nytt i v2
}
```

### Service-lager strategi
```java
@Service
public class ProductService {
    
    // Gemensam logik
    private Product findProductById(Long id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException());
    }
    
    // Version-specifik mappning
    public ProductDTOv1 getProductV1(Long id) {
        Product product = findProductById(id);
        return mapToV1DTO(product);
    }
    
    public ProductDTOv2 getProductV2(Long id) {
        Product product = findProductById(id);
        return mapToV2DTO(product);
    }
}
```

---

## ⚠️ DEPRECATION POLICY

### Deprecation Headers
```java
@RestController
@RequestMapping("/api/v1/legacy")
public class LegacyController {
    
    @GetMapping("/old-endpoint")
    public ResponseEntity<?> oldEndpoint(HttpServletResponse response) {
        // Lägg till deprecation headers
        response.addHeader("Sunset", "2025-12-31");
        response.addHeader("Deprecation", "true");
        response.addHeader("Link", "</api/v2/new-endpoint>; rel=\"successor-version\"");
        
        // Logga användning för monitoring
        log.warn("Deprecated endpoint called: /api/v1/legacy/old-endpoint");
        
        return ResponseEntity.ok(legacyService.getData());
    }
}
```

### Deprecation Response Wrapper
```java
@Data
public class DeprecatedResponse<T> {
    private T data;
    private String deprecationNotice = "This endpoint is deprecated and will be removed on 2025-12-31";
    private String migrationGuide = "Please use /api/v2/new-endpoint instead";
    private String documentationUrl = "https://api.perfect8.com/migration/v1-to-v2";
    
    public DeprecatedResponse(T data) {
        this.data = data;
    }
}
```

---

## 🌐 NGINX ROUTING

### API Gateway konfiguration
```nginx
# nginx.conf
upstream backend_v1 {
    server shop-service:8085;
}

upstream backend_v2 {
    server shop-service-v2:8086;  # Ny container för v2
}

server {
    listen 80;
    
    # Version 1 (deprecated)
    location /api/v1/ {
        add_header Sunset "2025-12-31" always;
        add_header Deprecation "true" always;
        proxy_pass http://backend_v1;
    }
    
    # Version 2 (current)
    location /api/v2/ {
        proxy_pass http://backend_v2;
    }
    
    # Default till senaste version
    location /api/ {
        return 301 /api/v2$request_uri;
    }
}
```

---

## 📊 VERSION LIFECYCLE

### Tidslinje för en API-version
```
v1.0 Launch     ──────> Stable (12 månader)
                        │
                        ├─> v2.0 Beta (3 månader överlapp)
                        │
v1.0 Deprecated ──────> Sunset Warning (3 månader)
                        │
v1.0 Sunset     ──────> Removed (returnerar 410 Gone)
```

### HTTP Status för lifecycle
- **200 OK** - Aktiv endpoint
- **301 Moved Permanently** - Permanent flytt till ny version
- **308 Permanent Redirect** - Behåll HTTP metod vid redirect
- **410 Gone** - Endpoint borttagen permanent

---

## 🔧 BREAKING CHANGES DEFINITION

### Vad kräver ny major version?
- ❌ Ta bort ett fält från response
- ❌ Ändra datatyp på existerande fält
- ❌ Ändra URL-struktur
- ❌ Ändra HTTP-metod
- ❌ Ändra authentication-metod
- ❌ Ta bort endpoint

### Vad är bakåtkompatibelt?
- ✅ Lägga till nytt optional fält
- ✅ Lägga till ny endpoint
- ✅ Lägga till ny optional query parameter
- ✅ Returnera extra data i response
- ✅ Acceptera nya optional fields i request

---

## 📝 API DOCUMENTATION

### Version-specifik dokumentation
```java
@Configuration
public class OpenApiConfig {
    
    @Bean
    public GroupedOpenApi v1Api() {
        return GroupedOpenApi.builder()
            .group("v1")
            .pathsToMatch("/api/v1/**")
            .addOpenApiCustomiser(openApi -> 
                openApi.info(new Info()
                    .title("Perfect8 API v1")
                    .version("1.0")
                    .deprecated(true)
                    .description("DEPRECATED - Will be removed 2025-12-31")))
            .build();
    }
    
    @Bean
    public GroupedOpenApi v2Api() {
        return GroupedOpenApi.builder()
            .group("v2")
            .pathsToMatch("/api/v2/**")
            .addOpenApiCustomiser(openApi -> 
                openApi.info(new Info()
                    .title("Perfect8 API v2")
                    .version("2.0")
                    .description("Current stable version")))
            .build();
    }
}
```

---

## 🐳 DOCKER DEPLOYMENT

### Multi-version deployment
```yaml
version: '3.8'
services:
  # Version 1 (legacy)
  shop-service-v1:
    image: perfect8/shop-service:1.0
    environment:
      API_VERSION: v1
      DEPRECATION_DATE: "2025-12-31"
    labels:
      - "traefik.http.routers.shop-v1.rule=PathPrefix(`/api/v1/`)"
  
  # Version 2 (current)
  shop-service-v2:
    image: perfect8/shop-service:2.0
    environment:
      API_VERSION: v2
    labels:
      - "traefik.http.routers.shop-v2.rule=PathPrefix(`/api/v2/`)"
```

---

## 📋 MIGRATION CHECKLIST

### När du skapar ny API-version:
- [ ] Skapa ny controller-klass med vX suffix
- [ ] Skapa nya DTOs i version-specifik package
- [ ] Uppdatera Nginx routing
- [ ] Dokumentera breaking changes
- [ ] Sätt deprecation headers på gamla versionen
- [ ] Uppdatera API-dokumentation
- [ ] Informera frontend-team
- [ ] Sätt sunset-datum (minst 3 månader)
- [ ] Skapa migration guide
- [ ] Deploy parallellt med gamla versionen

---

## 🚨 MONITORING & ALERTS

### Deprecation monitoring
```java
@Component
@Slf4j
public class DeprecationMonitor {
    
    private final MeterRegistry meterRegistry;
    
    public void logDeprecatedCall(String endpoint, String version) {
        // Metrics för monitoring
        meterRegistry.counter("api.deprecated.calls",
            "endpoint", endpoint,
            "version", version).increment();
            
        // Logging för alerts
        log.warn("Deprecated API call - Endpoint: {}, Version: {}", endpoint, version);
    }
}
```

### Alert-regler
- Varna när deprecated endpoints används > 100 ggr/dag
- Alert 1 månad före sunset
- Critical alert 1 vecka före sunset

---

## 📊 CLIENT COMMUNICATION

### Deprecation email template
```markdown
Subject: API Version 1.0 Deprecation Notice

Dear API User,

We are writing to inform you that API v1.0 will be deprecated on 2025-12-31.

**What you need to do:**
- Migrate to API v2.0 before 2025-12-31
- Review the migration guide: [link]
- Test your integration in our staging environment

**Key changes in v2.0:**
- [List breaking changes]

**Timeline:**
- 2025-09-30: v1.0 marked as deprecated
- 2025-12-31: v1.0 sunset (removed)

Best regards,
Perfect8 API Team
```

---

## ✅ BEST PRACTICES

1. **Aldrig ta bort utan varning** - Minst 3 månaders deprecation
2. **Tydlig kommunikation** - Headers, emails, dokumentation
3. **Parallell drift** - Kör gamla och nya versioner samtidigt
4. **Monitoring** - Spåra användning av deprecated endpoints
5. **Graceful degradation** - Ge tydliga felmeddelanden vid sunset
6. **Version i URL** - Alltid explicit versioning (/api/v1/)
7. **Max 2 versioner** - Fas ut gamla aggressivt

---

## 🔴 EMERGENCY DEPRECATION

För säkerhetsproblem (kortare deprecation):
```java
response.addHeader("Sunset", "2025-02-01");  // 30 dagar
response.addHeader("Deprecation", "true");
response.addHeader("Deprecation-Reason", "security");
response.addHeader("Link", "</api/v2/secure-endpoint>; rel=\"successor-version\"");
```

---

**Version:** 1.0
**Senast uppdaterad:** 2025-01-27
**Policy gäller från:** 2025-02-01