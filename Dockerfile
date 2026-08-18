
# --- Build Stage ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Cache Maven dependencies separately to optimize Docker layer caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build final package
COPY src ./src
RUN mvn package -DskipTests -B

# --- Production Run Stage ---
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# Security: Create and switch to a non-privileged user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser:appgroup

# Copy built artifact from the build stage
COPY --from=build --chown=appuser:appgroup /app/target/*.jar app.jar

EXPOSE 8086

ENTRYPOINT ["java", "-jar", "app.jar"]