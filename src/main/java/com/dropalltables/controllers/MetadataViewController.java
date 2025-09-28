package com.dropalltables.controllers;

import java.util.ArrayList;
import java.util.List;

import com.dropalltables.data.DaoException;
import com.dropalltables.data.DaoMetadata;
import com.dropalltables.util.AlertUtil;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class MetadataViewController {

    @FXML
    private ComboBox<String> comboQueries;
    @FXML
    private TableView<MetadataRow> tableResults;
    @FXML
    private Label labelStatus;

    private DaoMetadata dao;

    @FXML
    public void initialize() {
        try {
            dao = new DaoMetadata();
        } catch (DaoException e) {
            AlertUtil.showError("Error", e.getMessage());
            return;
        }

        // populate combo with the available metadata queries
        comboQueries.setItems(FXCollections.observableArrayList(
                "All column names",
                "Primary key constraints",
                "Check constraints",
                "Consultant columns not INTEGER",
                "Table with max rows"));
        comboQueries.getSelectionModel().selectFirst();

        // prepare table with a single string column to start with
        TableColumn<MetadataRow, String> col = new TableColumn<>("Result");
        col.setCellValueFactory(new PropertyValueFactory<>("value"));
        tableResults.getColumns().add(col);
    }

    @FXML
    public void handleRunQuery() {
        if (dao == null)
            return;

        String choice = comboQueries.getValue();
        List<String> data = new ArrayList<>();

        try {
            if ("All column names".equals(choice)) {
                data = dao.getAllDatabaseColumns();
            } else if ("Primary key constraints".equals(choice)) {
                data = dao.getAllPKConstraints();
            } else if ("Check constraints".equals(choice)) {
                data = dao.getAllCheckConstraints();
            } else if ("Consultant columns not INTEGER".equals(choice)) {
                data = dao.getNonIntConsultantColumns();
            } else if ("Table with max rows".equals(choice)) {
                // this one returns a single descriptive string
                data.add(dao.getRowsFromMaxRowTable());
            }
        } catch (DaoException e) {
            AlertUtil.showError("Error", e.getMessage());
            return;
        }

        // wrap results for TableView
        List<MetadataRow> rows = new ArrayList<>();
        for (String s : data) {
            rows.add(new MetadataRow(s));
        }
        tableResults.setItems(FXCollections.observableArrayList(rows));
        labelStatus.setText("Rows: " + rows.size());
    }

    /** Simple holder for one-column table rows */
    public static class MetadataRow {
        private final String value;

        public MetadataRow(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
