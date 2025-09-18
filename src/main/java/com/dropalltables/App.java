package com.dropalltables;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class App extends Application {
    public static void main(String[] args) {
        launch(args);
        
    }

    @Override
    public void start(Stage primaryStage) {

        // Set a title to the window
        primaryStage.setTitle("Drop All Tables");

        // Create a root node
        VBox root = new VBox();
        
        // Add a Hello World label
        Label helloLabel = new Label("Hello World!");
        root.getChildren().add(helloLabel);
        
        // Create a new scene
        Scene scene = new Scene(root, 800, 600);
        
        // Show the scene in the primary stage
        primaryStage.setScene(scene);
        primaryStage.show();

    }
}