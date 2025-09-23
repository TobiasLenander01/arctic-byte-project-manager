package com.dropalltables.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;

public class MainController {
    private AppController appController;

    @FXML
    private ScrollPane scrollPaneContent;

    public ScrollPane getScrollPaneContent() {
        return scrollPaneContent;
    }

    public void handleButtonProjectsClickEvent(ActionEvent event) {
        appController.changeView("Projects");
    }

    public void handleButtonConsultantsClickEvent(ActionEvent event) {
        appController.changeView("Consultants");
    }

    public void setAppController(AppController appController) {
        this.appController = appController;
    }
}
