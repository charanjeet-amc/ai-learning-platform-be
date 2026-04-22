# Backend Dockerfile — multi-stage build for Railway / container deployments
FROM maven:3.9-eclipse-temurin-21-alpine AS build

ARG CACHE_BUST=1
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn clean package -DskipTests -q

# ── Runtime ──────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app
# DejaVu fonts required by OpenHTML-to-PDF for PDF certificate generation
RUN apk add --no-cache fontconfig ttf-dejavu && fc-cache -fv

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
COPY --from=build /app/target/*.jar app.jar
RUN chown appuser:appgroup app.jar
USER appuser

EXPOSE 8080

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
