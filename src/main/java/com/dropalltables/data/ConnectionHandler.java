package com.dropalltables.data;

import java.io.FileInputStream;
import java.util.Properties;

/*
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;
*/

public class ConnectionHandler {
    private String connectionURL;
    private final String propertiesFilePath = "src/main/resources/config.properties";

        // hämtar inloggningsuppgifter från properties filen under resources
        // ist för att skriva in dem i koden
    public ConnectionHandler() {

        Properties connectionProperties = new Properties();
    
        try {
                FileInputStream stream = new FileInputStream("src/main/resources/config.properties");
                connectionProperties.load(stream);        

        } catch (Exception e) {
                System.out.println("could not load properties file");
                System.exit(1);
        }
    }
        
}
