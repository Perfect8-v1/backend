# Shop Service Dockerfile with HEALTHCHECK
FROM eclipse-temurin:21-jre-jammy

# Metadata
LABEL maintainer="Perfect8"
LABEL version="1.0"
LABEL description="Perfect8 Shop Service"

# Install wget and curl for healthcheck
RUN apt-get update && apt-get install -y wget curl && rm -rf /var/lib/apt/lists/*

# Create app user
RUN groupadd -r spring && useradd -r -g spring spring

# Working directory
WORKDIR /app

# Copy JAR file - från service-mappen (inte target)
COPY shop-service-1.0.0.jar app.jar

# Healthcheck - retry every 30s, wait 6 minutes for startup
HEALTHCHECK --interval=30s --timeout=10s --start-period=360s --retries=10 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8085/actuator/health || exit 1

# Switch to app user
USER spring:spring

# Expose port
EXPOSE 8085

# Start application
ENTRYPOINT ["java", "-jar", "app.jar"]
