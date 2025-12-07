package io.life.user_service.dto.user;

import io.life.user_service.entity.UserRole;

public class UserDto {

    private Long id;
    private String username;
    private UserRole role;
    private Long workstationId;
    private String workstationName;

    public UserDto(Long id, String username, UserRole role, Long workstationId) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.workstationId = workstationId;
        this.workstationName = null;
    }

    public UserDto(Long id, String username, UserRole role, Long workstationId, String workstationName) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.workstationId = workstationId;
        this.workstationName = workstationName;
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

    public String getWorkstationName() {
        return workstationName;
    }

    public void setWorkstationName(String workstationName) {
        this.workstationName = workstationName;
    }

}
