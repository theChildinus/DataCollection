package com.zuowenfeng.AgentComposite.util;

import wsn.wsnclient.command.SendWSNCommandWSSyn;;

public class PublishComponentImpl implements PublishComponent {
	private SendWSNCommandWSSyn command;
	private String publishService;
	private String localService;
	
	public PublishComponentImpl( String publishService, String localService ) {
		this.command = new SendWSNCommandWSSyn(publishService, localService);
		//command.register("GL", localService);
	}
	
	@Override
	public void publish(String topic, String publish) throws Exception {
		// TODO Auto-generated method stub
		//SendWSNCommandWSAsyn command = new SendWSNCommandWSAsyn(publishService, localService );
		//this.command.register(topic, localService);
		command.notify(topic, publish);
	}

}
