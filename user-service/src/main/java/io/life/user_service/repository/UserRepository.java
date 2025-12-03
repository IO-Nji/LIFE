package io.life.user_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import io.life.user_service.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);
}
