package com.hunts.bail.ui.swing;

import com.hunts.bail.domain.User;
import com.hunts.bail.enums.Role;
import com.hunts.bail.service.UserManagementService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * UC4 – Manage Users / UC8 – Create User.
 * Only visible to Administrators.
 */
public class ManageUsersPanel extends JPanel {

    private final UserManagementService userService;
    private final User                  currentUser;
    private DefaultTableModel           tableModel;
    private List<User>                  userList;
    private JTable                      table;

    public ManageUsersPanel(UserManagementService userService, User currentUser) {
        this.userService  = userService;
        this.currentUser  = currentUser;
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
        JLabel t = SwingTheme.makeTitle("User Management");
        JLabel s = SwingTheme.makeMuted("Create, update and remove system user accounts. Administrator access only.");
        t.setAlignmentX(Component.LEFT_ALIGNMENT); s.setAlignmentX(Component.LEFT_ALIGNMENT);
        hl.add(t); hl.add(Box.createVerticalStrut(2)); hl.add(s);

        JButton createBtn  = SwingTheme.makePrimaryButton("+ Create User");
        JButton refreshBtn = SwingTheme.makeSecondaryButton("↻ Refresh");
        createBtn.addActionListener(e  -> showCreateDialog());
        refreshBtn.addActionListener(e -> refresh());

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setOpaque(false);
        btns.add(refreshBtn); btns.add(createBtn);
        header.add(hl, BorderLayout.WEST);
        header.add(btns, BorderLayout.EAST);

        // ── Table ─────────────────────────────────────────────────────────────
        String[] cols = {"User ID", "Username", "Full Name", "Role", "Actions"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel) {
            @Override public Component prepareRenderer(javax.swing.table.TableCellRenderer rend, int row, int col) {
                Component c = super.prepareRenderer(rend, row, col);
                if (!isRowSelected(row)) c.setBackground(row % 2 == 0 ? SwingTheme.BG_PANEL : SwingTheme.BG_TABLE_ALT);
                return c;
            }
        };
        SwingTheme.styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(280);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(200);
        table.getColumnModel().getColumn(3).setPreferredWidth(140);
        table.getColumnModel().getColumn(4).setPreferredWidth(80);

        // Double-click → action menu
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    int modelRow = table.convertRowIndexToModel(table.getSelectedRow());
                    showUserActions(userList.get(modelRow));
                }
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(SwingTheme.BG_PANEL);

        JPanel card = SwingTheme.makeCard();
        card.setLayout(new BorderLayout(0, 8));
        JLabel hint = SwingTheme.makeMuted("Double-click a row to edit, reset password or delete.");
        card.add(hint,   BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(0, 28, 28, 28));
        wrap.add(card);

        add(header, BorderLayout.NORTH);
        add(wrap,   BorderLayout.CENTER);
    }

    public void refresh() {
        try {
            userList = userService.getAllUsers(currentUser);
            tableModel.setRowCount(0);
            for (User u : userList) {
                tableModel.addRow(new Object[]{
                    u.getUserId(), u.getUsername(), u.getFullName(),
                    u.getRole().getDisplayName(), "⋯"
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Create User dialog ────────────────────────────────────────────────────
    private void showCreateDialog() {
        JTextField usernameField  = SwingTheme.makeField();
        JTextField fullNameField  = SwingTheme.makeField();
        JPasswordField passField  = SwingTheme.makePasswordField();
        JComboBox<Role> roleCombo = SwingTheme.makeCombo(Role.values());

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(new EmptyBorder(8, 8, 8, 8));
        form.add(SwingTheme.makeLabel("Username:"));        form.add(usernameField);
        form.add(SwingTheme.makeLabel("Full Name:"));       form.add(fullNameField);
        form.add(SwingTheme.makeLabel("Temporary Password:")); form.add(passField);
        form.add(SwingTheme.makeLabel("Role:"));            form.add(roleCombo);

        int res = JOptionPane.showConfirmDialog(this, form, "Create New User",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;

        try {
            userService.createUser(currentUser,
                    usernameField.getText().trim(),
                    new String(passField.getPassword()),
                    (Role) roleCombo.getSelectedItem(),
                    fullNameField.getText().trim());
            JOptionPane.showMessageDialog(this, "User created successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            refresh();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Per-user action menu ──────────────────────────────────────────────────
    private void showUserActions(User target) {
        String[] options = {"Edit Role / Name", "Reset Password", "Delete User", "Cancel"};
        int choice = JOptionPane.showOptionDialog(this,
                "Choose an action for user: " + target.getUsername(),
                "User Actions", JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        switch (choice) {
            case 0 -> showEditDialog(target);
            case 1 -> showResetPasswordDialog(target);
            case 2 -> confirmDelete(target);
        }
    }

    private void showEditDialog(User target) {
        JTextField fullNameField  = SwingTheme.makeField();
        fullNameField.setText(target.getFullName());
        JComboBox<Role> roleCombo = SwingTheme.makeCombo(Role.values());
        roleCombo.setSelectedItem(target.getRole());

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(new EmptyBorder(8, 8, 8, 8));
        form.add(SwingTheme.makeLabel("Full Name:")); form.add(fullNameField);
        form.add(SwingTheme.makeLabel("Role:"));      form.add(roleCombo);

        int res = JOptionPane.showConfirmDialog(this, form, "Edit User: " + target.getUsername(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;

        try {
            userService.updateUser(currentUser, target.getUserId(),
                    (Role) roleCombo.getSelectedItem(), fullNameField.getText().trim());
            JOptionPane.showMessageDialog(this, "User updated.", "Success", JOptionPane.INFORMATION_MESSAGE);
            refresh();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showResetPasswordDialog(User target) {
        JPasswordField newPass = SwingTheme.makePasswordField();
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(new EmptyBorder(8, 8, 8, 8));
        form.add(SwingTheme.makeLabel("New Password:")); form.add(newPass);

        int res = JOptionPane.showConfirmDialog(this, form, "Reset Password: " + target.getUsername(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;

        try {
            userService.resetPassword(currentUser, target.getUserId(), new String(newPass.getPassword()));
            JOptionPane.showMessageDialog(this, "Password reset successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void confirmDelete(User target) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Permanently delete user '" + target.getUsername() + "'? This cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            userService.deleteUser(currentUser, target.getUserId());
            JOptionPane.showMessageDialog(this, "User deleted.", "Success", JOptionPane.INFORMATION_MESSAGE);
            refresh();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
