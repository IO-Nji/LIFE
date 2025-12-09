package io.life.user_service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import io.life.user_service.entity.User;
import io.life.user_service.entity.UserRole;
import io.life.user_service.repository.UserRepository;

@SpringBootTest
class UserServiceTests {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @AfterEach
    void cleanUp() {
        userRepository.deleteAll();
    }

    @Test
    void registerUserHashesPasswordAndPersistsUser() {
        User savedUser = userService.registerUser("alice", "s3cr3t", UserRole.ADMIN, 42L);

        assertNotNull(savedUser.getId());
        assertEquals("alice", savedUser.getUsername());
        assertEquals(UserRole.ADMIN, savedUser.getRole());
        assertEquals(42L, savedUser.getWorkstationId());
        assertNotEquals("s3cr3t", savedUser.getPasswordHash());
        assertTrue(passwordEncoder.matches("s3cr3t", savedUser.getPasswordHash()));
    }

    @Test
    void findByUsernameReturnsSavedUser() {
        userService.registerUser("bob", "password", UserRole.VIEWER, null);

        assertTrue(userService.findByUsername("bob").isPresent());
        assertFalse(userService.findByUsername("missing").isPresent());
    }

    @Test
    void registerUserPreventsDuplicateUsernames() {
        userService.registerUser("charlie", "secret", UserRole.VIEWER, null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> userService.registerUser("charlie", "secret", UserRole.ADMIN, null));
        assertTrue(exception.getMessage().contains("Username already in use"));
    }
}
