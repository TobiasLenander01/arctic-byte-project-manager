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
        String sql = "INSERT INTO Milestone (Name, MilestoneDate, ProjectID) VALUES (?, ?, ?)";
        try (Connection conn = connectionHandler.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, milestone.getName());
            stmt.setTimestamp(2, Timestamp.valueOf(milestone.getDate().atStartOfDay()));
            stmt.setInt(3, milestone.getProjectId());
            stmt.executeUpdate();
        }
    }

    public List<Milestone> getAllMilestones() throws SQLException, IOException {
        List<Milestone> milestones = new ArrayList<>();
        String sql = "SELECT * FROM Milestone ORDER BY MilestoneDate";
        try (Connection conn = connectionHandler.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                milestones.add(instantiateMilestone(rs));
            }
        }
        return milestones;
    }

    public Milestone getMilestoneById(int milestoneId) throws SQLException, IOException {
        String sql = "SELECT * FROM Milestone WHERE MilestoneID = ?";
        try (Connection conn = connectionHandler.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, milestoneId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return instantiateMilestone(rs);
                }
            }
        }
        return null;
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

    public void deleteMilestone(int milestoneId) throws SQLException, IOException {
        String sql = "DELETE FROM Milestone WHERE MilestoneID = ?";
        try (Connection conn = connectionHandler.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, milestoneId);
            stmt.executeUpdate();
        }
    }
    private Milestone instantiateMilestone(ResultSet rs) throws SQLException, IOException {
        int milestoneId = rs.getInt("MilestoneID");
        String name = rs.getString("Name");
        LocalDate date = rs.getTimestamp("MilestoneDate").toLocalDateTime().toLocalDate();
        int projectId = rs.getInt("ProjectID");

        DaoProject daoProject = new DaoProject();
        Project project = daoProject.getProjectById(projectId);

        return new Milestone(milestoneId, name, date, project);
    }
}
