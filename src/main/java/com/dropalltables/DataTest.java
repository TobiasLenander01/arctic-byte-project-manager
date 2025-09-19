package com.dropalltables;

import java.io.IOException;

import com.dropalltables.data.ConnectionHandler;

public class DataTest {
    public static void main(String[] args) {
        System.out.println("--- RUNNING DATA TESTS ---");

        // TEST DATABASE CONNECTION
        try {
            ConnectionHandler connectionHandler = new ConnectionHandler();
            if (connectionHandler.getConnection() != null) {
                System.out.println("Database connection successful.");
            } else {
                System.out.println("Database connection failed.");
            }
        } catch (IOException | RuntimeException | java.sql.SQLException e) {
            System.out.println("Error during database connection test: " + e.getMessage());
        }
    }
}
