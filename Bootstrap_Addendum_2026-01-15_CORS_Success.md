# Bootstrap Addendum 2026-01-14: CORS Fix & Shop Endpoints

## Session: 2026-01-14 eftermiddag + 2026-01-15 förmiddag
## Branch: sop
## Status: ✅ FUNGERAR

---

## RESULTAT

| Test | Status |
|------|--------|
| Login via Gateway | ✅ 200 |
| JWT-validering | ✅ Fungerar |
| CORS | ✅ Ingen 500 |
| `/shop/api/cart/` | ✅ 200 |
| `/shop/api/customers/profile` | ✅ 200 |

---

## PROBLEM 1: CORS 500-fel (LÖST)

**Orsak:** Konflikt mellan WebConfig och `@CrossOrigin` på controllers.

**Lösning:** Tog bort `@CrossOrigin` från alla shop-controllers. CORS hanteras endast av WebConfig.

### Ändrade filer
| Fil | Ändring |
|-----|---------|
| CartController.java | Borttagen `@CrossOrigin`, fixad `@GetMapping({"", "/"})` |
| ProductController.java | Borttagen `@CrossOrigin` |
| PaymentController.java | Borttagen `@CrossOrigin` |
| CategoryController.java | Borttagen `@CrossOrigin` |
| CustomerController.java | Borttagen `@CrossOrigin` |
| CustomerService.java | Tillagd `searchCustomers()`, fix `getPhone()` |

---

## PROBLEM 2: Customer not found (LÖST)

**Orsak:** `cmb@p8.se` fanns i adminDB.users men inte i shopDB.customers.

**Lösning:** 
```sql
INSERT INTO shopDB.customers (email, first_name, last_name, user_id, active, 
  email_verified, newsletter_subscribed, marketing_consent, created_date, updated_date) 
VALUES ('cmb@p8.se', 'Magnus', 'Berglund', 1, 1, 1, 0, 0, NOW(), NOW());
```

---

## IDENTIFIERAD TEKNISK SKULD (v2.0)

### 1. Redundant ID-struktur
- Customer har både `customerId` och `userId`
- Ursprunglig plan: varje mikroservice egen auth
- Nu: admin-service hanterar all auth
- **Åtgärd v2.0:** Ta bort `customerId`, använd `userId` som PK

### 2. Inkonsekvent telefon-namngivning
| Plats | Fält |
|-------|------|
| Customer.java | `phone` |
| Address.java | `phoneNumber` |
| shopDB.customers | `phone` |
| shopDB.addresses | `phone_number` |

**Åtgärd v2.0:** Standardisera på `phone`

### 3. Synkronisering adminDB ↔ shopDB
- Users skapas i adminDB
- Customers måste manuellt skapas i shopDB
- **Åtgärd v2.0:** Automatisk synk vid user-registrering

---

## TESTKOMMANDON

```bash
# Login och få token
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"cmb@p8.se","password":"magnus123"}' | jq -r '.accessToken')

# Testa cart
curl http://localhost:8080/shop/api/cart/ -H "Authorization: Bearer $TOKEN"

# Testa profile
curl http://localhost:8080/shop/api/customers/profile -H "Authorization: Bearer $TOKEN"
```

---

## NÄSTA STEG

1. Testa Flutter mot fungerande endpoints
2. Testa fler endpoints (products, categories, orders)
3. v2.0: Refactoring av ID-struktur
