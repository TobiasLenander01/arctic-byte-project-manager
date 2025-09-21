package com.dropalltables.data;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.dropalltables.models.Project;

public class DaoProject {
    private final ConnectionHandler connectionHandler;

    public DaoProject() throws IOException {
        this.connectionHandler = new ConnectionHandler();
    }

    public List<Project> getAllProjects() throws SQLException {
        List<Project> projects = new ArrayList<>();

        String query = "SELECT * FROM Project";

        try (Connection connection = connectionHandler.getConnection();
                PreparedStatement statement = connection.prepareStatement(query);
                ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Project project = instantiateProject(resultSet);
                projects.add(project);
            }
        }
        return projects;
    }

    public Project getProjectById(int consultantId) throws SQLException {
        String query = "SELECT * FROM Project WHERE ProjectID = ?";

        try (Connection connection = connectionHandler.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, consultantId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return instantiateProject(resultSet);
            }
        }
        return null;
    }

    private Project instantiateProject(ResultSet resultSet) throws SQLException {
        int projectNo = resultSet.getInt("ProjectNo");
        String name = resultSet.getString("Name");
        Date startDate = resultSet.getDate("StartDate");
        Date endDate = resultSet.getDate("EndDate");

        if (endDate != null) {
            return new Project(
                projectNo,
                name,
                startDate.toLocalDate(),
                endDate.toLocalDate()
            );
        }
        else {
            return new Project(
                projectNo, 
                name, 
                startDate.toLocalDate()
            );
        }
    }
}
