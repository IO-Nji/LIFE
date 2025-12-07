# 🧪 LIFE Application - Testing Framework Implementation Guide

> Complete walk-through for implementing unit tests, integration tests, and end-to-end tests for your application

---

## 📊 Current State Assessment

### ✅ What You Have

**Backend:**
- JUnit 5 (Jupiter) - Modern testing framework
- Mockito - Mocking library
- AssertJ - Fluent assertions
- Spring Test - Spring Boot test support
- H2 - In-memory database (perfect for testing)

**Frontend:**
- Vite - Fast build tool
- React 18 - Component library

### ❌ What's Missing

**Backend:**
- JaCoCo - Code coverage reporting
- Comprehensive unit tests (mostly skeleton tests)
- Test data builders/fixtures

**Frontend:**
- Vitest - Unit testing framework
- React Testing Library - Component testing
- MSW (Mock Service Worker) - API mocking

---

## 🏗️ Testing Strategy

### Testing Pyramid for Backend

```
        Unit Tests (60%)
      /                \
    Integration Tests (30%)
   /                      \
  E2E / API Tests (10%)
```

### What Each Layer Tests

| Layer | Framework | Coverage | Speed | Files |
|:------|:----------|:---------|:------|:------|
| **Unit** | JUnit 5 + Mockito | Logic, calculations, edge cases | Very Fast | Service, Repository |
| **Integration** | Spring Test + TestContainers | Service with real DB | Medium | Service, Controller |
| **E2E** | Postman/REST Assured | Full workflows | Slow | End-to-end scenarios |

---

## 🔧 Phase 1: Set Up Backend Testing Infrastructure

### Step 1.1: Add Testing Dependencies to All Services

Add to each `pom.xml` in the `<dependencies>` section:

```xml
<!-- Code Coverage -->
<dependency>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.10</version>
    <scope>test</scope>
</dependency>

<!-- Test Data Builder Helper -->
<dependency>
    <groupId>com.google.guava</groupId>
    <artifactId>guava</artifactId>
    <version>32.1.3-jre</version>
    <scope>test</scope>
</dependency>
```

Also add to `<build><plugins>` section:

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.10</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### Step 1.2: Create Base Test Class for Integration Tests

Create: `user-service/src/test/java/io/life/user_service/BaseIntegrationTest.java`

```java
package io.life.user_service;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class BaseIntegrationTest {
    
    @Autowired
    protected MockMvc mockMvc;
    
    // Placeholder for common setup
    @BeforeEach
    public void setUp() {
        // Common test setup
    }
}
```

### Step 1.3: Create Test Configuration

Create: `user-service/src/test/resources/application-test.yml`

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: create-drop
    show-sql: false
  h2:
    console:
      enabled: false

logging:
  level:
    root: WARN
    io.life: INFO
```

---

## 🎯 Phase 2: Write Unit Tests for Services

### Example 1: User Service Unit Test

Create: `user-service/src/test/java/io/life/user_service/service/UserServiceTest.java`

```java
package io.life.user_service.service;

import io.life.user_service.dto.UserRequestDTO;
import io.life.user_service.dto.UserResponseDTO;
import io.life.user_service.entity.User;
import io.life.user_service.repository.UserRepository;
import io.life.user_service.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("User Service Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @InjectMocks
    private UserService userService;
    
    private User testUser;
    private UserRequestDTO userRequestDTO;
    
    @BeforeEach
    void setUp() {
        // Arrange: Set up test data
        testUser = User.builder()
            .id(1L)
            .username("testuser")
            .email("test@example.com")
            .password("hashedpassword")
            .role("USER")
            .workstationId(1L)
            .build();
            
        userRequestDTO = UserRequestDTO.builder()
            .username("testuser")
            .email("test@example.com")
            .password("password123")
            .role("USER")
            .workstationId(1L)
            .build();
    }
    
    @Test
    @DisplayName("Should create user successfully with valid data")
    void testCreateUserSuccess() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedpassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        // Act
        UserResponseDTO result = userService.createUser(userRequestDTO);
        
        // Assert
        assertThat(result)
            .isNotNull()
            .hasFieldOrPropertyWithValue("username", "testuser")
            .hasFieldOrPropertyWithValue("email", "test@example.com");
        
        verify(userRepository, times(1)).save(any(User.class));
    }
    
    @Test
    @DisplayName("Should throw exception when username already exists")
    void testCreateUserDuplicateUsername() {
        // Arrange
        when(userRepository.existsByUsername("testuser")).thenReturn(true);
        
        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(userRequestDTO))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Username already exists");
        
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    @DisplayName("Should retrieve user by ID successfully")
    void testGetUserByIdSuccess() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        
        // Act
        UserResponseDTO result = userService.getUserById(1L);
        
        // Assert
        assertThat(result)
            .isNotNull()
            .hasFieldOrPropertyWithValue("username", "testuser");
    }
    
    @Test
    @DisplayName("Should throw exception when user not found")
    void testGetUserByIdNotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> userService.getUserById(999L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("User not found");
    }
    
    @Test
    @DisplayName("Should update user successfully")
    void testUpdateUserSuccess() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        UserRequestDTO updateDTO = UserRequestDTO.builder()
            .username("updateduser")
            .email("updated@example.com")
            .build();
        
        // Act
        UserResponseDTO result = userService.updateUser(1L, updateDTO);
        
        // Assert
        assertThat(result).isNotNull();
        verify(userRepository, times(1)).save(any(User.class));
    }
    
    @Test
    @DisplayName("Should delete user successfully")
    void testDeleteUserSuccess() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        
        // Act
        userService.deleteUser(1L);
        
        // Assert
        verify(userRepository, times(1)).deleteById(1L);
    }
}
```

### Test Execution

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=UserServiceTest

# Run with coverage report
mvn test jacoco:report

# View coverage report (opens in browser)
open user-service/target/site/jacoco/index.html
```

