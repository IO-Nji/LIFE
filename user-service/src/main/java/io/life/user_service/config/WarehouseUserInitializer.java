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
 */
@Component
public class WarehouseUserInitializer implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(WarehouseUserInitializer.class);
    private static final String WAREHOUSE_USERNAME = "warehouseOperator";
    private static final String WAREHOUSE_PASSWORD = "warehousePass";

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

