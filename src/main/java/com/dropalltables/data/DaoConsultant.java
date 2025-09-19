package com.dropalltables.data;
import java.sql.Connection;
import java.sql.SQLException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.ArrayList;
import com.dropalltables.models.Consultant;

public class DaoConsultant {

    private ConnectionHandler connectionHandler;

    public DaoConsultant() throws IOException {
        this.connectionHandler = new ConnectionHandler();
    }


    //metoder
    public List<Consultant> findAllConsultants() throws SQLException {
        List<Consultant> consultants = new ArrayList<>();
        String query = "SELECT * FROM Consultant";

        try (Connection connection = connectionHandler.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {              //while eftersom flera rader kan returneras
                Consultant consultant = consultantFindMethod(resultSet);
                consultants.add(consultant);
            }
        }
        return consultants;
    }

    public Consultant findConsultantById(int consultantId) throws SQLException {
        String query = "SELECT * FROM Consultant WHERE consultantId = ?";

        try (Connection connection = connectionHandler.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, consultantId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {         //if eftersom max en rad kan returneras
                return consultantFindMethod(resultSet);
            }
        }
        return null;
    }

    public Consultant consultantFindMethod(ResultSet resultSet) throws SQLException {
        return new Consultant(
            resultSet.getInt("consultantId"),
            resultSet.getInt("consultantNo"),
            resultSet.getString("consultantName"),
            resultSet.getString("title")
        );
    }



    public void insertConsultant(Consultant consultant) throws SQLException {
        String sql = "INSERT INTO Consultant (consultantNo, consultantName, title) VALUES (?, ?, ?)";
        try (Connection connection = connectionHandler.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, consultant.getConsultantNo());
            statement.setString(2, consultant.getConsultantName());
            statement.setString(3, consultant.getTitle());
            statement.executeUpdate();
        }
    }

    public void updateConsultant(Consultant consultant) {

    }

    public void deleteConsultant(int consultantId) { //här logik för att även radera alla projectassignment med konsultent

    }



}

