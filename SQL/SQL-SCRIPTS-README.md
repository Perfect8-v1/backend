# Perfect8 - SQL Schema Scripts

**Created:** 2025-11-09  
**Purpose:** Create and manage database schemas for all 5 services

---

## 📁 FILES INCLUDED

### SQL Files (10 total)

#### ⚠️ DROP Files (DANGEROUS!)
- `admin-DROP-TABLE.sql`
- `blog-DROP-TABLE.sql`
- `email-DROP-TABLE.sql`
- `image-DROP-TABLE.sql`
- `shop-DROP-TABLE.sql`

#### ✅ CREATE Files (SAFE)
- `admin-CREATE-TABLE.sql` - 1 table
- `blog-CREATE-TABLE.sql` - 5 tables
- `email-CREATE-TABLE.sql` - 1 table
- `image-CREATE-TABLE.sql` - 1 table
- `shop-CREATE-TABLE.sql` - 12 tables

### Bash Scripts (2 total)
- `run-create-tables.sh` - Runs all CREATE scripts ✅
- `run-drop-tables.sh` - Runs all DROP scripts ⚠️

---

## 🚀 QUICK START

### 1. Configure MySQL Connection

**Option A:** Edit scripts directly
```bash
# Open script and change these lines:
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-3306}"
DB_USER="${DB_USER:-root}"
DB_PASSWORD="${DB_PASSWORD:-your_password_here}"
```

**Option B:** Use environment variables
```bash
export DB_HOST=localhost
export DB_PORT=3306
export DB_USER=root
export DB_PASSWORD=your_password
```

**Option C:** Use .env (if using docker-compose)
The scripts will read from your environment automatically.

### 2. Create All Tables

```bash
bash run-create-tables.sh
```

**What it does:**
- Creates all 18 tables across 5 databases
- Shows progress for each service
- Gives summary at the end
- Safe to run multiple times (uses IF NOT EXISTS)

### 3. Verify Tables

```bash
# Check admin tables
mysql -uroot -p -e "SHOW TABLES" adminDB

# Check blog tables
mysql -uroot -p -e "SHOW TABLES" blogDB

# Check email tables
mysql -uroot -p -e "SHOW TABLES" emailDB

# Check image tables
mysql -uroot -p -e "SHOW TABLES" imageDB

# Check shop tables
mysql -uroot -p -e "SHOW TABLES" shopDB
```

### 4. Start Services with Validation

Make sure `application.properties` has:
```properties
spring.jpa.hibernate.ddl-auto=validate
```

Start services:
```bash
cd backend
mvn spring-boot:run -pl admin-service
mvn spring-boot:run -pl blog-service
mvn spring-boot:run -pl email-service
mvn spring-boot:run -pl image-service
mvn spring-boot:run -pl shop-service
```

If `validate` fails, you'll see errors like:
```
org.hibernate.tool.schema.spi.SchemaManagementException: 
Schema-validation: missing column [created_date]
```

This helps you find mismatches between entities and database!

---

## ⚠️ DROP ALL TABLES (DANGER!)

**ONLY USE FOR:**
- Clean slate / fresh start
- Development reset
- Testing

**NEVER USE IN PRODUCTION!**

```bash
bash run-drop-tables.sh
```

**Safety features:**
- Requires typing 'DELETE_ALL_DATA'
- Second confirmation prompt
- 3 second countdown
- Colored warnings

---

## 🗂️ DATABASE STRUCTURE

### adminDB (1 table)
- `admin_users` - Admin accounts with JWT auth

### blogDB (5 tables)
- `roles` - User roles (ROLE_USER, ROLE_ADMIN)
- `users` - Blog users
- `user_roles` - User-Role join table
- `posts` - Blog posts
- `image_references` - Post-Image links

### emailDB (1 table)
- `email_templates` - HTML email templates

### imageDB (1 table)
- `images` - Image metadata & processing status

### shopDB (12 tables)
- `categories` - Product categories (hierarchical)
- `products` - Product catalog
- `customers` - Customer accounts
- `addresses` - Customer addresses
- `carts` - Shopping carts
- `cart_items` - Cart items
- `orders` - Customer orders
- `order_items` - Order items
- `payments` - Payment transactions
- `shipments` - Shipment info
- `shipment_tracking` - Tracking events
- `inventory_transactions` - Inventory movements

**Total:** 18 tables across 5 databases

---

## 🔧 MANUAL USAGE

### Create Tables for Single Service

```bash
# Admin service
mysql -uroot -p adminDB < admin-CREATE-TABLE.sql

# Blog service
mysql -uroot -p blogDB < blog-CREATE-TABLE.sql

# Email service
mysql -uroot -p emailDB < email-CREATE-TABLE.sql

# Image service
mysql -uroot -p imageDB < image-CREATE-TABLE.sql

# Shop service
mysql -uroot -p shopDB < shop-CREATE-TABLE.sql
```

### Drop Tables for Single Service

```bash
# ⚠️ DANGER - This deletes all data!
mysql -uroot -p adminDB < admin-DROP-TABLE.sql
```

---

## 🐛 TROUBLESHOOTING

### Problem: "Access denied for user"
**Solution:** Check DB_USER and DB_PASSWORD in script or environment

### Problem: "Unknown database 'adminDB'"
**Solution:** Databases must exist first. Check docker-compose.yml:
```yaml
adminDB:
  environment:
    MYSQL_DATABASE: adminDB
```

### Problem: "Can't connect to MySQL server"
**Solution:** Check DB_HOST and DB_PORT. If using Docker:
```bash
docker ps  # Check if MySQL containers are running
```

### Problem: "Table already exists"
**Solution:** This is fine! Scripts use `IF NOT EXISTS`. If you want fresh tables, run DROP script first.

### Problem: Validation error "missing column [created_at]"
**Solution:** You have old schema with `created_at`. Run DROP script, then CREATE script again.

### Problem: Validation error "wrong column type"
**Solution:** Check entity field type matches SQL type:
- Java `LocalDateTime` = SQL `DATETIME(6)`
- Java `Long` = SQL `BIGINT`
- Java `String` = SQL `VARCHAR`
- Java `boolean` = SQL `BOOLEAN`

---

## 📝 NAMING CONVENTIONS

All timestamp fields use `*Date` suffix (NOT `*At`):
- ✅ `created_date`
- ✅ `updated_date`
- ✅ `last_login_date`
- ❌ `created_at`
- ❌ `updated_at`

This matches the refactoring done on 2025-11-07.

---

## 🎯 NEXT STEPS

After creating tables:

1. **Start services** with `spring.jpa.hibernate.ddl-auto=validate`
2. **Check logs** for validation errors
3. **Fix any mismatches** between entities and schema
4. **Test endpoints** to ensure data is saved correctly
5. **Commit SQL files** to your repository

---

## 📚 REFERENCES

- Magnum Opus - ADHD-anpassad utveckling
- Perfect8_Feature_Map_v1.1.md - Complete entity documentation
- Missforstand_Analys.md - Common mistakes to avoid

---

**Created by:** Magnus & Claude  
**Date:** 2025-11-09  
**Version:** 1.0  
**License:** MIT
