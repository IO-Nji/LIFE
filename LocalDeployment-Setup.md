This is an excellent application setup for learning microservices, containerization, and DevOps! Having an API Gateway as a dedicated microservice simplifies the outer Nginx layer and provides a clearer, more robust architecture. Using individual H2 databases per service for development is also a common pattern, with PostgreSQL for shared data like user authentication.

Let's customize the approach for your local Ubuntu server setup, providing a step-by-step guide and a detailed checklist.

---

## DevOps CI/CD Deployment Guide for LEGO Sample Factory (Local Ubuntu Server)

This guide will walk you through setting up a robust, open-source CI/CD pipeline on your local Ubuntu server to deploy your React frontend and six Spring Boot microservices.

### 1. Understanding Your Application & Deployment Goals

Your application architecture:
*   **Frontend:** React 18+ (Vite, Axios, Context API)
*   **Microservices (6 Spring Boot apps):**
    1.  `user-service`: User management, authentication, authorization (uses **PostgreSQL**)
    2.  `masterdata-service`: Workstations, item definitions (uses **H2 file-based**)
    3.  `inventory-service`: Stock levels, transactions (uses **H2 file-based**)
    4.  `order-processing-service`: Order management (uses **H2 file-based**)
    5.  `simal-integration-service`: Production scheduling mock (uses **H2 file-based**)
    6.  `api-gateway`: Centralized entry point, request routing (Spring Cloud Gateway, no explicit DB)

**Deployment Goal:** Deploy this entire stack on your local Ubuntu server using Docker and orchestrate with Docker Compose, automating builds and deployments via GitLab CI/CD.

### 2. The DevOps Stack: Open-Source & Minimal

*   **Version Control:** Git
*   **CI/CD & Code Hosting:** GitLab Community Edition (CE) - self-hosted on your Ubuntu server.
*   **Containerization:** Docker
*   **Container Orchestration (local):** Docker Compose
*   **Deployment Access:** SSH

### 3. Step-by-Step Deployment Guide

This guide assumes your Ubuntu server is running, connected to your home network, and you have basic SSH access from your Windows PC.

#### Phase 1: Prepare Your Development Environment (Windows PC)

This phase focuses on structuring your code for Dockerization.

1.  **Project Root Directory:**
    *   Create a main directory on your Windows PC for your entire project (e.g., `lego-factory-app`).

2.  **Organize Your Codebase:**
    *   Move your React frontend into a `frontend/` subdirectory.
    *   Move each Spring Boot microservice into its own subdirectory (e.g., `user-service/`, `masterdata-service/`, etc.).
    *   Create a new directory `nginx-root-proxy/` at the project root for the main Nginx configuration.

3.  **Frontend (React/Vite) Dockerfile & Nginx Config:**
    *   **`frontend/Dockerfile`**:
        ```dockerfile
        # Stage 1: Build the React application
        FROM node:18-alpine AS build
        WORKDIR /app
        COPY package.json yarn.lock ./ # Use yarn.lock if you use yarn
        RUN yarn install --frozen-lockfile # or npm install
        COPY . ./
        RUN yarn build # or npm run build

        # Stage 2: Serve the React application with Nginx
        FROM nginx:alpine
        # Remove default Nginx config
        RUN rm /etc/nginx/conf.d/default.conf
        # Copy custom Nginx config for this container
        COPY ./nginx.conf /etc/nginx/conf.d/frontend.conf
        # Copy the built React app from the build stage
        COPY --from=build /app/dist /usr/share/nginx/html # Vite output is usually 'dist'
        EXPOSE 80
        CMD ["nginx", "-g", "daemon off;"]
        ```
    *   **`frontend/nginx.conf`**: (This Nginx is *inside* the frontend container, serving the static React files)
        ```nginx
        server {
            listen 80;
            server_name localhost; # This is internal to the container

            root /usr/share/nginx/html;
            index index.html index.htm;

            location / {
                try_files $uri $uri/ /index.html;
            }

            # If you need to proxy API calls *during local React dev server run* (not dockerized frontend)
            # This is largely irrelevant when served by Nginx root proxy, but good to have
            # location /api {
            #     proxy_pass http://api-gateway:8080; # 'api-gateway' is the service name
            #     proxy_set_header Host $host;
            #     proxy_set_header X-Real-IP $remote_addr;
            # }
        }
        ```
    *   **React `vite.config.js` (important for API calls):** Ensure your React app makes API calls with relative paths (e.g., `/api/users/login`) so the root Nginx proxy can correctly intercept them. If you used `VITE_API_URL` environment variables, make sure they are set correctly for production, or simply rely on relative paths.

