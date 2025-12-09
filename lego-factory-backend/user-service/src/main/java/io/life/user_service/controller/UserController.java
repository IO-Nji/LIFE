package io.life.user_service.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.life.user_service.dto.user.UserCreateRequest;
import io.life.user_service.dto.user.UserDto;
import io.life.user_service.dto.user.UserMapper;
import io.life.user_service.dto.user.UserUpdateRequest;
import io.life.user_service.entity.User;
import io.life.user_service.service.UserService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public UserDto currentUser(Authentication authentication) {
        User user = userService.findByUsername(authentication.getName())
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + authentication.getName()));
        return UserMapper.toDto(user);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserDto> listUsers() {
        return UserMapper.toDtoList(userService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserDto getUserById(@PathVariable Long id) {
        return userService.findById(id)
            .map(UserMapper::toDto)
            .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserCreateRequest request) {
        User user = userService.registerUser(request.getUsername(), request.getPassword(), request.getRole(),
            request.getWorkstationId());
        return ResponseEntity.status(HttpStatus.CREATED).body(UserMapper.toDto(user));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserDto updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        User user = userService.updateUser(id, request.getUsername(), request.getRole(), request.getWorkstationId(),
            request.getPassword());
        return UserMapper.toDto(user);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }
}
