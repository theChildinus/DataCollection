package com.zuowenfeng.monitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.Locale;
import java.util.ResourceBundle;

import com.configuration.HostConfiguration;
import com.configuration.LocalConfiguration;

public class SendTest {
	private Socket socket;
	private PrintWriter pw;
	private Thread thread1;
	//private ResourceBundle bundle = ResourceBundle.getBundle("dao.Connection", Locale.ENGLISH);
	//private ResourceBundle bundle2 = ResourceBundle.getBundle("com.zuowenfeng.conf.localConfiguration", Locale.ENGLISH);
	//private ResourceBundle bundle3 = ResourceBundle.getBundle("com.zuowenfeng.conf.hostConfiguration", Locale.ENGLISH);
	private HostConfiguration hc = new HostConfiguration();
	private LocalConfiguration lc = new LocalConfiguration();
	
	public SendTest() throws UnknownHostException, IOException, ClassNotFoundException, SQLException {
		//String ip = bundle.getString("url");
		hc.getHostConfiguration();
		lc.getLocalConfiguration();
		//String ip = bundle2.getString("url");
		String ip = lc.getUrl();
		int port = lc.getPort();
		//ServerSocket servers = new ServerSocket(port);
		IPConfiguration conf = new IPConfiguration(ip);
		conf.IPDeletion();
		socket = new Socket(hc.getUrl(), hc.getPort());
		pw = new PrintWriter(socket.getOutputStream(), true );
		pw.println("" + ip + " " + port);
		
		BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		String msg = br.readLine();
		
		if ( msg.equals("wait")) {
			br.readLine();
		}
		
		//IPConfiguration conf = new IPConfiguration(ip);
		conf.IPAddition();
		thread1 = new Thread(new Listener());
		thread1.start();
		Thread thread2 = new Thread(new backups());
		thread2.start();
	}
	
	public static void main (String[] args ) throws UnknownHostException, IOException, ClassNotFoundException, SQLException {
		SendTest test = new SendTest();
	}
	
	class Listener implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
				//BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				
			while ( true ) {
				pw.println("response");
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
		
	}
	
	class backups implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			while ( true ) {
				
				try {
					BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					String msg = br.readLine();
					
					if ( msg.indexOf("delete") != -1 ) {
						System.out.println("Deleting IP...");
						String[] splits = msg.split(" ");
						IPConfiguration conf = new IPConfiguration(splits[1]);
						conf.IPDeletion();
						System.out.println("Delete successfully.");
						pw.println("success");
					}
					
					else if ( msg.indexOf("add") != -1 ) {
						System.out.println("Adding IP...");
						//System.out.println("IP: " + InetAddress.getLocalHost().getHostAddress() + " " + msg );
//						Backup backupIP = new Backup(new String[]{InetAddress.getLocalHost().getHostAddress(),msg});
						//Backup backs = new Backup(msg);
						String[] splits = msg.split(" ");
						IPConfiguration conf = new IPConfiguration(splits[1]);
						conf.IPAddition();
						System.out.println("Add successfully.");
						pw.println("success");
					}
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}
		
	}
}