4.  **Backend Microservices (Spring Boot) Dockerfiles (6 total):**
    *   For *each* of your six microservice directories (e.g., `user-service/`, `masterdata-service/`), create a `Dockerfile`:
        ```dockerfile
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
        EXPOSE 8080 # Expose the port your Spring Boot app listens on
        ENTRYPOINT ["java", "-jar", "app.jar"]
        ```

5.  **Main Nginx Reverse Proxy Configuration:**
    *   Create a file `nginx-root-proxy/nginx.conf` at your project root:
        ```nginx
        server {
            listen 80;
            server_name _; # Listen on all hostnames

            # Route static files from the React frontend container
            location / {
                proxy_pass http://frontend:80; # 'frontend' is the service name for your React app
                proxy_set_header Host $host;
                proxy_set_header X-Real-IP $remote_addr;
                proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
                proxy_set_header X-Forwarded-Proto $scheme;
            }

            # Route all /api requests to the API Gateway microservice
            location /api/ {
                proxy_pass http://api-gateway:8080/; # 'api-gateway' is the service name
                proxy_set_header Host $host;
                proxy_set_header X-Real-IP $remote_addr;
                proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
                proxy_set_header X-Forwarded-Proto $scheme;
            }
        }
        ```

6.  **Spring Boot `application.properties`/`application.yml` Customization (per service):**
    *   **`user-service`**:
        ```properties
        # ... other user-service properties
        spring.datasource.url=jdbc:postgresql://postgres-db:5432/lego_factory_auth # 'postgres-db' is the service name
        spring.datasource.username=${POSTGRES_USER}
        spring.datasource.password=${POSTGRES_PASSWORD}
        spring.jpa.hibernate.ddl-auto=update
        # Add JWT secret for user-service to sign/validate tokens
        application.security.jwt.secret=${JWT_SECRET}
        ```
    *   **`masterdata-service`, `inventory-service`, `order-processing-service`, `simal-integration-service`**:
        ```properties
        # ... other service properties
        spring.datasource.url=jdbc:h2:file:/app/data/<service_name>_db;DB_CLOSE_ON_EXIT=FALSE;AUTO_SERVER=TRUE
        spring.datasource.driverClassName=org.h2.Driver
        spring.datasource.username=sa
        spring.datasource.password=
        spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
        spring.jpa.hibernate.ddl-auto=update
        ```
        *Important*: `DB_CLOSE_ON_EXIT=FALSE` and `AUTO_SERVER=TRUE` help H2 work better in persistent file mode, especially with Docker volumes.
    *   **`api-gateway`**:
        ```properties
        # ... api-gateway properties
        spring.cloud.gateway.routes[0].id=users_route
        spring.cloud.gateway.routes[0].uri=lb://user-service
        spring.cloud.gateway.routes[0].predicates[0]=Path=/api/users/**
        spring.cloud.gateway.routes[1].id=masterdata_route
        spring.cloud.gateway.routes[1].uri=lb://masterdata-service
        spring.cloud.gateway.routes[1].predicates[0]=Path=/api/masterdata/**
        # ... (add routes for inventory, order-processing, simal-integration)
        ```
        `lb://` indicates a load-balanced route, where the service name in Docker Compose is used.

7.  **`docker-compose.yml` File:**
    *   Create this file at your project root (`lego-factory-app/docker-compose.yml`).
    *   **Define all 9 services:** `nginx` (root proxy), `frontend` (React app with its Nginx), `api-gateway`, `user-service`, `masterdata-service`, `inventory-service`, `order-processing-service`, `simal-integration-service`, `postgres-db`.
    *   **Build Contexts:** Ensure `context` points to the correct service directory for each `build` instruction.
    *   **Volumes for H2:** For each microservice using H2, add a named volume for its `/app/data` directory (e.g., `masterdata_h2_data:/app/data`).
    *   **Volumes for PostgreSQL:** Standard volume for `/var/lib/postgresql/data`.
    *   **Environment Variables:** Pass necessary environment variables (PostgreSQL creds, JWT secret, etc.) to the respective services.
    *   **Dependencies:** Correctly set `depends_on` (e.g., `user-service` depends on `postgres-db`, `api-gateway` depends on all other microservices, `nginx` depends on `frontend` and `api-gateway`).
    *   **Networks:** Ensure all services are on the same `app-network`.

8.  **Environment Variables File (`.env`):**
    *   Create `.env` at your project root (`lego-factory-app/.env`).
    *   Add:
        ```
        POSTGRES_USER=lego_user
        POSTGRES_PASSWORD=your_secure_db_password
        JWT_SECRET=your_super_secret_jwt_key_that_is_long_and_random # Make this very long and random!
        ```
    *   Add `.env` to your project's `.gitignore` file.

