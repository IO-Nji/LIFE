# Troubleshooting Guide

## Common Deployment Issues

### Docker Build Failures

#### Maven Build Errors
**Symptoms**: Build stage fails with Maven dependency errors
```bash
[ERROR] Failed to execute goal on project user-service: 
Could not resolve dependencies for project com.example:user-service:jar:1.0.0
```

**Solutions**:
1. Check Maven repository connectivity:
```bash
docker run --rm maven:3.8-openjdk-17 mvn dependency:resolve
```

2. Clear Maven cache in CI/CD:
```yaml
# Add to .gitlab-ci.yml build jobs
script:
  - rm -rf ~/.m2/repository
  - docker build --no-cache -t $IMAGE_NAME .
```

3. Verify `pom.xml` dependencies and versions

#### Docker Image Size Issues
**Symptoms**: Build timeouts, slow deployments
**Solution**: Optimize Dockerfile multi-stage builds
```dockerfile
# Bad: Single stage with development tools
FROM openjdk:17-jdk-slim
COPY . .
RUN mvn clean package

# Good: Multi-stage build
FROM openjdk:17-jdk-slim AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

FROM openjdk:17-jre-slim
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### GitLab CI/CD Issues

#### Runner Connection Problems
**Symptoms**: Jobs stuck in "pending" state
**Debug Steps**:
```bash
# Check runner status
sudo gitlab-runner status

# Verify runner registration
sudo gitlab-runner list

# Test runner connectivity
sudo gitlab-runner exec docker test-job
```

**Solutions**:
1. Re-register runner with correct URL and token
2. Check network connectivity between runner and GitLab
3. Verify runner tags match job requirements

#### SSH Deployment Failures
**Symptoms**: Deploy stage fails with SSH connection errors
```bash
ssh: connect to host 192.168.1.100 port 22: Connection refused
```

**Debug Steps**:
```bash
# Test SSH connection manually
ssh -i ~/.ssh/deploy_key deployuser@192.168.1.100

# Check SSH service on server
sudo systemctl status ssh

# Verify SSH key format
ssh-keygen -l -f ~/.ssh/deploy_key
```

**Solutions**:
1. Ensure SSH service is running on target server
2. Verify SSH key is in correct format (RSA recommended)
3. Check firewall rules allow SSH (port 22)
4. Confirm `deployuser` exists and has proper permissions

### Application Runtime Issues

#### Service Discovery Problems
**Symptoms**: Services can't communicate with each other
```bash
java.net.ConnectException: Connection refused: http://user-service:8080
```

**Debug Steps**:
```bash
# Check Docker network
docker network ls
docker network inspect lego-factory-app_app-network

# Verify service names
docker-compose ps

# Test inter-service connectivity
docker exec api-gateway ping user-service
```

**Solutions**:
1. Ensure all services are on same Docker network
2. Use Docker Compose service names for hostnames
3. Check service startup order dependencies

#### Database Connection Issues

##### PostgreSQL Connection Failures
**Symptoms**: User service fails to start
```bash
org.postgresql.util.PSQLException: Connection to localhost:5432 refused
```

**Debug Steps**:
```bash
# Check PostgreSQL container
docker logs postgres-db

# Test database connection
docker exec postgres-db psql -U lego_user -d user_db -c "\dt"

# Verify environment variables
docker exec user-service env | grep POSTGRES
```

**Solutions**:
1. Ensure PostgreSQL container starts before dependent services
2. Verify database credentials in `.env` file
3. Check database initialization scripts

##### H2 Database Issues
**Symptoms**: Service fails to persist data
```bash
org.h2.jdbc.JdbcSQLException: Database "/app/data/inventory_db" not found
```

**Debug Steps**:
```bash
# Check volume mounts
docker inspect inventory-service | grep Mounts

# Verify data directory permissions
docker exec inventory-service ls -la /app/data/

# Check H2 database files
docker exec inventory-service find /app/data -name "*.db"
```

**Solutions**:
1. Ensure data volumes are properly mounted
2. Check directory permissions (user `1000` for container)
3. Verify H2 connection URL format

### Performance Issues

#### Slow Application Response
**Symptoms**: Frontend requests timeout, slow page loads

**Debug Steps**:
```bash
# Check container resource usage
docker stats

# Monitor database connections
docker exec postgres-db psql -U lego_user -d user_db -c "SELECT * FROM pg_stat_activity;"

# Check application logs for slow queries
docker logs user-service | grep -i "slow"
```

**Solutions**:
1. Increase container memory limits
2. Add database indexes for frequently queried fields
3. Implement connection pooling optimization
4. Enable query caching

#### High Memory Usage
**Symptoms**: Services getting killed (OOMKilled)
```bash
docker logs api-gateway
# Shows: Killed (exit code 137)
```

**Solutions**:
1. Increase Docker memory limits:
```yaml
# docker-compose.yml
services:
  api-gateway:
    deploy:
      resources:
        limits:
          memory: 1G
        reservations:
          memory: 512M
```

2. Optimize JVM heap settings:
```dockerfile
ENTRYPOINT ["java", "-Xmx512m", "-Xms256m", "-jar", "app.jar"]
```

### Security Issues

#### JWT Token Problems
**Symptoms**: Authentication failures, token validation errors
```bash
io.jsonwebtoken.security.SignatureException: JWT signature does not match
```

**Debug Steps**:
```bash
# Check JWT secret configuration
docker exec user-service env | grep JWT_SECRET

# Verify token format in browser dev tools
# Check Authorization header in network tab
```

**Solutions**:
1. Ensure JWT secret is consistently configured across services
2. Verify secret is sufficiently long (64+ characters)
3. Check token expiration and refresh logic

#### CORS Issues
**Symptoms**: Frontend can't call backend APIs
```javascript
Access to XMLHttpRequest at 'http://api-gateway:8080/api/users' 
from origin 'http://localhost:3000' has been blocked by CORS policy
```

**Solutions**:
1. Configure CORS in API Gateway:
```java
@Configuration
public class CorsConfig {
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://your-domain.com"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

2. Use nginx proxy for production deployments

## Monitoring and Logging

### Centralized Logging
```bash
# View all service logs
docker-compose logs -f

# View specific service logs
docker logs -f lego-factory-app_user-service_1

# Search logs for errors
docker-compose logs | grep -i error
```

### Health Check Endpoints
Add to each service:
```java
@RestController
public class HealthController {
    @GetMapping("/actuator/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}
```

### Performance Monitoring
```bash
# Monitor resource usage
docker stats --no-stream

# Check database performance
docker exec postgres-db psql -U lego_user -d user_db -c "
SELECT query, mean_exec_time, calls 
FROM pg_stat_statements 
ORDER BY mean_exec_time DESC LIMIT 10;"
```

## Emergency Procedures

### Quick Rollback
```bash
# Stop current deployment
docker-compose down

# Revert to previous image tags
git checkout HEAD~1 docker-compose.yml

# Redeploy previous version
docker-compose up -d
```

### Service Recovery
```bash
# Restart specific service
docker-compose restart user-service

# Rebuild and restart service
docker-compose up -d --build user-service

# Scale service instances
docker-compose up -d --scale api-gateway=2
```

### Database Recovery
```bash
# Backup current data
docker exec postgres-db pg_dump -U lego_user user_db > backup.sql

# Restore from backup
docker exec -i postgres-db psql -U lego_user user_db < backup.sql
```