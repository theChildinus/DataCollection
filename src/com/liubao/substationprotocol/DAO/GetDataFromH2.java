package com.liubao.substationprotocol.DAO;

import java.io.IOException;
import java.sql.*;
import java.util.*;

import com.configuration.MemoryDBConfiguration;
import com.zuowenfeng.beans.GLAnalogControl;
import com.zuowenfeng.beans.GLAnalogMeasure;
import com.zuowenfeng.beans.GLDigitalControl;
import com.zuowenfeng.beans.GLDigitalMeasure;
import com.zuowenfeng.beans.HeatingPoint;
import com.zuowenfeng.beans.RJLAnalogControl;
import com.zuowenfeng.beans.RJLAnalogMeasure;
import com.zuowenfeng.beans.RJLDigitalControl;
import com.zuowenfeng.beans.RJLDigitalMeasure;
//import java.io.*;

class GetDataFromH2 {
	
	//Connection conn;
	Statement stmt;
	ResultSet rs;
	String command;
	AllTables allTables = new AllTables();
	static Connection conn;
	
	static {
		
		try {
			conn = GetDataFromH2.getConnection();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/* 得到数据库中的所有的表 */
	/**
	 * @return
	 * @throws IOException 
	 */
	AllTables getAllTables() throws IOException{
		
		try{
			//conn = GetDataFromH2.getConnection();
			stmt = conn.createStatement();
			/*==========================================================================================================*/	
			/* 取得表GL_ANALOG_MEASURE的数据*/
			ArrayList<HeatingPoint> dataFromGlAnalogMeasure = new ArrayList<HeatingPoint>();
			
			command = "select * from GL_ANALOG_MEASURE";
		    rs = stmt.executeQuery(command);
			
			while(rs.next()){	
				GLAnalogMeasure oneRecord1 = new GLAnalogMeasure();
				oneRecord1.setDataType(0);
				oneRecord1.setDeviceID(rs.getString("device_id"));
				oneRecord1.setSensorID(rs.getString("sensor_id"));
				oneRecord1.setPLCID(rs.getString("plc_id"));
//				oneRecord1.setBoilerRoom("boilerRoom");
//				oneRecord1.setBoiler(rs.getString("boiler"));
//				oneRecord1.setField(rs.getString("field"));
//				oneRecord1.setDescription(rs.getString("description"));
				oneRecord1.setTimeStamp(rs.getTimestamp("timestamp"));
				oneRecord1.setDate(rs.getDate("date"));
				oneRecord1.setTime(rs.getTime("time"));
//				oneRecord1.setMeasure_type(rs.getString("measure_type"));
//				oneRecord1.setMeasure_unit(rs.getString("measure_unit"));
				oneRecord1.setValue(rs.getFloat("value"));
//				oneRecord1.setFactor(rs.getFloat("factor"));
//				oneRecord1.setOffset(rs.getFloat("offset"));
//				oneRecord1.setState(rs.getInt("state"));
//				oneRecord1.setHighlimit(rs.getFloat("highlimit"));
//				oneRecord1.setLowlimit(rs.getFloat("lowlimit"));
//				oneRecord1.setHighhighlimit(rs.getFloat("highhighlimit"));
//				oneRecord1.setLowlowlimit(rs.getFloat("lowlowlimit"));
				oneRecord1.setOutbound(rs.getInt("outbound"));
				oneRecord1.setBlockValue(rs.getInt("blockvalue"));
				oneRecord1.setBlockFlag(rs.getInt("blockflag"));
				dataFromGlAnalogMeasure.add(oneRecord1);
				//System.out.println(oneRecord1.getValue());
			}
			
			allTables.table_GlAnalogMeasure = dataFromGlAnalogMeasure;
			rs = null;
			
			
			/*==========================================================================================================*/
			/*  取得表GL_DIGITAL_MEASURE的数据  */
			ArrayList<HeatingPoint> dataFromGlDigitalMeasure = new ArrayList<HeatingPoint>();
			
			command = "select * from GL_DIGITAL_MEASURE";
			rs = stmt.executeQuery(command);
				
				while(rs.next()){
					GLDigitalMeasure oneRecord2 = new GLDigitalMeasure();
					oneRecord2.setDataType(2);
					oneRecord2.setDeviceID(rs.getString("device_id"));
					oneRecord2.setSensorID(rs.getString("sensor_id"));
					oneRecord2.setPLCID(rs.getString("plc_id"));
//					oneRecord2.setBoilerRoom(rs.getString("boilerRoom"));
//					oneRecord2.setBoiler(rs.getString("boiler"));
//					oneRecord2.setField(rs.getString("field"));
//					oneRecord2.setDescription(rs.getString("description"));
					oneRecord2.setTimestamp(rs.getTimestamp("timestamp"));
					oneRecord2.setDate(rs.getDate("date"));
					oneRecord2.setTime(rs.getTime("time"));
//					oneRecord2.setMeasure_type(rs.getString("measure_type"));
					oneRecord2.setState(rs.getInt("state"));
					oneRecord2.setValue(rs.getFloat("value"));
//					oneRecord2.setWhenout(rs.getInt("whenout"));
					oneRecord2.setIsbeyond(rs.getInt("isbeyond"));
					oneRecord2.setClose(rs.getInt("close"));
					oneRecord2.setOpen(rs.getInt("open"));
					oneRecord2.setBlockValue(rs.getInt("blockvalue"));
					oneRecord2.setBlockFlag(rs.getInt("blockflag"));
					dataFromGlDigitalMeasure.add(oneRecord2); //
				}
				
				allTables.table_GlDigitalMeasure = dataFromGlDigitalMeasure;
				rs = null;
				
				
				/*==========================================================================================================*/		
				/*    取得表RJL_ANALOG_MEASURE的数据         */
				ArrayList<HeatingPoint> dataFromRjlAnalogMeasure = new ArrayList<HeatingPoint>();
				
				command = "select * from RJL_ANALOG_MEASURE";
				rs = stmt.executeQuery(command);
				
				while(rs.next()){
					RJLAnalogMeasure oneRecord3 = new RJLAnalogMeasure();
					oneRecord3.setDeviceID(rs.getString("device_id"));
					oneRecord3.setSensorID(rs.getString("sensor_id"));
					oneRecord3.setPLCID(rs.getString("plc_id"));
//					oneRecord3.setCommunity(rs.getString("community"));
//					oneRecord3.setBuilding(rs.getString("building"));
//					oneRecord3.setResident(rs.getString("resident"));
//					oneRecord3.setField(rs.getString("field"));
//					oneRecord3.setDescription(rs.getString("description"));
					oneRecord3.setTimestamp(rs.getTimestamp("timestamp"));
					oneRecord3.setDate(rs.getDate("date"));
					oneRecord3.setTime(rs.getTime("time"));
//					oneRecord3.setMeasure_type(rs.getString("measure_type"));
//					oneRecord3.setMeasure_unit(rs.getString("measure_unit"));
					oneRecord3.setValue(rs.getFloat("value"));
//					oneRecord3.setFactor(rs.getFloat("factor"));
//					oneRecord3.setOffset(rs.getFloat("offset"));
//					oneRecord3.setState(rs.getInt("state"));
//					oneRecord3.setHighlimit(rs.getFloat("highlimit"));
//					oneRecord3.setLowlimit(rs.getFloat("lowlimit"));
//					oneRecord3.setHighhighlimit(rs.getFloat("highhighlimit"));
//					oneRecord3.setLowlowlimit(rs.getFloat("lowlowlimit"));
					oneRecord3.setOutbound(rs.getInt("outbound"));
					oneRecord3.setBlockValue(rs.getInt("blockvalue"));
					oneRecord3.setBlockFlag(rs.getInt("blockflag"));
					dataFromRjlAnalogMeasure.add(oneRecord3);
				}
				
				allTables.table_RjlAnalogMeasure = dataFromRjlAnalogMeasure;
				rs = null;
				
				/*==========================================================================================================*/
				/*    获取表RJL_DIGITAL_MEASURE的数据   */
				ArrayList<HeatingPoint> dataFromRjlDigitalMeasure = new ArrayList<HeatingPoint>();
				
				command = "select * from RJL_DIGITAL_MEASURE";
				rs = stmt.executeQuery(command);
					
					while(rs.next()){
						RJLDigitalMeasure oneRecord4 = new RJLDigitalMeasure();
						oneRecord4.setDeviceID(rs.getString("device_id"));
						oneRecord4.setSensorID(rs.getString("sensor_id"));
						oneRecord4.setPLCID(rs.getString("plc_id"));
//						oneRecord4.setCommunity(rs.getString("community"));
//						oneRecord4.setBuilding(rs.getString("building"));
//						oneRecord4.setResident(rs.getString("resident"));
//						oneRecord4.setField(rs.getString("field"));
//						oneRecord4.setDescription(rs.getString("description"));
						oneRecord4.setTimestamp(rs.getTimestamp("timestamp"));
						oneRecord4.setDate(rs.getDate("date"));
						oneRecord4.setTime(rs.getTime("time"));
//						oneRecord4.setMeasure_type(rs.getString("measure_type"));
						oneRecord4.setState(rs.getInt("state"));
						oneRecord4.setValue(rs.getInt("value"));
//						oneRecord4.setWhenout(rs.getInt("whenout"));
						oneRecord4.setIsbeyond(rs.getInt("isbeyond"));
						oneRecord4.setClose(rs.getInt("close"));
						oneRecord4.setOpen(rs.getInt("open"));
						oneRecord4.setBlockValue(rs.getInt("blockvalue"));
						oneRecord4.setBlockFlag(rs.getInt("blockflag"));
						
						dataFromRjlDigitalMeasure.add(oneRecord4);
					}
					
					allTables.table_RjlDigitalMeasure = dataFromRjlDigitalMeasure;
					rs = null;
					
					/*==========================================================================================================*/
					/*   获取表GL_ANALOG_CONTROL数据        */
					ArrayList<HeatingPoint> dataFromAnalogControl = new ArrayList<HeatingPoint>();
					
					command = "select * from GL_ANALOG_CONTROL";
					rs = stmt.executeQuery(command);
						
						while(rs.next()){
							GLAnalogControl oneRecord5 = new GLAnalogControl();
							oneRecord5.setDataType(1);
							oneRecord5.setDeviceID(rs.getString("device_id"));
							oneRecord5.setSensorID(rs.getString("sensor_id"));
							oneRecord5.setPLCID(rs.getString("plc_id"));
							oneRecord5.setBoilerRoom(rs.getString("boilerRoom"));
							oneRecord5.setBoiler(rs.getString("boiler"));
							oneRecord5.setField(rs.getString("field"));
							oneRecord5.setDescription(rs.getString("description"));
							oneRecord5.setTimeStamp(rs.getTimestamp("timestamp"));
							oneRecord5.setDate(rs.getDate("date"));
							oneRecord5.setTime(rs.getTime("time"));
							oneRecord5.setMeasure_type(rs.getString("measure_type"));
							oneRecord5.setMeasure_unit(rs.getString("measure_unit"));
							oneRecord5.setValue(rs.getFloat("value"));
							oneRecord5.setFactor(rs.getFloat("factor"));
							oneRecord5.setOffset(rs.getFloat("offset"));
							oneRecord5.setState(rs.getByte("state"));
							oneRecord5.setBlockValue(rs.getInt("blockvalue"));
							oneRecord5.setBlockFlag(rs.getInt("blockflag"));
							dataFromAnalogControl.add(oneRecord5);
						}
				allTables.table_GlAnalogControl = dataFromAnalogControl;
				rs = null;
				
				/*==========================================================================================================*/
				/*    获取表GL_DIGITAL_CONTROL的数据        */
				ArrayList<HeatingPoint> dataFromGlDigitalControl = new ArrayList<HeatingPoint>();
				
				command = "select * from GL_DIGITAL_CONTROL";
				rs = stmt.executeQuery(command);
					
					while(rs.next()){
						GLDigitalControl oneRecord6 = new GLDigitalControl();
						oneRecord6.setDataType(3);
						oneRecord6.setDeviceID(rs.getString("device_id"));
						oneRecord6.setSensorID(rs.getString("sensor_id"));
						oneRecord6.setPLCID(rs.getString("plc_id"));
						oneRecord6.setBoilerRoom(rs.getString("boilerRoom"));
						oneRecord6.setBoiler(rs.getString("boiler"));
						oneRecord6.setField(rs.getString("field"));
						oneRecord6.setDescription(rs.getString("description"));
						oneRecord6.setTimestamp(rs.getTimestamp("timestamp"));
						oneRecord6.setDate(rs.getDate("date"));
						oneRecord6.setTime(rs.getTime("time"));
						oneRecord6.setMeasure_type(rs.getString("measure_type"));
						oneRecord6.setValue(rs.getInt("value"));
						oneRecord6.setClose(rs.getInt("close"));
						oneRecord6.setOpen(rs.getInt("open"));
						oneRecord6.setBlockValue(rs.getInt("blockvalue"));
						oneRecord6.setBlockFlag(rs.getInt("blockflag"));
						
						dataFromGlDigitalControl.add(oneRecord6);
					}
					
				allTables.table_GlDigitalControl = dataFromGlDigitalControl;
				rs = null;
				
				/*==========================================================================================================*/
				/*    取得表RJL_ANALOG_CONTROL的数据         */
				ArrayList<HeatingPoint> dataFromRjlAnalogControl = new ArrayList<HeatingPoint>();
				
				command = "select * from RJL_ANALOG_MEASURE";
				rs = stmt.executeQuery(command);
					
					while(rs.next()){
						RJLAnalogControl oneRecord7 = new RJLAnalogControl();
						oneRecord7.setDeviceID(rs.getString("device_id"));
						oneRecord7.setSensorID(rs.getString("sensor_id"));
						oneRecord7.setPLCID(rs.getString("plc_id"));
						oneRecord7.setCommunity(rs.getString("community"));
						oneRecord7.setBuilding(rs.getString("building"));
						oneRecord7.setResident(rs.getString("resident"));
						oneRecord7.setField(rs.getString("field"));
						oneRecord7.setDescription(rs.getString("description"));
						oneRecord7.setTimestamp(rs.getTimestamp("timestamp"));
						oneRecord7.setDate(rs.getDate("date"));
						oneRecord7.setTime(rs.getTime("time"));
						oneRecord7.setMeasure_type(rs.getString("measure_type"));
						oneRecord7.setMeasure_unit(rs.getString("measure_unit"));
						oneRecord7.setValue(rs.getFloat("value"));
						oneRecord7.setFactor(rs.getFloat("factor"));
						oneRecord7.setOffset(rs.getFloat("offset"));
						oneRecord7.setState(rs.getByte("state"));
						oneRecord7.setBlockValue(rs.getInt("blockvalue"));
						oneRecord7.setBlockFlag(rs.getInt("blockflag"));
						
						dataFromRjlAnalogControl.add(oneRecord7);
					}
				
					allTables.table_RjlAnalogControl = dataFromRjlAnalogControl;
					rs = null;
					
					/*==========================================================================================================*/
					/*    获取表RJL_DIGITAL_MEASURE的数据*/
					ArrayList<HeatingPoint> dataFromRjlDigitalControl = new ArrayList<HeatingPoint>();
					
					command = "select * from RJL_DIGITAL_MEASURE";
					rs = stmt.executeQuery(command);
						
						while(rs.next()){
							RJLDigitalControl oneRecord8 = new RJLDigitalControl();
							oneRecord8.setDeviceID(rs.getString("device_id"));
							oneRecord8.setSensorID(rs.getString("sensor_id"));
							oneRecord8.setPLCID(rs.getString("plc_id"));
							oneRecord8.setCommunity(rs.getString("community"));
							oneRecord8.setBuilding(rs.getString("building"));
							oneRecord8.setResident(rs.getString("resident"));
							oneRecord8.setField(rs.getString("field"));
							oneRecord8.setDescription(rs.getString("description"));
							oneRecord8.setTimestamp(rs.getTimestamp("timestamp"));
							oneRecord8.setDate(rs.getDate("date"));
							oneRecord8.setTime(rs.getTime("time"));
							oneRecord8.setMeasure_type(rs.getString("measure_type"));
							oneRecord8.setValue(rs.getInt("value"));
							oneRecord8.setClose(rs.getInt("close"));
							oneRecord8.setOpen(rs.getInt("open"));
							oneRecord8.setBlockValue(rs.getInt("blockvalue"));
							oneRecord8.setBlockFlag(rs.getInt("blockflag"));
							
							dataFromRjlDigitalControl.add(oneRecord8);
						}
						
					allTables.table_RjlDigitalControl = dataFromRjlDigitalControl;
					
		}catch(SQLException sqle){
			System.out.println("sqle  " + sqle);
		}finally{
			GetDataFromH2.closeAll(conn, stmt, rs);
		}
		
		return allTables;
	}
	
	/*    建立数据库连接    */
	static Connection getConnection() throws IOException{
		Connection conn = null;
		
		MemoryDBConfiguration configuration = new MemoryDBConfiguration();
		configuration.getConnectionString();
		
		String driver = "org.h2.Driver";
		String url="jdbc:h2:tcp://" + configuration.getUrl() + "/" + configuration.getDatabase();
		System.out.println("jdbc-url is: " + url);
		String user= configuration.getUsername();
		String password= configuration.getPassword();

			try{
				
				
				Class.forName(driver);  //加载驱动器
				conn = DriverManager.getConnection(url, user, password); //建立数据库连接并返回
				System.out.println("数据库连接成功" + conn);
			}
			catch(Exception e){
				e.printStackTrace();
			}
			
			return conn;	
		}
	
	/*   关闭连接，释放各种资源    */
	static void closeAll(Connection conn,Statement stmt,ResultSet rs){
		try{
			rs.close();
			stmt.close();
			//conn.close();
		}catch(SQLException sqle){
			System.out.print(sqle);
		}
	}
	
}
