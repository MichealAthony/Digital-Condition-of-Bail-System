package com.hunts.bail.ui.swing;

import com.hunts.bail.domain.AuditLog;
import com.hunts.bail.domain.User;
import com.hunts.bail.service.AuditService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * UC7 – Generate Audit Report.
 * Filters audit log by date range and/or action type, with export to text.
 */
public class AuditReportPanel extends JPanel {

    private final AuditService  auditService;
    private final User          currentUser;
    private DefaultTableModel   tableModel;

    private JTextField  fromDateField, toDateField;
    private JComboBox<String> actionFilter;
    private JLabel      countLabel;

    private static final DateTimeFormatter FMT     = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public AuditReportPanel(AuditService auditService, User currentUser) {
        this.auditService  = auditService;
        this.currentUser   = currentUser;
        setBackground(SwingTheme.BG_MAIN);
        setLayout(new BorderLayout());
        buildContent();
        generateReport();
    }

    private void buildContent() {
        // ── Header ────────────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(SwingTheme.BG_MAIN);
        header.setBorder(new EmptyBorder(24, 28, 12, 28));
        JPanel hl = new JPanel(); hl.setOpaque(false);
        hl.setLayout(new BoxLayout(hl, BoxLayout.Y_AXIS));
        JLabel t = SwingTheme.makeTitle("Audit Report");
        JLabel s = SwingTheme.makeMuted("Filter and export audit trail records.");
        t.setAlignmentX(Component.LEFT_ALIGNMENT); s.setAlignmentX(Component.LEFT_ALIGNMENT);
        hl.add(t); hl.add(Box.createVerticalStrut(2)); hl.add(s);
        header.add(hl, BorderLayout.WEST);

        // ── Filter bar ────────────────────────────────────────────────────────
        JPanel filterCard = SwingTheme.makeCard();
        filterCard.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 6));
        filterCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SwingTheme.BORDER_CLR),
                new EmptyBorder(8, 12, 8, 12)));

        fromDateField = SwingTheme.makeField(); fromDateField.setPreferredSize(new Dimension(110, 32));
        fromDateField.setText(LocalDate.now().minusMonths(1).format(DATE_FMT));
        toDateField   = SwingTheme.makeField(); toDateField.setPreferredSize(new Dimension(110, 32));
        toDateField.setText(LocalDate.now().format(DATE_FMT));

        String[] actions = {"All Actions",
                AuditService.ACTION_CREATE_BAIL_RECORD,
                AuditService.ACTION_UPDATE_BAIL_RECORD,
                AuditService.ACTION_DELETE_BAIL_RECORD,
                AuditService.ACTION_MANAGE_BAIL_RECORD,
                AuditService.ACTION_LOGIN,
                AuditService.ACTION_CREATE_USER,
                AuditService.ACTION_UPDATE_USER,
                AuditService.ACTION_DELETE_USER};
        actionFilter = SwingTheme.makeCombo(actions);
        actionFilter.setPreferredSize(new Dimension(200, 32));

        JButton genBtn    = SwingTheme.makePrimaryButton("Generate Report");
        JButton exportBtn = SwingTheme.makeSecondaryButton("Export to Text");
        genBtn.addActionListener(e    -> generateReport());
        exportBtn.addActionListener(e -> exportReport());

        countLabel = SwingTheme.makeMuted("0 records");

        filterCard.add(SwingTheme.makeLabel("From:"));  filterCard.add(fromDateField);
        filterCard.add(SwingTheme.makeLabel("To:"));    filterCard.add(toDateField);
        filterCard.add(SwingTheme.makeLabel("Action:")); filterCard.add(actionFilter);
        filterCard.add(genBtn);
        filterCard.add(exportBtn);
        filterCard.add(countLabel);

        JPanel filterWrap = new JPanel(new BorderLayout());
        filterWrap.setOpaque(false);
        filterWrap.setBorder(new EmptyBorder(0, 28, 8, 28));
        filterWrap.add(filterCard);

        // ── Table ─────────────────────────────────────────────────────────────
        String[] cols = {"Timestamp", "Username", "Action Type", "Target ID", "Details"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(tableModel) {
            @Override public Component prepareRenderer(javax.swing.table.TableCellRenderer rend, int row, int col) {
                Component c = super.prepareRenderer(rend, row, col);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? SwingTheme.BG_PANEL : SwingTheme.BG_TABLE_ALT);
                    if (col == 2) {
                        String v = (String) getValueAt(row, col);
                        if (v == null) return c;
                        c.setForeground(v.contains("CREATE") ? SwingTheme.SUCCESS
                                : v.contains("DELETE") ? SwingTheme.ERROR : SwingTheme.STEEL);
                    } else c.setForeground(SwingTheme.FG_BODY);
                }
                return c;
            }
        };
        SwingTheme.styleTable(table);
        table.setAutoCreateRowSorter(true);
        table.getColumnModel().getColumn(0).setPreferredWidth(150);
        table.getColumnModel().getColumn(1).setPreferredWidth(130);
        table.getColumnModel().getColumn(2).setPreferredWidth(180);
        table.getColumnModel().getColumn(3).setPreferredWidth(140);
        table.getColumnModel().getColumn(4).setPreferredWidth(320);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(SwingTheme.BG_PANEL);

        JPanel card = SwingTheme.makeCard();
        card.setLayout(new BorderLayout());
        card.add(scroll, BorderLayout.CENTER);

        JPanel tableWrap = new JPanel(new BorderLayout());
        tableWrap.setOpaque(false);
        tableWrap.setBorder(new EmptyBorder(0, 28, 28, 28));
        tableWrap.add(card);

        JPanel north = new JPanel(new BorderLayout());
        north.setOpaque(false);
        north.add(header,     BorderLayout.NORTH);
        north.add(filterWrap, BorderLayout.CENTER);

        add(north,     BorderLayout.NORTH);
        add(tableWrap, BorderLayout.CENTER);
    }

    private void generateReport() {
        auditService.log(currentUser, AuditService.ACTION_GENERATE_REPORT,
                "Audit report generated by " + currentUser.getUsername());

        List<AuditLog> all = auditService.getAll();

        // Date filter
        try {
            LocalDateTime from = LocalDate.parse(fromDateField.getText().trim(), DATE_FMT).atStartOfDay();
            LocalDateTime to   = LocalDate.parse(toDateField.getText().trim(),   DATE_FMT).atTime(23, 59, 59);
            all = all.stream()
                    .filter(e -> !e.getTimestamp().isBefore(from) && !e.getTimestamp().isAfter(to))
                    .collect(Collectors.toList());
        } catch (Exception ignored) {}

        // Action filter
        String selectedAction = (String) actionFilter.getSelectedItem();
        if (selectedAction != null && !selectedAction.equals("All Actions")) {
            all = all.stream().filter(e -> e.getActionType().equals(selectedAction)).collect(Collectors.toList());
        }

        tableModel.setRowCount(0);
        List<AuditLog> reversed = all;
        for (int i = reversed.size() - 1; i >= 0; i--) {
            AuditLog e = reversed.get(i);
            tableModel.addRow(new Object[]{
                e.getTimestamp().format(FMT),
                e.getActingUsername(),
                e.getActionType(),
                e.getTargetId(),
                e.getDetails()
            });
        }
        countLabel.setText(tableModel.getRowCount() + " record(s)");
    }

    private void exportReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("DCBS AUDIT REPORT\n");
        sb.append("Generated by: ").append(currentUser.getUsername()).append("\n");
        sb.append("Generated at: ").append(LocalDateTime.now().format(FMT)).append("\n");
        sb.append("Period: ").append(fromDateField.getText()).append(" to ").append(toDateField.getText()).append("\n");
        sb.append("Action filter: ").append(actionFilter.getSelectedItem()).append("\n");
        sb.append("=".repeat(80)).append("\n\n");

        for (int row = 0; row < tableModel.getRowCount(); row++) {
            sb.append(String.format("%-24s | %-16s | %-22s | %-20s%n",
                    tableModel.getValueAt(row, 0),
                    tableModel.getValueAt(row, 1),
                    tableModel.getValueAt(row, 2),
                    tableModel.getValueAt(row, 3)));
            sb.append("   ").append(tableModel.getValueAt(row, 4)).append("\n");
            sb.append("-".repeat(80)).append("\n");
        }
        sb.append("\nTotal records: ").append(tableModel.getRowCount());

        JTextArea ta = new JTextArea(sb.toString());
        ta.setFont(SwingTheme.FONT_MONO);
        ta.setEditable(false);
        JScrollPane sp = new JScrollPane(ta);
        sp.setPreferredSize(new Dimension(760, 500));
        JOptionPane.showMessageDialog(this, sp, "Audit Report Export", JOptionPane.PLAIN_MESSAGE);
    }
}
