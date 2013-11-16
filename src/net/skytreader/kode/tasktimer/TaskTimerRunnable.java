package net.skytreader.kode.tasktimer;

import net.skytreader.kode.utils.DatabaseDriver;
import net.skytreader.kode.utils.MyFrame;

import java.awt.AWTException;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import java.io.IOException;

import java.net.URL;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.text.SimpleDateFormat;

import java.util.Calendar;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.UIManager;

/**
Where all the good stuff happens!

TODO: Clean-up the code!!!
TODO: Clean input strings!!

@author Chad Estioco
@version Summer 2011
*/
public class TaskTimerRunnable implements Runnable{
	/*
	TODO: Allow user to dynamically change databases while the program
	is running.
	TODO: What happens when the database tables change contents while the
	program is running (change not invoked by the program).
	*/

	private final int WIDTH = 390;
	private final int HEIGHT = 180;
	private final int TIMER_DELAY = 1000;
	private final String APPLICATION_TITLE = "Task Timer";
	private final String TITLE_BAR = APPLICATION_TITLE + " - skytreader";
	private final String INITIAL_TIMER = "00:00:00";
	private int seconds = 0;
	private int minutes = 0;
	private int hours = 0;
	private int curTaskId = -1;
	private String timerDisplay = INITIAL_TIMER;
	private String startDateTime;
	
	private JButton startButton;
	private JButton stopButton;
	private JComboBox taskBox;
	private JFrame mainFrame;
	private JLabel timerLabel;
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private Statement stmt;
	private Timer timerLabelTimer;
	private TrayIcon ti;
	
	private DatabaseDriver minEDCDriver;
	
