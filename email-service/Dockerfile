FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="Perfect8"
LABEL version="1.0.0"
LABEL description="Perfect8 Email Service"

RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

COPY target/email-service-1.0.0.jar app.jar

USER spring:spring

EXPOSE 8083

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8083/actuator/health || exit 1

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", \
  "app.jar"]
