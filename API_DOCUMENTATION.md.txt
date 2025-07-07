// API_DOCUMENTATION.md
```markdown
# API Documentation

## Authentication

All protected endpoints require JWT token in Authorization header:
```
Authorization: Bearer <token>
```

### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "username": "admin",
  "email": "admin@perfect8.com"
}
```

### Register
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "newuser",
  "email": "newuser@example.com",
  "password": "password123"
}
```

## Posts API

### Get All Published Posts
```http
GET /api/posts/public?page=0&size=10&sort=publishedAt,desc
```

### Get Post by Slug
```http
GET /api/posts/public/my-first-post
```

### Create Post (WRITER/ADMIN only)
```http
POST /api/posts/create
Authorization: Bearer <token>
Content-Type: application/json

{
  "title": "My New Post",
  "content": "Post content here...",
  "excerpt": "Short description",
  "published": true,
  "links": ["https://example.com", "https://google.com"]
}
```

### Update Post (WRITER/ADMIN only)
```http
PUT /api/posts/update/1
Authorization: Bearer <token>
Content-Type: application/json

{
  "title": "Updated Title",
  "content": "Updated content...",
  "excerpt": "Updated excerpt",
  "published": true,
  "links": ["https://example.com"]
}
```

### Delete Post (WRITER/ADMIN only)
```http
DELETE /api/posts/delete/1
Authorization: Bearer <token>
```

## Image API

### Upload Image
```http
POST /api/images/upload
Authorization: Bearer <token>
Content-Type: multipart/form-data

file: <binary>
```

Response:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "filename": "image.jpg",
  "contentType": "image/jpeg",
  "size": 102400,
  "url": "/api/images/view/550e8400-e29b-41d4-a716-446655440000",
  "createdAt": "2024-01-01T12:00:00"
}
```

### View Image (Public)
```http
GET /api/images/view/550e8400-e29b-41d4-a716-446655440000
```

### Get Image Info
```http
GET /api/images/550e8400-e29b-41d4-a716-446655440000
Authorization: Bearer <token>
```

### Delete Image
```http
DELETE /api/images/550e8400-e29b-41d4-a716-446655440000
Authorization: Bearer <token>
```

## Admin API

### Get All Users (ADMIN only)
```http
GET /api/admin/users?page=0&size=20
Authorization: Bearer <token>
```

### Delete User (ADMIN only)
```http
DELETE /api/admin/users/2
Authorization: Bearer <token>
```

## Error Responses

All errors follow this format:
```json
{
  "status": 404,
  "message": "Resource not found",
  "timestamp": "2024-01-01T12:00:00"
}
```

Validation errors include field details:
```json
{
  "status": 400,
  "message": "Validation failed",
  "timestamp": "2024-01-01T12:00:00",
  "errors": {
    "title": "Title is required",
    "content": "Content is required"
  }
}