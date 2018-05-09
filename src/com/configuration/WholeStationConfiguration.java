package com.configuration;

import java.io.IOException;

public class WholeStationConfiguration extends Configuration {
	private int port;
	private int wirelessPort;
	
	public WholeStationConfiguration() throws IOException {
		super();
		// TODO Auto-generated constructor stub
	}

	public void getConnectionString() {
		
		for ( int i = 0; i <= configurationContent.length - 1; i++ ) {
			
			if ( configurationContent[ i ].startsWith("WholestationPort")) {
				port = Integer.parseInt(configurationContent[ i ].split("=")[1]);
				wirelessPort = Integer.parseInt(configurationContent[ i + 1 ].split("=")[1]);
			}
			
		}
		
	}
	
	public int getPort() {
		return this.port;
	}
	
	public int getWirelessPort() {
		return this.wirelessPort;
	}
	
}
