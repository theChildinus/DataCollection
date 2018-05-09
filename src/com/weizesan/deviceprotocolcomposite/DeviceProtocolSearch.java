package com.weizesan.deviceprotocolcomposite;


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.net.*;

import com.weizesan.connection.databasecomposite.DataBaseComposite;
import com.weizesan.protocolcomposite.CDeviceProtocol;
import com.weizesan.protocolcomposite.CH700;
import com.weizesan.protocolcomposite.CIHDC;


public class DeviceProtocolSearch  {
	
	private String ip;             //所接入的客户端ip
	private String deviceProtocol;  //设备层使用的是什么协议，IHDC还是其它
	private String physicalAddress;    //从报文中解析出来真正的设备号，与配置表中的设备号应该对应，如30303030303031323334解析后应该是1234
	private int port;              //所接入的客户端的端口
	
	private byte byteNumber[];  //接收到登录包中的设备号，即DTU的设备号，此设备号为原始值，如30303030303031323334
	private byte byteStream[]; //接收到的数据包	
	private int recordNumber;      //查询到设备层使用的协议后，应该保存其记录号，方便于之后查询数据层的协议等等
	private Socket socket;     //接入客户端的socket
		
	public static CDeviceProtocol pp;

	public boolean AnalyzeData(byte stream[],Socket socket,int length) throws Exception {
		
		this.socket=socket;
		ip=socket.getInetAddress().toString().substring(1);
		port=socket.getPort();
		byteStream=Arrays.copyOfRange(stream, 0, length);
		
		if(IsIHDC()){        	    
			pp= new CIHDC(recordNumber,socket);
		    System.out.println("**********搜素到IHDC协议,终端设备号是:"+physicalAddress);
		    return true;			
		}		
		else if(IsH700()){   
			pp= new CH700(recordNumber,socket);
		    System.out.println("**********搜素到H700协议,终端设备号是:"+physicalAddress);
			return true;
		}   
		return false;		
}
	
		
	public boolean IsIHDC() throws Exception {
		physicalAddress=GetDeviceNumber(); 
		if (CheckDeviceNumber()) { 
			if (deviceProtocol.equals("IHDC")) {
				return true;
			} else{
				return false;
			}				
		} else {
			if (socket != null)
				socket.close();
			return false;
		}
	}
	
		
	public boolean IsH700() throws Exception {
		physicalAddress=GetDeviceNumber();
		if (CheckDeviceNumber()) {
			if (deviceProtocol.equals("H700")) {
				return true;
			} else {
				return false;
			}
		} else {
			if (socket != null)
				socket.close();
			return false;
		}
	}
		
	public boolean CheckDeviceNumber() throws Exception{
				
		DataBaseComposite dc=new DataBaseComposite();
		Connection conn=dc.getmysql();
		Statement stmt=conn.createStatement();
		String queryDeviceNumber="select * from deviceinfo where device_physical_address='"+physicalAddress+"' ";
		ResultSet rs=stmt.executeQuery(queryDeviceNumber);
		if(rs.next())
		{
			deviceProtocol=rs.getString("device_protocol");  
			recordNumber=rs.getInt("record_number");                   

			String updateDataBase="update deviceinfo set device_ipaddress='"+ip+"',device_port='"+port+"' where device_physical_address='"+physicalAddress+"' ";
			stmt.executeUpdate(updateDataBase);	
			stmt.close();
			conn.close();
			return true;
		}
		else{
			stmt.close();
			conn.close();
			return false;
		}			
	}
	
	public String GetDeviceNumber(){
		
		byteNumber=Arrays.copyOfRange(byteStream, 4, 15);
		char number[] = new char[11] ;                   
		for(int i=0,j=0;i<byteNumber.length;i++){       
			char c=(char)byteNumber[i];               
			number[j]=c;                                  
			j++;		
		}
		return new String(number);              
	}
}
