package com.liubao.substationprotocol.wireless;


//import javax.comm.*;
import gnu.io.*;
//import com.configuration.DeviceInfoConfiguration;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.TooManyListenersException;
import java.util.concurrent.TimeUnit;

public class C232  implements SerialPortEventListener,
		CommPortOwnershipListener,Serializable {
	
	private  Serial232Parameters parameters;           // 串口所需数据规定
	private OutputStream os;
	private InputStream is;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	private CommPortIdentifier portId;
	private SerialPort sPort;

	private int readbytenum = 0;
	private int c_revLength = 0;     // 接收到的数据长度
	private int c_recordNo;          // 保存查询记录的编号
	private int c_dataprotocoltype; 

	private byte c_dataBuffer[] ; 
	private byte c_tempBuffer[] = new byte[1024]; 

	private boolean open;
	private boolean readfinish;
	private String c_device_id; // 保存与本机相关的设备号

	private boolean c_hasCommand;              // 是否有下发命令
//	private String s_device_id = "";
	private String s_plc_id = "";
	private String s_sensor_id = "";
	private float s_value;
	private int s_type ;
	
	private String dvpRtu = "DVP_MODBUS_RTU";
	private String dvpAsc = "DVP_MODBUS_ASC";
	private String sieRtu = "SIE_MODBUS_RTU";
	private String sieAsc = "SIE_MODBUS_ASC";
	
	private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss.S");
	
	private int c_count = 0;
	
	public C232() throws Exception {     
		
		parameters = new Serial232Parameters();
	//	c_recordNo = recordoNo;
		try {
			Query232();             
			openConnection();
			System.out.println("已打开连接");
		} catch (Exception e) {
			e.printStackTrace();
		} 		
	}

	public boolean ReceiveCommand(String device_id, String plc_id,String sensor_id, float value, int type,boolean flag) {
		                                                   
		// 接收下发命令包,判断其中的参数是否合法
		
		if (device_id == null || plc_id == null || sensor_id == null || type < 0  || !flag  ) {// 如果有非法参数,则置false	
			c_hasCommand = false;
			return c_hasCommand;
			
		} else if (c_device_id.equals(device_id)) {        
			                                    
				s_plc_id = plc_id;
				s_sensor_id = sensor_id;
				s_value = value;
				s_type = type;
				c_hasCommand = true;
				return c_hasCommand;
				
		} else {
			c_hasCommand = false;
			return c_hasCommand;
		}
	}

	public void Write(byte[] buffer) throws Exception { 
		try {
			System.out.println("**********写串口,内容如下：");
//			if (os != null && buffer.length>0){
//				System.out.print("内容如下: ");
//				for(int i=0;i<buffer.length;i++)
//					System.out.print(buffer[i]+" ");
//				System.out.println();
//				
//			}	
			System.out.println("写入字节长度："+buffer.length+"内容："+Arrays.toString(buffer));
			os.write(buffer);
			//os = null;
		} catch (IOException e) {
		//	sPort.close();
		//	open = false;
		System.out.println("Serial Send Error:" + e);
		e.printStackTrace();
			
		}
	}
	
	public void Read() throws IOException{
//		System.out.println("********读串口");
		
		byte[] tmp = new byte[1024];
		BufferedInputStream bis = new BufferedInputStream(is);
		int count = bis.read(tmp);
		if(count != -1){
			System.out.println("Read方法中：大小为"+count);
//			for(int i = 0 ; i < count ; i++){
//				System.out.print(tmp[i]-'0'+" ");
//			}
			System.out.println(new String(tmp));
			System.out.println("");
		}
		
	}
	
//	public void WriteObject(Object object) throws Exception {
//		try {
//			oos = new ObjectOutputStream(os);
//			oos.writeObject(object);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}		
//	}
//	
//	public void ReceiveObject() throws Exception{
//		ois = new ObjectInputStream(is);
//		ResultSet rs = (ResultSet)ois.readObject();
//		System.out.println(rs.getString(1));
//		//暂时拿第一列测试ResultSet是否能传过去
//	}

	public void Recevie() throws Exception { 

//		System.out.println("**********C232接收操作中");
		byte[] buffer;
		int symbol ;
		if (readfinish) {
//			System.out.println("1");
			buffer = Arrays.copyOfRange(c_dataBuffer, 0, c_revLength);
//			
			readfinish = false;
			c_revLength = 0;
		} else {
			buffer = null;
		}
		if (buffer != null) {
			c_count = 0;
			
			buffer = null;
		} else{
			c_count++;
//			System.out.println("##c232.java##: c_count == "+c_count);
			if ( c_count > 10 ) {
				c_count = 0;
			}
			return;
		}
				
	}

	public void sendBreak() throws ClassNotFoundException {
		// 延迟1秒
		if (sPort == null) {
			try {
				
				System.out.println("数据已上报");
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

		sPort.setDTR(true);
		sPort.setRTS(true);
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
				if(oos!=null)oos.close();
				if(ois!=null)ois.close();
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
		System.out.println("在serialEvent中");
//		while(true)
		{
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
				 System.out.println("**********读串口");
				try {
					int tmp = 0;
					c_dataBuffer = new byte[1024];
					while (is.available() > 0) {
						readbytenum = is.read(c_tempBuffer);
//						c_dataBuffer = new byte[readbytenum];
						for (int i = 0; i < (readbytenum)&&tmp<1024; i++) {
							c_dataBuffer[tmp] = c_tempBuffer[i];
//							c_revLength++;
							tmp++;
						}
						try {
							TimeUnit.MILLISECONDS.sleep(30);
							//感觉50更合适
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					
					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
	//				readbytenum = is.read(c_tempBuffer);
					
					System.out.println(new String(c_dataBuffer));
					try {
						DataHandler dh = new DataHandler(c_dataBuffer, tmp);
						dh.processData();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					
//					is.read(c_tempBuffer);
//					System.out.println("读串口数据：");
//					System.out.println("数据包大小为："+tmp);
//					System.out.println("数据内容：");
//					for(int n = 0;n<tmp;n++)
//						System.out.print(c_dataBuffer[n]+" ");
//					System.out.println();
					
//					System.out.println();
//					float[] ff = byteArrayToFloatArray(c_dataBuffer, tmp);
//					for(int t = 0 ; t < ff.length ; t++){
//						System.out.print(ff[t]+"  ");
//					}
//					System.out.println();
//					if( ( ((int)ff[2]+3) == ff.length) && ( (int)ff[0]<200 )&& ( (int)ff[0]>10 )){
//						System.out.println("这个是标准数据包，请解析");
//						int tablenum = (int)ff[0]%10;
//						int colnum = (int)ff[0]/10;
//						String tablename;
//						String columnname;
//						switch(tablenum){
//						case 1: tablename = "gl_analog_measure"; break;
//						case 2: tablename = "gl_analog_control"; break;
//						case 3: tablename = "gg_simida"; break;
//						case 4: tablename = "gl_digital_control"; break;
//						default: tablename = "gg_simida";
//						}
//						
//						switch(colnum){
//						case 1: columnname = "VALUE"; break;
//						case 2: columnname = "STATE"; break;
//						//3暂时空着
//						case 4: columnname = "FACTOR"; break;
//						case 5: columnname = "OFFSET"; break;
//						case 6: columnname = "OUTBOUND"; break;
//						case 7: columnname = "BLOCKFLAG"; break;
//						case 8: columnname = "BLOCKVALUE"; break;
//						case 9: columnname = "CLOSE"; break;
//						case 10: columnname = "OPEN"; break;
//						case 11: columnname = "WHENOUT"; break;
//						case 12: columnname = "ISBEYOND"; break;
//						default: columnname = "GGSIMIDA";
//						}
//						System.out.println(tablename+" "+columnname);
//						//ff[1]为这个数据包中数据的Device_ID，ff[2]为数据条目数
//						//ff[3]~ff[2+ff[2]]为按顺序排列好的数据
//					}
					
					
					
					// 需要重新打散
				} catch (IOException ex) {
					//return;
				}
				// 通知RecevieProcess已经读完
				// setReadfinishFlg(true);
				readfinish = true;
				// RecevieProcess();
				break;
			}
		}
	}

	public  void Query232() throws Exception {// 查询数据层协议 ，查询232端口设置，并放入parameters
		//DataBaseComposite dc = new DataBaseComposite();
		Connection conn = getmysql();
		Statement stmt = conn.createStatement();

		String queryProtocol = "select * from rs232 where Status=1";
		// System.out.println(queryProtocol);
		ResultSet rs = stmt.executeQuery(queryProtocol);
		if(rs.next()){
		//System.out.println(rs.getInt("Status"));
				// 依照数据库设定串口
			String str = rs.getString("portname");
			System.out.println(str);
		parameters.setPortName(str);
		parameters.setBaudRate(rs.getString("baudrate"));
		parameters.setFlowControlIn(rs.getString("flowcontrolin"));
		parameters.setFlowControlOut(rs.getString("flowcontrolout"));
		parameters.setDatabits(rs.getString("databits"));
		parameters.setStopbits(rs.getString("stopbits"));
		parameters.setParity(rs.getString("parity"));		
		}
	}
	
	public static Connection getmysql() throws IOException {
		// TODO Auto-generated method stub
		try{
		   Class.forName("com.mysql.jdbc.Driver");
		   DeviceInfoConfiguration conf = new DeviceInfoConfiguration();
		   conf.getConnectionString();
		   Connection conn=DriverManager.getConnection("jdbc:mysql://" + conf.getURL() + ":" + conf.getPort() + "/" + conf.getDatabase(), conf.getUsername(), conf.getPassword());
		   return conn;
			
		}catch(ClassNotFoundException e){
			e.printStackTrace();
		}catch(SQLException e)
		{
			e.printStackTrace();
		}
		return null;
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
	
//	public static void main(String[] args) throws Exception{
//	//	C232 cc = new C232();
//		Query232();
//	}
}


