FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Install curl for health checks
RUN apk add --no-cache curl

# Copy Maven wrapper and project files
COPY mvnw ./
RUN chmod +x mvnw
COPY .mvn/ .mvn/
COPY pom.xml ./
COPY src ./src

# Build application using Maven wrapper
RUN ./mvnw clean package -DskipTests

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

CMD ["java", "-jar", "target/api-gateway-0.0.1-SNAPSHOT.jar"]