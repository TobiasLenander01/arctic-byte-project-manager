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

    public DaoConsultant() throws DaoException {
        try {
            this.connectionHandler = new ConnectionHandler();
        } catch (IOException e) {
            throw new DaoException("Failed to initialize ConnectionHandler: " + e.getMessage(), e);
        }
    }

    // metoder
    public List<Consultant> getAllConsultants() throws DaoException {
        List<Consultant> consultants = new ArrayList<>();
        String query = "SELECT * FROM Consultant";

        try (Connection connection = connectionHandler.getConnection();
                PreparedStatement statement = connection.prepareStatement(query);
                ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) { // while eftersom flera rader kan returneras
                Consultant consultant = instantiateConsultant(resultSet);
                consultants.add(consultant);
            }
        } catch (SQLException e) {
            throw new DaoException("failed to retrieve all consultants", e);
        }
        return consultants;
    }

    public Consultant getConsultantByNo(int consultantNo) throws DaoException {
        String query = "SELECT * FROM Consultant WHERE consultantNo = ?";

        try (Connection connection = connectionHandler.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, consultantNo);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) { // if eftersom max en rad kan returneras
                return instantiateConsultant(resultSet);
            }
        } catch (SQLException e) {
            throw new DaoException("not_found ConsultantNo: " + consultantNo, e);
        }
        return null;
    }

    public Consultant getConsultantByID(int consultantID) throws DaoException {
        String query = "SELECT * FROM Consultant WHERE consultantID = ?";

        try (Connection connection = connectionHandler.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, consultantID);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) { // if eftersom max en rad kan returneras
                return instantiateConsultant(resultSet);
            }
        } catch (SQLException e) {
            throw new DaoException("not_found ConsultantID: " + consultantID, e);
        }
        return null;
    }

    public Consultant instantiateConsultant(ResultSet resultSet) throws SQLException {
        return new Consultant(
                resultSet.getInt("consultantNo"),
                resultSet.getString("consultantName"),
                resultSet.getString("title"));
    }

    public void insertConsultant(Consultant consultant) throws DaoException {
        String sql = "INSERT INTO Consultant (ConsultantNo, ConsultantName, Title) VALUES (?, ?, ?)";
        try (Connection connection = connectionHandler.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, consultant.getConsultantNo());
            statement.setString(2, consultant.getName());
            statement.setString(3, consultant.getTitle());
            statement.executeUpdate();
        } catch (SQLException e) {
            // consultNo redan taget
            throw new DaoException("duplicate: ConsultantNo " + consultant.getConsultantNo(), e);
        }
    }

    /**
     * behöver tre variabler gamla för WHERE och alla tre nya värden.
     * I controller fixa att autofilla de gamla värdena i texfält
     * updatering sker oavsett om nya värden är samma som gamla
     * skulle eventuallt kunna göra med consultant object som argument
     * m.a.o updateConsultant(int oldConsultantNo, Consultant newConsultant)
     */
    public void updateConsultant(int oldConsultantNo, Consultant newConsultant) throws DaoException {
        String sql = "UPDATE Consultant SET ConsultantNo = ?, ConsultantName = ?, Title = ? WHERE ConsultantNo = ?";
        try (Connection connection = connectionHandler.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, newConsultant.getConsultantNo());
            statement.setString(2, newConsultant.getName());
            statement.setString(3, newConsultant.getTitle());
            statement.setInt(4, oldConsultantNo);
            int rows = statement.executeUpdate();
            // om raden med oldConsultantNo inte existerar
            if (rows == 0) {
                throw new DaoException("not_found: " + oldConsultantNo);
            }
        } catch (SQLException e) {
            throw new DaoException("Database error: " + e.getMessage(), e);
        }
    }

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
                    throw new DaoException("not_found: " + consultantNo);
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Database error: " + e.getMessage(), e);
        }
    }

    /// här logik för att även radera alla projectassignment med konsultent??
    public void deleteConsultant(int consultantNo) throws DaoException {
        try {
            int foundConsultantID = getConsultantID(consultantNo);
            DaoProjectAssignment daoPA = new DaoProjectAssignment();
            daoPA.deleteProjectAssignmentByConsultantID(foundConsultantID);

            String sql = "DELETE FROM Consultant WHERE ConsultantNo = ?";
            try (Connection connection = connectionHandler.getConnection();
                    PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, consultantNo);
                int rowsAffected = statement.executeUpdate();
                if (rowsAffected == 0) {
                    throw new DaoException("not_found: " + consultantNo);
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Database error: " + e.getMessage(), e);
        }
    }

    // returns list of all consultants NOT assigned to a specific project
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
            throw new DaoException("Failed to retrieve consultants not in project " + projectID, e);
        }
        return consultants;
    }
}
