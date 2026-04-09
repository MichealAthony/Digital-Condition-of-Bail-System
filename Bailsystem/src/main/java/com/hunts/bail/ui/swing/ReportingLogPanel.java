package com.hunts.bail.ui.swing;

import com.hunts.bail.controller.BailRecordController;
import com.hunts.bail.domain.BailCondition;
import com.hunts.bail.domain.BailRecord;
import com.hunts.bail.domain.ReportingEntry;
import com.hunts.bail.domain.User;
import com.hunts.bail.enums.ConditionType;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Displays and logs reporting visits for a specific bail record.
 *
 * Shows:
 *  - The reporting-schedule conditions on this record
 *  - A history table of all logged visits
 *  - A form to log a new visit (Present / Absent / Late)
 */
public class ReportingLogPanel extends JPanel {

    private final BailRecordController controller;
    private final User                 currentUser;
    private BailRecord                 record;
    private final Runnable             onBack;

    private DefaultTableModel historyModel;
    private JComboBox<ConditionItem>      conditionCombo;
    private JTextField                    scheduledDateField;
    private JComboBox<ReportingEntry.Outcome> outcomeCombo;
    private JTextArea                     notesArea;
    private JLabel                        statusLabel;

    private static final DateTimeFormatter DT_FMT   = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");
    private static final DateTimeFormatter DATE_FMT  = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public ReportingLogPanel(BailRecordController controller, User currentUser,
                              BailRecord record, Runnable onBack) {
        this.controller  = controller;
        this.currentUser = currentUser;
        this.record      = record;
        this.onBack      = onBack;
        setBackground(SwingTheme.BG_MAIN);
        setLayout(new BorderLayout());
        buildContent();
        refreshHistory();
    }

    private void buildContent() {
        // ── Header ────────────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(SwingTheme.BG_MAIN);
        header.setBorder(new EmptyBorder(20, 28, 10, 28));

        JPanel hl = new JPanel(); hl.setOpaque(false);
        hl.setLayout(new BoxLayout(hl, BoxLayout.Y_AXIS));
        JLabel t = SwingTheme.makeTitle("Reporting Log");
        JLabel s = SwingTheme.makeMuted("Bail ID: " + record.getBailId()
                + "  |  Accused: " + record.getAccusedPerson().getFullName()
                + "  |  Status: " + record.getStatus().getDisplayName());
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        s.setAlignmentX(Component.LEFT_ALIGNMENT);
        hl.add(t); hl.add(Box.createVerticalStrut(2)); hl.add(s);

        JButton backBtn = SwingTheme.makeSecondaryButton("← Back to Records");
        backBtn.addActionListener(e -> onBack.run());
        header.add(hl,      BorderLayout.WEST);
        header.add(backBtn, BorderLayout.EAST);

        // ── Body (left = log form, right = history table) ─────────────────────
        JPanel body = new JPanel(new BorderLayout(16, 0));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(0, 28, 28, 28));
        body.add(buildLogForm(),    BorderLayout.WEST);
        body.add(buildHistoryPanel(), BorderLayout.CENTER);

        statusLabel = new JLabel(" ");
        statusLabel.setFont(SwingTheme.FONT_SMALL);
        statusLabel.setBorder(new EmptyBorder(4, 28, 8, 28));

        add(header,      BorderLayout.NORTH);
        add(body,        BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
    }

