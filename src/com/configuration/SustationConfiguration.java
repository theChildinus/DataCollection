package com.configuration;

import java.io.IOException;

public class SustationConfiguration extends Configuration {
	private String ip;
	private int port;
	
	public SustationConfiguration() throws IOException {
		super();
		// TODO Auto-generated constructor stub
	}

	public void getConnectionString() {
		
		for ( int i = 0; i <= configurationContent.length - 1; i++ ) {
			
			if ( configurationContent[ i ].startsWith("SubstationUrl")) {
				ip = configurationContent[ i ].split("=")[1];
				port = Integer.parseInt(configurationContent[ i + 1 ].split("=")[1]);
			}
			
		}
		
	}
	
	public String getIP() {
		return this.ip;
	}
	
	public int getPort() {
		return this.port;
	}
}
