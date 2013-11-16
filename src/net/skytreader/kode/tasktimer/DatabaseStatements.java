package net.skytreader.kode.tasktimer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
TODO: Clean-up code!
*/
public class DatabaseStatements{
	
	private ConfigReader cfReader;
	private volatile static DatabaseStatements instance;
	
	private final String serverName;
	private final String dbName;
	private final String mysqlUsername;
	private final String mysqlPassword;
	
	public final int SERVER_INDEX = 0;
	public final int USERNAME_INDEX = 1;
	public final int PASSWORD_INDEX = 2;
	public final int DBNAME_INDEX = 3;
	
	private final String SERVER_CVAR = "dbserver";
	private final String USERNAME_CVAR = "username";
	private final String PASSWORD_CVAR = "password";
	private final String DBNAME_CVAR = "dbname"; 
	
	public static final String COL_TIMESTARTED = "timestarted";
	public static final String COL_TIMEENDED = "timeended";
	public static final String COL_TASKPERFORMED = "taskperformed";
	public static final String COL_TASKID = "taskid";
	
	public static final String TBL_TIMELOGS = "timelogs";
	public static final String TBL_TASKS = "tasks";
	
	public static final String[] TABLE_NAMES = {TBL_TASKS, TBL_TIMELOGS};
	
	private static final String[] TIMELOGS_ROWS = {COL_TIMESTARTED + " DATETIME NOT NULL",
	                                               COL_TIMEENDED + " DATETIME NOT NULL", 
					               COL_TASKID + " INTEGER NOT NULL"};
	private static final String[] TASKS_ROWS = {COL_TASKID + " INTEGER NOT NULL AUTO_INCREMENT",
	                                     COL_TASKPERFORMED + " VARCHAR(140) UNIQUE NOT NULL"};
	public static final String[][] TABLE_ROWS = {TASKS_ROWS, TIMELOGS_ROWS};
	
	private static final String[] TIMELOGS_INTEGRITY_CONSTRAINTS = {"PRIMARY KEY (timestarted)",
	                                                         "FOREIGN KEY (" + COL_TASKID + ") REFERENCES " + TBL_TASKS};
	private static final String[] TASKS_INTEGRITY_CONSTRAINTS = {"PRIMARY KEY (" + COL_TASKID + ")"};
	public static final String[][] TABLE_CONSTRAINTS = {TASKS_INTEGRITY_CONSTRAINTS, TIMELOGS_INTEGRITY_CONSTRAINTS};
	public final static String CONFIG_FILENAME = "dbconfig.txt";
	
	private DatabaseStatements() throws IOException{
		cfReader = new ConfigReader(DatabaseStatements.CONFIG_FILENAME);
		serverName = cfReader.getConfigValue(SERVER_CVAR);
		dbName = cfReader.getConfigValue(DBNAME_CVAR);
		mysqlUsername = cfReader.getConfigValue(USERNAME_CVAR);
		mysqlPassword = cfReader.getConfigValue(PASSWORD_CVAR);
	}
	
	public static DatabaseStatements getInstance() throws IOException{
		if(instance == null){
			synchronized(DatabaseStatements.class){
				if(instance == null){
					instance = new DatabaseStatements();
				}
			}
		}
		return instance;
	}
	
	public String getServer(){
		return serverName;
	}
	
	public String getUsername(){
		return mysqlUsername;
	}
	
	public String getPassword(){
		return mysqlPassword;
	}
	
	public String getDBName(){
		return dbName;
	}
	
	/**
	Call this method when any of the values serverName | dbName |
	mysqlUsername | mysqlPassword are modified so that they will be reflected
	on the config file.
	
	TODO: Try writing this without using assert.
	*/
	public void updateConfig(String[] newValues) throws IOException{
		assert newValues.length >= 4 :
		       "Must provide an array of length at least 4. See docs for details.";
		
		serverName = 
		
		PrintWriter ConfigWriter = new PrintWriter(
		  new BufferedWriter(new FileWriter(DatabaseStatements.CONFIG_FILENAME)));
		ConfigWriter.write(SERVER_CVAR + " = " + newValues[SERVER_INDEX]);
		ConfigWriter.write(USERNAME_CVAR + " = " + newValues[USERNAME_INDEX]);
		ConfigWriter.write(PASSWORD_CVAR + " = " + newValues[PASSWORD_INDEX]);
		ConfigWriter.write(DBNAME_CVAR + " = " + newValues[DBNAME_INDEX]);
	}
}
