package io.life.gateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
        return http
            .csrf(csrf -> csrf.disable())
            .authorizeExchange(exchanges -> exchanges
                // Allow public access to root health endpoint only
                .pathMatchers("/").permitAll()
                // Allow CORS preflight requests
                .pathMatchers("OPTIONS", "/**").permitAll()
                // All other requests need authentication
                .anyExchange().authenticated()
            )
            .build();
    }
}