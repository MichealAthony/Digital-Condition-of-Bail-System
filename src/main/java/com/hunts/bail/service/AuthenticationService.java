package com.hunts.bail.service;

import com.hunts.bail.domain.User;
import com.hunts.bail.enums.Role;
import com.hunts.bail.exception.AuthenticationException;
import com.hunts.bail.repository.UserRepository;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;
import java.util.Optional;

public class AuthenticationService {

    private final UserRepository userRepository;
    private final AuditService auditService;

    public AuthenticationService(UserRepository userRepository, AuditService auditService) {
        this.userRepository = Objects.requireNonNull(userRepository);
        this.auditService   = Objects.requireNonNull(auditService);
    }

    public User authenticate(String username, String plainPassword) {
        Objects.requireNonNull(username,      "Username required.");
        Objects.requireNonNull(plainPassword, "Password required.");
        Optional<User> found = userRepository.findByUsername(username);
        if (found.isEmpty() || !found.get().getPasswordHash().equals(hash(plainPassword))) {
            throw new AuthenticationException("Invalid username or password. Access denied.");
        }
        User user = found.get();
        auditService.log(user, AuditService.ACTION_LOGIN, "User '" + username + "' logged in successfully.");
        return user;
    }

    /** Requires exactly this role. */
    public void requireRole(User user, Role requiredRole) {
        Objects.requireNonNull(user);
        Objects.requireNonNull(requiredRole);
        if (!user.hasRole(requiredRole)) {
            throw new AuthenticationException(
                "Access denied. Role required: " + requiredRole.getDisplayName()
                + ". Your role: " + user.getRole().getDisplayName());
        }
    }

    /** Requires the user to hold at least one of the given roles. */
    public void requireAnyRole(User user, Role... permittedRoles) {
        Objects.requireNonNull(user);
        for (Role r : permittedRoles) {
            if (user.hasRole(r)) return;
        }
        throw new AuthenticationException(
            "Access denied. Your role (" + user.getRole().getDisplayName()
            + ") does not have permission for this action.");
    }

    /** Returns true if the user holds any of the specified roles. */
    public boolean hasAnyRole(User user, Role... roles) {
        for (Role r : roles) if (user.hasRole(r)) return true;
        return false;
    }

    public static String hash(String plainText) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(plainText.getBytes());
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 unavailable.", e);
        }
    }
}
