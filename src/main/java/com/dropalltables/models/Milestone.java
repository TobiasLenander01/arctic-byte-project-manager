package com.dropalltables.models;

import java.time.LocalDate;

public class Milestone {
    
    private int milestoneId;
    private String name;
    private LocalDate date;
    private Project project;
    

    public Milestone(int milestoneId, String name, LocalDate date, Project project) {
        this.milestoneId = milestoneId;
        this.name = name;
        this.date = date;
        this.project = project;
    }
    public Milestone() {
    }

    public int getMilestoneId() {
        return milestoneId;
    }

    public void setMilestoneId(int milestoneId) {
        this.milestoneId = milestoneId;
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
    public int getProjectId() {
        return project.getProjectNo();
    }
}

