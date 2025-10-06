#!/bin/bash
# setup-dockerfiles.sh - Skapar Dockerfiles med KORREKTA JAR-namn för Perfect8

set -e

GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${BLUE}Skapar Dockerfiles för alla services med korrekta JAR-namn...${NC}"
echo ""

# Admin Service - admin-service-1.0.0.jar
echo -e "${BLUE}Skapar Dockerfile för admin-service...${NC}"
cat > "admin-service/Dockerfile" <<'EOF'
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="Perfect8"
LABEL version="1.0.0"
LABEL description="Perfect8 Admin Service"

RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

COPY target/admin-service-1.0.0.jar app.jar

USER spring:spring

EXPOSE 8081

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8081/actuator/health || exit 1

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", \
  "app.jar"]
EOF
echo -e "${GREEN}✓ admin-service/Dockerfile (JAR: admin-service-1.0.0.jar)${NC}"

# Blog Service - blog-service-1.0.0.jar
echo -e "${BLUE}Skapar Dockerfile för blog-service...${NC}"
cat > "blog-service/Dockerfile" <<'EOF'
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="Perfect8"
LABEL version="1.0.0"
LABEL description="Perfect8 Blog Service"

RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

COPY target/blog-service-1.0.0.jar app.jar

USER spring:spring

EXPOSE 8082

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8082/actuator/health || exit 1

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", \
  "app.jar"]
EOF
echo -e "${GREEN}✓ blog-service/Dockerfile (JAR: blog-service-1.0.0.jar)${NC}"

# Email Service - email-service-1.0.0.jar
echo -e "${BLUE}Skapar Dockerfile för email-service...${NC}"
cat > "email-service/Dockerfile" <<'EOF'
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
EOF
echo -e "${GREEN}✓ email-service/Dockerfile (JAR: email.service-1.0.0.jar)${NC}"

# Image Service - image-service.jar (OBS! Inget versionsnummer!)
echo -e "${BLUE}Skapar Dockerfile för image-service...${NC}"
cat > "image-service/Dockerfile" <<'EOF'
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="Perfect8"
LABEL version="1.0.0"
LABEL description="Perfect8 Image Service"

RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

COPY target/image-service.jar app.jar

USER spring:spring

EXPOSE 8084

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8084/actuator/health || exit 1

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", \
  "app.jar"]
EOF
echo -e "${GREEN}✓ image-service/Dockerfile (JAR: image-service.jar)${NC}"

# Shop Service - shop-service-1.0.0.jar
echo -e "${BLUE}Skapar Dockerfile för shop-service...${NC}"
cat > "shop-service/Dockerfile" <<'EOF'
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="Perfect8"
LABEL version="1.0.0"
LABEL description="Perfect8 Shop Service"

RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

COPY target/shop-service-1.0.0.jar app.jar

USER spring:spring

EXPOSE 8085

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8085/actuator/health || exit 1

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", \
  "app.jar"]
EOF
echo -e "${GREEN}✓ shop-service/Dockerfile (JAR: shop-service-1.0.0.jar)${NC}"

echo ""
echo -e "${GREEN}════════════════════════════════════════${NC}"
echo -e "${GREEN}Alla Dockerfiles skapade!${NC}"
echo -e "${GREEN}════════════════════════════════════════${NC}"
echo ""

echo -e "${YELLOW}JAR-namn som används:${NC}"
echo "  • admin-service-1.0.0.jar"
echo "  • blog-service-1.0.0.jar"
echo "  • email-service-1.0.0.jar"
echo "  • image-service.jar         ⚠️  (ingen version!)"
echo "  • shop-service-1.0.0.jar"
echo ""

echo -e "${BLUE}Nästa steg:${NC}"
echo "  1. Verifiera Dockerfiles: cat admin-service/Dockerfile"
echo "  2. Bygg images: ./build-images.sh"
echo "  3. Starta systemet: ./start-perfect8.sh"
echo ""
