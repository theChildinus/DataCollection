package com.liubao.substationprotocol.DAO;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;

import com.configuration.NotifyConfiguration;
import com.configuration.UpdateDBInfoConfiguration;
import com.factory.ServicemixConfFactory;
import com.zuowenfeng.AgentComposite.util.DataAnalyzeComponent;
import com.zuowenfeng.AgentComposite.util.DataAnalyzeComponentImpl;
import com.zuowenfeng.AgentComposite.util.DataBaseComponent;
import com.zuowenfeng.AgentComposite.util.DataBaseComponentImpl;
import com.zuowenfeng.AgentComposite.util.PublishComponentImpl;
import com.zuowenfeng.beans.GLAnalogControl;
import com.zuowenfeng.beans.GLAnalogMeasure;
import com.zuowenfeng.beans.GLDigitalControl;
import com.zuowenfeng.beans.GLDigitalMeasure;
import com.zuowenfeng.beans.HeatingPoint;
import com.zuowenfeng.beans.RJLAnalogMeasure;
import com.zuowenfeng.beans.RJLDigitalMeasure;
import com.zuowenfeng.monitor.monitorDAO.StationDAO;

class MultiThreadsHandler implements Runnable{
	Socket incoming = null;
	
	AllTables allTables = null;
	
//	ArrayList<HeatingPoint> table_GlAnalogMeasure = null;
//	ArrayList<HeatingPoint> table_GlDigitalMeasure = null;
//	ArrayList<HeatingPoint> table_RjlAnalogMeasure = null;
//	ArrayList<HeatingPoint> table_RjlDigitalMeasure = null;
//	ArrayList<HeatingPoint> table_GlAnalogControl = null;
//	ArrayList<HeatingPoint> table_GlDigitalControl = null;
//	ArrayList<HeatingPoint> table_RjlAnalogControl = null;
//	ArrayList<HeatingPoint> table_RjlDigitalControl = null;
	private PublishComponentImpl comp;
	public StringBuilder builder;
	private DataBaseComponent component;
	static Connection conn;
	
	public MultiThreadsHandler(Socket i, int s ) throws IOException{
		incoming = i;
		UpdateDBInfoConfiguration conf = new UpdateDBInfoConfiguration();
		conf.getUpdateConfiguration();
		comp = new PublishComponentImpl("http://" + conf.getUrl() + ":" + conf.getPort() + "/" + conf.getServicename() + s, "http://" + ServicemixConfFactory.conf2.getUrl() + ":" + ServicemixConfFactory.conf2.getPort() + "/cxf/NotificationProxy");
		builder = new StringBuilder();
		component = new DataBaseComponentImpl();
		conn = component.geth2();
	}
	
	public void publishUpdate( String topic, String content, int i ) throws Exception {
		comp.publish(topic, content);
		topic = null;
	}
	
