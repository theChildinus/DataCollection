package com.zuowenfeng.test;

import javax.xml.ws.Endpoint;

import com.zuowenfeng.AgentComposite.util.NotificationProcessImpl;

public class StartingServerApp {

	public static void main ( String[] args ) {
		NotificationProcessImpl implementor = new NotificationProcessImpl();
		String localhost = "http://10.108.165.37:9000/INotificationProcess";
		Endpoint.publish(localhost, implementor);
	}
}
