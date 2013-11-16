package net.skytreader.kode.tasktimer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import java.util.HashMap;

public class ConfigReader{
	
	private final String valueDelimeter = " = ";
	
	private BufferedReader cfReader;
	private HashMap<String, String> configValues;
	
	public ConfigReader(String configFilename) throws IOException{
		cfReader = new BufferedReader(new FileReader(configFilename));
		configValues = new HashMap<String, String>();
		loadValues();
	}
	
	private void loadValues() throws IOException{
		String assignment = cfReader.readLine();
		
		while(assignment != null){
			String[] decompose = assignment.split(valueDelimeter);
			if(decompose.length == 2){
				configValues.put(decompose[0], decompose[1]);
			} else{
				configValues.put(decompose[0], "");
			}
			assignment = cfReader.readLine();
		}
		
		cfReader.close();
	}
	
	public String getConfigValue(String configVariable){
		return configValues.get(configVariable);
	}
	
}
