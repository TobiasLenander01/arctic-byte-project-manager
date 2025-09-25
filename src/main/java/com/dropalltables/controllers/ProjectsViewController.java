package com.dropalltables.controllers;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.dropalltables.data.DaoConsultant;
import com.dropalltables.data.DaoException;
import com.dropalltables.data.DaoMilestone;
import com.dropalltables.data.DaoProject;
import com.dropalltables.data.DaoProjectAssignment;
import com.dropalltables.models.Consultant;
import com.dropalltables.models.Milestone;
import com.dropalltables.models.Project;
import com.dropalltables.models.ProjectAssignment;
import com.dropalltables.util.AlertUtil;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
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
    private Label labelMilestonesHeader;
    @FXML
    private TableView<Milestone> tableViewMilestones;
    @FXML
    private TableColumn<Milestone, String> tableColumnMilestoneName;
    @FXML
    private TableColumn<Milestone, String> tableColumnMilestoneDate;

    // --- Buttons ---
    @FXML
    private Button buttonSetHours;
    @FXML
    private Button buttonAddMilestone;
    @FXML
    private Button buttonDeleteMilestone;
    @FXML
    private Button buttonCreateProject;
    @FXML
    private Button buttonDeleteProject;
    @FXML
    private Button buttonUpdateProject;

    // --- Labels ---
    @FXML
    private Label labelAllConsultantsProjects;

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
        updateAllConsultantsProjectsLabel();

        // disable add hours button if no consultant is selected
        buttonSetHours.disableProperty().bind(
                tableViewConsultantsOnProject.getSelectionModel().selectedItemProperty().isNull());
    }

    // --- assign consultant to a project ---
    @FXML
    public void buttonAssignConsultantAction() {
        Project selectedProject = tableViewProjects.getSelectionModel().getSelectedItem();
        if (selectedProject == null) {
            AlertUtil.showInfo("No project selected", "Please select a project first.");
            return;
        }

        try {
            // Fetch consultants not in this project
            DaoProject daoProject = new DaoProject();
            int projectID = daoProject.getProjectID(selectedProject.getProjectNo());

            DaoConsultant daoConsultant = new DaoConsultant();
            List<Consultant> available = daoConsultant.getConsultantsNotInProject(projectID);

            if (available.isEmpty()) {
                AlertUtil.showInfo("No consultants available", "All consultants are already assigned.");
                return;
            }

            // --- Show selection dialog ---
            // Make a simple list of consultant strings
            List<String> consultantChoices = new ArrayList<>();
            for (Consultant c : available) {
                consultantChoices.add(c.getName() + " (" + c.getTitle() + ")");
            }

            // Show dialog
            ChoiceDialog<String> dialog = new ChoiceDialog<>(null, consultantChoices);
            dialog.setTitle("Assign Consultant");
            dialog.setHeaderText("Assign consultant to project: " + selectedProject.getName());
            dialog.setContentText("Choose consultant:");

            // Handle selection
            dialog.showAndWait().ifPresent(choice -> {
                // Find the consultant that matches the string
                Consultant selectedConsultant = null;
                for (Consultant c : available) {
                    String display = c.getName() + " (" + c.getTitle() + ")";
                    if (display.equals(choice)) {
                        selectedConsultant = c;
                        break;
                    }
                }

                if (selectedConsultant != null) {
                    try {
                        DaoProjectAssignment daoPA = new DaoProjectAssignment();
                        DaoConsultant daoC = new DaoConsultant();
                        DaoProject daoP = new DaoProject();

                        daoPA.insertProjectAssignment(
                                daoC.getConsultantID(selectedConsultant.getConsultantNo()),
                                daoP.getProjectID(selectedProject.getProjectNo()));

                        loadConsultantsForProject(selectedProject);
                    } catch (Exception e) {
                        e.printStackTrace();
                        AlertUtil.showError("Error", "Failed to assign consultant: " + e.getMessage());
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Failed to fetch available consultants: " + e.getMessage());
        }
    }

    // --- remove consultant from project assignment ---
    @FXML
    public void buttonRemoveConsultantAction() {
        Project selectedProject = tableViewProjects.getSelectionModel().getSelectedItem();
        ProjectAssignment selectedAssignment = tableViewConsultantsOnProject.getSelectionModel().getSelectedItem();

        if (selectedProject == null) {
            AlertUtil.showInfo("No project selected", "Please select a project first.");
            return;
        }

        if (selectedAssignment == null) {
            AlertUtil.showInfo("No consultant selected", "Please select a consultant to remove.");
            return;
        }

        try {
            DaoProjectAssignment daoPA = new DaoProjectAssignment();
            daoPA.deleteProjectAssignment(selectedAssignment.getConsultantID(), selectedAssignment.getProjectID());

            // refresh consultants table
            loadConsultantsForProject(selectedProject);

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Failed to remove consultant: " + e.getMessage());
        }
    }

    // --- Create project ---
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
            dialog.showAndWait();

            Project newProject = controller.getCreatedProject();
            if (newProject != null) {
                DaoProject dao = new DaoProject();
                dao.insertProject(newProject);
                loadProjectsFromDatabase();
            }
        } catch (Exception e) {
            AlertUtil.showError("Error", "Failed to add project.");
        }
    }

    // --- Update project ---
    @FXML
    public void buttonUpdateProjectAction() {
        Project selected = tableViewProjects.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showInfo("No project selected", "Please select a project to update.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CreateProjectWindow.fxml"));
            Parent root = loader.load();

            CreateProjectWindowController controller = loader.getController();
            controller.setProjectForEdit(selected); // prefill form with selected project

            Stage dialog = new Stage();
            dialog.setTitle("Update Project");
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(new Scene(root));
            dialog.showAndWait();

            Project updatedProject = controller.getCreatedProject();
            if (updatedProject != null) {
                DaoProject dao = new DaoProject();
                dao.updateProject(updatedProject);
                loadProjectsFromDatabase();
            }
        } catch (DaoException | IOException e) {
            AlertUtil.showError("Error", "Failed to update project.");
        }
    }

    // --- Delete project ---
    @FXML
    public void buttonDeleteProjectAction() {
        Project selected = tableViewProjects.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showInfo("No selection", "Please select a project to delete.");
            return;
        }
        try {
            DaoProject dao = new DaoProject();
            dao.deleteProject(selected.getProjectNo());

            projectData.remove(selected);
            consultantData.clear();
            milestoneData.clear();
            tableViewProjects.getSelectionModel().clearSelection();
        } catch (DaoException e) {
            AlertUtil.showError("Error", "Failed to delete project.");
        }
    }

    // --- Add milestone ---
    @FXML
    public void buttonAddMilestoneAction() {
        Project selected = tableViewProjects.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showInfo("No project selected", "Please select a project first.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Milestone");
        dialog.setHeaderText("New milestone for project: " + selected.getName());
        dialog.setContentText("Enter milestone name:");

        dialog.showAndWait().ifPresent(name -> {
            try {
                int milestoneNo = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
                Milestone milestone = new Milestone(milestoneNo, name, LocalDate.now(), selected);

                DaoMilestone dao = new DaoMilestone();
                dao.insertMilestone(milestone);

                loadMilestonesForProject(selected);
            } catch (DaoException e) {
                AlertUtil.showError("Error", "Failed to add milestone.");
            }
        });
    }

    // --- Delete milestone ---
    @FXML
    public void buttonDeleteMilestoneAction() {
        Milestone selectedMilestone = tableViewMilestones.getSelectionModel().getSelectedItem();
        if (selectedMilestone == null) {
            AlertUtil.showInfo("No milestone selected", "Please select a milestone to delete.");
            return;
        }

        try {
            DaoMilestone dao = new DaoMilestone();
            dao.deleteMilestone(selectedMilestone.getMilestoneNo());

            Project currentProject = tableViewProjects.getSelectionModel().getSelectedItem();
            if (currentProject != null) {
                loadMilestonesForProject(currentProject);
            }
        } catch (DaoException e) {
            AlertUtil.showError("Error", "Failed to delete milestone: " + e.getMessage());
        }
    }

    // --- Add hours ---
    @FXML
    public void buttonSetHoursAction() {
        ProjectAssignment selected = tableViewConsultantsOnProject.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Set Hours");
        dialog.setHeaderText("Consultant: " + selected.getConsultantName());
        dialog.setContentText("Enter number of hours:");

        dialog.showAndWait().ifPresent(input -> {
            try {
                int hours = Integer.parseInt(input);
                if (hours < 0) {
                    throw new NumberFormatException("Negative hours not allowed");
                }

                DaoProjectAssignment dao = new DaoProjectAssignment();
                dao.updateHours(selected.getConsultantID(), selected.getProjectID(), hours);

                loadConsultantsForProject(findSelectedProject());
            } catch (NumberFormatException e) {
                AlertUtil.showError("Invalid input", "Please enter a valid number. Number must be positive.");
            } catch (DaoException e) {
                AlertUtil.showError("Error", "Failed to update hours: " + e.getMessage());
            }
        });
    }

    // --- Setup columns ---
    private void setupProjectColumns() {
        tableColumnProjectNumber.setCellValueFactory(new PropertyValueFactory<>("projectNo"));
        tableColumnProjectName.setCellValueFactory(new PropertyValueFactory<>("name"));
        tableColumnProjectStartDate.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        tableColumnProjectEndDate.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        tableViewProjects.setItems(projectData);
    }

    private void setupConsultantColumns() {
        tableColumnConsultantNo.setCellValueFactory(new PropertyValueFactory<>("consultantNo"));
        tableColumnConsultantName.setCellValueFactory(new PropertyValueFactory<>("consultantName"));
        tableColumnConsultantTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        tableColumnConsultantHours.setCellValueFactory(new PropertyValueFactory<>("hoursWorked"));
        tableViewConsultantsOnProject.setItems(consultantData);
    }

    private void setupMilestoneColumns() {
        tableColumnMilestoneName.setCellValueFactory(new PropertyValueFactory<>("name"));
        tableColumnMilestoneDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        tableViewMilestones.setItems(milestoneData);
    }

    // --- Loaders ---
    private void loadProjectsFromDatabase() {
        try {
            DaoProject dao = new DaoProject();
            List<Project> projects = dao.getAllProjects();
            projectData.setAll(projects);
        } catch (DaoException e) {
            AlertUtil.showError("Error", "Failed to load projects from database. Check connection or contact support.");
        }
    }

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

    private void loadConsultantsForProject(Project project) {
        try {
            DaoProject daoProject = new DaoProject();
            Integer projectID = daoProject.getProjectID(project.getProjectNo());

            DaoProjectAssignment daoPA = new DaoProjectAssignment();
            List<ProjectAssignment> assignments = daoPA.getAssignmentsWithConsultants(projectID);
            consultantData.setAll(assignments);
        } catch (DaoException e) {
            consultantData.clear();
        }
    }

    private void loadMilestonesForProject(Project project) {
        try {
            DaoMilestone daoMilestone = new DaoMilestone();
            List<Milestone> milestones = daoMilestone.getMilestonesByProjectNo(project.getProjectNo());
            milestoneData.setAll(milestones);

            labelMilestonesHeader.setText("Milestones (" + milestones.size() + ")");
        } catch (DaoException e) {
            milestoneData.clear();
            labelMilestonesHeader.setText("Milestones (0)");
        }
    }

    private Project findSelectedProject() {
        return tableViewProjects.getSelectionModel().getSelectedItem();
    }

    // --- Label for "projects with every consultant" ---
    private void updateAllConsultantsProjectsLabel() {
        try {
            DaoProjectAssignment daoPA = new DaoProjectAssignment();
            List<Integer> projectIDs = daoPA.projectsThatInvolveEveryConsultant();

            if (projectIDs.isEmpty()) {
                labelAllConsultantsProjects.setText("No project involves every consultant.");
                return;
            }

            DaoProject daoProject = new DaoProject();
            List<String> projectNames = new ArrayList<>();
            for (Integer id : projectIDs) {
                Project project = daoProject.getProjectByID(id);
                if (project != null) {
                    projectNames.add(project.getName());
                }
            }

            labelAllConsultantsProjects.setText(
                    "Projects that involve every consultant: " + String.join(", ", projectNames));
        } catch (DaoException e) {
            labelAllConsultantsProjects.setText("Error retrieving data.");
        }
    }
}
