package com.hunts.bail.ui.swing;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Base64;

/**
 * Reusable photo-upload widget.
 * Shows a thumbnail preview and stores the image as a base64 string.
 * Used in both CreateBailRecordPanel and UpdateBailRecordPanel.
 */
public class PhotoUploadHelper extends JPanel {

    private String base64Data = "";          // empty = no photo
    private final JLabel    preview;
    private final JLabel    filenameLabel;
    private static final int THUMB_W = 110;
    private static final int THUMB_H = 140;

    public PhotoUploadHelper() {
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // Placeholder preview box
        preview = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xEEF2FA));
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                g2.setColor(SwingTheme.BORDER_CLR);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                super.paintComponent(g);
            }
        };
        preview.setPreferredSize(new Dimension(THUMB_W, THUMB_H));
        preview.setMinimumSize(new Dimension(THUMB_W, THUMB_H));
        preview.setMaximumSize(new Dimension(THUMB_W, THUMB_H));
        preview.setHorizontalAlignment(SwingConstants.CENTER);
        preview.setVerticalAlignment(SwingConstants.CENTER);
        preview.setText("<html><center><font color='#9CA3AF'>No<br>Photo</font></center></html>");
        preview.setFont(SwingTheme.FONT_SMALL);
        preview.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(preview);
        add(Box.createVerticalStrut(6));

        JButton uploadBtn = SwingTheme.makeSecondaryButton("📷 Upload Photo");
        uploadBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        uploadBtn.addActionListener(e -> choosePhoto());
        add(uploadBtn);
        add(Box.createVerticalStrut(4));

        JButton clearBtn = SwingTheme.makeDangerButton("✕ Clear");
        clearBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        clearBtn.addActionListener(e -> clearPhoto());
        add(clearBtn);
        add(Box.createVerticalStrut(4));

        filenameLabel = SwingTheme.makeMuted("");
        filenameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(filenameLabel);
    }

    private void choosePhoto() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Photo of Accused");
        chooser.setFileFilter(new FileNameExtensionFilter("Images (JPG, PNG, GIF)", "jpg", "jpeg", "png", "gif"));
        chooser.setAcceptAllFileFilterUsed(false);
        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        try {
            BufferedImage img = ImageIO.read(file);
            if (img == null) { JOptionPane.showMessageDialog(this, "Could not read image file."); return; }

            // Scale to thumb for preview
            Image scaled = img.getScaledInstance(THUMB_W - 8, THUMB_H - 8, Image.SCALE_SMOOTH);
            preview.setIcon(new ImageIcon(scaled));
            preview.setText("");

            // Encode full image as base64 (resize to max 400px wide to keep data manageable)
            BufferedImage resized = resizeImage(img, 400);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            String ext = file.getName().toLowerCase().endsWith("png") ? "png" : "jpeg";
            ImageIO.write(resized, ext, baos);
            base64Data = Base64.getEncoder().encodeToString(baos.toByteArray());
            filenameLabel.setText(file.getName().length() > 22
                    ? file.getName().substring(0, 19) + "…" : file.getName());

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading image: " + ex.getMessage());
        }
    }

    private void clearPhoto() {
        base64Data = "";
        preview.setIcon(null);
        preview.setText("<html><center><font color='#9CA3AF'>No<br>Photo</font></center></html>");
        filenameLabel.setText("");
    }

    /** Call this to load an existing base64 photo (e.g. when editing a record). */
    public void loadBase64(String b64) {
        if (b64 == null || b64.isEmpty()) return;
        try {
            byte[] bytes = Base64.getDecoder().decode(b64);
            java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(bytes);
            BufferedImage img = ImageIO.read(bais);
            if (img == null) return;
            Image scaled = img.getScaledInstance(THUMB_W - 8, THUMB_H - 8, Image.SCALE_SMOOTH);
            preview.setIcon(new ImageIcon(scaled));
            preview.setText("");
            base64Data = b64;
            filenameLabel.setText("(existing photo)");
        } catch (Exception ex) { /* ignore bad stored data */ }
    }

    /** Returns the base64 string, or empty string if no photo was selected. */
    public String getBase64() { return base64Data; }

    /** Produces a scaled BufferedImage, keeping aspect ratio, max maxWidth px wide. */
    private BufferedImage resizeImage(BufferedImage src, int maxWidth) {
        if (src.getWidth() <= maxWidth) return src;
        int w = maxWidth;
        int h = (int) ((double) src.getHeight() / src.getWidth() * maxWidth);
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(src, 0, 0, w, h, null);
        g.dispose();
        return out;
    }
}
