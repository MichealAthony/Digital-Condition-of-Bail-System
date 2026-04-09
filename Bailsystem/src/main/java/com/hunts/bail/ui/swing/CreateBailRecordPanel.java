package com.hunts.bail.ui.swing;

import com.hunts.bail.controller.BailRecordController;
import com.hunts.bail.domain.*;
import com.hunts.bail.enums.ConditionType;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** UC1 – Create Bail Record form with photo upload. */
public class CreateBailRecordPanel extends JPanel {

    private final BailRecordController controller;
    private final User                  currentUser;
    private final Runnable              onSuccess;

    private JTextField fullNameField, dobField, nationalIdField,
                       addressField, contactField, biometricField;
    private DefaultListModel<String>       offenseModel;
    private JTextField                     offenseInput;
    private DefaultListModel<BailCondition> conditionModel;
    private JComboBox<ConditionType>       condTypeCombo;
    private JTextField                     condDescField, condParamsField;
    private JTextArea                      suretyArea;
    private JLabel                         statusLabel;
    private PhotoUploadHelper              photoWidget;

    public CreateBailRecordPanel(BailRecordController controller, User currentUser, Runnable onSuccess) {
        this.controller  = controller;
        this.currentUser = currentUser;
        this.onSuccess   = onSuccess;
        setBackground(SwingTheme.BG_MAIN);
        setLayout(new BorderLayout());
        buildContent();
    }

    private void buildContent() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(SwingTheme.BG_MAIN);
        header.setBorder(new EmptyBorder(24, 28, 12, 28));
        JPanel ht = new JPanel(); ht.setOpaque(false); ht.setLayout(new BoxLayout(ht, BoxLayout.Y_AXIS));
        JLabel t = SwingTheme.makeTitle("Create Bail Record");
        JLabel s = SwingTheme.makeMuted("Complete all required fields marked * and submit.");
        t.setAlignmentX(Component.LEFT_ALIGNMENT); s.setAlignmentX(Component.LEFT_ALIGNMENT);
        ht.add(t); ht.add(Box.createVerticalStrut(2)); ht.add(s);
        header.add(ht, BorderLayout.WEST);

