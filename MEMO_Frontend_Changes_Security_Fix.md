# MEMO: Frontend-ändringar efter Säkerhetsfix

**Datum:** 2026-02-05  
**Version:** 1.3.1  
**Status:** ⏳ Väntar på implementation

---

## 🎯 SAMMANFATTNING

Backend `OrderController.java` har uppdaterats med ägarskapsverifiering.
Frontend behöver anpassas för nya/ändrade endpoints.

---

## 🆕 NYTT ENDPOINT

### `GET /api/orders/my-orders`

**Beskrivning:** Hämtar inloggad kunds ordrar automatiskt (rekommenderas!)

**Fördelar:**
- Ingen customerId behövs i URL
- Säkrare - backend läser customerId från JWT
- Enklare frontend-kod

**Flutter-implementation:**
```dart
// FÖRE (osäkert):
Future<List<Order>> getOrders(int customerId) async {
  final response = await http.get('/api/orders/customer/$customerId');
  // ...
}

// EFTER (säkert):
Future<List<Order>> getMyOrders() async {
  final response = await http.get('/api/orders/my-orders');
  // Backend hämtar customerId från JWT automatiskt
}
```

---

## ✏️ ÄNDRADE ENDPOINTS

| Endpoint | Ändring | Frontend-åtgärd |
|----------|---------|-----------------|
| `GET /api/orders/{orderId}` | Kräver ägarskap | Ingen (hanteras i backend) |
| `GET /api/orders/number/{orderNumber}` | Kräver ägarskap | Ingen |
| `GET /api/orders/customer/{customerId}` | Verifierar match | Byt till `/my-orders` |
| `POST /api/orders/{orderId}/cancel` | Kräver ägarskap | Ingen |
| `POST /api/orders/{orderId}/return` | Kräver ägarskap | Ingen |

---

## 🔐 NYA ADMIN-ENDPOINTS

Om du bygger admin-panel i Flutter:

| Endpoint | Beskrivning |
|----------|-------------|
| `GET /api/orders/admin/all` | Alla ordrar |
| `GET /api/orders/admin/status/{status}` | Ordrar per status |
| `GET /api/orders/admin/today` | Dagens ordrar |
| `GET /api/orders/admin/requiring-attention` | Kräver åtgärd |
| `PUT /api/orders/admin/{orderId}/status` | Uppdatera status |
| `GET /api/orders/admin/customer/{customerId}` | Kundens ordrar |
| `DELETE /api/orders/admin/{orderId}` | Radera (SUPER_ADMIN) |

---

## 📋 CHECKLISTA

### order_service.dart
- [ ] Byt `getOrders(customerId)` → `getMyOrders()`
- [ ] Ta bort customerId-parameter från order-anrop
- [ ] Lägg till admin-endpoints (om admin-panel finns)

### Screens att uppdatera
- [ ] `orders_screen.dart` - Använd `/my-orders`
- [ ] `order_detail_screen.dart` - Ingen ändring krävs
- [ ] `admin_orders_screen.dart` - Använd `/admin/*` endpoints

---

## ⚠️ FELHANTERING

Backend returnerar nu `403 Forbidden` om kund försöker:
- Se andras ordrar
- Avbryta andras ordrar
- Returnera andras ordrar

**Flutter bör hantera:**
```dart
if (response.statusCode == 403) {
  // Visa felmeddelande: "Du har inte behörighet"
  // ELLER logga ut användaren (kan vara token-problem)
}
```

---

## 🗓️ PRIORITET

1. **HÖG:** Byt till `/my-orders` i `order_service.dart`
2. **MEDIUM:** Lägg till 403-hantering
3. **LÅG:** Admin-endpoints (om admin-panel byggs)

---

**Skapad:** 2026-02-05  
**Relaterad fil:** `OrderController.java` (backend)
