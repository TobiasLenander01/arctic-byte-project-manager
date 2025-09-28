package com.dropalltables.controllers;

import java.time.LocalDate;

import com.dropalltables.data.DaoException;
import com.dropalltables.data.DaoMilestone;
import com.dropalltables.models.Milestone;
import com.dropalltables.models.Project;
import com.dropalltables.util.AlertUtil;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Dialog controller for creating OR editing a Milestone.
 *
 * • When used for **create**, call {@link #setProject(Project)} before showing.
 * • When used for **edit**, call {@link #setMilestoneForEdit(Milestone)}.
 *
 * Validation rules:
 * • Milestone number must be positive integer.
 * • Name cannot be blank.
 * • Date cannot be before 2022-01-01, before project start, or after project
 * end.
 * • On create, milestone number must be unique in the DB.
 */
public class CreateMilestoneWindowController {

    // ------------------------------------------------------------------------
    // --- FXML UI fields
    // ------------------------------------------------------------------------
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

    // ------------------------------------------------------------------------
    // --- Internal state
    // ------------------------------------------------------------------------
    private Milestone milestone = null; // will hold the created/edited milestone
    private Project project; // project this milestone belongs to
    private boolean editing = false; // true when editing an existing milestone

    // ------------------------------------------------------------------------
    // --- Init
    // ------------------------------------------------------------------------
    public void initialize() {
        // Default the date to today when creating a brand-new milestone
        datePickerMilestoneDate.setValue(LocalDate.now());
    }

    /**
     * Called when creating a brand-new milestone.
     * Sets the project reference and adjusts default date to a sensible range.
     */
    public void setProject(Project project) {
        this.project = project;
        if (project != null) {
            labelProjectInfo.setText("For project: " + project.getName() + " (#" + project.getProjectNo() + ")");
            // Pick a reasonable default date between 2022-01-01, project start, and project
            // end.
            LocalDate minimumDate = LocalDate.of(2022, 1, 1);
            LocalDate defaultDate = LocalDate.now();

            if (project.getStartDate() != null) {
                LocalDate start = project.getStartDate();
                if (start.isAfter(defaultDate))
                    defaultDate = start;
                if (start.isAfter(minimumDate))
                    minimumDate = start;
            }
            if (project.getEndDate() != null) {
                LocalDate end = project.getEndDate();
                if (defaultDate.isAfter(end))
                    defaultDate = end;
            }
            if (defaultDate.isBefore(minimumDate))
                defaultDate = minimumDate;

            datePickerMilestoneDate.setValue(defaultDate);
        }
    }

    /**
     * Called when editing an existing milestone.
     * Pre-fills all fields and sets editing mode.
     */
    public void setMilestoneForEdit(Milestone m) {
        this.milestone = m;
        this.project = m.getProject();
        this.editing = true;

        labelHeader.setText("Updating milestone");
        labelProjectInfo.setText("Editing milestone #" + m.getMilestoneNo() +
                " for project " + project.getName());

        textFieldMilestoneNo.setText(String.valueOf(m.getMilestoneNo()));
        textFieldMilestoneNo.setDisable(true); // cannot change number when editing
        textFieldMilestoneName.setText(m.getName());
        datePickerMilestoneDate.setValue(m.getDate());
    }

    // ------------------------------------------------------------------------
    // --- Button handlers
    // ------------------------------------------------------------------------
    @FXML
    public void handleSaveAction() {
        // Reset milestone to null each time Save is clicked
        milestone = null;

        String milestoneNoText = textFieldMilestoneNo.getText();
        String name = textFieldMilestoneName.getText();
        LocalDate date = datePickerMilestoneDate.getValue();

        // --- Validate number
        int milestoneNo;
        try {
            milestoneNo = Integer.parseInt(milestoneNoText.trim());
            if (milestoneNo <= 0) {
                AlertUtil.showError("Invalid input", "Milestone number must be a positive number.");
                return;
            }
        } catch (NumberFormatException e) {
            AlertUtil.showError("Invalid input", "Milestone number must be a valid positive number.");
            return;
        }

        // --- Validate name
        if (name == null || name.trim().isEmpty()) {
            AlertUtil.showError("Missing data", "Milestone name cannot be empty.");
            return;
        }

        // --- Validate date presence
        if (date == null) {
            AlertUtil.showError("Missing data", "Milestone date cannot be empty.");
            return;
        }

        // --- Date constraints
        LocalDate minimumDate = LocalDate.of(2022, 1, 1);
        if (date.isBefore(minimumDate)) {
            AlertUtil.showError("Invalid Date", "Milestone date cannot be before " + minimumDate + ".");
            return;
        }
        if (project != null && project.getStartDate() != null && date.isBefore(project.getStartDate())) {
            AlertUtil.showError("Invalid Date",
                    "Milestone date cannot be before the project start date (" + project.getStartDate() + ").");
            return;
        }
        if (project != null && project.getEndDate() != null && date.isAfter(project.getEndDate())) {
            AlertUtil.showError("Invalid Date",
                    "Milestone date cannot be after the project end date (" + project.getEndDate() + ").");
            return;
        }

        // --- Uniqueness check only when creating
        if (!editing) {
            try {
                DaoMilestone dao = new DaoMilestone();
                if (dao.milestoneNoExists(milestoneNo)) {
                    AlertUtil.showError("Duplicate Milestone Number",
                            "Milestone number " + milestoneNo + " already exists. Please choose a different number.");
                    return;
                }
            } catch (DaoException e) {
                AlertUtil.showError("Error", e.getMessage());
                return;
            }
        }

        // --- Construct the result object
        milestone = new Milestone(milestoneNo, name.trim(), date, project);
        closeWindow();
    }

    @FXML
    public void handleCancelAction() {
        milestone = null; // discard
        closeWindow();
    }

    // ------------------------------------------------------------------------
    // --- Accessor for caller
    // ------------------------------------------------------------------------
    public Milestone getCreatedMilestone() {
        return milestone;
    }

    // ------------------------------------------------------------------------
    // --- Utility
    // ------------------------------------------------------------------------
    private void closeWindow() {
        Stage stage = (Stage) buttonOk.getScene().getWindow();
        stage.close();
    }
}
