FROM openjdk:17-jdk-slim

WORKDIR /app

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# Create data directory for H2 database
RUN mkdir -p /app/data && chmod 755 /app/data

# Copy Maven wrapper and pom.xml
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build application
RUN ./mvnw clean package -DskipTests && \
    mv target/*.jar app.jar

# Expose SIMAL Integration Service port
EXPOSE 8016

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8016/actuator/health || exit 1

# Volume for H2 database persistence
VOLUME ["/app/data"]

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]