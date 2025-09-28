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

    /**
     * Constructor for DaoMilestone.
     * Initializes the ConnectionHandler.
     * 
     * @throws DaoException if unable to connect to the database.
     */
    public DaoMilestone() throws DaoException {
        try {
            this.connectionHandler = new ConnectionHandler();
        } catch (IOException e) {
            throw new DaoException("Unable to connect to the database. Please check your connection and try again.");
        }
    }

    /**
     * Inserts a new milestone into the database.
     * It converts the project number from the Milestone object into a project ID
     * before insertion.
     * 
     * @param milestone The milestone object to insert.
     * @throws DaoException if the project is not found or if there is an error
     *                      during insertion.
     */
    public void insertMilestone(Milestone milestone) throws DaoException {
        String sql = """
                INSERT INTO Milestone (MilestoneNo, MilestoneName, MilestoneDate, ProjectID)
                VALUES (?, ?, ?, ?)
                """;
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

    /**
     * Retrieves a list of all milestones for a specific project, ordered by date.
     * 
     * @param projectNo The number of the project whose milestones are to be
     *                  retrieved.
     * @return A list of milestones for the specified project.
     * @throws DaoException if there is an error loading the milestones.
     */
    public List<Milestone> getMilestonesByProjectNo(int projectNo) throws DaoException {
        List<Milestone> milestones = new ArrayList<>();
        String sql = """
                SELECT *
                FROM Milestone
                WHERE ProjectID = (SELECT ProjectID FROM Project WHERE ProjectNo = ?)
                ORDER BY MilestoneDate
                """;
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

    /**
     * Deletes a specific milestone from the database by its number.
     * 
     * @param milestoneNo The number of the milestone to delete.
     * @throws DaoException if there is an error during deletion.
     */
    public void deleteMilestone(int milestoneNo) throws DaoException {
        String sql = """
                DELETE FROM Milestone
                WHERE MilestoneNo = ?
                """;
        try (Connection conn = connectionHandler.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, milestoneNo);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Unable to delete milestone. Please try again.");
        }
    }

    /**
     * Deletes all milestones associated with a specific project.
     * 
     * @param projectNo The number of the project for which to delete all
     *                  milestones.
     * @throws DaoException if there is an error during deletion.
     */
    public void deleteMilestonesByProjectNo(int projectNo) throws DaoException {
        String sql = """
                DELETE FROM Milestone
                WHERE ProjectID = (SELECT ProjectID FROM Project WHERE ProjectNo = ?)
                """;
        try (Connection conn = connectionHandler.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, projectNo);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Unable to delete milestones for the selected project. Please try again.");
        }
    }

    /**
     * Checks if a milestone with a specific number already exists in the database.
     * 
     * @param milestoneNo The milestone number to check.
     * @return true if the milestone number exists, false otherwise.
     * @throws DaoException if there is an error during the check.
     */
    public boolean milestoneNoExists(int milestoneNo) throws DaoException {
        String sql = """
                SELECT COUNT(*)
                FROM Milestone
                WHERE MilestoneNo = ?
                """;
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

    /**
     * Helper method to create a Milestone object from a ResultSet.
     * This involves fetching the associated Project object.
     * 
     * @param rs The ResultSet containing milestone data.
     * @return A new Milestone object.
     * @throws DaoException if there is an error reading the ResultSet or fetching
     *                      the associated project.
     */
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

    public int updateMilestone(Milestone m) throws DaoException {
        if (m == null || m.getMilestoneNo() <= 0)
            throw new IllegalArgumentException("Invalid milestone");
        String sql = """
                UPDATE Milestone
                SET MilestoneName = ?, MilestoneDate = ?
                WHERE MilestoneID = ?
                """;
        try (Connection c = connectionHandler.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, m.getName());
            ps.setDate(2, java.sql.Date.valueOf(m.getDate()));
            ps.setInt(3, m.getMilestoneNo());
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Failed to update milestone: " + e.getMessage(), e);
        }
    }
}
