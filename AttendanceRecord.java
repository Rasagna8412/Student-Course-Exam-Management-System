package models;

public class AttendanceRecord {
    private String studentId;
    private String courseCode;
    private double attendancePercentage;

    public AttendanceRecord(String studentId, String courseCode, double attendancePercentage) {
        this.studentId = studentId;
        this.courseCode = courseCode;
        this.attendancePercentage = attendancePercentage;
    }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public double getAttendancePercentage() { return attendancePercentage; }
    public void setAttendancePercentage(double attendancePercentage) { this.attendancePercentage = attendancePercentage; }

    public Object[] toObjectArray() {
        return new Object[]{studentId, courseCode, String.format("%.1f%%", attendancePercentage)};
    }
}
