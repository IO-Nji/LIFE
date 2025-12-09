package io.life.user_service.service;

import io.life.user_service.entity.User;
import io.life.user_service.entity.UserRole;
import io.life.user_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService.
 * 
 * Uses Mockito to mock the UserRepository and PasswordEncoder dependencies.
 * This allows us to test UserService logic in isolation,
 * without needing a real database.
 * 
 * Test format: Given-When-Then
 * - Given: Setup test data and mocks
 * - When: Call the method we're testing
 * - Then: Verify the result
 */
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
    
    @BeforeEach
    void setUp() {
        // Create a test user that we'll use in multiple tests
        testUser = new User("testuser", "hashed_password", UserRole.ADMIN, null);
    }
    
    @Test
    @DisplayName("Should find user by username when user exists")
    void testFindByUsernameSuccess() {
        // Given: User exists in repository
        when(userRepository.findByUsername("testuser"))
            .thenReturn(Optional.of(testUser));
        
        // When: We search for the user
        Optional<User> result = userService.findByUsername("testuser");
        
        // Then: We should find the user
        assertThat(result)
            .isPresent()
            .contains(testUser);
        
        // Verify that repository was called exactly once
        verify(userRepository, times(1)).findByUsername("testuser");
    }
    
    @Test
    @DisplayName("Should return empty when user does not exist")
    void testFindByUsernameNotFound() {
        // Given: User does not exist in repository
        when(userRepository.findByUsername("nonexistent"))
            .thenReturn(Optional.empty());
        
        // When: We search for the user
        Optional<User> result = userService.findByUsername("nonexistent");
        
        // Then: We should get empty Optional
        assertThat(result).isEmpty();
        
        verify(userRepository, times(1)).findByUsername("nonexistent");
    }
    
    @Test
    @DisplayName("Should return all users")
    void testGetAllUsers() {
        // Given: Multiple users in repository
        User user1 = new User("user1", "hash1", UserRole.ADMIN, null);
        User user2 = new User("user2", "hash2", UserRole.PLANT_WAREHOUSE, null);
        
        when(userRepository.findAll()).thenReturn(java.util.List.of(user1, user2));
        
        // When: We get all users
        var result = userService.findAll();
        
        // Then: All users should be returned
        assertThat(result)
            .hasSize(2)
            .containsExactly(user1, user2);
        
        verify(userRepository).findAll();
    }
    
    @Test
    @DisplayName("Should delete user successfully")
    void testDeleteUserSuccess() {
        // Given: A user exists
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(true);
        
        // When: We delete the user
        userService.deleteUser(userId);
        
        // Then: Repository delete should be called
        verify(userRepository).deleteById(userId);
    }
    
    @Test
    @DisplayName("Should reject registration with duplicate username")
    void testRegisterUserWithDuplicateUsername() {
        // Given: Username already exists in repository
        when(userRepository.existsByUsername("testuser")).thenReturn(true);
        
        // When/Then: Attempting to register with duplicate username should throw exception
        assertThatThrownBy(() -> 
            userService.registerUser("testuser", "password123", UserRole.ADMIN, null)
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Username already in use");
        
        // Verify that save was NOT called
        verify(userRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("Should successfully register new user with unique username")
    void testRegisterUserSuccess() {
        // Given: Username does not exist and password encoder is configured
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("rawpassword123")).thenReturn("encoded_password");
        
        User savedUser = new User("newuser", "encoded_password", UserRole.PLANT_WAREHOUSE, null);
        when(userRepository.save(any())).thenReturn(savedUser);
        
        // When: We register a new user
        User result = userService.registerUser("newuser", "rawpassword123", UserRole.PLANT_WAREHOUSE, null);
        
        // Then: User should be saved with encoded password
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("newuser");
        
        // Verify password was encoded
        verify(passwordEncoder).encode("rawpassword123");
        
        // Verify save was called with correct user data
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getUsername()).isEqualTo("newuser");
    }
}
