# Simple Dockerfile để test
FROM openjdk:21

# Install Maven
COPY . /app
WORKDIR /app

# Give permission to mvnw
RUN chmod +x ./mvnw

# Build
RUN ./mvnw clean package -DskipTests

# Run
EXPOSE 8080
CMD ["java", "-jar", "target/vibetalk-api-0.0.1-SNAPSHOT.jar"]