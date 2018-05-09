package com.configuration;

import java.io.IOException;

public class ServicemixConfiguration extends Configuration {
	private String url;
	private int port;
	
	public ServicemixConfiguration() throws IOException {
		super();
	}
	
	public void getServicemixConnection() throws IOException {
		
		for ( int i = 0; i <= configurationContent.length - 1; i++ ) {
			
			if ( configurationContent[ i ].startsWith("SubscribeUrl")) {
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
