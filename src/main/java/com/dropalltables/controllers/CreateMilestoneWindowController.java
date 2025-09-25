package com.dropalltables.controllers;

import com.dropalltables.util.*;
import com.dropalltables.data.DaoException;
import com.dropalltables.data.DaoMilestone;

import java.time.LocalDate;

import com.dropalltables.models.Milestone;
import com.dropalltables.models.Project;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class CreateMilestoneWindowController {

    @FXML
    private TextField textFieldMilestoneNo;
    @FXML
    private TextField textFieldMilestoneName;
    @FXML
    private DatePicker datePickerMilestoneDate;
    @FXML
    private Label labelHeader;
    @FXML
    private Label labelProjectInfo;
    @FXML
    private Button buttonOk;
    @FXML
    private Button buttonCancel;

    private Milestone milestone = null; // holds the created milestone
    private Project project; // the project this milestone belongs to

    public void initialize() {
        // Set default date to today
        datePickerMilestoneDate.setValue(LocalDate.now());
    }

    /** Set the project for which the milestone will be created */
    public void setProject(Project project) {
        this.project = project;
        if (project != null) {
            labelProjectInfo.setText("For project: " + project.getName() + " (#" + project.getProjectNo() + ")");
            
            // Set appropriate default date based on project start date and minimum constraint
            LocalDate minimumDate = LocalDate.of(2022, 1, 1);
            LocalDate defaultDate = LocalDate.now();
            
            // Consider project start date
            if (project.getStartDate() != null) {
                LocalDate projectStart = project.getStartDate();
                // Use the latest of: project start date, minimum date (2022-01-01), or today
                if (projectStart.isAfter(defaultDate)) {
                    defaultDate = projectStart;
                }
                if (projectStart.isAfter(minimumDate)) {
                    minimumDate = projectStart;
                }
            }
            
            // Consider project end date
            if (project.getEndDate() != null) {
                LocalDate projectEnd = project.getEndDate();
                // If default date is after project end, use project end date
                if (defaultDate.isAfter(projectEnd)) {
                    defaultDate = projectEnd;
                }
            }
            
            // Ensure default date is not before minimum
            if (defaultDate.isBefore(minimumDate)) {
                defaultDate = minimumDate;
            }
            
            datePickerMilestoneDate.setValue(defaultDate);
        }
    }

    /** OK button: create milestone */
    @FXML
    private void handleSaveAction() {
        // Reset milestone to null at the start of each save attempt
        milestone = null;
        
        String milestoneNoText = textFieldMilestoneNo.getText();
        String name = textFieldMilestoneName.getText();
        LocalDate date = datePickerMilestoneDate.getValue();

        // Validate Milestone Number
        int milestoneNo;
        try {
            milestoneNo = Integer.parseInt(milestoneNoText.trim());
            if (milestoneNo <= 0) {
                AlertUtil.showError("Invalid input", "Milestone number must be a positive number.");
                return; // keep window open
            }
        } catch (NumberFormatException e) {
            AlertUtil.showError("Invalid input", "Milestone number must be a valid positive number.");
            return; // keep window open
        }

        // Validate Name
        if (name == null || name.trim().isEmpty()) {
            AlertUtil.showError("Missing data", "Milestone name cannot be empty.");
            return; // keep window open
        }

        // Validate Date
        if (date == null) {
            AlertUtil.showError("Missing data", "Milestone date cannot be empty.");
            return; // keep window open
        }

        // Validate milestone date constraints
        LocalDate minimumDate = LocalDate.of(2022, 1, 1);
        if (date.isBefore(minimumDate)) {
            AlertUtil.showError("Invalid Date", "Milestone date cannot be before " + minimumDate + ".");
            return; // keep window open
        }

        // Only validate against project start date if the project has a start date
        if (project != null && project.getStartDate() != null && date.isBefore(project.getStartDate())) {
            AlertUtil.showError("Invalid Date", 
                "Milestone date cannot be before the project start date (" + project.getStartDate() + ").");
            return; // keep window open
        }

        // Only validate against project end date if the project has an end date
        if (project != null && project.getEndDate() != null && date.isAfter(project.getEndDate())) {
            AlertUtil.showError("Invalid Date", 
                "Milestone date cannot be after the project end date (" + project.getEndDate() + ").");
            return; // keep window open
        }

        // Check if milestone number already exists
        try {
            DaoMilestone dao = new DaoMilestone();
            if (dao.milestoneNoExists(milestoneNo)) {
                AlertUtil.showError("Duplicate Milestone Number", 
                    "Milestone number " + milestoneNo + " already exists. Please choose a different number.");
                return; // keep window open
            }
        } catch (DaoException e) {
            AlertUtil.showError("Error", "Failed to check milestone number: " + e.getMessage());
            return; // keep window open
        }

        // Create milestone
        milestone = new Milestone(milestoneNo, name.trim(), date, project);
        closeWindow();
    }

    @FXML
    private void handleCancelAction() {
        milestone = null; // discard
        closeWindow();
    }

    public Milestone getCreatedMilestone() {
        return milestone;
    }

    private void closeWindow() {
        Stage stage = (Stage) buttonOk.getScene().getWindow();
        stage.close();
    }
}