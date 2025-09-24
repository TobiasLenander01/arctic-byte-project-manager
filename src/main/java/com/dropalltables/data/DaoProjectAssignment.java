package com.dropalltables.data;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.dropalltables.models.Consultant;
import com.dropalltables.models.ProjectAssignment;

public class DaoProjectAssignment {
    private ConnectionHandler connectionHandler;

    @FunctionalInterface
    private interface Binder {
        void bind(PreparedStatement ps) throws SQLException; //behåll SQL här!! catch i ExcUpd
    }

    // Removes duplicate code when updating database
    private int execUpdate(String sql, Binder binder) throws DaoException {
        try (Connection c = connectionHandler.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            binder.bind(ps);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Database error executing update: " + sql, e);
        }
    }

    public DaoProjectAssignment() throws DaoException {
        try {
            this.connectionHandler = new ConnectionHandler();
        } catch (IOException e) {
            throw new DaoException("Failed to initialize DaoProjectAssignment", e);
        }
    }

    private ProjectAssignment instantiateProjectAssignment(ResultSet rs) throws SQLException {
        return new ProjectAssignment(
                rs.getInt("ConsultantID"),
                rs.getInt("ProjectID"),
                rs.getInt("HoursWorked"));
    }

    // Returns a project assignment matching consultantID and projectID
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
            throw new DaoException("not_found ProjectAssignment consultant="
                    + consultantID + ", project=" + projectID, e);
        }
        return null;
    }

    /*
     * Assigns a consultant to a project starting with 0 hours worked. Uniqueness is
     * handled in DB?
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

    // Updates the hours worked by a consultant on a project
    public int updateHours(int consultantID, int projectID, int addHours) throws DaoException {
        ProjectAssignment pa = findProjectAssignment(consultantID, projectID);

        if (pa == null) {
            throw new DaoException("No ProjectAssignment found for consultant="
                    + consultantID + ", project=" + projectID);
        } 
        pa.incrementHoursWorked(addHours);
        
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

    public int deleteProjectAssignmentByConsultantID(int consultantID) throws DaoException {
        String sql = """
                DELETE FROM Project_Assignment
                WHERE ConsultantID = ?
                """;
        return execUpdate(sql, ps -> {
            ps.setInt(1, consultantID);
        });
    }

    public int deleteProjectAssignmentByProjectID(int projectID) throws DaoException {
        String sql = """
                DELETE FROM Project_Assignment
                WHERE ProjectID = ?
                """;
        return execUpdate(sql, ps -> {
            ps.setInt(1, projectID);
        });
    }

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
            throw new DaoException("Failed to get ProjectAssignments for projectID=" + projectID, e);
        }
        return list;
    }

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
            throw new DaoException("Failed to get ProjectAssignments for consultantID=" + consultantID, e);
        }
        return list;
    }

    // Returns total number of hours on all project assignments by a consultant
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
            throw new DaoException("Failed to calculate total hours for consultantID=" + consultantID, e);
        }
        return hours;
    }

    // Returns total number of hours on all project assignments by all consultants
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
            throw new DaoException("Failed to calculate total hours for all consultants", e);
        }
        return 0;
    }

    // Returns ID of consultant with the most worked hours. No handling of ties.
    public int hardestWorkingConsultant() throws DaoException {
        String sql = """
                SELECT TOP 1 ConsultantID
                FROM Project_Assignment
                GROUP BY ConsultantID
                ORDER BY SUM(HoursWorked) DESC
                """;

        try (Connection conn = connectionHandler.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ConsultantID");
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Database error hardestWorkingConsultant", e);
        }
        return 0;
    }

    // Returns list of all projects that involve every consultant
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
            throw new DaoException("Database error projectsThatInvolveEveryConsultant", e);
        }
        return ids;
    }

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
            throw new DaoException("Failed to get active ProjectAssignments for consultantID=" + consultantID, e);
        }
        return list;
    }

    // Returns all consultant assignments for a project,
    // including consultant details (ConsultantNo, Name, Title)
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
            throw new DaoException("Database error getAssignmentsWithConsultants for projectID=" + projectID, e);
        }
        return list;
    }

}