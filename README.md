# 🎓 Student Management System

A desktop application built in **Java Swing** for managing courses, student enrollments, exam schedules, relative grading, attendance tracking, executive analytics, and individual transcript printing.

---

## 🌟 Key Features

### 📚 Core Academic Modules
- **Course Catalog Management**: Create, edit, search, and delete academic courses with course codes, titles, and credit values.
- **Student Enrollment**: Manage student profiles with details like Student ID, Name, Academic Branch, Section, and Primary Enrolled Course.
- **Exam Scheduling**: Schedule and organize course examinations with exam IDs and dates.
- **Grade Tracking**: Record student marks per course and calculate letter grades automatically.
- **Attendance Management**: Track attended vs. conducted classes with automatic percentage calculations and low-attendance warnings (< 75%).

### 📊 Relative Grading Engine
- **Percentile-Based Grading**: Evaluates student performance relative to peers in the same course.
- **Default Grade Distribution**:
  - `A+`: Top 10%
  - `A`: Next 20% (Cumulative 30%)
  - `B`: Next 30% (Cumulative 60%)
  - `C`: Next 25% (Cumulative 85%)
  - `F`: Remaining 15%
- **Custom Threshold Configuration**: Interactive dialog to adjust percentile allocations on the fly.
- **Tie Handling**: Automatically assigns identical grades to students with identical marks.

### 📈 Executive Analytics & Topper Reports
- **Branch & Section Filtering**: Filter academic statistics by specific branches and sections.
- **Performance Summary Cards**: View Total Students, Class Average Marks, and Class Topper details instantly.
- **Visual Distribution Charts**: Built-in `Graphics2D` chart panel rendering real-time grade distributions (`A+` to `F`).

### 🖨️ Transcripts & Grade Sheets
- **Individual Student Grade Sheet**: Display comprehensive academic transcripts for any student, including course-wise marks, relative grades, and attendance rates.
- **Print & Export to PDF**: Native Swing printing support for generating hard copies or saving grade sheets as PDF documents.

### 💾 Persistence & Data Management
- **Automatic Serialization**: Loads data on startup and auto-saves changes to `data.dat` whenever records are added, edited, or deleted.
- **Fail-Safe Startup**: Initializes empty data structures gracefully if no previous save file exists.

---

## 🏗️ Project Architecture

```
student-management-system/
├── bin/                       # Compiled bytecode (.class files)
├── data.dat                   # Serialized object data file (Auto-generated)
├── src/
│   ├── Main.java              # Application entry point
│   ├── models/                # Serializable Data Entities
│   │   ├── AppData.java
│   │   ├── Student.java
│   │   ├── Course.java
│   │   ├── Exam.java
│   │   ├── GradeRecord.java
│   │   └── AttendanceRecord.java
│   ├── services/              # Business Logic & Persistence
│   │   ├── DataManager.java
│   │   └── RelativeGradingEngine.java
│   ├── modules/               # UI Table Modules & Operations
│   │   ├── CourseModule.java
│   │   ├── StudentModule.java
│   │   ├── ExamModule.java
│   │   ├── GradeModule.java
│   │   └── AttendanceModule.java
│   └── ui/                    # Swing Dialogs & Main Interface
│       ├── MainFrame.java
│       ├── GradingConfigDialog.java
│       ├── SummaryReportDialog.java
│       ├── StudentGradeSheetDialog.java
│       └── GradeDistributionChartPanel.java
└── README.md
```

---

## 🚀 Getting Started

### Prerequisites
- **Java Development Kit (JDK)**: Version 8 or higher.

### Compilation

Open your terminal or command prompt in the root directory of the project and run:

```bash
javac -d bin src/*.java src/*/*.java
```

### Running the Application

Execute the application using:

```bash
java -cp bin Main
```

---

## 💻 User Interface Overview

1. **Main Workspace**: Tabbed layout providing access to *Course Catalog*, *Student Enrollment*, *Exam Scheduling*, *Grade Tracking*, and *Attendance Management*.
2. **Search & Filter Bar**: Located above each table for real-time string filtering.
3. **Quick Toolbar**: Direct buttons to print grade sheets, view summary reports, adjust grading thresholds, and recalculate relative grades.
4. **Status Bar**: Displays real-time record counts for all entities.

---

## 🛠️ Technology Stack

- **Language**: Java 8+
- **GUI Framework**: Java Swing with `Nimbus` Look & Feel
- **Graphics**: Custom `Graphics2D` component for charts
- **Persistence**: Java Binary Object Serialization (`java.io.Serializable`)

---

## 📄 License

This project is provided as an open open-source educational codebase. Feel free to modify and extend it for academic or personal administrative use.
