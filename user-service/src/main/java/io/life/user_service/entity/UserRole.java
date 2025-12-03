package io.life.user_service.entity;

/**
 * Enumerates supported application roles. Expand as additional workstations are wired in.
 */
public enum UserRole {
    ADMIN,
    PLANT_WAREHOUSE,
    MODULES_SUPERMARKET,
    PRODUCTION_PLANNING,
    PRODUCTION_CONTROL,
    ASSEMBLY_CONTROL,
    PARTS_SUPPLY,
    MANUFACTURING,
    VIEWER
}
