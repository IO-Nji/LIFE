# Coding Standards and Conventions

## Java/Spring Boot Standards

### Package Structure
```
com.example.{service}/
├── controller/          # REST endpoints (@RestController)
│   ├── dto/            # Data Transfer Objects
│   └── advice/         # Global exception handlers
├── service/            # Business logic (@Service)
│   └── impl/           # Service implementations
├── repository/         # Data access (@Repository)
├── model/              # JPA entities
├── config/             # Configuration classes
├── security/           # Security configurations
└── utils/              # Utility classes
```

### Naming Conventions

#### Classes and Interfaces
- **Controllers**: `{Domain}Controller` (e.g., `UserController`)
- **Services**: `{Domain}Service` interface, `{Domain}ServiceImpl` implementation
- **Repositories**: `{Entity}Repository`
- **Entities**: Singular noun (e.g., `User`, `InventoryItem`)
- **DTOs**: `{Purpose}Dto` (e.g., `LoginRequestDto`, `UserResponseDto`)

#### Methods and Variables
- **RESTful Endpoints**: HTTP verbs + nouns
  ```java
  @GetMapping("/users/{id}")
  @PostMapping("/users")
  @PutMapping("/users/{id}")
  @DeleteMapping("/users/{id}")
  ```
- **Service Methods**: Action-oriented
  ```java
  public User createUser(CreateUserRequest request)
  public List<User> findUsersByRole(Role role)
  public void deleteUserById(Long id)
  ```

### Annotation Standards

#### Controller Layer
```java
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "${app.frontend.url}")
@Validated
@Slf4j
public class UserController {
    
    @PostMapping("/login")
    @Operation(summary = "User authentication")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        // Implementation
    }
}
```

#### Service Layer
```java
@Service
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {
    
    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        // Implementation
    }
}
```

#### Repository Layer
```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmail(@Param("email") String email);
}
```

### Configuration Patterns

#### Database Configuration
```java
@Configuration
@EnableJpaRepositories
@EnableTransactionManagement
public class DatabaseConfig {
    
    @Bean
    @ConfigurationProperties("spring.datasource")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }
}
```

#### Security Configuration
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .anyRequest().authenticated()
            )
            .build();
    }
}
```

## React/Frontend Standards

### Component Structure
```
src/
├── components/         # Reusable UI components
│   ├── common/        # Generic components (Button, Modal)
│   └── domain/        # Domain-specific components
├── pages/             # Route-level components
├── services/          # API communication
├── hooks/             # Custom React hooks
├── context/           # React Context providers
├── utils/             # Utility functions
└── styles/            # CSS/SCSS files
```

### Component Naming
- **Components**: PascalCase (e.g., `UserProfile`, `InventoryTable`)
- **Files**: Match component name (e.g., `UserProfile.jsx`)
- **Props**: camelCase with descriptive names
- **Handlers**: `handle{Action}` (e.g., `handleSubmit`, `handleUserClick`)

### React Patterns

#### Function Components with Hooks
```javascript
import React, { useState, useEffect, useCallback } from 'react';
import { useAuth } from '../context/AuthContext';

const UserProfile = ({ userId }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const { token } = useAuth();

  const fetchUser = useCallback(async () => {
    try {
      setLoading(true);
      const response = await userService.getUser(userId, token);
      setUser(response.data);
    } catch (error) {
      console.error('Failed to fetch user:', error);
    } finally {
      setLoading(false);
    }
  }, [userId, token]);

  useEffect(() => {
    fetchUser();
  }, [fetchUser]);

  if (loading) return <div>Loading...</div>;
  if (!user) return <div>User not found</div>;

  return (
    <div className="user-profile">
      <h2>{user.name}</h2>
      <p>{user.email}</p>
    </div>
  );
};

export default UserProfile;
```

#### API Service Pattern
```javascript
// services/userService.js
import axios from 'axios';

const API_BASE = '/api/users';

export const userService = {
  async login(credentials) {
    const response = await axios.post(`${API_BASE}/login`, credentials);
    return response.data;
  },

  async getProfile(token) {
    const response = await axios.get(`${API_BASE}/profile`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    return response.data;
  },

  async updateProfile(profileData, token) {
    const response = await axios.put(`${API_BASE}/profile`, profileData, {
      headers: { Authorization: `Bearer ${token}` }
    });
    return response.data;
  }
};
```

## Error Handling Standards

### Backend Error Handling
```java
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException ex) {
        log.warn("Validation error: {}", ex.getMessage());
        return ResponseEntity.badRequest()
            .body(new ErrorResponse("VALIDATION_ERROR", ex.getMessage()));
    }
    
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(EntityNotFoundException ex) {
        log.warn("Entity not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse("NOT_FOUND", ex.getMessage()));
    }
}
```

### Frontend Error Handling
```javascript
// hooks/useApi.js
import { useState, useCallback } from 'react';

export const useApi = () => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const execute = useCallback(async (apiCall) => {
    try {
      setLoading(true);
      setError(null);
      const result = await apiCall();
      return result;
    } catch (err) {
      setError(err.response?.data?.message || 'An error occurred');
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  return { execute, loading, error };
};
```

## Testing Standards

### Unit Test Structure
```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private UserServiceImpl userService;
    
    @Test
    @DisplayName("Should create user successfully")
    void shouldCreateUserSuccessfully() {
        // Given
        CreateUserRequest request = new CreateUserRequest("test@example.com", "password");
        User expectedUser = new User("test@example.com");
        when(userRepository.save(any(User.class))).thenReturn(expectedUser);
        
        // When
        User result = userService.createUser(request);
        
        // Then
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        verify(userRepository).save(any(User.class));
    }
}
```

### Integration Test Pattern
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:testdb")
class UserControllerIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void shouldCreateUserViaApi() {
        // Given
        CreateUserRequest request = new CreateUserRequest("test@example.com", "password");
        
        // When
        ResponseEntity<UserResponse> response = restTemplate.postForEntity(
            "/api/users", request, UserResponse.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getEmail()).isEqualTo("test@example.com");
    }
}
```

## Documentation Standards

### OpenAPI/Swagger Documentation
```java
@RestController
@Tag(name = "User Management", description = "User operations")
public class UserController {
    
    @PostMapping("/users")
    @Operation(summary = "Create new user", description = "Creates a new user account")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "User created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "409", description = "User already exists")
    })
    public ResponseEntity<UserResponse> createUser(
        @Valid @RequestBody 
        @Parameter(description = "User creation data") 
        CreateUserRequest request) {
        // Implementation
    }
}
```

### Code Comments
- **JavaDoc**: For public APIs and complex business logic
- **Inline Comments**: For non-obvious implementation details
- **TODO Comments**: For planned improvements with JIRA ticket references

```java
/**
 * Calculates inventory reorder point based on lead time and consumption rate.
 * 
 * @param item the inventory item
 * @param leadTimeDays supplier lead time in days
 * @return recommended reorder point quantity
 */
public int calculateReorderPoint(InventoryItem item, int leadTimeDays) {
    // TODO: LIFE-123 - Implement seasonal demand adjustment
    return item.getDailyConsumption() * leadTimeDays + item.getSafetyStock();
}
```