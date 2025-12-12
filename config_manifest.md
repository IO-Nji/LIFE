# Application Configuration Manifest for LEGO Sample Factory

This document defines all configuration variables (keys, port numbers, user credentials, JWT secrets, etc.) used by the LEGO Sample Factory microservices application. These variables are consumed by Spring Boot microservices and the React frontend.

**IMPORTANT:** This file contains placeholder values only.

- Actual local development values should be stored in the **`.env` file at the project root (and added to `.gitignore`).**
- For Docker deployment, these values will be passed as environment variables to containers.

---

## I. Global Application Settings & Shared Secrets

These variables apply across multiple services or define core application behavior.

| Variable Name             | Type      | Description                                                                 | Default/Example Value                 | Used By                                  |
| :------------------------ | :-------- | :-------------------------------------------------------------------------- | :------------------------------------ | :--------------------------------------- |
| `APP_NAME`                | String    | The overall application name.                                               | `lego-sample-factory`                 | All services (for logging, context)      |
| `APP_ENV`                 | String    | The current environment (e.g., `development`, `test`, `production`).        | `development`                         | All services (for logging, conditional behavior) |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | String | JPA DDL generation strategy. Options: `none`, `update`, `create`, `create-drop` | `create-drop` (for dev), `none` (for prod) | All Spring Boot services using JPA       |
| `JWT_SECRET`              | String    | **Secret:** Key used to sign and verify JWTs. Must be long, random, and kept secret. | `MySecretKeyForJWTTokenGeneration2024AtLeast32Characters` | `user-service`, `api-gateway` (for validation) |
| `JWT_EXPIRATION`          | String    | JWT token expiration time in ISO-8601 duration format.                      | `PT1H` (1 hour)                       | `user-service`, `api-gateway`            |
| `API_KEY_SIMAL_INTEGRATION` | String    | **Secret:** API key for `simal-integration-service` to authenticate with SimAL.Scheduler. | `simal_integration_api_key_123`       | `simal-integration-service`              |
| `SPRING_PROFILES_ACTIVE`  | String    | Active Spring Boot profiles for environment-specific configuration.         | `development`                         | All services                             |
| `SPRING_CONFIG_IMPORT`    | String    | Spring Boot config import directive for loading external configuration.     | `optional:file:.env`                 | All services                             |

---

## II. Database Configuration

### PostgreSQL Database (`postgres-db` service)

| Variable Name             | Type      | Description                                                                 | Default/Example Value                 | Used By                                  |
| :------------------------ | :-------- | :-------------------------------------------------------------------------- | :------------------------------------ | :--------------------------------------- |
| `POSTGRES_DB_NAME`        | String    | Name of the PostgreSQL database.                                            | `lego_factory_auth`                   | `postgres-db`, `user-service`            |
| `POSTGRES_USER`           | String    | Username for PostgreSQL database access.                                    | `lego_user`                           | `postgres-db`, `user-service`            |
| `POSTGRES_PASSWORD`       | String    | **Secret:** Password for the PostgreSQL user.                               | `my_strong_local_postgres_password`   | `postgres-db`, `user-service`            |
| `POSTGRES_PORT`           | Number    | Internal port for PostgreSQL.                                               | `5432`                                | `postgres-db`, `user-service`            |
| `SPRING_DATASOURCE_URL`   | String    | Full JDBC URL for PostgreSQL connection (referencing `postgres-db` host).   | `jdbc:postgresql://postgres-db:5432/lego_factory_auth` | `user-service`                           |

### H2 Database Configuration (per microservice)

| Variable Name             | Type      | Description                                                                 | Default/Example Value                 | Used By                                  |
| :------------------------ | :-------- | :-------------------------------------------------------------------------- | :------------------------------------ | :--------------------------------------- |
| `H2_DB_USER_PATH`         | String    | JDBC URL for `user-service`'s H2 database.                                  | `jdbc:h2:mem:lego_factory_auth;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE` | `user-service`                           |
| `H2_DB_MASTERDATA_PATH`   | String    | JDBC URL for `masterdata-service`'s H2 database.                           | `jdbc:h2:mem:masterdata_db;MODE=PostgreSQL;DB_CLOSE_DELAY=-1` | `masterdata-service`                     |
| `H2_DB_INVENTORY_PATH`    | String    | JDBC URL for `inventory-service`'s H2 database.                            | `jdbc:h2:mem:inventory_db;MODE=PostgreSQL;DB_CLOSE_DELAY=-1` | `inventory-service`                      |
| `H2_DB_ORDER_PROCESSING_PATH` | String  | JDBC URL for `order-processing-service`'s H2 database.                     | `jdbc:h2:mem:order_processing_db;MODE=PostgreSQL;DB_CLOSE_DELAY=-1` | `order-processing-service`               |
| `H2_DB_SIMAL_INTEGRATION_PATH` | String | JDBC URL for `simal-integration-service`'s H2 database.                    | `jdbc:h2:mem:simal_integration_db;MODE=PostgreSQL;DB_CLOSE_DELAY=-1` | `simal-integration-service`              |