#### Phase 2: Set Up Your Ubuntu Server for Deployment

This phase prepares your Ubuntu server to receive and run your Dockerized application.

1.  **SSH into Your Ubuntu Server.**

2.  **System Update & Basic Tools:**
    ```bash
    sudo apt update && sudo apt upgrade -y
    sudo apt install -y curl git openssh-server apt-transport-https ca-certificates software-properties-common
    ```

3.  **Install Docker:**
    ```bash
    curl -fsSL https://get.docker.com -o get-docker.sh
    sudo sh get-docker.sh
    sudo usermod -aG docker ${USER} # Add your current user to the docker group
    newgrp docker # Apply group changes immediately (or logout/login)
    ```
    Verify Docker installation: `docker run hello-world`

4.  **Install Docker Compose:**
    ```bash
    sudo curl -L "https://github.com/docker/compose/releases/download/v2.24.1/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    sudo chmod +x /usr/local/bin/docker-compose
    ```
    Verify Compose installation: `docker-compose --version`

5.  **Firewall Configuration (`ufw`):**
    ```bash
    sudo ufw allow ssh # Allow SSH access
    sudo ufw allow http # Allow HTTP access (port 80)
    sudo ufw enable # Enable the firewall
    sudo ufw status # Check status
    ```
    *If you plan to use HTTPS (recommended for production), also `sudo ufw allow https`.*

6.  **Create a Dedicated Deployment User:**
    *   This is good practice for security; the GitLab Runner will SSH as this user.
    ```bash
    sudo adduser deployuser # Follow prompts to set password etc.
    sudo usermod -aG docker deployuser # Add to docker group
    sudo mkdir -p /home/deployuser/.ssh
    sudo chown deployuser:deployuser /home/deployuser/.ssh
    sudo chmod 700 /home/deployuser/.ssh
    ```

#### Phase 3: Set Up GitLab CE (Self-Hosted)

1.  **Install GitLab Community Edition:**
    *   Still SSHed into your Ubuntu server.
    *   Add GitLab's official repository:
        ```bash
        curl https://packages.gitlab.com/install/repositories/gitlab/gitlab-ee/script.deb.sh | sudo bash
        ```
    *   Install GitLab CE, replacing `<YOUR_SERVER_LOCAL_IP>` with your Ubuntu server's actual local IP (e.g., `192.168.1.100`):
        ```bash
        sudo EXTERNAL_URL="http://<YOUR_SERVER_LOCAL_IP>" apt-get install gitlab-ce
        ```
    *   This will take some time to install and configure.

2.  **Access GitLab:**
    *   From your Windows PC, open a browser and go to `http://<YOUR_SERVER_LOCAL_IP>`.
    *   You'll be prompted to set the initial password for the `root` user.

3.  **Create a New Project in GitLab:**
    *   Log in as `root`.
    *   Create a new blank project (e.g., named `lego-factory-app`). Keep it private.

#### Phase 4: Configure GitLab Runner

1.  **Install GitLab Runner:**
    *   Still SSHed into your Ubuntu server.
    *   Install:
        ```bash
        curl -L "https://packages.gitlab.com/install/repositories/runner/gitlab-runner/script.deb.sh" | sudo bash
        sudo apt-get install gitlab-runner
        ```

2.  **Register the Runner:**
    *   In your GitLab project in the browser, go to `Settings > CI/CD > Runners`. Expand the "Expand" button in the "Specific runners" or "Group runners" section. Copy the "registration token".
    *   On your Ubuntu server, run:
        ```bash
        sudo gitlab-runner register
        ```
        *   **GitLab instance URL:** `http://<YOUR_SERVER_LOCAL_IP>`
        *   **Registration token:** Paste the token from GitLab.
        *   **Description:** `Local Docker Runner`
        *   **Tags:** `docker, deploy, local` (Important for `gitlab-ci.yml` to select this runner)
        *   **Executor:** `docker`
        *   **Default Docker image:** `alpine:latest`

3.  **Configure Runner for Docker Access:**
    *   Edit the runner's configuration:
        ```bash
        sudo nano /etc/gitlab-runner/config.toml
        ```
    *   Find the `[[runners]]` section for your newly registered runner. Under the `[runners.docker]` subsection, add/modify the `volumes` line:
        ```toml
        [[runners]]
          # ... other runner config ...
          executor = "docker"
          [runners.docker]
            # ... other docker config ...
            volumes = ["/var/run/docker.sock:/var/run/docker.sock", "/cache"] # <--- ADD THIS LINE
            # If you encounter permission issues with Docker socket, sometimes `privileged = true` is needed, but try without first.
        ```
    *   Save and exit.
    *   Restart the runner:
        ```bash
        sudo gitlab-runner restart
        ```

