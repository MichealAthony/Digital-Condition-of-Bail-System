package com.hunts.bail.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Creates the SQLite database file and all tables on first run.
 *
 * The database file is created in the working directory as dcbs.db.
 * If the file already exists, all CREATE TABLE IF NOT EXISTS statements
 * are no-ops, so this is safe to call every time the application starts.
 *
 * Table design:
 *   bail_records       — one row per bail record (scalar fields only)
 *   accused_persons    — one row per accused (linked to bail_records by bail_id)
 *   offenses           — one row per offense per accused
 *   bail_conditions    — one row per condition per bail record
 *   reporting_entries  — one row per visit log entry per bail record
 *   audit_log          — one row per audit event (append-only)
 *   users              — one row per system user account
 *
 * OOD principles:
 *   Single Responsibility — this class only initialises the schema.
 *   Open/Closed           — adding a table means adding a statement here only.
 */
public final class DatabaseInitialiser {

    public static final String DB_URL = "jdbc:sqlite:dcbs.db";

    private DatabaseInitialiser() {}

    /** Call once at application startup to ensure the schema exists. */
    public static void initialise() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite JDBC driver not found. " +
                "Ensure sqlite-jdbc-3.45.1.0.jar is on the classpath.", e);
        }
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            // Enable WAL mode for better concurrent read performance
            stmt.execute("PRAGMA journal_mode=WAL");
            stmt.execute("PRAGMA foreign_keys=ON");

            // ── bail_records ─────────────────────────────────────────────
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS bail_records (
                    bail_id      TEXT PRIMARY KEY,
                    surety_info  TEXT NOT NULL DEFAULT '',
                    status       TEXT NOT NULL DEFAULT 'ACTIVE',
                    created_at   TEXT NOT NULL,
                    updated_at   TEXT NOT NULL
                )
            """);

            // ── accused_persons ───────────────────────────────────────────
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS accused_persons (
                    person_id       TEXT PRIMARY KEY,
                    bail_id         TEXT NOT NULL,
                    full_name       TEXT NOT NULL,
                    date_of_birth   TEXT NOT NULL,
                    national_id     TEXT NOT NULL,
                    address         TEXT NOT NULL,
                    contact_number  TEXT NOT NULL,
                    biometric_ref   TEXT NOT NULL DEFAULT '',
                    photo_base64    TEXT NOT NULL DEFAULT '',
                    FOREIGN KEY (bail_id) REFERENCES bail_records(bail_id) ON DELETE CASCADE
                )
            """);

            // ── offenses (one row per offense per accused) ────────────────
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS offenses (
                    id         INTEGER PRIMARY KEY AUTOINCREMENT,
                    bail_id    TEXT NOT NULL,
                    offense    TEXT NOT NULL,
                    FOREIGN KEY (bail_id) REFERENCES bail_records(bail_id) ON DELETE CASCADE
                )
            """);

            // ── bail_conditions ───────────────────────────────────────────
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS bail_conditions (
                    condition_id        TEXT PRIMARY KEY,
                    bail_id             TEXT NOT NULL,
                    condition_type      TEXT NOT NULL,
                    description         TEXT NOT NULL,
                    parameters          TEXT NOT NULL DEFAULT '',
                    compliance_status   TEXT NOT NULL DEFAULT 'PENDING',
                    FOREIGN KEY (bail_id) REFERENCES bail_records(bail_id) ON DELETE CASCADE
                )
            """);

            // ── reporting_entries ─────────────────────────────────────────
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS reporting_entries (
                    entry_id            TEXT PRIMARY KEY,
                    bail_id             TEXT NOT NULL,
                    condition_id        TEXT NOT NULL,
                    scheduled_date      TEXT NOT NULL,
                    logged_at           TEXT NOT NULL,
                    outcome             TEXT NOT NULL,
                    logged_by_user_id   TEXT NOT NULL,
                    logged_by_username  TEXT NOT NULL,
                    notes               TEXT NOT NULL DEFAULT '',
                    FOREIGN KEY (bail_id) REFERENCES bail_records(bail_id) ON DELETE CASCADE
                )
            """);

            // ── audit_log ─────────────────────────────────────────────────
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS audit_log (
                    log_id           TEXT PRIMARY KEY,
                    acting_user_id   TEXT NOT NULL,
                    acting_username  TEXT NOT NULL,
                    action_type      TEXT NOT NULL,
                    target_id        TEXT NOT NULL DEFAULT '',
                    timestamp        TEXT NOT NULL,
                    details          TEXT NOT NULL DEFAULT ''
                )
            """);

            // ── users ─────────────────────────────────────────────────────
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    user_id       TEXT PRIMARY KEY,
                    username      TEXT NOT NULL UNIQUE,
                    password_hash TEXT NOT NULL,
                    role          TEXT NOT NULL,
                    full_name     TEXT NOT NULL
                )
            """);

            // ── Indexes for common lookups ─────────────────────────────────
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_bail_status    ON bail_records(status)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_national_id    ON accused_persons(national_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_audit_type     ON audit_log(action_type)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_audit_time     ON audit_log(timestamp)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_report_bail    ON reporting_entries(bail_id)");

            System.out.println("[DCBS] Database initialised — dcbs.db ready.");

        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialise database: " + e.getMessage(), e);
        }
    }

    /** Opens and returns a new connection. Caller is responsible for closing it. */
    public static Connection getConnection() throws SQLException {
        try { Class.forName("org.sqlite.JDBC"); } catch (ClassNotFoundException ignored) {}
        return DriverManager.getConnection(DB_URL);
    }
}