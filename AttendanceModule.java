package modules;

import models.AttendanceRecord;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class AttendanceModule {
    private final List<AttendanceRecord> attendanceRecords = new ArrayList<>();
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final TableRowSorter<DefaultTableModel> sorter;

    public AttendanceModule() {
        String[] columns = {"Student ID", "Course Code", "Attendance %"};
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

        // Highlight low attendance (< 75%) in red text
        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value != null) {
                    String str = value.toString().replace("%", "").trim();
                    try {
                        double pct = Double.parseDouble(str);
                        if (pct < 75.0) {
                            c.setForeground(new Color(200, 30, 30));
                            c.setFont(c.getFont().deriveFont(Font.BOLD));
                        } else {
                            c.setForeground(isSelected ? table.getSelectionForeground() : new Color(30, 130, 60));
                        }
                    } catch (NumberFormatException ignored) {}
                }
                return c;
            }
        });

        preloadSampleData();
    }

    private void preloadSampleData() {
        addAttendanceInternal(new AttendanceRecord("STU1001", "CS101", 92.5));
        addAttendanceInternal(new AttendanceRecord("STU1002", "CS101", 85.0));
        addAttendanceInternal(new AttendanceRecord("STU1003", "CS201", 78.0));
        addAttendanceInternal(new AttendanceRecord("STU1004", "EC101", 68.0)); // Low attendance alert
        addAttendanceInternal(new AttendanceRecord("STU1005", "EC101", 95.0));
        addAttendanceInternal(new AttendanceRecord("STU1006", "IT301", 88.0));
        addAttendanceInternal(new AttendanceRecord("STU1007", "IT301", 62.5)); // Low attendance alert
        addAttendanceInternal(new AttendanceRecord("STU1008", "ME101", 80.0));
        addAttendanceInternal(new AttendanceRecord("STU1009", "CS101", 72.0)); // Low attendance alert
        addAttendanceInternal(new AttendanceRecord("STU1010", "CS201", 90.0));
    }

    private void addAttendanceInternal(AttendanceRecord record) {
        attendanceRecords.add(record);
        tableModel.addRow(record.toObjectArray());
    }

    public List<AttendanceRecord> getAttendanceRecords() {
        return attendanceRecords;
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

    public void addAttendanceDialog(Component parent, List<String> availableStudents, List<String> availableCourses) {
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

        JTextField pctField = new JTextField(15);

        JPanel panel = new JPanel(new GridLayout(3, 2, 8, 8));
        panel.add(new JLabel("Student ID:"));
        panel.add(studentCombo);
        panel.add(new JLabel("Course Code:"));
        panel.add(courseCombo);
        panel.add(new JLabel("Attendance % (0-100):"));
        panel.add(pctField);

        int result = JOptionPane.showConfirmDialog(parent, panel, "Add Attendance Record", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String studentId = (String) studentCombo.getSelectedItem();
            String courseCode = (String) courseCombo.getSelectedItem();
            String pctStr = pctField.getText().trim();

            if (pctStr.isEmpty()) {
                JOptionPane.showMessageDialog(parent, "Attendance percentage cannot be empty!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                double pct = Double.parseDouble(pctStr);
                if (pct < 0 || pct > 100) {
                    JOptionPane.showMessageDialog(parent, "Attendance % must be between 0 and 100!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Check duplicate
                for (AttendanceRecord ar : attendanceRecords) {
                    if (ar.getStudentId().equalsIgnoreCase(studentId) && ar.getCourseCode().equalsIgnoreCase(courseCode)) {
                        JOptionPane.showMessageDialog(parent, "Attendance record for this student and course already exists!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                AttendanceRecord rec = new AttendanceRecord(studentId, courseCode, pct);
                addAttendanceInternal(rec);
                JOptionPane.showMessageDialog(parent, "Attendance recorded successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(parent, "Attendance % must be a valid number!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void editAttendanceDialog(Component parent) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(parent, "Please select an attendance record to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);
        AttendanceRecord record = attendanceRecords.get(modelRow);

        JTextField studentField = new JTextField(record.getStudentId(), 15);
        studentField.setEditable(false);
        JTextField courseField = new JTextField(record.getCourseCode(), 15);
        courseField.setEditable(false);
        JTextField pctField = new JTextField(String.valueOf(record.getAttendancePercentage()), 15);

        JPanel panel = new JPanel(new GridLayout(3, 2, 8, 8));
        panel.add(new JLabel("Student ID:"));
        panel.add(studentField);
        panel.add(new JLabel("Course Code:"));
        panel.add(courseField);
        panel.add(new JLabel("Attendance % (0-100):"));
        panel.add(pctField);

        int result = JOptionPane.showConfirmDialog(parent, panel, "Edit Attendance Record", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String pctStr = pctField.getText().trim();
            if (pctStr.isEmpty()) {
                JOptionPane.showMessageDialog(parent, "Attendance % cannot be empty!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                double pct = Double.parseDouble(pctStr);
                if (pct < 0 || pct > 100) {
                    JOptionPane.showMessageDialog(parent, "Attendance % must be between 0 and 100!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                record.setAttendancePercentage(pct);
                tableModel.setValueAt(String.format("%.1f%%", pct), modelRow, 2);
                JOptionPane.showMessageDialog(parent, "Attendance record updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(parent, "Attendance % must be a valid number!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void deleteAttendanceDialog(Component parent) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(parent, "Please select an attendance record to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);
        AttendanceRecord record = attendanceRecords.get(modelRow);

        int confirm = JOptionPane.showConfirmDialog(parent, 
                "Are you sure you want to delete attendance record for " + record.getStudentId() + " (" + record.getCourseCode() + ")?", 
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            attendanceRecords.remove(modelRow);
            tableModel.removeRow(modelRow);
            JOptionPane.showMessageDialog(parent, "Attendance record deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
