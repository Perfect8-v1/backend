# Admin Service Dockerfile with wget for healthcheck
FROM eclipse-temurin:21-jre-alpine

# Metadata
LABEL maintainer="Perfect8"
LABEL version="1.0"
LABEL description="Perfect8 Admin Service"

# Install wget for healthcheck
RUN apk add --no-cache wget

# Create app user
RUN addgroup -S spring && adduser -S spring -G spring

# Working directory
WORKDIR /app

# Copy JAR file
COPY target/admin-service-1.0.0.jar app.jar

# Switch to app user
USER spring:spring

# Expose port
EXPOSE 8081

# Start application
ENTRYPOINT ["java", "-jar", "app.jar"]
