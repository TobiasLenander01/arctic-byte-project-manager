package com.dropalltables.controllers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import com.dropalltables.data.DaoConsultant;
import com.dropalltables.data.DaoException;
import com.dropalltables.models.Consultant;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class ConsultantsViewController {

    @FXML
    private TableView<Consultant> tableViewConsultants;
    @FXML
    private TableColumn<Consultant, Number> tableColumnConsultantNo;
    @FXML
    private TableColumn<Consultant, String> tableColumnConsultantName;
    @FXML
    private TableColumn<Consultant, String> tableColumnConsultantTitle;

    private final ObservableList<Consultant> consultantData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTableColumns();
        loadConsultantsFromDatabase();
    }

    private void setupTableColumns() {
        // Match the getters in Consultant: getConsultantNo(), getName(), getTitle()
        tableColumnConsultantNo.setCellValueFactory(new PropertyValueFactory<>("consultantNo"));
        tableColumnConsultantName.setCellValueFactory(new PropertyValueFactory<>("name"));
        tableColumnConsultantTitle.setCellValueFactory(new PropertyValueFactory<>("title"));

        tableViewConsultants.setItems(consultantData);
    }

    private void loadConsultantsFromDatabase() {
        try {
            DaoConsultant dao = new DaoConsultant();
            List<Consultant> consultants = dao.getAllConsultants();
            consultantData.setAll(consultants);
        } catch (IOException | DaoException e) {
            e.printStackTrace();
        }
    }
}
