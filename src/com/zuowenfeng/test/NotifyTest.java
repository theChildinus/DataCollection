package com.zuowenfeng.test;

import wsn.wsnclient.command.SendWSNCommandWSSyn;;


public class NotifyTest {

	public static void main ( String[] args ) throws Exception {
		SendWSNCommandWSSyn command = new SendWSNCommandWSSyn("http://10.108.165.37:9003/INotificationProcess", "http://10.108.165.37:8181/cxf/NotificationProxy");
		String content = "<coolsql xmlns=\"\" xmlns:ns6=\"http://docs.oasis-open.org/wsn/b-2\"><Sql>update GL_ANALOG_CONTROL set timestamp = timestamp '2012-12-05 15:14:51.578', date = date '2012-12-05', time = time '15:14:51', value = 0.0, blockvalue = 0, blockflag = 0 where device_id = '9' and plc_id = '4' and sensor_id = 'D2000';</Sql><Level>realtime</Level></coolsql><Sql>update GL_ANALOG_CONTROL set timestamp = timestamp '2012-12-05 15:14:51.593', date = date '2012-12-05', time = time '15:14:51', value = 0.0, blockvalue = 0, blockflag = 0 where device_id = '9' and plc_id = '4' and sensor_id = 'D2002';</Sql><Level>realtime</Level></coolsql><Sql>update GL_ANALOG_CONTROL set timestamp = timestamp '2012-12-05 15:14:51.609', date = date '2012-12-05', time = time '15:14:51', value = 2.0, blockvalue = 0, blockflag = 0 where device_id = '9' and plc_id = '4' and sensor_id = 'D2004';</Sql><Level>realtime</Level></coolsql><Sql>update GL_ANALOG_CONTROL set timestamp = timestamp '2012-12-05 15:14:51.64', date = date '2012-12-05', time = time '15:14:51', value = 0.0, blockvalue = 0, blockflag = 0 where device_id = '9' and plc_id = '4' and sensor_id = 'D2008';</Sql><Level>realtime</Level></coolsql>";
		System.out.println(content);
		
		for ( int i = 0; i <= 2; i++ ) {
		
		Thread thread = new Thread(new notifyThread( command ));
		thread.start();
		}
	}
	
}

class notifyThread implements Runnable {
	private SendWSNCommandWSSyn command;
	
	public notifyThread( SendWSNCommandWSSyn command ) {
		//SendWSNCommandWSSyn command = new SendWSNCommandWSSyn("http://10.108.165.37:9003/INotificationProcess", "http://10.108.165.37:8181/cxf/NotificationProxy");
		this.command = command;
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		String content = "<coolsql xmlns=\"\" xmlns:ns6=\"http://docs.oasis-open.org/wsn/b-2\"><Sql>update GL_ANALOG_CONTROL set timestamp = timestamp '2012-12-05 15:14:51.578', date = date '2012-12-05', time = time '15:14:51', value = 0.0, blockvalue = 0, blockflag = 0 where device_id = '9' and plc_id = '4' and sensor_id = 'D2000';</Sql><Level>realtime</Level></coolsql><Sql>update GL_ANALOG_CONTROL set timestamp = timestamp '2012-12-05 15:14:51.593', date = date '2012-12-05', time = time '15:14:51', value = 0.0, blockvalue = 0, blockflag = 0 where device_id = '9' and plc_id = '4' and sensor_id = 'D2002';</Sql><Level>realtime</Level></coolsql><Sql>update GL_ANALOG_CONTROL set timestamp = timestamp '2012-12-05 15:14:51.609', date = date '2012-12-05', time = time '15:14:51', value = 2.0, blockvalue = 0, blockflag = 0 where device_id = '9' and plc_id = '4' and sensor_id = 'D2004';</Sql><Level>realtime</Level></coolsql><Sql>update GL_ANALOG_CONTROL set timestamp = timestamp '2012-12-05 15:14:51.64', date = date '2012-12-05', time = time '15:14:51', value = 0.0, blockvalue = 0, blockflag = 0 where device_id = '9' and plc_id = '4' and sensor_id = 'D2008';</Sql><Level>realtime</Level></coolsql>";
		while ( true ) {
			//command.register("GL", "http://10.108.172.178:8181/cxf/NotificationProxy");
				
			command.notify("GL", content );
			command.notify("all", content);
//			try {
//				Thread.sleep(2000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
		
	}
	
}