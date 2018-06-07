package com.liubao.substationprotocol.DAO;

//import java.io.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
//import java.util.*;
import java.util.ArrayList;
import java.util.concurrent.*;

import com.configuration.WholeStationConfiguration;
import com.factory.ServicemixConfFactory;
import com.zuowenfeng.connection.viewcomposite.MainConfigurationDialog;

public class ReceiveData {
	int port;
	public static ArrayList<EstablishedSocketList>  establishedSocketList = new ArrayList<EstablishedSocketList>();
	
	public static void main(String[] args) throws IOException{	
		ServicemixConfFactory factory = new ServicemixConfFactory();
		factory.createServicemixConfInstance();
		MainConfigurationDialog dialog = new MainConfigurationDialog(null);
		
		WholeStationConfiguration conf = new WholeStationConfiguration();
		conf.getConnectionString();
		factory.createServicemixConfInstance();
		
		try{
			ServerSocket s = new ServerSocket(conf.getPort());
			ExecutorService executorService = Executors.newFixedThreadPool(10);
			
			while(true){
				System.out.println("Waiting for connect...");
				Socket incoming = s.accept();
				System.out.println("aaaaa");
				EstablishedSocketList establishedSocket = new EstablishedSocketList();
				establishedSocket.incoming = incoming;
				BufferedReader br = new BufferedReader(new InputStreamReader(incoming.getInputStream()));
				establishedSocket.hostName = br.readLine();
				System.out.println("hostName:"+establishedSocket.hostName);
				//br.close();
				for(int i = 0;i < establishedSocketList.size();i++){
					if(establishedSocket.hostName.equals(establishedSocketList.get(i).hostName))
						establishedSocketList.remove(i);
				}//更新Socket连接列表
				establishedSocketList.add(establishedSocket);
				
				executorService.execute(new MultiThreadsHandler(incoming, 4));
				//MultiThreadsHandler handler = new MultiThreadsHandler(incoming);
				//handler.run();
				/*Runnable r = new MultiThreadsHandler(incoming);
				Thread t = new Thread(r);
				t.start();*/
			}
		}
		
		catch(Exception e){
			e.printStackTrace();
		}
	}  //end of main
	
	public ReceiveData( int port ) {
		this.port = port;
	}
	
	public void startReceiver() throws IOException {
		ServerSocket s = new ServerSocket(port);
		
		while ( true ) {
			System.out.println("Waiting for connect...");
			Socket incoming = s.accept();
			EstablishedSocketList establishedSocket = new EstablishedSocketList();
			establishedSocket.incoming = incoming;
            System.out.println("ReceiveData Socket is: " + incoming);
			BufferedReader br = new BufferedReader(new InputStreamReader(incoming.getInputStream()));
			establishedSocket.hostName = br.readLine();
			System.out.println("hostName:"+establishedSocket.hostName);
			//br.close();
			for(int i = 0;i < establishedSocketList.size();i++){
				if(establishedSocket.hostName == establishedSocketList.get(i).hostName)
					establishedSocketList.remove(i);
			}//更新Socket连接列表
			establishedSocketList.add(establishedSocket);
			Thread handler = new Thread(new MultiThreadsHandler(incoming, 4));
			handler.start();
		}
		
	}
	
}  // end of the class

