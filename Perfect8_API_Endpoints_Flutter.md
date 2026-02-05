# Perfect8 API Endpoints - Flutter Checklista

**Version:** 1.3  
**Genererad från:** Backend Test Suite (77 tester)  
**Syfte:** Verifiera att Flutter-appen har kod för alla backend-endpoints  
**Base URL:** `https://p8.rantila.com`

---

## 📋 INSTRUKTIONER

1. Öppna detta dokument i VS Code
2. Öppna Flutter-projektet: `lib/services/`
3. Markera [x] för varje endpoint som finns implementerad
4. Notera saknade endpoints för implementation

---

## 🔐 AUTH ENDPOINTS (auth_service.dart)

**Gateway path:** `/api/auth/*` (inget service-prefix)

| Metod | Endpoint | Request Body | Response | Flutter | Testad |
|-------|----------|--------------|----------|---------|--------|
| POST | `/api/auth/login` | `{email, password}` | `{accessToken, refreshToken, user}` | [ ] | ✅ |
| POST | `/api/auth/register` | `{email, password, firstName?, lastName?}` | `{accessToken, refreshToken, user}` | [ ] | ✅ |
| POST | `/api/auth/refresh` | `{refreshToken}` | `{accessToken, refreshToken}` | [ ] | ✅ |
| POST | `/api/auth/logout` | `{refreshToken}` | `200 OK` | [ ] | ✅ |

### Flutter Implementation

```dart
// lib/services/auth_service.dart

class AuthService {
  final String baseUrl = 'https://p8.rantila.com';
  
  // ✅ POST /api/auth/login
  Future<AuthResponse> login(String email, String password);
  
  // ✅ POST /api/auth/register
  Future<AuthResponse> register(RegisterRequest request);
  
  // ✅ POST /api/auth/refresh
  Future<AuthResponse> refreshToken(String refreshToken);
  
  // ✅ POST /api/auth/logout
  Future<void> logout(String refreshToken);
}
```

---

## 🛍️ SHOP - PRODUCTS (product_service.dart)

**Gateway path:** `/shop/api/products`

| Metod | Endpoint | Auth | Response | Flutter | Testad |
|-------|----------|------|----------|---------|--------|
| GET | `/shop/api/products` | ❌ | Lista produkter | [ ] | ✅ |
| GET | `/shop/api/products/{id}` | ❌ | En produkt | [ ] | ✅ |
| GET | `/shop/api/products/search?q=` | ❌ | Sökresultat | [ ] | ⚠️ |
| POST | `/shop/api/products` | ✅ ADMIN | Skapa produkt | [ ] | ✅ |
| PUT | `/shop/api/products/{id}` | ✅ ADMIN | Uppdatera produkt | [ ] | ✅ |
| DELETE | `/shop/api/products/{id}` | ✅ ADMIN | Ta bort produkt | [ ] | ✅ |

### Flutter Implementation

```dart
// lib/services/product_service.dart

class ProductService {
  // ❌ Ingen auth krävs
  Future<List<Product>> getProducts();
  Future<Product> getProduct(int id);
  Future<List<Product>> searchProducts(String query);
  
  // ✅ Admin auth krävs
  Future<Product> createProduct(ProductRequest request, String token);
  Future<Product> updateProduct(int id, ProductRequest request, String token);
  Future<void> deleteProduct(int id, String token);
}
```

---

## 📁 SHOP - CATEGORIES (product_service.dart eller category_service.dart)

**Gateway path:** `/shop/api/categories`

| Metod | Endpoint | Auth | Response | Flutter | Testad |
|-------|----------|------|----------|---------|--------|
| GET | `/shop/api/categories` | ❌ | Lista kategorier | [ ] | ✅ |
| GET | `/shop/api/categories/{id}` | ❌ | En kategori | [ ] | ✅ |
| GET | `/shop/api/categories/{id}/products` | ❌ | Produkter i kategori | [ ] | ✅ |

### Flutter Implementation

```dart
// lib/services/product_service.dart (eller separat)

Future<List<Category>> getCategories();
Future<Category> getCategory(int id);
Future<List<Product>> getProductsByCategory(int categoryId);
```

---

## 🛒 SHOP - CART (cart_service.dart)

**Gateway path:** `/shop/api/cart`  
**Auth:** ✅ JWT krävs för alla endpoints

| Metod | Endpoint | Request Body | Response | Flutter | Testad |
|-------|----------|--------------|----------|---------|--------|
| GET | `/shop/api/cart` | - | Kundvagn | [ ] | ✅ |
| POST | `/shop/api/cart/add` | `{productId, quantity}` | Uppdaterad vagn | [ ] | ✅ |
| PUT | `/shop/api/cart/update` | `{cartItemId, quantity}` | Uppdaterad vagn | [ ] | ✅ |
| DELETE | `/shop/api/cart/remove/{itemId}` | - | Uppdaterad vagn | [ ] | ✅ |
| DELETE | `/shop/api/cart/clear` | - | Tom vagn | [ ] | ⚠️ |

