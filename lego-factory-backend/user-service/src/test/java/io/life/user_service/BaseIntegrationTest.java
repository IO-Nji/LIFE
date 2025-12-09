package io.life.user_service;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base class for integration tests in user-service.
 * 
 * Provides:
 * - Spring application context (full stack)
 * - MockMvc for testing HTTP endpoints
 * - H2 in-memory database (configured in application-test.yml)
 * - Transaction management for test isolation
 * 
 * Usage: Extend this class in your integration tests to get all setup automatically.
 * 
 * Example:
 * <pre>
 * @DisplayName("User Controller Tests")
 * class UserControllerIntegrationTest extends BaseIntegrationTest {
 *     @Test
 *     void testGetAllUsers() throws Exception {
 *         mockMvc.perform(get("/api/users"))
 *             .andExpect(status().isOk());
 *     }
 * }
 * </pre>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class BaseIntegrationTest {
    
    /**
     * MockMvc is used to test HTTP endpoints without starting a server.
     * Available in all subclasses.
     */
    @Autowired
    protected MockMvc mockMvc;
    
    /**
     * Common setup for all tests.
     * Override in subclasses if you need test-specific initialization.
     */
    @BeforeEach
    public void setUp() {
        // Common test setup can go here
        // By default, each test runs in its own transaction
        // and the transaction is rolled back after the test
    }
}
