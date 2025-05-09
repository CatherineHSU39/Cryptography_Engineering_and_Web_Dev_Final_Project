# auth/Dockerfile

FROM openjdk:17-jdk-slim

# Metadata
LABEL maintainer="a313706026.mg13@nycu.edu.tw"

# Set working directory
WORKDIR /app

# Copy build artifact (assuming using Maven or Gradle)
COPY ./target/*.jar ./app.jar

# Expose default Spring Boot port
EXPOSE 8081

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]