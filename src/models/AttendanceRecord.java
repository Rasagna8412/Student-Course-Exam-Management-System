package models;

import java.io.Serializable;

public class AttendanceRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    private String studentId;
    private String courseCode;
    private int classesPresent;
    private int classesConducted;

    public AttendanceRecord(String studentId, String courseCode, int classesPresent, int classesConducted) {
        this.studentId = studentId;
        this.courseCode = courseCode;
        this.classesPresent = classesPresent;
        this.classesConducted = classesConducted;
    }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public int getClassesPresent() { return classesPresent; }
    public void setClassesPresent(int classesPresent) { this.classesPresent = classesPresent; }

    public int getClassesConducted() { return classesConducted; }
    public void setClassesConducted(int classesConducted) { this.classesConducted = classesConducted; }

    public double getAttendancePercentage() {
        if (classesConducted <= 0) return 0.0;
        return ((double) classesPresent / classesConducted) * 100.0;
    }

    public Object[] toObjectArray() {
        return new Object[]{studentId, courseCode, classesPresent, classesConducted, String.format("%.1f%%", getAttendancePercentage())};
    }
}
