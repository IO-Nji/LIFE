package io.life.user_service.config;

import io.life.user_service.entity.User;
import io.life.user_service.entity.UserRole;
import io.life.user_service.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Seeds a Modules Supermarket operator account for local development.
 * This allows testing Modules Supermarket functionality without manual user creation.
 * 
 * DEPRECATED: This initializer is now superseded by {@link ComprehensiveUserInitializer}.
 * It is kept for reference but is not auto-wired due to removal of @Component annotation.
 * Use the comprehensive initializer for all test user creation.
 */
// @Component - DISABLED: Use ComprehensiveUserInitializer instead
public class ModulesSupermarketUserInitializer implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModulesSupermarketUserInitializer.class);
    private static final String MODULES_SUPERMARKET_USERNAME = "modules_supermarket_user";
    private static final String MODULES_SUPERMARKET_PASSWORD = "modules_supermarket_Pass123";

    private final UserService userService;

    public ModulesSupermarketUserInitializer(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void run(String... args) {
        userService.findByUsername(MODULES_SUPERMARKET_USERNAME)
            .or(this::createModulesSupermarketOperator)
            .ifPresent(user -> LOGGER.debug("Modules Supermarket operator bootstrap check complete for '{}'.", user.getUsername()));
    }

    private Optional<User> createModulesSupermarketOperator() {
        LOGGER.info("Provisioning default Modules Supermarket operator account '{}'.", MODULES_SUPERMARKET_USERNAME);
        
        // Assign Modules Supermarket operator to Modules Supermarket workstation (ID 8)
        // This workstation is seeded by MasterdataService during its initialization
        User user = userService.registerUser(MODULES_SUPERMARKET_USERNAME, MODULES_SUPERMARKET_PASSWORD, UserRole.MODULES_SUPERMARKET, 8L);
        return Optional.of(user);
    }
}
