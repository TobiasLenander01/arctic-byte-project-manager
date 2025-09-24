package com.dropalltables.data;

import java.io.IOException;
import java.sql.*;
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
            throw new DaoException("Failed to initialize ConnectionHandler: " + e.getMessage(), e);
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
                    throw new DaoException("not_found ProjectNo: " + milestone.getProjectNo());
                }
                stmt.setInt(4, projectID);
            } catch (SQLException e) {
                throw new DaoException("Failed to get ProjectID for ProjectNo: " + milestone.getProjectNo(), e);
            }
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Failed to insert milestone: " + milestone, e);
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
            throw new DaoException("Failed to retrieve milestones for ProjectID: " + projectID, e);
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
            throw new DaoException("Failed to retrieve milestones for ProjectNo: " + projectNo, e);
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
            throw new DaoException("Failed to count milestones for ProjectID: " + projectID, e);
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
            throw new DaoException("Failed to count milestones for ProjectNo: " + projectNo, e);
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
            throw new DaoException("Failed to delete " + milestoneNo, e);
        }
    }

    public void deleteMilestonesByProject(int projectID) throws DaoException {
        String sql = "DELETE FROM Milestone WHERE ProjectID = ?";
        try (Connection conn = connectionHandler.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, projectID);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Failed to delete milestones for ProjectID: " + projectID, e);
        }
    }

    public void deleteMilestonesByProjectNo(int projectNo) throws DaoException {
        String sql = "DELETE FROM Milestone WHERE ProjectID = (SELECT ProjectID FROM Project WHERE ProjectNo = ?)";
        try (Connection conn = connectionHandler.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, projectNo);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Failed to delete milestones for ProjectNo: " + projectNo, e);
        }
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
            throw new DaoException("Failed to map Milestone from ResultSet", e);
        } 
    }
}