    // ── Log new visit form ────────────────────────────────────────────────────
    private JPanel buildLogForm() {
        JPanel card = SwingTheme.makeCard();
        card.setPreferredSize(new Dimension(340, 0));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel heading = SwingTheme.makeHeading("Log a Visit");
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(heading);
        card.add(Box.createVerticalStrut(14));

        // Condition selector — only REPORTING_SCHEDULE conditions
        List<BailCondition> reportingConditions = record.getConditions().stream()
                .filter(c -> c.getConditionType() == ConditionType.REPORTING_SCHEDULE)
                .collect(Collectors.toList());

        JLabel condLabel = SwingTheme.makeLabel("Reporting Condition *");
        condLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(condLabel);
        card.add(Box.createVerticalStrut(4));

        if (reportingConditions.isEmpty()) {
            JLabel none = SwingTheme.makeMuted("No reporting conditions on this record.");
            none.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(none);
            return card;
        }

        ConditionItem[] items = reportingConditions.stream()
                .map(ConditionItem::new).toArray(ConditionItem[]::new);
        conditionCombo = SwingTheme.makeCombo(items);
        conditionCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        conditionCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        card.add(conditionCombo);
        card.add(Box.createVerticalStrut(10));

        // Scheduled date
        JLabel dateLabel = SwingTheme.makeLabel("Scheduled Report Date * (YYYY-MM-DD)");
        dateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(dateLabel);
        card.add(Box.createVerticalStrut(4));
        scheduledDateField = SwingTheme.makeField();
        scheduledDateField.setText(LocalDate.now().format(DATE_FMT));
        scheduledDateField.setAlignmentX(Component.LEFT_ALIGNMENT);
        scheduledDateField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        card.add(scheduledDateField);
        card.add(Box.createVerticalStrut(10));

        // Outcome
        JLabel outcomeLabel = SwingTheme.makeLabel("Outcome *");
        outcomeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(outcomeLabel);
        card.add(Box.createVerticalStrut(4));
        outcomeCombo = SwingTheme.makeCombo(ReportingEntry.Outcome.values());
        outcomeCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        outcomeCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        card.add(outcomeCombo);
        card.add(Box.createVerticalStrut(10));

        // Notes
        JLabel notesLabel = SwingTheme.makeLabel("Notes / Officer Remarks");
        notesLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(notesLabel);
        card.add(Box.createVerticalStrut(4));
        notesArea = SwingTheme.makeTextArea(4, 28);
        notesArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        JScrollPane noteScroll = new JScrollPane(notesArea);
        noteScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        noteScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        card.add(noteScroll);
        card.add(Box.createVerticalStrut(14));

        JButton submitBtn = SwingTheme.makePrimaryButton("Log Visit");
        submitBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        submitBtn.addActionListener(e -> submitReport());
        card.add(submitBtn);

        return card;
    }

