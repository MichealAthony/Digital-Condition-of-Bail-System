package com.hunts.bail.repository;

import com.hunts.bail.domain.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    void save(User user);
    Optional<User> findByUsername(String username);
    Optional<User> findById(String userId);
    boolean existsByUsername(String username);
    void delete(String userId);
    List<User> findAll();
}
