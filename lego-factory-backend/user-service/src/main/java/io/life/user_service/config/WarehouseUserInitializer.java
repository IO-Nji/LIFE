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
 * Seeds a Plant Warehouse operator account for local development.
 * This allows testing warehouse functionality without manual user creation.
 * 
 * DEPRECATED: This initializer is now superseded by {@link ComprehensiveUserInitializer}.
 * It is kept for reference but is not auto-wired due to removal of @Component annotation.
 * Use the comprehensive initializer for all test user creation.
 */
// @Component - DISABLED: Use ComprehensiveUserInitializer instead
public class WarehouseUserInitializer implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(WarehouseUserInitializer.class);
    private static final String WAREHOUSE_USERNAME = "plant_warehouse_user";
    private static final String WAREHOUSE_PASSWORD = "plant_warehouse_Pass123";

    private final UserService userService;

    public WarehouseUserInitializer(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void run(String... args) {
        userService.findByUsername(WAREHOUSE_USERNAME)
            .or(this::createWarehouseOperator)
            .ifPresent(user -> LOGGER.debug("Warehouse operator bootstrap check complete for '{}'.", user.getUsername()));
    }

    private Optional<User> createWarehouseOperator() {
        LOGGER.info("Provisioning default warehouse operator account '{}'.", WAREHOUSE_USERNAME);
        
        // Assign warehouse operator to Plant Warehouse workstation (ID 7)
        // This workstation is seeded by MasterdataService during its initialization
        User user = userService.registerUser(WAREHOUSE_USERNAME, WAREHOUSE_PASSWORD, UserRole.PLANT_WAREHOUSE, 7L);
        return Optional.of(user);
    }
}

