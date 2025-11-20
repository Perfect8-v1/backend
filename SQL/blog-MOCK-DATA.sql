-- ================================================
-- blog-MOCK-DATA.sql
-- Database: blogDB
-- Created: 2025-11-16
-- Purpose: Mock data for blog-service testing
-- 
-- IMPORTANT NOTES:
-- - Password hash: BCrypt hash for "password123"
-- - Roles: ROLE_USER, ROLE_ADMIN (from blog/model/Role.java)
-- - Swedish tech/development blog content
-- - 5 tables: roles → users → user_roles → posts → image_references
-- ================================================

-- ================================================
-- TRUNCATE TABLES (Safe reload, FK-aware order)
-- ================================================

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE image_references;
TRUNCATE TABLE posts;
TRUNCATE TABLE user_roles;
TRUNCATE TABLE users;
TRUNCATE TABLE roles;
SET FOREIGN_KEY_CHECKS = 1;

-- ================================================
-- INSERT MOCK DATA: roles
-- ================================================

INSERT INTO roles (name) VALUES
('ROLE_USER'),
('ROLE_ADMIN');

-- ================================================
-- INSERT MOCK DATA: users
-- ================================================

INSERT INTO users (
    username,
    email,
    password_hash,
    first_name,
    last_name,
    created_date,
    updated_date
) VALUES
-- Admin Authors
(
    'magnus.b',
    'magnus@perfect8blog.se',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- password123
    'Magnus',
    'Berglund',
    '2025-01-15 10:00:00.000000',
    '2025-11-16 09:00:00.000000'
),
(
    'sara.dev',
    'sara.lindgren@perfect8blog.se',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- password123
    'Sara',
    'Lindgren',
    '2025-02-20 11:30:00.000000',
    '2025-11-15 14:20:00.000000'
),

-- Regular Authors
(
    'erik.tech',
    'erik.svensson@perfect8blog.se',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- password123
    'Erik',
    'Svensson',
    '2025-03-10 09:00:00.000000',
    '2025-11-10 16:45:00.000000'
),
(
    'anna.code',
    'anna.eriksson@perfect8blog.se',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- password123
    'Anna',
    'Eriksson',
    '2025-04-05 13:00:00.000000',
    '2025-11-12 10:30:00.000000'
),
(
    'johan.spring',
    'johan.andersson@perfect8blog.se',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- password123
    'Johan',
    'Andersson',
    '2025-05-12 15:30:00.000000',
    '2025-11-08 12:00:00.000000'
);

-- ================================================
-- INSERT MOCK DATA: user_roles
-- ================================================

INSERT INTO user_roles (user_id, role_id) VALUES
-- Magnus & Sara = ADMIN
(1, 1), -- Magnus: ROLE_USER
(1, 2), -- Magnus: ROLE_ADMIN
(2, 1), -- Sara: ROLE_USER
(2, 2), -- Sara: ROLE_ADMIN

-- Erik, Anna, Johan = USER only
(3, 1), -- Erik: ROLE_USER
(4, 1), -- Anna: ROLE_USER
(5, 1); -- Johan: ROLE_USER

-- ================================================
-- INSERT MOCK DATA: posts
-- ================================================

INSERT INTO posts (
    title,
    content,
    slug,
    user_id,
    published_date,
    created_date,
    updated_date,
    published,
    view_count
) VALUES
-- Published Posts (visible to public)
(
    'Introduktion till Spring Boot 3.4',
    '<h2>Välkommen till Spring Boot 3.4</h2>
<p>Spring Boot 3.4 kommer med många spännande förbättringar som gör Java-utveckling ännu enklare. I denna artikel går vi igenom de viktigaste nyheterna.</p>

<h3>Viktiga förbättringar</h3>
<ul>
<li>Förbättrad startup-tid med Virtual Threads</li>
<li>Native image support direkt out-of-the-box</li>
<li>Observability med Micrometer förbättrat</li>
<li>HTTP/3 support via Netty</li>
</ul>

<h3>Kom igång</h3>
<p>För att börja med Spring Boot 3.4, se till att du har Java 21 installerat. Detta är minimum requirement för denna version.</p>

<pre><code class="language-xml">
&lt;dependency&gt;
    &lt;groupId&gt;org.springframework.boot&lt;/groupId&gt;
    &lt;artifactId&gt;spring-boot-starter-web&lt;/artifactId&gt;
    &lt;version&gt;3.4.1&lt;/version&gt;
&lt;/dependency&gt;
</code></pre>

<p>Läs mer i den officiella dokumentationen!</p>',
    'introduktion-till-spring-boot-34',
    1, -- Magnus
    '2025-11-01 08:00:00.000000',
    '2025-10-28 14:30:00.000000',
    '2025-11-01 07:55:00.000000',
    TRUE, -- Published
    1847
),

