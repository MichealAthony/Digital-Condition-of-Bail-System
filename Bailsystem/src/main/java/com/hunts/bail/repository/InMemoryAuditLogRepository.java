package com.hunts.bail.repository;

import com.hunts.bail.domain.AuditLog;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * In-memory implementation of AuditLogRepository.
 */
public class InMemoryAuditLogRepository implements AuditLogRepository {

    private final List<AuditLog> store = new ArrayList<>();

    @Override
    public void save(AuditLog entry) {
        Objects.requireNonNull(entry, "AuditLog entry must not be null.");
        store.add(entry);
        System.out.println(entry); // Also print to console for visibility during demo
    }

    @Override
    public List<AuditLog> findByUserId(String userId) {
        return store.stream()
                .filter(e -> e.getActingUserId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<AuditLog> findByDateRange(LocalDateTime from, LocalDateTime to) {
        return store.stream()
                .filter(e -> !e.getTimestamp().isBefore(from) && !e.getTimestamp().isAfter(to))
                .collect(Collectors.toList());
    }

    @Override
    public List<AuditLog> findByActionType(String actionType) {
        return store.stream()
                .filter(e -> e.getActionType().equals(actionType))
                .collect(Collectors.toList());
    }

    @Override
    public List<AuditLog> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(store));
    }
}
