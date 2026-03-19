package com.hunts.bail.enums;

/**
 * Represents the lifecycle status of a BailRecord.
 */
public enum BailStatus {
    ACTIVE("Active"),
    EXPIRED("Expired"),
    REVOKED("Revoked"),
    COMPLETED("Completed");

    private final String displayName;

    BailStatus(String displayName) {
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
