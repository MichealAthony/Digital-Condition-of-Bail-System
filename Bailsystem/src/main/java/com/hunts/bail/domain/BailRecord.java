package com.hunts.bail.domain;

import com.hunts.bail.enums.BailStatus;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Aggregate root for a bail record.
 */
public class BailRecord {

    private final String              bailId;
    private final AccusedPerson       accusedPerson;
    private final List<BailCondition> conditions;
    private final String              suretyInfo;
    private BailStatus                status;
    private final LocalDateTime       createdAt;
    private LocalDateTime             updatedAt;
    private final List<ReportingEntry> reportingLog;

    /** Standard constructor — used by BailRecordFactory for new records. */
    BailRecord(String bailId, AccusedPerson accusedPerson,
               List<BailCondition> conditions, String suretyInfo) {
        this.bailId        = Objects.requireNonNull(bailId);
        this.accusedPerson = Objects.requireNonNull(accusedPerson);
        this.conditions    = new ArrayList<>(Objects.requireNonNull(conditions));
        this.suretyInfo    = suretyInfo != null ? suretyInfo : "";
        this.status        = BailStatus.ACTIVE;
        this.createdAt     = LocalDateTime.now();
        this.updatedAt     = this.createdAt;
        this.reportingLog  = new ArrayList<>();
    }

    /**
     * Rehydration constructor — restores a record from the database
     * with its original timestamps and status preserved.
     * Package-private: only BailRecordRehydrator in the repository layer
     * should call this.
     */
    static BailRecord forRehydration(String bailId, AccusedPerson accusedPerson,
                                      List<BailCondition> conditions, String suretyInfo,
                                      BailStatus status, LocalDateTime createdAt,
                                      LocalDateTime updatedAt) {
        BailRecord r = new BailRecord(bailId, accusedPerson, conditions, suretyInfo);
        r.status    = status;
        // Overwrite the auto-set timestamps
        return new BailRecord(bailId, accusedPerson, conditions, suretyInfo,
                              status, createdAt, updatedAt);
    }

    /** Private full constructor used by forRehydration only. */
    private BailRecord(String bailId, AccusedPerson accusedPerson,
                        List<BailCondition> conditions, String suretyInfo,
                        BailStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.bailId        = Objects.requireNonNull(bailId);
        this.accusedPerson = Objects.requireNonNull(accusedPerson);
        this.conditions    = new ArrayList<>(Objects.requireNonNull(conditions));
        this.suretyInfo    = suretyInfo != null ? suretyInfo : "";
        this.status        = status;
        this.createdAt     = createdAt;
        this.updatedAt     = updatedAt;
        this.reportingLog  = new ArrayList<>();
    }

    /** Attaches a reporting entry without triggering compliance update.
     *  Used only during database rehydration. */
    void attachReportingEntry(ReportingEntry entry) {
        reportingLog.add(Objects.requireNonNull(entry));
    }

    public String             getBailId()        { return bailId; }
    public AccusedPerson      getAccusedPerson() { return accusedPerson; }
    public BailStatus         getStatus()        { return status; }
    public String             getSuretyInfo()    { return suretyInfo; }
    public LocalDateTime      getCreatedAt()     { return createdAt; }
    public LocalDateTime      getUpdatedAt()     { return updatedAt; }
    public List<BailCondition>  getConditions()  { return Collections.unmodifiableList(conditions); }
    public List<ReportingEntry> getReportingLog(){ return Collections.unmodifiableList(reportingLog); }

    public void addCondition(BailCondition c) { conditions.add(Objects.requireNonNull(c)); touch(); }

    public void logReport(ReportingEntry entry) {
        reportingLog.add(Objects.requireNonNull(entry));
        conditions.stream()
                .filter(c -> c.getConditionId().equals(entry.getConditionId()))
                .findFirst()
                .ifPresent(c -> c.updateCompliance(
                        entry.getOutcome() == ReportingEntry.Outcome.PRESENT  ? "COMPLIANT"
                      : entry.getOutcome() == ReportingEntry.Outcome.LATE     ? "COMPLIANT"
                      : "BREACHED"));
        touch();
    }

    public void revoke()   { if (status != BailStatus.ACTIVE) throw new IllegalStateException("Only ACTIVE records can be revoked."); status = BailStatus.REVOKED;   touch(); }
    public void expire()   { status = BailStatus.EXPIRED;   touch(); }
    public void complete() { status = BailStatus.COMPLETED; touch(); }

    private void touch() { this.updatedAt = LocalDateTime.now(); }

    public String getSummary() {
        return String.format("BailRecord[id=%s, accused=%s, status=%s, conditions=%d, reports=%d, created=%s]",
                bailId, accusedPerson.getFullName(), status, conditions.size(), reportingLog.size(), createdAt);
    }
    @Override public String toString() { return getSummary(); }
}
