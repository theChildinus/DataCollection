package com.wenpeng.org.apache.servicemix.wsn.push;
import javax.jws.WebService;

//import edu.bupt.intt.wsmonitor.message.utility.MessageHandler;

//import edu.bupt.intt.wsmonitor.message.utility.jmsHandler;

@WebService(endpointInterface="com.wenpeng.org.apache.servicemix.wsn.push.INotificationProcess",
		serviceName="INotificationProcess")
public class NotificationProcessImpl implements INotificationProcess{

	@Override
	public void notificationProcess(String notification) {
		System.out.println("[Notification Message]: "+notification);
		System.out.println("######################1111");
//		MessageHandler messagehandler = new MessageHandler();
		System.out.println("######################");
//		System.out.println(messagehandler.onMessage(notification));
		System.out.println("######################3333");
	}
	
}

