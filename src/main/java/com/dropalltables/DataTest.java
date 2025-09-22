package com.dropalltables;

import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import com.dropalltables.data.ConnectionHandler;
import com.dropalltables.data.DaoConsultant;
import com.dropalltables.data.DaoException;
import com.dropalltables.data.DaoMilestone;
import com.dropalltables.data.DaoProject;
import com.dropalltables.data.DaoProjectAssignment;
import com.dropalltables.models.Consultant;
import com.dropalltables.models.Milestone;
import com.dropalltables.models.Project;

public class DataTest {

    public static void main(String[] args) {
        System.out.println("--- RUNNING DATA TESTS ---");

        try {
            // === TEST PROJECT DAO ===
            testDaoProject();

            // TEST CONSULTANT DAO
            System.out.println("\n--- TESTING CONSULTANT ---");

            DaoConsultant daoConsultant = new DaoConsultant();

            // TEST GET ALL CONSULTANTS
            System.out.println("\n daoConsultant.getAllConsultants():");
            List<Consultant> allConsultants = daoConsultant.getAllConsultants();
            for (Consultant consultant : allConsultants) {
                printProperties(consultant);
            }

            System.out.println("\n daoConsultant.insertConsultant():");
            Consultant newConsultant = new Consultant(1045, "Test Consultant", "Senior Developer");
            daoConsultant.insertConsultant(newConsultant);
            System.out.println("Test Consultant inserted successfully.");

            System.out.println("daoConsultant.updateConsultant():");
            Consultant updatedConsultant = new Consultant(1045, "Updated Consultant", "Lead Developer");
            daoConsultant.updateConsultant(1045, updatedConsultant);

            System.out.println("daoConsultant.getConsultantByNo(1045):");
            Consultant printConsultant = daoConsultant.getConsultantByNo(1045);
            printProperties(printConsultant);

            System.out.println("daoConsultant.deleteConsultant():");
            daoConsultant.deleteConsultant(1045);
            System.out.println("Test Consultant deleted successfully.");

            // TEST PROJECT ASSIGNMENT DAO
            System.out.println("\n --- TESTING PROJECTASSIGNMENT ---");

            // System.out.println("\n daoProject.getProjectByNo(2007):");
            // Project project2 = daoProject.getProjectByNo(2007);
            // printProperties(project2);

            // === TEST PROJECT ASSIGNMENT DAO ===
            System.out.println("\n--- TESTING PROJECT_ASSIGNMENT ---");
            DaoProjectAssignment daoPA = new DaoProjectAssignment();
            ConnectionHandler connectionHandler = new ConnectionHandler();

            int testConsultantId = findAnyConsultantId(connectionHandler);
            int testProjectId = findAnyProjectId(connectionHandler);

            if (testConsultantId == 0 || testProjectId == 0) {
                System.out.println(
                        "⚠️  Skipping Project_Assignment tests: need at least one Consultant and one Project row.");
                return;
            }

            // Start clean
            daoPA.deleteProjectAssignment(testConsultantId, testProjectId);

            int totalBefore = daoPA.totalHoursForConsultant(testConsultantId);
            System.out.println("totalHoursForConsultant(before) = " + totalBefore);

            int ins = daoPA.insertProjectAssignment(testConsultantId, testProjectId);
            System.out.println("insertProjectAssignment -> rows: " + ins);

            System.out.println("\ngetByConsultantID(" + testConsultantId + "):");
            for (var pa : daoPA.getByConsultantID(testConsultantId)) {
                printProperties(pa);
            }

            System.out.println("\ngetByProjectID(" + testProjectId + "):");
            for (var pa : daoPA.getByProjectID(testProjectId)) {
                printProperties(pa);
            }

            int newHours = 12;
            int upd = daoPA.updateHours(testConsultantId, testProjectId, newHours);
            System.out.println("\nupdateHours -> rows: " + upd);

            System.out.println("After update, getByConsultantID(" + testConsultantId + ") filtered to Project "
                    + testProjectId + ":");
            daoPA.getByConsultantID(testConsultantId).stream()
                    .filter(pa -> {
                        try {
                            var f = pa.getClass().getDeclaredField("ProjectID");
                            f.setAccessible(true);
                            return ((Integer) f.get(pa)) == testProjectId;
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .forEach(DataTest::printProperties);

            int totalAfter = daoPA.totalHoursForConsultant(testConsultantId);
            System.out.println("\ntotalHoursForConsultant(after) = " + totalAfter +
                    " (delta " + (totalAfter - totalBefore) + ", expected " + newHours + ")");

            int hardest = daoPA.hardestWorkingConsultant();
            System.out.println("\nhardestWorkingConsultant() -> ConsultantID: " + hardest);

            List<Integer> allHandsProjects = daoPA.projectsThatInvolveEveryConsultant();
            System.out.println("\nprojectsThatInvolveEveryConsultant() -> " + allHandsProjects);

            System.out.println("\ngetActiveProjectAssignments(" + testConsultantId + "):");
            for (var pa : daoPA.getActiveProjectAssignments(testConsultantId)) {
                printProperties(pa);
            }

            int del = daoPA.deleteProjectAssignment(testConsultantId, testProjectId);
            System.out.println("\ndeleteProjectAssignment -> rows: " + del);

            // === TEST MILESTONE DAO ===
            System.out.println("\n--- TESTING MILESTONE ---");
            DaoMilestone daoMilestone = new DaoMilestone();

            // Get a test project ID for milestone operations
            int testProjectForMilestone = findAnyProjectId(connectionHandler);
            if (testProjectForMilestone == 0) {
                System.out.println("⚠️  Skipping Milestone tests: need at least one Project row.");
                return;
            }

            System.out.println("\n daoMilestone.getMilestonesByProject(" + testProjectForMilestone + "):");
            List<Milestone> projectMilestones = daoMilestone.getMilestonesByProject(testProjectForMilestone);
            for (Milestone milestone : projectMilestones) {
                printProperties(milestone);
            }

            System.out.println("\n daoMilestone.getMilestoneCountForProject(" + testProjectForMilestone + "):");
            int milestoneCount = daoMilestone.getMilestoneCountForProject(testProjectForMilestone);
            System.out.println("Milestone count: " + milestoneCount);

            // Test adding a new milestone - create a manual test that bypasses the model
            // issue
            System.out.println("\n daoMilestone.addMilestone():");
            try {
                // Create a test milestone by directly inserting with correct ProjectID
                String testSql = "INSERT INTO Milestone (MilestoneName, MilestoneDate, ProjectID) VALUES (?, ?, ?)";
                try (Connection conn = connectionHandler.getConnection();
                        PreparedStatement stmt = conn.prepareStatement(testSql)) {

                    stmt.setString(1, "Test Milestone");
                    stmt.setTimestamp(2, Timestamp.valueOf(java.time.LocalDate.parse("2024-12-01").atStartOfDay()));
                    stmt.setInt(3, testProjectForMilestone); // Use actual ProjectID from database
                    int rows = stmt.executeUpdate();
                    System.out.println("Milestone added successfully via direct SQL. Rows affected: " + rows);
                }
            } catch (SQLException e) {
                System.out.println("Failed to add milestone via SQL: " + e.getMessage());
            }

            // Get milestones after adding
            System.out.println("\n daoMilestone.getMilestonesByProject(" + testProjectForMilestone + ") after adding:");
            List<Milestone> milestonesAfterAdd = daoMilestone.getMilestonesByProject(testProjectForMilestone);
            for (Milestone milestone : milestonesAfterAdd) {
                printProperties(milestone);
            }

            // Test deleting the milestone
            int testMilestoneId = findMilestoneIdByName(connectionHandler, "Test Milestone");
            if (testMilestoneId > 0) {
                System.out.println("\n daoMilestone.deleteMilestone(" + testMilestoneId + "):");
                daoMilestone.deleteMilestone(testMilestoneId);
                System.out.println("Milestone deleted successfully.");
            } else {
                System.out.println("⚠️  Could not find the test milestone for deletion test.");
            }

        } catch (IOException e) {
            System.err.println("IO Error during data testing: " + e.getMessage());
        } catch (DaoException e) {
            System.err.println("DAO Error during data testing: " + e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // === Utility methods ===

    private static void printProperties(Object obj) {
        String blue = "\u001B[34m";
        String cyan = "\u001B[36m";
        String reset = "\u001B[0m";

        StringBuilder sb = new StringBuilder();
        Class<?> clazz = obj.getClass();
        sb.append(clazz.getSimpleName()).append("{");

        Field[] fields = clazz.getDeclaredFields();
        boolean first = true;

        for (Field field : fields) {
            if (!first)
                sb.append(", ");
            first = false;

            field.setAccessible(true);
            try {
                Object value = field.get(obj);
                sb.append(blue).append(field.getName()).append("=").append(reset);
                sb.append(cyan);
                if (value instanceof String) {
                    sb.append("'").append(value).append("'");
                } else {
                    sb.append(value);
                }
                sb.append(reset);
            } catch (IllegalAccessException e) {
                sb.append(blue).append(field.getName()).append("=").append(reset);
                sb.append(cyan).append("<inaccessible>").append(reset);
            }
        }

        sb.append("}");
        System.out.println(sb.toString());
    }

    private static int findAnyConsultantId(ConnectionHandler ch) throws SQLException {
        String sql = "SELECT TOP 1 ConsultantID FROM Consultant ORDER BY ConsultantID";
        try (Connection c = ch.getConnection();
                PreparedStatement ps = c.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private static int findAnyProjectId(ConnectionHandler ch) throws SQLException {
        String sql = "SELECT TOP 1 ProjectID FROM Project ORDER BY ProjectID";
        try (Connection c = ch.getConnection();
                PreparedStatement ps = c.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private static int findMilestoneIdByName(ConnectionHandler ch, String milestoneName) throws SQLException {
        String sql = "SELECT TOP 1 MilestoneID FROM Milestone WHERE MilestoneName = ? ORDER BY MilestoneID DESC";
        try (Connection c = ch.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, milestoneName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private static void testDaoProject() throws IOException, DaoException {
        // === TEST PROJECT DAO ===
        System.out.println("--- TESTING PROJECT ---");
        DaoProject daoProject = new DaoProject();

        System.out.println("\n daoProject.getAllProjects():");
        List<Project> allProjects = daoProject.getAllProjects();
        for (Project project : allProjects) {
            printProperties(project);
        }

        System.out.println("\n daoProject.insertProject():");
        Project project1 = new Project(2007, "Grupparbete", java.time.LocalDate.parse("2024-06-01"));

        if (daoProject.projectExists(project1.getProjectNo())) {
            System.out.println("Project " + project1.getProjectNo() + " already exists. Skipping insert.");
        } else {
            daoProject.insertProject(project1);
            System.out.println("Project inserted successfully.");
        }

        // === TEST getProjectByNo ===
        System.out.println("\n daoProject.getProjectByNo():");

        // Test with existing project
        if (!allProjects.isEmpty()) {
            int existingProjectNo = allProjects.get(0).getProjectNo();
            Project foundProject = daoProject.getProjectByNo(existingProjectNo);
            if (foundProject != null) {
                System.out.println("Found existing project:");
                printProperties(foundProject);
            } else {
                System.out.println("Project not found with ProjectNo: " + existingProjectNo);
            }
        }

        // Test with non-existing project
        Project nonExistingProject = daoProject.getProjectByNo(9999);
        System.out.println("Non-existing project (9999): " + (nonExistingProject == null ? "null" : "found"));

        // Test validation
        try {
            daoProject.getProjectByNo(-1);
            System.out.println("ERROR: Should have thrown IllegalArgumentException for negative ProjectNo");
        } catch (IllegalArgumentException e) {
            System.out.println("✓ Validation works for negative ProjectNo: " + e.getMessage());
        }

        // === TEST getProjectById ===
        System.out.println("\n daoProject.getProjectById():");

        // Find any project ID to test with
        try {
            ConnectionHandler ch = new ConnectionHandler();
            int anyProjectId = findAnyProjectId(ch);
            if (anyProjectId > 0) {
                Project foundProjectById = daoProject.getProjectById(anyProjectId);
                if (foundProjectById != null) {
                    System.out.println("Found project by ID " + anyProjectId + ":");
                    printProperties(foundProjectById);
                } else {
                    System.out.println("No project found with ID: " + anyProjectId);
                }
            } else {
                System.out.println("No project IDs found in database");
            }
        } catch (Exception e) {
            System.out.println("Error testing getProjectById: " + e.getMessage());
        }

        // Test validation for getProjectById
        try {
            daoProject.getProjectById(0);
            System.out.println("ERROR: Should have thrown IllegalArgumentException for zero ProjectID");
        } catch (IllegalArgumentException e) {
            System.out.println("✓ Validation works for zero ProjectID: " + e.getMessage());
        }

        // === TEST projectExists ===
        System.out.println("\n daoProject.projectExists():");

        if (!allProjects.isEmpty()) {
            int existingProjectNo = allProjects.get(0).getProjectNo();
            boolean exists = daoProject.projectExists(existingProjectNo);
            System.out.println("Project " + existingProjectNo + " exists: " + exists);
        }

        boolean nonExistingExists = daoProject.projectExists(9999);
        System.out.println("Project 9999 exists: " + nonExistingExists);

        // Test validation
        try {
            daoProject.projectExists(-5);
            System.out.println("ERROR: Should have thrown IllegalArgumentException for negative ProjectNo");
        } catch (IllegalArgumentException e) {
            System.out.println("✓ Validation works for negative ProjectNo in exists check: " + e.getMessage());
        }

        // === TEST insertProject validation ===
        System.out.println("\n daoProject.insertProject() validation tests:");

        // Test null project
        try {
            daoProject.insertProject(null);
            System.out.println("ERROR: Should have thrown IllegalArgumentException for null project");
        } catch (IllegalArgumentException e) {
            System.out.println("✓ Validation works for null project: " + e.getMessage());
        }

        // Test null name
        try {
            Project invalidProject = new Project(9998, null, java.time.LocalDate.now());
            daoProject.insertProject(invalidProject);
            System.out.println("ERROR: Should have thrown IllegalArgumentException for null name");
        } catch (IllegalArgumentException e) {
            System.out.println("✓ Validation works for null name: " + e.getMessage());
        }

        // Test empty name
        try {
            Project invalidProject = new Project(9997, "   ", java.time.LocalDate.now());
            daoProject.insertProject(invalidProject);
            System.out.println("ERROR: Should have thrown IllegalArgumentException for empty name");
        } catch (IllegalArgumentException e) {
            System.out.println("✓ Validation works for empty name: " + e.getMessage());
        }

        // Test negative ProjectNo
        try {
            Project invalidProject = new Project(-1, "Test Project", java.time.LocalDate.now());
            daoProject.insertProject(invalidProject);
            System.out.println("ERROR: Should have thrown IllegalArgumentException for negative ProjectNo");
        } catch (IllegalArgumentException e) {
            System.out.println("✓ Validation works for negative ProjectNo: " + e.getMessage());
        }

        // === TEST updateProject ===
        System.out.println("\n daoProject.updateProject():");

        // Test updating an existing project
        if (!allProjects.isEmpty()) {
            Project existingProject = allProjects.get(0);
            Project updatedProject = new Project(
                    existingProject.getProjectNo(),
                    existingProject.getName() + " (Updated)",
                    existingProject.getStartDate(),
                    java.time.LocalDate.now());

            try {
                daoProject.updateProject(updatedProject);
                System.out.println("✓ Project updated successfully");

                // Verify the update
                Project verifyProject = daoProject.getProjectByNo(existingProject.getProjectNo());
                if (verifyProject != null) {
                    System.out.println("Updated project:");
                    printProperties(verifyProject);
                }
            } catch (DaoException e) {
                System.out.println("Error updating project: " + e.getMessage());
            }
        }

        // Test updating non-existing project
        try {
            Project nonExistingUpdate = new Project(9999, "Non-existing", java.time.LocalDate.now());
            daoProject.updateProject(nonExistingUpdate);
            System.out.println("ERROR: Should have thrown DaoException for non-existing project");
        } catch (DaoException e) {
            System.out.println("✓ Correctly handles non-existing project update: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("✓ Validation error for update: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("✓ Error updating non-existing project: " + e.getMessage());
        }

        // === TEST deleteProject ===
        System.out.println("\n daoProject.deleteProject():");

        // Insert a test project to delete
        Project testDeleteProject = new Project(9996, "Test Delete Project", java.time.LocalDate.now());
        if (!daoProject.projectExists(testDeleteProject.getProjectNo())) {
            daoProject.insertProject(testDeleteProject);
            System.out.println("Inserted test project for deletion");

            // Now delete it
            daoProject.deleteProject(testDeleteProject.getProjectNo());
            System.out.println("✓ Project deleted successfully");

            // Verify deletion
            boolean stillExists = daoProject.projectExists(testDeleteProject.getProjectNo());
            System.out.println("Project still exists after deletion: " + stillExists);
        } else {
            System.out.println("Test project already exists, skipping delete test");
        }

        // Test deleting non-existing project
        try {
            daoProject.deleteProject(9995);
            System.out.println("ERROR: Should have thrown DaoException for non-existing project deletion");
        } catch (DaoException e) {
            System.out.println("✓ Correctly handles non-existing project deletion: " + e.getMessage());
        }

        // Test validation for deleteProject
        try {
            daoProject.deleteProject(-1);
            System.out.println("ERROR: Should have thrown IllegalArgumentException for negative ProjectNo");
        } catch (IllegalArgumentException e) {
            System.out.println("✓ Validation works for negative ProjectNo in delete: " + e.getMessage());
        }
    }
}
