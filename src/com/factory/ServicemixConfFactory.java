package com.factory;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.configuration.NotifyConfiguration;
import com.configuration.ServicemixConfiguration;

public class ServicemixConfFactory {
	public static ServicemixConfiguration conf;
	public static NotifyConfiguration conf2;
	
	public static void createServicemixConfInstance() throws IOException {
		conf = new ServicemixConfiguration();
		conf.getServicemixConnection();
		
		conf2 = new NotifyConfiguration();
		conf2.getNotifyConnection();
	}
	
}
