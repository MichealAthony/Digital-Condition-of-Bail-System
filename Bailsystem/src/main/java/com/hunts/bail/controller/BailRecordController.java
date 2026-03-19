package com.hunts.bail.controller;

import com.hunts.bail.domain.*;
import com.hunts.bail.exception.AuthenticationException;
import com.hunts.bail.exception.ValidationException;
import com.hunts.bail.service.BailRecordService;
import com.hunts.bail.service.NotificationService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class BailRecordController {

    private final BailRecordService   service;
    private final NotificationService notifier;

    public BailRecordController(BailRecordService service, NotificationService notifier) {
        this.service  = Objects.requireNonNull(service);
        this.notifier = Objects.requireNonNull(notifier);
    }

    // UC1
    public BailRecord handleCreateBailRecord(User actor, AccusedPerson accused,
                                              List<BailCondition> conditions, String suretyInfo) {
        try {
            BailRecord r = service.createBailRecord(actor, accused, conditions, suretyInfo);
            notifier.notifySuccess("Bail Record Created",
                "Bail ID: " + r.getBailId() + "  |  Accused: " + r.getAccusedPerson().getFullName());
            return r;
        } catch (AuthenticationException e) { notifier.notifyError("Access Denied", e.getMessage()); return null;
        } catch (ValidationException e)      { notifier.notifyError("Validation Failed", e.getErrors()); return null;
        } catch (Exception e)                { notifier.notifyError("System Error", e.getMessage()); return null; }
    }

    // UC2
    public BailRecord handleManageBailRecord(User actor, String bailId, String action) {
        try {
            BailRecord r = service.manageBailRecord(actor, bailId, action);
            notifier.notifySuccess("Record Updated", "Bail ID " + bailId + " status changed.");
            return r;
        } catch (AuthenticationException e) { notifier.notifyError("Access Denied", e.getMessage()); return null;
        } catch (Exception e)               { notifier.notifyError("Error", e.getMessage()); return null; }
    }

    // UC3
    public List<BailRecord> handleListAllRecords() { return service.getAllRecords(); }
    public List<BailRecord> handleSearch(String query) { return service.search(query); }
    public Optional<BailRecord> handleFindById(String bailId) { return service.findById(bailId); }

    // UC5
    public boolean handleDeleteBailRecord(User actor, String bailId) {
        try {
            service.deleteBailRecord(actor, bailId);
            notifier.notifySuccess("Record Deleted", "Bail ID " + bailId + " permanently removed.");
            return true;
        } catch (AuthenticationException e) { notifier.notifyError("Access Denied", e.getMessage()); return false;
        } catch (Exception e)               { notifier.notifyError("Delete Failed", e.getMessage()); return false; }
    }

    // UC6
    public BailRecord handleUpdateBailRecord(User actor, String bailId, AccusedPerson accused,
                                              List<BailCondition> conditions, String suretyInfo) {
        try {
            BailRecord r = service.updateBailRecord(actor, bailId, accused, conditions, suretyInfo);
            notifier.notifySuccess("Record Updated", "Bail ID " + bailId + " has been updated.");
            return r;
        } catch (AuthenticationException e) { notifier.notifyError("Access Denied", e.getMessage()); return null;
        } catch (ValidationException e)      { notifier.notifyError("Validation Failed", e.getErrors()); return null;
        } catch (Exception e)                { notifier.notifyError("Error", e.getMessage()); return null; }
    }

    // Reporting compliance
    public ReportingEntry handleLogReport(User actor, String bailId, String conditionId,
                                           LocalDateTime scheduledDate,
                                           ReportingEntry.Outcome outcome, String notes) {
        try {
            ReportingEntry entry = service.logReport(actor, bailId, conditionId,
                    scheduledDate, outcome, notes);
            notifier.notifySuccess("Report Logged",
                "Visit recorded for Bail ID " + bailId + " | Outcome: " + outcome);
            return entry;
        } catch (AuthenticationException e) { notifier.notifyError("Access Denied", e.getMessage()); return null;
        } catch (Exception e)               { notifier.notifyError("Error logging report", e.getMessage()); return null; }
    }
}
