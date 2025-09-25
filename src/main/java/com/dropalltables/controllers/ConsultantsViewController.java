package com.dropalltables.controllers;

import java.util.List;
import java.util.stream.Collectors;

import com.dropalltables.data.DaoConsultant;
import com.dropalltables.data.DaoException;
import com.dropalltables.data.DaoProjectAssignment;
import com.dropalltables.models.Consultant;
import com.dropalltables.util.AlertUtil;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ConsultantsViewController {

    @FXML
    private TableView<Consultant> tableViewConsultants;
    @FXML
    private TableColumn<Consultant, Number> tableColumnConsultantNo;
    @FXML
    private TableColumn<Consultant, String> tableColumnConsultantName;
    @FXML
    private TableColumn<Consultant, String> tableColumnConsultantTitle;

    @FXML
    private Label labelConsultantNo;
    @FXML
    private Label labelConsultantName;
    @FXML
    private Label labelConsultantTitle;
    @FXML
    private Label labelConsultantAssignments;
    @FXML
    private Label labelConsultantHours;
    @FXML
    private Label labelConsultantCount;

    @FXML
    private TextField textFieldFilterNo;
    @FXML
    private TextField textFieldFilterName;
    @FXML
    private TextField textFieldFilterTitle;

    @FXML
    private ToggleButton toggleLowProjects; // new toggle

    private final ObservableList<Consultant> consultantData = FXCollections.observableArrayList();
    private List<Consultant> allConsultantsCache; // keeps the full list for toggling

    @FXML
    public void initialize() {
        setupTableColumns();
        loadConsultantsFromDatabase();

        // update info box when consultant selected
        tableViewConsultants.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldSel, newSel) -> showConsultantInfo(newSel));

        // live text filters
        textFieldFilterNo.textProperty().addListener((obs, o, n) -> applyFilters());
        textFieldFilterName.textProperty().addListener((obs, o, n) -> applyFilters());
        textFieldFilterTitle.textProperty().addListener((obs, o, n) -> applyFilters());
    }

    private void setupTableColumns() {
        tableColumnConsultantNo.setCellValueFactory(new PropertyValueFactory<>("consultantNo"));
        tableColumnConsultantName.setCellValueFactory(new PropertyValueFactory<>("name"));
        tableColumnConsultantTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        tableViewConsultants.setItems(consultantData);
    }

    /** Loads all consultants and updates the header label. */
    private void loadConsultantsFromDatabase() {
        try {
            DaoConsultant daoCon = new DaoConsultant();
            allConsultantsCache = daoCon.getAllConsultants(); // keep full list
            consultantData.setAll(allConsultantsCache);

            DaoProjectAssignment daoPA = new DaoProjectAssignment();
            int hardestID = daoPA.hardestWorkingConsultant();

            List<String> lowProjectConsultants = daoPA.consultantsInMaxNbrOfProjects(3);

            labelConsultantCount.setText(
                    "Total consultants: " + allConsultantsCache.size() + "\n" +
                            "Total hours worked: " + daoPA.totalHoursForAllConsultants() + "\n" +
                            "Hardest working: " + daoCon.getConsultantByID(hardestID).getName() +
                            " (" + daoPA.totalHoursForConsultant(hardestID) + " hrs)\n" +
                            "≤3 projects: " + String.join(", ", lowProjectConsultants));

        } catch (DaoException e) {
            AlertUtil.showError("Error", "Failed to load consultants: " + e.getMessage());
        }
    }

    /** Called when the toggle button is clicked. */
    @FXML
    private void handleToggleLowProjects() {
        if (toggleLowProjects.isSelected()) {
            try {
                DaoProjectAssignment daoPA = new DaoProjectAssignment();
                // get consultant names with <=3 projects
                List<String> lowNames = daoPA.consultantsInMaxNbrOfProjects(3);

                // filter cached list by those names
                List<Consultant> filtered = allConsultantsCache.stream()
                        .filter(c -> lowNames.contains(c.getName()))
                        .collect(Collectors.toList());
                consultantData.setAll(filtered);
            } catch (DaoException e) {
                AlertUtil.showError("Error", "Could not filter consultants: " + e.getMessage());
            }
        } else {
            // show all again
            consultantData.setAll(allConsultantsCache);
        }
        applyFilters(); // reapply text filters if user typed any
    }

    private void applyFilters() {
        String filterNo = textFieldFilterNo.getText().trim();
        String filterName = textFieldFilterName.getText().toLowerCase().trim();
        String filterTitle = textFieldFilterTitle.getText().toLowerCase().trim();

        consultantData.setAll(
                allConsultantsCache.stream()
                        .filter(c -> !toggleLowProjects.isSelected() || passesLowProjects(c))
                        .filter(c -> filterNo.isEmpty() || String.valueOf(c.getConsultantNo()).startsWith(filterNo))
                        .filter(c -> filterName.isEmpty() || c.getName().toLowerCase().contains(filterName))
                        .filter(c -> filterTitle.isEmpty() || c.getTitle().toLowerCase().contains(filterTitle))
                        .collect(Collectors.toList()));
    }

    // Check whether a consultant belongs to the <=3 projects group by name
    private boolean passesLowProjects(Consultant c) {
        try {
            DaoProjectAssignment daoPA = new DaoProjectAssignment();
            List<String> low = daoPA.consultantsInMaxNbrOfProjects(3);
            return low.contains(c.getName());
        } catch (DaoException e) {
            return false;
        }
    }

    // === CRUD Buttons remain unchanged ===
    @FXML
    public void buttonCreateConsultantAction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CreateConsultantWindow.fxml"));
            Parent root = loader.load();
            CreateConsultantWindowController controller = loader.getController();

            Stage dialog = new Stage();
            dialog.setTitle("Add Consultant");
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(new Scene(root));
            dialog.showAndWait();

            Consultant newConsultant = controller.getConsultant();
            if (newConsultant != null) {
                new DaoConsultant().insertConsultant(newConsultant);
                loadConsultantsFromDatabase();
            }
        } catch (Exception e) {
            AlertUtil.showError("Error", "Failed to add consultant: " + e.getMessage());
        }
    }

    @FXML
    public void buttonEditConsultantAction() {
        Consultant selected = tableViewConsultants.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showInfo("No selection", "Please select a consultant to edit.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CreateConsultantWindow.fxml"));
            Parent root = loader.load();
            CreateConsultantWindowController controller = loader.getController();
            controller.setConsultantForEdit(selected);

            Stage dialog = new Stage();
            dialog.setTitle("Edit Consultant");
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(new Scene(root));
            dialog.showAndWait();

            Consultant updated = controller.getConsultant();
            if (updated != null) {
                new DaoConsultant().updateConsultant(selected.getConsultantNo(), updated);
                loadConsultantsFromDatabase();
            }
        } catch (Exception e) {
            AlertUtil.showError("Error", "Failed to edit consultant: " + e.getMessage());
        }
    }

    @FXML
    public void buttonDeleteConsultantAction() {
        Consultant selected = tableViewConsultants.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No selection", "Please select a consultant to delete.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete consultant \"" + selected.getName() + "\"?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    new DaoConsultant().deleteConsultant(selected.getConsultantNo());
                    loadConsultantsFromDatabase();
                } catch (DaoException e) {
                    showAlert("Error", "Failed to delete consultant: " + e.getMessage());
                }
            }
        });
    }

    private void showConsultantInfo(Consultant consultant) {
        if (consultant == null) {
            clearConsultantInfo();
            return;
        }
        try {
            DaoConsultant daoConsultant = new DaoConsultant();
            int consultantID = daoConsultant.getConsultantID(consultant.getConsultantNo());

            DaoProjectAssignment daoPA = new DaoProjectAssignment();
            int assignmentCount = daoPA.getByConsultantID(consultantID).size();
            int totalHours = daoPA.totalHoursForConsultant(consultantID);

            labelConsultantNo.setText("No: " + consultant.getConsultantNo());
            labelConsultantName.setText("Name: " + consultant.getName());
            labelConsultantTitle.setText("Title: " + consultant.getTitle());
            labelConsultantAssignments.setText("Assignments: " + assignmentCount);
            labelConsultantHours.setText("Total hours: " + totalHours);
        } catch (Exception e) {
            clearConsultantInfo();
        }
    }

    private void clearConsultantInfo() {
        labelConsultantNo.setText("No:");
        labelConsultantName.setText("Name:");
        labelConsultantTitle.setText("Title:");
        labelConsultantAssignments.setText("Assignments:");
        labelConsultantHours.setText("Total Hours:");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
