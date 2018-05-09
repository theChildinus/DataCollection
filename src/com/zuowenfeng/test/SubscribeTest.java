package com.zuowenfeng.test;

import javax.xml.ws.Endpoint;

import wsn.wsnclient.command.SendWSNCommand;

import com.zuowenfeng.AgentComposite.util.NotificationProcessImpl;

public class SubscribeTest {

	public static void main ( String[] args ) throws Exception {
		SendWSNCommand command = new SendWSNCommand("http://10.108.164.8:9000/INotificationProcess4", "http://10.108.165.37:8193");
//		String response = command.createPullPoint();
		String response = command.createPullPoint();
		
		if ( response.equals("ok")) {
			String response2 = command.subscribe("all");
			//command.getMessage("2");
			
			if ( response2.equals("ok")) {
				System.out.println("Subscribe successfully.");
			}
			
			else {
				System.out.println("Failure in subscribe.");
			}
			
		}
	
		else {
			System.out.println("Failure in createPullPoint");
		}

	}
}
