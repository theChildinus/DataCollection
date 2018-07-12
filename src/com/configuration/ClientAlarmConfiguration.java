package com.configuration;

import java.io.IOException;

public class ClientAlarmConfiguration extends Configuration {
	private String url;
	private int port;
	private String servicename;
	
	public ClientAlarmConfiguration() throws IOException {
		super();
	}
	
	public void getClientAlarmConfiguration() throws IOException {
		
		for ( int i = 0; i <= configurationContent.length - 1; i++ ) {
			
			if ( configurationContent[ i ].startsWith("ClientAlarmUrl") ) {
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
