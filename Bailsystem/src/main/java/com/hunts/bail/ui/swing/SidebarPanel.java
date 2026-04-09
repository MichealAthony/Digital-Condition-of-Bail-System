package com.hunts.bail.ui.swing;

import com.hunts.bail.domain.User;
import com.hunts.bail.enums.Role;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SidebarPanel extends JPanel {

    public static final String PAGE_DASHBOARD    = "Dashboard";
    public static final String PAGE_CREATE_BAIL  = "Create Bail Record";
    public static final String PAGE_VIEW_RECORDS = "View Records";
    public static final String PAGE_AUDIT_LOG    = "Audit Log";
    public static final String PAGE_AUDIT_REPORT = "Audit Report";
    public static final String PAGE_MANAGE_USERS = "Manage Users";

    private final Consumer<String> onNavigate;
    private final List<NavItem>    items = new ArrayList<>();

    public SidebarPanel(User currentUser, Consumer<String> onNavigate) {
        this.onNavigate = onNavigate;
        setPreferredSize(new Dimension(220, Integer.MAX_VALUE));
        setBackground(SwingTheme.NAVY);
        setLayout(new BorderLayout());
        buildContent(currentUser);
    }

    private void buildContent(User currentUser) {
        JPanel logo = new JPanel();
        logo.setBackground(new Color(0x162B4D));
        logo.setLayout(new BoxLayout(logo, BoxLayout.Y_AXIS));
        logo.setBorder(new EmptyBorder(20, 16, 20, 16));
        JLabel icon = new JLabel("⚖"); icon.setFont(new Font("SansSerif", Font.PLAIN, 30)); icon.setForeground(SwingTheme.ACCENT); icon.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel name = new JLabel("DCBS"); name.setFont(new Font("SansSerif", Font.BOLD, 16)); name.setForeground(SwingTheme.FG_WHITE); name.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel sub  = new JLabel("Bail Management"); sub.setFont(SwingTheme.FONT_SMALL); sub.setForeground(new Color(0x8AAACF)); sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        logo.add(icon); logo.add(Box.createVerticalStrut(6)); logo.add(name); logo.add(sub);

        JPanel nav = new JPanel();
        nav.setBackground(SwingTheme.NAVY);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBorder(new EmptyBorder(12, 0, 0, 0));

        addNavItem(nav, "🏠", PAGE_DASHBOARD,    true);
        // Create only for roles that can create
        if (isAnyRole(currentUser, Role.POLICE_OFFICER, Role.SUPERVISOR, Role.MANAGER, Role.ADMINISTRATOR))
            addNavItem(nav, "➕", PAGE_CREATE_BAIL, false);
        addNavItem(nav, "📋", PAGE_VIEW_RECORDS, false);

        // Divider label
        JLabel auditSection = new JLabel("  REPORTS");
        auditSection.setFont(new Font("SansSerif", Font.BOLD, 9));
        auditSection.setForeground(new Color(0x5577AA));
        auditSection.setBorder(new EmptyBorder(14, 16, 4, 0));
        nav.add(auditSection);

        addNavItem(nav, "📜", PAGE_AUDIT_LOG,    false);
        if (isAnyRole(currentUser, Role.SUPERVISOR, Role.MANAGER, Role.ADMINISTRATOR))
            addNavItem(nav, "📊", PAGE_AUDIT_REPORT, false);

        // Admin section
        if (currentUser.getRole() == Role.ADMINISTRATOR) {
            JLabel adminSection = new JLabel("  ADMINISTRATION");
            adminSection.setFont(new Font("SansSerif", Font.BOLD, 9));
            adminSection.setForeground(new Color(0x5577AA));
            adminSection.setBorder(new EmptyBorder(14, 16, 4, 0));
            nav.add(adminSection);
            addNavItem(nav, "👥", PAGE_MANAGE_USERS, false);
        }

        JPanel userPanel = new JPanel();
        userPanel.setBackground(new Color(0x162B4D));
        userPanel.setBorder(new EmptyBorder(12, 16, 16, 16));
        userPanel.setLayout(new BoxLayout(userPanel, BoxLayout.Y_AXIS));
        JLabel roleTag   = new JLabel(currentUser.getRole().getDisplayName().toUpperCase()); roleTag.setFont(new Font("SansSerif", Font.BOLD, 9)); roleTag.setForeground(SwingTheme.ACCENT); roleTag.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel userName  = new JLabel(currentUser.getFullName()); userName.setFont(SwingTheme.FONT_LABEL); userName.setForeground(SwingTheme.FG_WHITE); userName.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel handle    = new JLabel("@" + currentUser.getUsername()); handle.setFont(SwingTheme.FONT_SMALL); handle.setForeground(new Color(0x8AAACF)); handle.setAlignmentX(Component.LEFT_ALIGNMENT);
        userPanel.add(roleTag); userPanel.add(Box.createVerticalStrut(2)); userPanel.add(userName); userPanel.add(handle);

        add(logo,      BorderLayout.NORTH);
        add(nav,       BorderLayout.CENTER);
        add(userPanel, BorderLayout.SOUTH);
    }

    private void addNavItem(JPanel parent, String emoji, String label, boolean active) {
        NavItem item = new NavItem(emoji, label, () -> { setActive(label); onNavigate.accept(label); });
        items.add(item);
        parent.add(item);
        if (active) item.setActive(true);
    }

    public void setActive(String pageLabel) {
        items.forEach(i -> i.setActive(i.label.equals(pageLabel)));
        repaint();
    }

    private boolean isAnyRole(User user, Role... roles) {
        for (Role r : roles) if (user.getRole() == r) return true;
        return false;
    }

    static class NavItem extends JPanel {
        final String label;
        boolean isActive, hover;
        NavItem(String emoji, String label, Runnable onClick) {
            this.label = label;
            setOpaque(false);
            setLayout(new FlowLayout(FlowLayout.LEFT, 14, 10));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
            JLabel ic = new JLabel(emoji); ic.setFont(new Font("SansSerif", Font.PLAIN, 16)); ic.setForeground(Color.WHITE);
            JLabel tx = new JLabel(label); tx.setFont(SwingTheme.FONT_BODY); tx.setForeground(new Color(0xCCDDEE));
            add(ic); add(tx);
            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e)  { onClick.run(); }
                @Override public void mouseEntered(MouseEvent e)  { hover = true;  repaint(); }
                @Override public void mouseExited(MouseEvent e)   { hover = false; repaint(); }
            });
        }
        void setActive(boolean a) { isActive = a; repaint(); }
        @Override protected void paintComponent(Graphics g) {
            if (isActive) { g.setColor(SwingTheme.STEEL); g.fillRect(0,0,getWidth(),getHeight()); g.setColor(SwingTheme.ACCENT); g.fillRect(0,0,4,getHeight()); }
            else if (hover) { g.setColor(new Color(0x2A4A78)); g.fillRect(0,0,getWidth(),getHeight()); }
            super.paintComponent(g);
        }
    }
}
