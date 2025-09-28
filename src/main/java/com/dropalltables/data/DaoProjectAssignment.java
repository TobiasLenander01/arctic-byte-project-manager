package com.dropalltables.data;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.dropalltables.models.ProjectAssignment;

public class DaoProjectAssignment {
    private ConnectionHandler connectionHandler;

    @FunctionalInterface
    private interface Binder {
        void bind(PreparedStatement ps) throws SQLException;
    }

    /**
     * A private helper method to execute update, insert, or delete SQL statements.
     * This reduces boilerplate code for database write operations.
     * 
     * @param sql    The SQL statement to execute.
     * @param binder A lambda expression to bind parameters to the
     *               PreparedStatement.
     * @return The number of rows affected by the operation.
     * @throws DaoException if a database access error occurs.
     */
    private int execUpdate(String sql, Binder binder) throws DaoException {
        try (Connection c = connectionHandler.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            binder.bind(ps);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Unable to save changes. Please try again.");
        }
    }

    /**
     * Constructor for DaoProjectAssignment.
     * Initializes the ConnectionHandler.
     * 
     * @throws DaoException if unable to connect to the database.
     */
    public DaoProjectAssignment() throws DaoException {
        try {
            this.connectionHandler = new ConnectionHandler();
        } catch (IOException e) {
            throw new DaoException("Unable to connect to the database. Please check your connection and try again.");
        }
    }

    /**
     * Helper method to create a ProjectAssignment object from a ResultSet.
     * 
     * @param rs The ResultSet containing project assignment data.
     * @return A new ProjectAssignment object.
     * @throws SQLException if there is an error reading the ResultSet.
     */
    private ProjectAssignment instantiateProjectAssignment(ResultSet rs) throws SQLException {
        return new ProjectAssignment(
                rs.getInt("ConsultantID"),
                rs.getInt("ProjectID"),
                rs.getInt("HoursWorked"));
    }

    /**
     * Finds and returns a specific project assignment based on consultant and
     * project IDs.
     * 
     * @param consultantID The ID of the consultant.
     * @param projectID    The ID of the project.
     * @return The ProjectAssignment object if found, otherwise null.
     * @throws DaoException if a database access error occurs.
     */
    private ProjectAssignment findProjectAssignment(int consultantID, int projectID) throws DaoException {
        String sql = """
                SELECT ConsultantID, ProjectID, HoursWorked
                FROM Project_Assignment
                WHERE ConsultantID = ?
                  AND ProjectID = ?
                """;

        try (Connection c = connectionHandler.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, consultantID);
            ps.setInt(2, projectID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return instantiateProjectAssignment(rs);
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Unable to find the assignment. Please try again.");
        }
        return null;
    }

    /**
     * Assigns a consultant to a project with an initial 0 hours worked.
     * 
     * @param consultantID The ID of the consultant to assign.
     * @param projectID    The ID of the project to assign the consultant to.
     * @return The number of rows affected (should be 1 on success).
     * @throws DaoException if a database access error occurs.
     */
    public int insertProjectAssignment(int consultantID, int projectID) throws DaoException {
        String sql = """
                INSERT INTO Project_Assignment (ConsultantID, ProjectID, HoursWorked)
                VALUES (?, ?, 0)
                """;
        return execUpdate(sql, ps -> {
            ps.setInt(1, consultantID);
            ps.setInt(2, projectID);
        });
    }

