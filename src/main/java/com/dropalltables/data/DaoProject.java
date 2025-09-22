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

    public List<Project> getAllProjects() throws DaoException {
        List<Project> projects = new ArrayList<>();

        String query = "SELECT * FROM Project";

        try (Connection connection = connectionHandler.getConnection();
                PreparedStatement statement = connection.prepareStatement(query);
                ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Project project = instantiateProject(resultSet);
                projects.add(project);
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to retrieve projects from database", e);
        }
        return projects;
    }

    public Project getProjectByNo(int projectNo) throws DaoException {
        String query = "SELECT * FROM Project WHERE ProjectNo = ?";

        try (Connection connection = connectionHandler.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, projectNo);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return instantiateProject(resultSet);
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to retrieve project by ProjectNo: " + projectNo, e);
        }
        return null;
    }

    public void insertProject(Project project) throws DaoException {
        String insert = """
                INSERT INTO Project (ProjectNo, Name, StartDate)
                VALUES (?, ?, ?);
                """;

        String insertWithEnddate = """
                INSERT INTO Project (ProjectNo, Name, StartDate, EndDate)
                VALUES (?, ?, ?, ?);
                """;

        try (Connection connection = connectionHandler.getConnection()) {
            if (project.getEndDate() == null) {
                try (PreparedStatement statement = connection.prepareStatement(insert)) {
                    statement.setInt(1, project.getProjectNo());
                    statement.setString(2, project.getName());
                    statement.setDate(3, java.sql.Date.valueOf(project.getStartDate()));
                    statement.execute();
                }
            } else {
                try (PreparedStatement statement = connection.prepareStatement(insertWithEnddate)) {
                    statement.setInt(1, project.getProjectNo());
                    statement.setString(2, project.getName());
                    statement.setDate(3, java.sql.Date.valueOf(project.getStartDate()));
                    statement.setDate(4, java.sql.Date.valueOf(project.getEndDate()));
                    statement.execute();
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to insert project: " + project.getName() +
                    ". SQL Error: " + e.getMessage() + " (Error Code: " + e.getErrorCode() + ")", e);
        }
    }

    public boolean projectExists(int projectNo) throws DaoException {
        String query = "SELECT COUNT(*) FROM Project WHERE ProjectNo = ?";

        try (Connection connection = connectionHandler.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, projectNo);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to check if project exists with ProjectNo: " + projectNo, e);
        }
        return false;
    }

    public void deleteProject(int projectNo) throws DaoException {
        String query = "DELETE FROM Project WHERE ProjectNo = ?";

        try (Connection connection = connectionHandler.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, projectNo);
            int rowsAffected = statement.executeUpdate();

            if (rowsAffected == 0) {
                throw new DaoException("No project found with ProjectNo: " + projectNo);
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to delete project with ProjectNo: " + projectNo, e);
        }
    }

    private Project instantiateProject(ResultSet resultSet) throws DaoException {
        try {
            int projectNo = resultSet.getInt("ProjectNo");
            String name = resultSet.getString("Name");
            Date startDate = resultSet.getDate("StartDate");
            Date endDate = resultSet.getDate("EndDate");

            if (endDate != null) {
                return new Project(
                        projectNo,
                        name,
                        startDate.toLocalDate(),
                        endDate.toLocalDate());
            } else {
                return new Project(
                        projectNo,
                        name,
                        startDate.toLocalDate());
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to instantiate project from result set", e);
        }
    }
}
