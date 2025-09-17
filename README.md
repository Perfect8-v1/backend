# 📰 Blog Microservices - Java Spring Boot

This project implements a secure, containerized blog application with image handling using Java and Spring Boot.

## 🔧 Technologies
- Spring Boot 3.4+
- Spring Security (JWT)
- Docker & Docker Compose
- MySQL (MariaDB)
- RESTful communication between services

## 📁 Project Structure
- `blogg-service/` - blog posts, users, roles, JWT auth, image references
- `image-service/` - stores image BLOBs, fetches via REST
- `docker-compose.yml` - microservices orchestration
- `.env` - secrets/config (NOT committed to Git)

## 🚀 How to Run
```bash
docker-compose build
docker-compose up
```

## 📦 Sample Endpoints
See deployment/testing section for curl examples

## 🧪 Local Development
```bash
cd blogg-service && mvn spring-boot:run
```

## 🔐 Security
- JWT authentication & authorization
- Role-based access (WRITER, READER, ADMIN)

## 🧩 Future Work
- Add Swagger UI docs*
- Add Disqus for comments
- CI/CD to production Kubernetes or ECS

*We have noticed that Intellij only has support for Thymeleaf and Swagger when user have an Intellij Ultimate plan.
Because of this we have avoided importing these libraries into out project.