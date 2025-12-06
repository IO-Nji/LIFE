package io.life.user_service.config;

import io.life.user_service.entity.User;
import io.life.user_service.entity.UserRole;
import io.life.user_service.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Comprehensive User Initializer - Seeds all test users for system-wide testing.
 * 
 * Naming Convention:
 * - Username: {role_name}_user (lowercase, underscores)
 * - Password: {role_name}_Pass123 (consistent pattern)
 * 
 * Workstation Assignments:
 * - Manufacturing roles: WS-1, WS-2, WS-3 (Injection Molding, Parts Pre-Production, Part Finishing)
 * - Assembly roles: WS-4, WS-5, WS-6 (Gear Assembly, Motor Assembly, Final Assembly)
 * - Warehouse roles: WS-7, WS-8, WS-9 (Plant Warehouse, Modules Supermarket, Parts Supply)
 * - Planning/Control roles: null (global scope)
 * 
 * Expected Workstation IDs (from DataInitializer):
 * 1. Injection Molding Station 1
 * 2. Parts Pre-Production 1
 * 3. Part Finishing 1
 * 4. Gear Assembly 1
 * 5. Motor Assembly 1
 * 6. Final Assembly 1
 * 7. Plant Warehouse
 * 8. Modules Supermarket
 * 9. Parts Supply Warehouse
 */
@Component
public class ComprehensiveUserInitializer implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComprehensiveUserInitializer.class);

    /**
     * Test User Configuration
     * Format: {username, password, role, workstationId}
     */
    private static class TestUserConfig {
        final String username;
        final String password;
        final UserRole role;
        final Long workstationId;
        final String description;

        TestUserConfig(String username, String password, UserRole role, Long workstationId, String description) {
            this.username = username;
            this.password = password;
            this.role = role;
            this.workstationId = workstationId;
            this.description = description;
        }
    }

    private final UserService userService;

    public ComprehensiveUserInitializer(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void run(String... args) {
        LOGGER.info("Starting comprehensive user initialization...");
        List<TestUserConfig> testUsers = getTestUserConfigs();
        
        int created = 0;
        int existing = 0;
        
        for (TestUserConfig config : testUsers) {
            if (userService.findByUsername(config.username).isEmpty()) {
                userService.registerUser(config.username, config.password, config.role, config.workstationId);
                LOGGER.info("✓ Created user '{}' with role {} assigned to workstation {}. {}",
                    config.username, config.role, 
                    config.workstationId != null ? config.workstationId : "N/A",
                    config.description);
                created++;
            } else {
                LOGGER.debug("User '{}' already exists, skipping.", config.username);
                existing++;
            }
        }
        
        LOGGER.info("User initialization complete: {} created, {} existing", created, existing);
    }

    /**
     * Returns list of all test user configurations.
     * Maintains uniform naming: {role}_user / {role}_Pass123
     */
    private List<TestUserConfig> getTestUserConfigs() {
        return Arrays.asList(
            // ========== ADMIN ROLE ==========
            new TestUserConfig("lego_admin", "lego_Pass123", UserRole.ADMIN, null,
                "System administrator - full access"),

            // ========== WAREHOUSE ROLES (WS-7, WS-8, WS-9) ==========
            new TestUserConfig("plant_warehouse_user", "plant_warehouse_Pass123", UserRole.PLANT_WAREHOUSE, 7L,
                "Plant Warehouse (Main) - order creation point"),
            new TestUserConfig("modules_supermarket_user", "modules_supermarket_Pass123", UserRole.MODULES_SUPERMARKET, 8L,
                "Modules Supermarket - module fulfillment"),
            new TestUserConfig("parts_supply_user", "parts_supply_Pass123", UserRole.PARTS_SUPPLY, 9L,
                "Parts Supply Warehouse - parts fulfillment"),

            // ========== PLANNING & CONTROL ROLES (No workstation) ==========
            new TestUserConfig("production_planning_user", "production_planning_Pass123", UserRole.PRODUCTION_PLANNING, null,
                "Production Planning - order scheduling and SimAL integration"),
            new TestUserConfig("production_control_user", "production_control_Pass123", UserRole.PRODUCTION_CONTROL, null,
                "Production Control - order control and status management"),
            new TestUserConfig("assembly_control_user", "assembly_control_Pass123", UserRole.ASSEMBLY_CONTROL, null,
                "Assembly Control - assembly station coordination"),

            // ========== MANUFACTURING WORKSTATIONS (WS-1, WS-2, WS-3) ==========
            new TestUserConfig("injection_molding_user", "injection_molding_Pass123", UserRole.MANUFACTURING, 1L,
                "Injection Molding Station 1 - plastic component production"),
            new TestUserConfig("parts_preproduction_user", "parts_preproduction_Pass123", UserRole.MANUFACTURING, 2L,
                "Parts Pre-Production Station 1 - component assembly"),
            new TestUserConfig("part_finishing_user", "part_finishing_Pass123", UserRole.MANUFACTURING, 3L,
                "Part Finishing Station 1 - quality control and finishing"),

            // ========== ASSEMBLY WORKSTATIONS (WS-4, WS-5, WS-6) ==========
            new TestUserConfig("gear_assembly_user", "gear_assembly_Pass123", UserRole.ASSEMBLY_CONTROL, 4L,
                "Gear Assembly Station 1 - gear module assembly"),
            new TestUserConfig("motor_assembly_user", "motor_assembly_Pass123", UserRole.ASSEMBLY_CONTROL, 5L,
                "Motor Assembly Station 1 - motor module assembly"),
            new TestUserConfig("final_assembly_user", "final_assembly_Pass123", UserRole.ASSEMBLY_CONTROL, 6L,
                "Final Assembly Station 1 - product final assembly"),

            // ========== VIEWER ROLE (No workstation) ==========
            new TestUserConfig("viewer_user", "viewer_Pass123", UserRole.VIEWER, null,
                "Viewer - read-only access to dashboards")
        );
    }
}
