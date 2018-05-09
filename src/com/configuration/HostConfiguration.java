package com.configuration;

import java.io.IOException;

public class HostConfiguration extends Configuration {
	private String url;
	private int port;
	
	public HostConfiguration() throws IOException {
		super();
		// TODO Auto-generated constructor stub
	}

	public void getHostConfiguration() {
		
		for ( int i = 0; i <= configurationContent.length - 1; i++ ) {
			
			if ( configurationContent[ i ].startsWith("HostUrl")) {
				url = configurationContent[ i ].split("=")[1];
				port = Integer.parseInt(configurationContent[ i + 1 ].split("=")[1]);
				break;
			}
			
		}
		
	}
	
	public String getUrl() {
		return this.url;
	}
	
	public int getPort() {
		return this.port;
	}
	
}
