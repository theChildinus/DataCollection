package com.configuration;

import java.io.IOException;

public class MemoryDBConfiguration extends Configuration {
	private String url;
	private String database;
	private String username;
	private String password;
	
	public MemoryDBConfiguration() throws IOException {
		super();
	}
	
	public void getConnectionString() throws IOException {

		for ( int i = 0; i <= configurationContent.length - 1; i++ ) {
			
			if ( configurationContent[ i ].startsWith("MemoryUrl")) {
				url = configurationContent[ i ].split("=")[1];
				database = configurationContent[ i + 1 ].split("=")[1];
				username = configurationContent[ i + 2 ].split("=")[1];
				String[] temp = configurationContent[ i + 3 ].split("=");
				
				if ( temp.length == 2 ) {
					password = temp[1];
				}
				
				else
					password = "";
			}
			
		}
		
	}
	
	public String getUrl() {
		return this.url;
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
