package com.hunts.bail.domain;

import com.hunts.bail.enums.ConditionType;

import java.util.Objects;

/**
 * Represents a single condition imposed on an accused person as part of their bail.
 *
 * OOD principles applied:
 *  - Encapsulation  : mutable only through an explicit updateCompliance() method.
 *  - Single Responsibility : manages one bail condition and its compliance state.
 */
public class BailCondition {

    private final String conditionId;
    private final ConditionType conditionType;
    private final String description;
    private final String parameters;       // e.g. "Report every Tuesday 9am–12pm"
    private String complianceStatus;       // mutable — updated as accused reports

    public BailCondition(String conditionId,
                         ConditionType conditionType,
                         String description,
                         String parameters) {
        this.conditionId       = Objects.requireNonNull(conditionId,     "conditionId must not be null");
        this.conditionType     = Objects.requireNonNull(conditionType,   "conditionType must not be null");
        this.description       = Objects.requireNonNull(description,     "description must not be null");
        this.parameters        = parameters != null ? parameters : "";
        this.complianceStatus  = "PENDING";
    }

    // ── Getters ────────────────────────────────────────────────────────────────

    public String getConditionId()       { return conditionId; }
    public ConditionType getConditionType() { return conditionType; }
    public String getDescription()       { return description; }
    public String getParameters()        { return parameters; }
    public String getComplianceStatus()  { return complianceStatus; }

    /**
     * Updates compliance status.
     * @param status one of "COMPLIANT", "BREACHED", "PENDING"
     */
    public void updateCompliance(String status) {
        if (!status.equals("COMPLIANT") && !status.equals("BREACHED") && !status.equals("PENDING")) {
            throw new IllegalArgumentException("Invalid compliance status: " + status);
        }
        this.complianceStatus = status;
    }

    public String getSummary() {
        return String.format(
                "BailCondition[id=%s, type=%s, status=%s, params=%s]",
                conditionId, conditionType, complianceStatus, parameters
        );
    }

    @Override
    public String toString() { return getSummary(); }
}
