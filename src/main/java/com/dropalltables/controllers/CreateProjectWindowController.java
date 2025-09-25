package com.dropalltables.controllers;

import com.dropalltables.util.*;

import java.time.LocalDate;

import com.dropalltables.models.Project;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class CreateProjectWindowController {

    @FXML
    private TextField textFieldProjectNo;
    @FXML
    private TextField textFieldProjectName;
    @FXML
    private DatePicker datePickerStartDate;
    @FXML
    private DatePicker datePickerEndDate;
    @FXML
    private Label labelHeader;
    @FXML
    private Button buttonOk;
    @FXML
    private Button buttonCancel;

    private Project project; // holds either a new or existing project

    /** Pre-fills form when editing */
    public void setProjectForEdit(Project project) {
        if (project != null) {
            this.project = project;
            textFieldProjectNo.setText(String.valueOf(project.getProjectNo()));
            textFieldProjectNo.setDisable(true); // No editing of project number
            textFieldProjectName.setText(project.getName());
            datePickerStartDate.setValue(project.getStartDate());
            datePickerEndDate.setValue(project.getEndDate());

            // Update UI texts for edit mode
            labelHeader.setText("Edit Project");
            buttonOk.setText("Save");
        }
    }

    /** OK button: create or update */
    @FXML
    private void handleOkAction() {
        String name = textFieldProjectName.getText();
        LocalDate startDate = datePickerStartDate.getValue();
        LocalDate endDate = datePickerEndDate.getValue();

        // Parse ProjectNo
        int projectNo;
        try {
            projectNo = Integer.parseInt(textFieldProjectNo.getText());
        } catch (NumberFormatException e) {
            AlertUtil.showError("Invalid input", "Project number must be a valid integer.");
            return; // keep window open
        }

        // Validate Start Date
        if (startDate == null) {
            AlertUtil.showError("Missing data", "Start date cannot be empty.");
            return; // keep window open
        }

        // Validate Name
        if (name == null || name.trim().isEmpty()) {
            AlertUtil.showError("Missing data", "Project name cannot be empty.");
            return; // keep window open
        }

        // Validate that end date is not before start date (if both are provided)
        if (endDate != null && endDate.isBefore(startDate)) {
            AlertUtil.showError("Invalid Date", 
                "End date (" + endDate + ") cannot be before start date (" + startDate + ").");
            return; // keep window open
        }

        if (project == null) {
            // Creating new
            project = new Project(projectNo, name, startDate, endDate);
        } else {
            // Updating existing
            project.setProjectNo(projectNo);
            project.setName(name);
            project.setStartDate(startDate);
            project.setEndDate(endDate);
        }

        closeWindow();
    }

    @FXML
    private void handleCancelAction() {
        project = null; // discard
        closeWindow();
    }

    public Project getCreatedProject() {
        return project;
    }

    private void closeWindow() {
        Stage stage = (Stage) buttonOk.getScene().getWindow();
        stage.close();
    }
}
