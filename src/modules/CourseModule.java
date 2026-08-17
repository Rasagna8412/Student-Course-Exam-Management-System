package modules;

import models.Course;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CourseModule {
    private final List<Course> courses = new ArrayList<>();
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final TableRowSorter<DefaultTableModel> sorter;
    private Runnable saveCallback;

    public CourseModule() {
        String[] columns = {"Course Code", "Course Name", "Credits"};
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
    }

    public void setSaveCallback(Runnable saveCallback) {
        this.saveCallback = saveCallback;
    }

    private void notifySave() {
        if (saveCallback != null) {
            saveCallback.run();
        }
    }

    public void loadRecords(List<Course> recordList) {
        courses.clear();
        tableModel.setRowCount(0);
        if (recordList != null) {
            for (Course c : recordList) {
                addCourseInternal(c);
            }
        }
    }

    private void addCourseInternal(Course course) {
        courses.add(course);
        tableModel.addRow(course.toObjectArray());
    }

    public List<Course> getCourses() {
        return courses;
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

    public void addCourseDialog(Component parent) {
        JTextField codeField = new JTextField(15);
        JTextField nameField = new JTextField(15);
        JTextField creditsField = new JTextField(15);

        JPanel panel = new JPanel(new GridLayout(3, 2, 8, 8));
        panel.add(new JLabel("Course Code:"));
        panel.add(codeField);
        panel.add(new JLabel("Course Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Credits:"));
        panel.add(creditsField);

        int result = JOptionPane.showConfirmDialog(parent, panel, "Add New Course", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String code = codeField.getText().trim();
            String name = nameField.getText().trim();
            String creditsStr = creditsField.getText().trim();

            if (code.isEmpty() || name.isEmpty() || creditsStr.isEmpty()) {
                JOptionPane.showMessageDialog(parent, "All fields are required!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Check duplicate course code
            for (Course c : courses) {
                if (c.getCourseCode().equalsIgnoreCase(code)) {
                    JOptionPane.showMessageDialog(parent, "Course Code already exists!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            try {
                int credits = Integer.parseInt(creditsStr);
                if (credits <= 0) {
                    JOptionPane.showMessageDialog(parent, "Credits must be a positive integer!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                addCourseInternal(new Course(code, name, credits));
                notifySave();
                JOptionPane.showMessageDialog(parent, "Course added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(parent, "Credits must be a valid numeric integer!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void editCourseDialog(Component parent) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(parent, "Please select a course to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);
        Course course = courses.get(modelRow);

        JTextField codeField = new JTextField(course.getCourseCode(), 15);
        codeField.setEditable(false); // Course code is key
        JTextField nameField = new JTextField(course.getCourseName(), 15);
        JTextField creditsField = new JTextField(String.valueOf(course.getCredits()), 15);

        JPanel panel = new JPanel(new GridLayout(3, 2, 8, 8));
        panel.add(new JLabel("Course Code:"));
        panel.add(codeField);
        panel.add(new JLabel("Course Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Credits:"));
        panel.add(creditsField);

        int result = JOptionPane.showConfirmDialog(parent, panel, "Edit Course", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String creditsStr = creditsField.getText().trim();

            if (name.isEmpty() || creditsStr.isEmpty()) {
                JOptionPane.showMessageDialog(parent, "All fields are required!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                int credits = Integer.parseInt(creditsStr);
                if (credits <= 0) {
                    JOptionPane.showMessageDialog(parent, "Credits must be a positive integer!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                course.setCourseName(name);
                course.setCredits(credits);

                tableModel.setValueAt(name, modelRow, 1);
                tableModel.setValueAt(credits, modelRow, 2);
                notifySave();
                JOptionPane.showMessageDialog(parent, "Course updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(parent, "Credits must be a valid numeric integer!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void deleteCourseDialog(Component parent) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(parent, "Please select a course to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);
        Course course = courses.get(modelRow);

        int confirm = JOptionPane.showConfirmDialog(parent, 
                "Are you sure you want to delete course " + course.getCourseCode() + " - " + course.getCourseName() + "?", 
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            courses.remove(modelRow);
            tableModel.removeRow(modelRow);
            notifySave();
            JOptionPane.showMessageDialog(parent, "Course deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
