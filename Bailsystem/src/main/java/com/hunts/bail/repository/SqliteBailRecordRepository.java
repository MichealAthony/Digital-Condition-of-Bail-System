package com.hunts.bail.repository;


import com.hunts.bail.domain.*;
import com.hunts.bail.domain.BailRecordRehydrator;
import com.hunts.bail.enums.BailStatus;
import com.hunts.bail.enums.ConditionType;
import com.hunts.bail.exception.RecordPersistenceException;
// BailRecordRehydrator is in com.hunts.bail.domain — imported via wildcard above

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * SQLite-backed implementation of BailRecordRepository.
 *
 * Replaces InMemoryBailRecordRepository with no changes required
 * to any service, controller, or UI class — this is the Dependency
 * Inversion Principle in practice.
 *
 * Each BailRecord spans five tables:
 *   bail_records, accused_persons, offenses,
 *   bail_conditions, reporting_entries
 *
 * All multi-table writes use transactions so the database is never
 * left in a partially-written state.
 *
 * OOD principles:
 *   Single Responsibility — persistence logic only, no business rules.
 *   Liskov Substitution   — fully substitutable for InMemoryBailRecordRepository.
 *   Open/Closed           — new fields added by extending, not modifying, this class.
 */
public class SqliteBailRecordRepository implements BailRecordRepository {

    // ── Write operations ──────────────────────────────────────────────────────