(
    'Docker och Microservices - Best Practices',
    '<h2>Docker i produktion</h2>
<p>Att köra microservices i Docker containers är standard idag. Men hur gör man det rätt? Här är mina tips efter 5 år med Docker i produktion.</p>

<h3>Health Checks är viktiga</h3>
<p>Glöm aldrig att lägga till health checks i din docker-compose.yml:</p>

<pre><code class="language-yaml">
healthcheck:
  test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
  interval: 5s
  timeout: 3s
  retries: 40
</code></pre>

<h3>Multi-stage builds</h3>
<p>Använd multi-stage builds för att hålla dina images små. Detta sparar både diskutrymme och deploymenttid.</p>

<h3>Environment variables</h3>
<p>Håll känsliga värden utanför din Dockerfile. Använd .env filer som inte checkas in i Git.</p>',
    'docker-och-microservices-best-practices',
    2, -- Sara
    '2025-11-05 10:30:00.000000',
    '2025-11-04 16:20:00.000000',
    '2025-11-05 10:25:00.000000',
    TRUE, -- Published
    923
),

(
    'JWT Authentication med Spring Security',
    '<h2>Säker autentisering med JWT</h2>
<p>JSON Web Tokens (JWT) är en populär metod för stateless authentication. I denna guide visar jag hur du implementerar det med Spring Security.</p>

<h3>Vad är JWT?</h3>
<p>JWT är en öppen standard (RFC 7519) för att säkert överföra information mellan parter som ett JSON-objekt.</p>

<h3>Implementation</h3>
<p>För att komma igång behöver du först lägga till JJWT-biblioteket:</p>

<pre><code class="language-xml">
&lt;dependency&gt;
    &lt;groupId&gt;io.jsonwebtoken&lt;/groupId&gt;
    &lt;artifactId&gt;jjwt-api&lt;/artifactId&gt;
    &lt;version&gt;0.12.3&lt;/version&gt;
&lt;/dependency&gt;
</code></pre>

<h3>Security Filter Chain</h3>
<p>Skapa en JwtAuthenticationFilter som validerar tokens på varje request. Detta filter ska köras före UsernamePasswordAuthenticationFilter.</p>

<h3>Best Practices</h3>
<ul>
<li>Använd stark secret key (minst 256 bits)</li>
<li>Sätt rimliga expiration times (15-60 min)</li>
<li>Implementera refresh tokens</li>
<li>Validera ALLA claims i token</li>
</ul>',
    'jwt-authentication-med-spring-security',
    1, -- Magnus
    '2025-11-08 12:00:00.000000',
    '2025-11-07 09:45:00.000000',
    '2025-11-08 11:58:00.000000',
    TRUE, -- Published
    1456
),