        JPanel form = new JPanel();
        form.setBackground(SwingTheme.BG_MAIN);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(0, 28, 28, 28));
        form.add(buildAccusedSection());
        form.add(Box.createVerticalStrut(14));
        form.add(buildOffensesSection());
        form.add(Box.createVerticalStrut(14));
        form.add(buildConditionsSection());
        form.add(Box.createVerticalStrut(14));
        form.add(buildSuretySection());
        form.add(Box.createVerticalStrut(14));
        form.add(buildActionRow());

        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getViewport().setBackground(SwingTheme.BG_MAIN);

        statusLabel = new JLabel(" ");
        statusLabel.setFont(SwingTheme.FONT_SMALL);
        statusLabel.setBorder(new EmptyBorder(4, 28, 8, 28));

        add(header,      BorderLayout.NORTH);
        add(scroll,      BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
    }

    private JPanel buildAccusedSection() {
        JPanel card = sectionCard("Accused Person Details");
        card.setLayout(new BorderLayout(12, 0));

        // Left: photo widget
        photoWidget = new PhotoUploadHelper();
        JPanel photoWrap = new JPanel();
        photoWrap.setOpaque(false);
        photoWrap.setBorder(new EmptyBorder(4, 4, 4, 12));
        photoWrap.setLayout(new BoxLayout(photoWrap, BoxLayout.Y_AXIS));
        photoWrap.add(SwingTheme.makeLabel("Photo"));
        photoWrap.add(Box.createVerticalStrut(4));
        photoWrap.add(photoWidget);
        card.add(photoWrap, BorderLayout.WEST);

        // Right: fields grid
        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL; gc.insets = new Insets(4,6,4,6); gc.weightx = 0.5;

        fullNameField = SwingTheme.makeField(); dobField = SwingTheme.makeField();
        nationalIdField = SwingTheme.makeField(); addressField = SwingTheme.makeField();
        contactField = SwingTheme.makeField(); biometricField = SwingTheme.makeField();

        addRow(fields, gc, 0, "Full Name *", fullNameField, "Date of Birth * (YYYY-MM-DD)", dobField);
        addRow(fields, gc, 1, "National ID *", nationalIdField, "Contact Number *", contactField);
        addRow(fields, gc, 2, "Address *", addressField, "Biometric Reference", biometricField);
        card.add(fields, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildOffensesSection() {
        JPanel card = sectionCard("Offenses Charged *");
        card.setLayout(new BorderLayout(8, 8));
        offenseModel = new DefaultListModel<>();
        JList<String> list = new JList<>(offenseModel);
        list.setFont(SwingTheme.FONT_BODY);
        JScrollPane sp = new JScrollPane(list);
        sp.setPreferredSize(new Dimension(0, 80));
        sp.setBorder(BorderFactory.createLineBorder(SwingTheme.BORDER_CLR));
        offenseInput = SwingTheme.makeField();
        JButton addBtn = SwingTheme.makeSecondaryButton("Add");
        JButton remBtn = SwingTheme.makeDangerButton("Remove");
        addBtn.addActionListener(e -> { String tx = offenseInput.getText().trim(); if (!tx.isEmpty()) { offenseModel.addElement(tx); offenseInput.setText(""); } });
        remBtn.addActionListener(e -> { int i = list.getSelectedIndex(); if (i >= 0) offenseModel.remove(i); });
        offenseInput.addActionListener(e -> addBtn.doClick());
        JPanel row = new JPanel(new BorderLayout(6, 0)); row.setOpaque(false);
        row.add(offenseInput, BorderLayout.CENTER);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0)); btns.setOpaque(false);
        btns.add(addBtn); btns.add(remBtn); row.add(btns, BorderLayout.EAST);
        card.add(sp, BorderLayout.CENTER); card.add(row, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildConditionsSection() {
        JPanel card = sectionCard("Bail Conditions * (at least one required)");
        card.setLayout(new BorderLayout(8, 8));
        conditionModel = new DefaultListModel<>();
        JList<BailCondition> condList = new JList<>(conditionModel);
        condList.setFont(SwingTheme.FONT_BODY);
        condList.setCellRenderer((l, v, i, sel, foc) -> {
            JLabel lbl = new JLabel("  [" + v.getConditionType().getDisplayName() + "] " + v.getDescription());
            lbl.setFont(SwingTheme.FONT_BODY); lbl.setOpaque(true);
            lbl.setBackground(sel ? new Color(0xDBEAFE) : i%2==0 ? SwingTheme.BG_PANEL : SwingTheme.BG_TABLE_ALT);
            lbl.setBorder(new EmptyBorder(4,4,4,4)); return lbl;
        });
        JScrollPane sp = new JScrollPane(condList); sp.setPreferredSize(new Dimension(0, 100));
        sp.setBorder(BorderFactory.createLineBorder(SwingTheme.BORDER_CLR));
        condTypeCombo = SwingTheme.makeCombo(ConditionType.values());
        condDescField = SwingTheme.makeField(); condParamsField = SwingTheme.makeField();
        JPanel inputs = new JPanel(new GridBagLayout()); inputs.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints(); gc.fill = GridBagConstraints.HORIZONTAL; gc.insets = new Insets(3,4,3,4);
        gc.gridx=0;gc.gridy=0;gc.weightx=0;inputs.add(SwingTheme.makeLabel("Type:"),gc);
        gc.gridx=1;gc.weightx=0.3;inputs.add(condTypeCombo,gc);
        gc.gridx=2;gc.weightx=0;inputs.add(SwingTheme.makeLabel("Description:"),gc);
        gc.gridx=3;gc.weightx=0.7;inputs.add(condDescField,gc);
        gc.gridx=0;gc.gridy=1;gc.weightx=0;inputs.add(SwingTheme.makeLabel("Parameters:"),gc);
        gc.gridx=1;gc.gridy=1;gc.weightx=1;gc.gridwidth=3;inputs.add(condParamsField,gc);
        JButton addCond = SwingTheme.makeSecondaryButton("Add Condition");
        JButton remCond = SwingTheme.makeDangerButton("Remove");
        addCond.addActionListener(e -> {
            String desc = condDescField.getText().trim();
            if (desc.isEmpty()) return;
            conditionModel.addElement(new BailCondition(UUID.randomUUID().toString(),
                    (ConditionType) condTypeCombo.getSelectedItem(), desc, condParamsField.getText().trim()));
            condDescField.setText(""); condParamsField.setText("");
        });
        remCond.addActionListener(e -> { int i = condList.getSelectedIndex(); if (i >= 0) conditionModel.remove(i); });
        JPanel row = new JPanel(new BorderLayout(8,0)); row.setOpaque(false);
        row.add(inputs, BorderLayout.CENTER);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT,6,0)); btns.setOpaque(false);
        btns.add(addCond); btns.add(remCond); row.add(btns, BorderLayout.EAST);
        card.add(sp, BorderLayout.CENTER); card.add(row, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildSuretySection() {
        JPanel card = sectionCard("Surety Information");
        card.setLayout(new BorderLayout());
        suretyArea = SwingTheme.makeTextArea(3, 60);
        card.add(new JScrollPane(suretyArea), BorderLayout.CENTER);
        return card;
    }

    private JPanel buildActionRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        row.setOpaque(false);
        JButton clearBtn  = SwingTheme.makeSecondaryButton("Clear Form");
        JButton submitBtn = SwingTheme.makePrimaryButton("Submit Bail Record");
        clearBtn.addActionListener(e -> clearForm());
        submitBtn.addActionListener(e -> submitForm());
        row.add(clearBtn); row.add(submitBtn);
        return row;
    }

    private void submitForm() {
        LocalDate dob;
        try { dob = LocalDate.parse(dobField.getText().trim()); } catch (DateTimeParseException ex) { dob = null; }
        List<String> offenses = new ArrayList<>();
        for (int i = 0; i < offenseModel.size(); i++) offenses.add(offenseModel.get(i));
        AccusedPerson.Builder ab = AccusedPerson.builder(UUID.randomUUID().toString())
                .fullName(fullNameField.getText().trim())
                .nationalId(nationalIdField.getText().trim())
                .address(addressField.getText().trim())
                .contactNumber(contactField.getText().trim())
                .biometricRef(biometricField.getText().trim())
                .photoBase64(photoWidget.getBase64())
                .offenses(offenses);
        if (dob != null) ab.dateOfBirth(dob);
        List<BailCondition> conditions = new ArrayList<>();
        for (int i = 0; i < conditionModel.size(); i++) conditions.add(conditionModel.get(i));
        BailRecord result = controller.handleCreateBailRecord(currentUser, ab.build(), conditions, suretyArea.getText().trim());
        if (result != null) {
            flashStatus("✔  Bail record " + result.getBailId() + " created.", SwingTheme.SUCCESS);
            clearForm();
            onSuccess.run();
        } else {
            flashStatus("✘  Submission failed — check errors above.", SwingTheme.ERROR);
        }
    }

    private void clearForm() {
        fullNameField.setText(""); dobField.setText(""); nationalIdField.setText("");
        addressField.setText(""); contactField.setText(""); biometricField.setText("");
        offenseModel.clear(); conditionModel.clear(); suretyArea.setText("");
        condDescField.setText(""); condParamsField.setText("");
        photoWidget.loadBase64("");
        statusLabel.setText(" ");
    }

    private void flashStatus(String msg, Color color) { statusLabel.setText(msg); statusLabel.setForeground(color); }

    private JPanel sectionCard(String title) {
        JPanel card = new JPanel(); card.setBackground(SwingTheme.BG_PANEL);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        TitledBorder tb = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(SwingTheme.BORDER_CLR), " " + title + " ");
        tb.setTitleFont(SwingTheme.FONT_HEADING); tb.setTitleColor(SwingTheme.NAVY);
        card.setBorder(BorderFactory.createCompoundBorder(tb, new EmptyBorder(8,12,12,12)));
        return card;
    }

    private void addRow(JPanel card, GridBagConstraints gc, int row, String l1, JComponent f1, String l2, JComponent f2) {
        gc.gridy = row*2; gc.gridx=0; card.add(makeBlock(l1,f1),gc); gc.gridx=1; card.add(makeBlock(l2,f2),gc);
    }

    private JPanel makeBlock(String label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0,3)); p.setOpaque(false); p.setBorder(new EmptyBorder(4,4,4,4));
        p.add(SwingTheme.makeLabel(label), BorderLayout.NORTH);
        field.setPreferredSize(new Dimension(0,36)); p.add(field, BorderLayout.CENTER);
        return p;
    }
}
