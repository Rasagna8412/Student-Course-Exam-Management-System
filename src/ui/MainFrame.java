package ui;

import models.AppData;
import models.Course;
import models.Student;
import modules.*;
import services.DataManager;
import services.RelativeGradingEngine;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame {

    private final RelativeGradingEngine gradingEngine;
    private final CourseModule courseModule;
    private final StudentModule studentModule;
    private final ExamModule examModule;
    private final GradeModule gradeModule;
    private final AttendanceModule attendanceModule;

    private final JLabel statusLabel;

    public MainFrame() {
        super("Java Student Course & Exam Management System");

        // Set Look and Feel to Nimbus if available for modern appearance
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {}

        // Initialize Services & Modules
        gradingEngine = new RelativeGradingEngine();
        courseModule = new CourseModule();
        studentModule = new StudentModule();
        examModule = new ExamModule();
        gradeModule = new GradeModule(gradingEngine);
        attendanceModule = new AttendanceModule();

        // Register Save Callbacks to auto-persist changes into data.dat
        Runnable saveAction = this::saveAllData;
        courseModule.setSaveCallback(saveAction);
        studentModule.setSaveCallback(saveAction);
        examModule.setSaveCallback(saveAction);
        gradeModule.setSaveCallback(saveAction);
        attendanceModule.setSaveCallback(saveAction);

        // Load persisted data from data.dat
        loadAllData();

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmExit();
            }
        });

        setSize(1150, 720);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Setup Menu Bar
        setJMenuBar(createMenuBar());

        // Setup Header & Quick Toolbar
        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.add(createHeaderPanel(), BorderLayout.NORTH);
        topContainer.add(createQuickToolBar(), BorderLayout.SOUTH);
        add(topContainer, BorderLayout.NORTH);

        // Setup Main Tabbed Pane with 5 required tabs
        JTabbedPane mainTabbedPane = new JTabbedPane(JTabbedPane.TOP);
        mainTabbedPane.setFont(new Font("SansSerif", Font.BOLD, 13));

        mainTabbedPane.addTab("Course Catalog", createModulePanel(
                courseModule.getTable(),
                courseModule::filter,
                e -> {
                    courseModule.addCourseDialog(this);
                    updateStatus();
                },
                e -> courseModule.editCourseDialog(this),
                e -> {
                    courseModule.deleteCourseDialog(this);
                    updateStatus();
                }
        ));

        mainTabbedPane.addTab("Student Enrollment", createStudentPanel());

        mainTabbedPane.addTab("Exam Scheduling", createModulePanel(
                examModule.getTable(),
                examModule::filter,
                e -> {
                    examModule.addExamDialog(this, getAvailableCourseCodes());
                    updateStatus();
                },
                e -> examModule.editExamDialog(this, getAvailableCourseCodes()),
                e -> {
                    examModule.deleteExamDialog(this);
                    updateStatus();
                }
        ));

        mainTabbedPane.addTab("Grade Tracking", createGradePanel());

        mainTabbedPane.addTab("Attendance Management", createModulePanel(
                attendanceModule.getTable(),
                attendanceModule::filter,
                e -> {
                    attendanceModule.addAttendanceDialog(this, getAvailableStudentIds(), getAvailableCourseCodes());
                    updateStatus();
                },
                e -> attendanceModule.editAttendanceDialog(this),
                e -> {
                    attendanceModule.deleteAttendanceDialog(this);
                    updateStatus();
                }
        ));

        add(mainTabbedPane, BorderLayout.CENTER);

        // Status Bar at Bottom
        statusLabel = new JLabel(" Ready");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(210, 210, 210)),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        statusPanel.add(statusLabel, BorderLayout.WEST);
        add(statusPanel, BorderLayout.SOUTH);

        updateStatus();
    }

    private void loadAllData() {
        AppData appData = DataManager.loadData();
        courseModule.loadRecords(appData.getCourses());
        studentModule.loadRecords(appData.getStudents());
        examModule.loadRecords(appData.getExams());
        gradeModule.loadRecords(appData.getGradeRecords());
        attendanceModule.loadRecords(appData.getAttendanceRecords());
    }

    private void saveAllData() {
        AppData appData = new AppData(
                courseModule.getCourses(),
                studentModule.getStudents(),
                examModule.getExams(),
                gradeModule.getGradeRecords(),
                attendanceModule.getAttendanceRecords()
        );
        DataManager.saveData(appData);
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setFont(new Font("SansSerif", Font.PLAIN, 13));

        // File Menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem recalcItem = new JMenuItem("Recalculate Relative Grades");
        recalcItem.addActionListener(e -> {
            gradeModule.recalculateAllGrades();
            saveAllData();
            JOptionPane.showMessageDialog(this, "Grades successfully recalculated using active percentile thresholds!", "Grading Engine", JOptionPane.INFORMATION_MESSAGE);
        });

        JMenuItem printGradeSheetItem = new JMenuItem("Print Student Grade Sheet");
        printGradeSheetItem.addActionListener(e -> openStudentGradeSheetDialog(null));

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> confirmExit());

        fileMenu.add(recalcItem);
        fileMenu.add(printGradeSheetItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        // Grading Menu
        JMenu gradingMenu = new JMenu("Grading");
        JMenuItem configGradingItem = new JMenuItem("Configure Relative Thresholds");
        configGradingItem.addActionListener(e -> openGradingConfigDialog());
        gradingMenu.add(configGradingItem);

        // Reports Menu
        JMenu reportsMenu = new JMenu("Reports");
        JMenuItem summaryReportItem = new JMenuItem("Summary Reports & Toppers");
        summaryReportItem.addActionListener(e -> openSummaryReports());

        JMenuItem individualSheetItem = new JMenuItem("Individual Student Grade Sheet");
        individualSheetItem.addActionListener(e -> openStudentGradeSheetDialog(null));

        reportsMenu.add(summaryReportItem);
        reportsMenu.add(individualSheetItem);

        // Help Menu
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About System");
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(gradingMenu);
        menuBar.add(reportsMenu);
        menuBar.add(helpMenu);

        return menuBar;
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(25, 40, 65));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("Student Course & Exam Management System");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("Integrated Academic Analytics, Class Topper Reports & Individual Student Grade Sheets");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(180, 200, 225));

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(subtitleLabel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createQuickToolBar() {
        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        toolbarPanel.setBackground(new Color(240, 243, 248));
        toolbarPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(210, 215, 225)));

        JButton gradeSheetBtn = createStyledButton("🖨️ Print Student Grade Sheet", new Color(180, 90, 30));
        gradeSheetBtn.addActionListener(e -> openStudentGradeSheetDialog(null));

        JButton reportsBtn = createStyledButton("Summary Reports & Toppers", new Color(40, 120, 180));
        reportsBtn.addActionListener(e -> openSummaryReports());

        JButton gradingConfigBtn = createStyledButton("Grading Thresholds", new Color(100, 70, 160));
        gradingConfigBtn.addActionListener(e -> openGradingConfigDialog());

        JButton recalcBtn = createStyledButton("Recalculate Relative Grades", new Color(40, 150, 90));
        recalcBtn.addActionListener(e -> {
            gradeModule.recalculateAllGrades();
            saveAllData();
            JOptionPane.showMessageDialog(this, "Relative grades recalculated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        });

        toolbarPanel.add(gradeSheetBtn);
        toolbarPanel.add(reportsBtn);
        toolbarPanel.add(gradingConfigBtn);
        toolbarPanel.add(recalcBtn);

        return toolbarPanel;
    }

    private interface FilterHandler {
        void filter(String text);
    }

    private JPanel createModulePanel(JTable table, FilterHandler filterHandler,
                                     java.awt.event.ActionListener addAction,
                                     java.awt.event.ActionListener editAction,
                                     java.awt.event.ActionListener deleteAction) {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Top Filter Bar
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JLabel searchLabel = new JLabel("Search & Filter:");
        searchLabel.setFont(new Font("SansSerif", Font.BOLD, 12));

        JTextField searchField = new JTextField(22);
        JButton clearBtn = new JButton("Clear");

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filterHandler.filter(searchField.getText()); }
            @Override
            public void removeUpdate(DocumentEvent e) { filterHandler.filter(searchField.getText()); }
            @Override
            public void changedUpdate(DocumentEvent e) { filterHandler.filter(searchField.getText()); }
        });

        clearBtn.addActionListener(e -> {
            searchField.setText("");
            filterHandler.filter("");
        });

        filterPanel.add(searchLabel);
        filterPanel.add(searchField);
        filterPanel.add(clearBtn);
        mainPanel.add(filterPanel, BorderLayout.NORTH);

        // Center Table JScrollPane
        JScrollPane scrollPane = new JScrollPane(table);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Bottom Action Bar
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));

        JButton addBtn = createStyledButton("+ Add Record", new Color(40, 140, 70));
        addBtn.addActionListener(addAction);

        JButton editBtn = createStyledButton("Edit Selected", new Color(40, 100, 180));
        editBtn.addActionListener(editAction);

        JButton deleteBtn = createStyledButton("Delete Selected", new Color(190, 50, 50));
        deleteBtn.addActionListener(deleteAction);

        btnPanel.add(addBtn);
        btnPanel.add(editBtn);
        btnPanel.add(deleteBtn);

        mainPanel.add(btnPanel, BorderLayout.SOUTH);

        return mainPanel;
    }

    private JPanel createStudentPanel() {
        JPanel panel = createModulePanel(
                studentModule.getTable(),
                studentModule::filter,
                e -> {
                    studentModule.addStudentDialog(this, getAvailableCourseCodes());
                    updateStatus();
                },
                e -> studentModule.editStudentDialog(this, getAvailableCourseCodes()),
                e -> {
                    studentModule.deleteStudentDialog(this);
                    updateStatus();
                }
        );

        Component southComp = ((BorderLayout) panel.getLayout()).getLayoutComponent(BorderLayout.SOUTH);
        if (southComp instanceof JPanel) {
            JPanel btnPanel = (JPanel) southComp;
            JButton sheetBtn = createStyledButton("🖨️ View Grade Sheet", new Color(180, 90, 30));
            sheetBtn.addActionListener(e -> {
                int row = studentModule.getTable().getSelectedRow();
                String targetId = null;
                if (row >= 0) {
                    int modelRow = studentModule.getTable().convertRowIndexToModel(row);
                    targetId = studentModule.getStudents().get(modelRow).getStudentId();
                }
                openStudentGradeSheetDialog(targetId);
            });
            btnPanel.add(sheetBtn);
        }

        return panel;
    }

    private JPanel createGradePanel() {
        JPanel panel = createModulePanel(
                gradeModule.getTable(),
                gradeModule::filter,
                e -> {
                    gradeModule.addGradeDialog(this, getAvailableStudentIds(), getAvailableCourseCodes());
                    updateStatus();
                },
                e -> gradeModule.editGradeDialog(this),
                e -> {
                    gradeModule.deleteGradeDialog(this);
                    updateStatus();
                }
        );

        // Add extra buttons to bottom panel specifically for Grade Module
        Component southComp = ((BorderLayout) panel.getLayout()).getLayoutComponent(BorderLayout.SOUTH);
        if (southComp instanceof JPanel) {
            JPanel btnPanel = (JPanel) southComp;

            JButton sheetBtn = createStyledButton("🖨️ Print Grade Sheet", new Color(180, 90, 30));
            sheetBtn.addActionListener(e -> {
                int row = gradeModule.getTable().getSelectedRow();
                String targetId = null;
                if (row >= 0) {
                    int modelRow = gradeModule.getTable().convertRowIndexToModel(row);
                    targetId = gradeModule.getGradeRecords().get(modelRow).getStudentId();
                }
                openStudentGradeSheetDialog(targetId);
            });

            JButton recalcBtn = createStyledButton("Recalculate Grades", new Color(100, 70, 160));
            recalcBtn.addActionListener(e -> {
                gradeModule.recalculateAllGrades();
                saveAllData();
                JOptionPane.showMessageDialog(this, "Relative grades recalculated successfully!", "Relative Grading Engine", JOptionPane.INFORMATION_MESSAGE);
            });

            btnPanel.add(sheetBtn);
            btnPanel.add(recalcBtn);
        }

        return panel;
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private List<String> getAvailableCourseCodes() {
        List<String> codes = new ArrayList<>();
        for (Course c : courseModule.getCourses()) {
            codes.add(c.getCourseCode());
        }
        return codes;
    }

    private List<String> getAvailableStudentIds() {
        List<String> ids = new ArrayList<>();
        for (Student s : studentModule.getStudents()) {
            ids.add(s.getStudentId());
        }
        return ids;
    }

    private void openGradingConfigDialog() {
        GradingConfigDialog dialog = new GradingConfigDialog(this, gradingEngine);
        dialog.setVisible(true);
        if (dialog.isUpdated()) {
            gradeModule.recalculateAllGrades();
            saveAllData();
            JOptionPane.showMessageDialog(this, "Grades successfully recalculated with new percentile thresholds!", "Grading Engine", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void openSummaryReports() {
        SummaryReportDialog dialog = new SummaryReportDialog(this, studentModule, gradeModule, attendanceModule);
        dialog.setVisible(true);
    }

    private void openStudentGradeSheetDialog(String defaultStudentId) {
        StudentGradeSheetDialog dialog = new StudentGradeSheetDialog(this, studentModule, courseModule, gradeModule, attendanceModule, defaultStudentId);
        dialog.setVisible(true);
    }

    private void showAboutDialog() {
        String msg = "<html><body style='width: 340px; padding: 10px; font-family: sans-serif;'>"
                + "<h2 style='color: #1A365D; margin-bottom: 5px;'>Java Student Management System</h2>"
                + "<b>Version:</b> 1.0 (Production Release)<br>"
                + "<b>Architecture:</b> Java Swing (Pure OOP Architecture)<br>"
                + "<b>Grading Algorithm:</b> Dynamic Relative Grading Engine<br><br>"
                + "<b>Features:</b>"
                + "<ul style='margin-left: 15px; padding-left: 0;'>"
                + "<li>5 Core Management Modules (Course, Student, Exam, Grade, Attendance)</li>"
                + "<li>Branch & Section Grouped Analytics with Class Toppers & Class Average Marks</li>"
                + "<li>Individual Student Grade Sheet Printing & PDF Export</li>"
                + "<li>Custom Graphics2D Visual Grade Distribution Charts</li>"
                + "<li>Low Attendance Automatic Alerts (&lt; 75%)</li>"
                + "</ul>"
                + "Designed for high reliability and clean execution on Java 8+.</body></html>";

        JOptionPane.showMessageDialog(this, msg, "About System", JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateStatus() {
        int courses = courseModule.getCourses().size();
        int students = studentModule.getStudents().size();
        int exams = examModule.getExams().size();
        int grades = gradeModule.getGradeRecords().size();
        int att = attendanceModule.getAttendanceRecords().size();

        statusLabel.setText(String.format(" Status: Ready | Courses: %d | Students: %d | Exams: %d | Grade Records: %d | Attendance Records: %d",
                courses, students, exams, grades, att));
    }

    private void confirmExit() {
        saveAllData();
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to exit the application?",
                "Confirm Exit", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }
}
