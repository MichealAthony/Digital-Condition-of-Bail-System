package com.hunts.bail.ui.swing;

import com.hunts.bail.controller.BailRecordController;
import com.hunts.bail.domain.BailRecord;
import com.hunts.bail.domain.User;
import com.hunts.bail.enums.Role;
import com.hunts.bail.repository.AuditLogRepository;
import com.hunts.bail.service.AuditService;
import com.hunts.bail.service.BailRecordService;
import com.hunts.bail.service.UserManagementService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MainFrame extends JFrame {

    private final User                  currentUser;
    private final BailRecordService     bailService;
    private final BailRecordController  controller;
    private final AuditLogRepository    auditRepo;
    private final AuditService          auditService;
    private final UserManagementService userService;

    private CardLayout           cardLayout;
    private JPanel               contentArea;
    private SidebarPanel         sidebar;

    private DashboardPanel       dashboardPanel;
    private ViewRecordsPanel     viewRecordsPanel;
    private AuditLogPanel        auditLogPanel;
    private AuditReportPanel     auditReportPanel;
    private ManageUsersPanel     manageUsersPanel;

    private JPanel               editWrapper;       // UC6
    private JPanel               reportingWrapper;  // Reporting log

    private static final String  CARD_EDIT      = "EditRecord";
    private static final String  CARD_REPORTING = "ReportingLog";

    public MainFrame(User currentUser,
                     BailRecordService bailService,
                     BailRecordController controller,
                     AuditLogRepository auditRepo,
                     AuditService auditService,
                     UserManagementService userService) {
        this.currentUser  = currentUser;
        this.bailService   = bailService;
        this.controller    = controller;
        this.auditRepo     = auditRepo;
        this.auditService  = auditService;
        this.userService   = userService;
        initUI();
    }

    private void initUI() {
        setTitle("DCBS – Bail Management System  |  "
                + currentUser.getFullName() + "  (" + currentUser.getRole().getDisplayName() + ")");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 800);
        setMinimumSize(new Dimension(960, 620));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(SwingTheme.BG_MAIN);
        setContentPane(root);

        sidebar = new SidebarPanel(currentUser, this::navigateTo);

        cardLayout  = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setBackground(SwingTheme.BG_MAIN);

        editWrapper      = new JPanel(new BorderLayout()); editWrapper.setBackground(SwingTheme.BG_MAIN);
        reportingWrapper = new JPanel(new BorderLayout()); reportingWrapper.setBackground(SwingTheme.BG_MAIN);

        dashboardPanel   = buildDashboard();
        viewRecordsPanel = new ViewRecordsPanel(bailService, controller, currentUser,
                this::openEditRecord, this::openReportingLog);
        auditLogPanel    = new AuditLogPanel(auditRepo);
        auditReportPanel = new AuditReportPanel(auditService, currentUser);

        contentArea.add(dashboardPanel,       SidebarPanel.PAGE_DASHBOARD);
        contentArea.add(buildCreateWrapper(), SidebarPanel.PAGE_CREATE_BAIL);
        contentArea.add(viewRecordsPanel,     SidebarPanel.PAGE_VIEW_RECORDS);
        contentArea.add(auditLogPanel,        SidebarPanel.PAGE_AUDIT_LOG);
        contentArea.add(auditReportPanel,     SidebarPanel.PAGE_AUDIT_REPORT);
        contentArea.add(editWrapper,          CARD_EDIT);
        contentArea.add(reportingWrapper,     CARD_REPORTING);

        if (currentUser.getRole() == Role.ADMINISTRATOR) {
            manageUsersPanel = new ManageUsersPanel(userService, currentUser);
            contentArea.add(manageUsersPanel, SidebarPanel.PAGE_MANAGE_USERS);
        }

        root.add(buildTopBar(),  BorderLayout.NORTH);
        root.add(sidebar,        BorderLayout.WEST);
        root.add(contentArea,    BorderLayout.CENTER);

        cardLayout.show(contentArea, SidebarPanel.PAGE_DASHBOARD);
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(SwingTheme.BG_PANEL);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, SwingTheme.BORDER_CLR),
                new EmptyBorder(8, 20, 8, 20)));
        JLabel title = new JLabel("Digitalized Condition of Bail System");
        title.setFont(SwingTheme.FONT_HEADING); title.setForeground(SwingTheme.NAVY);
        JButton logoutBtn = SwingTheme.makeSecondaryButton("Log Out");
        logoutBtn.addActionListener(e -> logout());
        bar.add(title,     BorderLayout.WEST);
        bar.add(logoutBtn, BorderLayout.EAST);
        return bar;
    }

    private DashboardPanel buildDashboard() {
        return new DashboardPanel(bailService, currentUser,
                () -> navigateTo(SidebarPanel.PAGE_CREATE_BAIL));
    }

    private JPanel buildCreateWrapper() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(SwingTheme.BG_MAIN);
        wrapper.add(new CreateBailRecordPanel(controller, currentUser, () ->
            SwingUtilities.invokeLater(() -> {
                refreshAll();
                navigateTo(SidebarPanel.PAGE_VIEW_RECORDS);
            })
        ), BorderLayout.CENTER);
        return wrapper;
    }

    /** UC6 – open edit form for a specific record */
    private void openEditRecord(BailRecord record) {
        editWrapper.removeAll();
        editWrapper.add(new UpdateBailRecordPanel(controller, currentUser, record, () ->
            SwingUtilities.invokeLater(() -> {
                refreshAll();
                navigateTo(SidebarPanel.PAGE_VIEW_RECORDS);
            })
        ), BorderLayout.CENTER);
        editWrapper.revalidate();
        editWrapper.repaint();
        sidebar.setActive(SidebarPanel.PAGE_VIEW_RECORDS);
        cardLayout.show(contentArea, CARD_EDIT);
    }

    /** Reporting log – open for a specific record */
    private void openReportingLog(BailRecord record) {
        reportingWrapper.removeAll();
        reportingWrapper.add(new ReportingLogPanel(controller, currentUser, record, () ->
            SwingUtilities.invokeLater(() -> {
                refreshAll();
                navigateTo(SidebarPanel.PAGE_VIEW_RECORDS);
            })
        ), BorderLayout.CENTER);
        reportingWrapper.revalidate();
        reportingWrapper.repaint();
        sidebar.setActive(SidebarPanel.PAGE_VIEW_RECORDS);
        cardLayout.show(contentArea, CARD_REPORTING);
    }

    public void navigateTo(String page) {
        sidebar.setActive(page);
        switch (page) {
            case SidebarPanel.PAGE_VIEW_RECORDS -> viewRecordsPanel.refresh();
            case SidebarPanel.PAGE_AUDIT_LOG    -> auditLogPanel.refresh();
            case SidebarPanel.PAGE_MANAGE_USERS -> { if (manageUsersPanel != null) manageUsersPanel.refresh(); }
        }
        cardLayout.show(contentArea, page);
    }

    private void refreshAll() {
        contentArea.remove(dashboardPanel);
        dashboardPanel = buildDashboard();
        contentArea.add(dashboardPanel, SidebarPanel.PAGE_DASHBOARD);
        viewRecordsPanel.refresh();
        auditLogPanel.refresh();
    }

    private void logout() {
        int ok = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to log out?", "Confirm Logout",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) { dispose(); BailSwingApp.showLogin(); }
    }
}
