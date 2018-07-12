package com.zuowenfeng.AgentComposite.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import com.configuration.AnalogAlarmConfiguration;
import com.configuration.SwitchAlarmConfiguration;
import com.configuration.UpdateDBInfoConfiguration;
import com.factory.MsgReceiverFactory;
import com.factory.ServicemixConfFactory;
import com.zuowenfeng.beans.*;

public class DataAnalyzeComponentImpl implements DataAnalyzeComponent {
//	static Logger logger = Logger.getLogger("logManualCompare");
//	static {//这个是整个类加载一次，跟实例没有关系，否则就出现了很多log临时文件
//		
//		Handler h;
//		try {
//			
//			h = new FileHandler("logMKAgent/compare.txt");
//			h.setFormatter(new SimpleFormatter());
//			logger.addHandler(h);
//		} catch (SecurityException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		
//	}

	private DataBaseComponent  dataBaseComponent;
	
	private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss.S");
	private SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
	
	private Date date = new Date();
	
	private ArrayList<HeatingPoint> GLAnalogArray = new ArrayList<HeatingPoint> ();
	private ArrayList<HeatingPoint> RJLAnalogArray = new ArrayList<HeatingPoint> ();
	private ArrayList<HeatingPoint> GLDigitalArray = new ArrayList<HeatingPoint> ();
	private ArrayList<HeatingPoint> RJLDigitalArray = new ArrayList<HeatingPoint> ();
	private ArrayList<HeatingPoint> GLAnalogControl = new ArrayList<HeatingPoint> ();
	private ArrayList<HeatingPoint> GLDigitalControl = new ArrayList<HeatingPoint> ();
	private PublishComponentImpl comp;
	private PublishComponentImpl component;
	static Connection conn;
	
	public ArrayList<HeatingPoint> getGLArrayList () {
		return GLAnalogArray;
	}
	
	public ArrayList<HeatingPoint> getRJLArrayList () {
		return RJLAnalogArray;
	}
	
	public ArrayList<HeatingPoint> getGLDigitalArrayList () {
		return GLDigitalArray;
	}
	
	public ArrayList<HeatingPoint> getRJLDigitalArrayList () {
		return RJLDigitalArray;
	}
	
    public void setDataBaseComponent(DataBaseComponent dataBaseComponent) throws IOException {//composite中定义了引用，这里不使用这个引用也会出问题
        this.dataBaseComponent = dataBaseComponent;
		UpdateDBInfoConfiguration conf = new UpdateDBInfoConfiguration();
		conf.getUpdateConfiguration();
		comp = new PublishComponentImpl("http://" + conf.getUrl() + ":" + conf.getPort() + "/" + conf.getServicename(), "http://" + ServicemixConfFactory.conf2.getUrl() + ":" + ServicemixConfFactory.conf2.getPort() + "/cxf/NotificationProxy");
		AnalogAlarmConfiguration conf2 = new AnalogAlarmConfiguration();
		conf2.getConnectionString();
		component = new PublishComponentImpl("http://" + conf2.getUrl() + ":" + conf2.getPort() + "/" + conf2.getServicename(), "http://" + ServicemixConfFactory.conf2.getUrl() + ":" + ServicemixConfFactory.conf2.getPort() + "/cxf/NotificationProxy");
		conn = this.dataBaseComponent.geth2();
    }
	
