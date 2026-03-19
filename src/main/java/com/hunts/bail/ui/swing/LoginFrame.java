package com.hunts.bail.ui.swing;

import com.hunts.bail.domain.User;
import com.hunts.bail.exception.AuthenticationException;
import com.hunts.bail.service.AuthenticationService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * Login screen — the first window the user sees.
 * On successful authentication it hands the User object to MainFrame.
 */
public class LoginFrame extends JFrame {

    private final AuthenticationService authService;
    private final Runnable              onLoginSuccess;   // callback → MainFrame

    private JTextField     usernameField;
    private JPasswordField passwordField;
    private JLabel         errorLabel;
    private JButton        loginButton;

    // Injected by BailSwingApp so MainFrame can be shown after login
    private java.util.function.Consumer<User> launchMain;

    public LoginFrame(AuthenticationService authService,
                      java.util.function.Consumer<User> launchMain) {
        this.authService  = authService;
        this.launchMain   = launchMain;
        this.onLoginSuccess = null;
        initUI();
    }

    private void initUI() {
        setTitle("DCBS – Digitalized Condition of Bail System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setSize(440, 560);
        setLocationRelativeTo(null);

        // Root panel with BorderLayout
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(SwingTheme.BG_MAIN);
        setContentPane(root);

        // Header banner 
        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(SwingTheme.NAVY);
        banner.setBorder(new EmptyBorder(28, 28, 28, 28));

        JLabel crest = new JLabel("⚖", SwingConstants.CENTER);
        crest.setFont(new Font("SansSerif", Font.PLAIN, 48));
        crest.setForeground(SwingTheme.ACCENT);

        JLabel sysTitle = new JLabel("Bail Management System", SwingConstants.CENTER);
        sysTitle.setFont(SwingTheme.FONT_TITLE);
        sysTitle.setForeground(SwingTheme.FG_WHITE);

        JLabel subtitle = new JLabel("Hunts Bay Police Station", SwingConstants.CENTER);
        subtitle.setFont(SwingTheme.FONT_SMALL);
        subtitle.setForeground(new Color(0xAACCEE));

        JPanel bannerText = new JPanel();
        bannerText.setLayout(new BoxLayout(bannerText, BoxLayout.Y_AXIS));
        bannerText.setOpaque(false);
        crest.setAlignmentX(Component.CENTER_ALIGNMENT);
        sysTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        bannerText.add(crest);
        bannerText.add(Box.createVerticalStrut(6));
        bannerText.add(sysTitle);
        bannerText.add(Box.createVerticalStrut(4));
        bannerText.add(subtitle);
        banner.add(bannerText, BorderLayout.CENTER);

        // Login card 
        JPanel card = SwingTheme.makeCard();
        card.setLayout(new GridBagLayout());
        card.setBorder(new EmptyBorder(28, 32, 28, 32));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill      = GridBagConstraints.HORIZONTAL;
        gc.insets    = new Insets(6, 0, 6, 0);
        gc.gridwidth = 1;
        gc.weightx   = 1.0;

        JLabel loginHeading = SwingTheme.makeTitle("Officer Login");
        loginHeading.setHorizontalAlignment(SwingConstants.CENTER);
        gc.gridx = 0; gc.gridy = 0;
        card.add(loginHeading, gc);

        gc.gridy = 1;
        card.add(Box.createVerticalStrut(4), gc);

        gc.gridy = 2;
        card.add(SwingTheme.makeLabel("Username"), gc);

        usernameField = SwingTheme.makeField();
        usernameField.setPreferredSize(new Dimension(340, 38));
        gc.gridy = 3;
        card.add(usernameField, gc);

        gc.gridy = 4;
        card.add(SwingTheme.makeLabel("Password"), gc);

        passwordField = SwingTheme.makePasswordField();
        passwordField.setPreferredSize(new Dimension(340, 38));
        gc.gridy = 5;
        card.add(passwordField, gc);

        errorLabel = new JLabel(" ");
        errorLabel.setFont(SwingTheme.FONT_SMALL);
        errorLabel.setForeground(SwingTheme.ERROR);
        errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gc.gridy = 6;
        card.add(errorLabel, gc);

        loginButton = SwingTheme.makePrimaryButton("Sign In");
        loginButton.setPreferredSize(new Dimension(340, 42));
        gc.gridy = 7;
        gc.insets = new Insets(8, 0, 0, 0);
        card.add(loginButton, gc);

        // Demo credentials hint
        JPanel hint = new JPanel();
        hint.setBackground(new Color(0xEEF2FA));
        hint.setBorder(new EmptyBorder(10, 12, 10, 12));
        hint.setLayout(new BoxLayout(hint, BoxLayout.Y_AXIS));
        String[] creds = {
                "officer.brown  /  Officer#2026  (Police Officer)",
                "sup.hamilton   /  Super#2026    (Supervisor)",
                "admin.james    /  Admin#2026    (Administrator)"
        };
        JLabel hintTitle = SwingTheme.makeMuted("Demo Credentials:");
        hintTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        hint.add(hintTitle);
        hint.add(Box.createVerticalStrut(4));
        for (String c : creds) {
            JLabel cl = SwingTheme.makeMuted("  " + c);
            cl.setFont(new Font("Monospaced", Font.PLAIN, 11));
            cl.setAlignmentX(Component.LEFT_ALIGNMENT);
            hint.add(cl);
        }

        // Footer 
        JLabel footer = SwingTheme.makeMuted("COMP2171 · Group Wed 6–8pm · © 2026");
        footer.setHorizontalAlignment(SwingConstants.CENTER);
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(SwingTheme.BG_MAIN);
        footerPanel.setBorder(new EmptyBorder(0, 0, 10, 0));
        footerPanel.add(footer, BorderLayout.CENTER);

        // Assemble login card and hint into centre panel
        JPanel centre = new JPanel();
        centre.setBackground(SwingTheme.BG_MAIN);
        centre.setLayout(new BoxLayout(centre, BoxLayout.Y_AXIS));
        centre.setBorder(new EmptyBorder(16, 24, 8, 24));
        centre.add(card);
        centre.add(Box.createVerticalStrut(10));
        centre.add(hint);

        root.add(banner, BorderLayout.NORTH);
        root.add(centre, BorderLayout.CENTER);
        root.add(footerPanel, BorderLayout.SOUTH);

        // Actions 
        loginButton.addActionListener(e -> attemptLogin());
        passwordField.addActionListener(e -> attemptLogin());
        usernameField.addActionListener(e -> passwordField.requestFocus());
    }

    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password.");
            return;
        }

        loginButton.setEnabled(false);
        loginButton.setText("Signing in…");

        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override protected User doInBackground() {
                return authService.authenticate(username, password);
            }
            @Override protected void done() {
                try {
                    User user = get();
                    dispose();
                    launchMain.accept(user);
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    showError(cause instanceof AuthenticationException
                            ? cause.getMessage()
                            : "Unexpected error. Please try again.");
                    loginButton.setEnabled(true);
                    loginButton.setText("Sign In");
                    passwordField.setText("");
                }
            }
        };
        worker.execute();
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
    }
}
