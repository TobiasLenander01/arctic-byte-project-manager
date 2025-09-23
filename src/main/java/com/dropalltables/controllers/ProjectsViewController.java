package com.dropalltables.controllers;

import java.io.IOException;
import java.util.List;

import com.dropalltables.data.DaoException;
import com.dropalltables.data.DaoProject;
import com.dropalltables.data.DaoProjectAssignment;
import com.dropalltables.data.DaoMilestone;
import com.dropalltables.models.Project;
import com.dropalltables.models.ProjectAssignment;
import com.dropalltables.models.Milestone;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ProjectsViewController {
    // --- Table Projects ---
    @FXML
    private TableView<Project> tableViewProjects;
    @FXML
    private TableColumn<Project, Number> tableColumnProjectNumber;
    @FXML
    private TableColumn<Project, String> tableColumnProjectName;
    @FXML
    private TableColumn<Project, String> tableColumnProjectStartDate;
    @FXML
    private TableColumn<Project, String> tableColumnProjectEndDate;

    // --- Table Consultants ---
    @FXML
    private TableView<ProjectAssignment> tableViewConsultantsOnProject;
    @FXML
    private TableColumn<ProjectAssignment, Integer> tableColumnConsultantNo;
    @FXML
    private TableColumn<ProjectAssignment, String> tableColumnConsultantName;
    @FXML
    private TableColumn<ProjectAssignment, String> tableColumnConsultantTitle;
    @FXML
    private TableColumn<ProjectAssignment, Integer> tableColumnConsultantHours;

    // --- Table Milestones ---
    @FXML
    private TableView<Milestone> tableViewMilestones;
    @FXML
    private TableColumn<Milestone, String> tableColumnMilestoneName;
    @FXML
    private TableColumn<Milestone, String> tableColumnMilestoneDate;

    // --- Button ---
    @FXML
    private Button buttonAddHours;

    // --- Data lists ---
    private final ObservableList<Project> projectData = FXCollections.observableArrayList();
    private final ObservableList<ProjectAssignment> consultantData = FXCollections.observableArrayList();
    private final ObservableList<Milestone> milestoneData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupProjectColumns();
        setupConsultantColumns();
        setupMilestoneColumns();
        loadProjectsFromDatabase();
        setupSelectionListener();

        // disable button if no consultant is selected
        buttonAddHours.disableProperty().bind(
                tableViewConsultantsOnProject.getSelectionModel().selectedItemProperty().isNull());
    }

    @FXML
    public void buttonCreateProjectAction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CreateProjectWindow.fxml"));
            Parent root = loader.load();

            CreateProjectWindowController controller = loader.getController();

            Stage dialog = new Stage();
            dialog.setTitle("Add Project");
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(new Scene(root));
            dialog.showAndWait(); // block until closed

            Project newProject = controller.getCreatedProject();
            if (newProject != null) {
                // Save to DB
                DaoProject dao = new DaoProject();
                dao.insertProject(newProject);

                // Refresh table
                loadProjectsFromDatabase();
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to add project: " + e.getMessage());
        }
    }

    // --- Setup columns for Projects table ---
    private void setupProjectColumns() {
        tableColumnProjectNumber.setCellValueFactory(new PropertyValueFactory<>("projectNo"));
        tableColumnProjectName.setCellValueFactory(new PropertyValueFactory<>("name"));
        tableColumnProjectStartDate.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        tableColumnProjectEndDate.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        tableViewProjects.setItems(projectData);
    }

    // --- Setup columns for Consultants table ---
    private void setupConsultantColumns() {
        tableColumnConsultantNo.setCellValueFactory(new PropertyValueFactory<>("consultantNo"));
        tableColumnConsultantName.setCellValueFactory(new PropertyValueFactory<>("consultantName"));
        tableColumnConsultantTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        tableColumnConsultantHours.setCellValueFactory(new PropertyValueFactory<>("hoursWorked"));
        tableViewConsultantsOnProject.setItems(consultantData);
    }

    // --- Setup columns for Milestones table ---
    private void setupMilestoneColumns() {
        tableColumnMilestoneName.setCellValueFactory(new PropertyValueFactory<>("name"));
        tableColumnMilestoneDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        tableViewMilestones.setItems(milestoneData);
    }

    // --- Load all projects from DB ---
    private void loadProjectsFromDatabase() {
        try {
            DaoProject dao = new DaoProject();
            List<Project> projects = dao.getAllProjects();
            projectData.setAll(projects);
        } catch (IOException | DaoException e) {
            e.printStackTrace();
        }
    }

    // --- Called when a project is selected ---
    private void setupSelectionListener() {
        tableViewProjects.getSelectionModel().selectedItemProperty().addListener((obs, oldProject, newProject) -> {
            if (newProject != null) {
                loadConsultantsForProject(newProject);
                loadMilestonesForProject(newProject);
            } else {
                consultantData.clear();
                milestoneData.clear();
            }
        });
    }

    // --- Load consultants working on a specific project ---
    private void loadConsultantsForProject(Project project) {
        try {
            DaoProject daoProject = new DaoProject();
            Integer projectID = daoProject.getProjectId(project.getProjectNo());

            DaoProjectAssignment daoPA = new DaoProjectAssignment();
            List<ProjectAssignment> assignments = daoPA.getAssignmentsWithConsultants(projectID);
            consultantData.setAll(assignments);
        } catch (Exception e) {
            e.printStackTrace();
            consultantData.clear();
        }
    }

    // --- Load milestones belonging to a specific project ---
    private void loadMilestonesForProject(Project project) {
        try {
            DaoMilestone daoMilestone = new DaoMilestone();
            List<Milestone> milestones = daoMilestone.getMilestonesByProjectNo(project.getProjectNo());
            milestoneData.setAll(milestones);
        } catch (Exception e) {
            e.printStackTrace();
            milestoneData.clear();
        }
    }

    // --- Button: Delete project ---
    @FXML
    public void buttonDeleteProjectAction() {
        Project selected = tableViewProjects.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return; // show error message? disable button when nothing is selected?
        }
        try {
            DaoProject delete = new DaoProject();
            delete.deleteProject(selected.getProjectNo());

            // remove from table + clear dependent tables
            projectData.remove(selected);
            consultantData.clear();
            milestoneData.clear();
            tableViewProjects.getSelectionModel().clearSelection();

        } catch (IOException | DaoException e) {
            e.printStackTrace();
        }

    }

    // --- Button: add hours to selected consultant ---
    @FXML
    public void buttonAddHoursAction() {
        ProjectAssignment selected = tableViewConsultantsOnProject.getSelectionModel().getSelectedItem();
        if (selected == null) {
            // should never happen, button is disabled when no row selected
            showAlert("No consultant selected", "Please select a consultant to add hours.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Hours");
        dialog.setHeaderText("Consultant: " + selected.getConsultantName());
        dialog.setContentText("Enter number of hours to add:");

        dialog.showAndWait().ifPresent(input -> {
            try {
                int hoursToAdd = Integer.parseInt(input);

                DaoProjectAssignment dao = new DaoProjectAssignment();
                dao.updateHours(selected.getConsultantID(), selected.getProjectID(), hoursToAdd);

                // refresh consultants table
                loadConsultantsForProject(findSelectedProject());

            } catch (NumberFormatException e) {
                showAlert("Invalid input", "Please enter a valid number.");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error", "Failed to update hours: " + e.getMessage());
            }
        });
    }

    // --- Find currently selected project ---
    private Project findSelectedProject() {
        return tableViewProjects.getSelectionModel().getSelectedItem();
    }

    // --- Helper: show dialog messages ---
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
