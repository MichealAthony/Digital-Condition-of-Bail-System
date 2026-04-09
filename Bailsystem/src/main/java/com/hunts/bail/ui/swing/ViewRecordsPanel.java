package com.hunts.bail.ui.swing;

import com.hunts.bail.controller.BailRecordController;
import com.hunts.bail.domain.AccusedPerson;
import com.hunts.bail.domain.BailRecord;
import com.hunts.bail.enums.Role;
import com.hunts.bail.service.BailRecordService;
import com.hunts.bail.domain.User;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * UC2/UC3/UC5/UC6 – View, search, manage, update, delete and open reporting log.
 */
public class ViewRecordsPanel extends JPanel {

    private final BailRecordService    service;
    private final BailRecordController controller;
    private final User                 currentUser;
    private final Consumer<BailRecord> onEdit;        // → UpdateBailRecordPanel
    private final Consumer<BailRecord> onReportLog;   // → ReportingLogPanel

    private DefaultTableModel tableModel;
    private JTable            table;
    private JTextField        searchField;
    private List<BailRecord>  allRecords;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    public ViewRecordsPanel(BailRecordService service, BailRecordController controller,
                            User currentUser,
                            Consumer<BailRecord> onEdit,
                            Consumer<BailRecord> onReportLog) {
        this.service      = service;
        this.controller   = controller;
        this.currentUser  = currentUser;
        this.onEdit       = onEdit;
        this.onReportLog  = onReportLog;
        setBackground(SwingTheme.BG_MAIN);
        setLayout(new BorderLayout());
        buildContent();
        refresh();
    }