#### Phase 5: Implement CI/CD Pipeline (`.gitlab-ci.yml`)

1.  **Generate SSH Key for Deployment:**
    *   On your **Windows PC**, generate a new SSH key pair *specifically for GitLab CI/CD*:
        ```bash
        ssh-keygen -t rsa -b 4096 -f ~/.ssh/gitlab_deploy_key -N "" # -N "" creates a key without a passphrase
        ```
    *   **Add Public Key to Ubuntu Server:** Copy the content of `~/.ssh/gitlab_deploy_key.pub` and append it to the `authorized_keys` file for the `deployuser` on your Ubuntu server:
        ```bash
        # On Ubuntu server, as sudo
        sudo sh -c 'echo "PASTE_YOUR_PUBLIC_KEY_CONTENT_HERE" >> /home/deployuser/.ssh/authorized_keys'
        sudo chown deployuser:deployuser /home/deployuser/.ssh/authorized_keys
        sudo chmod 600 /home/deployuser/.ssh/authorized_keys
        ```
        *Test SSH connection:* From your Windows PC, `ssh -i ~/.ssh/gitlab_deploy_key deployuser@<YOUR_SERVER_LOCAL_IP>`. It should connect without asking for a password.

2.  **Configure GitLab CI/CD Variables:**
    *   In your GitLab project in the browser, go to `Settings > CI/CD > Variables`.
    *   Add the following variables:
        *   `SSH_PRIVATE_KEY`: **Value:** Copy the entire content of your *private* key (`~/.ssh/gitlab_deploy_key`). **Type:** `File`. **Flags:** `Protected`.
        *   `SERVER_IP`: **Value:** Your Ubuntu server's local IP (e.g., `192.168.1.100`). **Type:** `Variable`.
        *   `SERVER_USER`: **Value:** `deployuser`. **Type:** `Variable`.
        *   `PROJECT_PATH_ON_SERVER`: **Value:** `/opt/lego-factory-app`. **Type:** `Variable`. (This is where your project will reside on the server).
        *   `POSTGRES_USER`: **Value:** `lego_user`. **Type:** `Variable`. `Protected`.
        *   `POSTGRES_PASSWORD`: **Value:** `your_secure_db_password`. **Type:** `Variable`. `Protected`.
        *   `JWT_SECRET`: **Value:** `your_super_secret_jwt_key_that_is_long_and_random`. **Type:** `Variable`. `Protected`.

