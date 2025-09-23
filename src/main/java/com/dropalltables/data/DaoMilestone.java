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

    public DaoMilestone() throws IOException {
        this.connectionHandler = new ConnectionHandler();
    }

    public void addMilestone(Milestone milestone) throws SQLException, IOException {
        String sql = "INSERT INTO Milestone (MilestoneNo, MilestoneName, MilestoneDate, ProjectID) VALUES (?, ?, ?, ?)";
        try (Connection conn = connectionHandler.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, milestone.getMilestoneNo());
            stmt.setString(2, milestone.getName());
            stmt.setTimestamp(3, Timestamp.valueOf(milestone.getDate().atStartOfDay()));
            
            // Use DaoProject.getProjectId() to convert ProjectNo to ProjectID
            DaoProject daoProject = new DaoProject();
            try {
                Integer projectId = daoProject.getProjectId(milestone.getProjectNo());
                if (projectId == null) {
                    throw new SQLException("Project not found with ProjectNo: " + milestone.getProjectNo());
                }
                stmt.setInt(4, projectId);
            } catch (DaoException e) {
                throw new SQLException("Failed to get ProjectID for ProjectNo: " + milestone.getProjectNo(), e);
            }
            
            stmt.executeUpdate();
        }
    }

    public List<Milestone> getMilestonesByProject(int projectId) throws SQLException, IOException {
        List<Milestone> milestones = new ArrayList<>();
        String sql = "SELECT * FROM Milestone WHERE ProjectID = ? ORDER BY MilestoneDate";
        try (Connection conn = connectionHandler.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, projectId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    milestones.add(instantiateMilestone(rs));
                }
            }
        }
        return milestones;
    }

    public List<Milestone> getMilestonesByProjectNo(int projectNo) throws SQLException, IOException {
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
        }
        return milestones;
    }

    public int getMilestoneCountForProject(int projectId) throws SQLException, IOException {
        String sql = "SELECT COUNT(*) FROM Milestone WHERE ProjectID = ?";
        try (Connection conn = connectionHandler.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, projectId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    public int getMilestoneCountForProjectNo(int projectNo) throws SQLException, IOException {
        String sql = "SELECT COUNT(*) FROM Milestone WHERE ProjectID = (SELECT ProjectID FROM Project WHERE ProjectNo = ?)";
        try (Connection conn = connectionHandler.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, projectNo);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    public void deleteMilestone(int milestoneNo) throws SQLException, IOException {
        String sql = "DELETE FROM Milestone WHERE MilestoneNo = ?";
        try (Connection conn = connectionHandler.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, milestoneNo);
            stmt.executeUpdate();
        }
    }

    public void deleteMilestonesByProject(int projectId) throws SQLException, IOException {
        String sql = "DELETE FROM Milestone WHERE ProjectID = ?";
        try (Connection conn = connectionHandler.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, projectId);
            stmt.executeUpdate();
        }
    }

    public void deleteMilestonesByProjectNo(int projectNo) throws SQLException, IOException {
        String sql = "DELETE FROM Milestone WHERE ProjectID = (SELECT ProjectID FROM Project WHERE ProjectNo = ?)";
        try (Connection conn = connectionHandler.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, projectNo);
            stmt.executeUpdate();
        }
    }

    private Milestone instantiateMilestone(ResultSet rs) throws SQLException, IOException {
        int milestoneNo = rs.getInt("MilestoneNo");
        String name = rs.getString("MilestoneName");
        LocalDate date = rs.getTimestamp("MilestoneDate").toLocalDateTime().toLocalDate();
        int projectId = rs.getInt("ProjectID");

        DaoProject daoProject = new DaoProject();
        Project project;
        try {
            project = daoProject.getProjectById(projectId);
        } catch (DaoException e) {
            throw new IOException("Failed to fetch project for milestone", e);
        }

        return new Milestone(milestoneNo, name, date, project);
    }
}
