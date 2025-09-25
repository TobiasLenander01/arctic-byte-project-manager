-- =========================================================
-- DROP TABLES (order matters: drop children first)
-- =========================================================
DROP TABLE IF EXISTS Project_Assignment;

DROP TABLE IF EXISTS Milestone;

DROP TABLE IF EXISTS Consultant;

DROP TABLE IF EXISTS Project;

-- =========================================================
-- CREATE TABLES
-- =========================================================
CREATE TABLE Consultant (
    ConsultantID INT IDENTITY(1, 1),
    ConsultantNo INT NOT NULL,
    ConsultantName VARCHAR(255) NOT NULL,
    Title VARCHAR(255) NOT NULL,
    CONSTRAINT PK_Consultant_ConsultantID PRIMARY KEY (ConsultantID),
    CONSTRAINT UQ_Consultant_ConsultantNo UNIQUE (ConsultantNo)
);

CREATE TABLE Project (
    ProjectID INT IDENTITY(1, 1),
    ProjectNo INT NOT NULL,
    ProjectName VARCHAR(255) NOT NULL,
    StartDate DATETIME NOT NULL,
    EndDate DATETIME NULL,
    CONSTRAINT PK_Project_ProjectID PRIMARY KEY (ProjectID),
    CONSTRAINT UQ_Project_ProjectNo UNIQUE (ProjectNo)
);

CREATE TABLE Milestone (
    MilestoneID INT IDENTITY(1, 1),
    MilestoneNo INT NOT NULL,
    MilestoneName VARCHAR(255) NOT NULL,
    MilestoneDate DATETIME NOT NULL,
    ProjectID INT NOT NULL,
    CONSTRAINT PK_Milestone_MilestoneID PRIMARY KEY (MilestoneID),
    CONSTRAINT UQ_Milestone_MilestoneNo UNIQUE (MilestoneNo),
    CONSTRAINT CK_Milestone_Date CHECK (MilestoneDate >= '2022-01-01'),
    CONSTRAINT FK_Milestone_ProjectID FOREIGN KEY (ProjectID) REFERENCES Project(ProjectID) ON DELETE CASCADE -- <-- cascade delete milestones when project deleted
);

CREATE TABLE Project_Assignment (
    ProjectID INT NOT NULL,
    ConsultantID INT NOT NULL,
    HoursWorked INT NOT NULL CHECK (HoursWorked >= 0),
    CONSTRAINT PK_Project_Assignment PRIMARY KEY (ProjectID, ConsultantID),
    CONSTRAINT FK_PA_Project FOREIGN KEY (ProjectID) REFERENCES Project(ProjectID) ON DELETE CASCADE,
    -- <-- cascade delete assignments when project deleted
    CONSTRAINT FK_PA_Consultant FOREIGN KEY (ConsultantID) REFERENCES Consultant(ConsultantID) ON DELETE CASCADE -- <-- cascade delete assignments when consultant deleted
);

-- =========================================================
-- TEST DATA
-- =========================================================
-- Consultants
INSERT INTO
    Consultant (ConsultantNo, ConsultantName, Title)
VALUES
    (1001, 'Alice Smith', 'Senior Consultant'),
    (1002, 'Bob Johnson', 'Consultant'),
    (1003, 'Carol Lee', 'Project Manager'),
    (1004, 'David Brown', 'Technical Lead'),
    (1005, 'Emma Wilson', 'Business Analyst'),
    (1006, 'Frank Davis', 'Consultant'),
    (1007, 'Grace Miller', 'Senior Developer'),
    (1008, 'Henry Garcia', 'Consultant'),
    (1009, 'Isabella Lopez', 'Project Manager'),
    (1010, 'Jack White', 'Consultant'),
    (1011, 'Karen Adams', 'Senior Consultant'),
    (1012, 'Liam Thompson', 'Consultant'),
    (1013, 'Mia Martinez', 'Business Analyst'),
    (1014, 'Noah Anderson', 'Consultant'),
    (1015, 'Olivia Taylor', 'Senior Developer'),
    (1016, 'Paul Walker', 'Consultant'),
    (1017, 'Quinn Harris', 'Technical Lead'),
    (1018, 'Rachel King', 'Consultant'),
    (1019, 'Samuel Scott', 'Project Manager'),
    (1020, 'Tina Evans', 'Consultant'),
    (1021, 'Uma Rivera', 'Business Analyst'),
    (1022, 'Victor Lewis', 'Consultant'),
    (1023, 'Wendy Hall', 'Senior Consultant'),
    (1024, 'Xavier Allen', 'Consultant');