(
    'Hibernate Performance Tips',
    '<h2>Optimera dina Hibernate queries</h2>
<p>Hibernate är fantastiskt, men det kan lätt bli långsamt om du inte är försiktig. Här är mina bästa tips för att undvika vanliga fallgropar.</p>

<h3>N+1 Problem</h3>
<p>Det vanligaste problemet är N+1 queries. Lös detta med @EntityGraph eller JOIN FETCH:</p>

<pre><code class="language-java">
@Query("SELECT o FROM Order o JOIN FETCH o.orderItems WHERE o.orderId = :id")
Order findByIdWithItems(@Param("id") Long id);
</code></pre>

<h3>Lazy Loading</h3>
<p>Använd @OneToMany(fetch = FetchType.LAZY) som default. Hämta bara data när du verkligen behöver den.</p>

<h3>Batch Fetching</h3>
<p>Konfigurera batch size för att minska antalet queries:</p>

<pre><code class="language-properties">
spring.jpa.properties.hibernate.default_batch_fetch_size=20
</code></pre>

<h3>Second Level Cache</h3>
<p>För data som sällan ändras, använd second level cache med Redis eller Caffeine.</p>',
    'hibernate-performance-tips',
    3, -- Erik
    '2025-11-10 14:30:00.000000',
    '2025-11-09 13:15:00.000000',
    '2025-11-10 14:28:00.000000',
    TRUE, -- Published
    734
),

(
    'Flutter för Backend-utvecklare',
    '<h2>Från Java till Flutter</h2>
<p>Som backend-utvecklare kan Flutter kännas helt nytt. Men oroa dig inte - Dart är faktiskt ganska likt Java!</p>

<h3>Dart vs Java</h3>
<p>Dart har många likheter med Java:</p>
<ul>
<li>Objektorienterat språk</li>
<li>Stark typing (men med type inference)</li>
<li>Liknande syntax för klasser och metoder</li>
<li>Futures (motsvarar CompletableFuture)</li>
</ul>

<h3>REST API Integration</h3>
<p>Att konsumera ditt Spring Boot API från Flutter är enkelt med http-paketet:</p>

<pre><code class="language-dart">
final response = await http.get(
  Uri.parse("https://api.perfect8.se/products"),
  headers: {"Authorization": "Bearer $token"}
);
</code></pre>

<h3>State Management</h3>
<p>För state management rekommenderar jag Riverpod eller Provider. Dessa fungerar lite som Spring Beans - dependency injection för Flutter!</p>',
    'flutter-for-backend-utvecklare',
    4, -- Anna
    '2025-11-12 09:00:00.000000',
    '2025-11-11 15:30:00.000000',
    '2025-11-12 08:57:00.000000',
    TRUE, -- Published
    512
),

-- Draft Posts (not published yet)
(
    'MySQL 8.0 Performance Tuning',
    '<h2>Optimera din MySQL databas</h2>
<p>Draft: Work in progress...</p>

<h3>Indexering</h3>
<p>TODO: Förklara hur man skapar effektiva index...</p>

<h3>Query Optimization</h3>
<p>TODO: EXPLAIN ANALYZE examples...</p>',
    'mysql-80-performance-tuning',
    5, -- Johan
    NULL, -- Not published
    '2025-11-14 10:00:00.000000',
    '2025-11-15 16:30:00.000000',
    FALSE, -- Draft
    0
),

(
    'Kubernetes för Java-utvecklare',
    '<h2>Deploying Spring Boot till Kubernetes</h2>
<p>Draft: Outline...</p>

<ul>
<li>Pods och Services</li>
<li>ConfigMaps och Secrets</li>
<li>Health checks och Readiness probes</li>
<li>Horizontal Pod Autoscaling</li>
</ul>

<p>TODO: Fyll i detaljer...</p>',
    'kubernetes-for-java-utvecklare',
    2, -- Sara
    NULL, -- Not published
    '2025-11-15 11:20:00.000000',
    '2025-11-16 08:45:00.000000',
    FALSE, -- Draft
    0
),

(
    'ADHD-anpassad Utveckling',
    '<h2>Produktivitet med ADHD</h2>
<p>Som utvecklare med ADHD har jag lärt mig några viktiga tekniker för att hålla fokus och vara produktiv.</p>

<h3>En sak i taget</h3>
<p>Det viktigaste för mig är att bara fokusera på EN fil, EN funktion, EN bug åt gången. Multitasking fungerar inte.</p>

<h3>Dokumentera allt</h3>
<p>Mitt korta arbetsminne betyder att jag måste skriva ner ALLT. Därför har jag skapat Bootstrap Addendum-systemet.</p>

<h3>Max 2 alternativ</h3>
<p>För många val skapar beslutsvånda. Begränsa alternativen till max 2.</p>

<h3>Kompletta filer</h3>
<p>Aldrig "ändra rad 47". Alltid ge mig hela filen. Detta eliminerar felkällor.</p>',
    'adhd-anpassad-utveckling',
    1, -- Magnus
    '2025-11-13 15:00:00.000000',
    '2025-11-12 12:00:00.000000',
    '2025-11-13 14:58:00.000000',
    TRUE, -- Published
    2103
);

