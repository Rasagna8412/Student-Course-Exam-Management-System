package ui;

import services.RelativeGradingEngine;

import javax.swing.*;
import java.awt.*;

public class GradingConfigDialog extends JDialog {
    private final RelativeGradingEngine engine;
    private boolean updated = false;

    public GradingConfigDialog(Frame owner, RelativeGradingEngine engine) {
        super(owner, "Relative Grading Configuration", true);
        this.engine = engine;

        setLayout(new BorderLayout(15, 15));
        setResizable(false);

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        headerPanel.setBackground(new Color(40, 50, 70));
        JLabel titleLabel = new JLabel("Configure Relative Grading Percentile Bands");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JTextField aPlusField = new JTextField(String.valueOf(engine.getTopAPlusPct()), 10);
        JTextField aField = new JTextField(String.valueOf(engine.getNextAPct()), 10);
        JTextField bField = new JTextField(String.valueOf(engine.getNextBPct()), 10);
        JTextField cField = new JTextField(String.valueOf(engine.getNextCPct()), 10);
        JLabel fLabel = new JLabel(String.format("%.1f%% (Automatic remainder)", engine.getFPct()));
        fLabel.setFont(new Font("SansSerif", Font.BOLD, 12));

        formPanel.add(new JLabel("A+ Percentile Band (Top %):"));
        formPanel.add(aPlusField);
        formPanel.add(new JLabel("A Percentile Band (Next %):"));
        formPanel.add(aField);
        formPanel.add(new JLabel("B Percentile Band (Next %):"));
        formPanel.add(bField);
        formPanel.add(new JLabel("C Percentile Band (Next %):"));
        formPanel.add(cField);
        formPanel.add(new JLabel("F Grade Band (Remainder):"));
        formPanel.add(fLabel);

        add(formPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        JButton saveBtn = new JButton("Apply & Recalculate");
        saveBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        saveBtn.setBackground(new Color(50, 120, 200));
        saveBtn.setForeground(Color.WHITE);

        JButton cancelBtn = new JButton("Cancel");

        saveBtn.addActionListener(e -> {
            try {
                double aPlus = Double.parseDouble(aPlusField.getText().trim());
                double aVal = Double.parseDouble(aField.getText().trim());
                double bVal = Double.parseDouble(bField.getText().trim());
                double cVal = Double.parseDouble(cField.getText().trim());

                if (aPlus < 0 || aVal < 0 || bVal < 0 || cVal < 0) {
                    JOptionPane.showMessageDialog(this, "Percentile values must be non-negative!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (aPlus + aVal + bVal + cVal > 100.0) {
                    JOptionPane.showMessageDialog(this, "The sum of percentages cannot exceed 100%!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                engine.setThresholds(aPlus, aVal, bVal, cVal);
                updated = true;
                dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid numeric percentage values!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dispose());

        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);
        add(btnPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(owner);
    }

    public boolean isUpdated() {
        return updated;
    }
}