3.  **Create `.gitlab-ci.yml`:**
    *   Create this file at your project root (`lego-factory-app/.gitlab-ci.yml`).
    *   This file defines the CI/CD pipeline.

    ```yaml
    stages:
      - build
      - deploy

    variables:
      # DOCKER_DRIVER: overlay2 # Optional, for Docker in Docker
      # DOCKER_TLS_CERTDIR: ""  # Optional, for Docker in Docker
      # CI_DEBUG_TRACE: "true" # Uncomment for debugging pipeline issues

    .build_template: &build_definition # Anchor for reusable build definition
      image: docker:latest
      services:
        - docker:dind # Docker in Docker service
      before_script:
        - docker login -u gitlab-ci-token -p $CI_JOB_TOKEN $CI_REGISTRY
      script:
        - echo "Building $SERVICE_NAME image..."
        - docker build -t $CI_REGISTRY_IMAGE/$SERVICE_NAME:$CI_COMMIT_SHORT_SHA $BUILD_CONTEXT
        - docker push $CI_REGISTRY_IMAGE/$SERVICE_NAME:$CI_COMMIT_SHORT_SHA
      tags:
        - docker # Uses the runner configured with 'docker' tag

    # Build Jobs for all services
    build_frontend:
      <<: *build_definition # Use the reusable definition
      stage: build
      variables:
        SERVICE_NAME: frontend
        BUILD_CONTEXT: ./frontend

    build_api_gateway:
      <<: *build_definition
      stage: build
      variables:
        SERVICE_NAME: api-gateway
        BUILD_CONTEXT: ./api-gateway

    build_user_service:
      <<: *build_definition
      stage: build
      variables:
        SERVICE_NAME: user-service
        BUILD_CONTEXT: ./user-service

    build_masterdata_service:
      <<: *build_definition
      stage: build
      variables:
        SERVICE_NAME: masterdata-service
        BUILD_CONTEXT: ./masterdata-service

    build_inventory_service:
      <<: *build_definition
      stage: build
      variables:
        SERVICE_NAME: inventory-service
        BUILD_CONTEXT: ./inventory-service

    build_order_processing_service:
      <<: *build_definition
      stage: build
      variables:
        SERVICE_NAME: order-processing-service
        BUILD_CONTEXT: ./order-processing-service

    build_simal_integration_service:
      <<: *build_definition
      stage: build
      variables:
        SERVICE_NAME: simal-integration-service
        BUILD_CONTEXT: ./simal-integration-service

    # Deployment Job
    deploy_app:
      stage: deploy
      image: alpine/git # A lightweight image with git for cloning
      before_script:
        - apk add --no-cache openssh-client # Install SSH client
        - eval $(ssh-agent -s) # Start SSH agent
        - echo "$SSH_PRIVATE_KEY" | tr -d '\r' | ssh-add - # Add SSH key from variable
        - mkdir -p ~/.ssh
        - chmod 700 ~/.ssh
        - ssh-keyscan -H $SERVER_IP >> ~/.ssh/known_hosts # Add server to known hosts
        - chmod 644 ~/.ssh/known_hosts
      script:
        - ssh ${SERVER_USER}@${SERVER_IP} "
            mkdir -p ${PROJECT_PATH_ON_SERVER} && \
            cd ${PROJECT_PATH_ON_SERVER} && \
            docker login -u gitlab-ci-token -p $CI_JOB_TOKEN $CI_REGISTRY && \
            # Pull the latest docker-compose.yml from the repo
            git init && \
            git remote add origin $CI_REPOSITORY_URL || true && \
            git fetch origin $CI_COMMIT_BRANCH && \
            git reset --hard origin/$CI_COMMIT_BRANCH && \
            # Create/Update .env file on the server with CI/CD variables
            echo "POSTGRES_USER=$POSTGRES_USER" > .env && \
            echo "POSTGRES_PASSWORD=$POSTGRES_PASSWORD" >> .env && \
            echo "JWT_SECRET=$JWT_SECRET" >> .env && \
            # ... add any other necessary environment variables to .env on server
            docker-compose pull && \
            docker-compose up -d --remove-orphans
          "
      environment:
        name: local-dev
        url: http://${SERVER_IP}
      rules:
        - if: $CI_COMMIT_BRANCH == $CI_DEFAULT_BRANCH # Deploy only on pushes to the default branch
      tags:
        - deploy # Uses the runner configured with 'deploy' tag
    ```

#### Phase 6: Initial Deployment & Verification

1.  **Clone Project on Ubuntu Server (Manual First Time):**
    *   SSH into your Ubuntu server as your main user (not deployuser):
        ```bash
        ssh your-main-user@<YOUR_SERVER_LOCAL_IP>
        ```
    *   Create the deployment directory with proper ownership:
        ```bash
        sudo mkdir -p /opt/lego-factory-app
        sudo chown -R deployuser:deployuser /opt/lego-factory-app
        ```
    *   Now switch to deployuser and clone the project:
        ```bash
        sudo su - deployuser
        cd /opt/lego-factory-app
        git clone http://<YOUR_SERVER_LOCAL_IP>/root/lego-factory-app.git .
        ```
    *   **Fix project structure** - move files to correct location:
        ```bash
        # If cloned with LIFE subdirectory, move contents up one level
        if [ -d "LIFE" ]; then
            mv LIFE/* . 2>/dev/null || true
            mv LIFE/.* . 2>/dev/null || true
            rmdir LIFE
        fi
        
        # Ensure docker-compose.yml is in the root with the services
        if [ ! -f "docker-compose.yml" ]; then
            if [ -f "lego-factory-backend/docker-compose.yml" ]; then
                mv lego-factory-backend/docker-compose.yml .
            fi
        fi
        ```
    *   Create the environment file manually for initial setup:
        ```bash
        cat > .env << EOF
POSTGRES_USER=lego_user
POSTGRES_PASSWORD=your_secure_db_password
JWT_SECRET=your_super_secret_jwt_key_that_is_long_and_random_123456789abcdefghijklmnopqrstuvwxyz
EOF
        ```
    *   Set proper permissions:
        ```bash
        chmod 600 .env
        ```
    *   **Verify structure** before building:
        ```bash
        # Should see docker-compose.yml and service directories at same level
        ls -la
        # Should show: docker-compose.yml, api-gateway/, user-service/, etc.
        ```

