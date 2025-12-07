package io.life.user_service.dto.user;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.life.user_service.entity.User;

public final class UserMapper {

    private static final Map<Long, String> WORKSTATION_NAMES = new HashMap<>();
    
    static {
        // Workstation ID to Name mappings (from masterdata DataInitializer)
        WORKSTATION_NAMES.put(1L, "Injection Molding Station 1");
        WORKSTATION_NAMES.put(2L, "Parts Pre-Production 1");
        WORKSTATION_NAMES.put(3L, "Part Finishing 1");
        WORKSTATION_NAMES.put(4L, "Gear Assembly 1");
        WORKSTATION_NAMES.put(5L, "Motor Assembly 1");
        WORKSTATION_NAMES.put(6L, "Final Assembly 1");
        WORKSTATION_NAMES.put(7L, "Plant Warehouse");
        WORKSTATION_NAMES.put(8L, "Modules Supermarket");
        WORKSTATION_NAMES.put(9L, "Parts Supply Warehouse");
    }

    private UserMapper() {
    }

    public static UserDto toDto(User user) {
        String workstationName = user.getWorkstationId() != null 
            ? WORKSTATION_NAMES.getOrDefault(user.getWorkstationId(), "Unknown Workstation")
            : null;
        return new UserDto(user.getId(), user.getUsername(), user.getRole(), user.getWorkstationId(), workstationName);
    }

    public static List<UserDto> toDtoList(List<User> users) {
        return users.stream()
            .map(UserMapper::toDto)
            .collect(Collectors.toList());
    }
}
