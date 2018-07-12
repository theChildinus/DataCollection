package com.configuration;

import java.io.IOException;

public class LocalConfiguration extends Configuration {
	private String url;
	private int port;
	private String servicename;
	
	public LocalConfiguration() throws IOException {
		super();
		// TODO Auto-generated constructor stub
	}

	public void getLocalConfiguration() {
		
		for ( int i = 0; i <= configurationContent.length - 1; i++ ) {
			
			if ( configurationContent[ i ].startsWith("LocalUrl")) {
				url = configurationContent[ i ].split("=")[1];
				port = Integer.parseInt(configurationContent[ i + 1 ].split("=")[1]);
				servicename = configurationContent[ i + 2 ].split("=")[1];
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
	
	public String getServicename() {
		return this.servicename;
	}
	
}