	public HeatingPoint checkDataType( String deviceid, String plcid, String sensorid, boolean isAnalog ) throws SQLException, FileNotFoundException {
		
		try {
			//Connection conn = dataBaseComponent.geth2();
			Statement stmt = conn.createStatement();
			String hsql;
			ResultSet rs;
			
			if ( isAnalog == true ) {
				hsql = "select * from GL_ANALOG_MEASURE where device_id = '" + deviceid + "' and plc_id = '" + plcid + "' and sensor_id = '" + sensorid + "';";
				rs = stmt.executeQuery(hsql);
				
				if ( rs.next() == false ) {
					
					hsql = "select * from GL_ANALOG_CONTROL where device_id = '" + deviceid + "' and plc_id = '" + plcid + "' and sensor_id = '" + sensorid + "';";
					rs = stmt.executeQuery(hsql);
					System.out.println(hsql);
					
					if ( rs.next() == false ) {
						return null;
					}
					
					GLAnalogControl gac = new GLAnalogControl();
					gac.setDataType(1);
					gac.setDeviceID(rs.getString("device_id"));
					gac.setPLCID(rs.getString("plc_id"));
					gac.setSensorID(rs.getString("sensor_id"));
					gac.setBoilerRoom(rs.getString("boilerRoom"));
					gac.setBoiler(rs.getString("boiler"));
					gac.setField(rs.getString("field"));
					gac.setDescription(rs.getString("description"));
					gac.setTimeStamp(rs.getTimestamp("timestamp"));
					gac.setDate(rs.getDate("date"));
					gac.setTime(rs.getTime("time"));
					gac.setMeasure_type(rs.getString("measure_type"));
					gac.setMeasure_unit(rs.getString("measure_unit"));
					gac.setValue(rs.getFloat("value"));
					gac.setFactor(rs.getFloat("factor"));
					gac.setOffset(rs.getFloat("offset"));
					gac.setState(rs.getInt("state"));
					
					//conn.close();
					stmt.close();
					return gac;
//					hsql = "select * from RJL_ANALOG_MEASURE where device_id = '" + deviceid + "' and plc_id = '" + plcid + "' and sensor_id = '" + sensorid + "';";
//					rs = stmt.executeQuery(hsql);
//					
//					if ( rs.next() == false ) {
//						conn.close();
//						stmt.close();
//						return null;
//					}
//					
//					RJLAnalogMeasure rjlAnalogMeasure = new RJLAnalogMeasure();
//					rjlAnalogMeasure.setDataType(4);
//					rjlAnalogMeasure.setDeviceID(rs.getString("device_id"));
//					rjlAnalogMeasure.setPLCID(rs.getString("plc_id"));
//					rjlAnalogMeasure.setSensorID(rs.getString("sensor_id"));
//					rjlAnalogMeasure.setCommunity(rs.getString("community"));
//					rjlAnalogMeasure.setBuilding(rs.getString("building"));
//					rjlAnalogMeasure.setResident(rs.getString("resident"));
//					rjlAnalogMeasure.setField(rs.getString("field"));
//					rjlAnalogMeasure.setDescription(rs.getString("description"));
//					rjlAnalogMeasure.setTimestamp(rs.getTimestamp("timestamp"));
//					rjlAnalogMeasure.setDate(rs.getDate("date"));
//					rjlAnalogMeasure.setTime(rs.getTime("time"));
//					rjlAnalogMeasure.setMeasure_type(rs.getString("measure_type"));
//					rjlAnalogMeasure.setMeasure_unit(rs.getString("measure_unit"));
//					rjlAnalogMeasure.setValue(rs.getFloat("value"));
//					rjlAnalogMeasure.setFactor(rs.getFloat("factor"));
//					rjlAnalogMeasure.setOffset(rs.getFloat("offset"));
//					rjlAnalogMeasure.setState(rs.getInt("state"));
//					rjlAnalogMeasure.setHighlimit(rs.getFloat("highlimit"));
//					rjlAnalogMeasure.setHighhighlimit(rs.getFloat("highhighlimit"));
//					rjlAnalogMeasure.setLowlimit(rs.getFloat("lowlimit"));
//					rjlAnalogMeasure.setLowlowlimit(rs.getFloat("lowlowlimit"));
//					rjlAnalogMeasure.setOutbound(rs.getInt("outbound"));
//					
////					System.out.println(rjlAnalogMeasure.getDeviceID());
////					System.out.println(rjlAnalogMeasure.getPLCID());
////					System.out.println(rjlAnalogMeasure.getSensorID());
////					System.out.println(rjlAnalogMeasure.getCommunity());
//					
//					conn.close();
//					stmt.close();
//					return rjlAnalogMeasure;
				}
				
				GLAnalogMeasure glAnalogMeasure = new GLAnalogMeasure();
				glAnalogMeasure.setDataType(0);
				glAnalogMeasure.setDeviceID(rs.getString("device_id"));
				glAnalogMeasure.setPLCID(rs.getString("plc_id"));
				glAnalogMeasure.setSensorID(rs.getString("sensor_id"));
				glAnalogMeasure.setBoilerRoom(rs.getString("boilerRoom"));
				glAnalogMeasure.setBoiler(rs.getString("boiler"));
				glAnalogMeasure.setField(rs.getString("field"));
				glAnalogMeasure.setDescription(rs.getString("description"));
				glAnalogMeasure.setTimeStamp(rs.getTimestamp("timestamp"));
				glAnalogMeasure.setDate(rs.getDate("date"));
				glAnalogMeasure.setTime(rs.getTime("time"));
				glAnalogMeasure.setMeasure_type(rs.getString("measure_type"));
				glAnalogMeasure.setMeasure_unit(rs.getString("measure_unit"));
				glAnalogMeasure.setValue(rs.getFloat("value"));
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
				
				glAnalogMeasure.setOutbound(rs.getInt("outbound"));
				
				//conn.close();
				stmt.close();
				return glAnalogMeasure;
			}
			
			else {
				hsql = "select * from GL_DIGITAL_MEASURE where device_id = '" + deviceid + "' and plc_id = '" + plcid + "' and sensor_id = '" + sensorid + "';";
//				System.out.println(hsql);
				rs = stmt.executeQuery(hsql);
				
				if ( rs.next() == false ) {
//					hsql = "select * from RJL_DIGITAL_MEASURE where device_id = '" + deviceid + "' and plc_id = '" + plcid + "' and sensor_id = '" + sensorid + "';";
//					rs = stmt.executeQuery(hsql);
//					
//					if ( rs.next() == false ) {
//						conn.close();
//						stmt.close();
//						return null;
//					}
//					
//					RJLDigitalMeasure rjlDigitalMeasure = new RJLDigitalMeasure();
//					rjlDigitalMeasure.setDataType(2);
//					rjlDigitalMeasure.setDeviceID(rs.getString("device_id"));
//					rjlDigitalMeasure.setPLCID(rs.getString("plc_id"));
//					rjlDigitalMeasure.setSensorID(rs.getString("sensor_id"));
//					rjlDigitalMeasure.setCommunity(rs.getString("community"));
//					rjlDigitalMeasure.setBuilding(rs.getString("building"));
//					rjlDigitalMeasure.setResident(rs.getString("resident"));
//					rjlDigitalMeasure.setField(rs.getString("field"));
//					rjlDigitalMeasure.setDescription(rs.getString("description"));
//					rjlDigitalMeasure.setTimestamp(rs.getTimestamp("timestamp"));
//					rjlDigitalMeasure.setDate(rs.getDate("date"));
//					rjlDigitalMeasure.setTime(rs.getTime("time"));
//					rjlDigitalMeasure.setMeasure_type(rs.getString("measure_type"));
//					rjlDigitalMeasure.setState(rs.getInt("state"));
//					rjlDigitalMeasure.setValue(rs.getFloat("value"));
//					rjlDigitalMeasure.setWhenout(rs.getInt("whenout"));
//					rjlDigitalMeasure.setIsbeyond(rs.getInt("isbeyond"));
//					rjlDigitalMeasure.setOpen(rs.getInt("open"));
//					rjlDigitalMeasure.setClose(rs.getInt("close"));
//					
//					conn.close();
//					stmt.close();
//					return rjlDigitalMeasure;
					
					hsql = "select * from GL_DIGITAL_CONTROL where device_id = '" + deviceid + "' and plc_id = '" + plcid + "' and sensor_id = '" + sensorid + "';";
					rs = stmt.executeQuery(hsql);
					
					if ( rs.next() ) {
						GLDigitalControl gdc = new GLDigitalControl();
						gdc.setDataType(3);
						gdc.setDeviceID(rs.getString("device_id"));
						gdc.setPLCID(rs.getString("plc_id"));
						gdc.setSensorID(rs.getString("sensor_id"));
						gdc.setBoilerRoom(rs.getString("boilerRoom"));
						gdc.setBoiler(rs.getString("boiler"));
						gdc.setField(rs.getString("field"));
						gdc.setDescription(rs.getString("description"));
						gdc.setTimestamp(rs.getTimestamp("timestamp"));
						gdc.setDate(rs.getDate("date"));
						gdc.setTime(rs.getTime("time"));
						gdc.setMeasure_type(rs.getString("measure_type"));
						//gdc.setState(rs.getInt("state"));
						gdc.setValue(rs.getFloat("value"));
						gdc.setOpen(rs.getInt("open"));
						gdc.setClose(rs.getInt("close"));
						
						stmt.close();
						conn.close();
						
						return gdc;
					}
					
					stmt.close();
					//conn.close();
					return null;
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
	
	public String resouceInstanceBinding( HeatingPoint p ) {
		String result = "";
		int type = p.getDataType();
		
		switch( type ) {
		case 0:
			GLAnalogMeasure gam = (GLAnalogMeasure)p;
			result = result.concat( gam.getBoilerRoom() + "_" + gam.getBoiler() + "_" + gam.getMeasure_type());
			break;
			
		case 2:
			GLDigitalMeasure gdm = (GLDigitalMeasure)p;
			result = result.concat(gdm.getBoilerRoom() + "_" + gdm.getBoiler() + "_" + gdm.getMeasure_type());
			break;
		
		case 4:
			RJLAnalogMeasure ram = (RJLAnalogMeasure)p;
			
			if ( ram.getResident() == null ) {
				result = result.concat(ram.getCommunity() + "_" + ram.getBuilding() + "_" + ram.getMeasure_type());
			}
			
			else {
				result = result.concat(ram.getCommunity() + "_" + ram.getBuilding() + "_" + ram.getResident() + "_" + ram.getMeasure_type());
			}
			
			break;
			
		case 6:
			RJLDigitalMeasure rdm = (RJLDigitalMeasure)p;
			
			if ( rdm.getResident() == null ) {
				result = result.concat(rdm.getCommunity() + "_" + rdm.getBuilding() + "_" + rdm.getMeasure_type());
			}
			
			else {
				result = result.concat(rdm.getCommunity() + "_" + rdm.getBuilding() + "_" + rdm.getResident() + "_" + rdm.getMeasure_type());
			}
		
		}
		return result;
	}

	public float offsetCalculate( float factor, float offset, float oldValue, boolean direction ) {
		
		if ( direction == true ) {
			return factor * oldValue + offset;
		}
		
		else
			return ( oldValue - offset ) / factor; 
	}

	public boolean checkRedundant( HeatingPoint p, Timestamp time ) {
		Timestamp oldTime = null;
		
		switch( p.getDataType() ) {
		case 0:
			oldTime = ((GLAnalogMeasure)p).getTimeStamp();
			break;
			
		case 1:
			oldTime = ((GLAnalogControl)p).getTimeStamp();
			break;
			
		case 2:
			oldTime = ((GLDigitalMeasure)p).getTimestamp();
			break;
			
		case 3:
			oldTime = ((GLDigitalControl)p).getTimestamp();
			break;
			
		case 4:
			oldTime = ((RJLAnalogMeasure)p).getTimestamp();
			break;
			
		case 5:
			oldTime = ((RJLAnalogMeasure)p).getTimestamp();
			break;
			
		case 6:
			oldTime = ((RJLDigitalMeasure)p).getTimestamp();
			break;
			
		case 7:
			oldTime = ((RJLDigitalControl)p).getTimestamp();
			break;
			
		}
		
		if ( time.compareTo(oldTime) < 0 ) {
			return true;
		}
		
		else
			return false;
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
	
	public HeatingPoint findDownLocation( String notification ) throws SQLException, FileNotFoundException {
		//DataBaseComponent dbc = new DataBaseComponentImpl();
		//Connection conn = dbc.geth2();
		Statement stmt = conn.createStatement();
		
		String sql = "select * from GL_ANALOG_CONTROL where topic = '" + notification + "';";
		System.out.println(sql);
		ResultSet rs = stmt.executeQuery(sql);
			
		if ( rs.next() ) {
			GLAnalogControl gac = new GLAnalogControl();
			gac.setDataType(1);
			gac.setDeviceID(rs.getString("device_id"));
			gac.setSensorID(rs.getString("sensor_id"));
			gac.setPLCID(rs.getString("plc_id"));
			gac.setWordType(rs.getInt("word_count"));
			gac.setBoilerRoom(rs.getString("boilerRoom"));
			gac.setBoiler(rs.getString("boiler"));
			gac.setField(rs.getString("field"));
			gac.setDescription(rs.getString("description"));
			gac.setTimeStamp(Timestamp.valueOf(rs.getString("timestamp")));
			gac.setDate(java.sql.Date.valueOf(rs.getString("date")));
			gac.setTime(rs.getTime("time"));
			gac.setMeasure_type(rs.getString("measure_type"));
			gac.setMeasure_unit(rs.getString("measure_unit"));
			gac.setValue(rs.getFloat("value"));
			gac.setFactor(rs.getFloat("factor"));
			gac.setOffset(rs.getFloat("offset"));
			gac.setState(rs.getInt("state"));
				
			//conn.close();
			stmt.close();
			return gac;		
		}
		
		sql = "select * from RJL_ANALOG_CONTROL where topic = '" + notification + "';";
		rs = stmt.executeQuery(sql);
		
		if ( rs.next() ) {
			RJLAnalogControl rac = new RJLAnalogControl();
			rac.setDataType(5);
			rac.setDeviceID(rs.getString("device_id"));
			rac.setSensorID(rs.getString("sensor_id"));
			rac.setPLCID(rs.getString("plc_id"));
			rac.setCommunity(rs.getString("community"));
			rac.setBuilding(rs.getString("building"));
			rac.setResident(rs.getString("resident"));
			rac.setField(rs.getString("field"));
			rac.setDescription(rs.getString("description"));
			rac.setTimestamp(rs.getTimestamp("timestamp"));
			rac.setDate(rs.getDate("date"));
			rac.setTime(rs.getTime("time"));
			rac.setMeasure_type(rs.getString("measure_type"));
			rac.setMeasure_unit(rs.getString("measure_unit"));
			rac.setValue(rs.getFloat("value"));
			rac.setFactor(rs.getFloat("factor"));
			rac.setOffset(rs.getFloat("offset"));
			rac.setState(rs.getInt("state"));
			
			//conn.close();
			stmt.close();
			return rac;
		}
		
		sql = "select * from GL_DIGITAL_CONTROL where topic = '" + notification + "';";
		rs = stmt.executeQuery(sql);
		
		if ( rs.next() ) {
			GLDigitalControl gdc = new GLDigitalControl();
			gdc.setDataType(3);
			gdc.setDeviceID(rs.getString("device_id"));
			gdc.setSensorID(rs.getString("sensor_id"));
			gdc.setPLCID(rs.getString("plc_id"));
			gdc.setBoilerRoom(rs.getString("boilerRoom"));
			gdc.setBoiler(rs.getString("boiler"));
			gdc.setField(rs.getString("field"));
			gdc.setDescription(rs.getString("description"));
			gdc.setTimestamp(rs.getTimestamp("timestamp"));
			gdc.setDate(rs.getDate("date"));
			gdc.setTime(rs.getTime("time"));
			gdc.setMeasure_type(rs.getString("measure_type"));
			gdc.setValue(rs.getFloat("value"));
			gdc.setClose(rs.getInt("close"));
			gdc.setOpen(rs.getInt("open"));
			
			//conn.close();
			stmt.close();
			return gdc;
		}
		
		sql = "select * from RJL_DIGITAL_CONTROL where topic = '" + notification + "';";
		rs = stmt.executeQuery(sql);
		
		if ( rs.next() ) {
			RJLDigitalControl rdc = new RJLDigitalControl();
			rdc.setDataType(7);
			rdc.setDeviceID(rs.getString("device_id"));
			rdc.setSensorID(rs.getString("sensor_id"));
			rdc.setPLCID(rs.getString("sensor_id"));
			rdc.setCommunity(rs.getString("community"));
			rdc.setBuilding(rs.getString("building"));
			rdc.setResident(rs.getString("resident"));
			rdc.setField(rs.getString("field"));
			rdc.setDescription(rs.getString("description"));
			rdc.setTimestamp(rs.getTimestamp("timestamp"));
			rdc.setDate(rs.getDate("date"));
			rdc.setTime(rs.getTime("time"));
			rdc.setMeasure_type(rs.getString("measure_type"));
			rdc.setValue(rs.getFloat("value"));
			rdc.setClose(rs.getInt("close"));
			rdc.setOpen(rs.getInt("open"));
			
			//conn.close();
			stmt.close();
			return rdc;
		}
		
		return null;
	}
	
	public void publishUpdate( String topic, String content ) throws Exception {
		comp.publish(topic, content);
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
	
	public void UpAnalogData(String address,byte plcAddress,String sensorAddr,float value, int status ) throws Exception{//上传模拟量，需与下层模块进行接口设计的协调
		date = new Date();
//		DataAnalyzeComponent analyzeComponent = new DataAnalyzeComponentImpl();
//		DataBaseComponent t = new DataBaseComponentImpl();
//		analyzeComponent.setDataBaseComponent(t);
		String sensorString = sensorAddr;
	
		System.out.println("Up");
		System.out.println("设备地址是: "+address+"PLC编号: "+plcAddress+"传感器地址:"+sensorString);
		MsgReceiverFactory.receiver.receive(address, "" + plcAddress, sensorString, value, format.format(date));
		
		HeatingPoint p = checkDataType(address, "" + plcAddress, sensorString, true );

		if ( p == null ) {
			System.out.println("Message error");
		}
		
		else {
			boolean isRedundant = checkRedundant(p, new Timestamp(date.getTime()));
		
			if ( !isRedundant ) {
				//资源绑定
				String instanceMsg = resouceInstanceBinding(p);
				float values = value;
				System.out.println(instanceMsg);
				
				if ( p.getDataType() == 0 || ( p.getDataType() == 4 ) ) {
					
					if ( p.getDataType() == 0 ) {
						GLAnalogMeasure glm = (GLAnalogMeasure)p;
						//System.out.println(format2.format(new Date()));
						glm.setTimeStamp(new Timestamp(date.getTime()));
						glm.setDate(new java.sql.Date(date.getTime()));
						glm.setTime(new java.sql.Time(date.getTime()));
						//glm.setValue(analyzeComponent.offsetCalculate((float)glm.getFactor(), (float)glm.getOffset(), value, true));
						glm.setValue(value);
						values = (float) glm.getValue();
						GLAnalogArray.add(p);
					}
					
					else {
						RJLAnalogMeasure ram = (RJLAnalogMeasure)p;
						ram.setTimestamp(new Timestamp(date.getTime()));
						//System.out.println(format2.format(new Date()));
						ram.setDate(new java.sql.Date(date.getTime()));
						ram.setTime(new java.sql.Time(date.getTime()));
						ram.setValue(offsetCalculate((float)ram.getFactor(), (float)ram.getOffset(), value, true));
						values = (float)ram.getValue();
						RJLAnalogArray.add(p);
					}
					
				}
				
				else if ( p.getDataType() == 1 ) {
					GLAnalogControl gac = (GLAnalogControl)p;
					gac.setTimeStamp(new Timestamp(date.getTime()));
					gac.setDate(new java.sql.Date(date.getTime()));
					gac.setTime(new java.sql.Time(date.getTime()));
					//glm.setValue(analyzeComponent.offsetCalculate((float)glm.getFactor(), (float)glm.getOffset(), value, true));
					gac.setValue(value);
					values = (float) gac.getValue();
					GLAnalogControl.add(p);
				}
				
				System.out.println("RealValue:" + values );
				//String publishMsg = ObjectToMess(instanceMsg, values);
				//System.out.println(publishMsg);
				AnalogAlarmConfiguration conf2 = new AnalogAlarmConfiguration();
				conf2.getConnectionString();
				//PublishComponent component = new PublishComponentImpl("http://" + conf2.getUrl() + ":" + conf2.getPort() + "/" + conf2.getServicename(), "http://" + ServicemixConfFactory.conf.getUrl() + ":" + ServicemixConfFactory.conf.getPort());
				//BindingConfiguration conf = new BindingConfiguration();
				//conf.getBindingConfiguration();
				String publishString = "";
				String controlPublish = "";
				
				if ( GLAnalogArray.size() != 0 ) {
					publishString = objectToUpdateMsg(GLAnalogArray);
				}
				
				if ( GLAnalogControl.size() != 0 ) {
					controlPublish = objectToUpdateMsg(GLAnalogControl);
				}
				
				if ( !publishString.equals("")) {
					publishUpdate("all", publishString);
				}
				
				if ( !controlPublish.equals("") ) {
					System.out.println(controlPublish);
					publishUpdate("gl_analog_control", controlPublish);
				}
				
				System.out.println("已发布数据更新");
//				System.out.println(publishString);
//				UpdateDBInfoConfiguration conf3 = new UpdateDBInfoConfiguration();
//				conf3.getUpdateConfiguration();
//				String topics = "";
//				System.out.println(ServicemixConfFactory.conf.getUrl());
//				System.out.println(ServicemixConfFactory.conf.getPort());
//				
//				if ( p.getDataType() == 0 ) {
//					topics = "GL_ANALOG_MEASURE";
//				}
//				
//				else if ( p.getDataType() == 2 ) {
//					topics = "GL_DIGITAL_MEASURE";
//				}
//				
//				else if ( p.getDataType() == 4 ) {
//					topics = "RJL_ANALOG_MEASURE";
//				}
//				
//				else {
//					topics = "RJL_DIGITAL_MEASURE";
//				}
//				
//				component.publish("http://" + conf3.getUrl() + ":" + conf3.getPort() + "/" + conf3.getServicename(), "http://" + ServicemixConfFactory.conf.getUrl() + ":" + ServicemixConfFactory.conf.getPort(), topics, publishString );
				//component.publish("http://" + conf.getUrl() + ":" + conf.getPort() + "/" + conf.getServicename(), "http://" + ServicemixConfFactory.conf.getUrl() + ":" + ServicemixConfFactory.conf.getPort(), instanceMsg, publishMsg);
				
				//事件生成
				if ( p.getDataType() == 1 ) {
					return;
				}
				
				int outbound = checkOutbound(p);
				System.out.println("outbound:" + outbound);
				int types = p.getDataType();
				
				if ( types == 0 ) {
					GLAnalogMeasure gam = (GLAnalogMeasure)p;
					String topic = gam.getMeasure_type();
					gam.setOutbound(outbound);
					String location = gam.getDescription();
					float finalValue = (float)gam.getValue();
					
					if ( outbound != 0 ) {
						String msg = objectToOutbound(gam.getBoilerRoom(), topic, location, topic, finalValue, "" + outbound );
						System.out.println(msg);
						component.publish("GL", msg);
					}
					
				}
				
				else if ( types == 4 ) {
					RJLAnalogMeasure ram = (RJLAnalogMeasure)p;
					String topic = ram.getMeasure_type();
					System.out.println(ram.getDescription());
					String location = ram.getDescription();
					float finalValue = (float)ram.getValue();
					ram.setOutbound(outbound);
					
					if ( outbound != 0 ) {
						String msg = objectToOutbound(ram.getBuilding(), topic, location, topic, finalValue, "" + outbound );
						System.out.println(msg);
						component.publish("GL", msg );
					}
					
				}
				
			}
			
		}
		
	}
	
	public String objectToUpdateMsg( ArrayList<HeatingPoint> heatArray ) {
		StringBuilder builder = new StringBuilder();
		builder.append("<coolsql xmlns=\"\" xmlns:ns6=\"http://docs.oasis-open.org/wsn/b-2\">");
		
		for ( int i = 0; i <= heatArray.size() - 1; i++ ) {
			HeatingPoint p = heatArray.get(i);
			
			if ( p.getDataType() == 0 ) {
				GLAnalogMeasure gam = (GLAnalogMeasure)p;
				builder.append("<Sql>update GL_ANALOG_MEASURE set timestamp = timestamp '" + gam.getTimeStamp() + "', date = date '" + gam.getDate() + "', time = time '" + gam.getTime() + "', value = " + gam.getValue() + ", outbound = " + 
									gam.getOutbound() + " where device_id = '" + gam.getDeviceID() + "' and plc_id = '" + gam.getPLCID() + "' and sensor_id = '" + 
									gam.getSensorID() + "';</Sql>");
			}
			
			else if ( p.getDataType() == 1 ) {
				GLAnalogControl gac = (GLAnalogControl)p;
				builder.append("<Sql>update GL_ANALOG_CONTROL set timestamp = timestamp '" + gac.getTimeStamp() + "', date = date '" + gac.getDate() + "', time = time '" + gac.getTime() + "', value = " + gac.getValue() + 
						" where device_id = '" + gac.getDeviceID() + "' and plc_id = '" + gac.getPLCID() + "' and sensor_id = '" + 
						gac.getSensorID() + "';</Sql>");
			}
		
			else if ( p.getDataType() == 2 ) {
				GLDigitalMeasure gdm = (GLDigitalMeasure)p;
				builder.append("<Sql>update GL_DIGITAL_MEASURE set timestamp = timestamp '" + gdm.getTimestamp() + "', date = date '" + gdm.getDate() + "', time = time '" + gdm.getTime() + "', value = " + gdm.getValue() + ", open = " + 
									gdm.getOpen() + ", close = " + gdm.getClose() + ", isbeyond = " + gdm.getIsbeyond() + ", state = " + gdm.getState() +
									" where device_id = '" + gdm.getDeviceID() + "' and plc_id = '" + gdm.getPLCID() + "' and sensor_id = '" + gdm.getSensorID() 
									+ "';</Sql>");
			}
		
			else if ( p.getDataType() == 3 ) {
				GLDigitalControl gdc = (GLDigitalControl)p;
				builder.append("<Sql>update GL_DIGITAL_CONTROL set timestamp = timestamp '" + gdc.getTimestamp() + "', date = date '" + gdc.getDate() + "', time = time '" + gdc.getTime() + "', value = " + gdc.getValue() + ", open = " + 
						gdc.getOpen() + ", close = " + gdc.getClose() + " where device_id = '" + gdc.getDeviceID() + "' and plc_id = '" + gdc.getPLCID() + "' and sensor_id = '" + gdc.getSensorID() 
						+ "';</Sql>");
			}
			
			else if ( p.getDataType() == 4 ) {
				RJLAnalogMeasure ram = (RJLAnalogMeasure)p;
				builder.append("<Sql>update RJL_ANALOG_MEASURE set timestamp = timestamp '" + ram.getTimestamp() + "', date = date '" + ram.getDate() + "', time = time '" + ram.getTime() + "', value = " + ram.getValue() + ", outbound = " + 
					ram.getOutbound() + " where device_id = '" + ram.getDeviceID() + "' and plc_id = '" + ram.getPLCID() + "' and sensor_id = '" + 
					ram.getSensorID() + "';</Sql>");
			}
		
			else if ( p.getDataType() == 6 ) {
				RJLDigitalMeasure rdm = (RJLDigitalMeasure)p;
				builder.append("<Sql>update RJL_DIGITAL_MEASURE set timestamp = timestamp '" + rdm.getTimestamp() + "', date = date '" + rdm.getDate() + "', time = time '" + rdm.getTime() + "', value = " + rdm.getValue() + ", open = " + 
					rdm.getOpen() + ", close = " + rdm.getClose() + ", isbeyond = " + rdm.getIsbeyond() + ", state = " + rdm.getState() +
					" where device_id = '" + rdm.getDeviceID() + "' and plc_id = '" + rdm.getPLCID() + "' and sensor_id = '" + rdm.getSensorID() 
					+ "';</Sql>");
			}
			
		}
		
		heatArray.clear();
		builder.append("<Level>realtime</Level>");
		builder.append("</coolsql>");
		return builder.toString();
	}
	
	public void UpDigitalData(String address,byte plcAddress,String sensorAddr,int value, int status ) throws Exception{//上传信号量，需与上层模块进行接口设计的协调
		DataAnalyzeComponent analyzeComponent = new DataAnalyzeComponentImpl();
		date = new Date();
//		String date = new Date().toString();
//		String result = "解析后：\n" + date + "设备地址:" + address + "\n";
//		result = result.concat("从机地址:" + "" + plcAddress + "\n");
//		result = result.concat("传感器地址:" + "" + sensorAddr[1] + sensorAddr[0] + "\n");
		//MsgReceiverFactory.receiver.receive(result);
		DataBaseComponent t = new DataBaseComponentImpl();
		analyzeComponent.setDataBaseComponent(t);
		String sensorString = sensorAddr;
		
		System.out.println("Up");
		System.out.println("设备地址是: "+address+"PLC编号: "+plcAddress+"传感器地址:"+sensorString);
		MsgReceiverFactory.receiver.receive(address, "" + plcAddress, sensorString, value, format.format(date));
		HeatingPoint p = analyzeComponent.checkDataType( address, "" + plcAddress, sensorString, false );
		
		if ( p == null ) {
			System.out.println("Message error");
		}
		
		else {
			boolean isRedundant = analyzeComponent.checkRedundant(p, new Timestamp(date.getTime()));
		
			if ( !isRedundant ) {
				//资源绑定
				String instanceMsg = analyzeComponent.resouceInstanceBinding(p);
				float values = 0f;
				System.out.println(instanceMsg);
				
				if ( p.getDataType() == 2 || ( p.getDataType() == 6 ) ) {
					
					if ( p.getDataType() == 2 ) {
						GLDigitalMeasure gdm = (GLDigitalMeasure)p;
						gdm.setTimestamp(new Timestamp(date.getTime()));
						gdm.setDate(new java.sql.Date(date.getTime()));
						gdm.setTime(new java.sql.Time(date.getTime()));
						if ( gdm.getValue() == 0 && ( value == 1 ) ) {
							int a = gdm.getOpen() + 1;
							gdm.setOpen(a);
						}
						
						else if ( gdm.getValue() == 1 && ( value == 0 ) ) {
							int a = gdm.getClose() + 1;
							gdm.setClose(a);
						}
						
						gdm.setValue(value);
						values = gdm.getValue();
						GLDigitalArray.add(p);
					}
					
					else if ( p.getDataType() == 6 ) {
						RJLDigitalMeasure rdm = (RJLDigitalMeasure)p;
						rdm.setTimestamp(Timestamp.valueOf(format.format(new Date())));
						rdm.setDate(java.sql.Date.valueOf(format2.format(new Date()).split(" ")[0]));
						rdm.setTime(java.sql.Time.valueOf(format2.format(new Date()).split(" ")[1]));
						
						if ( rdm.getValue() == 0 && ( value == 1 ) ) {
							int a = rdm.getOpen() + 1;
							rdm.setOpen(a);
						}
						
						else if ( rdm.getValue() == 1 && ( value == 0 ) ) {
							int a = rdm.getClose() + 1;
							rdm.setClose(a);
						}
						
						rdm.setValue(value);
						values = rdm.getValue();
						RJLDigitalArray.add(p);
					}
					
				}
				
				else if ( p.getDataType() == 3 ) {
					GLDigitalControl gdc = (GLDigitalControl)p;
					gdc.setTimestamp(new Timestamp(date.getTime()));
					gdc.setDate(new java.sql.Date(date.getTime()));
					gdc.setTime(new java.sql.Time(date.getTime()));
					gdc.setValue(value);
					values=value;
					GLDigitalControl.add(p);
				}
				
				System.out.println("RealValue:" + values );
				String publishString = "";
				
				if ( GLDigitalControl.size() != 0 ) {
					publishString = objectToUpdateMsg(GLDigitalControl);
					System.out.println(publishString);
				}
				
				if ( !publishString.equals("") ) {
					publishUpdate("gl_digital_control", publishString );
				}
				
				System.out.println("已发布数据更新");
//				String publishMsg = ObjectToMess(instanceMsg, values);
//				System.out.println(publishMsg);
				SwitchAlarmConfiguration conf2 = new SwitchAlarmConfiguration();
				conf2.getAlarmConnection();
				PublishComponent component = new PublishComponentImpl("http://" + conf2.getUrl() + ":" + conf2.getPort() + "/" + conf2.getServiceName(), "http://" + ServicemixConfFactory.conf.getUrl() + ":" + ServicemixConfFactory.conf.getPort());
				//BindingConfiguration conf = new BindingConfiguration();
				//conf.getBindingConfiguration();
				//component.publish("http://" + conf.getUrl() + ":" + conf.getPort() + "/" + conf.getServicename(), "http://" + ServicemixConfFactory.conf.getUrl() + ":" + ServicemixConfFactory.conf.getPort(), instanceMsg, publishMsg);
//				String publishString = objectToUpdateMsg(p);
//				UpdateDBInfoConfiguration conf3 = new UpdateDBInfoConfiguration();
//				conf3.getUpdateConfiguration();
//				String topics = "";
//				
//				if ( p.getDataType() == 0 ) {
//					topics = "GL_ANALOG_MEASURE";
//				}
//				
//				else if ( p.getDataType() == 2 ) {
//					topics = "GL_DIGITAL_MEASURE";
//				}
//				
//				else if ( p.getDataType() == 4 ) {
//					topics = "RJL_ANALOG_MEASURE";
//				}
//				
//				else {
//					topics = "RJL_DIGITAL_MEASURE";
//				}
//				
//				component.publish("http://" + conf3.getUrl() + ":" + conf3.getPort() + "/" + conf3.getServicename(), "http://" + ServicemixConfFactory.conf.getUrl() + ":" + ServicemixConfFactory.conf.getPort(), topics, publishString );
				
				//事件生成
				int outbound = analyzeComponent.checkOutbound(p);
				System.out.println("outbound:" + outbound);
				int types = p.getDataType();
				
				if ( types == 2 ) {
					GLDigitalMeasure gdm = (GLDigitalMeasure)p;
					String topic = gdm.getMeasure_type();
					String location = gdm.getDescription();
					float finalValue = (float)gdm.getValue();
					gdm.setIsbeyond(outbound);
					gdm.setState(outbound);
					
					if ( outbound == 2 ) {
						String msg = objectToOutbound(gdm.getBoilerRoom(), topic, location, topic, finalValue, "" + 4 );
						component.publish("alarmDigital", msg );
					}
					
				}
				
				else if ( types == 6 ) {
					RJLDigitalMeasure rdm = (RJLDigitalMeasure)p;
					String topic = rdm.getMeasure_type();
					String location = rdm.getDescription();
					float finalValue = (float)rdm.getValue();
					rdm.setIsbeyond(outbound);
					rdm.setState(outbound);
					
					if ( outbound == 2 ) {
						String msg = objectToOutbound(rdm.getBuilding(), topic, location, topic, finalValue, "" + 4 );
						System.out.println(msg);
						component.publish("alarmDigital", msg );
					}
					
				}
				
			}
			
		}
		
	}
	
	public static void main ( String[] args ) throws Exception {
		ServicemixConfFactory conf = new ServicemixConfFactory();
		conf.createServicemixConfInstance();
		MsgReceiverFactory factory = new MsgReceiverFactory();
		factory.createMsgReceiverInstance();
		DataAnalyzeComponentImpl a = new DataAnalyzeComponentImpl();
		DataBaseComponent b = new DataBaseComponentImpl();
		a.setDataBaseComponent(b);
		//a.UpAnalogOpcData("4321", "0", "0", 10);
		//a.UpAnalogData("1", 1, "D0", 10, 0);
		//System.out.println(a.resouceInstanceBinding("1", "1", "1"));
	}

	@Override
	public void UpAnalogOpcData(String address, String plcAddress,
			String sensorAddr, float value) throws Exception {
		// TODO Auto-generated method stub
		date = new Date();
		
		System.out.println("Up");
		System.out.println("设备地址是: "+address+"PLC编号: "+plcAddress+"传感器地址:"+sensorAddr);
		//MsgReceiverFactory.receiver.receive(address, "no data".getBytes(), "no data".getBytes().length);
		//MsgReceiverFactory.receiver.receive(address, "" + plcAddress, sensorAddr, value, format.format(date));
		
		HeatingPoint p = checkDataType(address, "" + plcAddress, sensorAddr, true );

		if ( p == null ) {
			System.out.println("Message error");
		}
		
		else {
			boolean isRedundant = checkRedundant(p, Timestamp.valueOf(format.format(date)));
		
			if ( !isRedundant ) {
				//资源绑定
				String instanceMsg = resouceInstanceBinding(p);
				float values = value;
				System.out.println(instanceMsg);
				
				if ( p.getDataType() == 0 || ( p.getDataType() == 4 ) ) {
					
					if ( p.getDataType() == 0 ) {
						GLAnalogMeasure glm = (GLAnalogMeasure)p;
						System.out.println(format2.format(new Date()));
						glm.setTimeStamp(new Timestamp(date.getTime()));
						glm.setDate(new java.sql.Date(date.getTime()));
						glm.setTime(new java.sql.Time(date.getTime()));
						//glm.setValue(analyzeComponent.offsetCalculate((float)glm.getFactor(), (float)glm.getOffset(), value, true));
						glm.setValue(value);
						values = (float) glm.getValue();
						GLAnalogArray.add(p);
					}
					
					else {
						RJLAnalogMeasure ram = (RJLAnalogMeasure)p;
						ram.setTimestamp(Timestamp.valueOf(format.format(new Date())));
						System.out.println(format2.format(new Date()));
						ram.setDate(java.sql.Date.valueOf(format2.format(new Date()).split(" ")[0]));
						ram.setTime(java.sql.Time.valueOf(format2.format(new Date()).split(" ")[1]));
						//ram.setValue(analyzeComponent.offsetCalculate((float)ram.getFactor(), (float)ram.getOffset(), value, true));
						values = (float)ram.getValue();
						RJLAnalogArray.add(p);
					}
					
				}
				
				System.out.println("RealValue:" + values );
				//String publishMsg = ObjectToMess(instanceMsg, values);
				//System.out.println(publishMsg);
				AnalogAlarmConfiguration conf2 = new AnalogAlarmConfiguration();
				conf2.getConnectionString();
				//PublishComponent component = new PublishComponentImpl("http://" + conf2.getUrl() + ":" + conf2.getPort() + "/" + conf2.getServicename(), "http://" + ServicemixConfFactory.conf.getUrl() + ":" + ServicemixConfFactory.conf.getPort());
				//BindingConfiguration conf = new BindingConfiguration();
				//conf.getBindingConfiguration();
				String publishString = objectToUpdateMsg(GLAnalogArray);
				publishUpdate("all", publishString);
				System.out.println("已发布数据更新");
//				System.out.println(publishString);
//				UpdateDBInfoConfiguration conf3 = new UpdateDBInfoConfiguration();
//				conf3.getUpdateConfiguration();
//				String topics = "";
//				System.out.println(ServicemixConfFactory.conf.getUrl());
//				System.out.println(ServicemixConfFactory.conf.getPort());
//				
//				if ( p.getDataType() == 0 ) {
//					topics = "GL_ANALOG_MEASURE";
//				}
//				
//				else if ( p.getDataType() == 2 ) {
//					topics = "GL_DIGITAL_MEASURE";
//				}
//				
//				else if ( p.getDataType() == 4 ) {
//					topics = "RJL_ANALOG_MEASURE";
//				}
//				
//				else {
//					topics = "RJL_DIGITAL_MEASURE";
//				}
//				
//				component.publish("http://" + conf3.getUrl() + ":" + conf3.getPort() + "/" + conf3.getServicename(), "http://" + ServicemixConfFactory.conf.getUrl() + ":" + ServicemixConfFactory.conf.getPort(), topics, publishString );
				//component.publish("http://" + conf.getUrl() + ":" + conf.getPort() + "/" + conf.getServicename(), "http://" + ServicemixConfFactory.conf.getUrl() + ":" + ServicemixConfFactory.conf.getPort(), instanceMsg, publishMsg);
				
				//事件生成
				int outbound = checkOutbound(p);
				System.out.println("outbound:" + outbound);
				int types = p.getDataType();
				
				if ( types == 0 ) {
					GLAnalogMeasure gam = (GLAnalogMeasure)p;
					String topic = gam.getMeasure_type();
					gam.setOutbound(outbound);
					String location = gam.getDescription();
					float finalValue = (float)gam.getValue();
					
					if ( outbound != 0 ) {
						String msg = objectToOutbound(gam.getBoilerRoom(), topic, location, topic, finalValue, "" + outbound );
						System.out.println(msg);
						component.publish("GL", msg);
					}
					
				}
				
				else if ( types == 4 ) {
					RJLAnalogMeasure ram = (RJLAnalogMeasure)p;
					String topic = ram.getMeasure_type();
					System.out.println(ram.getDescription());
					String location = ram.getDescription();
					float finalValue = (float)ram.getValue();
					ram.setOutbound(outbound);
					
					if ( outbound != 0 ) {
						String msg = objectToOutbound(ram.getBuilding(), topic, location, topic, finalValue, "" + outbound );
						System.out.println(msg);
						component.publish("GL", msg );
					}
					
				}
				
			}
			
		}
		
	}
	
	public void UpdateStatus( String address, String plcAddress, String sensorAddr, int status ) throws Exception {
		date = new Date();
		HeatingPoint p = checkDataType(address, "" + plcAddress, sensorAddr, true );

		if ( p == null ) {
			System.out.println("Message error");
		}
		
		else {
			boolean isRedundant = checkRedundant(p, Timestamp.valueOf(format.format(date)));
			
			if ( ( !isRedundant ) && ( p.getDataType() == 0 ) ) {
				GLAnalogMeasure glm = (GLAnalogMeasure)p;
				System.out.println(format2.format(new Date()));
				glm.setTimeStamp(new Timestamp(date.getTime()));
				glm.setDate(new java.sql.Date(date.getTime()));
				glm.setTime(new java.sql.Time(date.getTime()));
				glm.setState(status);
				GLAnalogArray.add(p);
				String publishString = objectToUpdateMsg(GLAnalogArray);
				publishUpdate("all", publishString);
				System.out.println("已发布数据更新");
			}
			
			if ( ( !isRedundant ) && ( p.getDataType() == 3 ) ) {
				GLDigitalControl gdc = (GLDigitalControl)p;
				System.out.println(format2.format(new Date()));
				gdc.setTimestamp(new Timestamp(date.getTime()));
				gdc.setDate(new java.sql.Date(date.getTime()));
				gdc.setTime(new java.sql.Time(date.getTime()));
				gdc.setState(status);
				GLDigitalControl.add(p);
				String publishString = objectToUpdateMsg(GLDigitalControl);
				publishUpdate("all", publishString);
				System.out.println("已发布数据更新");
				
			}
			

		}
		
	}
	 
}
