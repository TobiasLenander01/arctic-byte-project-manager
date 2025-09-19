package com.dropalltables.data;

import java.io.InputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConnectionHandler {
    private String connectionURL;
    private final String propertiesFilePath = "src/main/resources/config.properties";

        // hämtar inloggningsuppgifter från properties filen under resources
        // ist för att skriva in dem i koden
        //om fil inte hittas throws IOException till klass som anropar konstruktorn
    public ConnectionHandler() throws IOException {

        Properties connectionProperties = new Properties();

        try (InputStream inputStream = getClass().getResourceAsStream(propertiesFilePath)) {

            if (inputStream != null) {
                connectionProperties.load(inputStream);
            } else {
                throw new IOException("Properties file not found: " + propertiesFilePath);
            }
        }

        String databaseServerName = connectionProperties.getProperty("database.server.name");
        String databaseServerPort = connectionProperties.getProperty("database.server.port");
        String databaseName = connectionProperties.getProperty("database.name");
        String databaseUsername = connectionProperties.getProperty("database.username");
        String databasePassword = connectionProperties.getProperty("database.password");

        connectionURL = "jdbc:sqlserver://"
                + databaseServerName + ";"
                + databaseServerPort + ";"
                + "database=" + databaseName + ";"
                + "user=" + databaseUsername + ";"
                + "password=" + databasePassword + ";"
                + "encrypt=true;"
                + "trustServerCertificate=true;";
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(connectionURL);
    } 

}
