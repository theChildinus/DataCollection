package com.zuowenfeng.AgentComposite.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import com.configuration.MemoryDBConfiguration;

public class DataBaseComponentImpl implements DataBaseComponent {

	public String realtime;

	public String realtimeuser;

	public String realtimepassword;

	public String persistentpermanent; 

	public String persistenttest; 

	public String persistentuser;

	public String persistentpassword;
	
	public MemoryDBConfiguration conf;
	
	public DataBaseComponentImpl() {
		
		try {
			conf = new MemoryDBConfiguration();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public Connection geth2() throws FileNotFoundException {
		// TODO Auto-generated method stub
		
		try {
			Class.forName("org.h2.Driver");
			conf.getConnectionString();
			Connection conn = DriverManager.getConnection("jdbc:h2:tcp://" + conf.getUrl() + "/" + conf.getDatabase(), conf.getUsername(), conf.getPassword());
			return conn;
			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

}
