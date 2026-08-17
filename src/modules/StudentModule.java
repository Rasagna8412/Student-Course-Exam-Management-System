package modules;

import models.Student;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class StudentModule {
    private final List<Student> students = new ArrayList<>();
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final TableRowSorter<DefaultTableModel> sorter;
    private Runnable saveCallback;

    public StudentModule() {
        String[] columns = {"Student ID", "Name", "Branch", "Section", "Course Enrolled"};
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

    public void loadRecords(List<Student> recordList) {
        students.clear();
        tableModel.setRowCount(0);
        if (recordList != null) {
            for (Student s : recordList) {
                addStudentInternal(s);
            }
        }
    }

    private void addStudentInternal(Student student) {
        students.add(student);
        tableModel.addRow(student.toObjectArray());
    }

    public List<Student> getStudents() {
        return students;
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

    public void addStudentDialog(Component parent, List<String> availableCourseCodes) {
        JTextField idField = new JTextField(15);
        JTextField nameField = new JTextField(15);
        JComboBox<String> branchCombo = new JComboBox<>(new String[]{"CSE", "ECE", "IT", "ME", "EE", "CIVIL"});
        JComboBox<String> sectionCombo = new JComboBox<>(new String[]{"A", "B", "C"});
        
        JComboBox<String> courseCombo;
        if (availableCourseCodes != null && !availableCourseCodes.isEmpty()) {
            courseCombo = new JComboBox<>(availableCourseCodes.toArray(new String[0]));
        } else {
            courseCombo = new JComboBox<>(new String[]{"CS101", "CS201", "EC101", "IT301", "MA102", "ME101"});
        }

        JPanel panel = new JPanel(new GridLayout(5, 2, 8, 8));
        panel.add(new JLabel("Student ID:"));
        panel.add(idField);
        panel.add(new JLabel("Student Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Branch:"));
        panel.add(branchCombo);
        panel.add(new JLabel("Section:"));
        panel.add(sectionCombo);
        panel.add(new JLabel("Course Enrolled:"));
        panel.add(courseCombo);

        int result = JOptionPane.showConfirmDialog(parent, panel, "Add New Student Enrollment", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String id = idField.getText().trim();
            String name = nameField.getText().trim();
            String branch = (String) branchCombo.getSelectedItem();
            String section = (String) sectionCombo.getSelectedItem();
            String course = (String) courseCombo.getSelectedItem();

            if (id.isEmpty() || name.isEmpty()) {
                JOptionPane.showMessageDialog(parent, "Student ID and Name cannot be empty!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            for (Student s : students) {
                if (s.getStudentId().equalsIgnoreCase(id)) {
                    JOptionPane.showMessageDialog(parent, "Student ID already exists!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            addStudentInternal(new Student(id, name, branch, section, course));
            notifySave();
            JOptionPane.showMessageDialog(parent, "Student enrolled successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void editStudentDialog(Component parent, List<String> availableCourseCodes) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(parent, "Please select a student to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);
        Student student = students.get(modelRow);

        JTextField idField = new JTextField(student.getStudentId(), 15);
        idField.setEditable(false);
        JTextField nameField = new JTextField(student.getName(), 15);
        JComboBox<String> branchCombo = new JComboBox<>(new String[]{"CSE", "ECE", "IT", "ME", "EE", "CIVIL"});
        branchCombo.setSelectedItem(student.getBranch());
        JComboBox<String> sectionCombo = new JComboBox<>(new String[]{"A", "B", "C"});
        sectionCombo.setSelectedItem(student.getSection());

        JComboBox<String> courseCombo;
        if (availableCourseCodes != null && !availableCourseCodes.isEmpty()) {
            courseCombo = new JComboBox<>(availableCourseCodes.toArray(new String[0]));
        } else {
            courseCombo = new JComboBox<>(new String[]{"CS101", "CS201", "EC101", "IT301", "MA102", "ME101"});
        }
        courseCombo.setSelectedItem(student.getCourseEnrolled());

        JPanel panel = new JPanel(new GridLayout(5, 2, 8, 8));
        panel.add(new JLabel("Student ID:"));
        panel.add(idField);
        panel.add(new JLabel("Student Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Branch:"));
        panel.add(branchCombo);
        panel.add(new JLabel("Section:"));
        panel.add(sectionCombo);
        panel.add(new JLabel("Course Enrolled:"));
        panel.add(courseCombo);

        int result = JOptionPane.showConfirmDialog(parent, panel, "Edit Student Enrollment", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String branch = (String) branchCombo.getSelectedItem();
            String section = (String) sectionCombo.getSelectedItem();
            String course = (String) courseCombo.getSelectedItem();

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(parent, "Student Name cannot be empty!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            student.setName(name);
            student.setBranch(branch);
            student.setSection(section);
            student.setCourseEnrolled(course);

            tableModel.setValueAt(name, modelRow, 1);
            tableModel.setValueAt(branch, modelRow, 2);
            tableModel.setValueAt(section, modelRow, 3);
            tableModel.setValueAt(course, modelRow, 4);

            notifySave();
            JOptionPane.showMessageDialog(parent, "Student details updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void deleteStudentDialog(Component parent) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(parent, "Please select a student to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);
        Student student = students.get(modelRow);

        int confirm = JOptionPane.showConfirmDialog(parent, 
                "Are you sure you want to remove student " + student.getStudentId() + " (" + student.getName() + ")?", 
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            students.remove(modelRow);
            tableModel.removeRow(modelRow);
            notifySave();
            JOptionPane.showMessageDialog(parent, "Student record deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
