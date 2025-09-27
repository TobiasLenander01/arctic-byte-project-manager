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

    /**
     * Constructor for DaoMetadata.
     * Initializes the ConnectionHandler.
     * @throws DaoException if unable to connect to the database.
     */
    public DaoMetadata() throws DaoException {
        try {
            this.connectionHandler = new ConnectionHandler();
        } catch (IOException e) {
            throw new DaoException("Unable to connect to the database. Please check your connection and try again.");
        }
    }

    /**
     * A generic helper method to fetch all values from a single column of a query result.
     * @param sql The SQL query to execute.
     * @param columnLabel The label of the column to retrieve data from.
     * @return A list of strings, where each string is a value from the specified column.
     * @throws DaoException if a database access error occurs.
     */
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

    /**
     * Retrieves a list of all column names from all tables in the database.
     * @return A sorted list of all column names.
     * @throws DaoException if a database access error occurs.
     */
    public List<String> getAllDatabaseColumns() throws DaoException {
        String sql = """
                SELECT COLUMN_NAME
                FROM INFORMATION_SCHEMA.COLUMNS
                ORDER BY COLUMN_NAME
                """;
        return fetchSingleColumn(sql, "COLUMN_NAME");
    }


    /**
     * Retrieves a list of all primary key constraint names in the database.
     * @return A list of primary key constraint names.
     * @throws DaoException if a database access error occurs.
     */
    public List<String> getAllPKConstraints() throws DaoException {
        String sql = """
                SELECT CONSTRAINT_NAME
                FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
                WHERE CONSTRAINT_TYPE = 'PRIMARY KEY'
                """;
        return fetchSingleColumn(sql, "CONSTRAINT_NAME");
    }


    /**
     * Retrieves a list of all check constraint names in the database.
     * @return A list of check constraint names.
     * @throws DaoException if a database access error occurs.
     */
    public List<String> getAllCheckConstraints() throws DaoException {
        String sql = """
                SELECT CONSTRAINT_NAME
                FROM INFORMATION_SCHEMA.CHECK_CONSTRAINTS
                """;
        return fetchSingleColumn(sql, "CONSTRAINT_NAME");
    }

    /**
     * Retrieves a list of column names from the 'Consultant' table that are not of the 'int' data type.
     * @return A list of non-integer column names from the 'Consultant' table.
     * @throws DaoException if a database access error occurs.
     */
    public List<String> getNonIntConsultantColumns() throws DaoException {
        String sql = """
                SELECT COLUMN_NAME
                FROM INFORMATION_SCHEMA.COLUMNS
                WHERE DATA_TYPE <> 'int'
                  AND TABLE_NAME = 'Consultant'
                """;
        return fetchSingleColumn(sql, "COLUMN_NAME");
    }

    /**
     * Finds the table with the most rows in the database and returns its name and row count.
     * @return A string containing the table name and its row count (e.g., "TableName (123 rows)"), or null if no tables are found.
     * @throws DaoException if a database access error occurs.
     */
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
