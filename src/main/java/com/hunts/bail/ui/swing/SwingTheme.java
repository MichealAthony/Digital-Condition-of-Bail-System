package com.hunts.bail.ui.swing;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Centralised look-and-feel constants for the DCBS Swing GUI.
 * All colours, fonts and shared component factories live here,
 * keeping every panel visually consistent.
 */
public final class SwingTheme {

    private SwingTheme() {}

    // Palette 
    public static final Color NAVY        = new Color(0x1F3864);
    public static final Color STEEL       = new Color(0x2E5FA3);
    public static final Color ACCENT      = new Color(0xC9A84C);   // gold
    public static final Color BG_MAIN     = new Color(0xF4F6FB);
    public static final Color BG_PANEL    = Color.WHITE;
    public static final Color BG_TABLE_H  = new Color(0x1F3864);
    public static final Color BG_TABLE_ALT= new Color(0xEEF2FA);
    public static final Color FG_WHITE    = Color.WHITE;
    public static final Color FG_BODY     = new Color(0x1A1A2E);
    public static final Color FG_MUTED    = new Color(0x6B7280);
    public static final Color SUCCESS     = new Color(0x16A34A);
    public static final Color ERROR       = new Color(0xDC2626);
    public static final Color BORDER_CLR  = new Color(0xD1D5DB);

    // Fonts 
    public static final Font FONT_TITLE   = new Font("SansSerif", Font.BOLD,  22);
    public static final Font FONT_HEADING = new Font("SansSerif", Font.BOLD,  15);
    public static final Font FONT_LABEL   = new Font("SansSerif", Font.BOLD,  12);
    public static final Font FONT_BODY    = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FONT_SMALL   = new Font("SansSerif", Font.PLAIN, 11);
    public static final Font FONT_MONO    = new Font("Monospaced", Font.PLAIN, 12);

    // Borders 
    public static final Border BORDER_FIELD =
            BorderFactory.createCompoundBorder(
                    new LineBorder(BORDER_CLR, 1, true),
                    new EmptyBorder(6, 10, 6, 10));

    public static final Border BORDER_CARD =
            BorderFactory.createCompoundBorder(
                    new LineBorder(BORDER_CLR, 1, true),
                    new EmptyBorder(16, 16, 16, 16));

    // Component factories 

    public static JLabel makeTitle(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_TITLE);
        l.setForeground(NAVY);
        return l;
    }

    public static JLabel makeHeading(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_HEADING);
        l.setForeground(NAVY);
        return l;
    }

    public static JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_LABEL);
        l.setForeground(FG_BODY);
        return l;
    }

    public static JLabel makeMuted(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_SMALL);
        l.setForeground(FG_MUTED);
        return l;
    }

    public static JTextField makeField() {
        JTextField f = new JTextField();
        f.setFont(FONT_BODY);
        f.setBorder(BORDER_FIELD);
        f.setBackground(BG_PANEL);
        return f;
    }

    public static JPasswordField makePasswordField() {
        JPasswordField f = new JPasswordField();
        f.setFont(FONT_BODY);
        f.setBorder(BORDER_FIELD);
        f.setBackground(BG_PANEL);
        return f;
    }

    public static JTextArea makeTextArea(int rows, int cols) {
        JTextArea ta = new JTextArea(rows, cols);
        ta.setFont(FONT_BODY);
        ta.setBorder(BORDER_FIELD);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        return ta;
    }

    public static <T> JComboBox<T> makeCombo(T[] items) {
        JComboBox<T> cb = new JComboBox<>(items);
        cb.setFont(FONT_BODY);
        cb.setBackground(BG_PANEL);
        return cb;
    }

    /** Primary navy button */
    public static JButton makePrimaryButton(String text) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? STEEL : NAVY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(FONT_LABEL);
        b.setForeground(FG_WHITE);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(b.getPreferredSize().width + 20, 38));
        return b;
    }

    /** Secondary outlined button */
    public static JButton makeSecondaryButton(String text) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? new Color(0xEEF2FA) : BG_PANEL);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(STEEL);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(FONT_LABEL);
        b.setForeground(STEEL);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(b.getPreferredSize().width + 20, 38));
        return b;
    }

    /** Danger red button */
    public static JButton makeDangerButton(String text) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? new Color(0xB91C1C) : SwingTheme.ERROR);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(FONT_LABEL);
        b.setForeground(FG_WHITE);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(b.getPreferredSize().width + 20, 38));
        return b;
    }

    /** Styled card panel with white background and border */
    public static JPanel makeCard() {
        JPanel p = new JPanel();
        p.setBackground(BG_PANEL);
        p.setBorder(BORDER_CARD);
        return p;
    }

    /** Style a JTable to match the theme */
    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setRowHeight(34);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(0xDBEAFE));
        table.setSelectionForeground(FG_BODY);
        table.setBackground(BG_PANEL);
        table.getTableHeader().setFont(FONT_LABEL);
        table.getTableHeader().setBackground(BG_TABLE_H);
        table.getTableHeader().setForeground(FG_WHITE);
        table.getTableHeader().setBorder(BorderFactory.createEmptyBorder());
        table.getTableHeader().setReorderingAllowed(false);
    }
}
