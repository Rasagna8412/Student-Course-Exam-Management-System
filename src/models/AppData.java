package models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AppData implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<Course> courses;
    private List<Student> students;
    private List<Exam> exams;
    private List<GradeRecord> gradeRecords;
    private List<AttendanceRecord> attendanceRecords;

    public AppData() {
        this.courses = new ArrayList<>();
        this.students = new ArrayList<>();
        this.exams = new ArrayList<>();
        this.gradeRecords = new ArrayList<>();
        this.attendanceRecords = new ArrayList<>();
    }

    public AppData(List<Course> courses, List<Student> students, List<Exam> exams,
                   List<GradeRecord> gradeRecords, List<AttendanceRecord> attendanceRecords) {
        this.courses = courses != null ? courses : new ArrayList<>();
        this.students = students != null ? students : new ArrayList<>();
        this.exams = exams != null ? exams : new ArrayList<>();
        this.gradeRecords = gradeRecords != null ? gradeRecords : new ArrayList<>();
        this.attendanceRecords = attendanceRecords != null ? attendanceRecords : new ArrayList<>();
    }

    public List<Course> getCourses() { return courses; }
    public void setCourses(List<Course> courses) { this.courses = courses; }

    public List<Student> getStudents() { return students; }
    public void setStudents(List<Student> students) { this.students = students; }

    public List<Exam> getExams() { return exams; }
    public void setExams(List<Exam> exams) { this.exams = exams; }

    public List<GradeRecord> getGradeRecords() { return gradeRecords; }
    public void setGradeRecords(List<GradeRecord> gradeRecords) { this.gradeRecords = gradeRecords; }

    public List<AttendanceRecord> getAttendanceRecords() { return attendanceRecords; }
    public void setAttendanceRecords(List<AttendanceRecord> attendanceRecords) { this.attendanceRecords = attendanceRecords; }
}
