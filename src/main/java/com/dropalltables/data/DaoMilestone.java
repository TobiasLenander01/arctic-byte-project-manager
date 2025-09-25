package com.dropalltables.data;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.dropalltables.models.Milestone;
import com.dropalltables.models.Project;

public class DaoMilestone {
    private final ConnectionHandler connectionHandler;

    public DaoMilestone() throws DaoException {
        try {
            this.connectionHandler = new ConnectionHandler();
        } catch (IOException e) {
            throw new DaoException("Unable to connect to the database. Please check your connection and try again.");
        }
    }

    public void insertMilestone(Milestone milestone) throws DaoException {
        String sql = "INSERT INTO Milestone (MilestoneNo, MilestoneName, MilestoneDate, ProjectID) VALUES (?, ?, ?, ?)";
        try (Connection conn = connectionHandler.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, milestone.getMilestoneNo());
            stmt.setString(2, milestone.getName());
            stmt.setTimestamp(3, Timestamp.valueOf(milestone.getDate().atStartOfDay()));
            
            // Use DaoProject.getProjectID() to convert ProjectNo to ProjectID
            DaoProject daoProject = new DaoProject();
            try {
                Integer projectID = daoProject.getProjectID(milestone.getProjectNo());
                if (projectID == null) {
                    throw new DaoException("Project not found. Please select a valid project.");
                }
                stmt.setInt(4, projectID);
            } catch (SQLException e) {
                throw new DaoException("Unable to find the selected project. Please try again.");
            }
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Unable to save milestone. Please check your input and try again.");
        }
    }

    public List<Milestone> getMilestonesByProject(int projectID) throws DaoException {
        List<Milestone> milestones = new ArrayList<>();
        String sql = "SELECT * FROM Milestone WHERE ProjectID = ? ORDER BY MilestoneDate";
        try (Connection conn = connectionHandler.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, projectID);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    milestones.add(instantiateMilestone(rs));
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Unable to load milestones. Please try again.");
        }
        return milestones;
    }

    public List<Milestone> getMilestonesByProjectNo(int projectNo) throws DaoException {
        List<Milestone> milestones = new ArrayList<>();
        String sql = "SELECT * FROM Milestone WHERE ProjectID = (SELECT ProjectID FROM Project WHERE ProjectNo = ?) ORDER BY MilestoneDate";
        try (Connection conn = connectionHandler.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, projectNo);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    milestones.add(instantiateMilestone(rs));
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Unable to load milestones for the selected project. Please try again.");
        }
        return milestones;
    }

    public int getMilestoneCountForProject(int projectID) throws DaoException {
        String sql = "SELECT COUNT(*) FROM Milestone WHERE ProjectID = ?";
        try (Connection conn = connectionHandler.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, projectID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Unable to count milestones. Please try again.");
        }
        return 0;
    }

    public int getMilestoneCountForProjectNo(int projectNo) throws DaoException {
        String sql = "SELECT COUNT(*) FROM Milestone WHERE ProjectID = (SELECT ProjectID FROM Project WHERE ProjectNo = ?)";
        try (Connection conn = connectionHandler.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, projectNo);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Unable to count milestones for the selected project. Please try again.");
        }
        return 0;
    }

    public void deleteMilestone(int milestoneNo) throws DaoException {
        String sql = "DELETE FROM Milestone WHERE MilestoneNo = ?";
        try (Connection conn = connectionHandler.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, milestoneNo);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Unable to delete milestone. Please try again.");
        }
    }

    public void deleteMilestonesByProject(int projectID) throws DaoException {
        String sql = "DELETE FROM Milestone WHERE ProjectID = ?";
           try (Connection conn = connectionHandler.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, projectID);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Unable to delete project milestones. Please try again.");
        }
    }

    public void deleteMilestonesByProjectNo(int projectNo) throws DaoException {
        String sql = "DELETE FROM Milestone WHERE ProjectID = (SELECT ProjectID FROM Project WHERE ProjectNo = ?)";
        try (Connection conn = connectionHandler.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, projectNo);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Unable to delete milestones for the selected project. Please try again.");
        }
    }

    public boolean milestoneNoExists(int milestoneNo) throws DaoException {
        String sql = "SELECT COUNT(*) FROM Milestone WHERE MilestoneNo = ?";
        try (Connection conn = connectionHandler.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, milestoneNo);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Unable to verify milestone number. Please try again.");
        }
        return false;
    }

    private Milestone instantiateMilestone(ResultSet rs) throws DaoException {
        try {
        int milestoneNo = rs.getInt("MilestoneNo");
        String name = rs.getString("MilestoneName");
        LocalDate date = rs.getTimestamp("MilestoneDate").toLocalDateTime().toLocalDate();
        int projectID = rs.getInt("ProjectID");

        DaoProject daoProject = new DaoProject();
        Project project = daoProject.getProjectByID(projectID);

        return new Milestone(milestoneNo, name, date, project);

        } catch (SQLException e) {
            throw new DaoException("Error loading milestone data. Please try again.");
        } 
    }
}
