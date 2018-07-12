package com.configuration;

import java.io.IOException;

public class CommandConfiguration extends Configuration {
	private String url;
	private int port;
	private String webservice;
	
	public CommandConfiguration() throws IOException {
		super();
	}
	
	public void getCommandConfiguration() throws IOException {
		
		for ( int i = 0; i <= configurationContent.length - 1; i++ ) {
			
			if ( configurationContent[ i ].startsWith("CommandUrl")) {
				url = configurationContent[ i ].split("=")[1];
				port = Integer.parseInt(configurationContent[ i + 1 ].split("=")[1]);
				webservice = configurationContent[ i + 2 ].split("=")[1];
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
	
	public String getWebService() {
		return this.webservice;
	}
	
}