### Database Connection Pooling

| Variable Name             | Type      | Description                                                                 | Default/Example Value                 | Used By                                  |
| :------------------------ | :-------- | :-------------------------------------------------------------------------- | :------------------------------------ | :--------------------------------------- |
| `DB_CONNECTION_POOL_SIZE` | Number    | Maximum number of database connections in the pool.                         | `10`                                  | All services with databases              |
| `DB_CONNECTION_TIMEOUT`   | Number    | Connection timeout in milliseconds.                                         | `20000` (20 seconds)                  | All services with databases              |
| `DB_IDLE_TIMEOUT`         | Number    | Connection idle timeout in milliseconds.                                    | `600000` (10 minutes)                 | All services with databases              |
| `DB_MAX_LIFETIME`         | Number    | Maximum connection lifetime in milliseconds.                                | `1800000` (30 minutes)                | All services with databases              |

---

## III. Security Configuration

| Variable Name             | Type      | Description                                                                 | Default/Example Value                 | Used By                                  |
| :------------------------ | :-------- | :-------------------------------------------------------------------------- | :------------------------------------ | :--------------------------------------- |
| `BCRYPT_STRENGTH`         | Number    | BCrypt password hashing strength (4-31).                                    | `10`                                  | `user-service`                           |
| `SESSION_TIMEOUT`         | String    | User session timeout in ISO-8601 duration format.                          | `PT30M` (30 minutes)                  | `user-service`                           |
| `MAX_LOGIN_ATTEMPTS`      | Number    | Maximum failed login attempts before account lockout.                       | `5`                                   | `user-service`                           |
| `LOCKOUT_DURATION`        | String    | Account lockout duration in ISO-8601 duration format.                       | `PT15M` (15 minutes)                  | `user-service`                           |

---

## IV. Microservice-Specific Settings

### `user-service`

| Variable Name             | Type      | Description                                                                 | Default/Example Value                 | Used By                                  |
| :------------------------ | :-------- | :-------------------------------------------------------------------------- | :------------------------------------ | :--------------------------------------- |
| `USER_SERVICE_PORT`       | Number    | Internal port for `user-service`.                                           | `8012`                                | `user-service`, `api-gateway`            |
| `ADMIN_USERNAME`          | String    | Default admin username for initial setup.                                   | `legoAdmin`                           | `user-service` (initialization)          |
| `ADMIN_PASSWORD`          | String    | Default admin password for initial setup.                                   | `legoPass`                            | `user-service` (initialization)          |
| `WAREHOUSE_USERNAME`      | String    | Default warehouse operator username.                                         | `warehouseOperator`                   | `user-service` (initialization)          |
| `WAREHOUSE_PASSWORD`      | String    | Default warehouse operator password.                                         | `warehousePass`                       | `user-service` (initialization)          |
| `MODULES_SUPERMARKET_USERNAME` | String | Default modules supermarket operator username.                               | `modulesSupermarketOp`                | `user-service` (initialization)          |
| `MODULES_SUPERMARKET_PASSWORD` | String | Default modules supermarket operator password.                               | `modulesPass`                         | `user-service` (initialization)          |

### `masterdata-service`

| Variable Name             | Type      | Description                                                                 | Default/Example Value                 | Used By                                  |
| :------------------------ | :-------- | :-------------------------------------------------------------------------- | :------------------------------------ | :--------------------------------------- |
| `MASTERDATA_SERVICE_PORT` | Number    | Internal port for `masterdata-service`.                                     | `8013`                                | `masterdata-service`, `api-gateway`      |

### `inventory-service`

| Variable Name             | Type      | Description                                                                 | Default/Example Value                 | Used By                                  |
| :------------------------ | :-------- | :-------------------------------------------------------------------------- | :------------------------------------ | :--------------------------------------- |
| `INVENTORY_SERVICE_PORT`  | Number    | Internal port for `inventory-service`.                                      | `8014`                                | `inventory-service`, `api-gateway`       |
| `INVENTORY_LOW_STOCK_THRESHOLD` | Number | Low stock threshold for inventory alerts.                                   | `10`                                  | `inventory-service`                      |

