package com.hunts.bail.service;

import com.hunts.bail.domain.User;
import com.hunts.bail.enums.Role;
import com.hunts.bail.exception.RecordPersistenceException;
import com.hunts.bail.repository.UserRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * UC4 – Manage Users / UC8 – Create User
 * Handles all user account lifecycle operations.
 * Only ADMINISTRATOR role may call these methods.
 */
public class UserManagementService {

    private final UserRepository      userRepository;
    private final AuthenticationService authService;
    private final AuditService         auditService;

    public UserManagementService(UserRepository userRepository,
                                  AuthenticationService authService,
                                  AuditService auditService) {
        this.userRepository = Objects.requireNonNull(userRepository);
        this.authService    = Objects.requireNonNull(authService);
        this.auditService   = Objects.requireNonNull(auditService);
    }

    /** UC8 – Create a new user account. */
    public User createUser(User actor, String username, String plainPassword,
                           Role role, String fullName) {
        authService.requireRole(actor, Role.ADMINISTRATOR);
        if (userRepository.existsByUsername(username)) {
            throw new RecordPersistenceException("Username '" + username + "' is already taken.");
        }
        if (username.isBlank() || plainPassword.isBlank() || fullName.isBlank()) {
            throw new IllegalArgumentException("Username, password and full name are all required.");
        }
        String hash = AuthenticationService.hash(plainPassword);
        User newUser = new User(UUID.randomUUID().toString(), username, hash, role, fullName);
        userRepository.save(newUser);
        auditService.log(actor, AuditService.ACTION_CREATE_USER, newUser.getUserId(),
                "Created user '" + username + "' with role " + role.getDisplayName());
        return newUser;
    }

    /** UC4 – Update user role or full name (password change handled separately). */
    public User updateUser(User actor, String targetUserId, Role newRole, String newFullName) {
        authService.requireRole(actor, Role.ADMINISTRATOR);
        Optional<User> found = userRepository.findById(targetUserId);
        if (found.isEmpty()) throw new RecordPersistenceException("User not found: " + targetUserId);
        User existing = found.get();
        User updated = new User(existing.getUserId(), existing.getUsername(),
                existing.getPasswordHash(), newRole, newFullName);
        userRepository.delete(existing.getUserId());
        userRepository.save(updated);
        auditService.log(actor, AuditService.ACTION_UPDATE_USER, targetUserId,
                "Updated user '" + existing.getUsername() + "': role=" + newRole.getDisplayName());
        return updated;
    }

    /** UC4 – Reset a user's password. */
    public void resetPassword(User actor, String targetUserId, String newPlainPassword) {
        authService.requireRole(actor, Role.ADMINISTRATOR);
        Optional<User> found = userRepository.findById(targetUserId);
        if (found.isEmpty()) throw new RecordPersistenceException("User not found: " + targetUserId);
        User existing = found.get();
        String newHash = AuthenticationService.hash(newPlainPassword);
        User updated = new User(existing.getUserId(), existing.getUsername(),
                newHash, existing.getRole(), existing.getFullName());
        userRepository.delete(existing.getUserId());
        userRepository.save(updated);
        auditService.log(actor, AuditService.ACTION_UPDATE_USER, targetUserId,
                "Password reset for user '" + existing.getUsername() + "'");
    }

    /** UC4 – Delete a user account. */
    public void deleteUser(User actor, String targetUserId) {
        authService.requireRole(actor, Role.ADMINISTRATOR);
        if (actor.getUserId().equals(targetUserId)) {
            throw new IllegalArgumentException("You cannot delete your own account.");
        }
        Optional<User> found = userRepository.findById(targetUserId);
        if (found.isEmpty()) throw new RecordPersistenceException("User not found: " + targetUserId);
        String username = found.get().getUsername();
        userRepository.delete(targetUserId);
        auditService.log(actor, AuditService.ACTION_DELETE_USER, targetUserId,
                "Deleted user '" + username + "'");
    }

    /** Returns all registered users. Admin only. */
    public List<User> getAllUsers(User actor) {
        authService.requireRole(actor, Role.ADMINISTRATOR);
        return userRepository.findAll();
    }

    public Optional<User> findById(String userId) {
        return userRepository.findById(userId);
    }
}
