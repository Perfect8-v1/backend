# Multi-stage Dockerfile for Perfect8 Backend Services
# Build stage - builds all services
FROM eclipse-temurin:17-jdk-alpine AS builder

# Install Maven
RUN apk add --no-cache maven

# Set working directory
WORKDIR /app

# Copy parent pom and common module first (for better layer caching)
COPY pom.xml .
COPY common/ ./common/

# Copy all service modules
COPY admin-service/ ./admin-service/
COPY blog-service/ ./blog-service/
COPY email-service/ ./email-service/
COPY image-service/ ./image-service/
COPY shop-service/ ./shop-service/

# Build all modules
RUN mvn clean package -DskipTests

# Runtime stage for admin-service
FROM eclipse-temurin:17-jre-alpine AS admin-service
WORKDIR /app
COPY --from=builder /app/admin-service/target/admin-service-*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]

# Runtime stage for blog-service
FROM eclipse-temurin:17-jre-alpine AS blog-service
WORKDIR /app
COPY --from=builder /app/blog-service/target/blog-service-*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]

# Runtime stage for email-service
FROM eclipse-temurin:17-jre-alpine AS email-service
WORKDIR /app
COPY --from=builder /app/email-service/target/email-service-*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]

# Runtime stage for image-service
FROM eclipse-temurin:17-jre-alpine AS image-service
WORKDIR /app
COPY --from=builder /app/image-service/target/image-service-*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]

# Runtime stage for shop-service
FROM eclipse-temurin:17-jre-alpine AS shop-service
WORKDIR /app
COPY --from=builder /app/shop-service/target/shop-service-*.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]