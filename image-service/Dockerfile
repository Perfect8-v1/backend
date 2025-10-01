FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
COPY common ./common
RUN mvn install -pl common -am -DskipTests

COPY image-service ./image-service
WORKDIR /app/image-service
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
RUN mkdir -p /app/uploads && chown spring:spring /app/uploads

USER spring:spring

COPY --from=build /app/image-service/target/image-service-*.jar app.jar

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8081/actuator/health || exit 1

EXPOSE 8081
VOLUME ["/app/uploads"]

ENTRYPOINT ["java", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Xmx512m", \
    "-Xms256m", \
    "-jar", \
    "app.jar"]