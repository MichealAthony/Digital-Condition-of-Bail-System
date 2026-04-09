package com.hunts.bail.service;

import com.hunts.bail.domain.AuditLog;
import com.hunts.bail.domain.User;
import com.hunts.bail.repository.AuditLogRepository;

import java.util.List;
import java.util.Objects;

public class AuditService {

    public static final String ACTION_CREATE_BAIL_RECORD  = "CREATE_BAIL_RECORD";
    public static final String ACTION_UPDATE_BAIL_RECORD  = "UPDATE_BAIL_RECORD";
    public static final String ACTION_DELETE_BAIL_RECORD  = "DELETE_BAIL_RECORD";
    public static final String ACTION_MANAGE_BAIL_RECORD  = "MANAGE_BAIL_RECORD";
    public static final String ACTION_LOGIN               = "USER_LOGIN";
    public static final String ACTION_LOGIN_FAILED        = "USER_LOGIN_FAILED";
    public static final String ACTION_CREATE_USER         = "CREATE_USER";
    public static final String ACTION_UPDATE_USER         = "UPDATE_USER";
    public static final String ACTION_DELETE_USER         = "DELETE_USER";
    public static final String ACTION_GENERATE_REPORT     = "GENERATE_AUDIT_REPORT";

    private final AuditLogRepository repository;

    public AuditService(AuditLogRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    public void log(User actor, String actionType, String targetId, String details) {
        Objects.requireNonNull(actor, "Actor must not be null when logging.");
        repository.save(new AuditLog(actor.getUserId(), actor.getUsername(), actionType, targetId, details));
    }

    public void log(User actor, String actionType, String details) {
        log(actor, actionType, "N/A", details);
    }

    public List<AuditLog> getAll() { return repository.findAll(); }
}
