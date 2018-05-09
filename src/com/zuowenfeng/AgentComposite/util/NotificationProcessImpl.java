package com.zuowenfeng.AgentComposite.util;

import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.jws.WebService;



import com.factory.MsgReceiverFactory;
import com.factory.PublishFactory;
import com.liubao.substationprotocol.DAO.ReceiveData;
import com.weizesan.connection.connectioncomposite.SetupConnection;
import com.wenpeng.org.apache.servicemix.wsn.push.INotificationProcess;
import com.zuowenfeng.beans.*;
import com.zuowenfeng.message.DownAnalyzedMessage;
import com.zuowenfeng.message.DownMessage;
import com.zuowenfeng.monitor.monitorDAO.StationDAO;
import com.zuowenfeng.variable.DeviceStaticData;


@WebService(endpointInterface="com.wenpeng.org.apache.servicemix.wsn.push.INotificationProcess",serviceName="INotificationProcess")

public class NotificationProcessImpl implements INotificationProcess {
	private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss.S");
	private SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
	
	@Override
	public void notificationProcess(String notification) {
		// TODO Auto-generated method stub
		System.out.println("aa");
		System.out.println(notification);
		Date date = new Date();
		
		try {
			int topicStart = notification.indexOf("<mytopic>");
			int topicEnd = notification.indexOf("</mytopic>");
			int valueStart = notification.indexOf("<value>");
			int valueEnd = notification.indexOf("</value>");
			
			if ( topicStart == -1 || ( ( topicEnd ) == -1 ) || ( valueStart == -1 ) || ( valueEnd == -1 ) ) {
				return;
			}
			
			String topic = notification.substring(topicStart + 9, topicEnd );
			String value = notification.substring(valueStart + 7, valueEnd );
			//String[] splits = topic.split("#");
			DataAnalyzeComponent dac = new DataAnalyzeComponentImpl();
			DataBaseComponent dbc = new DataBaseComponentImpl();
			dac.setDataBaseComponent(dbc);
			//System.out.println(splits[0] + splits[1] + splits[2] );
			HeatingPoint p = dac.findDownLocation(topic);
			
			if ( p == null ) {
				System.out.println("Message error.");
				return;
			}
			
			int types = p.getDataType();
			boolean isRedundant = dac.checkRedundant(p, new Timestamp(date.getTime()));
			String topics = "";
			
			if ( !isRedundant ) {
				String sensor_id = "";
				String plc_id = "";
				float controlValue = 0f;
				int downType = -1 ;
				String device_id = "";
				int type = 0;
				
				switch( types ) {
					case 1:
						GLAnalogControl gac = (GLAnalogControl)p;
						gac.setTimeStamp(new Timestamp(new Date().getTime()));
						gac.setDate(new java.sql.Date(new Date().getTime()));
						gac.setTime(new java.sql.Time(new Date().getTime()));
						float factor = (float)gac.getFactor();
						float offset = (float)gac.getOffset();
						float newValue = dac.offsetCalculate(factor, offset, Float.valueOf(value), false );
						gac.setValue(newValue);
						device_id = gac.getDeviceID();
						sensor_id = gac.getSensorID();
						plc_id = gac.getPLCID();
						controlValue = newValue;
						downType = gac.getState();
						type = gac.getWordType();
						System.out.println( "device_id:" + gac.getDeviceID());
						System.out.println( "sensor_id:" + gac.getSensorID());
						System.out.println( "plc_id:" + gac.getPLCID() );
						System.out.println( "value:" + gac.getValue() );
						System.out.println( "type:" + type );
						topics = "all";
						break;
				
					case 3:
						GLDigitalControl gdc = (GLDigitalControl)p;
						gdc.setTimestamp(Timestamp.valueOf(format.format(new Date())));
						gdc.setDate(java.sql.Date.valueOf(format2.format(new Date()).split(" ")[0]));
						gdc.setTime(java.sql.Time.valueOf(format2.format(new Date()).split(" ")[1]));
						float finalValue = Float.valueOf(value);
						gdc.setValue(finalValue);
						device_id = gdc.getDeviceID();
						sensor_id = gdc.getSensorID();
						plc_id = gdc.getPLCID();
						controlValue = finalValue;
						downType = 0;
						type = gdc.getState();
						if ( finalValue == 1 ) {
							gdc.setOpen(gdc.getOpen() + 1 );
						}
				
						else
							gdc.setClose(gdc.getClose() + 1 );
				
						System.out.println( "device_id:" + gdc.getDeviceID());
						System.out.println( "sensor_id:" + gdc.getSensorID());
						System.out.println( "plc_id:" + gdc.getPLCID() );
						System.out.println( "value:" + gdc.getValue() );
						topics = "all";
						break;
				
					case 5:
						RJLAnalogControl rac = (RJLAnalogControl)p;
						rac.setTimestamp(Timestamp.valueOf(format.format(new Date())));
						rac.setDate(java.sql.Date.valueOf(format2.format(new Date()).split(" ")[0]));
						rac.setTime(java.sql.Time.valueOf(format2.format(new Date()).split(" ")[1]));
						float rjlfactor = rac.getFactor();
						float rjloffset = rac.getOffset();
						float rjlnewValue = dac.offsetCalculate(rjlfactor, rjloffset, Float.valueOf(value), false);
						rac.setValue(rjlnewValue);
						device_id = rac.getDeviceID();
						sensor_id = rac.getSensorID();
						plc_id = rac.getPLCID();
						controlValue = rjlnewValue;
						downType = rac.getState();
						type = rac.getState();
						System.out.println( "device_id:" + rac.getDeviceID());
						System.out.println( "sensor_id:" + rac.getSensorID());
						System.out.println( "plc_id:" + rac.getPLCID() );
						System.out.println( "value:" + rac.getValue() );
						topics = "all";
						break;
				
					case 7:
						RJLDigitalControl rdc = (RJLDigitalControl)p;
						rdc.setTimestamp(Timestamp.valueOf(format.format(new Date())));
						rdc.setDate(java.sql.Date.valueOf(format2.format(new Date()).split(" ")[0]));
						rdc.setTime(java.sql.Time.valueOf(format2.format(new Date()).split(" ")[1]));
						float rjlfinalValue = Float.valueOf(value);
						rdc.setValue(rjlfinalValue);
						device_id = rdc.getDeviceID();
						sensor_id = rdc.getSensorID();
						plc_id = rdc.getPLCID();
						controlValue = rjlfinalValue;
						downType = 0;
						type = 0;
				
						if ( rjlfinalValue == 1 ) {
							rdc.setOpen(rdc.getOpen() + 1 );
						}
				
						else
							rdc.setClose(rdc.getClose() + 1 );
				
						System.out.println( "device_id:" + rdc.getDeviceID());
						System.out.println( "sensor_id:" + rdc.getSensorID());
						System.out.println( "plc_id:" + rdc.getPLCID() );
						System.out.println( "value:" + rdc.getValue() );
						topics = "all";
						break;
				}
				
				String results = objectToUpdateMsg(p);
				PublishFactory.comp.publish(topics, results);
				DeviceStaticData.device_id.add(device_id);
				DeviceStaticData.sensor_id.add(sensor_id);
				DeviceStaticData.plc_id.add(plc_id);
				DeviceStaticData.value.add(controlValue);
				DeviceStaticData.type.add(type);
				
				if ( notification.contains("<flag>") ) {
					DeviceStaticData.send.add(new Boolean(true));
				}
				
				System.out.println(SetupConnection.isWhole);
				if ( SetupConnection.isWhole == true ) {
					StationDAO dao = new StationDAO();
					ResultSet rs = dao.getAssignedResult(new String[]{"server_name"}, "device_id = '" + DeviceStaticData.device_id.get(DeviceStaticData.device_id.size() - 1 ) + "'");
					
					if ( rs.next() ) {
						System.out.println("server_name:" + rs.getString("server_name"));
						for ( int i = 0; i <= ReceiveData.establishedSocketList.size() - 1; i++ ) {
						
							if ( ReceiveData.establishedSocketList.get(i).hostName.equals(rs.getString("server_name"))) {
								Socket socket = ReceiveData.establishedSocketList.get(i).incoming;
								ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
								outputStream.writeObject(DeviceStaticData.device_id);
								outputStream.writeObject(DeviceStaticData.plc_id);
								outputStream.writeObject(DeviceStaticData.sensor_id);
								outputStream.writeObject(DeviceStaticData.value);
								outputStream.writeObject(DeviceStaticData.type);
								DeviceStaticData.device_id.clear();
								DeviceStaticData.plc_id.clear();
								DeviceStaticData.sensor_id.clear();
								DeviceStaticData.value.clear();
								DeviceStaticData.type.clear();
								
								System.out.println("Sending complete.");
								break;
							}
							
						}
						
					}
					
				}
				//new C232(0).ReceiveCommand( device_id, plc_id, sensor_id, controlValue, downType);
				//MsgReceiverFactory.receiver.receiveDown(device_id, plc_id, sensor_id, controlValue, downType);
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public String objectToUpdateMsg( HeatingPoint p ) {
		String content = "<coolsql xmlns=\"\" xmlns:ns6=\"http://docs.oasis-open.org/wsn/b-2\">";
		
		if ( p.getDataType() == 1 ) {
			GLAnalogControl gac = (GLAnalogControl)p;
			content = content.concat("<Sql>update GL_ANALOG_CONTROL set timestamp = timestamp '" + gac.getTimeStamp() + "', date = date '" + gac.getDate() + "', time = time '" + gac.getTime() + "', value = " + gac.getValue() + 
									" where device_id = '" + gac.getDeviceID() + "' and plc_id = '" + gac.getPLCID() + "' and sensor_id = '" + 
									gac.getSensorID() + "';</Sql>");
		}
		
		else if ( p.getDataType() == 3 ) {
			GLDigitalControl gdc = (GLDigitalControl)p;
			content = content.concat("<Sql>update GL_DIGITAL_CONTROL set timestamp = timestamp '" + gdc.getTimestamp() + "', date = date '" + gdc.getDate() + "', time = time '" + gdc.getTime() + "', value = " + gdc.getValue() + ", open = " + 
									gdc.getOpen() + ", close = " + gdc.getClose() + " where device_id = '" + gdc.getDeviceID() + "' and plc_id = '" + gdc.getPLCID() + 
									"' and sensor_id = '" + gdc.getSensorID() + "';</Sql>");
		}
		
		else if ( p.getDataType() == 5 ) {
			RJLAnalogControl rac = (RJLAnalogControl)p;
			content = content.concat("<Sql>update RJL_ANALOG_CONTROL set timestamp = timestamp '" + rac.getTimestamp() + "', date = date '" + rac.getDate() + "', time = time '" + rac.getTime() + "', value = " + rac.getValue() + 
									" where device_id = '" + rac.getDeviceID() + "' and plc_id = '" + rac.getPLCID() + "' and sensor_id = '" + rac.getSensorID() 
									+ "';</Sql>");
		}
		
		else if ( p.getDataType() == 7 ) {
			RJLDigitalControl rdc = (RJLDigitalControl)p;
			content = content.concat("<Sql>update RJL_DIGITAL_CONTROL set timestamp = timestamp '" + rdc.getTimestamp() + "', date = date '" + rdc.getDate() + "', time = time '" + rdc.getTime() + "', value = " + rdc.getValue() + ", open = " + 
					rdc.getOpen() + ", close = " + rdc.getClose() + " where device_id = '" + rdc.getDeviceID() + "' and plc_id = '" + rdc.getPLCID() + "' and sensor_id = '" + 
					rdc.getSensorID() + "';</Sql>");
		}
		
		content = content.concat("<Level>realtime</Level>");
		content = content.concat("</coolsql>");
		return content;
	}

}
