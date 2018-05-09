package com.factory;

import java.io.IOException;

import com.configuration.UpdateDBInfoConfiguration;
import com.zuowenfeng.AgentComposite.util.PublishComponentImpl;

public class PublishFactory {
	public static PublishComponentImpl comp;
	
	public void create() throws IOException {
		UpdateDBInfoConfiguration configuration = new UpdateDBInfoConfiguration();
		configuration.getUpdateConfiguration();
		comp = new PublishComponentImpl("http://" + configuration.getUrl() + ":" + configuration.getPort() + "/" + configuration.getServicename(), "http://" + ServicemixConfFactory.conf2.getUrl() + ":" + ServicemixConfFactory.conf2.getPort() + "/cxf/NotificationProxy");
	}
}
