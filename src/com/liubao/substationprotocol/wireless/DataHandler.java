package com.liubao.substationprotocol.wireless;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.configuration.UpdateDBInfoConfiguration;
import com.factory.ServicemixConfFactory;
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

public class DataHandler {
	
	private float[] ff;
	private int tmp;//数据包大小
	private DataBaseComponent impl;
	private Connection conn;
	private PublishComponentImpl impls;
	
	public DataHandler(byte[] c_dataBuffer , int tmp) throws Exception{
		System.out.println("读串口数据：");
		System.out.println("数据包大小为："+tmp);
		System.out.println("数据内容：");
		for(int n = 0;n<tmp;n++)
			System.out.print(c_dataBuffer[n]+" ");
		System.out.println();
		System.out.println(new String(c_dataBuffer));
		System.out.println();
		ff = byteArrayToFloatArray(c_dataBuffer, tmp);
		
		this.tmp = tmp;
		
		impl = new DataBaseComponentImpl();
		conn = impl.geth2();
		UpdateDBInfoConfiguration conf = new UpdateDBInfoConfiguration();
		conf.getUpdateConfiguration();
		ServicemixConfFactory factory = new ServicemixConfFactory();
		factory.createServicemixConfInstance();
		impls = new PublishComponentImpl("http://" + conf.getUrl() + ":" + conf.getPort() + "/" + conf.getServicename(), "http://" + ServicemixConfFactory.conf2.getUrl() + ":" + ServicemixConfFactory.conf2.getPort() + "/cxf/NotificationProxy");
	}
	