-- Projects
INSERT INTO
    Project (ProjectNo, ProjectName, StartDate, EndDate)
VALUES
    (
        2001,
        'Website Redesign',
        '2024-06-01',
        '2024-08-31'
    ),
    (
        2002,
        'Mobile App Development',
        '2024-07-15',
        NULL
    ),
    (
        2003,
        'Database Migration',
        '2024-09-01',
        '2024-12-31'
    ),
    (2004, 'API Integration', '2024-10-01', NULL),
    (
        2005,
        'Security Audit',
        '2024-08-15',
        '2024-09-30'
    ),
    (
        2006,
        'Cloud Infrastructure',
        '2025-01-01',
        '2025-06-30'
    ),
    (2007, 'CRM Upgrade', '2024-11-01', '2025-02-28'),
    (
        2008,
        'Data Warehouse',
        '2024-09-15',
        '2025-03-15'
    ),
    (2009, 'AI Pilot', '2024-12-01', NULL);

-- Project Assignments
INSERT INTO
    Project_Assignment (ProjectID, ConsultantID, HoursWorked)
VALUES
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
    (4, 10, 95),
    (5, 1, 50),
    (5, 5, 85),
    (5, 12, 60),
    (6, 3, 160),
    (6, 4, 200),
    (6, 6, 45),
    (7, 9, 120),
    (7, 13, 90),
    (7, 14, 100),
    (8, 11, 150),
    (8, 15, 95),
    (8, 16, 80),
    (9, 17, 200),
    (9, 18, 140),
    (9, 19, 180);

-- Milestones
INSERT INTO
    Milestone (
        MilestoneNo,
        MilestoneName,
        MilestoneDate,
        ProjectID
    )
VALUES
    (3001, 'Design Complete', '2024-06-15', 1),
    (3002, 'Development Start', '2024-06-20', 1),
    (3003, 'App Prototype', '2024-07-25', 2),
    (3004, 'Final Review', '2024-08-25', 1),
    (3005, 'Requirements Analysis', '2024-09-15', 3),
    (3006, 'Data Schema Design', '2024-10-01', 3),
    (3007, 'Migration Testing', '2024-11-15', 3),
    (3008, 'API Design Phase', '2024-10-15', 4),
    (3009, 'Integration Testing', '2024-11-30', 4),
    (3010, 'Security Assessment', '2024-08-30', 5),
    (3011, 'Vulnerability Report', '2024-09-15', 5),
    (3012, 'Infrastructure Planning', '2025-01-15', 6),
    (3013, 'Cloud Deployment', '2025-04-01', 6),
    (
        3014,
        'Performance Optimization',
        '2025-05-15',
        6
    ),
    (3015, 'Kickoff Meeting', '2024-11-10', 7),
    (3016, 'CRM Go-Live', '2025-02-20', 7),
    (3017, 'ETL Setup', '2024-10-05', 8),
    (3018, 'Warehouse Testing', '2025-02-10', 8),
    (3019, 'AI Model Training', '2025-01-15', 9),
    (3020, 'Pilot Evaluation', '2025-03-01', 9);

-- =========================================================
-- Quick sanity checks
-- =========================================================
SELECT
    *
FROM
    Consultant;

SELECT
    *
FROM
    Project;

SELECT
    *
FROM
    Project_Assignment;

SELECT
    *
FROM
    Milestone;