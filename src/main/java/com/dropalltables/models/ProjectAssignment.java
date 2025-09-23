package com.dropalltables.models;

public class ProjectAssignment {
    private int consultantID;
    private int projectID;
    private int hoursWorked;

    // Extra details for UI display
    private int consultantNo;
    private String consultantName;
    private String title;

    public ProjectAssignment(int consultantID, int projectID) {
        this.consultantID = consultantID;
        this.projectID = projectID;
        hoursWorked = 0;
    }

    public ProjectAssignment(int consultantID, int projectID, int hoursWorked) {
        this.consultantID = consultantID;
        this.projectID = projectID;
        this.hoursWorked = hoursWorked;
    }

    // Constructor with extra details
    public ProjectAssignment(int projectID, int consultantID, int hoursWorked,
            int consultantNo, String consultantName, String title) {
        this.projectID = projectID;
        this.consultantID = consultantID;
        this.hoursWorked = hoursWorked;
        this.consultantNo = consultantNo;
        this.consultantName = consultantName;
        this.title = title;
    }

    public int getConsultantID() {
        return consultantID;
    }

    public void setcConsultantID(int consultantID) {
        this.consultantID = consultantID;
    }

    public int getProjectID() {
        return projectID;
    }

    public void setProjectID(int projectID) {
        this.projectID = projectID;
    }

    public int getHoursWorked() {
        return hoursWorked;
    }

    public void setHoursWorked(int hoursWorked) {
        this.hoursWorked = hoursWorked;
    }

    public void incrementHoursWorked(int hours) {
        hoursWorked += hours;
    }

    public int getConsultantNo() {
        return consultantNo;
    }

    public String getConsultantName() {
        return consultantName;
    }

    public String getTitle() {
        return title;
    }
}
