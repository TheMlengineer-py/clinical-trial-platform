# ── Stage 1: Build ──────────────────────────────────────────────────────────
# Use the official Maven image with Java 21 to compile and package the app.
# We cache the dependency download step separately so rebuilds are fast
# when only source files change (not pom.xml).
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom.xml first — Maven will download all dependencies here.
# Docker caches this layer until pom.xml changes.
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Now copy source and build the fat JAR, skipping tests
# (tests run in CI via GitHub Actions, not inside Docker build).
COPY src ./src
RUN mvn package -DskipTests -B

# ── Stage 2: Runtime ────────────────────────────────────────────────────────
# Use a minimal JRE-only Alpine image — no Maven, no source code.
# Final image is ~100MB vs ~400MB if we used the build stage directly.
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy only the compiled JAR from the build stage.
COPY --from=build /app/target/*.jar app.jar

# Render.com maps external port 8080 by default.
EXPOSE 8080

# Spring Boot embeds Tomcat — no external app server needed.
ENTRYPOINT ["java", "-jar", "app.jar"]