    /**
     * Updates the number of hours worked by a consultant on a specific project.
     * 
     * @param consultantID The ID of the consultant.
     * @param projectID    The ID of the project.
     * @param hours        The new total hours worked.
     * @return The number of rows affected (should be 1 on success).
     * @throws DaoException if the assignment is not found or if a database error
     *                      occurs.
     */
    public int updateHours(int consultantID, int projectID, int hours) throws DaoException {
        ProjectAssignment pa = findProjectAssignment(consultantID, projectID);

        if (pa == null) {
            throw new DaoException("Assignment not found. The consultant may not be assigned to this project.");
        }

        pa.setHoursWorked(hours);

        String sql = """
                UPDATE Project_Assignment
                SET HoursWorked = ?
                WHERE ConsultantID = ?
                AND ProjectID = ?
                """;

        return execUpdate(sql, ps -> {
            ps.setInt(1, pa.getHoursWorked());
            ps.setInt(2, pa.getConsultantID());
            ps.setInt(3, pa.getProjectID());
        });
    }

    /**
     * Deletes a specific project assignment.
     * 
     * @param consultantID The ID of the consultant in the assignment.
     * @param projectID    The ID of the project in the assignment.
     * @return The number of rows affected.
     * @throws DaoException if a database access error occurs.
     */
    public int deleteProjectAssignment(int consultantID, int projectID) throws DaoException {
        String sql = """
                DELETE FROM Project_Assignment
                WHERE ConsultantID = ?
                AND ProjectID = ?
                """;
        return execUpdate(sql, ps -> {
            ps.setInt(1, consultantID);
            ps.setInt(2, projectID);
        });
    }

    /**
     * Deletes all project assignments for a specific consultant.
     * 
     * @param consultantID The ID of the consultant whose assignments are to be
     *                     deleted.
     * @return The number of rows affected.
     * @throws DaoException if a database access error occurs.
     */
    public int deleteProjectAssignmentByConsultantID(int consultantID) throws DaoException {
        String sql = """
                DELETE FROM Project_Assignment
                WHERE ConsultantID = ?
                """;
        return execUpdate(sql, ps -> {
            ps.setInt(1, consultantID);
        });
    }

    /**
     * Deletes all project assignments for a specific project.
     * 
     * @param projectID The ID of the project whose assignments are to be deleted.
     * @return The number of rows affected.
     * @throws DaoException if a database access error occurs.
     */
    public int deleteProjectAssignmentByProjectID(int projectID) throws DaoException {
        String sql = """
                DELETE FROM Project_Assignment
                WHERE ProjectID = ?
                """;
        return execUpdate(sql, ps -> {
            ps.setInt(1, projectID);
        });
    }

