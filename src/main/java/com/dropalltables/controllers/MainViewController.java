package com.dropalltables.controllers;

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

    public void setAppController(AppController appController) {
        this.appController = appController;
    }
}
