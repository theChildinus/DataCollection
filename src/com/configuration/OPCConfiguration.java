package com.configuration;

import java.io.IOException;

public class OPCConfiguration extends Configuration {
	private String directory;
	private String deviceid;
	private String opcTime;
	
	public OPCConfiguration() throws IOException {
		super();
	}
	
	public void getOPCConnection() {
		
		for ( int i = 0; i <= configurationContent.length - 1; i++ ) {
			
			if ( configurationContent[ i ].startsWith("OPCDirectory") ) {
				directory = configurationContent[ i ].split("=")[1];
				deviceid = configurationContent[ i + 1 ].split("=")[1];
				opcTime = configurationContent[ i + 2 ].split("=")[1];
			}
			
		}
		
	}
	
	public String getDirectory() {
		return this.directory;
	}
	
	public String getDeviceid() {
		return this.deviceid;
	}
	
	public String getOpcTime() {
		return this.opcTime;
	}
}
