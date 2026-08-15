package models;

public class Exam {
    private String examId;
    private String courseCode;
    private String examDate;

    public Exam(String examId, String courseCode, String examDate) {
        this.examId = examId;
        this.courseCode = courseCode;
        this.examDate = examDate;
    }

    public String getExamId() { return examId; }
    public void setExamId(String examId) { this.examId = examId; }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public String getExamDate() { return examDate; }
    public void setExamDate(String examDate) { this.examDate = examDate; }

    public Object[] toObjectArray() {
        return new Object[]{examId, courseCode, examDate};
    }
}
