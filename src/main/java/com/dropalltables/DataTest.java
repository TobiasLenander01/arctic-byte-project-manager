package com.dropalltables;

import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.List;

import com.dropalltables.data.ConnectionHandler;
import com.dropalltables.data.DaoProject;
import com.dropalltables.data.DaoProjectAssignment;
import com.dropalltables.models.Project;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DataTest {

    public static void main(String[] args) {
        System.out.println("--- RUNNING DATA TESTS ---");

        try {
            // TEST DATABASE CONNECTION
            ConnectionHandler connectionHandler = new ConnectionHandler();
            Connection connection = connectionHandler.getConnection();

            if (connection != null) {
                System.out.println("Database connection successful.");
            } else {
                System.out.println("Database connection failed.");
                return;
            }

            // TEST PROJECT DAO
            System.out.println("--- TESTING PROJECT ---");
            DaoProject daoProject = new DaoProject();

            // TEST GET ALL PROJECTS
            System.out.println("\n daoProject.getAllProjects():");
            List<Project> allProjects = daoProject.getAllProjects();
            for (Project project : allProjects) {
                printProperties(project);
            }

            // TEST GET PROJECT BY ID
            System.out.println("\n daoProject.getProjectById(1):");
            Project project1 = daoProject.getProjectById(1);
            printProperties(project1);

            // TODO: TEST CONSULTANT ETC..

            // TEST PROJECT ASSIGNMENT DAO
            System.out.println("\n--- TESTING PROJECT_ASSIGNMENT ---");
            DaoProjectAssignment daoPA = new DaoProjectAssignment();

            int testConsultantId = findAnyConsultantId(connectionHandler);
            int testProjectId = findAnyProjectId(connectionHandler);

            if (testConsultantId == 0 || testProjectId == 0) {
                System.out.println(
                        "⚠️  Skipping Project_Assignment tests: need at least one Consultant and one Project row.");
                return;
            }

            // Start clean (ignore result if nothing to delete)
            daoPA.deleteProjectAssignment(testConsultantId, testProjectId);

            // Baseline total for consultant
            int totalBefore = daoPA.totalHoursForConsultant(testConsultantId);
            System.out.println("totalHoursForConsultant(before) = " + totalBefore);

            // INSERT
            int ins = daoPA.insertProjectAssignment(testConsultantId, testProjectId);
            System.out.println("insertProjectAssignment -> rows: " + ins);

            // VERIFY via getByConsultantID / getByProjectID
            System.out.println("\ngetByConsultantID(" + testConsultantId + "):");
            for (var pa : daoPA.getByConsultantID(testConsultantId)) {
                printProperties(pa);
            }
            System.out.println("\ngetByProjectID(" + testProjectId + "):");
            for (var pa : daoPA.getByProjectID(testProjectId)) {
                printProperties(pa);
            }

            // UPDATE hours
            int newHours = 12;
            int upd = daoPA.updateHours(testConsultantId, testProjectId, newHours);
            System.out.println("\nupdateHours -> rows: " + upd);

            // VERIFY hours updated
            System.out.println("After update, getByConsultantID(" + testConsultantId + ") filtered to Project "
                    + testProjectId + ":");
            daoPA.getByConsultantID(testConsultantId).stream()
                    .filter(pa -> {
                        try {
                            // ProjectAssignment likely has a getProjectID(); if not, reflection printer
                            // below will still show it.
                            // Replace with pa.getProjectID() if your model exposes it.
                            var f = pa.getClass().getDeclaredField("projectID");
                            f.setAccessible(true);
                            return ((Integer) f.get(pa)) == testProjectId;
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .forEach(DataTest::printProperties);

            // VERIFY totals moved by +newHours (since we inserted fresh)
            int totalAfter = daoPA.totalHoursForConsultant(testConsultantId);
            System.out.println("\ntotalHoursForConsultant(after) = " + totalAfter +
                    " (delta " + (totalAfter - totalBefore) + ", expected " + newHours + ")");

            // HARDEST WORKING CONSULTANT
            int hardest = daoPA.hardestWorkingConsultant();
            System.out.println("\nhardestWorkingConsultant() -> ConsultantID: " + hardest);

            // PROJECTS THAT INVOLVE EVERY CONSULTANT
            List<Integer> allHandsProjects = daoPA.projectsThatInvolveEveryConsultant();
            System.out.println("\nprojectsThatInvolveEveryConsultant() -> " + allHandsProjects);

            // ACTIVE ASSIGNMENTS for the consultant (requires EndDate IS NULL fix in DAO)
            System.out.println("\ngetActiveProjectAssignments(" + testConsultantId + "):");
            for (var pa : daoPA.getActiveProjectAssignments(testConsultantId)) {
                printProperties(pa);
            }

            // CLEANUP
            int del = daoPA.deleteProjectAssignment(testConsultantId, testProjectId);
            System.out.println("\ndeleteProjectAssignment -> rows: " + del);

        } catch (IOException | RuntimeException | java.sql.SQLException e) {
            System.out.println("Error during data test: " + e.getMessage());
        }

    }

    /**
     * GENERISK METOD FÖR ATT VISA ETT OBJEKTS VARIABLER PÅ ETT SNYGGT SÄTT I
     * TERMINALEN
     */
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
            if (!first) {
                sb.append(", ");
            }
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
        // SQL Server dialect (TOP 1). If you switch DB, use the equivalent (e.g., LIMIT
        // 1).
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
}
