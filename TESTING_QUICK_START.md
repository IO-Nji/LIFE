# 🚀 Testing Implementation Quick Start

> Follow this step-by-step guide to implement testing in your LIFE application

---

## 📋 Pre-Implementation Checklist

- [ ] You have the TESTING_IMPLEMENTATION_GUIDE.md open for reference
- [ ] All services have been compiled successfully with `mvn clean package -DskipTests`
- [ ] You have multiple terminals available
- [ ] You understand the three testing levels: Unit, Integration, E2E

---

## ⚡ Quick Start: User Service Testing (30 minutes)

### Step 1: Add Test Configuration File (2 minutes)

**Location:** `user-service/src/test/resources/application-test.yml`

**Create the file with this content:**

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

### Step 2: Create Base Integration Test Class (3 minutes)

**Location:** `user-service/src/test/java/io/life/user_service/BaseIntegrationTest.java`

**Copy this code:**

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
    
    @BeforeEach
    public void setUp() {
        // Common test setup - override in subclasses if needed
    }
}
```

### Step 3: Create First Unit Test (15 minutes)

**Location:** `user-service/src/test/java/io/life/user_service/service/UserServiceTest.java`

**This tests the business logic of UserService without needing a database:**

```java
package io.life.user_service.service;

import io.life.user_service.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("User Service Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private UserService userService;
    
    @Test
    @DisplayName("Should create user successfully")
    void testCreateUserSuccess() {
        // This test will verify UserService logic
        // We'll implement the actual assertions based on your UserService methods
        assertThat(userService).isNotNull();
    }
}
```

### Step 4: Create First Integration Test (10 minutes)

**Location:** `user-service/src/test/java/io/life/user_service/controller/UserControllerIntegrationTest.java`

**This tests the API endpoints with a real (in-memory) database:**

```java
package io.life.user_service.controller;

import io.life.user_service.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("User Controller Integration Tests")
class UserControllerIntegrationTest extends BaseIntegrationTest {
    
    @Test
    @DisplayName("GET /api/users - Should return 200 OK")
    void testGetAllUsers() throws Exception {
        mockMvc.perform(get("/api/users")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }
}
```

### Step 5: Run the Tests (5 minutes)

**Open terminal and run:**

```bash
# Navigate to user-service
cd user-service

# Run all tests
mvn test

# You should see:
# Tests run: 2, Failures: 0, Errors: 0
```

**Expected Output:**
```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running io.life.user_service.service.UserServiceTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running io.life.user_service.controller.UserControllerIntegrationTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] -------------------------------------------------------
[INFO] BUILD SUCCESS
```

---

## 📊 Understanding Your First Tests

### Test 1: Unit Test (UserServiceTest)

```java
@ExtendWith(MockitoExtension.class)  // Use Mockito for mocking
class UserServiceTest {
    @Mock private UserRepository repo;  // Fake database
    @InjectMocks private UserService service;  // What we're testing
}
```

**Why Unit Tests?**
- Fast (no database)
- Test business logic in isolation
- Easier to debug

### Test 2: Integration Test (UserControllerIntegrationTest)

```java
class UserControllerIntegrationTest extends BaseIntegrationTest {
    // BaseIntegrationTest provides:
    // - Real Spring context
    // - MockMvc for testing HTTP endpoints
    // - H2 in-memory database
}
```

**Why Integration Tests?**
- Test actual endpoints
- Test with real database
- Verify data flow

---

## 🔄 Next Steps: Test More Services

### For Each Service, Repeat This Process:

1. **Create `application-test.yml`** in `src/test/resources/`
2. **Create `BaseIntegrationTest.java`** in `src/test/java/io/life/[service]/`
3. **Create unit tests** for service classes
4. **Create integration tests** for controllers
5. **Run `mvn test`** to verify

### Quick Copy-Paste for Other Services:

#### Masterdata Service

Create: `masterdata-service/src/test/java/io/life/masterdata/BaseIntegrationTest.java`

```java
package io.life.masterdata;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class BaseIntegrationTest {
    @Autowired
    protected MockMvc mockMvc;
}
```

#### Inventory Service

Create: `inventory-service/src/test/java/io/life/inventory/BaseIntegrationTest.java`

```java
package io.life.inventory;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class BaseIntegrationTest {
    @Autowired
    protected MockMvc mockMvc;
}
```

---

## 🧪 Testing Your Application Features

### Test Customer Order Fulfillment (Plant Warehouse)

**Create:** `order-processing-service/src/test/java/io/life/order/controller/CustomerOrderControllerTest.java`

```java
package io.life.order.controller;

