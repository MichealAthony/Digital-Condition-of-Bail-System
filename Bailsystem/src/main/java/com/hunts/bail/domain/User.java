package com.hunts.bail.domain;

import com.hunts.bail.enums.Role;

import java.util.Objects;

/**
 * Represents an authorised system user.
 *
 * OOD principles applied:
 *  - Encapsulation : password stored as hash; never returned as plain text.
 *  - Single Responsibility : holds user identity and role only.
 */
public class User {

    private final String userId;
    private final String username;
    private final String passwordHash;   // store only a hash — never plain text
    private final Role role;
    private final String fullName;

    public User(String userId, String username, String passwordHash,
                Role role, String fullName) {
        this.userId       = Objects.requireNonNull(userId);
        this.username     = Objects.requireNonNull(username);
        this.passwordHash = Objects.requireNonNull(passwordHash);
        this.role         = Objects.requireNonNull(role);
        this.fullName     = Objects.requireNonNull(fullName);
    }

    public String getUserId()      { return userId; }
    public String getUsername()    { return username; }
    public String getPasswordHash(){ return passwordHash; }
    public Role getRole()          { return role; }
    public String getFullName()    { return fullName; }

    public boolean hasRole(Role requiredRole) {
        return this.role == requiredRole;
    }

    @Override
    public String toString() {
        return String.format("User[id=%s, username=%s, role=%s]", userId, username, role);
    }
}
