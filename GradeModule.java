package modules;

import models.GradeRecord;
import services.RelativeGradingEngine;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GradeModule {
    private final List<GradeRecord> gradeRecords = new ArrayList<>();
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final TableRowSorter<DefaultTableModel> sorter;
    private final RelativeGradingEngine gradingEngine;

    public GradeModule(RelativeGradingEngine gradingEngine) {
        this.gradingEngine = gradingEngine;
        String[] columns = {"Student ID", "Course Code", "Marks", "Grade"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(28);
        table.getTableHeader().setReorderingAllowed(false);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));

        preloadSampleData();
        recalculateAllGrades();
    }

    private void preloadSampleData() {
        // CS101 Marks
        addGradeInternal(new GradeRecord("STU1001", "CS101", 94.5, "Pending"));
        addGradeInternal(new GradeRecord("STU1002", "CS101", 88.0, "Pending"));
        addGradeInternal(new GradeRecord("STU1009", "CS101", 76.0, "Pending"));
        
        // CS201 Marks
        addGradeInternal(new GradeRecord("STU1003", "CS201", 92.0, "Pending"));
        addGradeInternal(new GradeRecord("STU1010", "CS201", 64.0, "Pending"));

        // EC101 Marks
        addGradeInternal(new GradeRecord("STU1004", "EC101", 82.0, "Pending"));
        addGradeInternal(new GradeRecord("STU1005", "EC101", 58.5, "Pending"));

        // IT301 Marks
        addGradeInternal(new GradeRecord("STU1006", "IT301", 91.0, "Pending"));
        addGradeInternal(new GradeRecord("STU1007", "IT301", 84.0, "Pending"));

        // ME101 Marks
        addGradeInternal(new GradeRecord("STU1008", "ME101", 72.0, "Pending"));
    }

    private void addGradeInternal(GradeRecord record) {
        gradeRecords.add(record);
        tableModel.addRow(record.toObjectArray());
    }

    public List<GradeRecord> getGradeRecords() {
        return gradeRecords;
    }

    public JTable getTable() {
        return table;
    }

    public TableRowSorter<DefaultTableModel> getSorter() {
        return sorter;
    }

    public void filter(String query) {
        if (query == null || query.trim().isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + query.trim()));
        }
    }

    public void recalculateAllGrades() {
        gradingEngine.calculateGrades(gradeRecords);
        refreshTable();
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        for (GradeRecord rec : gradeRecords) {
            tableModel.addRow(rec.toObjectArray());
        }
    }

    public void addGradeDialog(Component parent, List<String> availableStudents, List<String> availableCourses) {
        JComboBox<String> studentCombo;
        if (availableStudents != null && !availableStudents.isEmpty()) {
            studentCombo = new JComboBox<>(availableStudents.toArray(new String[0]));
        } else {
            studentCombo = new JComboBox<>(new String[]{"STU1001", "STU1002", "STU1003", "STU1004"});
        }

        JComboBox<String> courseCombo;
        if (availableCourses != null && !availableCourses.isEmpty()) {
            courseCombo = new JComboBox<>(availableCourses.toArray(new String[0]));
        } else {
            courseCombo = new JComboBox<>(new String[]{"CS101", "CS201", "EC101", "IT301"});
        }

        JTextField marksField = new JTextField(15);

        JPanel panel = new JPanel(new GridLayout(3, 2, 8, 8));
        panel.add(new JLabel("Student ID:"));
        panel.add(studentCombo);
        panel.add(new JLabel("Course Code:"));
        panel.add(courseCombo);
        panel.add(new JLabel("Marks (0-100):"));
        panel.add(marksField);

        int result = JOptionPane.showConfirmDialog(parent, panel, "Add Grade Entry", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String studentId = (String) studentCombo.getSelectedItem();
            String courseCode = (String) courseCombo.getSelectedItem();
            String marksStr = marksField.getText().trim();

            if (marksStr.isEmpty()) {
                JOptionPane.showMessageDialog(parent, "Marks cannot be empty!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                double marks = Double.parseDouble(marksStr);
                if (marks < 0 || marks > 100) {
                    JOptionPane.showMessageDialog(parent, "Marks must be between 0 and 100!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Check duplicate record for same student and course
                for (GradeRecord gr : gradeRecords) {
                    if (gr.getStudentId().equalsIgnoreCase(studentId) && gr.getCourseCode().equalsIgnoreCase(courseCode)) {
                        JOptionPane.showMessageDialog(parent, "Grade record for this student and course already exists!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                GradeRecord newRecord = new GradeRecord(studentId, courseCode, marks, "Pending");
                gradeRecords.add(newRecord);
                recalculateAllGrades();
                JOptionPane.showMessageDialog(parent, "Grade recorded and relative grades updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(parent, "Marks must be a valid number!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void editGradeDialog(Component parent) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(parent, "Please select a grade record to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);
        GradeRecord record = gradeRecords.get(modelRow);

        JTextField studentField = new JTextField(record.getStudentId(), 15);
        studentField.setEditable(false);
        JTextField courseField = new JTextField(record.getCourseCode(), 15);
        courseField.setEditable(false);
        JTextField marksField = new JTextField(String.valueOf(record.getMarks()), 15);

        JPanel panel = new JPanel(new GridLayout(3, 2, 8, 8));
        panel.add(new JLabel("Student ID:"));
        panel.add(studentField);
        panel.add(new JLabel("Course Code:"));
        panel.add(courseField);
        panel.add(new JLabel("Marks (0-100):"));
        panel.add(marksField);

        int result = JOptionPane.showConfirmDialog(parent, panel, "Edit Grade Entry", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String marksStr = marksField.getText().trim();
            if (marksStr.isEmpty()) {
                JOptionPane.showMessageDialog(parent, "Marks cannot be empty!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                double marks = Double.parseDouble(marksStr);
                if (marks < 0 || marks > 100) {
                    JOptionPane.showMessageDialog(parent, "Marks must be between 0 and 100!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                record.setMarks(marks);
                recalculateAllGrades();
                JOptionPane.showMessageDialog(parent, "Marks updated and relative grades recalculated!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(parent, "Marks must be a valid number!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void deleteGradeDialog(Component parent) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(parent, "Please select a grade record to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);
        GradeRecord record = gradeRecords.get(modelRow);

        int confirm = JOptionPane.showConfirmDialog(parent, 
                "Are you sure you want to delete grade record for " + record.getStudentId() + " (" + record.getCourseCode() + ")?", 
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            gradeRecords.remove(modelRow);
            recalculateAllGrades();
            JOptionPane.showMessageDialog(parent, "Grade record deleted and relative grades recalculated!", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
