package com.hunts.bail.domain;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public final class AuditLog {

    private final String        logId;
    private final String        actingUserId;
    private final String        actingUsername;
    private final String        actionType;
    private final String        targetId;
    private final LocalDateTime timestamp;
    private final String        details;

    public AuditLog(String actingUserId, String actingUsername,
                    String actionType, String targetId, String details) {
        this.logId          = UUID.randomUUID().toString();
        this.actingUserId   = Objects.requireNonNull(actingUserId);
        this.actingUsername = Objects.requireNonNull(actingUsername);
        this.actionType     = Objects.requireNonNull(actionType);
        this.targetId       = targetId != null ? targetId : "";
        this.timestamp      = LocalDateTime.now();
        this.details        = details != null ? details : "";
    }

    /** Rehydration — restores exact logId and timestamp from database. */
    public static AuditLog rehydrate(String logId, String actingUserId,
                                      String actingUsername, String actionType,
                                      String targetId, LocalDateTime timestamp,
                                      String details) {
        return new AuditLog(logId, actingUserId, actingUsername,
                actionType, targetId, timestamp, details);
    }

    private AuditLog(String logId, String actingUserId, String actingUsername,
                     String actionType, String targetId, LocalDateTime timestamp,
                     String details) {
        this.logId          = logId;
        this.actingUserId   = actingUserId;
        this.actingUsername = actingUsername;
        this.actionType     = actionType;
        this.targetId       = targetId;
        this.timestamp      = timestamp;
        this.details        = details;
    }

    public String        getLogId()          { return logId; }
    public String        getActingUserId()   { return actingUserId; }
    public String        getActingUsername() { return actingUsername; }
    public String        getActionType()     { return actionType; }
    public String        getTargetId()       { return targetId; }
    public LocalDateTime getTimestamp()      { return timestamp; }
    public String        getDetails()        { return details; }
}
