# Nästa Arbetsdag 2026-01-16: Flutter Testing & v2.0 Planering

## STATUS VID SESSIONENS SLUT

✅ Backend v1.0 fungerar:
- Login: OK
- JWT: OK  
- CORS: OK
- Cart endpoint: OK
- Profile endpoint: OK

---

## DEL 1: Testa Flutter mot Backend

### Steg 1: Verifiera backend körs

```bash
ssh magnus@p8.rantila.com
cd ~/backend
docker ps --format "table {{.Names}}\t{{.Status}}"
```

### Steg 2: Testa i Flutter

Starta Flutter-appen och verifiera:
- [ ] Login fungerar
- [ ] Cart-sidan laddar
- [ ] Profil-sidan laddar
- [ ] Produkter visas

### Steg 3: Testa fler endpoints

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"cmb@p8.se","password":"magnus123"}' | jq -r '.accessToken')

# Products (public)
curl http://localhost:8080/shop/api/products

# Categories (public)
curl http://localhost:8080/shop/api/categories

# Lägg till i cart
curl -X POST http://localhost:8080/shop/api/cart/add \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"productId": 1, "quantity": 1}'
```

---

## DEL 2: Förberedelser v2.0

### Huvudproblem: customerId vs userId

**Nuvarande:**
```
adminDB.users:     user_id (PK), email, password_hash...
shopDB.customers:  customer_id (PK), user_id (FK), email...
```

**Problem:**
- Dubbel email-lagring
- Manuell synk krävs
- Guest customers (user_id = NULL) komplicerar

### Förslag A: Behåll separation (enklare)
- Automatisk customer-skapning vid user-registrering
- Event/webhook från admin-service till shop-service

### Förslag B: Dela user_id (renare)
- Ta bort customer_id helt
- Använd user_id som PK i customers
- Kräver databasmigration

### Frågor att besluta:
1. Ska guest checkout finnas i v2.0?
2. Vilken approach: A eller B?
3. Hur migrerar vi befintlig data?

---

## DEL 3: Telefon-standardisering

### Nuvarande inkonsistens
- Customer.phone
- Address.phoneNumber

### Fix (enkel)
I Address.java, ändra:
```java
// Från:
private String phoneNumber;

// Till:
@Column(name = "phone_number")  // behåll DB-kolumnnamn
private String phone;
```

Eller kör databasmigration:
```sql
ALTER TABLE addresses CHANGE phone_number phone VARCHAR(30);
```

---

## PRIORITETSORDNING

1. **Idag:** Testa Flutter mot alla endpoints
2. **Denna vecka:** Fixa eventuella endpoint-problem
3. **v2.0 sprint:** ID-refactoring + telefon-standardisering

---

## TESTANVÄNDARE

| Email | Lösenord | Roll | shopDB |
|-------|----------|------|--------|
| cmb@p8.se | magnus123 | SUPER_ADMIN | ✅ customer_id=7 |
| jonathan@p8.se | jonathan123 | SUPER_ADMIN | ❌ måste läggas till |
| shop@perfect8.com | shop123 | STAFF | ❌ måste läggas till |

### Lägg till saknade customers:
```bash
docker exec -it shopDB mysql -u root -pjava100rootpassword123 -e "
INSERT INTO shopDB.customers (email, first_name, last_name, user_id, active, email_verified, newsletter_subscribed, marketing_consent, created_date, updated_date) 
VALUES 
('jonathan@p8.se', 'Jonathan', 'Admin', 2, 1, 1, 0, 0, NOW(), NOW()),
('shop@perfect8.com', 'Shop', 'Staff', 3, 1, 1, 0, 0, NOW(), NOW());
"
```