-- ================================================
-- INSERT MOCK DATA: image_references
-- ================================================

INSERT INTO image_references (
    post_id,
    image_id,
    caption,
    display_order,
    created_date
) VALUES
-- Images for Spring Boot post
(1, 101, 'Spring Boot 3.4 Logo', 0, '2025-10-28 14:35:00.000000'),
(1, 102, 'Architecture Diagram', 1, '2025-10-28 14:40:00.000000'),

-- Images for Docker post
(2, 103, 'Docker Compose Example', 0, '2025-11-04 16:25:00.000000'),
(2, 104, 'Microservices Architecture', 1, '2025-11-04 16:30:00.000000'),

-- Images for JWT post
(3, 105, 'JWT Token Flow', 0, '2025-11-07 09:50:00.000000'),
(3, 106, 'Security Filter Chain', 1, '2025-11-07 09:55:00.000000'),
(3, 107, 'Authentication Sequence', 2, '2025-11-07 10:00:00.000000'),

-- Images for Hibernate post
(4, 108, 'N+1 Problem Diagram', 0, '2025-11-09 13:20:00.000000'),
(4, 109, 'Query Performance Graph', 1, '2025-11-09 13:25:00.000000'),

-- Images for Flutter post
(5, 110, 'Flutter Logo', 0, '2025-11-11 15:35:00.000000'),
(5, 111, 'REST API Integration', 1, '2025-11-11 15:40:00.000000'),

-- Images for ADHD post
(8, 112, 'Productivity Tips Infographic', 0, '2025-11-12 12:05:00.000000');

-- ================================================
-- VERIFICATION QUERIES (uncomment to test)
-- ================================================

-- -- Check users and their roles
-- SELECT 
--     u.user_id,
--     u.username,
--     u.email,
--     CONCAT(u.first_name, ' ', u.last_name) AS full_name,
--     GROUP_CONCAT(r.name ORDER BY r.name SEPARATOR ', ') AS roles,
--     DATE_FORMAT(u.created_date, '%Y-%m-%d') AS joined
-- FROM users u
-- LEFT JOIN user_roles ur ON u.user_id = ur.user_id
-- LEFT JOIN roles r ON ur.role_id = r.role_id
-- GROUP BY u.user_id
-- ORDER BY u.created_date;

-- -- Check posts with author info
-- SELECT 
--     p.post_id,
--     p.title,
--     p.slug,
--     CONCAT(u.first_name, ' ', u.last_name) AS author,
--     p.published,
--     p.view_count,
--     DATE_FORMAT(p.published_date, '%Y-%m-%d') AS published_on,
--     (SELECT COUNT(*) FROM image_references WHERE post_id = p.post_id) AS image_count
-- FROM posts p
-- JOIN users u ON p.user_id = u.user_id
-- ORDER BY p.published_date DESC;

-- -- Check image references
-- SELECT 
--     ir.image_reference_id,
--     p.title AS post_title,
--     ir.image_id,
--     ir.caption,
--     ir.display_order
-- FROM image_references ir
-- JOIN posts p ON ir.post_id = p.post_id
-- ORDER BY ir.post_id, ir.display_order;

-- ================================================
-- End of blog-MOCK-DATA.sql
-- 
-- SUMMARY:
-- - 5 blog users (2 ADMIN, 3 USER)
-- - 8 blog posts (6 published, 2 drafts)
-- - 12 image references
-- - Swedish tech/development content
-- - Realistic view counts and timestamps
-- - Ready for frontend testing
-- ================================================
