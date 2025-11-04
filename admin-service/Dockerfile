# Multi-stage Dockerfile for Perfect8 Backend Services
# Build stage - builds all services
FROM eclipse-temurin:21-jdk-jammy AS builder

# Install Maven
RUN apt-get update && \
    apt-get install -y maven && \
    rm -rf /var/lib/apt/lists/*

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
FROM eclipse-temurin:21-jre-jammy AS admin-service

# Install wget and curl for healthcheck
RUN apt-get update && \
    apt-get install -y wget curl && \
    rm -rf /var/lib/apt/lists/*

# Create app user
RUN groupadd -r spring && useradd -r -g spring spring

WORKDIR /app
COPY --from=builder /app/admin-service/target/admin-service-*.jar app.jar

# Healthcheck
HEALTHCHECK --interval=30s --timeout=10s --start-period=360s --retries=10 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8081/actuator/health || exit 1

# Switch to app user
USER spring:spring

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]

# Runtime stage for blog-service
FROM eclipse-temurin:21-jre-jammy AS blog-service

# Install wget and curl for healthcheck
RUN apt-get update && \
    apt-get install -y wget curl && \
    rm -rf /var/lib/apt/lists/*

# Create app user
RUN groupadd -r spring && useradd -r -g spring spring

WORKDIR /app
COPY --from=builder /app/blog-service/target/blog-service-*.jar app.jar

# Healthcheck
HEALTHCHECK --interval=30s --timeout=10s --start-period=360s --retries=10 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8082/actuator/health || exit 1

# Switch to app user
USER spring:spring

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]

# Runtime stage for email-service
FROM eclipse-temurin:21-jre-jammy AS email-service

# Install wget and curl for healthcheck
RUN apt-get update && \
    apt-get install -y wget curl && \
    rm -rf /var/lib/apt/lists/*

# Create app user
RUN groupadd -r spring && useradd -r -g spring spring

WORKDIR /app
COPY --from=builder /app/email-service/target/email-service-*.jar app.jar

# Healthcheck
HEALTHCHECK --interval=30s --timeout=10s --start-period=360s --retries=10 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8083/actuator/health || exit 1

# Switch to app user
USER spring:spring

EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]

# Runtime stage for image-service
FROM eclipse-temurin:21-jre-jammy AS image-service

# Install wget and curl for healthcheck
RUN apt-get update && \
    apt-get install -y wget curl && \
    rm -rf /var/lib/apt/lists/*

# Create app user
RUN groupadd -r spring && useradd -r -g spring spring

WORKDIR /app
COPY --from=builder /app/image-service/target/image-service-*.jar app.jar

# Healthcheck
HEALTHCHECK --interval=30s --timeout=10s --start-period=360s --retries=10 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8084/actuator/health || exit 1

# Switch to app user
USER spring:spring

EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]

# Runtime stage for shop-service
FROM eclipse-temurin:21-jre-jammy AS shop-service

# Install wget and curl for healthcheck
RUN apt-get update && \
    apt-get install -y wget curl && \
    rm -rf /var/lib/apt/lists/*

# Create app user
RUN groupadd -r spring && useradd -r -g spring spring

WORKDIR /app
COPY --from=builder /app/shop-service/target/shop-service-*.jar app.jar

# Healthcheck
HEALTHCHECK --interval=30s --timeout=10s --start-period=360s --retries=10 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8085/actuator/health || exit 1

# Switch to app user
USER spring:spring

EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
