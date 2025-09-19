

-- CREATE TABLES
CREATE TABLE Consultant
(
    ConsultantID int NOT NULL,
    Name varchar(255) NOT NULL,
    Title varchar(255) NOT NULL,
    PRIMARY KEY (ConsultantID)
);

CREATE TABLE Milestone
(
    MilestoneID int IDENTITY NOT NULL,
    Name varchar(255) NOT NULL,
    [Date] datetime NOT NULL,
    ProjectProjectID int NOT NULL,
    PRIMARY KEY (MilestoneID)
);

CREATE TABLE Project
(
    ProjectID int IDENTITY NOT NULL,
    Name varchar(255) NOT NULL,
    StartDate datetime NOT NULL,
    EndDate datetime NULL,
    PRIMARY KEY (ProjectID)
);

CREATE TABLE Project_Assignment
(
    ProjectProjectID int NOT NULL,
    ConsultantConsultantID int NOT NULL,
    Hours int NOT NULL,
    PRIMARY KEY (ProjectProjectID, ConsultantConsultantID)
);

-- CONSTRAINTS
ALTER TABLE Project_Assignment ADD CONSTRAINT FKProject_As501810 FOREIGN KEY (ProjectProjectID) REFERENCES Project (ProjectID);
ALTER TABLE Project_Assignment ADD CONSTRAINT FKProject_As548124 FOREIGN KEY (ConsultantConsultantID) REFERENCES Consultant (ConsultantID);
ALTER TABLE Milestone ADD CONSTRAINT FKMilestone501336 FOREIGN KEY (ProjectProjectID) REFERENCES Project (ProjectID);
ALTER TABLE Milestone ADD CONSTRAINT CK_Milestone_Date CHECK ([Date] >= '2022-01-01');
ALTER TABLE Project_Assignment ADD CONSTRAINT CK_Project_Assignment_Hours CHECK (Hours >= 0);


-- TEST DATA
INSERT INTO Consultant (ConsultantID, Name, Title) VALUES
(1, 'Alice Smith', 'Senior Consultant'),
(2, 'Bob Johnson', 'Consultant'),
(3, 'Carol Lee', 'Project Manager');

INSERT INTO Project (Name, StartDate, EndDate) VALUES
('Website Redesign', '2024-06-01', '2024-08-31'),
('Mobile App Development', '2024-07-15', NULL);

INSERT INTO Project_Assignment (ProjectProjectID, ConsultantConsultantID, Hours) VALUES
(1, 1, 120),
(1, 2, 80),
(2, 3, 100);