    // ── Visit history table ───────────────────────────────────────────────────
    private JPanel buildHistoryPanel() {
        JPanel card = SwingTheme.makeCard();
        card.setLayout(new BorderLayout(0, 8));

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.add(SwingTheme.makeHeading("Visit History"), BorderLayout.WEST);
        JButton refreshBtn = SwingTheme.makeSecondaryButton("↻");
        refreshBtn.setToolTipText("Refresh");
        refreshBtn.addActionListener(e -> refreshHistory());
        topRow.add(refreshBtn, BorderLayout.EAST);

        String[] cols = {"Scheduled Date", "Logged At", "Outcome", "Condition", "Logged By", "Notes"};
        historyModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(historyModel) {
            @Override public Component prepareRenderer(javax.swing.table.TableCellRenderer rend, int row, int col) {
                Component c = super.prepareRenderer(rend, row, col);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? SwingTheme.BG_PANEL : SwingTheme.BG_TABLE_ALT);
                    if (col == 2) {
                        String v = (String) getValueAt(row, col);
                        c.setForeground("PRESENT".equals(v) ? SwingTheme.SUCCESS
                                : "ABSENT".equals(v)  ? SwingTheme.ERROR
                                : new Color(0xD97706));
                    } else c.setForeground(SwingTheme.FG_BODY);
                }
                return c;
            }
        };
        SwingTheme.styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(120);
        table.getColumnModel().getColumn(1).setPreferredWidth(130);
        table.getColumnModel().getColumn(2).setPreferredWidth(80);
        table.getColumnModel().getColumn(3).setPreferredWidth(200);
        table.getColumnModel().getColumn(4).setPreferredWidth(110);
        table.getColumnModel().getColumn(5).setPreferredWidth(200);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(SwingTheme.BG_PANEL);

        // Compliance summary bar
        JPanel summaryBar = buildSummaryBar();

        card.add(topRow,     BorderLayout.NORTH);
        card.add(scroll,     BorderLayout.CENTER);
        card.add(summaryBar, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildSummaryBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 4));
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, SwingTheme.BORDER_CLR));

        List<ReportingEntry> log = record.getReportingLog();
        long present = log.stream().filter(e -> e.getOutcome() == ReportingEntry.Outcome.PRESENT).count();
        long absent  = log.stream().filter(e -> e.getOutcome() == ReportingEntry.Outcome.ABSENT).count();
        long late    = log.stream().filter(e -> e.getOutcome() == ReportingEntry.Outcome.LATE).count();

        bar.add(summaryChip("✅ Present: " + present,  SwingTheme.SUCCESS));
        bar.add(summaryChip("❌ Absent: "  + absent,   SwingTheme.ERROR));
        bar.add(summaryChip("⏱ Late: "    + late,      new Color(0xD97706)));
        bar.add(summaryChip("Total: "      + log.size(), SwingTheme.FG_MUTED));
        return bar;
    }

    private JLabel summaryChip(String text, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(SwingTheme.FONT_LABEL);
        l.setForeground(color);
        return l;
    }

    // ── Data ──────────────────────────────────────────────────────────────────

    private void refreshHistory() {
        // Re-fetch live record from controller
        controller.handleFindById(record.getBailId()).ifPresent(r -> this.record = r);

        if (historyModel == null) return;
        historyModel.setRowCount(0);
        List<ReportingEntry> log = record.getReportingLog();
        // Newest first
        for (int i = log.size() - 1; i >= 0; i--) {
            ReportingEntry e = log.get(i);
            // Find matching condition description
            String condDesc = record.getConditions().stream()
                    .filter(c -> c.getConditionId().equals(e.getConditionId()))
                    .map(BailCondition::getDescription)
                    .findFirst().orElse(e.getConditionId());
            historyModel.addRow(new Object[]{
                e.getScheduledDate().toLocalDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                e.getLoggedAt().format(DT_FMT),
                e.getOutcome().name(),
                condDesc,
                e.getLoggedByUsername(),
                e.getNotes()
            });
        }
    }

    private void submitReport() {
        if (conditionCombo == null) return;

        ConditionItem selected = (ConditionItem) conditionCombo.getSelectedItem();
        if (selected == null) return;

        LocalDateTime scheduledDate;
        try {
            scheduledDate = LocalDate.parse(scheduledDateField.getText().trim(), DATE_FMT).atTime(9, 0);
        } catch (DateTimeParseException ex) {
            flashStatus("Scheduled date must be in format YYYY-MM-DD.", SwingTheme.ERROR);
            return;
        }

        ReportingEntry.Outcome outcome = (ReportingEntry.Outcome) outcomeCombo.getSelectedItem();
        String notes = notesArea.getText().trim();

        ReportingEntry entry = controller.handleLogReport(currentUser,
                record.getBailId(), selected.condition.getConditionId(),
                scheduledDate, outcome, notes);

        if (entry != null) {
            flashStatus("✔  Visit logged: " + outcome + " on " + scheduledDate.toLocalDate(), SwingTheme.SUCCESS);
            notesArea.setText("");
            refreshHistory();
        } else {
            flashStatus("✘  Failed to log visit.", SwingTheme.ERROR);
        }
    }

    private void flashStatus(String msg, Color color) {
        statusLabel.setText(msg); statusLabel.setForeground(color);
    }

    /** Wrapper so ConditionType shows nicely in the combo box */
    private static class ConditionItem {
        final BailCondition condition;
        ConditionItem(BailCondition c) { this.condition = c; }
        @Override public String toString() {
            String desc = condition.getDescription();
            return "[" + condition.getConditionType().getDisplayName() + "] "
                    + (desc.length() > 50 ? desc.substring(0, 47) + "…" : desc);
        }
    }
}
