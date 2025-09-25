package com.dropalltables.data;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.dropalltables.models.Project;

public class DaoProject {
    private final ConnectionHandler connectionHandler;

    public DaoProject() throws DaoException {
        try {
            this.connectionHandler = new ConnectionHandler();
        } catch (IOException e) {
            throw new DaoException("Unable to connect to the database. Please check your connection and try again.");
        }
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
            throw new DaoException("Unable to load projects. Please try again.");
        }
        return projects;
    }

    public Project getProjectByNo(int projectNo) throws DaoException {
        if (projectNo <= 0) {
            throw new IllegalArgumentException("ProjectNo must be positive");
        }

        String query = "SELECT * FROM Project WHERE ProjectNo = ?";

        try (Connection connection = connectionHandler.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, projectNo);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return instantiateProject(resultSet);
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Unable to find project with number: " + projectNo);
        }
        return null;
    }

    public Project getProjectByID(int projectID) throws DaoException {
        if (projectID <= 0) {
            throw new IllegalArgumentException("ProjectID must be positive");
        }

        String query = "SELECT * FROM Project WHERE ProjectID = ?";

        try (Connection connection = connectionHandler.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, projectID);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return instantiateProject(resultSet);
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Unable to find the requested project. Please try again.");
        }
        return null;
    }

    public void insertProject(Project project) throws DaoException {
        if (project == null) {
            throw new IllegalArgumentException("Project cannot be null");
        }
        if (project.getName() == null || project.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Project name cannot be null or empty");
        }
        if (project.getStartDate() == null) {
            throw new IllegalArgumentException("Start date cannot be null");
        }
        if (project.getProjectNo() <= 0) {
            throw new IllegalArgumentException("ProjectNo must be positive");
        }

        String insert = """
                INSERT INTO Project (ProjectNo, ProjectName, StartDate, EndDate)
                VALUES (?, ?, ?, ?);
                """;

        try (Connection connection = connectionHandler.getConnection();
                PreparedStatement statement = connection.prepareStatement(insert)) {

            statement.setInt(1, project.getProjectNo());
            statement.setString(2, project.getName());
            statement.setDate(3, java.sql.Date.valueOf(project.getStartDate()));
            statement.setDate(4, project.getEndDate() != null ? java.sql.Date.valueOf(project.getEndDate()) : null);
            statement.executeUpdate();

        } catch (SQLException e) {
            if (e.getMessage().contains("duplicate")) {
                throw new DaoException("A project with number " + project.getProjectNo() + " already exists.");
            }
            throw new DaoException("Unable to save the project. Please check your input and try again.");
        }
    }

    public Integer getProjectID(int projectNo) throws DaoException {
        if (projectNo <= 0) {
            throw new IllegalArgumentException("ProjectNo must be positive");
        }

        String query = "SELECT ProjectID FROM Project WHERE ProjectNo = ?";

        try (Connection connection = connectionHandler.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, projectNo);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("ProjectID");
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Unable to find project with number: " + projectNo);
        }

        return null;
    }

    public void deleteProject(int projectNo) throws DaoException {
        if (projectNo <= 0) {
            throw new IllegalArgumentException("ProjectNo must be positive");
        }

        try {
            // Delete all milestones associated with this project
            DaoMilestone daoMilestone = new DaoMilestone();
            daoMilestone.deleteMilestonesByProjectNo(projectNo);

            // Delete all assignments associated with this project
            DaoProjectAssignment daoAssignment = new DaoProjectAssignment();
            int projectID = getProjectID(projectNo);
            daoAssignment.deleteProjectAssignmentByProjectID(projectID);

            // Then delete the project
            String query = "DELETE FROM Project WHERE ProjectNo = ?";
            try (Connection connection = connectionHandler.getConnection();
                    PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, projectNo);
                int rowsAffected = statement.executeUpdate();

                if (rowsAffected == 0) {
                    throw new DaoException("Project not found. It may have already been deleted.");
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Unable to delete the project. Please try again.");
        }
    }

    public void updateProject(Project project) throws DaoException {
        if (project == null) {
            throw new IllegalArgumentException("Project cannot be null");
        }
        if (project.getName() == null || project.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Project name cannot be null or empty");
        }
        if (project.getStartDate() == null) {
            throw new IllegalArgumentException("Start date cannot be null");
        }
        if (project.getProjectNo() <= 0) {
            throw new IllegalArgumentException("ProjectNo must be positive");
        }

        String query = "UPDATE Project SET ProjectName = ?, StartDate = ?, EndDate = ? WHERE ProjectNo = ?";

        try (Connection connection = connectionHandler.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, project.getName());
            statement.setDate(2, java.sql.Date.valueOf(project.getStartDate()));
            statement.setDate(3, project.getEndDate() != null ? java.sql.Date.valueOf(project.getEndDate()) : null);
            statement.setInt(4, project.getProjectNo());

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected == 0) {
                throw new DaoException("Project not found. It may have been deleted by another user.");
            }
        } catch (SQLException e) {
            throw new DaoException("Unable to update the project. Please check your input and try again.");
        }
    }

    private Project instantiateProject(ResultSet resultSet) throws DaoException {
        try {
            int projectNo = resultSet.getInt("ProjectNo");
            String projectName = resultSet.getString("ProjectName");
            Date startDate = resultSet.getDate("StartDate");
            Date endDate = resultSet.getDate("EndDate");

            LocalDate startLocalDate = startDate != null ? startDate.toLocalDate() : null;
            LocalDate endLocalDate = endDate != null ? endDate.toLocalDate() : null;

            if (endLocalDate != null) {
                return new Project(
                        projectNo,
                        projectName,
                        startLocalDate,
                        endLocalDate);
            } else {
                return new Project(
                        projectNo,
                        projectName,
                        startLocalDate);
            }
        } catch (SQLException e) {
            throw new DaoException("Error loading project data. Please try again.");
        }
    }
}
