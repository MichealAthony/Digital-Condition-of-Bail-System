package com.hunts.bail.enums;

/**
 * Defines the roles available in the Digitalized Condition of Bail System.
 * Role-Based Access Control (RBAC) is enforced via this enum.
 */
public enum Role {
    POLICE_OFFICER("Police Officer"),
    SUPERVISOR("Supervisor"),
    MANAGER("Manager"),
    ADMINISTRATOR("Administrator");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