---

## 🔗 Phase 3: Write Integration Tests for Controllers

### Example: User Controller Integration Test

Create: `user-service/src/test/java/io/life/user_service/controller/UserControllerIntegrationTest.java`

```java
package io.life.user_service.controller;

import io.life.user_service.BaseIntegrationTest;
import io.life.user_service.dto.UserRequestDTO;
import io.life.user_service.entity.User;
import io.life.user_service.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@DisplayName("User Controller Integration Tests")
class UserControllerIntegrationTest extends BaseIntegrationTest {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        // Clear repository
        userRepository.deleteAll();
        
        // Create test user
        testUser = User.builder()
            .username("testuser")
            .email("test@example.com")
            .password("hashedpassword")
            .role("USER")
            .workstationId(1L)
            .build();
        
        userRepository.save(testUser);
    }
    
    @Test
    @DisplayName("GET /api/users - Should return all users with 200 OK")
    void testGetAllUsers() throws Exception {
        mockMvc.perform(get("/api/users")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
            .andExpect(jsonPath("$[0].username", is("testuser")));
    }
    
    @Test
    @DisplayName("GET /api/users/{id} - Should return user with 200 OK")
    void testGetUserById() throws Exception {
        mockMvc.perform(get("/api/users/" + testUser.getId())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username", is("testuser")))
            .andExpect(jsonPath("$.email", is("test@example.com")));
    }
    
    @Test
    @DisplayName("GET /api/users/{id} - Should return 404 when user not found")
    void testGetUserByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/users/999")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }
    
    @Test
    @DisplayName("POST /api/users - Should create new user with 201 Created")
    void testCreateUser() throws Exception {
        UserRequestDTO newUser = UserRequestDTO.builder()
            .username("newuser")
            .email("new@example.com")
            .password("password123")
            .role("USER")
            .workstationId(1L)
            .build();
        
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.username", is("newuser")))
            .andExpect(jsonPath("$.email", is("new@example.com")));
    }
    
    @Test
    @DisplayName("POST /api/users - Should return 400 with invalid data")
    void testCreateUserInvalidData() throws Exception {
        UserRequestDTO invalidUser = UserRequestDTO.builder()
            .username("") // Empty username
            .email("invalid-email") // Invalid email
            .password("123") // Weak password
            .build();
        
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUser)))
            .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("PUT /api/users/{id} - Should update user with 200 OK")
    void testUpdateUser() throws Exception {
        UserRequestDTO updateDTO = UserRequestDTO.builder()
            .username("updateduser")
            .email("updated@example.com")
            .role("ADMIN")
            .build();
        
        mockMvc.perform(put("/api/users/" + testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username", is("updateduser")));
    }
    
    @Test
    @DisplayName("DELETE /api/users/{id} - Should delete user with 204 No Content")
    void testDeleteUser() throws Exception {
        mockMvc.perform(delete("/api/users/" + testUser.getId()))
            .andExpect(status().isNoContent());
        
        // Verify deletion
        mockMvc.perform(get("/api/users/" + testUser.getId()))
            .andExpect(status().isNotFound());
    }
}
```