2.  **Initial Manual Build and Deploy:**
    *   Verify you're in the correct directory with docker-compose.yml:
        ```bash
        cd /opt/lego-factory-app
        pwd  # Should show /opt/lego-factory-app
        ls docker-compose.yml  # Should exist
        ```
    *   Build all Docker images locally on the server (as deployuser):
        ```bash
        docker-compose build --no-cache
        ```
    *   If build fails due to missing service directories, check structure:
        ```bash
        ls -la  # Verify service directories are present
        # Expected: api-gateway/ user-service/ masterdata-service/ etc.
        ```
    *   Start the application stack:
        ```bash
        docker-compose up -d
        ```
    *   Check container status:
        ```bash
        docker-compose ps
        docker ps | grep lego
        ```
    *   Check logs for any errors:
        ```bash
        docker-compose logs
        ```

3.  **Initial Commit & Push from Windows PC:**
    *   On your Windows PC, add the GitLab remote and push:
        ```bash
        cd lego-factory-app
        git remote add origin http://<YOUR_SERVER_LOCAL_IP>/root/lego-factory-app.git
        git add .
        git commit -m "Initial commit with complete Docker setup"
        git push -u origin main
        ```

4.  **Monitor GitLab CI/CD Pipeline:**
    *   In your browser, go to `http://<YOUR_SERVER_LOCAL_IP>/root/lego-factory-app/-/pipelines`
    *   Watch the pipeline execution and check logs for any build or deployment issues
    *   If the pipeline fails, check individual job logs for specific error messages

5.  **Access & Verify Application:**
    *   Open your web browser and navigate to:
        ```
        http://<YOUR_SERVER_LOCAL_IP>
        ```
    *   Verify frontend loads correctly
    *   Test API endpoints through the frontend:
        - User registration/login functionality
        - Masterdata management features
        - Inventory operations
        - Order processing workflows

6.  **Health Check Commands (On Ubuntu Server):**
    *   Check all containers are running:
        ```bash
        docker-compose ps
        docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
        ```
    *   Check container logs:
        ```bash
        # API Gateway logs
        docker logs lego-api-gateway

        # Database logs
        docker logs lego-postgres-db

        # Individual service logs
        docker logs lego-user-service
        docker logs lego-masterdata-service
        docker logs lego-inventory-service
        docker logs lego-order-processing-service
        docker logs lego-simal-integration-service

        # Frontend logs
        docker logs lego-frontend

        # Root proxy logs
        docker logs lego-factory-nginx
        ```
    *   Test API endpoints directly:
        ```bash
        # Health check endpoint
        curl http://localhost/api/health

        # User service endpoint
        curl http://localhost/api/users/health

        # Other service endpoints
        curl http://localhost/api/masterdata/health
        curl http://localhost/api/inventory/health
        curl http://localhost/api/orders/health
        curl http://localhost/api/simal/health
        ```

7.  **Troubleshooting Common Issues:**
    *   **Permission issues with deployment directory:**
        ```bash
        # Run as your main user with sudo access
        sudo chown -R deployuser:deployuser /opt/lego-factory-app
        sudo chmod -R 755 /opt/lego-factory-app
        ```
    *   **Docker permission issues:**
        ```bash
        # Verify deployuser is in docker group
        groups deployuser
        # If not in docker group, add with main user:
        sudo usermod -aG docker deployuser
        # Then logout and login as deployuser
        ```
    *   **Containers not starting:**
        ```bash
        docker-compose logs <service-name>
        docker-compose down
        docker-compose up -d
        ```
    *   **Port conflicts:**
        ```bash
        sudo netstat -tulpn | grep :80
        sudo lsof -i :80
        ```
    *   **Database connection issues:**
        ```bash
        docker exec -it lego-postgres-db psql -U lego_user -d lego_factory_auth
        ```
    *   **Volume permission issues:**
        ```bash
        docker volume ls
        docker volume inspect lego-factory-backend_postgres_db_data
        ```
    *   **Network connectivity:**
        ```bash
        docker network ls
        docker network inspect lego-factory-backend_app-network
        ```
    *   **Docker-compose file not found:**
        ```bash
        # Check current directory and files
        pwd
        ls -la docker-compose.yml
        
        # If in wrong directory, navigate to correct one
        cd /opt/lego-factory-app
        
        # If docker-compose.yml is in subdirectory, move it
        find . -name "docker-compose.yml" -type f
        ```
    *   **Service directories not found during build:**
        ```bash
        # Verify all service directories exist at same level as docker-compose.yml
        ls -la | grep -E "(api-gateway|user-service|masterdata-service|inventory-service|order-processing-service|simal-integration-service)"
        
        # Check if services are in subdirectory
        find . -name "api-gateway" -type d
        ```
    *   **Project structure issues:**
        ```bash
        # If services are in lego-factory-backend subdirectory
        if [ -d "lego-factory-backend" ]; then
            echo "Moving backend services to root level"
            mv lego-factory-backend/* . 2>/dev/null || true
            rmdir lego-factory-backend
        fi
        
        # If frontend is in subdirectory
        if [ ! -d "lego-factory-frontend" ] && [ -d "frontend" ]; then
            mv frontend lego-factory-frontend
        fi
        ```

