package com.dropalltables.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import com.dropalltables.models.Consultant;
import com.dropalltables.util.AlertUtil;

public class CreateConsultantWindowController {

    @FXML
    private TextField textFieldConsultantNo;
    @FXML
    private TextField textFieldConsultantName;
    @FXML
    private TextField textFieldConsultantTitle;
    @FXML
    private Label labelHeader;
    @FXML
    private Button buttonOk;
    @FXML
    private Button buttonCancel;

    private Stage stage;
    private Consultant consultant; // created or edited consultant

    public void initialize() {
        Platform.runLater(() -> this.stage = (Stage) textFieldConsultantNo.getScene().getWindow());
    }

    // --- Called when opening in "edit" mode ---
    public void setConsultantForEdit(Consultant consultant) {
        if (consultant != null) {
            this.consultant = consultant;

            textFieldConsultantNo.setText(String.valueOf(consultant.getConsultantNo()));
            textFieldConsultantNo.setDisable(true); // usually PK, don’t allow edits
            textFieldConsultantName.setText(consultant.getName());
            textFieldConsultantTitle.setText(consultant.getTitle());

            // Update UI texts
            labelHeader.setText("Edit Consultant");
            buttonOk.setText("Save");
        }
    }

    // --- OK button ---
    @FXML
    private void handleOkAction() {
        // Validate ConsultantNo
        int consultantNo;
        try {
            consultantNo = Integer.parseInt(textFieldConsultantNo.getText().trim());
            if (consultantNo <= 0) {
                AlertUtil.showError("Invalid input", "Consultant number must be a positive integer.");
                return;
            }
        } catch (NumberFormatException e) {
            AlertUtil.showError("Invalid input", "Consultant number must be numeric.");
            return;
        }

        // Validate Name
        String name = textFieldConsultantName.getText().trim();
        if (name.isEmpty()) {
            AlertUtil.showError("Invalid input", "Consultant name cannot be empty.");
            return;
        }

        // Validate Title
        String title = textFieldConsultantTitle.getText().trim();
        if (title.isEmpty()) {
            AlertUtil.showError("Invalid input", "Consultant title cannot be empty.");
            return;
        }

        if (consultant == null) {
            // Creating new
            consultant = new Consultant(consultantNo, name, title);
        } else {
            // Editing existing
            consultant.setName(name);
            consultant.setTitle(title);
        }

        stage.close();
    }

    // --- Cancel button ---
    @FXML
    private void handleCancelAction() {
        consultant = null; // discard changes
        stage.close();
    }

    public Consultant getConsultant() {
        return consultant;
    }
}
