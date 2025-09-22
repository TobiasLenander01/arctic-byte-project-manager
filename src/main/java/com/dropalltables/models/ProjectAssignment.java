package com.dropalltables.models;

public class ProjectAssignment {
    private int consultantID;
    private int projectID;
    private int hoursWorked;

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
}
