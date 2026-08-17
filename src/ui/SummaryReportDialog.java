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

    private final StudentModule studentModule;
    private final GradeModule gradeModule;
    private final AttendanceModule attendanceModule;

    private final JComboBox<String> branchFilterCombo;
    private final JComboBox<String> sectionFilterCombo;

    private final JLabel topperCardLabel;
    private final JLabel classAvgCardLabel;
    private final JLabel totalStudentsCardLabel;

    private final DefaultTableModel summaryTableModel;

    public SummaryReportDialog(Frame owner, StudentModule studentModule, GradeModule gradeModule, AttendanceModule attendanceModule) {
        super(owner, "Executive Summary & Performance Analytics", true);
        this.studentModule = studentModule;
        this.gradeModule = gradeModule;
        this.attendanceModule = attendanceModule;

        setSize(900, 680);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(25, 40, 65));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("Academic Analytics, Class Toppers & Group Performance");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("Filter analytics by Branch and Section with dynamic Class Topper & Average Marks calculation");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(200, 210, 225));

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        JTabbedPane reportTabs = new JTabbedPane();
        reportTabs.setFont(new Font("SansSerif", Font.BOLD, 12));

        // Filter Bar & Cards Panel for Tab 1
        JPanel branchSecTabPanel = new JPanel(new BorderLayout(10, 10));
        branchSecTabPanel.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 8));
        filterBar.setBackground(new Color(240, 243, 248));
        filterBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(210, 215, 225)));

        filterBar.add(new JLabel("Filter Branch:"));
        branchFilterCombo = new JComboBox<>(new String[]{"ALL", "CSE", "ECE", "IT", "ME", "EE", "CIVIL"});
        filterBar.add(branchFilterCombo);

        filterBar.add(new JLabel("Filter Section:"));
        sectionFilterCombo = new JComboBox<>(new String[]{"ALL", "A", "B", "C"});
        filterBar.add(sectionFilterCombo);

        branchFilterCombo.addActionListener(e -> refreshBranchSectionAnalytics());
        sectionFilterCombo.addActionListener(e -> refreshBranchSectionAnalytics());

        branchSecTabPanel.add(filterBar, BorderLayout.NORTH);

        // Cards Panel (Topper, Class Average, Total Students)
        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 12, 0));
        cardsPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        topperCardLabel = createCardLabel("🏆 Class Topper", "N/A", new Color(40, 120, 70));
        classAvgCardLabel = createCardLabel("📊 Class Average Marks", "N/A", new Color(40, 100, 180));
        totalStudentsCardLabel = createCardLabel("👥 Total Enrolled", "0", new Color(100, 70, 160));

        cardsPanel.add(topperCardLabel);
        cardsPanel.add(classAvgCardLabel);
        cardsPanel.add(totalStudentsCardLabel);

        // Table
        String[] cols = {"Branch", "Section", "Students Enrolled", "Avg Marks", "Avg Attendance %", "Section Topper"};
        summaryTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(summaryTableModel);
        table.setRowHeight(28);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));

        JPanel tableAndCardsContainer = new JPanel(new BorderLayout(10, 10));
        tableAndCardsContainer.add(cardsPanel, BorderLayout.NORTH);
        tableAndCardsContainer.add(new JScrollPane(table), BorderLayout.CENTER);

        branchSecTabPanel.add(tableAndCardsContainer, BorderLayout.CENTER);

        reportTabs.addTab("Branch & Section Analytics", branchSecTabPanel);

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

        refreshBranchSectionAnalytics();
    }

    private JLabel createCardLabel(String title, String val, Color headerBg) {
        JLabel label = new JLabel();
        label.setOpaque(true);
        label.setBackground(new Color(248, 250, 252));
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(headerBg, 2),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        label.setText("<html><div style='text-align: center;'><b style='color: " + toHex(headerBg) + "; font-size: 11px;'>" + title + "</b><br><span style='font-size: 13px; font-weight: bold; color: #222;'>" + val + "</span></div></html>");
        return label;
    }

    private String toHex(Color c) {
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }

    private void refreshBranchSectionAnalytics() {
        summaryTableModel.setRowCount(0);

        String selBranch = (String) branchFilterCombo.getSelectedItem();
        String selSection = (String) sectionFilterCombo.getSelectedItem();

        // 1. Group students by "Branch - Section"
        Map<String, StudentGroupStats> statsMap = new TreeMap<>();
        Map<String, Student> studentMap = new HashMap<>();

        // Map studentId -> list of marks
        Map<String, List<Double>> studentMarksMap = new HashMap<>();
        // Map studentId -> list of attendance pct
        Map<String, List<Double>> studentAttMap = new HashMap<>();

        int matchingStudentsCount = 0;

        for (Student s : studentModule.getStudents()) {
            boolean branchMatch = "ALL".equalsIgnoreCase(selBranch) || s.getBranch().equalsIgnoreCase(selBranch);
            boolean secMatch = "ALL".equalsIgnoreCase(selSection) || s.getSection().equalsIgnoreCase(selSection);

            if (branchMatch && secMatch) {
                studentMap.put(s.getStudentId(), s);
                String groupKey = s.getBranch() + " - Sec " + s.getSection();
                statsMap.putIfAbsent(groupKey, new StudentGroupStats(s.getBranch(), s.getSection()));
                statsMap.get(groupKey).studentList.add(s);
                matchingStudentsCount++;
            }
        }

        // Aggregate marks
        for (GradeRecord gr : gradeModule.getGradeRecords()) {
            Student s = studentMap.get(gr.getStudentId());
            if (s != null) {
                studentMarksMap.computeIfAbsent(s.getStudentId(), k -> new ArrayList<>()).add(gr.getMarks());
            }
        }

        // Aggregate attendance
        for (AttendanceRecord ar : attendanceModule.getAttendanceRecords()) {
            Student s = studentMap.get(ar.getStudentId());
            if (s != null) {
                studentAttMap.computeIfAbsent(s.getStudentId(), k -> new ArrayList<>()).add(ar.getAttendancePercentage());
            }
        }

        Student overallTopper = null;
        double overallTopperAvg = -1.0;
        double overallTotalMarksSum = 0.0;
        int overallMarksCountSum = 0;

        // Process stats per group
        for (StudentGroupStats stats : statsMap.values()) {
            Student groupTopper = null;
            double groupTopperAvg = -1.0;

            for (Student s : stats.studentList) {
                List<Double> mList = studentMarksMap.get(s.getStudentId());
                if (mList != null && !mList.isEmpty()) {
                    double sSum = 0;
                    for (double m : mList) sSum += m;
                    double sAvg = sSum / mList.size();

                    stats.totalMarks += sSum;
                    stats.marksCount += mList.size();

                    overallTotalMarksSum += sSum;
                    overallMarksCountSum += mList.size();

                    if (sAvg > groupTopperAvg) {
                        groupTopperAvg = sAvg;
                        groupTopper = s;
                    }

                    if (sAvg > overallTopperAvg) {
                        overallTopperAvg = sAvg;
                        overallTopper = s;
                    }
                }

                List<Double> aList = studentAttMap.get(s.getStudentId());
                if (aList != null && !aList.isEmpty()) {
                    for (double a : aList) {
                        stats.totalAttendance += a;
                        stats.attendanceCount++;
                    }
                }
            }

            double groupAvgMarks = stats.marksCount > 0 ? (stats.totalMarks / stats.marksCount) : 0.0;
            double groupAvgAtt = stats.attendanceCount > 0 ? (stats.totalAttendance / stats.attendanceCount) : 0.0;
            String groupTopperStr = groupTopper != null ? String.format("%s (%.1f)", groupTopper.getName(), groupTopperAvg) : "N/A";

            summaryTableModel.addRow(new Object[]{
                    stats.branch,
                    stats.section,
                    stats.studentList.size(),
                    String.format("%.2f", groupAvgMarks),
                    String.format("%.1f%%", groupAvgAtt),
                    groupTopperStr
            });
        }

        // Update Card Labels
        if (overallTopper != null) {
            topperCardLabel.setText(String.format("<html><div style='text-align: center;'><b style='color: #287846; font-size: 11px;'>🏆 Class Topper</b><br><span style='font-size: 13px; font-weight: bold; color: #222;'>%s (%s)<br><span style='color: #1b5e20; font-size: 12px;'>Avg Marks: %.2f</span></span></div></html>",
                    overallTopper.getName(), overallTopper.getStudentId(), overallTopperAvg));
        } else {
            topperCardLabel.setText("<html><div style='text-align: center;'><b style='color: #287846; font-size: 11px;'>🏆 Class Topper</b><br><span style='font-size: 13px; font-weight: bold; color: #777;'>No Grades Recorded</span></div></html>");
        }

        if (overallMarksCountSum > 0) {
            double overallAvgMarks = overallTotalMarksSum / overallMarksCountSum;
            classAvgCardLabel.setText(String.format("<html><div style='text-align: center;'><b style='color: #2864b4; font-size: 11px;'>📊 Class Average Marks</b><br><span style='font-size: 16px; font-weight: bold; color: #1a365d;'>%.2f / 100</span></div></html>", overallAvgMarks));
        } else {
            classAvgCardLabel.setText("<html><div style='text-align: center;'><b style='color: #2864b4; font-size: 11px;'>📊 Class Average Marks</b><br><span style='font-size: 13px; font-weight: bold; color: #777;'>No Grades Recorded</span></div></html>");
        }

        totalStudentsCardLabel.setText(String.format("<html><div style='text-align: center;'><b style='color: #6446a0; font-size: 11px;'>👥 Total Enrolled</b><br><span style='font-size: 16px; font-weight: bold; color: #4a148c;'>%d Students</span></div></html>", matchingStudentsCount));
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
        List<Student> studentList = new ArrayList<>();
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
