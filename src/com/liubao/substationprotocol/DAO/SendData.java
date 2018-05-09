package com.liubao.substationprotocol.DAO;

import java.net.*;
import java.io.*;
import java.util.*;

import com.configuration.SustationConfiguration;
import com.factory.ServicemixConfFactory;
import com.zuowenfeng.connection.viewcomposite.MainConfigurationDialog;
import com.zuowenfeng.variable.DeviceStaticData;

public class SendData {
	static GetDataFromH2 data = null; 
	static AllTables allTables =null;
	static DeviceStaticData deviceData = new DeviceStaticData(); //命令中需要修改的参数
	
	static Socket sock = null;
	
	public static  Socket initSocket(String ip, int port) throws InterruptedException{
		Socket socket;
		try{
			 socket = new Socket(ip,port);
		}catch(IOException ioe){
			System.out.println("---断线，1分钟后重连---");
			//new Thread();
			/*延时一分钟*/
			Thread.sleep(1000);
			return null;
		}
		return socket;
	}
	
	public static void main(String[] args) throws IOException{
		ServicemixConfFactory factory = new ServicemixConfFactory();
		factory.createServicemixConfInstance();
		MainConfigurationDialog dialog = new MainConfigurationDialog(null);
		factory.createServicemixConfInstance();
		final SustationConfiguration subs = new SustationConfiguration();
		subs.getConnectionString();
		
		try{
		sock = initSocket(subs.getIP(),subs.getPort()); //建立Socket连接
		String hostname = InetAddress.getLocalHost().getHostName();
		PrintWriter pw = new PrintWriter(sock.getOutputStream(), true );
		pw.println(hostname);

		}catch(Exception e){
			e.printStackTrace();
		}
		
		Timer timer = new Timer();
		
		timer.schedule(new TimerTask(){
			public void run(){
				
				while(sock == null || sock.isClosed() ==true){
					try {
						sock = initSocket(subs.getIP(),subs.getPort());
						
						if ( sock != null ) {
							String hostname = InetAddress.getLocalHost().getHostName();
							PrintWriter pw = new PrintWriter(sock.getOutputStream(), true );
							pw.println(hostname);
						}
						
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						sock = null;
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						sock = null;
					}
				}
				
				data = new GetDataFromH2();
				
				
				try{
					allTables = data.getAllTables();
					ObjectOutputStream  clientOutputStream = new ObjectOutputStream(sock.getOutputStream());
				
					if ( sock != null ) {
						clientOutputStream.writeObject(allTables);
						System.out.println(new Date().getTime());
					}

					//clientOutputStream.close();
				}catch(IOException ioe){			
					sock = null;
				}
			}
		}, 0,3000);  //end of timer.schedule()
	
	}  //end of main
	
	public SendData( String url, int port ) throws UnknownHostException, IOException {
		final String surl = url;
		final int sport = port;
		
//		sock = new Socket(url, port);
//		
//		String hostname = InetAddress.getLocalHost().getHostName();
//		PrintWriter pw = new PrintWriter(sock.getOutputStream(), true );
//		pw.println(hostname);
		//pw.close();
//		Timer timer = new Timer();
//		
//		timer.schedule(new TimerTask() {
//
//			@Override
//			public void run() {
//				// TODO Auto-generated method stub
//				
//				while(sock == null || sock.isClosed() ==true){
//					try {
//						sock = SendData.initSocket(surl,sport);
//						
//						if ( sock != null ) {
//							String hostname = InetAddress.getLocalHost().getHostName();
//							PrintWriter pw = new PrintWriter(sock.getOutputStream(), true );
//							pw.println(hostname);
//						}
//						
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						sock = null;
//					} catch (UnknownHostException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						//e.printStackTrace();
//						sock = null;
//					}
//				}
//				
//				data = new GetDataFromH2();
//				
//				
//				try {
//					allTables = data.getAllTables();
//					ObjectOutputStream clientOutputStream = new ObjectOutputStream(sock.getOutputStream());
//					
//					if ( sock != null ) {
//						clientOutputStream.writeObject(allTables);
//					}
//					
//				}
//				
//				catch( IOException ioe ) {				
//					sock = null;
//				}
//				
//			}
//			
//		}, 0, 30000);
		
		while ( true ) {
			
			while ( sock == null || sock.isClosed() == true ) {
				
				try {
					sock = SendData.initSocket(surl, sport);
					
					if ( sock != null ) {
						String hostname = InetAddress.getLocalHost().getHostName();
						PrintWriter pw = new PrintWriter(sock.getOutputStream(), true );
						pw.println(hostname);
					}
					
				}catch (InterruptedException e) {
					// TODO Auto-generated catch block
					sock = null;
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
					sock = null;
				}
				
			}
			
			data = new GetDataFromH2();
			
			
			try {
				allTables = data.getAllTables();
				ObjectOutputStream clientOutputStream = new ObjectOutputStream(sock.getOutputStream());
				
				if ( sock != null ) {
					clientOutputStream.writeObject(allTables);
				}
				
			}
			
			catch( IOException ioe ) {				
				sock = null;
			}
			
			try {
				Thread.sleep(180000);
			} 
			
			catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}
	
	/*新添加函数，用于接收从主站发送的命令*/
	public static DeviceStaticData receiveCommand() throws ClassNotFoundException{
		
		try{
			
			if ( sock != null ) {
				InputStream is = sock.getInputStream();
				ObjectInputStream ois = new ObjectInputStream(is);
				DeviceStaticData.device_id.add(((ArrayList<String>) ois.readObject()).get(0));
				DeviceStaticData.plc_id.add(((ArrayList<String>) ois.readObject()).get(0));
				DeviceStaticData.sensor_id.add(((ArrayList<String>) ois.readObject()).get(0));
				DeviceStaticData.value.add(((ArrayList<Float>) ois.readObject()).get(0));
				DeviceStaticData.type.add(((ArrayList<Integer>) ois.readObject()).get(0));
				DeviceStaticData.send.add(new Boolean(true));
			
				System.out.println("deviceid:" + deviceData.device_id.get(DeviceStaticData.device_id.size() - 1 ) );
				System.out.println("plcid:" + deviceData.plc_id.get(DeviceStaticData.plc_id.size() - 1 ) );
				System.out.println("sensorid:" + deviceData.sensor_id.get(DeviceStaticData.sensor_id.size() - 1 ) );
			}
			
		}catch(IOException ioe){
			sock = null;
		}
		catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		return deviceData;
	}
	
}
