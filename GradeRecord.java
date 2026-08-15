package models;

public class GradeRecord {
    private String studentId;
    private String courseCode;
    private double marks;
    private String grade;

    public GradeRecord(String studentId, String courseCode, double marks, String grade) {
        this.studentId = studentId;
        this.courseCode = courseCode;
        this.marks = marks;
        this.grade = grade;
    }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public double getMarks() { return marks; }
    public void setMarks(double marks) { this.marks = marks; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    public Object[] toObjectArray() {
        return new Object[]{studentId, courseCode, marks, grade};
    }
}