    @Override
    public void save(BailRecord record) {
        try (Connection conn = DatabaseInitialiser.getConnection()) {
            conn.setAutoCommit(false);
            try {
                insertBailRecord(conn, record);
                insertAccusedPerson(conn, record.getBailId(), record.getAccusedPerson());
                insertOffenses(conn, record.getBailId(), record.getAccusedPerson().getOffenses());
                insertConditions(conn, record.getBailId(), record.getConditions());
                insertReportingEntries(conn, record.getReportingLog());
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RecordPersistenceException("Failed to save bail record: " + e.getMessage());
        }
    }

    @Override
    public void update(BailRecord record) {
        try (Connection conn = DatabaseInitialiser.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Delete dependent rows then re-insert — simplest correct approach
                deleteChildren(conn, record.getBailId());
                updateBailRecord(conn, record);
                insertAccusedPerson(conn, record.getBailId(), record.getAccusedPerson());
                insertOffenses(conn, record.getBailId(), record.getAccusedPerson().getOffenses());
                insertConditions(conn, record.getBailId(), record.getConditions());
                insertReportingEntries(conn, record.getReportingLog());
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RecordPersistenceException("Failed to update bail record: " + e.getMessage());
        }
    }

    @Override
    public void delete(String bailId) {
        // ON DELETE CASCADE handles child rows automatically
        String sql = "DELETE FROM bail_records WHERE bail_id = ?";
        try (Connection conn = DatabaseInitialiser.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bailId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RecordPersistenceException("Failed to delete bail record: " + e.getMessage());
        }
    }

    // ── Read operations ───────────────────────────────────────────────────────

    @Override
    public boolean existsById(String bailId) {
        String sql = "SELECT 1 FROM bail_records WHERE bail_id = ?";
        try (Connection conn = DatabaseInitialiser.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bailId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RecordPersistenceException("Failed to check bail record existence: " + e.getMessage());
        }
    }

    @Override
    public Optional<BailRecord> findById(String bailId) {
        String sql = "SELECT * FROM bail_records WHERE bail_id = ?";
        try (Connection conn = DatabaseInitialiser.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bailId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(hydrate(conn, rs));
            }
        } catch (SQLException e) {
            throw new RecordPersistenceException("Failed to find bail record: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<BailRecord> findAll() {
        String sql = "SELECT * FROM bail_records ORDER BY created_at DESC";
        List<BailRecord> records = new ArrayList<>();
        try (Connection conn = DatabaseInitialiser.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) records.add(hydrate(conn, rs));
        } catch (SQLException e) {
            throw new RecordPersistenceException("Failed to list bail records: " + e.getMessage());
        }
        return records;
    }

    // ── Private insert helpers ────────────────────────────────────────────────

    private void insertBailRecord(Connection conn, BailRecord r) throws SQLException {
        String sql = """
            INSERT INTO bail_records (bail_id, surety_info, status, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, r.getBailId());
            ps.setString(2, r.getSuretyInfo());
            ps.setString(3, r.getStatus().name());
            ps.setString(4, r.getCreatedAt().toString());
            ps.setString(5, r.getUpdatedAt().toString());
            ps.executeUpdate();
        }
    }

    private void updateBailRecord(Connection conn, BailRecord r) throws SQLException {
        String sql = """
            UPDATE bail_records SET surety_info=?, status=?, updated_at=?
            WHERE bail_id=?
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, r.getSuretyInfo());
            ps.setString(2, r.getStatus().name());
            ps.setString(3, r.getUpdatedAt().toString());
            ps.setString(4, r.getBailId());
            ps.executeUpdate();
        }
    }

    private void insertAccusedPerson(Connection conn, String bailId, AccusedPerson ap) throws SQLException {
        String sql = """
            INSERT INTO accused_persons
              (person_id, bail_id, full_name, date_of_birth, national_id,
               address, contact_number, biometric_ref, photo_base64)
            VALUES (?,?,?,?,?,?,?,?,?)
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ap.getPersonId());
            ps.setString(2, bailId);
            ps.setString(3, ap.getFullName());
            ps.setString(4, ap.getDateOfBirth().toString());
            ps.setString(5, ap.getNationalId());
            ps.setString(6, ap.getAddress());
            ps.setString(7, ap.getContactNumber());
            ps.setString(8, ap.getBiometricRef());
            ps.setString(9, ap.getPhotoBase64());
            ps.executeUpdate();
        }
    }

    private void insertOffenses(Connection conn, String bailId, List<String> offenses) throws SQLException {
        String sql = "INSERT INTO offenses (bail_id, offense) VALUES (?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (String o : offenses) {
                ps.setString(1, bailId);
                ps.setString(2, o);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void insertConditions(Connection conn, String bailId, List<BailCondition> conds) throws SQLException {
        String sql = """
            INSERT INTO bail_conditions
              (condition_id, bail_id, condition_type, description, parameters, compliance_status)
            VALUES (?,?,?,?,?,?)
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (BailCondition c : conds) {
                ps.setString(1, c.getConditionId());
                ps.setString(2, bailId);
                ps.setString(3, c.getConditionType().name());
                ps.setString(4, c.getDescription());
                ps.setString(5, c.getParameters());
                ps.setString(6, c.getComplianceStatus());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void insertReportingEntries(Connection conn, List<ReportingEntry> entries) throws SQLException {
        String sql = """
            INSERT INTO reporting_entries
              (entry_id, bail_id, condition_id, scheduled_date, logged_at,
               outcome, logged_by_user_id, logged_by_username, notes)
            VALUES (?,?,?,?,?,?,?,?,?)
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (ReportingEntry e : entries) {
                ps.setString(1, e.getEntryId());
                ps.setString(2, e.getBailId());
                ps.setString(3, e.getConditionId());
                ps.setString(4, e.getScheduledDate().toString());
                ps.setString(5, e.getLoggedAt().toString());
                ps.setString(6, e.getOutcome().name());
                ps.setString(7, e.getLoggedByUserId());
                ps.setString(8, e.getLoggedByUsername());
                ps.setString(9, e.getNotes());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void deleteChildren(Connection conn, String bailId) throws SQLException {
        // accused_persons, offenses, bail_conditions, reporting_entries
        // all have ON DELETE CASCADE so deleting from accused_persons
        // won't help — we delete from the child tables directly
        for (String table : List.of("reporting_entries", "bail_conditions",
                                     "offenses", "accused_persons")) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM " + table + " WHERE bail_id = ?")) {
                ps.setString(1, bailId);
                ps.executeUpdate();
            }
        }
    }

    // ── Hydration (ResultSet → BailRecord) ───────────────────────────────────

    private BailRecord hydrate(Connection conn, ResultSet rs) throws SQLException {
        String bailId     = rs.getString("bail_id");
        String suretyInfo = rs.getString("surety_info");
        BailStatus status = BailStatus.valueOf(rs.getString("status"));
        LocalDateTime createdAt = LocalDateTime.parse(rs.getString("created_at"));
        LocalDateTime updatedAt = LocalDateTime.parse(rs.getString("updated_at"));

        AccusedPerson accused   = loadAccused(conn, bailId);
        List<BailCondition> conditions = loadConditions(conn, bailId);
        List<ReportingEntry> entries   = loadReportingEntries(conn, bailId);

        // Use package-private BailRecordRehydrator to reconstruct the aggregate
        // without going through BailRecordFactory (which sets createdAt = now())
        return BailRecordRehydrator.rehydrate(
                bailId, accused, conditions, suretyInfo,
                status, createdAt, updatedAt, entries);
    }

    private AccusedPerson loadAccused(Connection conn, String bailId) throws SQLException {
        String sql = "SELECT * FROM accused_persons WHERE bail_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bailId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new RecordPersistenceException(
                        "No accused person found for bail_id: " + bailId);
                List<String> offenses = loadOffenses(conn, bailId);
                return AccusedPerson.builder(rs.getString("person_id"))
                        .fullName(rs.getString("full_name"))
                        .dateOfBirth(LocalDate.parse(rs.getString("date_of_birth")))
                        .nationalId(rs.getString("national_id"))
                        .address(rs.getString("address"))
                        .contactNumber(rs.getString("contact_number"))
                        .biometricRef(rs.getString("biometric_ref"))
                        .photoBase64(rs.getString("photo_base64"))
                        .offenses(offenses)
                        .build();
            }
        }
    }

    private List<String> loadOffenses(Connection conn, String bailId) throws SQLException {
        List<String> list = new ArrayList<>();
        String sql = "SELECT offense FROM offenses WHERE bail_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bailId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(rs.getString("offense"));
            }
        }
        return list;
    }

    private List<BailCondition> loadConditions(Connection conn, String bailId) throws SQLException {
        List<BailCondition> list = new ArrayList<>();
        String sql = "SELECT * FROM bail_conditions WHERE bail_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bailId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BailCondition c = new BailCondition(
                            rs.getString("condition_id"),
                            ConditionType.valueOf(rs.getString("condition_type")),
                            rs.getString("description"),
                            rs.getString("parameters"));
                    c.updateCompliance(rs.getString("compliance_status"));
                    list.add(c);
                }
            }
        }
        return list;
    }

    private List<ReportingEntry> loadReportingEntries(Connection conn, String bailId) throws SQLException {
        List<ReportingEntry> list = new ArrayList<>();
        String sql = "SELECT * FROM reporting_entries WHERE bail_id = ? ORDER BY logged_at";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bailId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(ReportingEntry.rehydrate(
                            rs.getString("entry_id"),
                            rs.getString("bail_id"),
                            rs.getString("condition_id"),
                            LocalDateTime.parse(rs.getString("scheduled_date")),
                            LocalDateTime.parse(rs.getString("logged_at")),
                            ReportingEntry.Outcome.valueOf(rs.getString("outcome")),
                            rs.getString("logged_by_user_id"),
                            rs.getString("logged_by_username"),
                            rs.getString("notes")));
                }
            }
        }
        return list;
    }
}
