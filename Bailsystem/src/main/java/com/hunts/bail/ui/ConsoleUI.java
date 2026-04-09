package com.hunts.bail.ui;

import com.hunts.bail.domain.*;
import com.hunts.bail.controller.BailRecordController;
import com.hunts.bail.enums.ConditionType;
import com.hunts.bail.exception.AuthenticationException;
import com.hunts.bail.service.AuthenticationService;
import com.hunts.bail.service.NotificationService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Simulates the User Interface layer for demonstration purposes.
 *
 * In a production system this would be replaced by a web/desktop front-end.
 * The console UI exercises the full stack (UC1) without any shortcuts.
 *
 * OOD principles applied:
 *  - Separation of Concerns : UI layer only — delegates everything to Controller.
 *  - Single Responsibility  : renders demo scenarios; no business logic here.
 */
public class ConsoleUI {

    private final AuthenticationService authService;
    private final BailRecordController  controller;
    private final NotificationService   notifier;

    public ConsoleUI(AuthenticationService authService,
                     BailRecordController controller,
                     NotificationService notifier) {
        this.authService = authService;
        this.controller  = controller;
        this.notifier    = notifier;
    }

    private static final String CYAN  = "\u001B[36m";
    private static final String BOLD  = "\u001B[1m";
    private static final String RESET = "\u001B[0m";
    private static final String LINE  =
            "═══════════════════════════════════════════════════════════";

    // ── Demo scenarios ────────────────────────────────────────────────────────

    /**
     * Scenario 1: Happy-path – officer creates a valid bail record.
     */
    public void runScenario1_SuccessfulCreate(String username, String password) {
        printScenarioHeader("SCENARIO 1 – Successful Bail Record Creation (UC1 Happy Path)");

        // Step A – Login
        User officer = loginAs(username, password);
        if (officer == null) return;

        // Step B – Build accused person
        AccusedPerson accused = AccusedPerson.builder(UUID.randomUUID().toString())
                .fullName("Marcus Delano Williams")
                .dateOfBirth(LocalDate.of(1990, 4, 15))
                .nationalId("JM1234567")
                .address("14 Harbour View Road, Kingston 11")
                .contactNumber("8761234567")
                .biometricRef("FP-HASH-XY99Z")
                .addOffense("Section 20 – Possession of Firearm Without a Licence")
                .addOffense("Section 22 – Illegal Discharge of Firearm")
                .build();

        // Step C – Build bail conditions
        List<BailCondition> conditions = List.of(
                new BailCondition(
                        UUID.randomUUID().toString(),
                        ConditionType.REPORTING_SCHEDULE,
                        "Accused must report to Hunts Bay Police Station every Tuesday between 9am–12pm.",
                        "Weekly – every Tuesday 09:00–12:00"
                ),
                new BailCondition(
                        UUID.randomUUID().toString(),
                        ConditionType.CURFEW,
                        "Accused must remain at their registered address between 10pm and 6am.",
                        "Daily curfew 22:00–06:00"
                ),
                new BailCondition(
                        UUID.randomUUID().toString(),
                        ConditionType.SURETY_BOND,
                        "Accused must provide surety of JMD $500,000.",
                        "Surety: Janet Williams (mother) – NID: JM9876543"
                )
        );

        // Step D – Submit through controller (full UC1 flow)
        BailRecord result = controller.handleCreateBailRecord(
                officer, accused, conditions, "Surety: Janet Williams (mother)");

        if (result != null) {
            printRecordSummary(result);
        }
    }

    /**
     * Scenario 2: Validation failure – missing fields.
     */
    public void runScenario2_ValidationFailure(String username, String password) {
        printScenarioHeader("SCENARIO 2 – Validation Failure (Missing Required Fields)");

        User officer = loginAs(username, password);
        if (officer == null) return;

        // Deliberately incomplete accused person (no address, no contact, no offenses)
        AccusedPerson incomplete = AccusedPerson.builder(UUID.randomUUID().toString())
                .fullName("") // blank name
                .dateOfBirth(LocalDate.of(2030, 1, 1)) // future date
                .nationalId("bad id!!")  // invalid format
                .address("")
                .contactNumber("not-a-phone")
                .build();

        controller.handleCreateBailRecord(officer, incomplete, List.of(), "");
    }

    /**
     * Scenario 3: Authorisation failure – wrong role.
     */
    public void runScenario3_AuthorisationFailure(String username, String password) {
        printScenarioHeader("SCENARIO 3 – Authorisation Failure (Wrong Role)");
        notifier.notifyInfo("Attempting to log in as a SUPERVISOR and create a bail record...");

        try {
            User supervisor = authService.authenticate(username, password);

            AccusedPerson accused = AccusedPerson.builder(UUID.randomUUID().toString())
                    .fullName("Test Person")
                    .dateOfBirth(LocalDate.of(1985, 6, 20))
                    .nationalId("JM1111111")
                    .address("1 Test Lane, Kingston")
                    .contactNumber("8769876543")
                    .addOffense("Theft")
                    .build();

            List<BailCondition> conditions = List.of(
                    new BailCondition(UUID.randomUUID().toString(),
                            ConditionType.REPORTING_SCHEDULE, "Weekly report", "Every Monday")
            );

            // Supervisor tries to call create — controller/service should block them
            controller.handleCreateBailRecord(supervisor, accused, conditions, "");

        } catch (AuthenticationException e) {
            notifier.notifyError("Login Failed", e.getMessage());
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private User loginAs(String username, String password) {
        try {
            notifier.notifyInfo("Authenticating as '" + username + "'...");
            User user = authService.authenticate(username, password);
            notifier.notifyInfo("Logged in successfully: " + user.getFullName()
                    + "  |  Role: " + user.getRole().getDisplayName());
            return user;
        } catch (AuthenticationException e) {
            notifier.notifyError("Login Failed", e.getMessage());
            return null;
        }
    }

    private void printScenarioHeader(String title) {
        System.out.println("\n" + LINE);
        System.out.println(BOLD + CYAN + "  " + title + RESET);
        System.out.println(LINE);
    }

    private void printRecordSummary(BailRecord record) {
        System.out.println(BOLD + "\n  ── Bail Record Summary ──────────────────────────────" + RESET);
        System.out.println("  Bail ID       : " + record.getBailId());
        System.out.println("  Accused       : " + record.getAccusedPerson().getFullName());
        System.out.println("  National ID   : " + record.getAccusedPerson().getNationalId());
        System.out.println("  Offenses      : " + record.getAccusedPerson().getOffenses());
        System.out.println("  Status        : " + record.getStatus().getDisplayName());
        System.out.println("  Created At    : " + record.getCreatedAt());
        System.out.println("  Conditions    : " + record.getConditions().size());
        record.getConditions().forEach(c ->
                System.out.println("    → [" + c.getConditionType() + "] " + c.getDescription()));
        System.out.println("  Surety Info   : " + record.getSuretyInfo());
        System.out.println();
    }
}
