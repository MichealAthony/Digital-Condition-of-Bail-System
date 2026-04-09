package com.hunts.bail.domain;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public final class ReportingEntry {

    public enum Outcome { PRESENT, ABSENT, LATE }

    private final String        entryId;
    private final String        bailId;
    private final String        conditionId;
    private final LocalDateTime scheduledDate;
    private final LocalDateTime loggedAt;
    private final Outcome       outcome;
    private final String        loggedByUserId;
    private final String        loggedByUsername;
    private final String        notes;

    /** Standard constructor — generates entryId and loggedAt = now(). */
    public ReportingEntry(String bailId, String conditionId,
                          LocalDateTime scheduledDate, Outcome outcome,
                          String loggedByUserId, String loggedByUsername,
                          String notes) {
        this.entryId          = UUID.randomUUID().toString();
        this.bailId           = Objects.requireNonNull(bailId);
        this.conditionId      = Objects.requireNonNull(conditionId);
        this.scheduledDate    = Objects.requireNonNull(scheduledDate);
        this.loggedAt         = LocalDateTime.now();
        this.outcome          = Objects.requireNonNull(outcome);
        this.loggedByUserId   = Objects.requireNonNull(loggedByUserId);
        this.loggedByUsername = Objects.requireNonNull(loggedByUsername);
        this.notes            = notes != null ? notes : "";
    }

    /** Rehydration constructor — restores all fields exactly as persisted. */
    public static ReportingEntry rehydrate(String entryId, String bailId,
                                            String conditionId, LocalDateTime scheduledDate,
                                            LocalDateTime loggedAt, Outcome outcome,
                                            String loggedByUserId, String loggedByUsername,
                                            String notes) {
        return new ReportingEntry(entryId, bailId, conditionId, scheduledDate,
                loggedAt, outcome, loggedByUserId, loggedByUsername, notes, true);
    }

    /** Private full constructor used by rehydrate() only. */
    private ReportingEntry(String entryId, String bailId, String conditionId,
                            LocalDateTime scheduledDate, LocalDateTime loggedAt,
                            Outcome outcome, String loggedByUserId,
                            String loggedByUsername, String notes, boolean rehydrating) {
        this.entryId          = entryId;
        this.bailId           = bailId;
        this.conditionId      = conditionId;
        this.scheduledDate    = scheduledDate;
        this.loggedAt         = loggedAt;
        this.outcome          = outcome;
        this.loggedByUserId   = loggedByUserId;
        this.loggedByUsername = loggedByUsername;
        this.notes            = notes;
    }

    public String        getEntryId()          { return entryId; }
    public String        getBailId()            { return bailId; }
    public String        getConditionId()       { return conditionId; }
    public LocalDateTime getScheduledDate()     { return scheduledDate; }
    public LocalDateTime getLoggedAt()          { return loggedAt; }
    public Outcome       getOutcome()           { return outcome; }
    public String        getLoggedByUserId()    { return loggedByUserId; }
    public String        getLoggedByUsername()  { return loggedByUsername; }
    public String        getNotes()             { return notes; }
}
