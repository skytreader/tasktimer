package net.skytreader.kode.tasktimer;

import net.skytreader.kode.utils.MyFrame;

import java.awt.Container;

import java.io.IOException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

public class DBConfigManager implements Runnable{
	
	private final int WIDTH = 300;
	private final int HEIGHT = 200;
	private final String EDIT_CONFIG_TITLE = "DB Configurations";
	private DatabaseStatements ds;
	
	public DBConfigManager(){
		try{
			ds = DatabaseStatements.getInstance();
		} catch(IOException ioe){
			ioe.printStackTrace();
		}
	}
	
	public void run(){
		JFrame mainFrame = new MyFrame(WIDTH, HEIGHT, EDIT_CONFIG_TITLE);
		mainFrame.setResizable(false);
		mainFrame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		Container mainFrameContainer = mainFrame.getContentPane();
		mainFrameContainer.setLayout(new BoxLayout(mainFrameContainer, BoxLayout.Y_AXIS));
		
		String[] configLabels = {"Server", "Username", "Password", "Database"};
		for(int i = 0; i < 4; i++){
			JLabel configLabel = new JLabel(configLabels[i]);
			JTextField configValue = (i == 2) ? new JPasswordField(getValue(i), 10) :
			                         new JTextField(getValue(i), 10);
			JPanel configField = new JPanel();
			configField.setLayout(new BoxLayout(configField, BoxLayout.X_AXIS));
			configField.add(configLabel);
			configField.add(configValue);
			mainFrameContainer.add(configField);
		}
		
		JPanel buttonPanel = new JPanel();
		JButton okButton = new JButton("  OK  ");
		JButton cancelButton = new JButton("Cancel");
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		mainFrameContainer.add(buttonPanel);
	}
	
	private String getValue(int config){
		switch(config){
			case 0:
				return ds.SERVER_NAME;
			case 1:
				return ds.MYSQL_USERNAME;
			case 2:
				return ds.MYSQL_PASSWORD;
			case 3:
				return ds.DB_NAME;
			default:
				return "";
		}
	}
}
