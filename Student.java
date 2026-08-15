package models;

public class Student {
    private String studentId;
    private String name;
    private String branch;
    private String section;
    private String courseEnrolled;

    public Student(String studentId, String name, String branch, String section, String courseEnrolled) {
        this.studentId = studentId;
        this.name = name;
        this.branch = branch;
        this.section = section;
        this.courseEnrolled = courseEnrolled;
    }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public String getCourseEnrolled() { return courseEnrolled; }
    public void setCourseEnrolled(String courseEnrolled) { this.courseEnrolled = courseEnrolled; }

    public Object[] toObjectArray() {
        return new Object[]{studentId, name, branch, section, courseEnrolled};
    }
}
