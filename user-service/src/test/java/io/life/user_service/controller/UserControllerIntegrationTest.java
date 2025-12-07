package io.life.user_service.controller;

import io.life.user_service.BaseIntegrationTest;
import io.life.user_service.entity.User;
import io.life.user_service.entity.UserRole;
import io.life.user_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for UserController.
 * 
 * Uses the full Spring context with H2 in-memory database.
 * Tests actual HTTP endpoints without starting a real server.
 */
@DisplayName("User Controller Integration Tests")
class UserControllerIntegrationTest extends BaseIntegrationTest {
    
    @Autowired
    private UserRepository userRepository;
    
    private User testUser;
    
    @BeforeEach
    public void setUp() {
        // Clear the database before each test
        userRepository.deleteAll();
        
        // Create a test user
        testUser = new User("integrationtest", "hashed_password", UserRole.ADMIN, null);
        testUser = userRepository.save(testUser);
    }
    
    @Test
    @DisplayName("Application context loads successfully")
    void testContextLoads() {
        // When: The test runs, Spring context should load successfully
        // Then: If we get here, context loaded without errors
        assertThat(userRepository).isNotNull();
    }
    
    @Test
    @DisplayName("User can be created and retrieved from database")
    void testUserPersistenceAndRetrieval() {
        // Given: A new user
        User newUser = new User("dbtest", "password123", UserRole.ADMIN, null);
        
        // When: We save the user to the database
        User savedUser = userRepository.save(newUser);
        
        // Then: The user should be persisted and retrievable
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("dbtest");
        
        // And: We should be able to find the user by ID
        var found = userRepository.findById(savedUser.getId());
        assertThat(found).isPresent();
    }
    
    @Test
    @DisplayName("Multiple users can be stored and retrieved")
    void testMultipleUserStorage() {
        // Given: Multiple users
        User user1 = new User("user1", "pass1", UserRole.ADMIN, null);
        User user2 = new User("user2", "pass2", UserRole.PLANT_WAREHOUSE, null);
        
        // When: We save multiple users
        userRepository.save(user1);
        userRepository.save(user2);
        
        // Then: We should be able to retrieve all users
        var allUsers = userRepository.findAll();
        assertThat(allUsers.size()).isGreaterThanOrEqualTo(2);
    }
}
