package com.dropalltables.models;

import java.time.LocalDate;

public class Milestone {
    
    private int milestoneNo;  // Natural key - user-visible identifier
    private String name;
    private LocalDate date;
    private Project project;
    

    public Milestone(int milestoneNo, String name, LocalDate date, Project project) {
        this.milestoneNo = milestoneNo;
        this.name = name;
        this.date = date;
        this.project = project;
    }
    public Milestone() {
    }

    public int getMilestoneNo() {
        return milestoneNo;
    }

    public void setMilestoneNo(int milestoneNo) {
        this.milestoneNo = milestoneNo;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }   
    public int getProjectNo() {
        return project.getProjectNo();
    }

    @Override
    public String toString() {
        return "Milestone{" +
                "milestoneNo=" + milestoneNo +
                ", name='" + name + '\'' +
                ", date=" + date +
                ", project=" + (project != null ? project.getName() : "null") +
                '}';
    }
}

