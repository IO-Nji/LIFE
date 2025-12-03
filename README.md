# LEGO Sample Factory Control System

A microservice-based prototype that digitizes control flows for the LEGO Sample Factory. The platform replaces paper-heavy processes with services that manage gateway routing, user management, reference data, and simulated production integration, accompanied by a React frontend for operator dashboards.

## Implemented Modules

- **lego-factory-parent** (Spring Boot, port 8010)
  - Core backend service configured with Spring Web, Security, Data JPA, and an in-memory H2 database for development.
- **api-gateway** (Spring Cloud Gateway, port 8011)
  - Entry point prepared to route external traffic to downstream services.
- **user-service** (Spring Boot, port 8012)
  - Scaffolded for authentication and authorization workflows, backed by its own H2 database.
- **masterdata-service** (Spring Boot, port 8013)
  - Hosts product and workstation reference data, using an independent H2 store.
- **simal-integration-service** (Spring Boot, port 8014)
  - Provides the adapter surface for the SimAL scheduler integration, backed by H2.
- **lego-factory-frontend** (React + Vite)
  - Client application initialized with `axios` and `react-router-dom` for future role-based dashboards.

## Technology Stack

- **Backend:** Java 21, Spring Boot 4, Spring Cloud Gateway, Maven Wrapper, individual H2 databases per service.
- **Frontend:** React 18, Vite tooling, Axios for API calls, React Router for navigation.
- **Tooling:** Visual Studio Code workspace, npm package manager, Node.js runtime for the frontend.

## Default Accounts

- `legoAdmin` / `legoPass` — provisioned automatically in the user-service with the `ADMIN` role for local development. Use this account when signing in from the frontend to manage additional users.

### User-Service REST API

- `POST /api/auth/login` — issue a JWT for valid credentials.
- `GET /api/users/me` — fetch the currently authenticated user's profile.
- `GET /api/users` *(ADMIN)* — list all users.
- `GET /api/users/{id}` *(ADMIN)* — fetch a user by identifier.
- `POST /api/users` *(ADMIN)* — create a new user.
- `PUT /api/users/{id}` *(ADMIN)* — update username, role, workstation, or password.
- `DELETE /api/users/{id}` *(ADMIN)* — remove a user.

## Project Vision

The full solution will coordinate customer orders, production planning, warehouse fulfillment, and workstation execution across LEGO Sample Factory roles. Upcoming work will link the gateway to each microservice, implement authentication flows in the user-service, expose domain APIs for master data and production events, and build frontend dashboards that surface these capabilities to operators.