### `order-processing-service`

| Variable Name             | Type      | Description                                                                 | Default/Example Value                 | Used By                                  |
| :------------------------ | :-------- | :-------------------------------------------------------------------------- | :------------------------------------ | :--------------------------------------- |
| `ORDER_PROCESSING_SERVICE_PORT` | Number | Internal port for `order-processing-service`.                               | `8015`                                | `order-processing-service`, `api-gateway` |
| `MAX_ORDER_ITEMS`         | Number    | Maximum number of items allowed per order.                                  | `100`                                 | `order-processing-service`               |
| `ORDER_PROCESSING_BATCH_SIZE` | Number  | Batch size for order processing operations.                                 | `50`                                  | `order-processing-service`               |
| `FACTORY_SIMULATION_SPEED` | Number   | Factory simulation speed multiplier.                                        | `1.0`                                 | `order-processing-service`               |

### `simal-integration-service`

| Variable Name             | Type      | Description                                                                 | Default/Example Value                 | Used By                                  |
| :------------------------ | :-------- | :-------------------------------------------------------------------------- | :------------------------------------ | :--------------------------------------- |
| `SIMAL_INTEGRATION_SERVICE_PORT` | Number | Internal port for `simal-integration-service`.                              | `8085`                                | `simal-integration-service`, `api-gateway` |
| `SIMAL_SCHEDULER_BASE_URL` | String   | Base URL for the external SimAL.Scheduler API.                              | `http://localhost:9000/api/simal`     | `simal-integration-service`              |

### `api-gateway`

| Variable Name             | Type      | Description                                                                 | Default/Example Value                 | Used By                                  |
| :------------------------ | :-------- | :-------------------------------------------------------------------------- | :------------------------------------ | :--------------------------------------- |
| `API_GATEWAY_PORT`        | Number    | Internal port for the `api-gateway`.                                        | `8011`                                | `api-gateway`, `nginx`                   |
| `API_GATEWAY_CORS_ALLOWED_ORIGINS` | String | Comma-separated list of allowed origins for CORS.                     | `http://localhost:3000,http://localhost:80,http://localhost,http://localhost:5173` | `api-gateway`                            |
| `API_GATEWAY_DEBUG_LOGGING_LEVEL` | String | Sets logging level for Spring Cloud Gateway for debugging.              | `INFO` (`DEBUG` for verbose)          | `api-gateway`                            |
| `EUREKA_CLIENT_ENABLED`   | Boolean   | Enable/disable Eureka service discovery.                                    | `false` (disabled for development)    | `api-gateway`, all services              |
| `SPRING_CLOUD_DISCOVERY_ENABLED` | Boolean | Enable/disable Spring Cloud service discovery.                        | `false` (disabled for development)    | `api-gateway`, all services              |
| `API_RATE_LIMIT_REQUESTS` | Number    | Number of requests allowed per rate limit window.                           | `100`                                 | `api-gateway`                            |
| `API_RATE_LIMIT_WINDOW`   | String    | Rate limit time window in ISO-8601 duration format.                         | `PT1M` (1 minute)                     | `api-gateway`                            |

### `frontend` (React app)

| Variable Name             | Type      | Description                                                                 | Default/Example Value                 | Used By                                  |
| :------------------------ | :-------- | :-------------------------------------------------------------------------- | :------------------------------------ | :--------------------------------------- |
| `VITE_API_GATEWAY_URL`    | String    | Base URL for the frontend to make API calls to the gateway.                 | `http://localhost:8011`               | `frontend` (React app)                   |
| `FRONTEND_SERVE_PORT`     | Number    | Internal Nginx port serving the React static files.                         | `80`                                  | `frontend` (Dockerfile)                  |

---

## V. External Services Configuration

| Variable Name             | Type      | Description                                                                 | Default/Example Value                 | Used By                                  |
| :------------------------ | :-------- | :-------------------------------------------------------------------------- | :------------------------------------ | :--------------------------------------- |
| `EXTERNAL_API_TIMEOUT`    | Number    | Timeout for external API calls in milliseconds.                             | `30000` (30 seconds)                  | All services making external calls       |
| `EXTERNAL_API_RETRY_COUNT` | Number   | Number of retry attempts for failed external API calls.                     | `3`                                   | All services making external calls       |

---

## VI. Logging Configuration