8.  **Verify CI/CD Integration:**
    *   Make a small change in your frontend or backend code on Windows PC
    *   Commit and push the changes:
        ```bash
        git add .
        git commit -m "Test CI/CD pipeline"
        git push origin main
        ```
    *   Monitor the GitLab pipeline execution
    *   Verify the changes are automatically deployed to your Ubuntu server
    *   Check that the application updates without manual intervention

---

### 4. Development Progress Checklist (for VS Code Copilot Guide)

This checklist breaks down the entire process into actionable items. You can use this as a reference point in VS Code, asking Copilot for details, code snippets, or explanations for each item.

**Phase 1: Local Project Preparation & Dockerization (On your Windows PC)**

*   **1.1 Project Structure**
    *   [ ] Create `lego-factory-app/` root directory.
    *   [ ] Move React app into `lego-factory-app/frontend/`.
    *   [ ] Move `user-service` into `lego-factory-app/user-service/`.
    *   [ ] Move `masterdata-service` into `lego-factory-app/masterdata-service/`.
    *   [ ] Move `inventory-service` into `lego-factory-app/inventory-service/`.
    *   [ ] Move `order-processing-service` into `lego-factory-app/order-processing-service/`.
    *   [ ] Move `simal-integration-service` into `lego-factory-app/simal-integration-service/`.
    *   [ ] Move `api-gateway` into `lego-factory-app/api-gateway/`.
    *   [ ] Create `lego-factory-app/nginx-root-proxy/` directory.

*   **1.2 Frontend Dockerization (`frontend/`)**
    *   [ ] Create `frontend/Dockerfile`.
    *   [ ] Create `frontend/nginx.conf` (for internal frontend server).
    *   [ ] Review `vite.config.js` for API routing.

*   **1.3 Backend Dockerization (for each of the 6 microservices)**
    *   [ ] For each microservice (`user-service`, `masterdata-service`, etc.), create `Dockerfile`.

*   **1.4 Spring Boot Application Configuration (per microservice)**
    *   [ ] `user-service`: Configure `application.properties` for PostgreSQL connection and JWT secret.
    *   [ ] `masterdata-service`: Configure `application.properties` for H2 file-based DB.
    *   [ ] `inventory-service`: Configure `application.properties` for H2 file-based DB.
    *   [ ] `order-processing-service`: Configure `application.properties` for H2 file-based DB.
    *   [ ] `simal-integration-service`: Configure `application.properties` for H2 file-based DB.
    *   [ ] `api-gateway`: Configure `application.properties` for routing to other services.

*   **1.5 Main Nginx Reverse Proxy Configuration (`nginx-root-proxy/`)**
    *   [ ] Create `nginx-root-proxy/nginx.conf` to serve frontend and proxy `/api` to `api-gateway`.

*   **1.6 Docker Compose File (`lego-factory-app/`)**
    *   [ ] Create `docker-compose.yml`.
    *   [ ] Define all 9 services (`nginx`, `frontend`, `api-gateway`, 5 other backends, `postgres-db`).
    *   [ ] Configure `build` contexts, `ports` (only for `nginx`), `environment` variables (referencing `.env`).
    *   [ ] Define volumes for `postgres-db_data` and all `_h2_data` (5 total).
    *   [ ] Define `app-network`.
    *   [ ] Set `depends_on` relationships.

*   **1.7 Environment Variables & Git Ignore (`lego-factory-app/`)**
    *   [ ] Create `.env` file with `POSTGRES_USER`, `POSTGRES_PASSWORD`, `JWT_SECRET`.
    *   [ ] Add `.env` to `.gitignore`.

*   **1.8 Local Docker Compose Test**
    *   [ ] Navigate to `lego-factory-app/` in terminal.
    *   [ ] `docker-compose build`.
    *   [ ] `docker-compose up -d`.
    *   [ ] Access `http://localhost` in browser, verify functionality.
    *   [ ] `docker-compose down`.

*   **1.9 Initial Git Repository Setup**
    *   [ ] Initialize Git repo (`git init`).
    *   [ ] Add all files (except `.env`).
    *   [ ] Make initial commit.

