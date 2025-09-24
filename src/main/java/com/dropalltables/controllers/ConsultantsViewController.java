package com.dropalltables.controllers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

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
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
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
    private Label labelConsultantCount; // NEW
    @FXML
    private TextField textFieldFilterNo;
    @FXML
    private TextField textFieldFilterName;
    @FXML
    private TextField textFieldFilterTitle;

    private final ObservableList<Consultant> consultantData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTableColumns();
        loadConsultantsFromDatabase();

        // update info box when consultant selected
        tableViewConsultants.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSel, newSel) -> showConsultantInfo(newSel));

        // live filtering
        textFieldFilterNo.textProperty().addListener((obs, o, n) -> applyFilters());
        textFieldFilterName.textProperty().addListener((obs, o, n) -> applyFilters());
        textFieldFilterTitle.textProperty().addListener((obs, o, n) -> applyFilters());
    }

    // --- Setup columns for Consultants table ---
    private void setupTableColumns() {
        tableColumnConsultantNo.setCellValueFactory(new PropertyValueFactory<>("consultantNo"));
        tableColumnConsultantName.setCellValueFactory(new PropertyValueFactory<>("name"));
        tableColumnConsultantTitle.setCellValueFactory(new PropertyValueFactory<>("title"));

        tableViewConsultants.setItems(consultantData);
    }

    // --- Load all consultants from DB ---
    private void loadConsultantsFromDatabase() {
        try {
            DaoConsultant dao = new DaoConsultant();
            List<Consultant> consultants = dao.getAllConsultants();
            consultantData.setAll(consultants);

            // update total count label (not affected by filters)
            labelConsultantCount.setText("Total consultants in system: " + consultants.size() + "\n"
                    + "Total hours worked by all consultants: "
                    + new DaoProjectAssignment().totalHoursForAllConsultants());

        } catch (DaoException | IOException | SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load consultants: " + e.getMessage());
        }
    }

    private void applyFilters() {
        String filterNo = textFieldFilterNo.getText().trim();
        String filterName = textFieldFilterName.getText().toLowerCase().trim();
        String filterTitle = textFieldFilterTitle.getText().toLowerCase().trim();

        List<Consultant> allConsultants;
        try {
            DaoConsultant dao = new DaoConsultant();
            allConsultants = dao.getAllConsultants();
        } catch (DaoException e) {
            e.printStackTrace();
            return;
        }

        consultantData.setAll(allConsultants.stream()
                .filter(c -> filterNo.isEmpty() || String.valueOf(c.getConsultantNo()).startsWith(filterNo))
                .filter(c -> filterName.isEmpty() || c.getName().toLowerCase().contains(filterName))
                .filter(c -> filterTitle.isEmpty() || c.getTitle().toLowerCase().contains(filterTitle))
                .toList());
    }

    // --- Button: open "Create Consultant" dialog ---
    // --- Button: open "Create Consultant" dialog ---
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
            dialog.showAndWait(); // block until dialog closes

            Consultant newConsultant = controller.getConsultant(); // ✅ unified getter
            if (newConsultant != null) {
                DaoConsultant dao = new DaoConsultant();
                dao.insertConsultant(newConsultant);

                loadConsultantsFromDatabase(); // refresh
            }

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Failed to add consultant: " + e.getMessage()); // ✅ use AlertUtil
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
                DaoConsultant dao = new DaoConsultant();
                dao.updateConsultant(selected.getConsultantNo(), updated); // ✅ correct usage
                loadConsultantsFromDatabase();
            }

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Failed to edit consultant: " + e.getMessage());
        }
    }

    // --- Button: delete selected consultant ---
    @FXML
    public void buttonDeleteConsultantAction() {
        Consultant selected = tableViewConsultants.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No selection", "Please select a consultant to delete.");
            return;
        }

        // Confirmation dialog
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete consultant \"" + selected.getName() + "\"?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText(null);

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    DaoConsultant dao = new DaoConsultant();
                    dao.deleteConsultant(selected.getConsultantNo());

                    // Refresh table and update count
                    loadConsultantsFromDatabase();
                } catch (DaoException e) {
                    e.printStackTrace();
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
            // --- translate ConsultantNo -> ConsultantID ---
            DaoConsultant daoConsultant = new DaoConsultant();
            int consultantID = daoConsultant.getConsultantID(consultant.getConsultantNo());

            // --- fetch assignments + total hours ---
            DaoProjectAssignment daoPA = new DaoProjectAssignment();
            int assignmentCount = daoPA.getByConsultantID(consultantID).size();
            int totalHours = daoPA.totalHoursForConsultant(consultantID);

            // --- update labels ---
            labelConsultantNo.setText("No: " + consultant.getConsultantNo());
            labelConsultantName.setText("Name: " + consultant.getName());
            labelConsultantTitle.setText("Title: " + consultant.getTitle());
            labelConsultantAssignments.setText("Current no. of assignments: " + assignmentCount);
            labelConsultantHours.setText("Total hours worked: " + totalHours);

        } catch (Exception e) {
            e.printStackTrace();
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

    // --- Helper: show alert messages ---
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
