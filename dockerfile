# =========================
# STAGE 1: BUILD
# =========================
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy pom để cache dependency
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source
COPY src src

# Build jar
RUN mvn clean package -DskipTests -B


# =========================
# STAGE 2: RUN
# =========================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy jar đã build
COPY --from=builder /app/target/*.jar app.jar

# Expose port Spring Boot
EXPOSE 8080

# Run app
ENTRYPOINT ["java", "-jar", "app.jar"]
