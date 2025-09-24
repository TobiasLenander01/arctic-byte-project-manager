package com.dropalltables.controllers;

import java.util.ArrayList;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;

public class MainViewController {
    private AppController appController;

    @FXML
    private ScrollPane scrollPaneContent;
    
    @FXML
    private Button buttonProjects;
    
    @FXML
    private Button buttonConsultants;
    
    @FXML
    private Button buttonMetadata;
    
    private List<javafx.scene.Node> navigationItems;

    public void initialize() {
        // Initialize the navigation items list
        navigationItems = new ArrayList<>();
        navigationItems.add(buttonProjects);
        navigationItems.add(buttonConsultants);
        navigationItems.add(buttonMetadata);
        
        // Set Projects as active initially
        setActiveState(buttonProjects);
    }

    public ScrollPane getScrollPaneContent() {
        return scrollPaneContent;
    }

    /**
     * Clears the active state from all navigation items
     */
    private void clearAllActiveStates() {
        for (javafx.scene.Node item : navigationItems) {
            item.getStyleClass().remove("nav-item-active");
        }
    }

    /**
     * Sets the active state on a specific navigation item
     */
    private void setActiveState(Node item) {
        clearAllActiveStates();
        if (!item.getStyleClass().contains("nav-item-active")) {
            item.getStyleClass().add("nav-item-active");
        }
    }

    public void handleButtonProjectsClickEvent(ActionEvent event) {
        setActiveState(buttonProjects);
        appController.changeView("ProjectsView");
    }

    public void handleButtonConsultantsClickEvent(ActionEvent event) {
        setActiveState(buttonConsultants);
        appController.changeView("ConsultantsView");
    }

    public void handleButtonMetadataClickEvent(ActionEvent event) {
        setActiveState(buttonMetadata);
        appController.changeView("MetadataView");
    }

    public void setAppController(AppController appController) {
        this.appController = appController;
    }
}
