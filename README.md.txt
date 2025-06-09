// README.md

# Perfect8 Blog Microservice

A microservice-based blog application built with Spring Boot, featuring separate services for blog content and image storage.

## Architecture

- **Blog Service**: Handles posts, users, authentication, and authorization
- **Image Service**: Manages image storage in MySQL BLOB format
- **Two MySQL Containers**: Separate databases for blog data and images

## Features

- JWT Authentication
- Three user roles: ADMIN, WRITER, READER
- Post management with rich content
- Image storage in database BLOBs
- Link management
- RESTful APIs
- Docker containerization
- GitLab CI/CD pipeline

## Prerequisites

- Java 17+
- Docker & Docker Compose
- Maven 3.8+
- MySQL 8.0+

## Quick Start

1. Clone the repository
2. Copy `.env.example` to `.env` and configure your environment variables
3. Run with Docker Compose:

```bash
docker-compose up -d