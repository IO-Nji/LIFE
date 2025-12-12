package io.life.inventory.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                // Allow public access to root health endpoint only
                .requestMatchers("/").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                // Allow CORS preflight requests
                .requestMatchers("OPTIONS", "/**").permitAll()
                // All other requests need authentication
                .anyRequest().authenticated()
            )
            // Disable frame options for H2 console
            .headers(headers -> headers.frameOptions().disable());

        return http.build();
    }
}