	public void objectToUpdateMsg( ArrayList<HeatingPoint> heatArray ) throws InterruptedException {
		builder.append("<coolsql xmlns=\"\" xmlns:ns6=\"http://docs.oasis-open.org/wsn/b-2\">");
		String topic = "";
		//System.out.println(allTables.table_GlAnalogMeasure.size() );
		
		while ( heatArray.size() != 0 ) {
			HeatingPoint p = heatArray.get(0);
			
			if ( p.getDataType() == 0 ) {
				GLAnalogMeasure gam = (GLAnalogMeasure)p;
				builder.append("<Sql>update GL_ANALOG_MEASURE set timestamp = timestamp '" + gam.getTimeStamp() + "', date = date '" + gam.getDate() + "', time = time '" + gam.getTime() + "', value = " + gam.getValue() + ", outbound = " + 
									gam.getOutbound() + ", blockvalue = " + gam.getBlockValue() + ", blockflag = " + gam.getBlockFlag() + " where device_id = '" + gam.getDeviceID() + "' and plc_id = '" + gam.getPLCID() + "' and sensor_id = '" + 
									gam.getSensorID() + "';</Sql>");
				gam = null;
				topic = "all";
			}
			
			else if ( p.getDataType() == 1 ) {
				GLAnalogControl gac = (GLAnalogControl)p;
				builder.append("<Sql>update GL_ANALOG_CONTROL set timestamp = timestamp '" + gac.getTimeStamp() + "', date = date '" + gac.getDate() + "', time = time '" + gac.getTime() + "', value = " + gac.getValue() + ", blockvalue = " +
				gac.getBlockValue() + ", blockflag = " + gac.getBlockFlag() + " where device_id = '" + gac.getDeviceID() + "' and plc_id = '" + gac.getPLCID() + "' and sensor_id = '" + gac.getSensorID() + "';</Sql>");
				topic = "gl_analog_control";
			}
		
			else if ( p.getDataType() == 2 ) {
				GLDigitalMeasure gdm = (GLDigitalMeasure)p;
				builder.append("<Sql>update GL_DIGITAL_MEASURE set timestamp = timestamp '" + gdm.getTimestamp() + "', date = date '" + gdm.getDate() + "', time = time '" + gdm.getTime() + "', value = " + gdm.getValue() + ", open = " + 
									gdm.getOpen() + ", close = " + gdm.getClose() + ", isbeyond = " + gdm.getIsbeyond() + ", state = " + gdm.getState() +
									", blockvalue = " + gdm.getBlockValue() + ", blockflag = " + gdm.getBlockFlag() + " where device_id = '" + gdm.getDeviceID() + "' and plc_id = '" + gdm.getPLCID() + "' and sensor_id = '" + gdm.getSensorID() 
									+ "';</Sql>");
				gdm = null;
				topic = "gl_digital_measure";
			}
			
			else if ( p.getDataType() == 3 ) {
				GLDigitalControl gdc = (GLDigitalControl)p;
				builder.append("<Sql>update GL_DIGITAL_CONTROL set timestamp = timestamp '" + gdc.getTimestamp() + "', date = date '" + gdc.getDate() + "', time = time '" + gdc.getTime() + "', value = " + gdc.getValue() + ", open = " + 
						gdc.getOpen() + ", close = " + gdc.getClose() + " where device_id = '" + gdc.getDeviceID() + "' and plc_id = '" + gdc.getPLCID() + "' and sensor_id = '" + gdc.getSensorID() 
						+ "';</Sql>");
				topic = "gl_digital_control";
			}
		
			else if ( p.getDataType() == 4 ) {
				RJLAnalogMeasure ram = (RJLAnalogMeasure)p;
				builder.append("<Sql>update RJL_ANALOG_MEASURE set timestamp = timestamp '" + ram.getTimestamp() + "', date = date '" + ram.getDate() + "', time = time '" + ram.getTime() + "', value = " + ram.getValue() + ", outbound = " + 
					ram.getOutbound() + ", blockvalue = " + ram.getBlockValue() + ", blockflag = " + ram.getBlockFlag() + " where device_id = '" + ram.getDeviceID() + "' and plc_id = '" + ram.getPLCID() + "' and sensor_id = '" + 
					ram.getSensorID() + "';</Sql>");
				ram = null;
			}
		
			else if ( p.getDataType() == 6 ) {
				RJLDigitalMeasure rdm = (RJLDigitalMeasure)p;
				builder.append("<Sql>update RJL_DIGITAL_MEASURE set timestamp = timestamp '" + rdm.getTimestamp() + "', date = date '" + rdm.getDate() + "', time = time '" + rdm.getTime() + "', value = " + rdm.getValue() + ", open = " + 
					rdm.getOpen() + ", close = " + rdm.getClose() + ", isbeyond = " + rdm.getIsbeyond() + ", state = " + rdm.getState() +
					", blockvalue = " + rdm.getBlockValue() + ", blockflag = " + rdm.getBlockFlag() + " where device_id = '" + rdm.getDeviceID() + "' and plc_id = '" + rdm.getPLCID() + "' and sensor_id = '" + rdm.getSensorID() 
					+ "';</Sql>");
				rdm = null;
			}
			
			p = null;
			heatArray.remove(0);
			
			if ( builder.length() > 1000 ) {
				builder.append("<Level>realtime</Level></coolsql>");
				//System.out.println(builder.toString());
				try {
					publishUpdate(topic, builder.toString(), 4);
					builder.delete(0, builder.length());
					builder.append("<coolsql xmlns=\"\" xmlns:ns6=\"http://docs.oasis-open.org/wsn/b-2\">");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				//Thread.sleep(1000);
			}
			
			builder.append("<Level>realtime</Level></coolsql>");
			//System.out.println(builder.toString());
		}
		
		if ( builder.length() > 1000 ) {
			builder.append("<Level>realtime</Level></coolsql>");
			
			try {
				publishUpdate(topic, builder.toString(), 4);
				builder.delete(0, builder.length());
				builder.append("<coolsql xmlns=\"\" xmlns:ns6=\"http://docs.oasis-open.org/wsn/b-2\">");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		builder.append("<Level>realtime</Level></coolsql>");
	}
	
	public String objectToOutbound( String boilerRoom, String topic, String location, String type, float value, String level ) {
		StringBuilder builder = new StringBuilder();
		builder.append("<alarm:Alarm xmlns:alarm=\"http://alarms.some-host\">");
		builder.append("<alarm:community>" + "锅炉房" + boilerRoom + "</alarm:community>");
		builder.append("<alarm:location>" + location + "</alarm:location>");
		builder.append("<alarm:type>" + type + "</alarm:type>");
		builder.append("<alarm:value>" + value + "</alarm:value>");
		builder.append("<alarm:level>" + level + "</alarm:level>");
		builder.append("</alarm:Alarm>");
		return builder.toString();
	}
	
	public HeatingPoint checkDataType( HeatingPoint p, String deviceid, String plcid, String sensorid, boolean isAnalog ) throws SQLException, FileNotFoundException {
		
		try {
			//Connection conn = component.geth2();
			Statement stmt = conn.createStatement();
			String hsql;
			ResultSet rs;
			
			if ( isAnalog == true && ( p.getDataType() == 0 ) ) {
				hsql = "select * from GL_ANALOG_MEASURE where device_id = '" + deviceid + "' and plc_id = '" + plcid + "' and sensor_id = '" + sensorid + "';";
				//System.out.println(hsql);
				rs = stmt.executeQuery(hsql);
				//System.out.println(rs.getString("device_id"));
				rs.next();
				GLAnalogMeasure glAnalogMeasure = (GLAnalogMeasure)p;
				glAnalogMeasure.setDataType(0);
				glAnalogMeasure.setBoilerRoom(rs.getString("boilerRoom"));
				glAnalogMeasure.setBoiler(rs.getString("boiler"));
				glAnalogMeasure.setField(rs.getString("field"));
				glAnalogMeasure.setDescription(rs.getString("description"));
				glAnalogMeasure.setMeasure_type(rs.getString("measure_type"));
				glAnalogMeasure.setMeasure_unit(rs.getString("measure_unit"));
				glAnalogMeasure.setFactor(rs.getFloat("factor"));
				glAnalogMeasure.setOffset(rs.getFloat("offset"));
				glAnalogMeasure.setState(rs.getInt("state"));
				
				if ( rs.getFloat("highlimit") != 0 ) {
					glAnalogMeasure.setHighlimit(rs.getFloat("highlimit"));
				}
				
				if ( rs.getFloat("highhighlimit") != 0 ) {
					glAnalogMeasure.setHighhighlimit(rs.getFloat("highhighlimit"));
				}
				
				if ( rs.getFloat("lowlimit") != 0 ) {
					glAnalogMeasure.setLowlimit(rs.getFloat("lowlimit"));
				}
				
				if ( rs.getFloat("lowlimit") != 0 ) {
					glAnalogMeasure.setLowlowlimit(rs.getFloat("lowlowlimit"));
				}
				
				rs.close();
				rs = null;
				//conn.close();
				stmt.close();
				return glAnalogMeasure;
			}
			
			else {
				hsql = "select * from GL_DIGITAL_MEASURE where device_id = '" + deviceid + "' and plc_id = '" + plcid + "' and sensor_id = '" + sensorid + "';";
//				System.out.println(hsql);
				rs = stmt.executeQuery(hsql);
				
				if ( rs.next() == false ) {
					hsql = "select * from RJL_DIGITAL_MEASURE where device_id = '" + deviceid + "' and plc_id = '" + plcid + "' and sensor_id = '" + sensorid + "';";
					rs = stmt.executeQuery(hsql);
					
					if ( rs.next() == false ) {
						//conn.close();
						stmt.close();
						return null;
					}
					
					RJLDigitalMeasure rjlDigitalMeasure = new RJLDigitalMeasure();
					rjlDigitalMeasure.setDataType(2);
					rjlDigitalMeasure.setDeviceID(rs.getString("device_id"));
					rjlDigitalMeasure.setPLCID(rs.getString("plc_id"));
					rjlDigitalMeasure.setSensorID(rs.getString("sensor_id"));
					rjlDigitalMeasure.setCommunity(rs.getString("community"));
					rjlDigitalMeasure.setBuilding(rs.getString("building"));
					rjlDigitalMeasure.setResident(rs.getString("resident"));
					rjlDigitalMeasure.setField(rs.getString("field"));
					rjlDigitalMeasure.setDescription(rs.getString("description"));
					rjlDigitalMeasure.setTimestamp(rs.getTimestamp("timestamp"));
					rjlDigitalMeasure.setDate(rs.getDate("date"));
					rjlDigitalMeasure.setTime(rs.getTime("time"));
					rjlDigitalMeasure.setMeasure_type(rs.getString("measure_type"));
					rjlDigitalMeasure.setState(rs.getInt("state"));
					rjlDigitalMeasure.setValue(rs.getFloat("value"));
					rjlDigitalMeasure.setWhenout(rs.getInt("whenout"));
					rjlDigitalMeasure.setIsbeyond(rs.getInt("isbeyond"));
					rjlDigitalMeasure.setOpen(rs.getInt("open"));
					rjlDigitalMeasure.setClose(rs.getInt("close"));
					
					//conn.close();
					stmt.close();
					return rjlDigitalMeasure;
				}
				
				GLDigitalMeasure glDigitalMeasure = new GLDigitalMeasure();
				glDigitalMeasure.setDataType(2);
				glDigitalMeasure.setDeviceID(rs.getString("device_id"));
				glDigitalMeasure.setPLCID(rs.getString("plc_id"));
				glDigitalMeasure.setSensorID(rs.getString("sensor_id"));
				glDigitalMeasure.setBoilerRoom(rs.getString("boilerRoom"));
				glDigitalMeasure.setBoiler(rs.getString("boiler"));
				glDigitalMeasure.setField(rs.getString("field"));
				glDigitalMeasure.setDescription(rs.getString("description"));
				glDigitalMeasure.setTimestamp(rs.getTimestamp("timestamp"));
				glDigitalMeasure.setDate(rs.getDate("date"));
				glDigitalMeasure.setTime(rs.getTime("time"));
				glDigitalMeasure.setMeasure_type(rs.getString("measure_type"));
				glDigitalMeasure.setState(rs.getInt("state"));
				glDigitalMeasure.setValue(rs.getFloat("value"));
				glDigitalMeasure.setWhenout(rs.getInt("whenout"));
				glDigitalMeasure.setIsbeyond(rs.getInt("isbeyond"));
				glDigitalMeasure.setOpen(rs.getInt("open"));
				glDigitalMeasure.setClose(rs.getInt("close"));
				
				//conn.close();
				stmt.close();
				return glDigitalMeasure;
			}
			
		}
		
		catch (Exception e) {
		// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	public void run(){
		int i = 0;

		while(true){
			try{

				ObjectInputStream ois = new ObjectInputStream(incoming.getInputStream());
				allTables= (AllTables)ois.readObject();
				Date date = new Date();
				System.out.println("Number " + i + ": " + date.getTime());
				
//				ArrayList<HeatingPoint> table_GlAnalogMeasure = allTables.table_GlAnalogMeasure;
//				
//				for(int s = 0;s<table_GlAnalogMeasure.size();s++){
//					GLAnalogMeasure measure = (GLAnalogMeasure)table_GlAnalogMeasure.get(s);
//					measure.setDataType(0);
//					GLAnalogMeasure raw = (GLAnalogMeasure)checkDataType(measure, measure.getDeviceID(), measure.getPLCID(), measure.getSensorID(), true);
//					measure.setLowlimit(raw.getLowlimit());
//					measure.setLowlowlimit(raw.getLowlowlimit());
//					measure.setHighlimit(raw.getHighlimit());
//					measure.setHighhighlimit(raw.getHighhighlimit());
//					
//					int outbound = checkOutbound(measure);
//					((GLAnalogMeasure)allTables.table_GlAnalogMeasure.get(s)).setOutbound(outbound);
//					
//					if ( outbound != 0 ) {
//						String msg = objectToOutbound(raw.getBoilerRoom(), raw.getMeasure_type(), raw.getDescription(), raw.getMeasure_type(), measure.getValue(), "" + outbound );
//						//System.out.println(msg);
//						publishUpdate("GL", msg, 1);
//					}
//					
//				}  //end of for
//				
//				objectToUpdateMsg(allTables.table_GlAnalogMeasure);
//				publishUpdate("all", builder.toString(), 4);
//				builder.delete(0, builder.length());
//				objectToUpdateMsg(allTables.table_GlAnalogControl);
//				publishUpdate("gl_analog_control", builder.toString(), 4);
//				builder.delete(0, builder.length());
//				objectToUpdateMsg(allTables.table_GlDigitalControl);
//				publishUpdate("gl_digital_control", builder.toString(), 4);
//				builder.delete(0, builder.length());
				
				System.out.println("Done!");
				i++;
				
				Date date2 = new Date();
				System.out.println("Time used:" + ( date2.getTime() - date.getTime() ) );
				System.gc();
			}
		
			catch(IOException ioe){
				break;
			} 
	
			catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
	
			catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		}
		
	}
	
	public int checkOutbound( HeatingPoint p ) {
		int type = p.getDataType();
		
		switch( type ) {
		case 0:
			GLAnalogMeasure gam = (GLAnalogMeasure)p;

			if ( gam.getValue() < gam.getLowlowlimit() ) {
				return -2;
			}
			
			else if ( gam.getValue() < gam.getLowlimit() ) {
				return -1;
			}
			
			else if ( gam.getValue() < gam.getHighlimit() ) {
				return 0;
			}
			
			else if ( gam.getValue() < gam.getHighhighlimit() ) {
				return 1;
			}
			
			else {
				return 2;
			}
			
		case 2:
			GLDigitalMeasure gdm = (GLDigitalMeasure)p;
			
			if ( gdm.getValue() == gdm.getWhenout() ) {
				return 1;
			}
			
			else {
				return 2;
			}
			
		case 4:
			RJLAnalogMeasure ram = (RJLAnalogMeasure)p;
			
			if ( ram.getValue() < ram.getLowlowlimit() ) {
				return -2;
			}
			
			else if ( ram.getValue() < ram.getLowlimit() ) {
				return -1;
			}
			
			else if ( ram.getValue() < ram.getHighlimit() ) {
				return 0;
			}
			
			else if ( ram.getValue() < ram.getHighhighlimit() ) {
				return 1;
			}
			
			else {
				return 2;
			}
			
		case 6:
			RJLDigitalMeasure rdm = (RJLDigitalMeasure)p;
			
			if ( rdm.getValue() == rdm.getWhenout() ) {
				return 1;
			}
			
			else {
				return 2;
			}
			
		}
		
		return -3;
	}
}