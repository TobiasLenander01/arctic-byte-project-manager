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

    public int getconsultantID() {
        return consultantID;
    }

    public void setconsultantID(int consultantID) {
        this.consultantID = consultantID;
    }

    public int getprojectID() {
        return projectID;
    }

    public void setprojectID(int projectID) {
        this.projectID = projectID;
    }

    public int getHoursWorked() {
        return hoursWorked;
    }

    public void setHoursWorked(int hoursWorked) {
        this.hoursWorked = hoursWorked;
    }

}
