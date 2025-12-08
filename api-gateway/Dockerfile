# Stage 1: Build the Spring Boot application
        FROM openjdk:17-jdk-slim AS build
        WORKDIR /app
        COPY .mvn/ .mvn
        COPY mvnw pom.xml ./
        RUN ./mvnw dependency:go-offline # Download dependencies first
        COPY src ./src
        RUN ./mvnw package -Dmaven.test.skip=true # Build the application

        # Stage 2: Create the final image
        FROM openjdk:17-jre-slim
        WORKDIR /app
        COPY --from=build /app/target/*.jar app.jar
        # Expose the port your Spring Boot app listens on
        EXPOSE 8080
        ENTRYPOINT ["java", "-jar", "app.jar"]