---

## 🌐 Phase 4: Set Up Frontend Testing

### Step 4.1: Install Testing Dependencies

```bash
cd lego-factory-frontend

# Install test frameworks
npm install -D vitest @testing-library/react @testing-library/jest-dom @testing-library/user-event
npm install -D jsdom
npm install -D @vitest/ui
npm install -D msw msw-storybook-addon
```

### Step 4.2: Create Vitest Configuration

Create: `lego-factory-frontend/vitest.config.js`

```javascript
import { defineConfig } from 'vitest/config';
import react from '@vitejs/plugin-react';
import path from 'path';

export default defineConfig({
  plugins: [react()],
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: ['./src/test/setup.js'],
    coverage: {
      provider: 'v8',
      reporter: ['text', 'json', 'html'],
      exclude: [
        'node_modules/',
        'src/test/',
      ]
    }
  },
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    }
  }
});
```

### Step 4.3: Create Test Setup File

Create: `lego-factory-frontend/src/test/setup.js`

```javascript
import { expect, afterEach, vi } from 'vitest';
import { cleanup } from '@testing-library/react';
import '@testing-library/jest-dom';

// Cleanup after each test
afterEach(() => {
  cleanup();
});

// Mock localStorage
const localStorageMock = {
  getItem: vi.fn(),
  setItem: vi.fn(),
  removeItem: vi.fn(),
  clear: vi.fn(),
};
global.localStorage = localStorageMock;

// Mock fetch for API calls
global.fetch = vi.fn();
```

### Step 4.4: Create Test Utilities

Create: `lego-factory-frontend/src/test/testUtils.jsx`

```javascript
import React from 'react';
import { render } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';

// Custom render function that includes common providers
const customRender = (ui, options = {}) => {
  const Wrapper = ({ children }) => (
    <BrowserRouter>
      {children}
    </BrowserRouter>
  );

  return render(ui, { wrapper: Wrapper, ...options });
};

export * from '@testing-library/react';
export { customRender as render };
```

---

## 🧬 Phase 5: Write Component Tests for Frontend

### Example 1: Login Component Test

Create: `lego-factory-frontend/src/components/__tests__/LoginForm.test.jsx`

```javascript
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@/test/testUtils';
import userEvent from '@testing-library/user-event';
import LoginForm from '../LoginForm';

describe('LoginForm Component', () => {
  
  beforeEach(() => {
    // Clear mocks before each test
    vi.clearAllMocks();
  });

  it('should render login form with username and password fields', () => {
    render(<LoginForm />);
    
    expect(screen.getByLabelText(/username/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /login/i })).toBeInTheDocument();
  });

  it('should display validation errors for empty fields', async () => {
    const user = userEvent.setup();
    render(<LoginForm />);
    
    const submitButton = screen.getByRole('button', { name: /login/i });
    await user.click(submitButton);
    
    await waitFor(() => {
      expect(screen.getByText(/username is required/i)).toBeInTheDocument();
      expect(screen.getByText(/password is required/i)).toBeInTheDocument();
    });
  });

  it('should submit form with valid credentials', async () => {
    const user = userEvent.setup();
    const mockOnSubmit = vi.fn();
    
    render(<LoginForm onSubmit={mockOnSubmit} />);
    
    const usernameInput = screen.getByLabelText(/username/i);
    const passwordInput = screen.getByLabelText(/password/i);
    const submitButton = screen.getByRole('button', { name: /login/i });
    
    await user.type(usernameInput, 'testuser');
    await user.type(passwordInput, 'password123');
    await user.click(submitButton);
    
    await waitFor(() => {
      expect(mockOnSubmit).toHaveBeenCalledWith({
        username: 'testuser',
        password: 'password123'
      });
    });
  });

  it('should display error message on login failure', async () => {
    const user = userEvent.setup();
    const mockOnSubmit = vi.fn().mockRejectedValue(
      new Error('Invalid credentials')
    );
    
    render(<LoginForm onSubmit={mockOnSubmit} />);
    
    const usernameInput = screen.getByLabelText(/username/i);
    const passwordInput = screen.getByLabelText(/password/i);
    const submitButton = screen.getByRole('button', { name: /login/i });
    
    await user.type(usernameInput, 'wronguser');
    await user.type(passwordInput, 'wrongpass');
    await user.click(submitButton);
    
    await waitFor(() => {
      expect(screen.getByText(/invalid credentials/i)).toBeInTheDocument();
    });
  });

  it('should disable submit button while loading', async () => {
    const user = userEvent.setup();
    const mockOnSubmit = vi.fn(() => new Promise(resolve => 
      setTimeout(resolve, 1000)
    ));
    
    render(<LoginForm onSubmit={mockOnSubmit} />);
    
    const usernameInput = screen.getByLabelText(/username/i);
    const passwordInput = screen.getByLabelText(/password/i);
    const submitButton = screen.getByRole('button', { name: /login/i });
    
    await user.type(usernameInput, 'testuser');
    await user.type(passwordInput, 'password123');
    await user.click(submitButton);
    
    expect(submitButton).toBeDisabled();
  });
});
```

