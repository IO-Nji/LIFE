# Development Setup Guide

## Prerequisites

- **Java 17+** (OpenJDK recommended)
- **Maven 3.8+**
- **Node.js 18+** with npm/yarn
- **Docker & Docker Compose**
- **Git**
- **IDE** (IntelliJ IDEA, VS Code recommended)

## Local Development Setup

### 1. Clone Repository
```bash
git clone <repository-url>
cd lego-factory
```

### 2. Environment Configuration
Copy the example environment file:
```bash
cp .env.example .env
```

Configure required variables:
```env
POSTGRES_USER=lego_user
POSTGRES_PASSWORD=your_secure_db_password
JWT_SECRET=your_super_secret_jwt_key_that_is_long_and_random
```

### 3. Database Setup
Start PostgreSQL for user service:
```bash
docker-compose up -d postgres-db
```

### 4. Backend Services Development

#### Individual Service Development
Navigate to any service directory:
```bash
cd user-service
mvn spring-boot:run
```

Each service runs on port 8080 by default. For parallel development, configure different ports in `application-dev.properties`.

#### All Services with Docker
```bash
docker-compose up -d --build
```

### 5. Frontend Development
```bash
cd lego-factory-frontend
npm install
npm run dev
```

Frontend runs on `http://localhost:5173` with Vite hot reload.

## Development Workflow

### Code Organization
```
service-name/
├── src/main/java/com/example/service/
│   ├── controller/     # REST endpoints
│   ├── service/        # Business logic
│   ├── repository/     # Data access
│   ├── model/          # Domain entities
│   └── config/         # Configuration classes
├── src/main/resources/
│   ├── application.properties
│   └── application-dev.properties
└── Dockerfile
```

### Testing Strategy
- **Unit Tests**: `mvn test` in each service
- **Integration Tests**: `mvn verify` with TestContainers
- **Frontend Tests**: `npm test` in frontend directory

### Debugging

#### Backend Services
1. Add debug configuration to `application-dev.properties`:
```properties
logging.level.com.example=DEBUG
spring.jpa.show-sql=true
```

2. Enable remote debugging:
```bash
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
```

#### Frontend Debugging
- Browser DevTools for React components
- Vite DevTools for build analysis
- Network tab for API call debugging

### Database Access

#### PostgreSQL (User Service)
```bash
docker exec -it postgres-db psql -U lego_user -d user_db
```

#### H2 Console (Other Services)
When running locally, access H2 console at:
`http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:file:/app/data/service_name_db`
- User: `sa`
- Password: (empty)

## IDE Configuration

### IntelliJ IDEA
1. Import as Maven project
2. Configure Java 17 SDK
3. Enable annotation processing
4. Install Spring Boot plugin

### VS Code
Install recommended extensions:
- Extension Pack for Java
- Spring Boot Extension Pack
- ES7+ React/Redux/React-Native snippets