package io.life.user_service.config;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import io.life.user_service.entity.User;
import io.life.user_service.entity.UserRole;
import io.life.user_service.service.UserService;

/**
 * Seeds an initial administrator account for local development so the frontend
 * can authenticate immediately. Passwords are encoded via {@link UserService}.
 */
@Component
public class AdminUserInitializer implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminUserInitializer.class);
    private static final String DEFAULT_ADMIN_USERNAME = "legoAdmin";
    private static final String DEFAULT_ADMIN_PASSWORD = "legoPass";

    private final UserService userService;

    public AdminUserInitializer(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void run(String... args) {
        userService.findByUsername(DEFAULT_ADMIN_USERNAME)
            .or(() -> createDefaultAdmin())
            .ifPresent(user -> LOGGER.debug("Admin bootstrap check complete for '{}'.", user.getUsername()));
    }

    private Optional<User> createDefaultAdmin() {
        LOGGER.info("Provisioning default admin account '{}'.", DEFAULT_ADMIN_USERNAME);
        return Optional.of(userService.registerUser(DEFAULT_ADMIN_USERNAME,
            DEFAULT_ADMIN_PASSWORD, UserRole.ADMIN, null));
    }
}
