package com.hunts.bail.ui.swing;

import com.hunts.bail.domain.AuditLog;
import com.hunts.bail.repository.AuditLogRepository;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Displays the immutable audit trail in a scrollable, filterable table.
 */
public class AuditLogPanel extends JPanel {

    private final AuditLogRepository    repo;
    private DefaultTableModel           tableModel;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss");

    public AuditLogPanel(AuditLogRepository repo) {
        this.repo = repo;
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

        JPanel hl = new JPanel();
        hl.setOpaque(false);
        hl.setLayout(new BoxLayout(hl, BoxLayout.Y_AXIS));
        JLabel t = SwingTheme.makeTitle("Audit Log");
        JLabel s = SwingTheme.makeMuted("Complete, tamper-evident record of all system actions.");
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        s.setAlignmentX(Component.LEFT_ALIGNMENT);
        hl.add(t); hl.add(Box.createVerticalStrut(2)); hl.add(s);

        JButton refresh = SwingTheme.makePrimaryButton("↻ Refresh");
        refresh.addActionListener(e -> this.refresh());

        header.add(hl,      BorderLayout.WEST);
        header.add(refresh, BorderLayout.EAST);

        // ── Table ─────────────────────────────────────────────────────────────
        String[] cols = {"Timestamp", "User", "Action", "Target ID", "Details"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(tableModel) {
            @Override public Component prepareRenderer(javax.swing.table.TableCellRenderer rend, int row, int col) {
                Component c = super.prepareRenderer(rend, row, col);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? SwingTheme.BG_PANEL : SwingTheme.BG_TABLE_ALT);
                    if (col == 2) {
                        String val = (String) getValueAt(row, col);
                        c.setForeground(val != null && val.contains("CREATE") ? SwingTheme.SUCCESS
                                : val != null && val.contains("DELETE") ? SwingTheme.ERROR
                                : SwingTheme.STEEL);
                    } else c.setForeground(SwingTheme.FG_BODY);
                }
                return c;
            }
        };
        SwingTheme.styleTable(table);
        table.setAutoCreateRowSorter(true);
        table.getColumnModel().getColumn(0).setPreferredWidth(140);
        table.getColumnModel().getColumn(1).setPreferredWidth(120);
        table.getColumnModel().getColumn(2).setPreferredWidth(160);
        table.getColumnModel().getColumn(3).setPreferredWidth(140);
        table.getColumnModel().getColumn(4).setPreferredWidth(300);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(SwingTheme.BG_PANEL);

        JPanel card = SwingTheme.makeCard();
        card.setLayout(new BorderLayout());
        card.add(scroll, BorderLayout.CENTER);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(0, 28, 28, 28));
        wrap.add(card);

        add(header, BorderLayout.NORTH);
        add(wrap,   BorderLayout.CENTER);
    }

    public void refresh() {
        tableModel.setRowCount(0);
        List<AuditLog> logs = repo.findAll();
        // Show newest first
        for (int i = logs.size() - 1; i >= 0; i--) {
            AuditLog l = logs.get(i);
            tableModel.addRow(new Object[]{
                    l.getTimestamp().format(FMT),
                    l.getActingUsername(),
                    l.getActionType(),
                    l.getTargetId(),
                    l.getDetails()
            });
        }
    }
}
