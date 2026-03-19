package com.hunts.bail.ui.swing;

import com.hunts.bail.controller.BailRecordController;
import com.hunts.bail.domain.User;
import com.hunts.bail.enums.Role;
import com.hunts.bail.repository.*;
import com.hunts.bail.service.*;

import javax.swing.*;
import java.util.UUID;

/**
 * Entry point for the full DCBS Swing GUI.
    * This class performs manual Dependency Injection to connect all layers together, and launches the LoginFrame.
 */
public class BailSwingApp {

    private static AuthenticationService authService;
    private static BailRecordService     bailService;
    private static BailRecordController  controller;
    private static AuditLogRepository    auditRepo;
    private static AuditService          auditService;
    private static UserManagementService userService;
    private static NotificationService   notifier;

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        bootstrap();
        SwingUtilities.invokeLater(BailSwingApp::showLogin);
    }

    private static void bootstrap() {
        BailRecordRepository bailRepo = new InMemoryBailRecordRepository();
        auditRepo                     = new InMemoryAuditLogRepository();
        UserRepository userRepo       = new InMemoryUserRepository();

        auditService = new AuditService(auditRepo);
        authService  = new AuthenticationService(userRepo, auditService);
        notifier     = new NotificationService();
        userService  = new UserManagementService(userRepo, authService, auditService);

        ValidationService  validator = new ValidationService();
        IDGeneratorService idGen     = new IDGeneratorService(bailRepo);

        bailService = new BailRecordService(bailRepo, validator, idGen, auditService, authService);
        controller  = new BailRecordController(bailService, notifier);

        // Seed demo users
        userRepo.save(new User(UUID.randomUUID().toString(), "officer.brown",
                AuthenticationService.hash("Officer#2026"), Role.POLICE_OFFICER, "Cst. Rohan Brown"));
        userRepo.save(new User(UUID.randomUUID().toString(), "sup.hamilton",
                AuthenticationService.hash("Super#2026"),  Role.SUPERVISOR,      "Sgt. Amoya Hamilton"));
        userRepo.save(new User(UUID.randomUUID().toString(), "manager.johnson",
                AuthenticationService.hash("Manager#2026"), Role.MANAGER,        "Insp. Delmika Johnson"));
        userRepo.save(new User(UUID.randomUUID().toString(), "admin.james",
                AuthenticationService.hash("Admin#2026"),  Role.ADMINISTRATOR,   "Adm. Steven-Kyle James"));
    }

    public static void showLogin() {
        SwingUtilities.invokeLater(() -> new LoginFrame(authService, BailSwingApp::showMain).setVisible(true));
    }

    private static void showMain(User user) {
        SwingUtilities.invokeLater(() ->
            new MainFrame(user, bailService, controller, auditRepo, auditService, userService).setVisible(true));
    }
}
