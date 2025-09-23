package com.dropalltables.controllers;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AppController {
    private final Stage primaryStage;
    private MainController mainViewController;

    public AppController(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void showPrimaryStage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Main.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.show();

            mainViewController = loader.getController();
            if (mainViewController != null) {
                mainViewController.setAppController(this);
            }

        } catch (IOException e) {
            System.out.println("Error loading FXML file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void changeView(String viewName) {
        if (mainViewController == null) {
            System.out.println("MainController is not initialized. Cannot change view to: " + viewName);
            return;
        }
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/" + viewName + ".fxml"));
        try {
            Parent content = loader.load();
            mainViewController.getScrollPaneContent().setContent(content);
        } catch (IOException e) {
            System.out.println("Error loading " + viewName + " FXML file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
