package com.dropalltables;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

import com.dropalltables.data.DaoException;
import com.dropalltables.data.DaoProject;
import com.dropalltables.models.Project;

public class DataTest {

    public static void main(String[] args) {
        System.out.println("--- RUNNING DATA TESTS ---");

        try {
            // TEST PROJECT DAO
            System.out.println("--- TESTING PROJECT ---");
            DaoProject daoProject = new DaoProject();

            // TEST GET ALL PROJECTS
            System.out.println("\n daoProject.getAllProjects():");
            List<Project> allProjects = daoProject.getAllProjects();
            for (Project project : allProjects) {
                printProperties(project);
            }

            // TEST INSERT PROJECT
            System.out.println("\n daoProject.insertProject():");
            Project project1 = new Project(2007, "Grupparbete", java.time.LocalDate.parse("2024-06-01"));
            
            // Check if project already exists
            if (daoProject.projectExists(project1.getProjectNo())) {
                System.out.println("Project with ProjectNo " + project1.getProjectNo() + " already exists. Skipping insert.");
            } else {
                daoProject.insertProject(project1);
                System.out.println("Project inserted successfully.");
            }

            // TEST GET PROJECT BY NO
            System.out.println("\n daoProject.getProjectById(1):");
            Project project2 = daoProject.getProjectByNo(2007);
            printProperties(project2);

            // TODO: TEST CONSULTANT ETC..

        } catch (IOException e) {
            System.err.println("IO Error during data testing: " + e.getMessage());
        } catch (DaoException e) {
            System.err.println("DAO Error during data testing: " + e.getMessage());
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
