package com.dropalltables.models;

import java.time.LocalDate;

public class Project {
    private int projectNo;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;

    public Project(int projectNo, String name, LocalDate startDate, LocalDate endDate) {
        this.projectNo = projectNo;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Project(int projectNo, String name, LocalDate startDate) {
        this.projectNo = projectNo;
        this.name = name;
        this.startDate = startDate;
        this.endDate = null;
    }

    public int getProjectNo() {
        return projectNo;
    }

    public void setProjectNo(int projectNo) {
        this.projectNo = projectNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    @Override
    public String toString() {
        return "Project{" +
                "projectNo=" + projectNo +
                ", name='" + name + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                '}';
    }
}