| Variable Name             | Type      | Description                                                                 | Default/Example Value                 | Used By                                  |
| :------------------------ | :-------- | :-------------------------------------------------------------------------- | :------------------------------------ | :--------------------------------------- |
| `LOGGING_LEVEL_IO_LIFE`   | String    | Logging level for application packages.                                     | `DEBUG` (for dev), `INFO` (for prod) | All services                             |
| `LOG_LEVEL_ROOT`          | String    | Root logger level.                                                          | `INFO`                                | All services                             |
| `LOG_LEVEL_SPRING`        | String    | Spring framework logging level.                                             | `INFO`                                | All services                             |
| `LOG_LEVEL_HIBERNATE`     | String    | Hibernate/JPA logging level.                                                | `WARN`                                | All services with JPA                    |
| `LOG_FILE_PATH`           | String    | Directory path for log files.                                               | `/var/log/lego-factory`               | All services (for production)            |
| `LOG_FILE_MAX_SIZE`       | String    | Maximum size for individual log files.                                      | `10MB`                                | All services                             |
| `LOG_FILE_MAX_HISTORY`    | Number    | Number of log files to keep in rotation.                                    | `30`                                  | All services                             |

---

## VII. Spring Boot Common Configuration

| Variable Name             | Type      | Description                                                                 | Default/Example Value                 | Used By                                  |
| :------------------------ | :-------- | :-------------------------------------------------------------------------- | :------------------------------------ | :--------------------------------------- |
| `SPRING_JPA_SHOW_SQL`     | Boolean   | Show SQL queries in logs for debugging.                                     | `true` (for dev), `false` (for prod) | All services with JPA                    |
| `SPRING_H2_CONSOLE_ENABLED` | Boolean | Enable H2 database console access.                                          | `true` (for dev), `false` (for prod) | Services using H2                        |
| `SPRING_H2_CONSOLE_PATH`  | String    | Path for H2 console access.                                                 | `/h2-console`                         | Services using H2                        |
| `MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE` | String | Actuator endpoints to expose.                                   | `health,info,metrics`                 | All services with Actuator               |
| `MANAGEMENT_ENDPOINT_HEALTH_SHOW_DETAILS` | String | Health endpoint detail level.                                     | `always` (for dev), `when-authorized` (for prod) | All services with Actuator               |

---

## VIII. Infrastructure & Deployment Configuration

### Docker Configuration

| Variable Name             | Type      | Description                                                                 | Default/Example Value                 | Used By                                  |
| :------------------------ | :-------- | :-------------------------------------------------------------------------- | :------------------------------------ | :--------------------------------------- |
| `DOCKER_RESTART_POLICY`   | String    | Docker container restart policy.                                            | `unless-stopped`                      | Docker Compose                           |
| `DOCKER_MEMORY_LIMIT`     | String    | Memory limit for containers.                                                | `512m`                                | Docker Compose                           |
| `DOCKER_CPU_LIMIT`        | Number    | CPU limit for containers.                                                   | `1.0`                                 | Docker Compose                           |

### Health Check Configuration

| Variable Name             | Type      | Description                                                                 | Default/Example Value                 | Used By                                  |
| :------------------------ | :-------- | :-------------------------------------------------------------------------- | :------------------------------------ | :--------------------------------------- |
| `HEALTH_CHECK_INTERVAL`   | String    | Interval between health checks.                                             | `30s`                                 | Docker Compose                           |
| `HEALTH_CHECK_TIMEOUT`    | String    | Timeout for health check requests.                                          | `10s`                                 | Docker Compose                           |
| `HEALTH_CHECK_RETRIES`    | Number    | Number of health check retries before marking unhealthy.                    | `3`                                   | Docker Compose                           |

### Nginx Root Proxy

| Variable Name             | Type      | Description                                                                 | Default/Example Value                 | Used By                                  |
| :------------------------ | :-------- | :-------------------------------------------------------------------------- | :------------------------------------ | :--------------------------------------- |
| `NGINX_ROOT_PROXY_PORT`   | Number    | External port for the main Nginx proxy (exposed to host).                   | `80`                                  | `nginx` (docker-compose)                 |

---

## IX. Business Logic & Feature Configuration

| Variable Name             | Type      | Description                                                                 | Default/Example Value                 | Used By                                  |
| :------------------------ | :-------- | :-------------------------------------------------------------------------- | :------------------------------------ | :--------------------------------------- |
| `FEATURE_ASSEMBLY_TRACKING` | Boolean  | Enable/disable assembly tracking functionality.                             | `true`                                | All relevant services                    |
| `FEATURE_REAL_TIME_INVENTORY` | Boolean | Enable/disable real-time inventory updates.                                 | `true`                                | `inventory-service`                      |
| `FEATURE_ADVANCED_REPORTING` | Boolean | Enable/disable advanced reporting features.                                 | `false`                               | All services                             |
| `FEATURE_SIMAL_INTEGRATION` | Boolean  | Enable/disable SimAL integration functionality.                             | `true`                                | `simal-integration-service`              |

