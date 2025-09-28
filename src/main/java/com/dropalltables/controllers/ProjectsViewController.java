package com.dropalltables.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.dropalltables.data.*;
import com.dropalltables.models.*;
import com.dropalltables.util.AlertUtil;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Controller for the Projects view.
 *
 * Responsibilities:
 * • Display and filter projects
 * • Manage consultant assignments (with >60 % resource warning)
 * • Full CRUD for projects and milestones
 * • Toggle filter to show only active projects (EndDate IS NULL)
 */
public class ProjectsViewController {

    // ------------------------------------------------------------------------
    // --- FXML UI elements
    // ------------------------------------------------------------------------
    // Project table + filters
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
    @FXML
    private CheckBox checkBoxActiveOnly; // NEW: show only active projects

    // Consultant table
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

    // Milestone table
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

    // Buttons
    @FXML
    private Button buttonSetHours;

    // ------------------------------------------------------------------------
    // --- Backing data
    // ------------------------------------------------------------------------
    private final ObservableList<Project> projectData = FXCollections.observableArrayList();
    private final ObservableList<ProjectAssignment> consultantData = FXCollections.observableArrayList();
    private final ObservableList<Milestone> milestoneData = FXCollections.observableArrayList();

    // ------------------------------------------------------------------------
    // --- Initialization
    // ------------------------------------------------------------------------
    @FXML
    public void initialize() {
        setupProjectColumns();
        setupConsultantColumns();
        setupMilestoneColumns();
        loadProjectsFromDatabase();
        setupSelectionListener();

        // Disable “set hours” unless a consultant is selected
        buttonSetHours.disableProperty()
                .bind(tableViewConsultantsOnProject.getSelectionModel().selectedItemProperty().isNull());

        // Live filters: text fields + active-only checkbox
        textFieldFilterNo.textProperty().addListener((obs, o, n) -> applyFilters());
        textFieldFilterName.textProperty().addListener((obs, o, n) -> applyFilters());
        textFieldFilterDate.textProperty().addListener((obs, o, n) -> applyFilters());
        checkBoxActiveOnly.selectedProperty().addListener((obs, o, n) -> applyFilters());
    }