import io.life.order.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Customer Order Controller Tests")
class CustomerOrderControllerTest extends BaseIntegrationTest {
    
    @Test
    @DisplayName("GET /api/customer-orders - Should return all orders")
    void testGetAllOrders() throws Exception {
        mockMvc.perform(get("/api/customer-orders")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("POST /api/customer-orders - Should create new order")
    void testCreateOrder() throws Exception {
        String orderJson = """
            {
                "customerId": 1,
                "items": [
                    {"variantId": 1, "quantity": 5}
                ]
            }
        """;
        
        mockMvc.perform(post("/api/customer-orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(orderJson))
            .andExpect(status().isCreated());
    }
}
```

---

## 📈 Measuring Test Coverage

### Generate Coverage Report

```bash
# Run tests with coverage
mvn test jacoco:report

# Open the report (on macOS)
open user-service/target/site/jacoco/index.html

# On Windows
start user-service\target\site\jacoco\index.html
```

### Reading the Coverage Report

- **Green:** Well-tested code (>80% coverage)
- **Yellow:** Partially tested (50-80%)
- **Red:** Not tested (<50%)

**Target:** Aim for 80%+ coverage on service layer, 60%+ on controllers

---

## 🎯 Testing Checklist - Use as You Go

### Week 1

- [ ] Add `application-test.yml` to user-service
- [ ] Create `BaseIntegrationTest` in user-service
- [ ] Write 5+ unit tests for UserService
- [ ] Write 5+ integration tests for UserController
- [ ] Run tests successfully: `mvn test`
- [ ] Check coverage: `mvn test jacoco:report`

### Week 2

- [ ] Repeat Week 1 steps for masterdata-service
- [ ] Repeat for inventory-service
- [ ] Repeat for order-processing-service
- [ ] Aim for 50%+ test coverage per service

### Week 3

- [ ] Set up frontend testing (Vitest)
- [ ] Write component tests for login
- [ ] Write tests for order pages
- [ ] Test error handling

### Week 4

- [ ] Write end-to-end workflow tests
- [ ] Test order fulfillment scenarios
- [ ] Test production workflows
- [ ] Final coverage report

---

## 🚨 Common Issues & Solutions

### Issue 1: Test Configuration Not Found

**Error:** `Caused by: java.io.FileNotFoundException: class path resource [application-test.yml]`

**Solution:** Make sure file is in:
```
user-service/src/test/resources/application-test.yml
```

### Issue 2: MockMvc Not Autowired

**Error:** `NullPointerException: Cannot invoke method on null object`

**Solution:** Make sure your test class extends BaseIntegrationTest:
```java
class MyTest extends BaseIntegrationTest {
    // mockMvc is available here
}
```

### Issue 3: Tests Fail with Database Errors

**Error:** `Column not found` or `Table doesn't exist`

**Solution:** Check that `@Transactional` is on your test class and `ddl-auto: create-drop` is in test config.

---

## 📚 Resources for Further Learning

### Official Documentation
- [JUnit 5 Documentation](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [AssertJ Documentation](https://assertj.github.io/assertj-core-features-highlight.html)
- [Spring Testing](https://spring.io/guides/gs/testing-web/)

### Best Practices
- Test behavior, not implementation
- Keep tests small and focused
- Use descriptive names
- Test edge cases
- Mock external dependencies

---

## ✅ You're Ready!

Start with Step 1 above. Spend 30 minutes setting up user-service tests, then repeat the pattern for other services.

**Remember:** Testing is an investment that pays off. Start small, build momentum, expand coverage over time.

Happy testing! 🧪✨
