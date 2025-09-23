# Perfect8 - Claude Instruktioner

## KRITISKA REGLER
1. **EN Java-fil per artifact** - Användaren har ADHD
2. **Inga redigeringar** - Alltid skapa helt nya filer
3. **Läsbara variabelnamn** - `orderId` inte `id`
4. **Less Strings, More Objects** - Använd starkt typade objekt
5. **Version 1.0 fokus** - Ingen Analytics, Metrics, Performance, Coupons
6. **IntelliJ Community** - Ingen Thymeleaf/Swagger

## PROJEKT SPECIFIKATIONER

### Stack
- **Spring Boot**: 3.2.12
- **Java**: 17
- **MySQL**: 8.0.33 (com.mysql:mysql-connector-j)
- **JWT**: io.jsonwebtoken:jjwt-* 0.11.5
- **Lombok**: För @Slf4j, @Data, @Builder
- **Maven**: Build-verktyg
- **GitHub**: https://github.com/Perfect8-v1/backend

### Modul-ordning (VIKTIGT!)
1. common (alla andra beror på denna)
2. admin-service (authentication/JWT)
3. blog-service
4. email-service
5. image-service
6. shop-service

### Kompilerings-status:
- ✅ common - Komplett
- ✅ admin-service - Komplett
- ✅ blog-service - Komplett
- ✅ email-service - Komplett
- 🔄 image-service - 90% klar, saknar Controller
- ⏸️ shop-service - Väntar på image-service

## TEKNISKA PRINCIPER

### Annotations
```java
@Slf4j              // Logging (Lombok)
@Builder            // DTOs (Lombok)
@Entity             // JPA entities
@Enumerated(EnumType.STRING)  // För enums i JPA
// Använd jakarta.validation.* (INTE javax!)
```

### Common Module
- **Enums**: Role, OrderStatus (delas mellan services)
- **Ingen komplex logik** i common
- Alla services har dependency till common

### Databas
- VARCHAR för enum-värden (inte MySQL ENUM)
- Roller normaliserade till UPPERCASE
- Indexering → Version 2.0

## VERSION 1.0 vs 2.0

### Version 1.0 (NU)
✅ Grundläggande e-handel och blogg
✅ Gmail för email (ingen egen loggning)
✅ Enkel orderhantering
✅ Kärnfunktionalitet

### Version 2.0 (FRAMTIDA)
⏸️ Analytics, Metrics, Performance
⏸️ Coupons, Dashboard
⏸️ Custom email-loggning
⏸️ Databas-indexering

## VANLIGA FEL OCH LÖSNINGAR

### Type Mismatches
- **String ↔ Enum**: `.name()` för enum→String, `Enum.valueOf()` för String→enum
- **BigDecimal ↔ Double**: `.doubleValue()` för BigDecimal→Double
- **LocalDateTime ↔ LocalDate**: `.toLocalDate()` eller `.atStartOfDay()`
- **List<String> ↔ String**: Kontrollera singular/plural

### Maven Kompilering
```bash
# Rekommenderat - visa bara fel
mvn clean compile -q

# Fortsätt från misslyckad modul
mvn clean compile -q -rf :email-service

# Kompilera image-service med dependencies
mvn clean install -pl image-service -am
```

## INTERAKTIONS-REGLER

### Vid varje session
1. Be om nytt addendum till Bootstrap.docx eller CLAUDE_INSTRUCTIONS.md
2. Presentera EN Java-fil åt gången
3. Ge max 2 alternativ att fortsätta
4. Vänta på svar innan frågor
5. Ge mig en färdig fil, aldrig instruktioner om hur jag skall redigera en fil.
    Stor källa till fel.

### Kodstil
- **Inga impl-klasser** - En implementation per funktionalitet
- **Undvik inner classes** - Skapar kompileringsproblem
- **Undvik hjälpmetoder** om möjligt
- **Inga stub-filer** - Gör den korrekta koden direkt

## STATUS (2025-09-19)
✅ Common Module
✅ Admin Service  
✅ Blog Service
✅ Email Service (Session 10 komplett)
🔄 Image Service (90% klar - Session 11)
⏸️ Shop Service (nästa)

## AKTUELLT FOKUS
- Shop-service kompileringsfel
- Flutter frontend för portfolio
- Deadline: Version 1.0 december 2025

## IMAGE SERVICE ARKITEKTUR (Session 11)
### Komponenter skapade:
- **Enums**: ImageStatus, ImageSize (med Lombok)
- **Entity**: Image (med alla URL-fält för olika storlekar)
- **Repository**: ImageRepository (med smart queries)
- **Service**: ImageService, ImageProcessingService
- **Exceptions**: ImageNotFoundException, InvalidImageException, ImageProcessingException, StorageException
- **DTO**: ImageDto (med nested DTOs)
- **Mapper**: ImageMapper (manuell, ingen MapStruct)
- **GlobalExceptionHandler**: Med Lombok och alla exceptions

### Bildbearbetning:
- **Thumbnailator** för bildhantering (0.4.20)
- **4 storlekar**: THUMBNAIL (150x150), SMALL (400x400), MEDIUM (800x800), LARGE (1600x1600)
- **Backend skapar alla storlekar** från ett original
- **Formats**: JPG, PNG, WEBP, BMP

### Konfiguration:
- **Port**: 8084
- **Database**: perfect8_images
- **Max filstorlek**: 10MB
- **Upload directory**: uploads/images

## REMEMBER
- Användaren Jonatan har ADHD - kort arbetsminne
- EN uppgift i taget
- Fokus på kärnfunktionalitet
- Behåll objektorienterad approach
- Be om addendum vid sessionsstart
- **Session 11**: Image Service arkitektur med Thumbnailator