### Flutter Implementation

```dart
// lib/services/cart_service.dart

class CartService {
  // Alla kräver JWT token i header
  Future<Cart> getCart(String token);
  Future<Cart> addToCart(int productId, int quantity, String token);
  Future<Cart> updateCartItem(int cartItemId, int quantity, String token);
  Future<Cart> removeFromCart(int cartItemId, String token);
  Future<void> clearCart(String token);
}
```

---

## 📦 SHOP - ORDERS (order_service.dart)

**Gateway path:** `/shop/api/orders`  
**Auth:** ✅ JWT krävs för alla endpoints

| Metod | Endpoint | Request Body | Response | Flutter | Testad |
|-------|----------|--------------|----------|---------|--------|
| GET | `/shop/api/orders` | - | Lista ordrar | [ ] | ✅ |
| GET | `/shop/api/orders/{id}` | - | Orderdetaljer | [ ] | ✅ |
| POST | `/shop/api/orders/create` | `{addressId, paymentMethod}` | Ny order | [ ] | ✅ |
| PUT | `/shop/api/orders/{id}/cancel` | - | Avbruten order | [ ] | ⚠️ |

### Flutter Implementation

```dart
// lib/services/order_service.dart

class OrderService {
  Future<List<Order>> getOrders(String token);
  Future<Order> getOrder(int orderId, String token);
  Future<Order> createOrder(CreateOrderRequest request, String token);
  Future<Order> cancelOrder(int orderId, String token);
}
```

---

## 👤 SHOP - CUSTOMERS (customer_service.dart)

**Gateway path:** `/shop/api/customers`  
**Auth:** ✅ JWT krävs

| Metod | Endpoint | Request Body | Response | Flutter | Testad |
|-------|----------|--------------|----------|---------|--------|
| GET | `/shop/api/customers/profile` | - | Kundprofil | [ ] | ✅ |
| PUT | `/shop/api/customers/profile` | `{firstName, lastName, phone}` | Uppdaterad profil | [ ] | ✅ |

### Flutter Implementation

```dart
// lib/services/customer_service.dart

class CustomerService {
  Future<Customer> getProfile(String token);
  Future<Customer> updateProfile(UpdateProfileRequest request, String token);
}
```

---

## 💳 SHOP - PAYMENTS (payment_service.dart)

**Gateway path:** `/shop/api/payments`  
**Auth:** ✅ JWT krävs

| Metod | Endpoint | Request Body | Response | Flutter | Testad |
|-------|----------|--------------|----------|---------|--------|
| POST | `/shop/api/payments/initiate` | `{orderId, method}` | PayPal redirect URL | [ ] | ⚠️ |
| POST | `/shop/api/payments/process` | `{paymentId, payerId}` | Bekräftelse | [ ] | ⚠️ |
| GET | `/shop/api/payments/{id}` | - | Betalningsstatus | [ ] | ⚠️ |

### Flutter Implementation

```dart
// lib/services/payment_service.dart (om separat)

class PaymentService {
  Future<PaymentInitResponse> initiatePayment(int orderId, String method, String token);
  Future<PaymentResponse> processPayment(String paymentId, String payerId, String token);
  Future<Payment> getPaymentStatus(int paymentId, String token);
}
```

---

## 📝 BLOG ENDPOINTS (blog_service.dart)

**Gateway path:** `/blog/api/posts`

| Metod | Endpoint | Auth | Response | Flutter | Testad |
|-------|----------|------|----------|---------|--------|
| GET | `/blog/api/posts` | ❌ | Lista posts | [ ] | ✅ |
| GET | `/blog/api/posts?page=0&size=10` | ❌ | Paginerad lista | [ ] | ✅ |
| GET | `/blog/api/posts/{slug}` | ❌ | En post | [ ] | ✅ |
| POST | `/blog/api/posts` | ✅ ADMIN | Skapa post | [ ] | ✅ |
| PUT | `/blog/api/posts/{id}` | ✅ ADMIN | Uppdatera post | [ ] | ✅ |
| DELETE | `/blog/api/posts/{id}` | ✅ ADMIN | Ta bort post | [ ] | ✅ |

### Flutter Implementation

```dart
// lib/services/blog_service.dart

class BlogService {
  // Publika
  Future<PaginatedResponse<Post>> getPosts({int page = 0, int size = 10});
  Future<Post> getPostBySlug(String slug);
  
  // Admin
  Future<Post> createPost(PostRequest request, String token);
  Future<Post> updatePost(int id, PostRequest request, String token);
  Future<void> deletePost(int id, String token);
}
```

---

