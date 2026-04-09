package com.hunts.bail.service;

import com.hunts.bail.domain.*;
import com.hunts.bail.enums.BailStatus;
import com.hunts.bail.enums.Role;
import com.hunts.bail.exception.RecordPersistenceException;
import com.hunts.bail.exception.ValidationException;
import com.hunts.bail.repository.BailRecordRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class BailRecordService {

    private final BailRecordRepository  repository;
    private final ValidationService     validationService;
    private final IDGeneratorService    idGeneratorService;
    private final AuditService          auditService;
    private final AuthenticationService authService;

    public BailRecordService(BailRecordRepository repository,
                             ValidationService validationService,
                             IDGeneratorService idGeneratorService,
                             AuditService auditService,
                             AuthenticationService authService) {
        this.repository         = Objects.requireNonNull(repository);
        this.validationService  = Objects.requireNonNull(validationService);
        this.idGeneratorService = Objects.requireNonNull(idGeneratorService);
        this.auditService       = Objects.requireNonNull(auditService);
        this.authService        = Objects.requireNonNull(authService);
    }

    // ── UC1 – Create ─────────────────────────────────────────────────────────
    public BailRecord createBailRecord(User actor, AccusedPerson accused,
                                       List<BailCondition> conditions, String suretyInfo) {
        authService.requireAnyRole(actor, Role.POLICE_OFFICER, Role.SUPERVISOR, Role.MANAGER, Role.ADMINISTRATOR);
        validationService.validateForCreate(accused, conditions, suretyInfo);
        String bailId = idGeneratorService.generateBailId();
        BailRecord record = BailRecordFactory.create(bailId, accused, conditions, suretyInfo);
        repository.save(record);
        auditService.log(actor, AuditService.ACTION_CREATE_BAIL_RECORD, bailId,
                "Created for: " + accused.getFullName() + " | Conditions: " + conditions.size());
        return record;
    }

    // ── UC2 – Manage status ───────────────────────────────────────────────────
    public BailRecord manageBailRecord(User actor, String bailId, String action) {
        authService.requireAnyRole(actor, Role.SUPERVISOR, Role.MANAGER, Role.ADMINISTRATOR);
        BailRecord record = findByIdOrThrow(bailId);
        String detail;
        switch (action.toUpperCase()) {
            case "REVOKE"   -> { record.revoke();   detail = "Record revoked"; }
            case "EXPIRE"   -> { record.expire();   detail = "Record marked expired"; }
            case "COMPLETE" -> { record.complete(); detail = "Record marked complete"; }
            default -> throw new IllegalArgumentException("Unknown action: " + action);
        }
        repository.update(record);
        auditService.log(actor, AuditService.ACTION_MANAGE_BAIL_RECORD, bailId,
                detail + " by " + actor.getUsername());
        return record;
    }

    // ── UC3 – Search ─────────────────────────────────────────────────────────
    public List<BailRecord> getAllRecords() { return repository.findAll(); }
    public Optional<BailRecord> findById(String bailId) { return repository.findById(bailId); }
    public List<BailRecord> search(String query) {
        String q = query.toLowerCase().trim();
        return repository.findAll().stream().filter(r ->
            r.getBailId().toLowerCase().contains(q) ||
            r.getAccusedPerson().getFullName().toLowerCase().contains(q) ||
            r.getAccusedPerson().getNationalId().toLowerCase().contains(q) ||
            r.getAccusedPerson().getOffenses().stream().anyMatch(o -> o.toLowerCase().contains(q)) ||
            r.getStatus().getDisplayName().toLowerCase().contains(q)
        ).collect(Collectors.toList());
    }

    // ── UC5 – Delete ─────────────────────────────────────────────────────────
    public void deleteBailRecord(User actor, String bailId) {
        authService.requireAnyRole(actor, Role.MANAGER, Role.ADMINISTRATOR);
        findByIdOrThrow(bailId);
        repository.delete(bailId);
        auditService.log(actor, AuditService.ACTION_DELETE_BAIL_RECORD, bailId,
                "Permanently deleted by " + actor.getUsername());
    }

    // ── UC6 – Update ─────────────────────────────────────────────────────────
    public BailRecord updateBailRecord(User actor, String bailId,
                                       AccusedPerson newAccused,
                                       List<BailCondition> newConditions,
                                       String newSuretyInfo) {
        authService.requireAnyRole(actor, Role.POLICE_OFFICER, Role.SUPERVISOR,
                Role.MANAGER, Role.ADMINISTRATOR);
        findByIdOrThrow(bailId);
        validationService.validateForCreate(newAccused, newConditions, newSuretyInfo);
        BailRecord updated = BailRecordFactory.create(bailId, newAccused, newConditions, newSuretyInfo);
        repository.update(updated);
        auditService.log(actor, AuditService.ACTION_UPDATE_BAIL_RECORD, bailId,
                "Updated by " + actor.getUsername() + " | Accused: " + newAccused.getFullName());
        return updated;
    }

    // ── Reporting compliance log ──────────────────────────────────────────────
    /**
     * Logs a reporting visit (present / absent / late) for a bail record.
     * Any authenticated officer may log a visit.
     */
    public ReportingEntry logReport(User actor, String bailId, String conditionId,
                                     LocalDateTime scheduledDate,
                                     ReportingEntry.Outcome outcome,
                                     String notes) {
        authService.requireAnyRole(actor, Role.POLICE_OFFICER, Role.SUPERVISOR,
                Role.MANAGER, Role.ADMINISTRATOR);
        BailRecord record = findByIdOrThrow(bailId);
        ReportingEntry entry = new ReportingEntry(bailId, conditionId, scheduledDate,
                outcome, actor.getUserId(), actor.getUsername(), notes);
        record.logReport(entry);
        repository.update(record);
        auditService.log(actor, "LOG_REPORT", bailId,
                "Report logged for " + record.getAccusedPerson().getFullName()
                + " | Outcome: " + outcome + " | Scheduled: " + scheduledDate.toLocalDate());
        return entry;
    }

    private BailRecord findByIdOrThrow(String bailId) {
        return repository.findById(bailId).orElseThrow(() ->
            new RecordPersistenceException("No bail record found with ID: " + bailId));
    }
}
