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

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
