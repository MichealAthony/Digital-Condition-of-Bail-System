package com.hunts.bail.ui.swing;

import com.hunts.bail.domain.BailRecord;
import com.hunts.bail.domain.User;
import com.hunts.bail.enums.BailStatus;
import com.hunts.bail.service.BailRecordService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Dashboard panel — shown on login. Displays key stats and recent records.
 */
public class DashboardPanel extends JPanel {

    private final BailRecordService service;
    private final User              currentUser;
    private final Runnable          onCreateNew;

    public DashboardPanel(BailRecordService service, User currentUser, Runnable onCreateNew) {
        this.service     = service;
        this.currentUser = currentUser;
        this.onCreateNew = onCreateNew;
        setBackground(SwingTheme.BG_MAIN);
        setLayout(new BorderLayout(0, 0));
        buildContent();
    }

    private void buildContent() {
        List<BailRecord> all = service.getAllRecords();

        // ── Page header ───────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(SwingTheme.BG_MAIN);
        header.setBorder(new EmptyBorder(24, 28, 12, 28));

        JPanel headLeft = new JPanel();
        headLeft.setOpaque(false);
        headLeft.setLayout(new BoxLayout(headLeft, BoxLayout.Y_AXIS));

        JLabel welcome = SwingTheme.makeTitle("Welcome, " + currentUser.getFullName().split(" ")[0]);
        JLabel sub = SwingTheme.makeMuted("Here's an overview of the bail management system.");
        welcome.setAlignmentX(Component.LEFT_ALIGNMENT);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        headLeft.add(welcome);
        headLeft.add(Box.createVerticalStrut(2));
        headLeft.add(sub);

        JButton newBtn = SwingTheme.makePrimaryButton("+ New Bail Record");
        newBtn.addActionListener(e -> onCreateNew.run());

        header.add(headLeft, BorderLayout.WEST);
        header.add(newBtn,   BorderLayout.EAST);

        // ── Stats cards ───────────────────────────────────────────────────────
        long active    = all.stream().filter(r -> r.getStatus() == BailStatus.ACTIVE).count();
        long revoked   = all.stream().filter(r -> r.getStatus() == BailStatus.REVOKED).count();
        long expired   = all.stream().filter(r -> r.getStatus() == BailStatus.EXPIRED).count();

        JPanel stats = new JPanel(new GridLayout(1, 4, 16, 0));
        stats.setOpaque(false);
        stats.setBorder(new EmptyBorder(0, 28, 16, 28));
        stats.add(statCard("Total Records",  String.valueOf(all.size()),  "📋", SwingTheme.STEEL));
        stats.add(statCard("Active",         String.valueOf(active),      "✅", new Color(0x16A34A)));
        stats.add(statCard("Revoked",        String.valueOf(revoked),     "🚫", new Color(0xDC2626)));
        stats.add(statCard("Expired",        String.valueOf(expired),     "⏰", new Color(0xD97706)));

        // ── Recent records table ──────────────────────────────────────────────
        String[] cols = {"Bail ID", "Accused Name", "National ID", "Status", "Created"};
        Object[][] rows;
        List<BailRecord> recent = all.size() > 10 ? all.subList(all.size()-10, all.size()) : all;
        rows = new Object[recent.size()][5];
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");
        for (int i = 0; i < recent.size(); i++) {
            BailRecord r = recent.get(i);
            rows[i][0] = r.getBailId();
            rows[i][1] = r.getAccusedPerson().getFullName();
            rows[i][2] = r.getAccusedPerson().getNationalId();
            rows[i][3] = r.getStatus().getDisplayName();
            rows[i][4] = r.getCreatedAt().format(fmt);
        }

        JTable table = new JTable(rows, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Component prepareRenderer(javax.swing.table.TableCellRenderer rend, int row, int col) {
                Component c = super.prepareRenderer(rend, row, col);
                if (!isRowSelected(row)) c.setBackground(row % 2 == 0 ? SwingTheme.BG_PANEL : SwingTheme.BG_TABLE_ALT);
                return c;
            }
        };
        SwingTheme.styleTable(table);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(SwingTheme.BG_PANEL);

        JPanel tableCard = SwingTheme.makeCard();
        tableCard.setLayout(new BorderLayout(0, 8));
        JLabel tableTitle = SwingTheme.makeHeading("Recent Bail Records");
        tableCard.add(tableTitle, BorderLayout.NORTH);
        tableCard.add(scroll,     BorderLayout.CENTER);

        JPanel tableWrap = new JPanel(new BorderLayout());
        tableWrap.setOpaque(false);
        tableWrap.setBorder(new EmptyBorder(0, 28, 28, 28));
        tableWrap.add(tableCard);

        // ── Assemble ──────────────────────────────────────────────────────────
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(header, BorderLayout.NORTH);
        top.add(stats,  BorderLayout.CENTER);

        add(top,       BorderLayout.NORTH);
        add(tableWrap, BorderLayout.CENTER);
    }

    private JPanel statCard(String title, String value, String emoji, Color accent) {
        JPanel card = new JPanel();
        card.setBackground(SwingTheme.BG_PANEL);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SwingTheme.BORDER_CLR, 1, true),
                new EmptyBorder(16, 16, 16, 16)));
        card.setLayout(new BorderLayout(0, 6));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel t = SwingTheme.makeMuted(title);
        JLabel ic = new JLabel(emoji);
        ic.setFont(new Font("SansSerif", Font.PLAIN, 22));
        top.add(t,  BorderLayout.WEST);
        top.add(ic, BorderLayout.EAST);

        JLabel val = new JLabel(value);
        val.setFont(new Font("SansSerif", Font.BOLD, 32));
        val.setForeground(accent);

        card.add(top, BorderLayout.NORTH);
        card.add(val, BorderLayout.CENTER);
        return card;
    }
}
