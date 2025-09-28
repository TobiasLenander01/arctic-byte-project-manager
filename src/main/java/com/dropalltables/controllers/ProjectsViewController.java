package com.dropalltables.controllers;

import java.io.IOException;
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
import javafx.scene.control.TextField;
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
    @FXML
    private TextField textFieldFilterNo;
    @FXML
    private TextField textFieldFilterName;
    @FXML
    private TextField textFieldFilterDate;

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
    private TableColumn<Milestone, Number> tableColumnMilestoneNo;
    @FXML
    private TableColumn<Milestone, String> tableColumnMilestoneName;
    @FXML
    private TableColumn<Milestone, String> tableColumnMilestoneDate;

    // --- Buttons ---
    @FXML
    private Button buttonSetHours;

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

        // disable button if no consultant selected
        buttonSetHours.disableProperty().bind(
                tableViewConsultantsOnProject.getSelectionModel().selectedItemProperty().isNull());

        // hook up filters
        textFieldFilterNo.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        textFieldFilterName.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        textFieldFilterDate.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    // --- logic to filter projects table ---
    private void applyFilters() {
        String filterNo = textFieldFilterNo.getText().trim();
        String filterName = textFieldFilterName.getText().toLowerCase().trim();
        String filterDate = textFieldFilterDate.getText().trim();

        List<Project> allProjects;
        try {
            DaoProject dao = new DaoProject();
            allProjects = dao.getAllProjects();
        } catch (DaoException e) {
            AlertUtil.showError("Error", e.getMessage());
            return;
        }

        projectData.setAll(allProjects.stream()
                // filter by project number
                .filter(p -> filterNo.isEmpty() || String.valueOf(p.getProjectNo()).startsWith(filterNo))
                // filter by name
                .filter(p -> filterName.isEmpty() || p.getName().toLowerCase().contains(filterName))
                // filter by date (match start or end date string)
                .filter(p -> {
                    if (filterDate.isEmpty())
                        return true;
                    boolean matchesStart = p.getStartDate() != null &&
                            p.getStartDate().toString().startsWith(filterDate);
                    boolean matchesEnd = p.getEndDate() != null &&
                            p.getEndDate().toString().startsWith(filterDate);
                    return matchesStart || matchesEnd;
                })
                .toList());
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
                    } catch (DaoException e) {
                        AlertUtil.showError("Error", e.getMessage());
                    }
                }
            });

        } catch (DaoException e) {
            AlertUtil.showError("Error", e.getMessage());
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

        } catch (DaoException e) {
            AlertUtil.showError("Error", e.getMessage());
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
        } catch (java.io.IOException e) {
            AlertUtil.showError("Error", "Failed to load dialog");
        } catch (DaoException e) {
            AlertUtil.showError("Error", e.getMessage());
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

        } catch (DaoException e) {
            AlertUtil.showError("Error", e.getMessage());
        } catch (IOException e) {
            AlertUtil.showError("Error", "Failed to load dialog");
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
            AlertUtil.showError("Error", e.getMessage());
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

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CreateMilestoneWindow.fxml"));
            Parent root = loader.load();

            CreateMilestoneWindowController controller = loader.getController();
            controller.setProject(selected);

            Stage dialog = new Stage();
            dialog.setTitle("Add Milestone");
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(new Scene(root));
            dialog.showAndWait();

            Milestone newMilestone = controller.getCreatedMilestone();
            if (newMilestone != null) {
                DaoMilestone dao = new DaoMilestone();
                dao.insertMilestone(newMilestone);
                loadMilestonesForProject(selected);

            }

        } catch (IOException e) {
            AlertUtil.showError("Error", "Failed to load dialog");
        } catch (DaoException e) {
            AlertUtil.showError("Error", e.getMessage());
        }
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
            AlertUtil.showError("Error", e.getMessage());
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
                AlertUtil.showError("Error", e.getMessage());
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
        tableColumnMilestoneNo.setCellValueFactory(new PropertyValueFactory<>("milestoneNo"));
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
            AlertUtil.showError("Error", e.getMessage());
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

    // --- Show "projects with every consultant" in an alert when button pressed ---
    @FXML
    public void buttonShowAllConsultantsProjectsAction() {
        try {
            DaoProjectAssignment daoPA = new DaoProjectAssignment();
            List<Integer> projectIDs = daoPA.projectsThatInvolveEveryConsultant();

            if (projectIDs.isEmpty()) {
                AlertUtil.showInfo("Projects", "No project involves every consultant.");
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

            AlertUtil.showInfo(
                    "Projects with Every Consultant",
                    "Projects that involve every consultant:\n" + String.join(", ", projectNames));
        } catch (DaoException e) {
            AlertUtil.showError("Error", "Error retrieving data.");
        }
    }
}
