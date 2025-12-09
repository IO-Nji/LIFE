package io.life.user_service.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
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
        validateUsernameAndRole(username, role);
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("Password must be provided");
        }
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

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return userRepository.findById(id);
    }

    @Transactional
    public User updateUser(Long id, String username, UserRole role, Long workstationId, String rawPassword) {
        if (id == null) {
            throw new IllegalArgumentException("User id must be provided");
        }
        validateUsernameAndRole(username, role);

        User user = userRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));

        String trimmedUsername = username.trim();
        if (!user.getUsername().equals(trimmedUsername) && userRepository.existsByUsername(trimmedUsername)) {
            throw new IllegalArgumentException("Username already in use: " + trimmedUsername);
        }

        user.setUsername(trimmedUsername);
        user.setRole(role);
        user.setWorkstationId(workstationId);

        if (rawPassword != null && !rawPassword.isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(rawPassword));
        }

        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("User id must be provided");
        }
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    private void validateUsernameAndRole(String username, UserRole role) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username must be provided");
        }
        if (role == null) {
            throw new IllegalArgumentException("Role must be provided");
        }
    }
}
