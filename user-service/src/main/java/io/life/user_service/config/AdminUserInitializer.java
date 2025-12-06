package io.life.user_service.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import io.life.user_service.entity.UserRole;
import io.life.user_service.service.UserService;

/**
 * Seeds an initial administrator account for local development so the frontend
 * can authenticate immediately. Passwords are encoded via {@link UserService}.
 * 
 * NOTE: This initializer is maintained for backward compatibility and legacy references.
 * The comprehensive user initialization is now handled by {@link ComprehensiveUserInitializer}.
 * This component will silently succeed if users are already seeded.
 */
@Component
public class AdminUserInitializer implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminUserInitializer.class);
    private static final String DEFAULT_ADMIN_USERNAME = "lego_admin";
    private static final String DEFAULT_ADMIN_PASSWORD = "lego_Pass123";

    private final UserService userService;

    public AdminUserInitializer(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void run(String... args) {
        // This check ensures backward compatibility if the comprehensive initializer hasn't run yet
        // In normal operation, ComprehensiveUserInitializer will handle all user creation
        if (userService.findByUsername(DEFAULT_ADMIN_USERNAME).isEmpty()) {
            createDefaultAdmin();
        }
    }

    private void createDefaultAdmin() {
        LOGGER.info("Provisioning default admin account '{}'.", DEFAULT_ADMIN_USERNAME);
        userService.registerUser(DEFAULT_ADMIN_USERNAME,
            DEFAULT_ADMIN_PASSWORD, UserRole.ADMIN, null);
        LOGGER.debug("Admin bootstrap check complete for '{}'.", DEFAULT_ADMIN_USERNAME);
    }
}
