# DCBS – Digitalized Condition of Bail System
## Data Persistence — SQLite Setup Guide

Data is persisted in a local SQLite database file (`dcbs.db`) created automatically
in the working directory on first run. All bail records, users, conditions, reporting
visits, and audit entries survive application restarts.

---

## Step 1 — Download the SQLite JDBC driver

  If SQLite JDBC driver is not bundled due to file size. Download it once:

**URL:** https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.45.1.0/sqlite-jdbc-3.45.1.0.jar

Place the downloaded jar in the `lib/` 
```

---

## Step 2 — Compile

```bash
find src -name "*.java" > sources.txt
javac -cp lib/sqlite-jdbc-3.45.1.0.jar -d out @sources.txt
```

---

## Step 3 — Run

**macOS / Linux:**
```bash
java -cp "out:lib/sqlite-jdbc-3.45.1.0.jar" com.hunts.bail.ui.swing.BailSwingApp
```

**Windows:**
```cmd
java -cp "out;lib\sqlite-jdbc-3.45.1.0.jar" com.hunts.bail.ui.swing.BailSwingApp
```

---

## What happens on first run

1. `dcbs.db` is created in the working directory
2. All 7 tables are created (`bail_records`, `accused_persons`, `offenses`,
   `bail_conditions`, `reporting_entries`, `audit_log`, `users`)
3. The four default user accounts are seeded into the `users` table
4. On all subsequent runs, the schema creation is a no-op and seeding is skipped

---

## Demo credentials

| Username         | Password      | Role           |
|------------------|---------------|----------------|
| officer.brown    | Officer#2026  | Police Officer |
| sup.hamilton     | Super#2026    | Supervisor     |
| manager.johnson  | Manager#2026  | Manager        |
| admin.james      | Admin#2026    | Administrator  |

---



## files 

| File | Purpose |
|------|---------|
| `repository/DatabaseInitialiser.java` | Creates `dcbs.db` and all tables on startup |
| `repository/SqliteBailRecordRepository.java` | JDBC impl of `BailRecordRepository` |
| `repository/SqliteAuditLogRepository.java` | JDBC impl of `AuditLogRepository` |
| `repository/SqliteUserRepository.java` | JDBC impl of `UserRepository` |
| `repository/BailRecordRehydrator.java` | Reconstructs `BailRecord` from DB rows |

Domain classes updated with rehydration support (no API changes):
- `BailRecord.java` — added `forRehydration()` and `attachReportingEntry()`
- `ReportingEntry.java` — added static `rehydrate()` factory method
- `AuditLog.java` — added static `rehydrate()` factory method
