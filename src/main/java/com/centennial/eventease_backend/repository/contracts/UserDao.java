package com.centennial.eventease_backend.repository.contracts;

import com.centennial.eventease_backend.entities.User;

import java.util.Optional;

public interface UserDao {
    void create(User user);
    Optional<User> findByUsername(String username);
}
