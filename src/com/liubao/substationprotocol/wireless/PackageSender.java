/**
 *该类应在子站运行，实现的主要功能为连接H2数据库，并从数据库 中取得所需数据
 *然后将数据写入指定串口，通过串口传送到GPRS设备模块，由该模块将数据自动发送到总站。
 * 
 * */

package com.liubao.substationprotocol.wireless;
//import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
//import java.sql.DriverManager;
import java.sql.ResultSet;
//import java.sql.SQLException;
import java.sql.Statement;
//import java.util.ArrayList;
import java.util.Arrays;
//import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
//import java.util.Timer;
//import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import com.mysql.jdbc.ResultSetImpl;

//import com.configuration.MemoryDBConfiguration;


public class PackageSender {
	
	private Connection conn =null;
	private  Statement stmt =null;
	private  ResultSet rs =null;
	private String command =null;
	private static  C232 pc = null;
	private H2Connection h2con = null;
	
	
	public void send() throws Exception  {
		try{
		pc = new C232();
		while(true){
			
			if(pc != null){
	 		//j建立数据库的相关连接
	 		h2con = new H2Connection();
	 		conn = h2con.getConnection();
	 		stmt = conn.createStatement();
	 		
	 		//发送表GL_ANALOG_MEASURE的数据
	 		packTable(stmt,"GL_ANALOG_MEASURE");
	 		TimeUnit.SECONDS.sleep(2);
	 		
//	 		//发送表GL_ANALOG_CONTROL的数据
//	 		packTable(stmt,"GL_ANALOG_CONTROL");
//	 		TimeUnit.SECONDS.sleep(2);
//	 		
//	 		//发送表GL_DIGITAL_MEASURE的数据
//	 		packTable(stmt,"GL_DIGITAL_MEASURE");
//	 		TimeUnit.SECONDS.sleep(2);
//	 		
//	 		//发送表GL_DIGITAL_CONTROL的数据
//	 		packTable(stmt,"GL_DIGITAL_CONTROL");
//	 		TimeUnit.SECONDS.sleep(2);
	 		
	 		TimeUnit.SECONDS.sleep(5);
	 		}else
	 			pc = new C232();
		}
	 	}finally{
	 		h2con.closeAll(conn, stmt, rs);
	 	}
	}
	
	
	//打包数据库中表的内容
	public void packTable(Statement stmt,String tableName) throws  Exception{
		
		 //最后要发送的数据，data[0]表示表名；data[1]该表中device_id的数目；data[2]表示第一个device_id号；data[3]表示device_id为data[2]的记录的条数；
		//data[(int)data[3]+4]表示第二个device_id号；data[(int)data[3]+5]表示device_id为data[(int)data[3]+4]的记录的条数；依此可向下类推
		 float[] data= new float[252];      //根据GPRS模块限制，每个包最多1024Byte，而非用户数据部分有16Byte，所以float[]最多252
		
		 float[] table =null;  
		 byte[] bs =null;
		 int device_id_count =0 ;     //用来记录device_id的个数
		 int i;      //data[]数组的索引
		 int recordCount = 0;     //总记录的条数
		 int len = 0;	//发送数据的长度
		 HashMap<String,Integer> dc = null;
		String device_id = null;
		
		System.out.println("以下是表"+tableName+"的数据");
		if(tableName == "GL_ANALOG_MEASURE")
			data[0] = 1.0f; //1.0f表示表GL_ANALOG_MEASURE
		if(tableName == "GL_ANALOG_CONTROL")
			data[0] = 2.0f;
		if(tableName == "GL_DIGITAL_MEASURE")
			data[0] = 3.0f;
		if(tableName == "GL_DIGITAL_CONTROL")
			data[0] =4.0f;
		command = "SELECT DEVICE_ID,COUNT(*) FROM "+tableName+" GROUP BY DEVICE_ID";
		
		rs = stmt.executeQuery(command);System.out.println("1");
		dc = new HashMap<String,Integer>();
		
		
		//取得所有device_id及其所对应的记录的条数
		while(rs.next()){
			dc.put(rs.getString("DEVICE_ID"), rs.getInt("COUNT(*)")); 
			device_id_count++;
		}
		data[1] =(float) device_id_count; //data[1]表示device_id的个数
		System.out.println("######"+"HashMap<String,Integer> dc = "+dc);

		
		i=2; //data[2]代表了第一个device_id的值
		Iterator<String> it = dc.keySet().iterator();
		while(it.hasNext()){
			device_id = (String)it.next();
			command = "SELECT VALUE FROM "+tableName+"  WHERE DEVICE_ID ="+device_id+"ORDER BY TOPIC";
			rs = stmt.executeQuery(command);
			data[i++] = Float.valueOf(device_id);
			data[i++] = (float) dc.get(device_id);
			recordCount += dc.get(device_id);
			while(rs.next()){
				data[i++] = rs.getFloat("VALUE");
			}
		}
		
		System.out.println("recordCount = "+recordCount);
		len = recordCount+device_id_count*2+2;
		System.out.println("发送数据的总长度len ="+len);
		table = new float[len];
		System.arraycopy(data,0,table,0,len);
		System.out.println("Table = "+Arrays.toString(table));
		bs = new byte[len*4];
		floatArrayToByteArray(table,bs);
	     System.out.println("Data to send is:"+Arrays.toString(bs));
	     System.out.println("字节流长度为："+bs.length);
		
	    pc.Write(bs); //将数据写入串口
	    //我是僵尸代码……
//	     command = "SELECT VALUE,Device_id FROM GL_ANALOG_MEASURE  WHERE DEVICE_ID =9 ORDER BY TOPIC";
//	     rs = stmt.executeQuery(command);
//	     SResultSet srs = new SResultSet(rs);	     
//	    pc.WriteObject(new String("ssss"));
	}
	
	
	//将float类型的数据转换为byte数组
	public static byte[] getByteArray(float x) {
		int index=0;
	    byte[] bb = new byte[4];
	    int l = Float.floatToIntBits(x);
	    for (int i = 0; i < 4; i++) {
	        bb[index + i] = new Integer(l).byteValue();
	        l = l >> 8;
	    }
	    return bb;
	}
	
	//float[]转换为byte[]
	public static void floatArrayToByteArray(float[] ff,byte[] bt){
		for(int i = 0;i<ff.length;i++){
			byte[] bb = new byte[4];
			bb= getByteArray(ff[i]);
			for(int j=0;j<bb.length;j++)
				bt[j+i*4] = bb[j];
		}
	}
	
	
	public static void main(String[] args) throws Exception{
		//final PackageSender ps = new PackageSender();
		//pc  = new C232();  //选定并打开串口
		
//		while(true){
//			try{
//			send();
//			Thread.sleep(60*1000);
//			}catch(Exception e){
//				System.out.println("写串口错误，重新打开串口！");
//				pc =new C232();
//			}
//		}
		PackageSender ps = new PackageSender();
	
			ps.send();
	}
}

class SResultSet implements Serializable{
	private ResultSet rs= null;
	
	public SResultSet(ResultSet rs) {
		this.rs = rs;		
	}
}
	