    private void buildContent() {
        // ── Header ────────────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setBackground(SwingTheme.BG_MAIN);
        header.setBorder(new EmptyBorder(24, 28, 12, 28));

        JPanel hl = new JPanel(); hl.setOpaque(false);
        hl.setLayout(new BoxLayout(hl, BoxLayout.Y_AXIS));
        JLabel t = SwingTheme.makeTitle("Bail Records");
        JLabel s = SwingTheme.makeMuted("Search, view, manage, update, delete and track reporting compliance.");
        t.setAlignmentX(Component.LEFT_ALIGNMENT); s.setAlignmentX(Component.LEFT_ALIGNMENT);
        hl.add(t); hl.add(Box.createVerticalStrut(2)); hl.add(s);

        searchField = SwingTheme.makeField();
        searchField.setPreferredSize(new Dimension(260, 36));
        searchField.setToolTipText("Search by name, bail ID, national ID, offense or status…");
        JButton searchBtn  = SwingTheme.makeSecondaryButton("🔍 Search");
        JButton refreshBtn = SwingTheme.makePrimaryButton("↻ Refresh");
        searchBtn.addActionListener(e  -> applySearch());
        refreshBtn.addActionListener(e -> refresh());
        searchField.addActionListener(e -> applySearch());

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controls.setOpaque(false);
        controls.add(searchField); controls.add(searchBtn); controls.add(refreshBtn);
        header.add(hl, BorderLayout.WEST);
        header.add(controls, BorderLayout.EAST);

        // ── Table ─────────────────────────────────────────────────────────────
        String[] cols = {"Photo", "Bail ID", "Accused Name", "National ID", "Offenses", "Status", "Reports", "Created"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) { return c == 0 ? ImageIcon.class : String.class; }
        };
        table = new JTable(tableModel) {
            @Override public Component prepareRenderer(javax.swing.table.TableCellRenderer rend, int row, int col) {
                Component c = super.prepareRenderer(rend, row, col);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? SwingTheme.BG_PANEL : SwingTheme.BG_TABLE_ALT);
                    if (col == 5) {
                        String v = (String) getValueAt(row, col);
                        c.setForeground("Active".equals(v) ? SwingTheme.SUCCESS
                                : "Revoked".equals(v) ? SwingTheme.ERROR : SwingTheme.FG_MUTED);
                    } else c.setForeground(SwingTheme.FG_BODY);
                }
                return c;
            }
        };
        SwingTheme.styleTable(table);
        table.setRowHeight(48);  // taller rows for photo thumbnails
        table.setAutoCreateRowSorter(true);
        table.getColumnModel().getColumn(0).setPreferredWidth(44);   // photo
        table.getColumnModel().getColumn(0).setMaxWidth(48);
        table.getColumnModel().getColumn(1).setPreferredWidth(160);
        table.getColumnModel().getColumn(2).setPreferredWidth(160);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);
        table.getColumnModel().getColumn(4).setPreferredWidth(180);
        table.getColumnModel().getColumn(5).setPreferredWidth(90);
        table.getColumnModel().getColumn(6).setPreferredWidth(80);
        table.getColumnModel().getColumn(7).setPreferredWidth(130);

        // Double-click → detail popup
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0)
                    showDetail(getSelectedRecord());
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(SwingTheme.BG_PANEL);

        // ── Action toolbar ────────────────────────────────────────────────────
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        toolbar.setOpaque(false);

        JButton viewBtn    = SwingTheme.makeSecondaryButton("👁 View Detail");
        JButton reportBtn  = SwingTheme.makePrimaryButton("📋 Log Reports");
        JButton editBtn    = SwingTheme.makeSecondaryButton("✏ Edit");
        JButton manageBtn  = SwingTheme.makeSecondaryButton("⚙ Change Status");
        JButton deleteBtn  = SwingTheme.makeDangerButton("🗑 Delete");

        viewBtn.addActionListener(e   -> { BailRecord r = getSelectedRecord(); if (r != null) showDetail(r); });
        reportBtn.addActionListener(e -> { BailRecord r = getSelectedRecord(); if (r != null) onReportLog.accept(r); });
        editBtn.addActionListener(e   -> { BailRecord r = getSelectedRecord(); if (r != null) onEdit.accept(r); });
        manageBtn.addActionListener(e -> { BailRecord r = getSelectedRecord(); if (r != null) showManageDialog(r); });
        deleteBtn.addActionListener(e -> { BailRecord r = getSelectedRecord(); if (r != null) confirmDelete(r); });

        toolbar.add(viewBtn);
        toolbar.add(reportBtn);  // all roles can log reports
        if (isAnyRole(Role.POLICE_OFFICER, Role.SUPERVISOR, Role.MANAGER, Role.ADMINISTRATOR)) toolbar.add(editBtn);
        if (isAnyRole(Role.SUPERVISOR, Role.MANAGER, Role.ADMINISTRATOR))                      toolbar.add(manageBtn);
        if (isAnyRole(Role.MANAGER, Role.ADMINISTRATOR))                                        toolbar.add(deleteBtn);

        JPanel card = SwingTheme.makeCard();
        card.setLayout(new BorderLayout());
        card.add(scroll,  BorderLayout.CENTER);
        card.add(toolbar, BorderLayout.SOUTH);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(0, 28, 28, 28));
        wrap.add(card);

        add(header, BorderLayout.NORTH);
        add(wrap,   BorderLayout.CENTER);
    }

    public void refresh() {
        allRecords = service.getAllRecords();
        populateTable(allRecords);
        if (searchField != null) searchField.setText("");
    }

    private void applySearch() {
        String q = searchField.getText().trim();
        populateTable(q.isEmpty() ? allRecords : service.search(q));
    }

    private void populateTable(List<BailRecord> records) {
        tableModel.setRowCount(0);
        for (BailRecord r : records) {
            ImageIcon thumb = buildThumb(r.getAccusedPerson());
            long reportCount = r.getReportingLog().size();
            tableModel.addRow(new Object[]{
                thumb,
                r.getBailId(),
                r.getAccusedPerson().getFullName(),
                r.getAccusedPerson().getNationalId(),
                String.join(", ", r.getAccusedPerson().getOffenses()),
                r.getStatus().getDisplayName(),
                reportCount + " visit(s)",
                r.getCreatedAt().format(FMT)
            });
        }
    }

    /** Builds a 40×44 thumbnail icon from the accused's photo, or returns null. */
    private ImageIcon buildThumb(AccusedPerson ap) {
        if (!ap.hasPhoto()) return null;
        try {
            byte[] bytes = Base64.getDecoder().decode(ap.getPhotoBase64());
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
            if (img == null) return null;
            Image scaled = img.getScaledInstance(36, 44, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (Exception e) { return null; }
    }

    private BailRecord getSelectedRecord() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) { JOptionPane.showMessageDialog(this, "Please select a record first."); return null; }
        int modelRow = table.convertRowIndexToModel(viewRow);
        String bailId = (String) tableModel.getValueAt(modelRow, 1);  // col 1 = bail ID (col 0 = photo)
        return allRecords.stream().filter(r -> r.getBailId().equals(bailId)).findFirst().orElse(null);
    }

    // UC2 – Change status
    private void showManageDialog(BailRecord record) {
        String[] actions = {"REVOKE", "EXPIRE", "COMPLETE", "Cancel"};
        int choice = JOptionPane.showOptionDialog(this,
                "Change status of Bail ID: " + record.getBailId()
                + "\nCurrent status: " + record.getStatus().getDisplayName(),
                "Change Bail Record Status", JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE, null, actions, actions[0]);
        if (choice < 0 || choice == 3) return;
        BailRecord updated = controller.handleManageBailRecord(currentUser, record.getBailId(), actions[choice]);
        if (updated != null) refresh();
    }

    // UC5 – Delete
    private void confirmDelete(BailRecord record) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Permanently delete Bail Record " + record.getBailId()
                + "\nAccused: " + record.getAccusedPerson().getFullName()
                + "\n\nThis action cannot be undone.",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            if (controller.handleDeleteBailRecord(currentUser, record.getBailId())) refresh();
        }
    }

    // Detail popup with photo
    private void showDetail(BailRecord r) {
        JPanel main = new JPanel(new BorderLayout(12, 0));
        main.setBackground(SwingTheme.BG_MAIN);

        // Photo panel (left)
        if (r.getAccusedPerson().hasPhoto()) {
            try {
                byte[] bytes = Base64.getDecoder().decode(r.getAccusedPerson().getPhotoBase64());
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
                if (img != null) {
                    Image scaled = img.getScaledInstance(130, 160, Image.SCALE_SMOOTH);
                    JLabel photoLbl = new JLabel(new ImageIcon(scaled));
                    photoLbl.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(SwingTheme.BORDER_CLR),
                            new EmptyBorder(4,4,4,4)));
                    JPanel photoWrap = new JPanel(new BorderLayout());
                    photoWrap.setOpaque(false);
                    photoWrap.add(photoLbl, BorderLayout.NORTH);
                    main.add(photoWrap, BorderLayout.WEST);
                }
            } catch (Exception ignored) {}
        }

        // Text detail
        JTextArea detail = new JTextArea();
        detail.setFont(SwingTheme.FONT_MONO);
        detail.setEditable(false);
        detail.setBackground(SwingTheme.BG_MAIN);
        StringBuilder sb = new StringBuilder();
        sb.append("══════════════════════════════════════\n  BAIL RECORD DETAIL\n══════════════════════════════════════\n");
        sb.append(String.format("  Bail ID     : %s%n  Status      : %s%n  Created     : %s%n  Updated     : %s%n",
                r.getBailId(), r.getStatus().getDisplayName(),
                r.getCreatedAt().format(FMT), r.getUpdatedAt().format(FMT)));
        AccusedPerson ap = r.getAccusedPerson();
        sb.append("\n─── Accused Person ─────────────────────\n");
        sb.append(String.format("  Name        : %s%n  National ID : %s%n  DOB         : %s%n  Address     : %s%n  Contact     : %s%n  Biometric   : %s%n",
                ap.getFullName(), ap.getNationalId(), ap.getDateOfBirth(),
                ap.getAddress(), ap.getContactNumber(), ap.getBiometricRef()));
        sb.append("\n─── Offenses ───────────────────────────\n");
        ap.getOffenses().forEach(o -> sb.append("  • ").append(o).append("\n"));
        sb.append("\n─── Bail Conditions ────────────────────\n");
        r.getConditions().forEach(c -> sb.append(String.format("  [%s] %s%n    Params: %s  |  Compliance: %s%n",
                c.getConditionType().getDisplayName(), c.getDescription(),
                c.getParameters(), c.getComplianceStatus())));
        sb.append("\n─── Surety ─────────────────────────────\n  ")
          .append(r.getSuretyInfo().isBlank() ? "(none)" : r.getSuretyInfo()).append("\n");

        // Reporting summary
        if (!r.getReportingLog().isEmpty()) {
            sb.append("\n─── Reporting Log ──────────────────────\n");
            r.getReportingLog().forEach(e -> sb.append(String.format(
                "  %s  %-8s  logged by %s  %s%n",
                e.getScheduledDate().toLocalDate(), e.getOutcome(),
                e.getLoggedByUsername(),
                e.getNotes().isBlank() ? "" : "– " + e.getNotes())));
        }

        detail.setText(sb.toString());
        JScrollPane sp = new JScrollPane(detail);
        main.add(sp, BorderLayout.CENTER);
        main.setPreferredSize(new Dimension(r.getAccusedPerson().hasPhoto() ? 660 : 520, 500));
        JOptionPane.showMessageDialog(this, main, "Record Detail – " + r.getBailId(), JOptionPane.PLAIN_MESSAGE);
    }

    private boolean isAnyRole(Role... roles) {
        for (Role role : roles) if (currentUser.getRole() == role) return true;
        return false;
    }
}
