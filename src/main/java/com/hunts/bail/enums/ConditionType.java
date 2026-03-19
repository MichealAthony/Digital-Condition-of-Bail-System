package com.hunts.bail.enums;

/**
 * Categories of bail conditions that may be imposed on an accused person.
 */
public enum ConditionType {
    REPORTING_SCHEDULE("Reporting Schedule"),
    CURFEW("Curfew"),
    TRAVEL_RESTRICTION("Travel Restriction"),
    SURETY_BOND("Surety Bond"),
    NON_CONTACT_ORDER("Non-Contact Order"),
    SURRENDER_PASSPORT("Surrender Passport"),
    OTHER("Other");

    private final String displayName;

    ConditionType(String displayName) {
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
