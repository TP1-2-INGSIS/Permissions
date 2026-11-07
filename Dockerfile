FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Cache Gradle dependencies before copying application sources.
COPY gradlew gradlew
COPY gradle gradle
COPY buildSrc buildSrc
COPY build.gradle settings.gradle gradle.properties ./
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon

COPY src src
RUN ./gradlew bootJar -x test --no-daemon

# Imagen base de Monitoring: JRE 21 + agente de New Relic (Debian)
FROM ghcr.io/tp1-2-ingsis/monitoring-config:latest
WORKDIR /app

# La imagen base es Debian, no Alpine
RUN groupadd --system spring && useradd --system --gid spring spring
USER spring:spring

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080
# New Relic: NEW_RELIC_LICENSE_KEY se inyecta en runtime; el nombre es propio de este servicio.
ENV NEW_RELIC_APP_NAME=permissions
ENTRYPOINT ["java", "-jar", "app.jar"]
