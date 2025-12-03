package io.life.user_service.dto.user;

import io.life.user_service.entity.UserRole;

public class UserDto {

    private Long id;
    private String username;
    private UserRole role;
    private Long workstationId;

    public UserDto(Long id, String username, UserRole role, Long workstationId) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.workstationId = workstationId;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public UserRole getRole() {
        return role;
    }

    public Long getWorkstationId() {
        return workstationId;
    }
}
