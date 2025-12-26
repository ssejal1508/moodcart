# Multi-stage Dockerfile for MoodCart Spring Boot app

# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom first to leverage Docker cache for dependencies
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

# Copy source and build
COPY src ./src
RUN mvn -q -DskipTests package

# ---- Runtime stage ----
FROM eclipse-temurin:17-jre
WORKDIR /app

# Render expects web services to listen on port 10000
ENV PORT=10000

# Copy the built jar from the build stage
COPY --from=build /app/target/moodcart-1.0.0.jar app.jar

EXPOSE 10000

ENTRYPOINT ["java","-jar","/app/app.jar"]
