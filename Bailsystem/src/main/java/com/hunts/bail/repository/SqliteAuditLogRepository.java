package com.hunts.bail.repository;

import com.hunts.bail.domain.AuditLog;
import com.hunts.bail.exception.RecordPersistenceException;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * SQLite-backed implementation of AuditLogRepository.
 * The audit_log table is append-only — no update or delete operations
 * are exposed, preserving the immutability of the audit trail.
 */
public class SqliteAuditLogRepository implements AuditLogRepository {

    @Override
    public void save(AuditLog entry) {
        String sql = """
            INSERT INTO audit_log
              (log_id, acting_user_id, acting_username, action_type,
               target_id, timestamp, details)
            VALUES (?,?,?,?,?,?,?)
        """;
        try (Connection conn = DatabaseInitialiser.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, entry.getLogId());
            ps.setString(2, entry.getActingUserId());
            ps.setString(3, entry.getActingUsername());
            ps.setString(4, entry.getActionType());
            ps.setString(5, entry.getTargetId());
            ps.setString(6, entry.getTimestamp().toString());
            ps.setString(7, entry.getDetails());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RecordPersistenceException("Failed to save audit log entry: " + e.getMessage());
        }
    }

    @Override
    public List<AuditLog> findByUserId(String userId) {
        return query("SELECT * FROM audit_log WHERE acting_user_id = ? ORDER BY timestamp DESC", userId);
    }

    @Override
    public List<AuditLog> findByDateRange(LocalDateTime from, LocalDateTime to) {
        String sql = "SELECT * FROM audit_log WHERE timestamp >= ? AND timestamp <= ? ORDER BY timestamp DESC";
        List<AuditLog> list = new ArrayList<>();
        try (Connection conn = DatabaseInitialiser.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, from.toString());
            ps.setString(2, to.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(hydrate(rs));
            }
        } catch (SQLException e) {
            throw new RecordPersistenceException("Failed to query audit log by date: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<AuditLog> findByActionType(String actionType) {
        return query("SELECT * FROM audit_log WHERE action_type = ? ORDER BY timestamp DESC", actionType);
    }

    @Override
    public List<AuditLog> findAll() {
        List<AuditLog> list = new ArrayList<>();
        try (Connection conn = DatabaseInitialiser.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT * FROM audit_log ORDER BY timestamp DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(hydrate(rs));
        } catch (SQLException e) {
            throw new RecordPersistenceException("Failed to list audit log: " + e.getMessage());
        }
        return list;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private List<AuditLog> query(String sql, String param) {
        List<AuditLog> list = new ArrayList<>();
        try (Connection conn = DatabaseInitialiser.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(hydrate(rs));
            }
        } catch (SQLException e) {
            throw new RecordPersistenceException("Failed to query audit log: " + e.getMessage());
        }
        return list;
    }

    private AuditLog hydrate(ResultSet rs) throws SQLException {
        return AuditLog.rehydrate(
                rs.getString("log_id"),
                rs.getString("acting_user_id"),
                rs.getString("acting_username"),
                rs.getString("action_type"),
                rs.getString("target_id"),
                LocalDateTime.parse(rs.getString("timestamp")),
                rs.getString("details"));
    }
}
