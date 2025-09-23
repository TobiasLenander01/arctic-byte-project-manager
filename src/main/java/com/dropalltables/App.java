package com.dropalltables;

import com.dropalltables.controllers.AppController;

import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Arctic Byte");
        AppController appController = new AppController(primaryStage);
        appController.showPrimaryStage();
        appController.changeView("ConsultantsView");
    }
}