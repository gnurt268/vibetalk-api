# ========== Stage 1: Build ==========
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

# Copy Maven wrapper + pom first (layer caching)
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x ./mvnw && ./mvnw dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# ========== Stage 2: Run ==========
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy JAR from builder
COPY --from=builder /app/target/vibetalk-api-0.0.1-SNAPSHOT.jar app.jar

# Set timezone
ENV TZ=Asia/Ho_Chi_Minh

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]