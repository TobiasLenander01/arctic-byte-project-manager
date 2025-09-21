package com.dropalltables;

import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.List;

import com.dropalltables.data.ConnectionHandler;
import com.dropalltables.data.DaoProject;
import com.dropalltables.models.Project;

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

}
