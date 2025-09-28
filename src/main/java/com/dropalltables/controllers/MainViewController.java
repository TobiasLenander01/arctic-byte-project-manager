package com.dropalltables.controllers;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.dropalltables.data.DaoConsultant;
import com.dropalltables.data.DaoException;
import com.dropalltables.data.DaoProject;
import com.dropalltables.data.DaoProjectAssignment;
import com.dropalltables.models.Consultant;
import com.dropalltables.util.AlertUtil;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;

/**
 * Main application frame:
 * • Handles left-sidebar navigation
 * • Wires top-menu actions: Close / Report / About
 */
public class MainViewController {

    // --------------------------------------------------------------------
    // UI references
    // --------------------------------------------------------------------
    private AppController appController;

    @FXML
    private ScrollPane scrollPaneContent;

    @FXML
    private Button buttonProjects;
    @FXML
    private Button buttonConsultants;
    @FXML
    private Button buttonMetadata;

    private List<Node> navigationItems;

    // --------------------------------------------------------------------
    // Initialisation
    // --------------------------------------------------------------------
    @FXML
    public void initialize() {
        navigationItems = new ArrayList<>();
        navigationItems.add(buttonProjects);
        navigationItems.add(buttonConsultants);
        navigationItems.add(buttonMetadata);

        // default active section
        setActiveState(buttonProjects);
    }

    public void setAppController(AppController appController) {
        this.appController = appController;
    }

    public ScrollPane getScrollPaneContent() {
        return scrollPaneContent;
    }

    // --------------------------------------------------------------------
    // Sidebar navigation
    // --------------------------------------------------------------------
    private void clearAllActiveStates() {
        for (Node item : navigationItems) {
            item.getStyleClass().remove("nav-item-active");
        }
    }

    private void setActiveState(Node item) {
        clearAllActiveStates();
        if (!item.getStyleClass().contains("nav-item-active")) {
            item.getStyleClass().add("nav-item-active");
        }
    }

    @FXML
    public void handleButtonProjectsClickEvent(ActionEvent e) {
        setActiveState(buttonProjects);
        appController.changeView("ProjectsView");
    }

    @FXML
    public void handleButtonConsultantsClickEvent(ActionEvent e) {
        setActiveState(buttonConsultants);
        appController.changeView("ConsultantsView");
    }

    @FXML
    public void handleButtonMetadataClickEvent(ActionEvent e) {
        setActiveState(buttonMetadata);
        appController.changeView("MetadataView");
    }

    // --------------------------------------------------------------------
    // Excel export button
    // --------------------------------------------------------------------
    @FXML
    public void handleButtonExcelClickEvent() {
        try {
            File file = new File("data/db_export.xlsx");
            if (!file.exists()) {
                AlertUtil.showInfo("File not found",
                        "The file 'db_export.xlsx' was not found in /data.");
                return;
            }
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            } else {
                AlertUtil.showError("Not supported",
                        "Opening Excel files is not supported on this system.");
            }
        } catch (IOException e) {
            AlertUtil.showError("Error", "Failed to open Excel file.");
        }
    }

    // --------------------------------------------------------------------
    // NEW — Menu: File → Close
    // --------------------------------------------------------------------
    @FXML
    public void handleMenuClose() {
        // Gracefully exit JavaFX application
        Platform.exit();
    }

    // --------------------------------------------------------------------
    // NEW — Menu: View → Report
    // Shows the “Consultant Report” that used to live in ConsultantsView
    // --------------------------------------------------------------------
    @FXML
    public void handleMenuReport() {
        try {
            DaoProjectAssignment daoPA = new DaoProjectAssignment();
            DaoConsultant daoCon = new DaoConsultant();
            DaoProject daoPro = new DaoProject();

            // --- Hardest-working consultants (ties included) -----------------
            List<Integer> topIds = daoPA.hardestWorkingConsultants();
            StringBuilder topSb = new StringBuilder();
            int maxHours = -1;
            for (Integer id : topIds) {
                Consultant c = daoCon.getConsultantByID(id);
                int hours = daoPA.totalHoursForConsultant(id);
                if (hours > maxHours)
                    maxHours = hours;
                topSb.append(c.getName())
                        .append(" (").append(c.getTitle()).append(") – ")
                        .append(hours).append(" hours\n");
            }

            // --- Totals for the whole system ---------------------------------
            int totalConsultants = daoCon.getAllConsultants().size();
            int totalHoursWorked = daoPA.totalHoursForAllConsultants(); // implement: SUM(HoursWorked)
            int totalProjects = daoPro.getAllProjects().size();
            int completedProjects = daoPro.getCompletedProjects().size(); // EndDate IS NOT NULL

            // --- Build and show the report -----------------------------------
            StringBuilder report = new StringBuilder();
            report.append("Hardest-working consultant(s):\n")
                    .append(topSb).append("\n")
                    .append("Total consultants: ").append(totalConsultants).append("\n")
                    .append("Total hours worked: ").append(totalHoursWorked).append("\n")
                    .append("Projects in total: ").append(totalProjects).append("\n")
                    .append("Completed projects: ").append(completedProjects);

            AlertUtil.showInfo("Consultant Report", report.toString());

        } catch (DaoException e) {
            if (e.getMessage().toLowerCase().contains("no project assignments")) {
                AlertUtil.showInfo("Consultant Report", "No consultant has logged any hours.");
            } else {
                AlertUtil.showError("Error", e.getMessage());
            }
        }
    }

    // --------------------------------------------------------------------
    // NEW — Menu: Help → About
    // Quick info about the group members
    // --------------------------------------------------------------------
    @FXML
    public void handleMenuAbout() {
        String info = """
                ARCTICBYTE Team
                Albin Dahlberg
                Elias Hero
                Tobias Lenander
                Pontus Liljeberg\n
                Course: Databaser SYSB23 - Lund University
                Version: 0.1 | 2025
                🏴‍☠️ No rights reserved
                """;
        AlertUtil.showInfo("About", info);
    }
}
