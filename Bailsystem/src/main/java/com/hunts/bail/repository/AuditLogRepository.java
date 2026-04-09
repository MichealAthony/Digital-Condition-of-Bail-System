package com.hunts.bail.repository;

import com.hunts.bail.domain.AuditLog;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for AuditLog persistence.
 *
 * OOD principles applied:
 *  - Dependency Inversion Principle : services depend on this interface,
 *    not a concrete store.
 */
public interface AuditLogRepository {

    /** Persists an audit log entry — must never fail silently. */
    void save(AuditLog entry);

    /** Returns all audit entries for a given user. */
    List<AuditLog> findByUserId(String userId);

    /** Returns all audit entries between two timestamps (inclusive). */
    List<AuditLog> findByDateRange(LocalDateTime from, LocalDateTime to);

    /** Returns all entries of a specific action type. */
    List<AuditLog> findByActionType(String actionType);

    /** Returns all entries. */
    List<AuditLog> findAll();
}
