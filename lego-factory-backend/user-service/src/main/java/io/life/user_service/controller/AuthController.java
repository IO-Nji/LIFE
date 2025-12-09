package io.life.user_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.life.user_service.dto.auth.LoginRequest;
import io.life.user_service.dto.auth.LoginResponse;
import io.life.user_service.dto.user.UserMapper;
import io.life.user_service.entity.User;
import io.life.user_service.security.JwtTokenProvider;
import io.life.user_service.security.JwtTokenProvider.JwtToken;
import io.life.user_service.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider,
            UserService userService) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        String username = request.getUsername().trim();
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(username, request.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userService.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        JwtToken token = tokenProvider.createToken(user);
        LoginResponse response = new LoginResponse(token.token(), "Bearer", token.expiresAt(), UserMapper.toDto(user));
        return ResponseEntity.ok(response);
    }

}
