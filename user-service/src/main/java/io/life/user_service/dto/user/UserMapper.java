package io.life.user_service.dto.user;

import java.util.List;
import java.util.stream.Collectors;

import io.life.user_service.entity.User;

public final class UserMapper {

    private UserMapper() {
    }

    public static UserDto toDto(User user) {
        return new UserDto(user.getId(), user.getUsername(), user.getRole(), user.getWorkstationId());
    }

    public static List<UserDto> toDtoList(List<User> users) {
        return users.stream()
            .map(UserMapper::toDto)
            .collect(Collectors.toList());
    }
}