    // ------------------------------------------------------------------------
    // --- Filtering
    // ------------------------------------------------------------------------
    /** Re-applies all project filters in memory (no extra DB calls). */
    private void applyFilters() {
        String filterNo = textFieldFilterNo.getText().trim();
        String filterName = textFieldFilterName.getText().toLowerCase().trim();
        String filterDate = textFieldFilterDate.getText().trim();
        boolean activeOnly = checkBoxActiveOnly.isSelected();

        try {
            List<Project> all = new DaoProject().getAllProjects();
            projectData.setAll(all.stream()
                    .filter(p -> filterNo.isEmpty() || String.valueOf(p.getProjectNo()).startsWith(filterNo))
                    .filter(p -> filterName.isEmpty() || p.getName().toLowerCase().contains(filterName))
                    .filter(p -> {
                        if (filterDate.isEmpty())
                            return true;
                        boolean s = p.getStartDate() != null && p.getStartDate().toString().startsWith(filterDate);
                        boolean e = p.getEndDate() != null && p.getEndDate().toString().startsWith(filterDate);
                        return s || e;
                    })
                    .filter(p -> !activeOnly || p.getEndDate() == null) // NEW filter
                    .toList());
        } catch (DaoException e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    // ------------------------------------------------------------------------
    // --- Project CRUD
    // ------------------------------------------------------------------------
    @FXML
    public void buttonCreateProjectAction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CreateProjectWindow.fxml"));
            Parent root = loader.load();
            CreateProjectWindowController c = loader.getController();

            Stage dialog = new Stage();
            dialog.setTitle("Add Project");
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(new Scene(root));
            dialog.showAndWait();

            Project p = c.getCreatedProject();
            if (p != null) {
                new DaoProject().insertProject(p);
                loadProjectsFromDatabase();
            }
        } catch (IOException | DaoException e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    @FXML
    public void buttonUpdateProjectAction() {
        Project sel = tableViewProjects.getSelectionModel().getSelectedItem();
        if (sel == null) {
            AlertUtil.showInfo("No project selected", "Please select a project to update.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CreateProjectWindow.fxml"));
            Parent root = loader.load();
            CreateProjectWindowController c = loader.getController();
            c.setProjectForEdit(sel);

            Stage dialog = new Stage();
            dialog.setTitle("Update Project");
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(new Scene(root));
            dialog.showAndWait();

            Project updated = c.getCreatedProject();
            if (updated != null) {
                new DaoProject().updateProject(updated);
                loadProjectsFromDatabase();
            }
        } catch (IOException | DaoException e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    @FXML
    public void buttonDeleteProjectAction() {
        Project sel = tableViewProjects.getSelectionModel().getSelectedItem();
        if (sel == null) {
            AlertUtil.showInfo("No selection", "Please select a project to delete.");
            return;
        }
        try {
            new DaoProject().deleteProject(sel.getProjectNo());
            projectData.remove(sel);
            consultantData.clear();
            milestoneData.clear();
            tableViewProjects.getSelectionModel().clearSelection();
        } catch (DaoException e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    // ------------------------------------------------------------------------
    // --- Consultant assignment & hours
    // ------------------------------------------------------------------------
    @FXML
    public void buttonAssignConsultantAction() {
        Project p = tableViewProjects.getSelectionModel().getSelectedItem();
        if (p == null) {
            AlertUtil.showInfo("No project selected", "Please select a project first.");
            return;
        }

        try {
            int projectID = new DaoProject().getProjectID(p.getProjectNo());
            List<Consultant> available = new DaoConsultant().getConsultantsNotInProject(projectID);
            if (available.isEmpty()) {
                AlertUtil.showInfo("No consultants available",
                        "All consultants are already assigned to this project.");
                return;
            }

            List<String> choices = new ArrayList<>();
            for (Consultant c : available) {
                choices.add(c.getName() + " (" + c.getTitle() + ")");
            }

            ChoiceDialog<String> dialog = new ChoiceDialog<>(null, choices);
            dialog.setTitle("Assign Consultant");
            dialog.setHeaderText("Assign consultant to project: " + p.getName());
            dialog.setContentText("Choose consultant:");

            dialog.showAndWait().ifPresent(choice -> {
                Consultant selected = available.stream()
                        .filter(c -> (c.getName() + " (" + c.getTitle() + ")").equals(choice))
                        .findFirst().orElse(null);
                if (selected == null)
                    return;

                try {
                    DaoProjectAssignment daoPA = new DaoProjectAssignment();
                    int consultantID = new DaoConsultant()
                            .getConsultantID(selected.getConsultantNo());

                    // Resource warning if this project would exceed 60 % of active consultants
                    if (daoPA.tooManyResources(projectID)) {
                        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                        confirm.setTitle("Resource Warning");
                        confirm.setHeaderText("High Resource Usage");
                        confirm.setContentText(
                                "Adding this consultant will make this project use more than 60 % "
                                        + "of the company's active consultant resources.\n\n"
                                        + "Do you want to continue?");
                        confirm.showAndWait().ifPresent(btn -> {
                            if (btn == ButtonType.OK)
                                insertAssignment(daoPA, consultantID, projectID, p);
                        });
                    } else {
                        insertAssignment(daoPA, consultantID, projectID, p);
                    }
                } catch (DaoException e) {
                    AlertUtil.showError("Error", e.getMessage());
                }
            });

        } catch (DaoException e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    @FXML
    public void buttonRemoveConsultantAction() {
        Project p = tableViewProjects.getSelectionModel().getSelectedItem();
        ProjectAssignment a = tableViewConsultantsOnProject.getSelectionModel().getSelectedItem();
        if (p == null) {
            AlertUtil.showInfo("No project selected", "Please select a project first.");
            return;
        }
        if (a == null) {
            AlertUtil.showInfo("No consultant selected", "Please select a consultant to remove.");
            return;
        }
        try {
            new DaoProjectAssignment().deleteProjectAssignment(a.getConsultantID(), a.getProjectID());
            loadConsultantsForProject(p);
        } catch (DaoException e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    @FXML
    public void buttonSetHoursAction() {
        ProjectAssignment sel = tableViewConsultantsOnProject.getSelectionModel().getSelectedItem();
        if (sel == null)
            return;

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Set Hours");
        dialog.setHeaderText("Consultant: " + sel.getConsultantName());
        dialog.setContentText("Enter number of hours:");

        dialog.showAndWait().ifPresent(input -> {
            try {
                int hours = Integer.parseInt(input);
                if (hours < 0)
                    throw new NumberFormatException();
                new DaoProjectAssignment().updateHours(sel.getConsultantID(), sel.getProjectID(), hours);
                loadConsultantsForProject(findSelectedProject());
            } catch (NumberFormatException e) {
                AlertUtil.showError("Invalid input", "Please enter a positive number.");
            } catch (DaoException e) {
                AlertUtil.showError("Error", e.getMessage());
            }
        });
    }

    private void insertAssignment(DaoProjectAssignment daoPA,
            int consultantID, int projectID, Project project) {
        try {
            daoPA.insertProjectAssignment(consultantID, projectID);
            loadConsultantsForProject(project);
        } catch (DaoException e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    // ------------------------------------------------------------------------
    // --- Milestone CRUD
    // ------------------------------------------------------------------------
    @FXML
    public void buttonAddMilestoneAction() {
        Project p = tableViewProjects.getSelectionModel().getSelectedItem();
        if (p == null) {
            AlertUtil.showInfo("No project selected", "Please select a project first.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CreateMilestoneWindow.fxml"));
            Parent root = loader.load();
            CreateMilestoneWindowController c = loader.getController();
            c.setProject(p);

            Stage dialog = new Stage();
            dialog.setTitle("Add Milestone");
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(new Scene(root));
            dialog.showAndWait();

            Milestone newM = c.getCreatedMilestone();
            if (newM != null) {
                new DaoMilestone().insertMilestone(newM);
                loadMilestonesForProject(p);
            }
        } catch (IOException | DaoException e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    @FXML
    public void buttonUpdateMilestoneAction() {
        Milestone m = tableViewMilestones.getSelectionModel().getSelectedItem();
        if (m == null) {
            AlertUtil.showInfo("No milestone selected", "Please select a milestone to update.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CreateMilestoneWindow.fxml"));
            Parent root = loader.load();
            CreateMilestoneWindowController c = loader.getController();
            c.setMilestoneForEdit(m);

            Stage dialog = new Stage();
            dialog.setTitle("Update Milestone");
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(new Scene(root));
            dialog.showAndWait();

            Milestone updated = c.getCreatedMilestone();
            if (updated != null) {
                new DaoMilestone().updateMilestone(updated);
                Project p = tableViewProjects.getSelectionModel().getSelectedItem();
                if (p != null)
                    loadMilestonesForProject(p);
            }
        } catch (IOException | DaoException e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    @FXML
    public void buttonDeleteMilestoneAction() {
        Milestone m = tableViewMilestones.getSelectionModel().getSelectedItem();
        if (m == null) {
            AlertUtil.showInfo("No milestone selected", "Please select a milestone to delete.");
            return;
        }
        try {
            new DaoMilestone().deleteMilestone(m.getMilestoneNo());
            Project p = tableViewProjects.getSelectionModel().getSelectedItem();
            if (p != null)
                loadMilestonesForProject(p);
        } catch (DaoException e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    // ------------------------------------------------------------------------
    // --- Helper methods
    // ------------------------------------------------------------------------
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

    private void loadProjectsFromDatabase() {
        try {
            projectData.setAll(new DaoProject().getAllProjects());
        } catch (DaoException e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    private void setupSelectionListener() {
        tableViewProjects.getSelectionModel().selectedItemProperty().addListener((obs, oldP, newP) -> {
            if (newP != null) {
                loadConsultantsForProject(newP);
                loadMilestonesForProject(newP);
            } else {
                consultantData.clear();
                milestoneData.clear();
            }
        });
    }

    private void loadConsultantsForProject(Project p) {
        try {
            int id = new DaoProject().getProjectID(p.getProjectNo());
            consultantData.setAll(new DaoProjectAssignment().getAssignmentsWithConsultants(id));
        } catch (DaoException e) {
            consultantData.clear();
        }
    }

    private void loadMilestonesForProject(Project p) {
        try {
            List<Milestone> ms = new DaoMilestone().getMilestonesByProjectNo(p.getProjectNo());
            milestoneData.setAll(ms);
            labelMilestonesHeader.setText("Milestones (" + ms.size() + ")");
        } catch (DaoException e) {
            milestoneData.clear();
            labelMilestonesHeader.setText("Milestones (0)");
        }
    }

    private Project findSelectedProject() {
        return tableViewProjects.getSelectionModel().getSelectedItem();
    }

    @FXML
    public void buttonShowAllConsultantsProjectsAction() {
        try {
            List<Integer> ids = new DaoProjectAssignment().projectsThatInvolveEveryConsultant();
            if (ids.isEmpty()) {
                AlertUtil.showInfo("Projects", "No project involves every consultant.");
                return;
            }
            List<String> names = new ArrayList<>();
            DaoProject dao = new DaoProject();
            for (Integer id : ids) {
                Project p = dao.getProjectByID(id);
                if (p != null)
                    names.add(p.getProjectNo() + ", " + p.getName());
            }
            AlertUtil.showInfo("Projects with Every Consultant",
                    "Projects that involve every consultant:\n" + String.join("\n", names));
        } catch (DaoException e) {
            AlertUtil.showError("Error", "Error retrieving data.");
        }
    }
}
