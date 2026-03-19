package com.hunts.bail;

import com.hunts.bail.controller.BailRecordController;
import com.hunts.bail.domain.User;
import com.hunts.bail.enums.Role;
import com.hunts.bail.repository.*;
import com.hunts.bail.service.*;
import com.hunts.bail.ui.ConsoleUI;

import java.util.UUID;

/**
 * Application entry point.
 *
 * Performs manual Dependency Injection to connect all layers together.
 */
public class BailSystemApplication {

    public static void main(String[] args) {

        // 1. Repositories (infrastructure layer) 
        BailRecordRepository bailRepo     = new InMemoryBailRecordRepository();
        AuditLogRepository   auditRepo    = new InMemoryAuditLogRepository();
        UserRepository       userRepo     = new InMemoryUserRepository();

        // 2. Services (application layer) 
        AuditService         auditService = new AuditService(auditRepo);
        AuthenticationService authService = new AuthenticationService(userRepo, auditService);
        ValidationService    validator    = new ValidationService();
        IDGeneratorService   idGenerator  = new IDGeneratorService(bailRepo);
        NotificationService  notifier     = new NotificationService();

        BailRecordService    bailService  = new BailRecordService(
                bailRepo, validator, idGenerator, auditService, authService);

        // 3. Controller (MVC layer) 
        BailRecordController controller = new BailRecordController(bailService, notifier);

        // 4. Seed demo users 
        //       Production would load these from a database.
        String officerHash    = AuthenticationService.hash("Officer#2026");
        String supervisorHash = AuthenticationService.hash("Super#2026");
        String adminHash      = AuthenticationService.hash("Admin#2026");

        userRepo.save(new User(UUID.randomUUID().toString(),
                "officer.brown", officerHash, Role.POLICE_OFFICER, "Cst. Rohan Brown"));
        userRepo.save(new User(UUID.randomUUID().toString(),
                "sup.hamilton", supervisorHash, Role.SUPERVISOR,   "Sgt. Amoya Hamilton"));
        userRepo.save(new User(UUID.randomUUID().toString(),
                "admin.james",  adminHash,      Role.ADMINISTRATOR, "Adm. Steven-Kyle James"));

        // 5. UI layer (simulates officer interactions) 
        ConsoleUI ui = new ConsoleUI(authService, controller, notifier);

        // Run all three demo scenarios
        ui.runScenario1_SuccessfulCreate ("officer.brown", "Officer#2026");
        ui.runScenario2_ValidationFailure("officer.brown", "Officer#2026");
        ui.runScenario3_AuthorisationFailure("sup.hamilton", "Super#2026");

        // ── 6. Print full audit trail ─────────────────────────────────────────
        System.out.println("\n═══════════════════════════════════════════════════════════");
        System.out.println("\u001B[1m  FULL AUDIT TRAIL\u001B[0m");
        System.out.println("═══════════════════════════════════════════════════════════");
        auditRepo.findAll().forEach(System.out::println);
    }
}