---

## X. File Handling & Storage Configuration

| Variable Name             | Type      | Description                                                                 | Default/Example Value                 | Used By                                  |
| :------------------------ | :-------- | :-------------------------------------------------------------------------- | :------------------------------------ | :--------------------------------------- |
| `FILE_UPLOAD_MAX_SIZE`    | String    | Maximum file upload size.                                                   | `10MB`                                | All services handling file uploads       |
| `FILE_UPLOAD_TEMP_DIR`    | String    | Temporary directory for file uploads.                                       | `/tmp/lego-factory-uploads`           | All services handling file uploads       |
| `FILE_STORAGE_PATH`       | String    | Permanent storage path for uploaded files.                                  | `/var/lego-factory/files`             | All services handling file storage       |

---

## XI. Caching & Performance Configuration

| Variable Name             | Type      | Description                                                                 | Default/Example Value                 | Used By                                  |
| :------------------------ | :-------- | :-------------------------------------------------------------------------- | :------------------------------------ | :--------------------------------------- |
| `CACHE_ENABLED`           | Boolean   | Enable/disable application-level caching.                                   | `true`                                | All services                             |
| `CACHE_TTL`               | String    | Cache time-to-live in ISO-8601 duration format.                             | `PT1H` (1 hour)                       | All services with caching               |
| `CACHE_MAX_SIZE`          | Number    | Maximum number of cache entries.                                            | `1000`                                | All services with caching               |

---

## XII. Monitoring & Observability Configuration

| Variable Name             | Type      | Description                                                                 | Default/Example Value                 | Used By                                  |
| :------------------------ | :-------- | :-------------------------------------------------------------------------- | :------------------------------------ | :--------------------------------------- |
| `METRICS_ENABLED`         | Boolean   | Enable/disable metrics collection.                                          | `true`                                | All services                             |
| `METRICS_EXPORT_INTERVAL` | String    | Metrics export interval in ISO-8601 duration format.                        | `PT1M` (1 minute)                     | All services with metrics               |
| `TRACING_ENABLED`         | Boolean   | Enable/disable distributed tracing.                                         | `false` (for development)             | All services                             |
| `TRACING_SAMPLE_RATE`     | Number    | Tracing sample rate (0.0 to 1.0).                                          | `0.1` (10%)                           | All services with tracing               |

---

## XIII. SSL/TLS Configuration (Production)

| Variable Name             | Type      | Description                                                                 | Default/Example Value                 | Used By                                  |
| :------------------------ | :-------- | :-------------------------------------------------------------------------- | :------------------------------------ | :--------------------------------------- |
| `SSL_ENABLED`             | Boolean   | Enable/disable SSL/TLS encryption.                                          | `false` (for dev), `true` (for prod) | All services                             |
| `SSL_KEYSTORE_PATH`       | String    | Path to SSL keystore file.                                                  | `/etc/ssl/lego-factory.p12`           | All services with SSL                    |
| `SSL_KEYSTORE_PASSWORD`   | String    | **Secret:** Password for SSL keystore.                                      | `changeme`                            | All services with SSL                    |
| `SSL_KEY_ALIAS`           | String    | Alias for SSL certificate in keystore.                                      | `lego-factory`                        | All services with SSL                    |

---

## XIV. Email & Notification Configuration

| Variable Name             | Type      | Description                                                                 | Default/Example Value                 | Used By                                  |
| :------------------------ | :-------- | :-------------------------------------------------------------------------- | :------------------------------------ | :--------------------------------------- |
| `EMAIL_ENABLED`           | Boolean   | Enable/disable email notifications.                                         | `false` (disabled for development)    | All services sending emails              |
| `SMTP_HOST`               | String    | SMTP server hostname.                                                       | `smtp.gmail.com`                      | All services sending emails              |
| `SMTP_PORT`               | Number    | SMTP server port.                                                           | `587`                                 | All services sending emails              |
| `SMTP_USERNAME`           | String    | SMTP authentication username.                                               | `your-email@gmail.com`                | All services sending emails              |
| `SMTP_PASSWORD`           | String    | **Secret:** SMTP authentication password.                                   | `your-app-password`                   | All services sending emails              |
| `EMAIL_FROM`              | String    | Default "from" email address for notifications.                             | `noreply@lego-factory.com`            | All services sending emails              |

---
