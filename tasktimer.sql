CREATE TABLE IF NOT EXISTS tasks(
    taskid INTEGER AUTO_INCREMENT PRIMARY KEY,
    taskperformed VARCHAR(140) UNIQUE NOT NULL
) ENGINE = INNODB;

CREATE TABLE IF NOT EXISTS timelogs(
    /*
    Because no two events may start that simultaneously.
    */
    timestarted DATETIME NOT NULL PRIMARY KEY,
    timeended DATETIME NOT NULL,
    taskid INTEGER NOT NULL,
    FOREIGN KEY (taskid) REFERENCES tasks (taskid)
) ENGINE = INNODB;