    /**
     * Retrieves all project assignments for a specific project.
     * 
     * @param projectID The ID of the project.
     * @return A list of project assignments.
     * @throws DaoException if a database access error occurs.
     */
    public List<ProjectAssignment> getByProjectID(int projectID) throws DaoException {
        List<ProjectAssignment> list = new ArrayList<>();
        String sql = """
                SELECT ConsultantID, ProjectID, HoursWorked
                FROM Project_Assignment
                WHERE ProjectID = ?
                """;
        try (Connection c = connectionHandler.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, projectID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(instantiateProjectAssignment(rs));
            }
        } catch (SQLException e) {
            throw new DaoException("Unable to load project assignments. Please try again.");
        }
        return list;
    }

    /**
     * Retrieves all project assignments for a specific consultant.
     * 
     * @param consultantID The ID of the consultant.
     * @return A list of project assignments.
     * @throws DaoException if a database access error occurs.
     */
    public List<ProjectAssignment> getByConsultantID(int consultantID) throws DaoException {
        List<ProjectAssignment> list = new ArrayList<>();
        String sql = """
                SELECT ConsultantID, ProjectID, HoursWorked
                FROM Project_Assignment
                WHERE ConsultantID = ?
                """;
        try (Connection c = connectionHandler.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, consultantID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(instantiateProjectAssignment(rs));
            }
        } catch (SQLException e) {
            throw new DaoException("Unable to load consultant assignments. Please try again.");
        }
        return list;
    }

    /**
     * Calculates and returns the total number of hours worked by a specific
     * consultant across all projects.
     * 
     * @param consultantID The ID of the consultant.
     * @return The total hours worked. Returns 0 if the consultant has no
     *         assignments.
     * @throws DaoException if a database access error occurs.
     */
    public int totalHoursForConsultant(int consultantID) throws DaoException {
        int hours = 0;

        String sql = """
                SELECT COALESCE(SUM(HoursWorked), 0) AS TotalHours
                FROM Project_Assignment
                WHERE ConsultantID = ?
                """;

        try (Connection conn = connectionHandler.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, consultantID);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("TotalHours");
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Unable to calculate total hours worked. Please try again.");
        }
        return hours;
    }

    /**
     * Calculates and returns the total number of hours worked by all consultants
     * across all projects.
     * 
     * @return The total hours worked across the entire system.
     * @throws DaoException if a database access error occurs.
     */
    public int totalHoursForAllConsultants() throws DaoException {
        String sql = """
                SELECT COALESCE(SUM(HoursWorked), 0) AS TotalHours
                FROM Project_Assignment
                """;

        try (Connection conn = connectionHandler.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("TotalHours");
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Unable to calculate total hours for all consultants. Please try again.");
        }
        return 0;
    }

    /**
     * Returns a list of the consultant(s) who worked the most hours in total.
     * 
     * @return List of consultant(s) with the most hours.
     * @throws DaoException if a database access error occurs.
     * @throws DaoException if no project assignments exist.
     */
    public List<Integer> hardestWorkingConsultants() throws DaoException {
        String sql = """
                SELECT ConsultantID
                FROM Project_Assignment
                GROUP BY ConsultantID
                HAVING SUM(HoursWorked) = (
                    SELECT MAX(TotalHours)
                    FROM (
                        SELECT SUM(HoursWorked) AS TotalHours
                        FROM Project_Assignment
                        GROUP BY ConsultantID
                    ) AS T
                )
                """;

        List<Integer> consultantIds = new ArrayList<>();

        try (Connection conn = connectionHandler.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                consultantIds.add(rs.getInt("ConsultantID"));
            }

        } catch (SQLException e) {
            throw new DaoException("Unable to find the hardest working consultants: " + e.getMessage());
        }
        if (consultantIds.isEmpty()) {
            throw new DaoException("No project assignments found.");
        }
        return consultantIds;
    }

    /**
     * Retrieves a list of consultant names who are assigned to a maximum of 'max'
     * projects.
     * 
     * @param max The maximum number of projects a consultant can be assigned to.
     * @return A list of consultant names meeting the criteria.
     * @throws DaoException if a database access error occurs.
     */
    public List<String> consultantsInMaxNbrOfProjects(int max) throws DaoException {
        List<String> consultants = new ArrayList<>();

        String sql = """
                SELECT c.ConsultantName
                FROM Consultant c
                LEFT JOIN Project_Assignment pa
                       ON c.ConsultantID = pa.ConsultantID
                GROUP BY c.ConsultantID, c.ConsultantName
                HAVING COUNT(pa.ProjectID) <= ?
                """;

        try (Connection conn = connectionHandler.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, max);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // read the consultant name from the result set
                    consultants.add(rs.getString("ConsultantName"));
                }
            }
        } catch (SQLException e) {
            throw new DaoException(
                    "Failed to retrieve consultants with <= " + max + " projects", e);
        }

        return consultants;
    }

    /**
     * Retrieves a list of project IDs for projects that involve every single
     * consultant in the database.
     * 
     * @return A list of project IDs.
     * @throws DaoException if a database access error occurs.
     */
    public List<Integer> projectsThatInvolveEveryConsultant() throws DaoException {
        String sql = """
                SELECT ProjectID
                FROM Project_Assignment
                GROUP BY ProjectID
                HAVING COUNT(DISTINCT ConsultantID) =
                       (SELECT COUNT(*) FROM Consultant)
                """;
        List<Integer> ids = new ArrayList<>();
        try (Connection conn = connectionHandler.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ids.add(rs.getInt("ProjectID"));
            }
        } catch (SQLException e) {
            throw new DaoException("Unable to find projects involving all consultants. Please try again.");
        }
        return ids;
    }

    /**
     * Retrieves all active project assignments for a specific consultant.
     * An active project is one that does not have an end date.
     * 
     * @param consultantID The ID of the consultant.
     * @return A list of active project assignments.
     * @throws DaoException if a database access error occurs.
     */
    public List<ProjectAssignment> getActiveProjectAssignments(int consultantID) throws DaoException {
        List<ProjectAssignment> list = new ArrayList<>();
        String sql = """
                SELECT pa.ConsultantID, pa.ProjectID, pa.HoursWorked
                FROM Project_Assignment pa
                JOIN Project p ON p.ProjectID = pa.ProjectID
                WHERE ConsultantID = ?
                AND EndDate IS NULL
                """;

        try (Connection c = connectionHandler.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, consultantID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(instantiateProjectAssignment(rs));
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Unable to load active assignments for consultant. Please try again.");
        }
        return list;
    }

    /**
     * Retrieves all assignments for a specific project, including detailed
     * information
     * about each assigned consultant (number, name, title).
     * 
     * @param projectID The ID of the project.
     * @return A list of ProjectAssignment objects, populated with consultant
     *         details.
     * @throws DaoException if a database access error occurs.
     */
    public List<ProjectAssignment> getAssignmentsWithConsultants(int projectID) throws DaoException {
        List<ProjectAssignment> list = new ArrayList<>();
        String sql = """
                SELECT pa.ConsultantID, pa.ProjectID, pa.HoursWorked,
                       c.ConsultantNo, c.ConsultantName, c.Title
                FROM Project_Assignment pa
                JOIN Consultant c ON pa.ConsultantID = c.ConsultantID
                WHERE pa.ProjectID = ?
                """;

        try (Connection c = connectionHandler.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, projectID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new ProjectAssignment(
                            rs.getInt("ProjectID"),
                            rs.getInt("ConsultantID"),
                            rs.getInt("HoursWorked"),
                            rs.getInt("ConsultantNo"),
                            rs.getString("ConsultantName"),
                            rs.getString("Title")));
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Unable to load project assignments with consultant details. Please try again.");
        }
        return list;
    }

    public boolean tooManyResources(int projectID) throws DaoException {
        String sql = """
                SELECT
                    /* Distinct consultants currently working on ANY active project */
                    (SELECT COUNT(DISTINCT pa2.ConsultantID)
                     FROM Project_Assignment pa2
                     JOIN Project p2 ON p2.ProjectID = pa2.ProjectID
                     WHERE p2.EndDate IS NULL) AS ActiveConsultants,

                    /* Consultants already on THIS project (regardless of its status) */
                    (SELECT COUNT(DISTINCT pa3.ConsultantID)
                     FROM Project_Assignment pa3
                     WHERE pa3.ProjectID = ?) AS ConsultantsOnProject,

                    /* Is THIS project active? */
                    (SELECT CASE WHEN p.EndDate IS NULL THEN 1 ELSE 0 END
                     FROM Project p
                     WHERE p.ProjectID = ?) AS IsProjectActive
                """;

        try (Connection conn = connectionHandler.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, projectID);
            ps.setInt(2, projectID);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    boolean isActiveProject = rs.getInt("IsProjectActive") == 1;
                    if (!isActiveProject) {
                        // Closed project: treat as not exceeding (or block upstream)
                        return false;
                    }

                    int activeConsultants = rs.getInt("ActiveConsultants"); // distinct across active projects
                    int onProject = rs.getInt("ConsultantsOnProject");

                    if (activeConsultants == 0) {
                        // No active projects -> nothing to compare against
                        return false;
                    }

                    double ratio = (onProject + 1) / (double) activeConsultants; // simulate adding one
                    return ratio > 0.60;
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Unable to check project resources: " + e.getMessage(), e);
        }

        return false;
    }

}