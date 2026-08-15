package modules;

import models.Exam;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ExamModule {
    private final List<Exam> exams = new ArrayList<>();
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final TableRowSorter<DefaultTableModel> sorter;

    public ExamModule() {
        String[] columns = {"Exam ID", "Course Code", "Exam Date"};
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
    }

    private void preloadSampleData() {
        addExamInternal(new Exam("EX101", "CS101", "2026-09-15"));
        addExamInternal(new Exam("EX102", "CS201", "2026-09-18"));
        addExamInternal(new Exam("EX103", "EC101", "2026-09-20"));
        addExamInternal(new Exam("EX104", "IT301", "2026-09-22"));
        addExamInternal(new Exam("EX105", "MA102", "2026-09-25"));
        addExamInternal(new Exam("EX106", "ME101", "2026-09-28"));
    }

    private void addExamInternal(Exam exam) {
        exams.add(exam);
        tableModel.addRow(exam.toObjectArray());
    }

    public List<Exam> getExams() {
        return exams;
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

    public void addExamDialog(Component parent, List<String> availableCourses) {
        JTextField idField = new JTextField(15);
        JComboBox<String> courseCombo;
        if (availableCourses != null && !availableCourses.isEmpty()) {
            courseCombo = new JComboBox<>(availableCourses.toArray(new String[0]));
        } else {
            courseCombo = new JComboBox<>(new String[]{"CS101", "CS201", "EC101", "IT301", "MA102", "ME101"});
        }
        JTextField dateField = new JTextField("2026-10-01", 15);

        JPanel panel = new JPanel(new GridLayout(3, 2, 8, 8));
        panel.add(new JLabel("Exam ID:"));
        panel.add(idField);
        panel.add(new JLabel("Course Code:"));
        panel.add(courseCombo);
        panel.add(new JLabel("Exam Date (YYYY-MM-DD):"));
        panel.add(dateField);

        int result = JOptionPane.showConfirmDialog(parent, panel, "Schedule New Exam", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String examId = idField.getText().trim();
            String course = (String) courseCombo.getSelectedItem();
            String date = dateField.getText().trim();

            if (examId.isEmpty() || date.isEmpty()) {
                JOptionPane.showMessageDialog(parent, "Exam ID and Date cannot be empty!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            for (Exam e : exams) {
                if (e.getExamId().equalsIgnoreCase(examId)) {
                    JOptionPane.showMessageDialog(parent, "Exam ID already exists!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            addExamInternal(new Exam(examId, course, date));
            JOptionPane.showMessageDialog(parent, "Exam scheduled successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void editExamDialog(Component parent, List<String> availableCourses) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(parent, "Please select an exam to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);
        Exam exam = exams.get(modelRow);

        JTextField idField = new JTextField(exam.getExamId(), 15);
        idField.setEditable(false);
        JComboBox<String> courseCombo;
        if (availableCourses != null && !availableCourses.isEmpty()) {
            courseCombo = new JComboBox<>(availableCourses.toArray(new String[0]));
        } else {
            courseCombo = new JComboBox<>(new String[]{"CS101", "CS201", "EC101", "IT301", "MA102", "ME101"});
        }
        courseCombo.setSelectedItem(exam.getCourseCode());
        JTextField dateField = new JTextField(exam.getExamDate(), 15);

        JPanel panel = new JPanel(new GridLayout(3, 2, 8, 8));
        panel.add(new JLabel("Exam ID:"));
        panel.add(idField);
        panel.add(new JLabel("Course Code:"));
        panel.add(courseCombo);
        panel.add(new JLabel("Exam Date (YYYY-MM-DD):"));
        panel.add(dateField);

        int result = JOptionPane.showConfirmDialog(parent, panel, "Edit Scheduled Exam", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String course = (String) courseCombo.getSelectedItem();
            String date = dateField.getText().trim();

            if (date.isEmpty()) {
                JOptionPane.showMessageDialog(parent, "Exam Date cannot be empty!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            exam.setCourseCode(course);
            exam.setExamDate(date);

            tableModel.setValueAt(course, modelRow, 1);
            tableModel.setValueAt(date, modelRow, 2);

            JOptionPane.showMessageDialog(parent, "Exam schedule updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void deleteExamDialog(Component parent) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(parent, "Please select an exam schedule to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);
        Exam exam = exams.get(modelRow);

        int confirm = JOptionPane.showConfirmDialog(parent, 
                "Are you sure you want to delete exam schedule " + exam.getExamId() + "?", 
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            exams.remove(modelRow);
            tableModel.removeRow(modelRow);
            JOptionPane.showMessageDialog(parent, "Exam schedule deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
