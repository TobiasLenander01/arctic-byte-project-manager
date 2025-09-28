package com.dropalltables.controllers;

import java.io.IOException;
import java.util.List;

import com.dropalltables.data.DaoConsultant;
import com.dropalltables.data.DaoException;
import com.dropalltables.data.DaoProject;
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
    private TableColumn<Consultant, Number> tableColumnProjectCount;

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
    private Label labelConsultantCount; // reused for total / filtered count

    @FXML
    private TextField textFieldFilterNo;
    @FXML
    private TextField textFieldFilterName;
    @FXML
    private TextField textFieldFilterTitle;
    @FXML
    private TextField textFieldFilterProjects;

    private final ObservableList<Consultant> consultantData = FXCollections.observableArrayList();
    private List<Consultant> allConsultantsCache;

    @FXML
    public void initialize() {
        setupTableColumns();
        loadConsultantsFromDatabase();

        tableViewConsultants.getSelectionModel().selectedItemProperty()
                .addListener((obs, o, n) -> showConsultantInfo(n));

        textFieldFilterNo.textProperty().addListener((obs, o, n) -> refreshVisibleConsultants());
        textFieldFilterName.textProperty().addListener((obs, o, n) -> refreshVisibleConsultants());
        textFieldFilterTitle.textProperty().addListener((obs, o, n) -> refreshVisibleConsultants());
        textFieldFilterProjects.textProperty().addListener((obs, o, n) -> refreshVisibleConsultants());
    }

    private void setupTableColumns() {
        tableColumnConsultantNo.setCellValueFactory(new PropertyValueFactory<>("consultantNo"));
        tableColumnConsultantName.setCellValueFactory(new PropertyValueFactory<>("name"));
        tableColumnConsultantTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        tableColumnProjectCount.setCellValueFactory(new PropertyValueFactory<>("projectCount"));
        tableViewConsultants.setItems(consultantData);
    }

    /** Load all consultants with project counts and display them. */
    private void loadConsultantsFromDatabase() {
        try {
            DaoConsultant daoCon = new DaoConsultant();
            allConsultantsCache = daoCon.getAllWithProjectCount();
            // initial table contents
            consultantData.setAll(allConsultantsCache);
            // initial label shows total consultants
            labelConsultantCount.setText("Total consultants: " + allConsultantsCache.size());
        } catch (DaoException e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    /** Applies all filters and updates the label text accordingly. */
    private void refreshVisibleConsultants() {
        String filterNo = textFieldFilterNo.getText().trim();
        String filterName = textFieldFilterName.getText().toLowerCase().trim();
        String filterTitle = textFieldFilterTitle.getText().toLowerCase().trim();
        String filterProj = textFieldFilterProjects.getText().trim();

        Integer maxProjects = null;
        if (!filterProj.isEmpty()) {
            try {
                int val = Integer.parseInt(filterProj);
                if (val >= 0)
                    maxProjects = val;
            } catch (NumberFormatException ignored) {
                /* ignore bad input */ }
        }
        final Integer limit = maxProjects;

        consultantData.setAll(
                allConsultantsCache.stream()
                        .filter(c -> filterNo.isEmpty() || String.valueOf(c.getConsultantNo()).startsWith(filterNo))
                        .filter(c -> filterName.isEmpty() || c.getName().toLowerCase().contains(filterName))
                        .filter(c -> filterTitle.isEmpty() || c.getTitle().toLowerCase().contains(filterTitle))
                        .filter(c -> limit == null || c.getProjectCount() <= limit)
                        .toList());

        // decide if any filter is active
        boolean anyFilter = !filterNo.isEmpty() || !filterName.isEmpty() || !filterTitle.isEmpty() || limit != null;

        if (anyFilter) {
            labelConsultantCount.setText("Filtered consultants: " + consultantData.size());
        } else {
            labelConsultantCount.setText("Total consultants: " + allConsultantsCache.size());
        }
    }

    // === CRUD Buttons ===
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
        } catch (IOException e) {
            AlertUtil.showError("Error", "Failed to load dialog");
        } catch (DaoException e) {
            AlertUtil.showError("Error", e.getMessage());
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
        } catch (IOException e) {
            AlertUtil.showError("Error", "Failed to load dialog");
        } catch (DaoException e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    @FXML
    public void buttonDeleteConsultantAction() {
        Consultant selected = tableViewConsultants.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showInfo("No selection", "Please select a consultant to delete.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete consultant \"" + selected.getName() + "\"?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.YES) {
                try {
                    new DaoConsultant().deleteConsultant(selected.getConsultantNo());
                    loadConsultantsFromDatabase();
                } catch (DaoException e) {
                    AlertUtil.showError("Error", e.getMessage());
                }
            }
        });
    }

    private void showConsultantInfo(Consultant c) {
        if (c == null) {
            clearConsultantInfo();
            return;
        }
        try {
            int hours = new com.dropalltables.data.DaoProjectAssignment()
                    .totalHoursForConsultant(
                            new com.dropalltables.data.DaoConsultant()
                                    .getConsultantID(c.getConsultantNo()));
            labelConsultantNo.setText("No: " + c.getConsultantNo());
            labelConsultantName.setText("Name: " + c.getName());
            labelConsultantTitle.setText("Title: " + c.getTitle());
            labelConsultantAssignments.setText("Projects: " + c.getProjectCount());
            labelConsultantHours.setText("Total hours worked: " + hours);
        } catch (DaoException e) {
            AlertUtil.showError("Error", e.getMessage());
            clearConsultantInfo();
        }
    }

    private void clearConsultantInfo() {
        labelConsultantNo.setText("No:");
        labelConsultantName.setText("Name:");
        labelConsultantTitle.setText("Title:");
        labelConsultantAssignments.setText("Projects:");
        labelConsultantHours.setText("Total Hours:");
    }
}
