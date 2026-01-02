# Use Eclipse Temurin JRE 21 (Alpine for smaller size)
FROM eclipse-temurin:21-jre-alpine

# Set working directory
WORKDIR /app

# Install wget for healthcheck
RUN apk add --no-cache wget

# Copy the JAR file (built by Maven and copied by copy-jars.sh)
COPY api-gateway-1.0.0.jar /app/api-gateway.jar

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=10s --timeout=5s --start-period=60s --retries=30 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "/app/api-gateway.jar"]