### Example 2: Order List Component Test

Create: `lego-factory-frontend/src/pages/__tests__/OrdersPage.test.jsx`

```javascript
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen, waitFor } from '@/test/testUtils';
import OrdersPage from '../OrdersPage';

// Mock the API
vi.mock('@/services/api', () => ({
  getOrders: vi.fn()
}));

describe('OrdersPage Component', () => {
  
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should display loading state initially', () => {
    render(<OrdersPage />);
    
    expect(screen.getByText(/loading/i)).toBeInTheDocument();
  });

  it('should display list of orders when data is loaded', async () => {
    const { getOrders } = await import('@/services/api');
    getOrders.mockResolvedValue([
      { id: 1, product: 'Product A', quantity: 5, status: 'PENDING' },
      { id: 2, product: 'Product B', quantity: 10, status: 'COMPLETED' }
    ]);
    
    render(<OrdersPage />);
    
    await waitFor(() => {
      expect(screen.getByText('Product A')).toBeInTheDocument();
      expect(screen.getByText('Product B')).toBeInTheDocument();
    });
  });

  it('should display error message when API fails', async () => {
    const { getOrders } = await import('@/services/api');
    getOrders.mockRejectedValue(new Error('API Error'));
    
    render(<OrdersPage />);
    
    await waitFor(() => {
      expect(screen.getByText(/error.*orders/i)).toBeInTheDocument();
    });
  });

  it('should display empty state when no orders exist', async () => {
    const { getOrders } = await import('@/services/api');
    getOrders.mockResolvedValue([]);
    
    render(<OrdersPage />);
    
    await waitFor(() => {
      expect(screen.getByText(/no orders/i)).toBeInTheDocument();
    });
  });
});
```

---

## 🚀 Phase 6: Test Execution Strategy

### Run Backend Tests

```bash
# Terminal 1: Run all tests
cd user-service
mvn test

# Run specific test
mvn test -Dtest=UserServiceTest

# Run with coverage
mvn test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

### Run Frontend Tests

```bash
# Terminal 2: Run all tests
cd lego-factory-frontend
npm test

# Run in watch mode (re-runs on file changes)
npm test -- --watch

# Run with coverage
npm test -- --coverage

# Run specific test file
npm test -- LoginForm.test.jsx

# View coverage UI
npm test -- --ui
```

---

## 📋 Phase 7: Test Organization for All Services

### Backend: User Service (`src/test/java/io/life/user_service/`)

```
├── BaseIntegrationTest.java          # Base class for integration tests
├── controller/
│   ├── UserControllerIntegrationTest.java
│   ├── AuthControllerIntegrationTest.java
│   └── AdminControllerIntegrationTest.java
├── service/
│   ├── UserServiceTest.java
│   ├── AuthenticationServiceTest.java
│   └── PasswordEncoderTest.java
└── repository/
    └── UserRepositoryTest.java
```

### Backend: Masterdata Service (`src/test/java/io/life/masterdata/`)

```
├── BaseIntegrationTest.java
├── controller/
│   ├── ProductVariantControllerTest.java
│   ├── ModuleControllerTest.java
│   └── PartControllerTest.java
├── service/
│   ├── ProductVariantServiceTest.java
│   ├── ModuleServiceTest.java
│   └── PartServiceTest.java
└── repository/
    ├── ProductVariantRepositoryTest.java
    ├── ModuleRepositoryTest.java
    └── PartRepositoryTest.java
```

### Backend: Inventory Service (`src/test/java/io/life/inventory/`)

```
├── BaseIntegrationTest.java
├── controller/
│   └── StockRecordControllerTest.java
├── service/
│   ├── StockRecordServiceTest.java
│   └── InventoryCalculationTest.java
└── repository/
    └── StockRecordRepositoryTest.java
