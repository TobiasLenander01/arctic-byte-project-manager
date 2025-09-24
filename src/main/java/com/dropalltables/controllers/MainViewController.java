package com.dropalltables.controllers;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import com.dropalltables.util.*;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import javafx.scene.control.ScrollPane;

public class MainViewController {
    private AppController appController;

    @FXML
    private ScrollPane scrollPaneContent;

    public ScrollPane getScrollPaneContent() {
        return scrollPaneContent;
    }

    public void handleButtonProjectsClickEvent(ActionEvent event) {
        appController.changeView("ProjectsView");
    }

    public void handleButtonConsultantsClickEvent(ActionEvent event) {
        appController.changeView("ConsultantsView");
    }

    public void handleButtonMetadataClickEvent(ActionEvent event) {
        appController.changeView("MetadataView");
    }

    public void setAppController(AppController appController) {
        this.appController = appController;
    }

    // Open excel file
    @FXML
    public void buttonOpenExcelAction() {
        try {
            // Point to your file in /data
            File file = new File("data/db_export.xlsx");

            if (!file.exists()) {
                AlertUtil.showError("File not found", "The file 'db_export.xlsx' was not found in /data.");
                return;
            }

            // Open with the system default Excel app
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            } else {
                AlertUtil.showWarning("Not supported", "Opening Excel files is not supported on this system.");
            }

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Failed to open Excel file: " + e.getMessage());
        }
    }

}
