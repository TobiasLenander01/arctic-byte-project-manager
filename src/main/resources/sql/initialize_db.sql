

-- DROP TABLES
DROP TABLE IF EXISTS Project_Assignment;
DROP TABLE IF EXISTS Milestone;
DROP TABLE IF EXISTS Consultant;
DROP TABLE IF EXISTS Project;

-- CREATE TABLES
CREATE TABLE Consultant
(
    ConsultantID int IDENTITY(1, 1),
    ConsultantNo int NOT NULL,
    ConsultantName varchar(255) NOT NULL,
    Title varchar(255) NOT NULL,
    
    CONSTRAINT PK_Consultant_ConsultantID PRIMARY KEY (ConsultantID),
    CONSTRAINT UQ_Consultant_ConsultantNo UNIQUE(ConsultantNo)
);

CREATE TABLE Project
(
    ProjectID int IDENTITY(1, 1),
    ProjectNo int NOT NULL,
    ProjectName varchar(255) NOT NULL,
    StartDate datetime NOT NULL,
    EndDate datetime NULL,

    CONSTRAINT PK_Project_ProjectID PRIMARY KEY (ProjectID),
    CONSTRAINT UQ_Project_ProjectNo UNIQUE(ProjectNo)
);

CREATE TABLE Milestone
(
    MilestoneID int IDENTITY(1, 1),
    MilestoneName varchar(255) NOT NULL,
    MilestoneDate datetime NOT NULL,
    ProjectID int NOT NULL,

    CONSTRAINT PK_Milestone_MilestoneID PRIMARY KEY (MilestoneID),
    CONSTRAINT CK_Milestone_Date CHECK (MilestoneDate >= '2022-01-01'),
    CONSTRAINT FK_Milestone_ProjectID FOREIGN KEY (ProjectID) REFERENCES Project(ProjectID)
);

CREATE TABLE Project_Assignment
(
    ProjectID int NOT NULL,
    ConsultantID int NOT NULL,
    HoursWorked int NOT NULL,
    
    CONSTRAINT PK_Project_Assignment_ProjectID PRIMARY KEY (ProjectID, ConsultantID),
    CONSTRAINT CK_Project_Assignment_Hours CHECK (HoursWorked >= 0),
    CONSTRAINT FK_Project_ConsultantID FOREIGN KEY (ConsultantID) REFERENCES Consultant (ConsultantID),
    CONSTRAINT FK_Project_ProjectID FOREIGN KEY (ProjectID) REFERENCES Project (ProjectID)
);

-- TEST DATA
INSERT INTO Consultant (ConsultantNo, ConsultantName, Title) VALUES
(1001, 'Alice Smith', 'Senior Consultant'),
(1002, 'Bob Johnson', 'Consultant'),
(1003, 'Carol Lee', 'Project Manager'),
(1004, 'David Brown', 'Technical Lead'),
(1005, 'Emma Wilson', 'Business Analyst'),
(1006, 'Frank Davis', 'Junior Consultant'),
(1007, 'Grace Miller', 'Senior Developer'),
(1008, 'Henry Garcia', 'UX Designer');

INSERT INTO Project (ProjectNo, ProjectName, StartDate, EndDate) VALUES
(2001, 'Website Redesign', '2024-06-01', '2024-08-31'),
(2002, 'Mobile App Development', '2024-07-15', NULL),
(2003, 'Database Migration', '2024-09-01', '2024-12-31'),
(2004, 'API Integration', '2024-10-01', NULL),
(2005, 'Security Audit', '2024-08-15', '2024-09-30'),
(2006, 'Cloud Infrastructure', '2025-01-01', '2025-06-30');

INSERT INTO Project_Assignment (ProjectID, ConsultantID, HoursWorked) VALUES
(1, 1, 120),
(1, 2, 80),
(1, 8, 60),
(2, 3, 100),
(2, 4, 150),
(2, 7, 90),
(3, 1, 200),
(3, 4, 180),
(3, 5, 75),
(4, 2, 140),
(4, 7, 110),
(5, 1, 50),
(5, 5, 85),
(6, 3, 160),
(6, 4, 200),
(6, 6, 45);

INSERT INTO Milestone (MilestoneName, MilestoneDate, ProjectID) VALUES
('Design Complete', '2024-06-15', 1),
('Development Start', '2024-06-20', 1),
('App Prototype', '2024-07-25', 2),
('Final Review', '2024-08-25', 1),
('Requirements Analysis', '2024-09-15', 3),
('Data Schema Design', '2024-10-01', 3),
('Migration Testing', '2024-11-15', 3),
('API Design Phase', '2024-10-15', 4),
('Integration Testing', '2024-11-30', 4),
('Security Assessment', '2024-08-30', 5),
('Vulnerability Report', '2024-09-15', 5),
('Infrastructure Planning', '2025-01-15', 6),
('Cloud Deployment', '2025-04-01', 6),
('Performance Optimization', '2025-05-15', 6);


-- SELECT STATEMENTS
SELECT * FROM Consultant;
SELECT * FROM Milestone;
SELECT * FROM Project;
SELECT * FROM Project_Assignment;