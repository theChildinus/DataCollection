package com.configuration;

import java.io.IOException;

public class DeviceInfoConfiguration extends Configuration {
	private String url;
	private int port;
	private String database;
	private String username;
	private String password;
	
	public DeviceInfoConfiguration() throws IOException {
		super();
	}
	
	public void getConnectionString() throws IOException {

		for ( int i = 0; i <= configurationContent.length - 1; i++ ) {
			
			if ( configurationContent[ i ].startsWith("DeviceUrl")) {
				url = configurationContent[ i ].split("=")[1];
				port = Integer.parseInt(configurationContent[ i + 1 ].split("=")[1]);
				database = configurationContent[ i + 2 ].split("=")[1];
				username = configurationContent[ i + 3 ].split("=")[1];
				password = configurationContent[ i + 4 ].split("=")[1];
				break;
			}
			
		}
		
	}
	
	public String getURL() {
		return this.url;
	}
	
	public int getPort() {
		return this.port;
	}
	
	public String getDatabase() {
		return this.database;
	}
	
	public String getUsername() {
		return this.username;
	}
	
	public String getPassword() {
		return this.password;
	}
	
}
