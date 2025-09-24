package com.dropalltables.controllers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import com.dropalltables.data.DaoMetadata;

import javafx.fxml.FXML;
import javafx.scene.text.Text;

public class MetadataViewController {
    @FXML
    Text textAllColumnNames;
    @FXML
    Text textPKConstraints;
    @FXML
    Text textCheckConstraints;
    @FXML
    Text textConsultantNotInteger;
    @FXML
    Text textMaxRowsTable;

    private DaoMetadata dao;

    @FXML
    public void initialize() {
        try {
            dao = new DaoMetadata();
            textAllColumnNames.setText("All column names:\n" + stringFromList(dao.getAllDatabaseColumns()));
            textPKConstraints
                    .setText("Names of all primary key constraints:\n" + stringFromList(dao.getAllPKConstraints()));
            textCheckConstraints
                    .setText("Names of all check constraints:\n" + stringFromList(dao.getAllCheckConstraints()));
            textConsultantNotInteger.setText("Names of all columns in Consultant table NOT of type Integer:\n"
                    + stringFromList(dao.getNonIntConsultantColumns()));
            textMaxRowsTable.setText("Name and number of rows of table in db with the highest number of rows:\n"
                    + dao.getRowsFromMaxRowTable());

        } catch (IOException | SQLException e) {
            e.printStackTrace();
            textAllColumnNames.setText("Failed to load metadata: " + e.getMessage());
        }
    }

    // transform list into a comma-separated string
    private String stringFromList(List<String> l) {
        if (l.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        for (String s : l) {
            sb.append(", " + s);
        }
        sb.delete(0, 2); // remove leading comma and whitespace
        return sb.toString();
    }
}
