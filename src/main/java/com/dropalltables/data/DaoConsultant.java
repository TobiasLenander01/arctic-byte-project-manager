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

    public DaoConsultant() throws IOException {
        this.connectionHandler = new ConnectionHandler();
    }


    //metoder
    public List<Consultant> getAllConsultants() throws SQLException {
        List<Consultant> consultants = new ArrayList<>();
        String query = "SELECT * FROM Consultant";

        try (Connection connection = connectionHandler.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {              //while eftersom flera rader kan returneras
                Consultant consultant = instantiateConsultant(resultSet);
                consultants.add(consultant);
            }
        }
        return consultants;
    }

    public Consultant getConsultantByNo(int consultantNo) throws SQLException {
        String query = "SELECT * FROM Consultant WHERE consultantNo = ?";

        try (Connection connection = connectionHandler.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, consultantNo);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {         //if eftersom max en rad kan returneras
                return instantiateConsultant(resultSet);
            }
        }
        return null;
    }

    public Consultant instantiateConsultant(ResultSet resultSet) throws SQLException {
        return new Consultant(
            resultSet.getInt("consultantNo"),
            resultSet.getString("consultantName"),
            resultSet.getString("title")
        );
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
            throw new DaoException("duplicate: ConsultantNo " + consultant.getConsultantNo() + " is already occupied.", e);
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
            //om raden med oldConsultantNo inte existerar
            if (rows == 0) {
                throw new DaoException("not_found: " + oldConsultantNo);
            }
        } catch (SQLException e) {
            throw new DaoException("Database error: " + e.getMessage(), e);
        }
    }
    
///här logik för att även radera alla projectassignment med konsultent??
    public void deleteConsultant(int consultantNo) throws DaoException {
        String sql = "DELETE FROM Consultant WHERE ConsultantNo = ?";
        try (Connection connection = connectionHandler.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, consultantNo);
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new DaoException("not_found: " + consultantNo);
            }
        } catch (SQLException e) {
            throw new DaoException("Database error: " + e.getMessage(), e);
        }
    }



}

