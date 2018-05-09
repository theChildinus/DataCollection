package com.zuowenfeng.exception;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.configuration.DeviceAlarmConfiguration;
import com.configuration.NotifyConfiguration;
import com.factory.ServicemixConfFactory;
import com.zuowenfeng.AgentComposite.util.PublishComponent;
import com.zuowenfeng.AgentComposite.util.PublishComponentImpl;
import com.zuowenfeng.monitor.monitorDAO.StationDAO;

public class DeviceException {
	private int recordNo;
	private String exceptionHostname;
	private String exceptionIP;
	private int exceptionPort;
	private Timestamp exceptionTime;
	private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss.S");
	private DeviceAlarmConfiguration dac;
	private PublishComponentImpl component;
	private NotifyConfiguration notifyConf;
	
	public void setTimestamp( Timestamp stamp ) {
		this.exceptionTime = stamp;
	}
	
	public DeviceException( int recordNo, Timestamp timestamp ) throws NumberFormatException, IOException {
		this.recordNo = recordNo;
		this.exceptionTime = timestamp;
		notifyConf = new NotifyConfiguration();
		notifyConf.getNotifyConnection();
		dac = new DeviceAlarmConfiguration();
		dac.getDeviceAlarmConfiguration();
		
		component = new PublishComponentImpl("http://" + dac.getUrl() + ":" + dac.getPort() + "/" + dac.getServicename(), "http://" + notifyConf.getUrl() + ":" + notifyConf.getPort() + "/cxf/NotificationProxy");
	}
	
	public void findExceptionDetails() throws ClassNotFoundException, SQLException, IOException {
		StationDAO dao = new StationDAO();
		ResultSet rs = dao.getAssignedResult(new String[]{"device_name", "device_ipaddress", "device_port"}, "record_number = " + this.recordNo + ";");
		
		if ( rs.next() ) {
			exceptionHostname = rs.getString("device_name");
			exceptionIP = rs.getString("device_ipaddress");
			exceptionPort = rs.getInt("device_port");
		}
		
	}
	
	public void sendDeviceException() throws Exception {
		String content = objectToDeviceAlarm();
		String topics = "deviceError";
		component.publish( topics, content);
	}
	
	public String objectToDeviceAlarm() {
		String msg = "<deviceAlarm:alarm xmlns:deviceAlarm=\"http://docs.oasis-open.org/wsn/b-2\">";
		msg = msg.concat("<deviceAlarm:name>" + exceptionHostname + "</deviceAlarm:name>");
		msg = msg.concat("<deviceAlarm:ip>" + exceptionIP + "</deviceAlarm:ip>");
		msg = msg.concat("<deviceAlarm:port>" + exceptionPort + "</deviceAlarm:port>");
		msg = msg.concat("<deviceAlarm:time>" + format.format(exceptionTime) + "</deviceAlarm:time>");
		msg = msg.concat("<deviceAlarm:comment>" + " "+ "</deviceAlarm:comment>");
		msg = msg.concat("</deviceAlarm:alarm>");
		return msg;
	}
	
	public static void main ( String[] args ) throws NumberFormatException, IOException, ClassNotFoundException, SQLException {
		SimpleDateFormat formats = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss.S");
		DeviceException exception = new DeviceException(1, Timestamp.valueOf(formats.format(new Date())));
		exception.findExceptionDetails();
		System.out.println(exception.objectToDeviceAlarm());
	}
}