## 🖼️ IMAGE ENDPOINTS (image_service.dart eller i api_service.dart)

**Gateway path:** `/image/api/images`

| Metod | Endpoint | Auth | Response | Flutter | Testad |
|-------|----------|------|----------|---------|--------|
| GET | `/image/api/images/{id}` | ❌ | Bild-data | [ ] | ✅ |
| GET | `/image/api/images/{id}/thumbnail/{size}` | ❌ | Thumbnail | [ ] | ✅ |
| GET | `/image/api/images/category/{category}` | ❌ | Bilder i kategori | [ ] | ✅ |
| POST | `/image/api/images/upload` | ✅ ADMIN | Ladda upp bild | [ ] | ✅ |
| DELETE | `/image/api/images/{id}` | ✅ ADMIN | Ta bort bild | [ ] | ✅ |

**Thumbnail sizes:** `SMALL`, `MEDIUM`, `LARGE`, `ORIGINAL`

### Flutter Implementation

```dart
// lib/services/image_service.dart (eller i api_service.dart)

String getImageUrl(int imageId) => '$baseUrl/image/api/images/$imageId';
String getThumbnailUrl(int imageId, String size) => 
    '$baseUrl/image/api/images/$imageId/thumbnail/$size';

Future<ImageResponse> uploadImage(File file, String altText, String token);
Future<void> deleteImage(int imageId, String token);
```

---

## 📧 EMAIL ENDPOINTS (email_service.dart) - ADMIN ONLY

**Gateway path:** `/email/*`  
**Auth:** ✅ ADMIN JWT krävs

| Metod | Endpoint | Request Body | Response | Flutter | Testad |
|-------|----------|--------------|----------|---------|--------|
| POST | `/email/send` | `{to, subject, template, variables}` | Skickat | [ ] | ✅ |
| GET | `/email/logs` | - | Email-historik | [ ] | ✅ |

### Flutter Implementation

```dart
// lib/services/email_service.dart (admin only)

class EmailService {
  Future<void> sendEmail(EmailRequest request, String token);
  Future<List<EmailLog>> getEmailLogs(String token);
}
```

---

## 🏥 HEALTH ENDPOINT

| Metod | Endpoint | Response | Flutter | Testad |
|-------|----------|----------|---------|--------|
| GET | `/actuator/health` | `{status: "UP"}` | [ ] | ✅ |

---

## 📊 SAMMANFATTNING

### Endpoints per Service

| Service | Totalt | Publika | Auth | Admin |
|---------|--------|---------|------|-------|
| Auth | 4 | 4 | - | - |
| Products | 6 | 3 | - | 3 |
| Categories | 3 | 3 | - | - |
| Cart | 5 | - | 5 | - |
| Orders | 4 | - | 4 | - |
| Customers | 2 | - | 2 | - |
| Payments | 3 | - | 3 | - |
| Blog | 6 | 3 | - | 3 |
| Image | 5 | 3 | - | 2 |
| Email | 2 | - | - | 2 |
| **TOTALT** | **40** | **16** | **14** | **10** |

### Flutter Services Checklista

```
lib/services/
├── [ ] api_service.dart       (Base HTTP, token handling)
├── [ ] auth_service.dart      (4 endpoints)
├── [ ] product_service.dart   (6+3 endpoints)
├── [ ] cart_service.dart      (5 endpoints)
├── [ ] order_service.dart     (4 endpoints)
├── [ ] customer_service.dart  (2 endpoints)
├── [ ] blog_service.dart      (6 endpoints)
├── [ ] email_service.dart     (2 endpoints, admin)
└── [ ] image_service.dart     (5 endpoints, eller i api_service)
```

---

## ⚠️ VIKTIGT: HTTP Headers

### Utan auth (publika endpoints)
```dart
headers: {
  'Content-Type': 'application/json',
  'Accept': 'application/json',
}
```

### Med auth (skyddade endpoints)
```dart
headers: {
  'Content-Type': 'application/json',
  'Accept': 'application/json',
  'Authorization': 'Bearer $accessToken',
}
```

---

## 🔄 Token Refresh Logic

```dart
// Pseudokod för automatisk token refresh

Future<Response> authenticatedRequest(RequestOptions options) async {
  try {
    return await dio.request(options);
  } on DioError catch (e) {
    if (e.response?.statusCode == 401) {
      // Token expired - refresh it
      final newTokens = await authService.refreshToken(refreshToken);
      
      // Retry with new token
      options.headers['Authorization'] = 'Bearer ${newTokens.accessToken}';
      return await dio.request(options);
    }
    rethrow;
  }
}
```

---

**Version:** 1.3  
**Skapad:** 2026-01-31  
**Källa:** Backend Test Suite (77 tester)

---

*Använd denna checklista för att verifiera Flutter-implementation mot backend API.*
