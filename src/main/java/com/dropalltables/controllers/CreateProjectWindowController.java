package com.dropalltables.controllers;

import java.time.LocalDate;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import com.dropalltables.models.Project;

public class CreateProjectWindowController {
    @FXML
    private DatePicker datePickerNewProject;
    @FXML
    private TextField textFieldProjectName;
    @FXML
    private TextField textFieldProjectNo;

    private Stage stage;
    private Project createdProject; // store newly created project

    public void initialize() {
        // Sets datepicker to current date
        datePickerNewProject.setValue(LocalDate.now());

        Platform.runLater(() -> {
            this.stage = (Stage) datePickerNewProject.getScene().getWindow();
        });
    }

    @FXML
    void buttonCancelAction(ActionEvent event) {
        stage.close();
    }

    @FXML
    void buttonCreateAction(ActionEvent event) {
        try {
            int projectNo = Integer.parseInt(textFieldProjectNo.getText());
            String name = textFieldProjectName.getText();
            LocalDate startDate = datePickerNewProject.getValue();

            createdProject = new Project(projectNo, name, startDate);

            stage.close();
        } catch (NumberFormatException e) {
            // TODO: show alert if project number is not numeric
            e.printStackTrace();
        }
    }

    // Getter so the calling controller can retrieve the created project
    public Project getCreatedProject() {
        return createdProject;
    }
}
