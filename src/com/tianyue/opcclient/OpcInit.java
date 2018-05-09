package com.tianyue.opcclient;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

/**
 * opcInit类用于初始化OPC客户端。
 * 每个OpcClientAPI实例对应某一个server-group组合
 * 如果需要访问多个server或group，请new多个opcInit实例并分别配置
 * 其构造函数参数为配置文件的地址（建议使用绝对路径），
 * readConfig方法实现对配置文件的读取和解析。
 * 解析后的相关信息存储于对象内部，在建立OPC连接的时候调用。
 * ArrayList<opcItem> itemList 用于在建立OPC连接前存储item列表
 * @author tian
 */

public class OpcInit {
	private File file;
	private Scanner input;
	private String s, s1, s2,s3,s4; //用于读取配置文件时的字符串处理操作
	public String IPAddress;		//OPC服务器IP地址
	public String serverName;		//OPC服务器名
	public String groupName;		//OPC服务器组名（一般为Group0）
	public ArrayList<OpcItem> itemList;
	public int itemCnt;				//统计当前添加的变量数量
	public int connectionId;   		//区分多个OPC服务器连接
									//一个OpcInit对象只对应一个Opc服务器连接
									//MainControlPanel中的basicAddress加OpcInit中的connetionId所得地址
									//应对应H2表中的DEVICE_ID字段
	/**
	 * 构造函数
	 * @param configFileName
	 * 其构造函数参数为配置文件的地址（建议使用绝对路径）
	 */
	public OpcInit(String configFileName, int connectionId) {
		file = new File(configFileName);
		input = null;
		itemList = new ArrayList<OpcItem>();
		itemCnt = 0;
		this.connectionId = connectionId;
	}
	/**
	 * 具体配置的实现
	 * @return
	 * 文件未找到则返回-1，配置完成则返回0
	 */
	public int readConfig() {
		try {
			input = new Scanner(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
		while (input.hasNext()) {
			s = input.nextLine();
			if (s.startsWith("//")) //注释行
				continue;
			s1 = s.substring(0, s.indexOf('='));
			s2 = s.substring(s.indexOf('=') + 1, s.length());
			if (s1.equals("IP"))
			    IPAddress = s2.trim();
			else 
			if (s1.equals("ServerName"))
				serverName = s2.trim();
			else
			if (s1.equals("GroupName"))
				groupName = s2.trim();
			else{   //变量定义行
				s3 = s2.substring(s2.indexOf('=') + 1, s2.length()).trim();
				s2 = s2.substring(0, s2.indexOf('=')).trim(); //变量名
				s4 = s3.substring(s3.indexOf('=') + 1, s3.length()).trim();//读写属性
				s3 = s3.substring(0, s3.indexOf('=')).trim(); //数据类型
				System.out.println(s1+" "+s2+" "+s3+" "+s4);
				OpcItem item = new OpcItem();
				item.name = s2;
				
				if (s3.equals("float"))
					item.valueType = 4;
				else
				if (s3.equals("double"))
					item.valueType = 5;
				else
				if (s3.equals("int"))
					item.valueType = 22;
				else
				if (s3.equals("bool"))
					item.valueType = 11;

				item.address = Integer.toHexString(itemCnt); 
				
				item.readable = (s4.startsWith("R"))?true:false;
				item.writable = (s4.endsWith("W"))?true:false;
				
				itemList.add(item);
				itemCnt++;		    	
			}
		}
		return 0;
	}
	
	public String getItemNameByAddress(String address){
		Iterator<OpcItem> it = itemList.iterator();
		OpcItem oi;
		while (it.hasNext())
		{
			oi = it.next();
			if (oi.address.equals(address))
				return oi.name;
		}
		return "null";
	}
	
	public int getItemIndexByName(String itemName){
		Iterator<OpcItem> it = itemList.iterator();
		OpcItem oi;
		int cnt=0;
		while (it.hasNext())
		{
			oi = it.next();
			if (oi.name.equals(itemName))
				return cnt;
			cnt++;
		}
		return -1;
	}

	public static void main(String args[]) {
		//建立实例，构造函数参数为配置文件地址（建议使用绝对路径）
		OpcInit init = new OpcInit("D:/OpcConfig.ini",0);
		if (init.readConfig()==0){
			OpcClientAPI opc = new OpcClientAPI();
			System.out.println(opc.connect(init.IPAddress, init.serverName, init.groupName));
			Iterator<OpcItem> it = init.itemList.iterator();
			while (it.hasNext())
			{
				OpcItem oi = (OpcItem) it.next();
				System.out.println(oi.name);
				opc.addItem(init.connectionId, oi.name, oi.valueType, true);
			}
			while (true){
				OpcItem oi;
				it = init.itemList.iterator();
				//一次读取整组
				if (it.hasNext()){
				    opc.readFloatSync(init.connectionId, ((OpcItem)it.next()).name);
				}
				// 之后直接访问相应存储单元输出即可
				it = init.itemList.iterator();
				while (it.hasNext())
				{
					oi = (OpcItem) it.next();
					System.out.println(opc.getFloatData(init.connectionId, oi.name));
				}
	
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
			
	}
}