```

### Frontend: Components (`src/components/__tests__/`)

```
├── LoginForm.test.jsx
├── Dashboard.test.jsx
├── OrderList.test.jsx
├── UserManagement.test.jsx
└── Navigation.test.jsx
```

### Frontend: Pages (`src/pages/__tests__/`)

```
├── LoginPage.test.jsx
├── AdminDashboard.test.jsx
├── ProductsPage.test.jsx
├── OrdersPage.test.jsx
└── InventoryPage.test.jsx
```

### Frontend: Services (`src/services/__tests__/`)

```
├── api.test.js
├── authService.test.js
└── errorHandler.test.js
```

---

## ✅ Step-by-Step Implementation Plan

### Week 1: Foundation

1. **Day 1-2:** Add testing dependencies and configuration (all services + frontend)
2. **Day 3-4:** Create BaseIntegrationTest and test setup files
3. **Day 5:** Write tests for User Service (unit + integration)

### Week 2: Expand Coverage

4. **Day 6-7:** Write tests for Masterdata Service
5. **Day 8-9:** Write tests for Inventory Service
6. **Day 10:** Write tests for Order Processing Service

### Week 3: Frontend & Integration

7. **Day 11-12:** Set up frontend testing framework
8. **Day 13-14:** Write component tests
9. **Day 15:** Write E2E workflow tests

---

## 📊 Testing Checklist

Use this checklist as you implement tests:

### User Service
- [ ] UserServiceTest (unit tests for business logic)
- [ ] UserControllerIntegrationTest (API endpoint tests)
- [ ] AuthenticationServiceTest (login/auth logic)
- [ ] UserRepositoryTest (database queries)

### Masterdata Service
- [ ] ProductVariantServiceTest
- [ ] ProductVariantControllerIntegrationTest
- [ ] ModuleServiceTest
- [ ] ModuleControllerIntegrationTest
- [ ] PartServiceTest
- [ ] PartControllerIntegrationTest

### Inventory Service
- [ ] StockRecordServiceTest
- [ ] StockRecordControllerIntegrationTest
- [ ] InventoryCalculationTest

### Order Processing Service
- [ ] OrderServiceTest
- [ ] OrderControllerIntegrationTest
- [ ] OrderStatusWorkflowTest

### Frontend Components
- [ ] LoginForm.test.jsx
- [ ] Dashboard.test.jsx
- [ ] OrderList.test.jsx
- [ ] UserManagement.test.jsx
- [ ] ProductGrid.test.jsx

### Frontend Pages
- [ ] AdminDashboard.test.jsx
- [ ] OrdersPage.test.jsx
- [ ] InventoryPage.test.jsx

### E2E Workflows
- [ ] Customer Order Fulfillment (Scenario 1)
- [ ] Warehouse Order to Modules Supermarket (Scenario 2)
- [ ] Production Workflow (Scenario 3)

---

## 🎓 Key Testing Principles

### Do's ✅
- Test behavior, not implementation
- Use descriptive test names (what, given, when, then)
- Keep tests independent
- Use assertions that are clear
- Mock external dependencies
- Test edge cases and errors

### Don'ts ❌
- Don't test framework functionality
- Don't create complex test data
- Don't sleep/wait in tests
- Don't test multiple behaviors in one test
- Don't skip error case testing
- Don't leave tests in "pending" state

---

## 📞 Quick Reference

### Common Assertions (AssertJ)

```java
// Strings
assertThat(text).isEqualTo("expected").containsIgnoreCase("test");

// Collections
assertThat(list).hasSize(3).contains("item1", "item2");

// Objects
assertThat(user).hasFieldOrPropertyWithValue("name", "John");

// Numbers
assertThat(count).isGreaterThan(0).isLessThan(100);

// Exceptions
assertThatThrownBy(() -> method()).isInstanceOf(Exception.class);
```

### Common Mockito Commands

```java
// Setup mock behavior
when(mock.method()).thenReturn(value);
when(mock.method(arg)).thenThrow(exception);

// Verify calls
verify(mock, times(1)).method();
verify(mock, never()).method();

// Argument capturing
ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
verify(mock).method(captor.capture());
User captured = captor.getValue();
```

---

## 🚀 Getting Started Now

1. **Open terminal in your project root**
2. **Choose a service to start** (recommend: user-service)
3. **Follow Phase 1** - Add testing dependencies
4. **Follow Phase 2** - Create UserServiceTest
5. **Run:** `mvn test`
6. **Check coverage:** `mvn test jacoco:report`
7. **Move to next service**

You're ready to implement comprehensive testing! Start with the user-service to get familiar with the patterns, then apply them to all other services.
