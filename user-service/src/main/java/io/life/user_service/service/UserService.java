package io.life.user_service.service;

import java.util.Objects;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.life.user_service.entity.User;
import io.life.user_service.entity.UserRole;
import io.life.user_service.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User registerUser(String username, String rawPassword, UserRole role, Long workstationId) {
        validateInputs(username, rawPassword, role);
        if (userRepository.existsByUsername(username.trim())) {
            throw new IllegalArgumentException("Username already in use: " + username);
        }

        String encodedPassword = passwordEncoder.encode(rawPassword);
        User user = new User(username.trim(), encodedPassword, role, workstationId);
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        if (username == null) {
            return Optional.empty();
        }
        return userRepository.findByUsername(username.trim());
    }

    private void validateInputs(String username, String rawPassword, UserRole role) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username must be provided");
        }
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("Password must be provided");
        }
        Objects.requireNonNull(role, "role");
    }
}
