package io.life.user_service.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

@SpringBootTest
class AuthControllerIntegrationTest {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Test
    void defaultAdminCredentialsAuthenticateSuccessfully() {
        Authentication result = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken("legoAdmin", "legoPass"));

        assertThat(result.isAuthenticated()).isTrue();
        assertThat(result.getName()).isEqualTo("legoAdmin");
    }
}
