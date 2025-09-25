package com.dropalltables.data;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DaoMetadata {
    private ConnectionHandler connectionHandler;

    public DaoMetadata() throws DaoException {
        try {
            this.connectionHandler = new ConnectionHandler();
        } catch (IOException e) {
            throw new DaoException("Unable to connect to the database. Please check your connection and try again.");
        }
    }

    // Helper method to avoid duplicate code when querying for single columns
    private List<String> fetchSingleColumn(String sql, String columnLabel) throws DaoException {
        List<String> list = new ArrayList<>();
        try (Connection c = connectionHandler.getConnection();
                PreparedStatement ps = c.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(rs.getString(columnLabel));
            }
        } catch (SQLException e) {
            throw new DaoException("Unable to load information. Please try again.");
        }
        return list;
    }

    // METADATA REQUIREMENTS

    // Names of all columns in database
    public List<String> getAllDatabaseColumns() throws DaoException {
        String sql = """
                SELECT COLUMN_NAME
                FROM INFORMATION_SCHEMA.COLUMNS
                ORDER BY COLUMN_NAME
                """;
        return fetchSingleColumn(sql, "COLUMN_NAME");
    }

    // Names of all primary key constraints in database
    public List<String> getAllPKConstraints() throws DaoException {
        String sql = """
                SELECT CONSTRAINT_NAME
                FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
                WHERE CONSTRAINT_TYPE = 'PRIMARY KEY'
                """;
        return fetchSingleColumn(sql, "CONSTRAINT_NAME");
    }

    // Names of all check constraints in database
    public List<String> getAllCheckConstraints() throws DaoException {
        String sql = """
                SELECT CONSTRAINT_NAME
                FROM INFORMATION_SCHEMA.CHECK_CONSTRAINTS
                """;
        return fetchSingleColumn(sql, "CONSTRAINT_NAME");
    }

    // Retrieve the names of all columns in Consultant table that are NOT of type
    // INTEGER and display them in the gui
    public List<String> getNonIntConsultantColumns() throws DaoException {
        String sql = """
                SELECT COLUMN_NAME
                FROM INFORMATION_SCHEMA.COLUMNS
                WHERE DATA_TYPE <> 'int'
                  AND TABLE_NAME = 'Consultant'
                """;
        return fetchSingleColumn(sql, "COLUMN_NAME");
    }

    // Retrieve the name and number of rows of the table in your database containing
    // the highest number of rows and display them in your application's gui
    public String getRowsFromMaxRowTable() throws DaoException {
        String result = null;
        String sql = """
                SELECT TOP 1
                t.name AS 'TableName',
                SUM(p.rows) AS 'RowCount'
                FROM sys.tables t
                JOIN sys.partitions p ON t.object_id = p.object_id
                WHERE p.index_id IN (0, 1)
                GROUP BY t.name
                ORDER BY SUM(p.rows) DESC
                """;

        try (Connection c = connectionHandler.getConnection();
                PreparedStatement ps = c.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                result = rs.getString("TableName") + " (" + rs.getInt("RowCount") + " rows)";
            }
        } catch (SQLException e) {
            throw new DaoException("Unable to load table information. Please try again.");
        }
        return result;
    }

}
