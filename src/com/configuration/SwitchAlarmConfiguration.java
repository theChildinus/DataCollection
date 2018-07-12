package com.configuration;

import java.io.IOException;

public class SwitchAlarmConfiguration extends Configuration {
	private String url;
	private int port;
	private String servicename;
	
	public SwitchAlarmConfiguration() throws IOException {
		super();	
	}
	
	public void getAlarmConnection() throws IOException {

		for ( int i = 0; i <= configurationContent.length - 1; i++ ) {
			
			if ( configurationContent[ i ].startsWith("SwitchAlarmUrl")) {
				url = configurationContent[ i ].split("=")[1];
				port = Integer.parseInt(configurationContent[ i + 1 ].split("=")[1]);
				servicename = configurationContent[ i + 2 ].split("=")[1];
			}
		}
	}
	
	public String getUrl() {
		return this.url;
	}
	
	public int getPort() {
		return this.port;
	}
	
	public String getServiceName() {
		return this.servicename;
	}
	
}
