package ui;

import models.AttendanceRecord;
import models.GradeRecord;
import models.Student;
import modules.AttendanceModule;
import modules.GradeModule;
import modules.StudentModule;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;

public class SummaryReportDialog extends JDialog {

    public SummaryReportDialog(Frame owner, StudentModule studentModule, GradeModule gradeModule, AttendanceModule attendanceModule) {
        super(owner, "Executive Summary & Performance Analytics", true);
        setSize(850, 600);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(30, 45, 65));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("Academic Performance & Attendance Summary");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("Analytics grouped by Branch & Section with Grade Distribution");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(200, 210, 225));

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        JTabbedPane reportTabs = new JTabbedPane();
        reportTabs.setFont(new Font("SansSerif", Font.BOLD, 12));

        // Tab 1: Branch & Section Performance Averages
        JPanel branchSecPanel = createBranchSectionSummaryPanel(studentModule, gradeModule, attendanceModule);
        reportTabs.addTab("Branch & Section Analytics", branchSecPanel);

        // Tab 2: Grade Distribution Visualizer
        JPanel chartTabPanel = new JPanel(new BorderLayout());
        chartTabPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GradeDistributionChartPanel chartPanel = new GradeDistributionChartPanel();
        chartPanel.updateData(gradeModule.getGradeRecords());
        chartTabPanel.add(chartPanel, BorderLayout.CENTER);
        reportTabs.addTab("Grade Distribution Chart", chartTabPanel);

        // Tab 3: Low Attendance Alert Report (< 75%)
        JPanel lowAttendancePanel = createLowAttendancePanel(studentModule, attendanceModule);
        reportTabs.addTab("Low Attendance Alerts (< 75%)", lowAttendancePanel);

        add(reportTabs, BorderLayout.CENTER);

        // Footer close button
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        JButton closeBtn = new JButton("Close Report");
        closeBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        closeBtn.addActionListener(e -> dispose());
        footerPanel.add(closeBtn);
        add(footerPanel, BorderLayout.SOUTH);
    }

    private JPanel createBranchSectionSummaryPanel(StudentModule studentModule, GradeModule gradeModule, AttendanceModule attendanceModule) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Map key: "Branch - Section" -> Stats
        Map<String, StudentGroupStats> statsMap = new TreeMap<>();

        Map<String, Student> studentMap = new HashMap<>();
        for (Student s : studentModule.getStudents()) {
            studentMap.put(s.getStudentId(), s);
            String groupKey = s.getBranch() + " - Sec " + s.getSection();
            statsMap.putIfAbsent(groupKey, new StudentGroupStats(s.getBranch(), s.getSection()));
            statsMap.get(groupKey).studentCount++;
        }

        // Aggregate marks
        for (GradeRecord gr : gradeModule.getGradeRecords()) {
            Student s = studentMap.get(gr.getStudentId());
            if (s != null) {
                String groupKey = s.getBranch() + " - Sec " + s.getSection();
                StudentGroupStats stats = statsMap.get(groupKey);
                if (stats != null) {
                    stats.totalMarks += gr.getMarks();
                    stats.marksCount++;
                }
            }
        }

        // Aggregate attendance
        for (AttendanceRecord ar : attendanceModule.getAttendanceRecords()) {
            Student s = studentMap.get(ar.getStudentId());
            if (s != null) {
                String groupKey = s.getBranch() + " - Sec " + s.getSection();
                StudentGroupStats stats = statsMap.get(groupKey);
                if (stats != null) {
                    stats.totalAttendance += ar.getAttendancePercentage();
                    stats.attendanceCount++;
                }
            }
        }

        String[] cols = {"Branch", "Section", "Students Enrolled", "Avg Marks", "Avg Attendance %"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        for (StudentGroupStats stats : statsMap.values()) {
            double avgMarks = stats.marksCount > 0 ? (stats.totalMarks / stats.marksCount) : 0.0;
            double avgAtt = stats.attendanceCount > 0 ? (stats.totalAttendance / stats.attendanceCount) : 0.0;

            model.addRow(new Object[]{
                    stats.branch,
                    stats.section,
                    stats.studentCount,
                    String.format("%.2f", avgMarks),
                    String.format("%.1f%%", avgAtt)
            });
        }

        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createLowAttendancePanel(StudentModule studentModule, AttendanceModule attendanceModule) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        Map<String, Student> studentMap = new HashMap<>();
        for (Student s : studentModule.getStudents()) {
            studentMap.put(s.getStudentId(), s);
        }

        String[] cols = {"Student ID", "Student Name", "Branch", "Section", "Course Code", "Attendance %"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        int alertCount = 0;
        for (AttendanceRecord ar : attendanceModule.getAttendanceRecords()) {
            if (ar.getAttendancePercentage() < 75.0) {
                alertCount++;
                Student s = studentMap.get(ar.getStudentId());
                String name = s != null ? s.getName() : "Unknown";
                String branch = s != null ? s.getBranch() : "-";
                String section = s != null ? s.getSection() : "-";

                model.addRow(new Object[]{
                        ar.getStudentId(),
                        name,
                        branch,
                        section,
                        ar.getCourseCode(),
                        String.format("%.1f%%", ar.getAttendancePercentage())
                });
            }
        }

        JLabel infoLabel = new JLabel("Total Students Flagged for Shortage of Attendance (< 75%): " + alertCount);
        infoLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        infoLabel.setForeground(new Color(180, 40, 40));
        panel.add(infoLabel, BorderLayout.NORTH);

        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));

        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setForeground(new Color(200, 30, 30));
                c.setFont(c.getFont().deriveFont(Font.BOLD));
                return c;
            }
        });

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private static class StudentGroupStats {
        String branch;
        String section;
        int studentCount = 0;
        double totalMarks = 0.0;
        int marksCount = 0;
        double totalAttendance = 0.0;
        int attendanceCount = 0;

        StudentGroupStats(String branch, String section) {
            this.branch = branch;
            this.section = section;
        }
    }
}
