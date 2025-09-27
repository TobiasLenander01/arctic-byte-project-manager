package com.dropalltables.data;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConnectionHandler {
    private final String connectionURL;
    private final String propertiesFilePath = "/config.properties";

    /**
     * Constructor for ConnectionHandler.
     * Reads database connection details from a properties file, builds the connection URL.
     * @throws IOException if the properties file cannot be found or read.
     */
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
        String databaseUsername = connectionProperties.getProperty("database.user.name");
        String databasePassword = connectionProperties.getProperty("database.user.password");

        connectionURL = "jdbc:sqlserver://"
                + databaseServerName + ":" + databaseServerPort + ";"
                + "database=" + databaseName + ";"
                + "user=" + databaseUsername + ";"
                + "password=" + databasePassword + ";"
                + "encrypt=true;"
                + "trustServerCertificate=true;";
    }

    /**
     * Establishes and returns a new connection to the database.
     * @return A new Connection object.
     * @throws SQLException if a database access error occurs.
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(connectionURL);
    }

}
