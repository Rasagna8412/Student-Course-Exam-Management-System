package ui;

import models.AttendanceRecord;
import models.Course;
import models.GradeRecord;
import models.Student;
import modules.AttendanceModule;
import modules.CourseModule;
import modules.GradeModule;
import modules.StudentModule;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.print.PrinterException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentGradeSheetDialog extends JDialog {

    private final StudentModule studentModule;
    private final CourseModule courseModule;
    private final GradeModule gradeModule;
    private final AttendanceModule attendanceModule;

    private final JComboBox<String> studentSelectCombo;
    private final JLabel studentIdLabel;
    private final JLabel studentNameLabel;
    private final JLabel branchLabel;
    private final JLabel sectionLabel;
    private final JLabel primaryCourseLabel;

    private final DefaultTableModel tableModel;
    private final JTable gradeTable;

    private final JLabel avgMarksLabel;
    private final JLabel avgAttLabel;
    private final JLabel statusBadgeLabel;
    private final JPanel printablePanel;

    public StudentGradeSheetDialog(Frame owner, StudentModule studentModule, CourseModule courseModule,
                                   GradeModule gradeModule, AttendanceModule attendanceModule,
                                   String defaultStudentId) {
        super(owner, "Official Student Academic Grade Sheet & Transcript", true);
        this.studentModule = studentModule;
        this.courseModule = courseModule;
        this.gradeModule = gradeModule;
        this.attendanceModule = attendanceModule;

        setSize(850, 680);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        // Top Selector Bar
        JPanel selectorBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        selectorBar.setBackground(new Color(240, 243, 248));
        selectorBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(210, 215, 225)));

        JLabel selectLabel = new JLabel("Select Student:");
        selectLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        selectorBar.add(selectLabel);

        studentSelectCombo = new JComboBox<>();
        studentSelectCombo.setFont(new Font("SansSerif", Font.PLAIN, 13));
        studentSelectCombo.setPreferredSize(new Dimension(300, 28));

        populateStudentCombo();
        if (defaultStudentId != null) {
            for (int i = 0; i < studentSelectCombo.getItemCount(); i++) {
                if (studentSelectCombo.getItemAt(i).startsWith(defaultStudentId)) {
                    studentSelectCombo.setSelectedIndex(i);
                    break;
                }
            }
        }

        studentSelectCombo.addActionListener(e -> updateGradeSheet());
        selectorBar.add(studentSelectCombo);

        JButton printBtn = new JButton("🖨️ Print / Save Grade Sheet");
        printBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        printBtn.setBackground(new Color(40, 120, 180));
        printBtn.setForeground(Color.WHITE);
        printBtn.setFocusPainted(false);
        printBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        printBtn.addActionListener(e -> printGradeSheet());
        selectorBar.add(printBtn);

        add(selectorBar, BorderLayout.NORTH);

        // Main Printable Panel
        printablePanel = new JPanel(new BorderLayout(15, 15));
        printablePanel.setBackground(Color.WHITE);
        printablePanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        // Header Student Card
        JPanel studentHeaderCard = new JPanel(new GridLayout(3, 2, 12, 6));
        studentHeaderCard.setBackground(new Color(248, 250, 252));
        studentHeaderCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 235), 1),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        studentIdLabel = createInfoLabel("Student ID: ", "-");
        studentNameLabel = createInfoLabel("Student Name: ", "-");
        branchLabel = createInfoLabel("Branch: ", "-");
        sectionLabel = createInfoLabel("Section: ", "-");
        primaryCourseLabel = createInfoLabel("Enrolled Program: ", "-");

        studentHeaderCard.add(studentIdLabel);
        studentHeaderCard.add(studentNameLabel);
        studentHeaderCard.add(branchLabel);
        studentHeaderCard.add(sectionLabel);
        studentHeaderCard.add(primaryCourseLabel);

        // Grade Sheet Table
        String[] cols = {"Course Code", "Course Title", "Marks (100)", "Grade", "Classes Present", "Classes Conducted", "Attendance %"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        gradeTable = new JTable(tableModel);
        gradeTable.setRowHeight(28);
        gradeTable.setFont(new Font("SansSerif", Font.PLAIN, 12));
        gradeTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        gradeTable.getTableHeader().setBackground(new Color(230, 235, 245));

        // Format Grade Column
        gradeTable.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value != null) {
                    String g = value.toString();
                    if (g.equals("A+")) c.setForeground(new Color(40, 140, 60));
                    else if (g.equals("A")) c.setForeground(new Color(30, 110, 180));
                    else if (g.equals("B")) c.setForeground(new Color(0, 150, 170));
                    else if (g.equals("C")) c.setForeground(new Color(210, 120, 0));
                    else if (g.equals("F")) c.setForeground(new Color(200, 30, 30));
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                }
                return c;
            }
        });

        // Format Attendance Column
        gradeTable.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value != null) {
                    try {
                        double pct = Double.parseDouble(value.toString().replace("%", "").trim());
                        if (pct < 75.0) {
                            c.setForeground(new Color(200, 30, 30));
                            c.setFont(c.getFont().deriveFont(Font.BOLD));
                        } else {
                            c.setForeground(new Color(30, 130, 60));
                        }
                    } catch (NumberFormatException ignored) {}
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(gradeTable);

        // Summary Card at Bottom
        JPanel summaryCard = new JPanel(new GridLayout(1, 3, 15, 0));
        summaryCard.setBackground(new Color(245, 247, 250));
        summaryCard.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        avgMarksLabel = new JLabel("Average Marks: N/A", SwingConstants.CENTER);
        avgMarksLabel.setFont(new Font("SansSerif", Font.BOLD, 13));

        avgAttLabel = new JLabel("Overall Attendance: N/A", SwingConstants.CENTER);
        avgAttLabel.setFont(new Font("SansSerif", Font.BOLD, 13));

        statusBadgeLabel = new JLabel("Academic Status: N/A", SwingConstants.CENTER);
        statusBadgeLabel.setFont(new Font("SansSerif", Font.BOLD, 13));

        summaryCard.add(avgMarksLabel);
        summaryCard.add(avgAttLabel);
        summaryCard.add(statusBadgeLabel);

        JPanel centerContainer = new JPanel(new BorderLayout(10, 10));
        centerContainer.setOpaque(false);
        centerContainer.add(studentHeaderCard, BorderLayout.NORTH);
        centerContainer.add(scrollPane, BorderLayout.CENTER);
        centerContainer.add(summaryCard, BorderLayout.SOUTH);

        printablePanel.add(centerContainer, BorderLayout.CENTER);

        add(printablePanel, BorderLayout.CENTER);

        // Footer Close Button
        JPanel footerBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        JButton closeBtn = new JButton("Close Sheet");
        closeBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        closeBtn.addActionListener(e -> dispose());
        footerBar.add(closeBtn);
        add(footerBar, BorderLayout.SOUTH);

        updateGradeSheet();
    }

    private JLabel createInfoLabel(String prefix, String val) {
        JLabel l = new JLabel("<html><b>" + prefix + "</b> " + val + "</html>");
        l.setFont(new Font("SansSerif", Font.PLAIN, 13));
        return l;
    }

    private void populateStudentCombo() {
        studentSelectCombo.removeAllItems();
        List<Student> list = studentModule.getStudents();
        if (list.isEmpty()) {
            studentSelectCombo.addItem("-- No Students Available --");
            return;
        }
        for (Student s : list) {
            studentSelectCombo.addItem(s.getStudentId() + " - " + s.getName() + " (" + s.getBranch() + " - " + s.getSection() + ")");
        }
    }

    private void updateGradeSheet() {
        tableModel.setRowCount(0);
        int sel = studentSelectCombo.getSelectedIndex();
        if (sel < 0 || studentModule.getStudents().isEmpty()) {
            studentIdLabel.setText("<html><b>Student ID:</b> -</html>");
            studentNameLabel.setText("<html><b>Student Name:</b> -</html>");
            branchLabel.setText("<html><b>Branch:</b> -</html>");
            sectionLabel.setText("<html><b>Section:</b> -</html>");
            primaryCourseLabel.setText("<html><b>Enrolled Program:</b> -</html>");
            avgMarksLabel.setText("Average Marks: N/A");
            avgAttLabel.setText("Overall Attendance: N/A");
            statusBadgeLabel.setText("Academic Status: N/A");
            return;
        }

        Student student = studentModule.getStudents().get(sel);
        studentIdLabel.setText("<html><b>Student ID:</b> " + student.getStudentId() + "</html>");
        studentNameLabel.setText("<html><b>Student Name:</b> " + student.getName() + "</html>");
        branchLabel.setText("<html><b>Branch:</b> " + student.getBranch() + "</html>");
        sectionLabel.setText("<html><b>Section:</b> " + student.getSection() + "</html>");
        primaryCourseLabel.setText("<html><b>Enrolled Program:</b> " + student.getCourseEnrolled() + "</html>");

        Map<String, String> courseNames = new HashMap<>();
        for (Course c : courseModule.getCourses()) {
            courseNames.put(c.getCourseCode(), c.getCourseName());
        }

        Map<String, AttendanceRecord> attMap = new HashMap<>();
        for (AttendanceRecord ar : attendanceModule.getAttendanceRecords()) {
            if (ar.getStudentId().equalsIgnoreCase(student.getStudentId())) {
                attMap.put(ar.getCourseCode(), ar);
            }
        }

        double totalMarks = 0;
        int marksCount = 0;
        int totalPresent = 0;
        int totalConducted = 0;

        for (GradeRecord gr : gradeModule.getGradeRecords()) {
            if (gr.getStudentId().equalsIgnoreCase(student.getStudentId())) {
                String cCode = gr.getCourseCode();
                String cName = courseNames.getOrDefault(cCode, "N/A");
                double marks = gr.getMarks();
                String grade = gr.getGrade();

                int present = 0;
                int conducted = 0;
                String pctStr = "N/A";

                AttendanceRecord ar = attMap.get(cCode);
                if (ar != null) {
                    present = ar.getClassesPresent();
                    conducted = ar.getClassesConducted();
                    pctStr = String.format("%.1f%%", ar.getAttendancePercentage());
                    totalPresent += present;
                    totalConducted += conducted;
                }

                tableModel.addRow(new Object[]{
                        cCode, cName, String.format("%.1f", marks), grade, present, conducted, pctStr
                });

                totalMarks += marks;
                marksCount++;
            }
        }

        if (marksCount > 0) {
            double avgMarks = totalMarks / marksCount;
            avgMarksLabel.setText(String.format("Average Marks: %.2f / 100", avgMarks));
        } else {
            avgMarksLabel.setText("Average Marks: No Grades Entered");
        }

        if (totalConducted > 0) {
            double overallAttPct = ((double) totalPresent / totalConducted) * 100.0;
            avgAttLabel.setText(String.format("Overall Attendance: %.1f%% (%d/%d)", overallAttPct, totalPresent, totalConducted));
            if (overallAttPct < 75.0) {
                statusBadgeLabel.setText("Academic Status: ⚠️ Shortage of Attendance");
                statusBadgeLabel.setForeground(new Color(200, 30, 30));
            } else {
                statusBadgeLabel.setText("Academic Status: ✅ Good Standing");
                statusBadgeLabel.setForeground(new Color(30, 130, 60));
            }
        } else {
            avgAttLabel.setText("Overall Attendance: No Attendance Recorded");
            statusBadgeLabel.setText("Academic Status: Pending Data");
            statusBadgeLabel.setForeground(Color.DARK_GRAY);
        }
    }

    private void printGradeSheet() {
        if (studentSelectCombo.getSelectedIndex() < 0 || studentModule.getStudents().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No student selected to print.", "Print Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Student s = studentModule.getStudents().get(studentSelectCombo.getSelectedIndex());

        try {
            MessageFormat header = new MessageFormat("Official Student Grade Sheet - " + s.getName() + " (" + s.getStudentId() + ")");
            MessageFormat footer = new MessageFormat("Branch: " + s.getBranch() + " | Section: " + s.getSection() + " | Page {0}");

            boolean complete = gradeTable.print(JTable.PrintMode.FIT_WIDTH, header, footer);
            if (complete) {
                JOptionPane.showMessageDialog(this, "Grade sheet sent to printer / PDF file successfully!", "Print Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (PrinterException ex) {
            JOptionPane.showMessageDialog(this, "Printing failed: " + ex.getMessage(), "Print Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