	public void processData(){
		
		
		for(int t = 0 ; t < ff.length ; t++){
			System.out.print(ff[t]+"  ");
		}
		System.out.println();
		if( ( ((int)ff[2]+3) == ff.length) && ( (int)ff[0]<200 )&& ( (int)ff[0]>10 )){
			System.out.println("这个是标准数据包，请解析");
			int tablenum = (int)ff[0]%10;
			int colnum = (int)ff[0]/10;
			String tablename;
			String columnname;
			switch(tablenum){
			case 1: tablename = "gl_analog_measure"; break;
			case 2: tablename = "gl_analog_control"; break;
			case 3: tablename = "gg_simida"; break;
			case 4: tablename = "gl_digital_control"; break;
			default: tablename = "gg_simida";
			}
			
			switch(colnum){
			case 1: columnname = "VALUE"; break;
			case 2: columnname = "STATE"; break;
			//3暂时空着
			case 4: columnname = "FACTOR"; break;
			case 5: columnname = "OFFSET"; break;
			case 6: columnname = "OUTBOUND"; break;
			case 7: columnname = "BLOCKFLAG"; break;
			case 8: columnname = "BLOCKVALUE"; break;
			case 9: columnname = "CLOSE"; break;
			case 10: columnname = "OPEN"; break;
			case 11: columnname = "WHENOUT"; break;
			case 12: columnname = "ISBEYOND"; break;
			default: columnname = "GGSIMIDA";
			}
			System.out.println(tablename+" "+columnname);
			//ff[1]为这个数据包中数据的Device_ID，ff[2]为数据条目数
			//ff[3]~ff[2+ff[2]]为按顺序排列好的数据
			float[] data = new float[(int)ff[2]];
			System.arraycopy(ff, 3, data, 0, (int) ff[2]);
			System.out.println("截去头部的数据："+data.toString());
			try {
				updatedatabase(((int)ff[1])+"", tablename, columnname,data );
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	//将字节流转换为float[]，方便插入数据库
		public float[] byteArrayToFloatArray(byte[] bb , int dataLen) {
			// TODO Auto-generated method stub
			float[] ff = new float[dataLen/4];
			//豹哥以前这个忘了写除以4，注意同步
			for(int i = 0;i<dataLen/4;i++){
				ff[i] = getFloat(bb,i*4);
			}
			return ff;
		}
		
		//byte[]转换为float类型数据
		public static float getFloat(byte[] b, int index) {
		    int l;
		    l = b[index + 0];
		    l &= 0xff;
		    l |= ((long) b[index + 1] << 8);
		    l &= 0xffff;
		    l |= ((long) b[index + 2] << 16);
		    l &= 0xffffff;
		    l |= ((long) b[index + 3] << 24);
		    return Float.intBitsToFloat(l);
		}
		
		public void updatedatabase( String device_id, String table, String column, float[] value ) throws Exception {
			ArrayList<HeatingPoint> result = checkDataType(device_id, table);
			
			if ( result.size() == 0 ) {
				System.out.println("No suitable data.");
				return;
			}
			
			if ( table.equals("gl_analog_control")) {
				
				if(column.equals("VALUE")) {
					
					for ( int i = 0; i <= value.length - 1; i++ ) {
						((GLAnalogControl)result.get(i)).setValue(value[i]);
					}
					
				}
				
				else if(column.equals("STATE")) {
					
					for ( int i = 0; i <= value.length - 1; i++ ) {
						((GLAnalogControl)result.get(i)).setState((int)value[i]);
					}
					
				}
				
				else if(column.equals("FACTOR")) {
					
					for ( int i = 0; i <= value.length - 1; i++ ) {
						((GLAnalogControl)result.get(i)).setFactor(value[i]);
					}
					
				}
				else if(column.equals("OFFSET")) {
					
					for ( int i = 0; i <= value.length - 1; i++ ) {
						((GLAnalogControl)result.get(i)).setOffset(value[i]);
					}
					
				}
				
				else if(column.equals("BLOCKFLAG")) {
					
					for ( int i = 0; i <= value.length - 1; i++ ) {
						((GLAnalogControl)result.get(i)).setBlockFlag((int)value[i]);
					}
					
				}
				
				else if(column.equals("BLOCKVALUE")) {
					
					for ( int i = 0; i <= value.length - 1; i++ ) {
						((GLAnalogControl)result.get(i)).setBlockValue((int)value[i]);
					}
					
				}
				
				else System.out.println("GL_ANALOG_CONTROL表中查询了未知字段");
			}
			
			else if ( table.equals("gl_analog_measure")) {
				
				if(column.equals("VALUE") ) {
					
					for ( int i = 0; i <= value.length - 1; i++ ) {
						((GLAnalogMeasure)result.get(i)).setValue(value[i]);
					}
					
				}
					
				else if(column.equals("STATE")) {
					
					for ( int i = 0; i <= value.length - 1; i++ ) {
						((GLAnalogMeasure)result.get(i)).setState((int)value[i]);
					}
					
				}
				//十位的3暂时空着
				else if(column.equals("FACTOR")) {
					
					for ( int i = 0; i <= value.length - 1; i++ ) {
						((GLAnalogMeasure)result.get(i)).setFactor(value[i]);
					}
					
				}
				
				else if(column.equals("OFFSET")) {
					
					for ( int i = 0; i <= value.length - 1; i++ ) {
						((GLAnalogMeasure)result.get(i)).setOffset(value[i]);
					}
					
				}
				
				else if(column.equals("OUTBOUND")) {
					
					for ( int i = 0; i <= value.length - 1; i++ ) {
						((GLAnalogMeasure)result.get(i)).setOutbound((int)value[i]);
					}
					
				}
				else if(column.equals("BLOCKFLAG")) {
					
					for ( int i = 0; i <= value.length - 1; i++ ) {
						((GLAnalogMeasure)result.get(i)).setBlockFlag((int)value[i]);
					}
					
				}
				
				else if(column.equals("BLOCKVALUE")) {
					
					for ( int i = 0; i <= value.length - 1; i++ ) {
						((GLAnalogMeasure)result.get(i)).setBlockValue((int)value[i]);
					}
					
				}
				
				else System.out.println("GL_ANALOG_MEASURE表中查询了未知字段");
			}
			
			else if ( table.equals("gl_digital_control") ) {
				
				if(column.equals("VALUE")) {
					
					for ( int i = 0; i <= value.length - 1; i++ ) {
						((GLDigitalControl)result.get(i)).setValue(value[i]);
					}
					
				}
				
				else if(column.equals("BLOCKFLAG")) {
					
					for ( int i = 0; i <= value.length - 1; i++ ) {
						((GLDigitalControl)result.get(i)).setBlockFlag((int)value[i]);
					}
					
				}
				
				else if(column.equals("BLOCKVALUE")) {
					
					for ( int i = 0; i <= value.length - 1; i++ ) {
						((GLDigitalControl)result.get(i)).setBlockValue((int)value[i]);
					}
					
				}
				
				else if(column.equals("CLOSE")) {
					
					for ( int i = 0; i <= value.length - 1; i++ ) {
						((GLDigitalControl)result.get(i)).setClose((int)value[i]);
					}
					
				}
				else if(column.equals("OPEN")) {
					
					for ( int i = 0; i <= value.length - 1; i++ ) {
						((GLDigitalControl)result.get(i)).setOpen((int)value[i]);
					}
					
				}
				
			}
			
			String sb = objectToUpdateMsg( result );
			String topic = table;
			
			if ( table.equals("gl_analog_measure")) {
				topic = "all";
			}
			impls.publish(topic, sb);
			System.out.println("已发布数据更新");
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
		
		public ArrayList<HeatingPoint> checkDataType( String device_id, String table ) throws SQLException {
			String hql = "select * from " + table + " where device_id = '" + device_id + "' order by topic;";
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(hql);
			ArrayList<HeatingPoint> arrays = new ArrayList<HeatingPoint> ();
			
			while ( rs.next() ) {
	 			
				if ( table.equals("gl_analog_measure") ) {
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
					arrays.add(glAnalogMeasure);
				}
				
				else if ( table.equals("gl_analog_control") ) {
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
					arrays.add(gac);
				}
				
				else if ( table.equals("gl_digital_control") ) {
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
					gdc.setState(rs.getInt("state"));
					gdc.setValue(rs.getFloat("value"));
					gdc.setOpen(rs.getInt("open"));
					gdc.setClose(rs.getInt("close"));
					arrays.add(gdc);
				}
			}
			
			return arrays;
		}
		
		public void objectToUpdateMsg() {
			StringBuilder builder = new StringBuilder();
			builder.append("<coolsql xmlns=\"\" xmlns:ns6=\"http://docs.oasis-open.org/wsn/b-2\">");
			
		}

}