	public void run(){
		try{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e){
		}
		
		createDatabase();
		
		mainFrame = new MyFrame(WIDTH, HEIGHT, TITLE_BAR);
		mainFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		mainFrame.setResizable(false);
		Container mainFrameContainer = mainFrame.getContentPane();
		mainFrameContainer.setLayout(new BoxLayout(mainFrameContainer, BoxLayout.Y_AXIS));
		
		JMenuBar menuBar = new JMenuBar();
		JMenu dbMenu = new JMenu("Database");
		JMenuItem configDB = new JMenuItem("Configure...");
		configDB.addActionListener(new DBConfigListener());
		dbMenu.add(configDB);
		menuBar.add(dbMenu);
		mainFrame.setJMenuBar(menuBar);
		
		//Create a combo box for the tasks
		taskBox = new JComboBox();
		taskBox.setEditable(true);
		taskBox.addActionListener(new EnterKeyListener());
		populateTaskBox();
		
		//Create the label for the timer
		timerLabel = new JLabel(timerDisplay);
		Font timerFont = timerLabel.getFont();
		timerLabel.setFont(new Font(timerFont.getFontName(), timerFont.getStyle(), 25));
		timerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		//Create the buttons
		JPanel buttonPanel = new JPanel();
		stopButton = new JButton("Stop");
		stopButton.addActionListener(new StopTimerListener());
		stopButton.setEnabled(false);
		startButton = new JButton("Start");
		startButton.addActionListener(new StartTimerListener());
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.add(stopButton);
		buttonPanel.add(startButton);
		
		//Add them to the frame's container
		mainFrameContainer.add(Box.createVerticalStrut(25));
		mainFrameContainer.add(taskBox);
		mainFrameContainer.add(timerLabel);
		mainFrameContainer.add(buttonPanel);
		mainFrameContainer.add(Box.createVerticalStrut(25));
		
		//Create the timer.
		timerLabelTimer = new Timer(TIMER_DELAY, new UpdateTimeListener());
		
		//Have an icon at the system tray
		if(SystemTray.isSupported()){
			final SystemTray st = SystemTray.getSystemTray();
			ti = new TrayIcon((createImage("images/tasktimer_icon.gif")).getImage(), timerDisplay + " - " + APPLICATION_TITLE, createPopUpMenu());
			ti.setImageAutoSize(true);
			
			ti.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					mainFrame.setVisible(true);
				}
			});
			
			try{
				st.add(ti);
			} catch(AWTException awte){
				awte.printStackTrace();
			}
		}
	}
	
	private void createDatabase(){
		try{
			System.out.println("createDatabase() called!");
			DatabaseStatements dbs = DatabaseStatements.getInstance();
			minEDCDriver = new DatabaseDriver(dbs.SERVER_NAME, dbs.DB_NAME,
			                                  dbs.MYSQL_USERNAME, dbs.MYSQL_PASSWORD,
			                                  DatabaseStatements.TABLE_NAMES,
			                                  DatabaseStatements.TABLE_ROWS,
			                                  DatabaseStatements.TABLE_CONSTRAINTS,
							  DatabaseDriver.MYISAM);
			stmt = minEDCDriver.getStatement();
		} catch(SQLException sqle){
			sqle.printStackTrace();
		} catch(ClassNotFoundException cnfe){
			cnfe.printStackTrace();
		} catch(IOException ioe){
			//TODO: Better error message.
			String errorMessage = "The file " + DatabaseStatements.CONFIG_FILENAME + " does not exist.";
			JOptionPane.showMessageDialog(mainFrame, errorMessage,
			                              "Config file error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void populateTaskBox(){
		try{
			stmt.execute("SELECT " + DatabaseStatements.COL_TASKPERFORMED + " FROM " +
			              DatabaseStatements.TBL_TASKS);
			ResultSet rs = stmt.getResultSet();
			rs.first();
			
			while(!rs.isAfterLast()){
				System.out.println("Adding item: " + rs.getString(DatabaseStatements.COL_TASKPERFORMED));
				taskBox.addItem(rs.getString(DatabaseStatements.COL_TASKPERFORMED));
				rs.next();
			}
		} catch(SQLException sqle){
			sqle.printStackTrace();
		}
	}
	
	private class DBConfigListener implements ActionListener{
		public void actionPerformed(ActionEvent ae){
			DBConfigManager dbcm = new DBConfigManager();
			dbcm.run();
		}
	}
	
	private class EnterKeyListener implements ActionListener{
		private boolean callOnce;
		
		public EnterKeyListener(){
			callOnce = true;
		}
		
		public void actionPerformed(ActionEvent ae){
			String currentTask = (String) taskBox.getEditor().getItem();
			
			if(timerLabel != null && callOnce){
				(new StartTimerListener(currentTask)).actionPerformed(ae);
			}
			
			callOnce = !callOnce;
		}
	}
	
	private void setCurTaskId(String task){
		try{
			stmt.execute("SELECT * FROM " + DatabaseStatements.TBL_TASKS + " WHERE " +
				         DatabaseStatements.COL_TASKPERFORMED + " = '" + task + "'");
			ResultSet rs = stmt.getResultSet();
			
			if(rs.last()){
				curTaskId = rs.getInt(DatabaseStatements.COL_TASKID);
				System.out.println(task + " exists!");
			} else{
				String[] autoGenerate = {DatabaseStatements.COL_TASKID};
				stmt.executeUpdate("INSERT INTO " + DatabaseStatements.TBL_TASKS + "(" +
				                    DatabaseStatements.COL_TASKPERFORMED + ") VALUES('" + task + "')", autoGenerate);
				stmt.execute("SELECT * FROM " + DatabaseStatements.TBL_TASKS + " WHERE " +
				             DatabaseStatements.COL_TASKPERFORMED + " = '" + task + "'");
				rs = stmt.getResultSet();
				rs.first();
				curTaskId = rs.getInt(DatabaseStatements.COL_TASKID);
				taskBox.addItem(task);
			}
			
		} catch(SQLException sqle){
			sqle.printStackTrace();
		}
	}
	
	/**
	This ActionListener is fed to a Timer object. Purpose is to update
	the timer display of the program.
	*/
	private class UpdateTimeListener implements ActionListener{
		
		/**
		Much as I would like to use Sir Quiwa's elegant modulo hack,
		I don't think I can do that here since, on every "overflow"
		I also need to update the minutes variable.
		*/
		public void actionPerformed(ActionEvent ae){
			seconds++;
			
			if(seconds == 60){
				seconds = 0;
				minutes++;
			}
			
			if(minutes == 60){
				minutes = 0;
				hours++;
			}
			
			String min = twoDigitFormat(minutes);
			String sec = twoDigitFormat(seconds);
			String hrs = twoDigitFormat(hours);
			
			timerDisplay = hrs + ":" + min + ":" + sec;
			timerLabel.setText(timerDisplay);
			String taskDescription = (String) taskBox.getSelectedItem();
			ti.setToolTip(timerDisplay + " - " + taskDescription);
		}
		
		private String twoDigitFormat(int val){
			return (val < 10) ? "0" + val : "" + val;
		}
		
	}
	
	/**
	Stops the timer and resets it back to 00:00:00.
	*/
	private class StopTimerListener implements ActionListener{
		public void actionPerformed(ActionEvent ae){
			timerLabelTimer.stop();
			seconds = 0;
			minutes = 0;
			hours = 0;
			timerLabel.setText(INITIAL_TIMER);
			timerDisplay = INITIAL_TIMER;
			ti.setToolTip(timerDisplay + " - " + APPLICATION_TITLE);
			stopButton.setEnabled(false);
			startButton.setEnabled(true);
			
			String timeLogTableName = DatabaseStatements.TBL_TIMELOGS;
			String taskPerformed = DatabaseStatements.COL_TASKPERFORMED;
			String[] autoGenerate = {DatabaseStatements.COL_TIMESTARTED, DatabaseStatements.COL_TIMEENDED};
			String endDateTime = getNow();
			try{
				stmt.executeUpdate("INSERT INTO " + timeLogTableName + " VALUES('" + startDateTime + "', '" + endDateTime + "', '" +
			                       curTaskId + "')");
			} catch(SQLException sqle){
				sqle.printStackTrace();
			}
		}
	}
	
	private class StartTimerListener implements ActionListener{
		private String taskDescription;
		
		public StartTimerListener(String td){
			taskDescription = td;
		}
		
		public StartTimerListener(){
			taskDescription = (String) taskBox.getSelectedItem();
		}
		
		public void actionPerformed(ActionEvent ae){
			if(taskDescription == null || taskDescription.equals("") ||
			   taskDescription.matches("\\s+")){
				JOptionPane.showMessageDialog(TaskTimerRunnable.this.mainFrame,
				                              "Please specify the task you are currently doing.",
				                              "Blank task",
				                              JOptionPane.ERROR_MESSAGE);
			} else{
				setCurTaskId(taskDescription);
				timerLabelTimer.start();
				startButton.setEnabled(false);
				stopButton.setEnabled(true);
				startDateTime = getNow();
			}
		}
	}
	
	private String getNow(){
		Calendar c = Calendar.getInstance();
		return sdf.format(c.getTime());
		
	}
	
	private static PopupMenu createPopUpMenu(){
		try{
			PopupMenu menu = new PopupMenu();
			MenuItem exit = new MenuItem("Exit");
			
			exit.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					System.exit(0);
				}
			});
			
			menu.add(exit);
			return menu;
		} catch(HeadlessException hle){
			hle.printStackTrace();
			return null;
		}
	}
	
	/**
	Leaf taken out of:
	http://download.oracle.com/javase/tutorial/uiswing/examples/misc/TrayIconDemoProject/src/misc/TrayIconDemo.java
	*/
	private static ImageIcon createImage(String filePath){
		//I haven't the faintest idea how this one works
		URL imageFilePath = TaskTimerRunnable.class.getResource(filePath);
		
		if(imageFilePath == null){
			//TODO: Better error handling
			System.err.println("Path does not exist.");
			return null;
		} else{
			return new ImageIcon(imageFilePath);
		}
	}
}
