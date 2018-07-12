package com.weizesan.protocolcomposite;

//import javax.comm.*;
import gnu.io.*;

import com.weizesan.connection.databasecomposite.DataBaseComposite;
import com.weizesan.protocolcomposite.cmodbus.DVP_MODBUS_ASC;
import com.weizesan.protocolcomposite.cmodbus.DVP_MODBUS_RTU;
import com.weizesan.protocolcomposite.cmodbus.SIE_MODBUS_ASC;
import com.weizesan.protocolcomposite.cmodbus.SIE_MODBUS_RTU;
import com.weizesan.protocolcomposite.cmodbus.CSlaveProtocol;
import com.zuowenfeng.exception.DeviceException;
import com.zuowenfeng.variable.DeviceStaticData;

import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.TooManyListenersException;

public class C485 extends CDeviceProtocol implements SerialPortEventListener,
		CommPortOwnershipListener {
	// 串口所需数据规定
	private Serial485Parameters parameters;
	private OutputStream os;
	private InputStream is;
	private CommPortIdentifier portId;
	private SerialPort sPort;

	private int readbytenum = 0;

	private int c_revLength = 0; // 接收到的数据长度
	private int c_recordNo; // 保存查询记录的编号
	private int c_dataprotocoltype; // 数据层协议类型,如果是MODBUS_RTU，则其值为1等等


	private byte c_dataBuffer[] = new byte[1024]; // 有上传数据包交给数据层协议来处理
	private byte c_tempBuffer[] = new byte[1024]; // 表示接收到的数据包

	private boolean open;
	private boolean readfinish;
	private String c_device_id; // 保存与本机相关的设备号

	private String s_plc_id = "";
	private String s_sensor_id = "";
	private float s_value;
	private int s_type ;
	private int commandNumber;

	private String dvpRtu = "DVP_MODBUS_RTU";
	private String dvpAsc = "DVP_MODBUS_ASC";
	private String sieRtu = "SIE_MODBUS_RTU";
	private String sieAsc = "SIE_MODBUS_ASC";
	
	
	private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss.S");
	private boolean c_hasCommand; // 是否有下发命令
	private boolean dtr;
	private boolean rts;
	
	private DeviceException exception;
	
	private int c_count = 0;

	CSlaveProtocol pl; 

	public C485(int recordoNo) throws Exception {
		
		parameters = new Serial485Parameters();
		c_recordNo = recordoNo;
		exception = new DeviceException(c_recordNo, Timestamp.valueOf(format.format(new Date())));
		try {
			Query485(); 
			openConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}
		switch (c_dataprotocoltype) { 
		case 1:
			pl = new DVP_MODBUS_RTU(c_recordNo); 
			break;
		case 2:
			pl = new DVP_MODBUS_ASC(c_recordNo); 
			break;
		case 3:
			pl = new SIE_MODBUS_RTU(c_recordNo); 
			break;
		case 4:
			pl = new SIE_MODBUS_ASC(c_recordNo); 
		default:
			break;
		}
	}

	public boolean ReceiveCommand(ArrayList<String> device_id, ArrayList<String> plc_id,ArrayList<String> sensor_id, ArrayList<Float> value, ArrayList<Integer> type ,ArrayList<Boolean> send) {

		if (device_id == null || plc_id == null || sensor_id == null
				|| type == null || send == null) {// 如果有非法参数,则置false
			c_hasCommand = false;
			return c_hasCommand;

		} else {
			commandNumber = 0;
			s_plc_id = device_id.get(commandNumber);
			s_sensor_id = sensor_id.get(commandNumber);
			s_value = value.get(commandNumber);
			s_type = type.get(commandNumber);
			c_hasCommand = true;
			return c_hasCommand;
		}
	}

	public byte[] Send() throws Exception  { 

		byte[] sendBuffer;
		if (c_hasCommand) { 		
			sendBuffer = pl.SendCommand(s_plc_id, s_sensor_id, s_value,s_type);			
		} else {
			sendBuffer = pl.SendProcess();
		}
		return sendBuffer;
	}

	public void Write(byte[] buffer) throws Exception { // 写串口
		try {
			System.out.println("**********写串口");
			if (os != null && buffer.length>0){
				System.out.print("内容是: ");
				for(int i=0;i<buffer.length;i++)
					System.out.print(buffer[i]+" ");
				System.out.println();
				os.write(buffer);
			}
		} catch (IOException e) {
			sPort.close();
			open = false;
			System.out.println("Serial Send Error:" + e);
			e.printStackTrace();
			DeviceException exception = new DeviceException(c_recordNo,
					Timestamp.valueOf(format.format(new Date())));
			exception.findExceptionDetails();
			exception.sendDeviceException();
		}
	}

	public void Recevie() throws Exception { // 接收处理

		System.out.println("**********C232接收中");
		byte[] buffer;
		int symbol ;
		if (readfinish) {
			buffer = Arrays.copyOfRange(c_dataBuffer, 0, c_revLength);
			readfinish = false;
			c_revLength = 0;
		} else {
			buffer = null;
		}
		if (buffer != null) {
			c_count = 0;
			symbol = pl.ReceiveProcess(buffer);                  // 调用MODBUS协议处理
			buffer = null;
		} else{
			c_count++;
			System.out.println(c_count);
			if ( c_count > 10 ) {
				exception.findExceptionDetails();
				exception.sendDeviceException();
				c_count = 0;
			}
			return;
		}

		// 0表示命令包的响应包CRC校验有错误
		// 1表示命令包的响应包不一致
		// 2表示命令包的响应包是正确的
		// 10表示接收的数据包长度有错误
		// 11表示头尾以及CRC检验有错误
		// 12表示接收到正确的数据包
		
		if (symbol == 2) {
			DeviceStaticData.device_id.remove(commandNumber);
			DeviceStaticData.plc_id.remove(commandNumber);
			DeviceStaticData.sensor_id.remove(commandNumber);
			DeviceStaticData.value.remove(commandNumber);
			DeviceStaticData.send.remove(commandNumber);
			DeviceStaticData.type.remove(commandNumber);
		}
		
	}

	public void sendBreak() {
		// 延迟1秒
		if (sPort == null) {
			try {
				exception.findExceptionDetails();
				exception.sendDeviceException();
				System.out.println("数据已上报");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return;
		}
		sPort.sendBreak(1000);
	}

	public void openConnection() throws Exception {
		// 将数据放入parameters，再取出
		// System.out.println("在类C232的openProcess方法中:");
		try {
			// CommPortIdentifier取得
			// System.out.println(parameters.getPortName());
			portId = CommPortIdentifier.getPortIdentifier(parameters
					.getPortName());
		} catch (NoSuchPortException ex) {
			DeviceException exception = new DeviceException(c_recordNo,
					Timestamp.valueOf(format.format(new Date())));
			exception.findExceptionDetails();
			exception.sendDeviceException();
			System.out.println("设备有问题,已上报");
			// ex.printStackTrace();
		}
		try {
			// 5000ms超时，这个应改为可设
			if (portId != null)
				sPort = (SerialPort) portId.open("portApp", 5000);
		} catch (PortInUseException ex) {
			ex.printStackTrace();
		}

		if (sPort == null)
			return;

		sPort.setDTR(dtr);
		sPort.setRTS(rts);
		setConnectionParameters();
		try {
			os = sPort.getOutputStream();
			is = sPort.getInputStream();
		} catch (IOException e) {
			e.printStackTrace();
			sPort.close();
			System.out.println("开启  i/o streams 错误");
		}
		try {
			sPort.addEventListener(this);
		} catch (TooManyListenersException e) {
			sPort.close();
			System.out.println("too many listeners added");
		}
		sPort.notifyOnDataAvailable(true);
		sPort.notifyOnBreakInterrupt(true);

		// sPort.notifyOnCarrierDetect(true);
		// sPort.notifyOnCTS(true);
		// sPort.notifyOnDSR(true);
		// sPort.notifyOnFramingError(true);
		// sPort.notifyOnOutputEmpty(true);
		// sPort.notifyOnOverrunError(true);
		// sPort.notifyOnParityError(true);
		// sPort.notifyOnRingIndicator(true);

		try {
			sPort.enableReceiveTimeout(30);
		} catch (UnsupportedCommOperationException e) {
		}
		portId.addPortOwnershipListener(this);
		open = true;
	}

	public void setConnectionParameters() {
		// 先记录原来的，如果发生错误就调用原来的设置
		int oldBaudRate = sPort.getBaudRate();
		int oldDatabits = sPort.getDataBits();
		int oldStopbits = sPort.getStopBits();
		int oldParity = sPort.getParity();
		// int oldFlowControl =
		// sPort.getFlowControlMode();//如果原来的需要更改应如何处理？此处未完善

		// 设置connection parameters
		try {
			sPort.setSerialPortParams(parameters.getBaudRate(),
					parameters.getDatabits(), parameters.getStopbits(),
					parameters.getParity());
		} catch (UnsupportedCommOperationException e) {
			parameters.setBaudRate(oldBaudRate);
			parameters.setDatabits(oldDatabits);
			parameters.setStopbits(oldStopbits);
			parameters.setParity(oldParity);
			e.printStackTrace();
		}
		// flow control设定
		try {
			sPort.setFlowControlMode(parameters.getFlowControlIn()
					| parameters.getFlowControlOut());
		} catch (UnsupportedCommOperationException e) {
			e.printStackTrace();
		}
	}

	public void closeConnection() {
		if (!open) {
			return;
		}
		if (sPort != null) {
			try {
				// 关闭 i/o streams
				os.close();
				is.close();
			} catch (IOException e) {
				System.err.println(e);
			}
			sPort.close();
			// 移除portId
			portId.removePortOwnershipListener(this);
		}
		open = false;
		System.out.println("全部关闭了");
	}

	@Override
	public void ownershipChange(int ownType) {
		// CommPortOwnershipListener.PORT_OWNED
		// 标志当前端口被占用,这个状态当一个设备得到了端口使用权时端口由UNOWNED变为此状态
		// CommPortOwnershipListener.PORT_UNOWNED
		// 标志当前端口空闲,未被任何程序占用,也没有程序请求此端口使用权
		// CommPortOwnershipListener.PORT_OWNERSHIP_REQUESTED
		// 标志当前端口被争用,即一个程序占用此端口,而另一个程序请求使用此端口,
		// 如果占用端口的程序监听到此事件,可以调用CommPort的close()方法来释放端口并把占用权交给请求占用权的程序.
		// if(ownType == CommPortOwnershipListener.PORT_UNOWNED)
		// openConnection();
		if (ownType == CommPortOwnershipListener.PORT_OWNERSHIP_REQUESTED)
			closeConnection();
		// if(ownType == CommPortOwnershipListener.PORT_OWNED)
		// sPort.close();

	}

	public void serialEvent(SerialPortEvent event) {// read
		switch (event.getEventType()) {
		case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
			System.out.println("串口输出区已经没数据了");
			break;
		case SerialPortEvent.BI:/* Break interrupt,通讯中断 */
		case SerialPortEvent.OE:/* Overrun error，溢位错误 */
		case SerialPortEvent.FE:/* Framing error，传帧错误 */
		case SerialPortEvent.PE:/* Parity error，校验错误 */
		case SerialPortEvent.CD:/* Carrier detect，载波检测 */
		case SerialPortEvent.CTS:/* Clear to send，清除发送 */
		case SerialPortEvent.DSR:/* Data set ready，数据设备就绪 */
		case SerialPortEvent.RI:/* Ring indicator，响铃指示 */
			System.out.println("出现异常");
			closeConnection();
			break;
		case SerialPortEvent.DATA_AVAILABLE:
			// System.out.println("**********读串口");
			try {
				while (is.available() > 0) {
					readbytenum = is.read(c_tempBuffer);
					for (int i = 0; i < (readbytenum); i++) {
						c_dataBuffer[c_revLength] = c_tempBuffer[i];
						c_revLength++;
					}
				}
				// 需要重新打散
			} catch (IOException ex) {
				return;
			}
			// 通知RecevieProcess已经读完
			// setReadfinishFlg(true);
			readfinish = true;
			// RecevieProcess();
			break;
		}
	}

	public void Query485() throws Exception {// 查询数据层协议 ，查询232端口设置，并放入parameters
		DataBaseComposite dc = new DataBaseComposite();
		Connection conn = dc.getmysql();
		Statement stmt = conn.createStatement();
		String queryProtocol = "select * from deviceinfo where record_number='"
				+ c_recordNo + "'";
		ResultSet rs = stmt.executeQuery(queryProtocol);
		if (rs.next()) {
			if (rs.getString("data_protocol").equals(dvpRtu))
				c_dataprotocoltype = 1;
			else if (rs.getString("data_protocol").equals(dvpAsc))
				c_dataprotocoltype = 2;
			else if (rs.getString("data_protocol").equals(sieRtu))
				c_dataprotocoltype = 3;
			else if (rs.getString("data_protocol").equals(sieAsc))
				c_dataprotocoltype = 4;
			c_device_id = rs.getString("device_id");
		}
		queryProtocol = "select * from rs485 where record_number='" + c_recordNo + "'";
		rs = stmt.executeQuery(queryProtocol);
		if (rs.next()) {
			// 依照数据库设定串口
			parameters.setPortName(rs.getString("portname"));
			parameters.setBaudRate(rs.getString("baudrate"));
			parameters.setFlowControlIn(rs.getString("flowcontrolin"));
			parameters.setFlowControlOut(rs.getString("flowcontrolout"));
			parameters.setDatabits(rs.getString("databits"));
			parameters.setStopbits(rs.getString("stopbits"));
			parameters.setParity(rs.getString("parity"));
			if (rs.getInt("dtr") == 1)
				dtr = true;
			else
				dtr = false;
			if (rs.getInt("rts") == 1)
				rts = true;
			else
				rts = false;

			// System.out.println(parameters.getParityString());
		}
	}
}