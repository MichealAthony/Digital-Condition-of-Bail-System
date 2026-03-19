package com.hunts.bail.repository;

import com.hunts.bail.domain.User;
import com.hunts.bail.exception.RecordPersistenceException;

import java.util.*;

public class InMemoryUserRepository implements UserRepository {

    private final Map<String, User> byId      = new LinkedHashMap<>();
    private final Map<String, User> byUsername = new LinkedHashMap<>();

    @Override public void save(User user) {
        Objects.requireNonNull(user);
        if (byUsername.containsKey(user.getUsername())) {
            throw new RecordPersistenceException("Username '" + user.getUsername() + "' is already taken.");
        }
        byId.put(user.getUserId(), user);
        byUsername.put(user.getUsername(), user);
    }

    @Override public Optional<User> findByUsername(String username) { return Optional.ofNullable(byUsername.get(username)); }
    @Override public Optional<User> findById(String userId)         { return Optional.ofNullable(byId.get(userId)); }
    @Override public boolean existsByUsername(String username)      { return byUsername.containsKey(username); }

    @Override public void delete(String userId) {
        User user = byId.remove(userId);
        if (user != null) byUsername.remove(user.getUsername());
    }

    @Override public List<User> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(byId.values()));
    }
}
