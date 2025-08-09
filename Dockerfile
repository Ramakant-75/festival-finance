# Use Java 17 Temurin base image
FROM eclipse-temurin:17-jdk-alpine

# Set the working directory in container
WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies (layer caching)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build the application
RUN ./mvnw clean package -DskipTests

# Expose the port your app runs on
EXPOSE 8090

# Run the Spring Boot app
CMD ["java", "-jar", "target/societyfest-0.0.1-SNAPSHOT.jar"]
