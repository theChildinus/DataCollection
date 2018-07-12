package com.zuowenfeng.monitor;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Locale;
import java.util.ResourceBundle;

public class LocalMonitor {

	public static void main ( String[] args ) throws InterruptedException, IOException  {
		ServerSocket socket = null;
		ResourceBundle bundle = ResourceBundle.getBundle("com.zuowenfeng.conf.localConfiguration", Locale.ENGLISH);
		
		while ( true ) {
			try {
				socket = new ServerSocket(9000);
				socket.close();
				IPConfiguration delete = new IPConfiguration(bundle.getString("url"));
				delete.IPDeletion();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("Error");
			}
		
			Thread.sleep(5000);
		}
	}
}
