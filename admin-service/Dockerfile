# Admin Service Dockerfile with HEALTHCHECK
FROM eclipse-temurin:21-jre-alpine

# Metadata
LABEL maintainer="Perfect8"
LABEL version="1.0"
LABEL description="Perfect8 Admin Service"

# Install wget for healthcheck
#RUN apk add --no-cache wget

# Create app user
RUN addgroup -S spring && adduser -S spring -G spring

# Working directory
WORKDIR /app

# Copy JAR file
COPY target/admin-service-1.0.0.jar app.jar

# Healthcheck - retry every 30s, wait 6 minutes for startup
#HEALTHCHECK --interval=30s --timeout=10s --start-period=360s --retries=10 \
#    CMD wget --no-verbose --tries=1 --spider http://localhost:8081/actuator/health || exit 1

# Switch to app user
USER spring:spring

# Expose port
EXPOSE 8081

# Start application
ENTRYPOINT ["java", "-jar", "app.jar"]
