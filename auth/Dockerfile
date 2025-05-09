# auth-server/Dockerfile

FROM openjdk:17-jdk-slim

# Metadata
LABEL maintainer="a313706026@nycu.edu.tw"

# Set working directory
WORKDIR /app

# Copy built JAR file
COPY ./target/*.jar ./app.jar

# Expose default Spring Boot port
EXPOSE 8081

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]