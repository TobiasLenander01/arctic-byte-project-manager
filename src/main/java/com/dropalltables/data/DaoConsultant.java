package com.dropalltables.data;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.dropalltables.models.Consultant;

public class DaoConsultant {

    private final ConnectionHandler connectionHandler;

    /**
     * Constructor for DaoConsultant.
     * Initializes the ConnectionHandler.
     * 
     * @throws DaoException if unable to connect to the database.
     */
    public DaoConsultant() throws DaoException {
        try {
            this.connectionHandler = new ConnectionHandler();
        } catch (IOException e) {
            throw new DaoException("Unable to connect to the database. Please check your connection and try again.");
        }
    }

    /**
     * Retrieves a list of all consultants from the database.
     * 
     * @return A list of all consultants.
     * @throws DaoException if there is an error loading the consultants.
     */
    public List<Consultant> getAllConsultants() throws DaoException {
        List<Consultant> consultants = new ArrayList<>();
        String query = """
                SELECT *
                FROM Consultant
                """;

        try (Connection connection = connectionHandler.getConnection();
                PreparedStatement statement = connection.prepareStatement(query);
                ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) { // while eftersom flera rader kan returneras
                Consultant consultant = instantiateConsultant(resultSet);
                consultants.add(consultant);
            }
        } catch (SQLException e) {
            throw new DaoException("Unable to load consultants. Please try again.");
        }
        return consultants;
    }

    /**
     * Retrieves a consultant by their consultant number.
     * 
     * @param consultantNo The number of the consultant to retrieve.
     * @return The consultant object, or null if not found.
     * @throws DaoException if there is an error finding the consultant.
     */
    public Consultant getConsultantByNo(int consultantNo) throws DaoException {
        String query = """
                SELECT *
                FROM Consultant
                WHERE consultantNo = ?
                """;

        try (Connection connection = connectionHandler.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, consultantNo);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) { // if eftersom max en rad kan returneras
                return instantiateConsultant(resultSet);
            }
        } catch (SQLException e) {
            throw new DaoException("Unable to find consultant with number: " + consultantNo);
        }
        return null;
    }

    /**
     * Retrieves a consultant by their internal database ID.
     * 
     * @param consultantID The ID of the consultant to retrieve.
     * @return The consultant object, or null if not found.
     * @throws DaoException if there is an error finding the consultant.
     */
    public Consultant getConsultantByID(int consultantID) throws DaoException {
        String query = """
                SELECT *
                FROM Consultant
                WHERE consultantID = ?
                """;

        try (Connection connection = connectionHandler.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, consultantID);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) { // if eftersom max en rad kan returneras
                return instantiateConsultant(resultSet);
            }
        } catch (SQLException e) {
            throw new DaoException("Unable to find the requested consultant. Please try again.");
        }
        return null;
    }

    /**
     * Helper method to create a Consultant object from a ResultSet.
     * 
     * @param resultSet The ResultSet containing consultant data.
     * @return A new Consultant object.
     * @throws SQLException if there is an error reading the ResultSet.
     */
    public Consultant instantiateConsultant(ResultSet resultSet) throws SQLException {
        return new Consultant(
                resultSet.getInt("consultantNo"),
                resultSet.getString("consultantName"),
                resultSet.getString("title"));
    }

    /**
     * Inserts a new consultant into the database.
     * 
     * @param consultant The consultant object to insert.
     * @throws DaoException if a consultant with the same number already exists or
     *                      if there is an error during insertion.
     */
    public void insertConsultant(Consultant consultant) throws DaoException {
        String sql = """
                INSERT INTO Consultant (ConsultantNo, ConsultantName, Title)
                VALUES (?, ?, ?)
                """;
        try (Connection connection = connectionHandler.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, consultant.getConsultantNo());
            statement.setString(2, consultant.getName());
            statement.setString(3, consultant.getTitle());
            statement.executeUpdate();
        } catch (SQLException e) {
            if (e.getMessage().contains("duplicate")) {
                throw new DaoException("A consultant with that number already exists.");
            }
            throw new DaoException("Unable to insert consultant. Please try again.");
        }
    }

    /**
     * Updates an existing consultant's information.
     * 
     * @param oldConsultantNo The current number of the consultant to be updated.
     * @param newConsultant   A consultant object containing the new information.
     * @throws DaoException if the consultant is not found or if there is an error
     *                      during the update.
     */
    public void updateConsultant(int oldConsultantNo, Consultant newConsultant) throws DaoException {
        String sql = """
                UPDATE Consultant
                SET ConsultantNo = ?, ConsultantName = ?, Title = ?
                WHERE ConsultantNo = ?
                """;
        try (Connection connection = connectionHandler.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, newConsultant.getConsultantNo());
            statement.setString(2, newConsultant.getName());
            statement.setString(3, newConsultant.getTitle());
            statement.setInt(4, oldConsultantNo);
            int rows = statement.executeUpdate();
            // om raden med oldConsultantNo inte existerar
            if (rows == 0) {
                throw new DaoException("Consultant not found. It may have been deleted by another user.");
            }
        } catch (SQLException e) {
            throw new DaoException("Unable to update consultant. Please check your input and try again.");
        }
    }

    /**
     * Retrieves the internal database ID of a consultant by their consultant
     * number.
     * 
     * @param consultantNo The number of the consultant.
     * @return The internal database ID of the consultant.
     * @throws DaoException if the consultant is not found or if there is an error.
     */
    public Integer getConsultantID(int consultantNo) throws DaoException {
        String sql = """
                SELECT ConsultantID
                FROM Consultant
                WHERE ConsultantNo = ?
                """;
        try (Connection connection = connectionHandler.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, consultantNo);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("ConsultantID");
                } else {
                    throw new DaoException("Consultant not found with number: " + consultantNo);
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Unable to find consultant information. Please try again.");
        }
    }

    /**
     * Deletes a consultant from the database, including all their project
     * assignments.
     * 
     * @param consultantNo The number of the consultant to delete.
     * @throws DaoException if the consultant is not found or if there is an error
     *                      during deletion.
     */
    public void deleteConsultant(int consultantNo) throws DaoException {
        try {
            int foundConsultantID = getConsultantID(consultantNo);
            DaoProjectAssignment daoPA = new DaoProjectAssignment();
            daoPA.deleteProjectAssignmentByConsultantID(foundConsultantID);

            String sql = """
                    DELETE FROM Consultant
                    WHERE ConsultantNo = ?
                    """;
            try (Connection connection = connectionHandler.getConnection();
                    PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, consultantNo);
                int rowsAffected = statement.executeUpdate();
                if (rowsAffected == 0) {
                    throw new DaoException("Consultant not found. It may have already been deleted.");
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Unable to delete consultant. Please try again.");
        }
    }

    /**
     * Retrieves a list of all consultants who are not assigned to a specific
     * project.
     * 
     * @param projectID The ID of the project to check against.
     * @return A list of consultants not in the specified project.
     * @throws DaoException if there is an error loading the consultants.
     */
    public List<Consultant> getConsultantsNotInProject(int projectID) throws DaoException {
        String sql = """
                SELECT c.*
                FROM Consultant c
                WHERE c.ConsultantID NOT IN (
                    SELECT pa.ConsultantID
                    FROM Project_Assignment pa
                    WHERE pa.ProjectID = ?

                )
                ORDER BY ConsultantName
                """;

        List<Consultant> consultants = new ArrayList<>();
        try (Connection conn = connectionHandler.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, projectID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    consultants.add(instantiateConsultant(rs));
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Unable to load available consultants. Please try again.");
        }
        return consultants;
    }

    public List<Consultant> getAllWithProjectCount() throws DaoException {
        String sql = """
                SELECT
                    c.ConsultantNo,
                    c.ConsultantName,
                    c.Title,
                    COUNT(DISTINCT pa.ProjectID) AS ProjectCount
                FROM Consultant c
                LEFT JOIN Project_Assignment pa
                       ON pa.ConsultantID = c.ConsultantID
                GROUP BY c.ConsultantNo, c.ConsultantName, c.Title
                ORDER BY c.ConsultantNo
                """;

        List<Consultant> list = new ArrayList<>();

        try (Connection con = connectionHandler.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                // build the Consultant with the three constructor fields
                Consultant c = new Consultant(
                        rs.getInt("ConsultantNo"),
                        rs.getString("ConsultantName"),
                        rs.getString("Title"));
                // set the projectCount from the query
                c.setProjectCount(rs.getInt("ProjectCount"));

                list.add(c);
            }

        } catch (SQLException e) {
            throw new DaoException("Failed to load consultants with project count", e);
        }

        return list;
    }

}
