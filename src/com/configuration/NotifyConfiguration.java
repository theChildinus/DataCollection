package com.configuration;

import java.io.IOException;

public class NotifyConfiguration extends Configuration {
	private String url;
	private int port;
	
	public NotifyConfiguration() throws IOException {
		super();
	}
	
	public void getNotifyConnection() throws IOException {
		
		for ( int i = 0; i <= configurationContent.length - 1; i++ ) {
			
			if ( configurationContent[ i ].startsWith("NotifyUrl")) {
				url = configurationContent[ i ].split("=")[1];
				port = Integer.parseInt(configurationContent[ i + 1 ].split("=")[1]);
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