**Phase 2: Ubuntu Server Setup (SSH into Ubuntu)**

*   **2.1 System Update & Tools**
    *   [ ] Run `sudo apt update && sudo apt upgrade -y`.
    *   [ ] Install necessary packages (`curl`, `git`, `openssh-server`, etc.).

*   **2.2 Docker & Docker Compose Installation**
    *   [ ] Install Docker.
    *   [ ] Add current user to `docker` group (`sudo usermod -aG docker ${USER}`).
    *   [ ] Log out and back in, or `newgrp docker`.
    *   [ ] Install Docker Compose.
    *   [ ] Verify `docker run hello-world` and `docker-compose --version`.

*   **2.3 Firewall Configuration (`ufw`)**
    *   [ ] Allow SSH (`sudo ufw allow ssh`).
    *   [ ] Allow HTTP (`sudo ufw allow http`).
    *   [ ] Enable firewall (`sudo ufw enable`).

*   **2.4 Dedicated Deployment User**
    *   [ ] Create `deployuser` (`sudo adduser deployuser`).
    *   [ ] Add `deployuser` to `docker` group (`sudo usermod -aG docker deployuser`).
    *   [ ] Create `.ssh` directory and set permissions for `deployuser`.

**Phase 3: GitLab CE & Runner Setup (On Ubuntu, via browser & SSH)**

*   **3.1 GitLab CE Installation**
    *   [ ] Install GitLab CE using `EXTERNAL_URL="http://<YOUR_SERVER_LOCAL_IP>"`.
    *   [ ] Access GitLab in browser, set root password.

*   **3.2 GitLab Project Creation**
    *   [ ] Create new blank project `lego-factory-app` in GitLab.
    *   [ ] Get project's Git remote URL.

*   **3.3 GitLab Runner Configuration**
    *   [ ] Install `gitlab-runner`.
    *   [ ] Register runner with `docker` executor and tags `docker, deploy, local`.
    *   [ ] Edit `config.toml` to mount Docker socket (`/var/run/docker.sock:/var/run/docker.sock`).
    *   [ ] Restart `gitlab-runner`.

**Phase 4: CI/CD Pipeline Implementation (On Windows PC, then GitLab)**

*   **4.1 SSH Key Generation & Server Setup**
    *   [ ] Generate CI/CD specific SSH key pair on Windows (`gitlab_deploy_key`).
    *   [ ] Add public key (`gitlab_deploy_key.pub`) to `/home/deployuser/.ssh/authorized_keys` on Ubuntu.
    *   [ ] Test SSH connection to `deployuser`.

*   **4.2 GitLab CI/CD Variables**
    *   [ ] In GitLab project `Settings > CI/CD > Variables`:
        *   [ ] Add `SSH_PRIVATE_KEY` (File, Protected).
        *   [ ] Add `SERVER_IP` (Variable).
        *   [ ] Add `SERVER_USER` (Variable, `deployuser`).
        *   [ ] Add `PROJECT_PATH_ON_SERVER` (Variable, `/opt/lego-factory-app`).
        *   [ ] Add `POSTGRES_USER` (Variable, Protected).
        *   [ ] Add `POSTGRES_PASSWORD` (Variable, Protected).
        *   [ ] Add `JWT_SECRET` (Variable, Protected).

*   **4.3 Create `.gitlab-ci.yml`**
    *   [ ] Create `lego-factory-app/.gitlab-ci.yml` (as detailed in Phase 5 of the guide).
    *   [ ] Define `build` jobs for `frontend`, `api-gateway`, and the 5 other microservices.
    *   [ ] Define `deploy_app` job for SSHing and `docker-compose` operations.

*   **4.4 Initial Git Push & CI/CD Trigger**
    *   [ ] On Windows PC, add `.gitlab-ci.yml`.
    *   [ ] Commit and push all changes to your GitLab CE project (`git remote add origin <URL>`, `git push origin main`).

**Phase 5: Verification & Maintenance**

*   **5.1 Monitor CI/CD Pipeline**
    *   [ ] Check GitLab `CI/CD > Pipelines` for execution status and logs.

*   **5.2 Access & Verify Application**
    *   [ ] In browser, go to `http://<YOUR_SERVER_LOCAL_IP>`, verify functionality.
    *   [ ] Log in to application, test microservice interactions.
    *   [ ] Check Docker containers and logs on Ubuntu server (`docker ps`, `docker logs`).

*   **5.3 Continuous Development**
    *   [ ] Make code changes on Windows, commit, push to GitLab.
    *   [ ] Observe automatic pipeline execution and deployment.

This comprehensive guide and checklist should provide a solid foundation for your learning and practical implementation!