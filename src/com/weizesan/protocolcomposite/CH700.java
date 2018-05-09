package com.weizesan.protocolcomposite;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;

import com.weizesan.connection.databasecomposite.DataBaseComposite;
import com.weizesan.protocolcomposite.cmodbus.DVP_MODBUS_ASC;
import com.weizesan.protocolcomposite.cmodbus.DVP_MODBUS_RTU;
import com.weizesan.protocolcomposite.cmodbus.SIE_MODBUS_ASC;
import com.weizesan.protocolcomposite.cmodbus.SIE_MODBUS_RTU;
import com.weizesan.protocolcomposite.cmodbus.CSlaveProtocol;
import com.zuowenfeng.variable.DeviceStaticData;



public class CH700 extends CDeviceProtocol{
		
	private Socket c_socket;

	private int c_recordNumber; //保存查询记录的编号	
	private int c_dataprotocoltype;  
	
	private byte c_sendBuffer[]; // 将要发送的数据包
	private byte c_dataBuffer[] = new byte[1024]; // 有上传数据包交给数据层协议来处理
	
	private byte c_deviceNumber[];   //保存h7000设备号
	
	private byte login=(byte)0x01;   //请求注册,心跳
	private byte loginRe=(byte)0x81; //注册成功
	
	private byte logout=(byte)0x02;//请求注销
	private byte logoutRe=(byte)0x82;//重启指令，注销成功

//	private byte finddtuip=(byte)0x03;//查询某dtu ip地址，此处基本无用
//	private byte orderlogin=(byte)0x83;//要求dtu注册
	
//	private byte receivecondsc=(byte)0x05;//收到来自dsc的正确数据包
//	private byte receivecondtu=(byte)0x85;//收到来自dtu的正确数据包,一般不用
	
	private byte updata=(byte)0x09;//数据上报包标识
	private byte downdata=(byte)0x89;//数据下发包标识
	//private byte updataRe=(byte)0x85;//数据上报响应包标识（如果是UDP上报，则需要响应包，TCP方式不需要响应包）
	
//	private byte wrongdsc=(byte)0x04;//收到来自dsc的无效数据包
	private byte wrongdtu=(byte)0x84;//收到来自dtu的无效数据包

	private byte header=(byte)0x7B;//这里表示7B起始位
	private byte tail=(byte)0x7B;//这里表示7B截止
	
	private byte c_responseType;        //这个表示响应类型，如登录响应、心跳响应
	
	private boolean canSend;
	
	private String dvpRtu = "DVP_MODBUS_RTU";
	private String dvpAsc = "DVP_MODBUS_ASC";
	private String sieRtu = "SIE_MODBUS_RTU";
	private String sieAsc = "SIE_MODBUS_ASC";
	
	private String c_device_id ;
	
	private boolean c_hasCommand;              // 是否有下发命令
	private String c_plc_id = "";
	private String c_sensor_id = "";
	private float c_value;
	private int c_type ;
	private int commandNumber;
		
	CSlaveProtocol pl;                 //建立一个数据层协议的实例，如果能识别是什么协议，则具体对其子类实例化

	public CH700(int recordoNo, Socket socket) throws Exception {
		
		c_recordNumber = recordoNo;
		c_socket = socket;
		try {
			InitQuery(); 
		} catch (Exception e) {
			e.printStackTrace();
		}
		switch (c_dataprotocoltype) {
		case 1:
			pl = new DVP_MODBUS_RTU(c_recordNumber); 
			break;
		case 2:
			pl = new DVP_MODBUS_ASC(c_recordNumber); 
			break;
		case 3:
			pl = new SIE_MODBUS_RTU(c_recordNumber); 
			break;
		case 4:
			pl = new SIE_MODBUS_ASC(c_recordNumber); 
		default:
			break;
		}
	}
	
	public boolean ReceiveCommand(ArrayList<String> device_id, ArrayList<String> plc_id,ArrayList<String> sensor_id, ArrayList<Float> value, ArrayList<Integer> type,ArrayList<Boolean> send){
		// 接收下发命令包,判断其中的参数是否合法
		if(device_id.size() == 0|| plc_id.size() == 0 || sensor_id.size() == 0 ||
				value.size() == 0 || type.size() == 0 || send.size() == 0){
			c_hasCommand = false;
			return c_hasCommand;
		}else if(device_id == null || plc_id == null || sensor_id == null ||
				value == null || type == null || send == null){
			c_hasCommand = false;
			return c_hasCommand;
			
		}else{
			commandNumber = 0;
			if(c_device_id.equals(device_id.get(commandNumber))){
				c_plc_id = plc_id.get(commandNumber);
				c_sensor_id = sensor_id.get(commandNumber);
				c_value = value.get(commandNumber);
				c_type = type.get(commandNumber);
				c_hasCommand = true;
				return c_hasCommand;
			}else{
				c_hasCommand = false;
				return c_hasCommand;
			}
			
		}
		
	}
	
	public void ReceiveProcess(byte[] buffer, int length) throws Exception {
		// 接收处理，首先需要判断包的类型，如果是01，则为心跳包或登录包，都需要进行响应。
		// 另外，09是上报包，89是下发包，如果是UDP上报包，则需要85对09进行响应，?tcp应该也需要
		
		System.out.println("**********ReceiveProcess处理");		
		if (buffer[0] == header && buffer[length - 1] == tail) {     //包头和包尾都是固定的值,便于判断
			
			if (buffer[1] == login) {// 登录包，则做登录响应,或者是发来的心跳包
				c_responseType = loginRe;
				canSend = true;
				System.out.println("**********解析为登录包");

			} else if (buffer[1] == logout) { // 下线包，做下线响应
				c_responseType = logoutRe;
				canSend = true;
				System.out.println("**********解析为下线包");

			} else if (buffer[1] == wrongdtu) { // 错误包，根据情况决定是否响应
				System.out.println("**********解析为错误包");
				
			} else if(buffer[1] == updata) { // 上报包，做上报处理，根据情况决定是否响应
				System.out.println("**********解析为上报包");
				canSend = false;
				c_dataBuffer = Arrays.copyOfRange(buffer, 15, length-1);
				if (pl != null){
					int symbol = -1;
					try {
						symbol = pl.ReceiveProcess(c_dataBuffer); // 调用MODBUS协议处理
					} catch (Exception e) {
						e.printStackTrace();
					}
					if(symbol == 2){
						DeviceStaticData.device_id.remove(commandNumber);
						DeviceStaticData.plc_id.remove(commandNumber);
						DeviceStaticData.sensor_id.remove(commandNumber);
						DeviceStaticData.value.remove(commandNumber);
						DeviceStaticData.type.remove(commandNumber);
						DeviceStaticData.send.remove(commandNumber);
					}
				}
					
			} else
				return;
		}
	}
	
	public void SendProcess()            
	{
	//发送处理,如果接收到数据包，则进行相关处理，如果是登录或心跳等包，则进行响应，如果是上报数据包，则剥离其设备层外壳，将使用数据层协议的方法来解析
															
		if(c_responseType==loginRe||c_responseType==logoutRe){       //如果是登录响应、心跳响应、下线响应，则可以直接打包发送
			System.out.println("**********SendProcess进行登录响应");
			c_sendBuffer=FormPackage();
		}
		else if (c_responseType==downdata)                         //如果是下发包,则在MODBUS里面进行打包
		{			
			if(pl==null)
				return;
			else{
				if(c_hasCommand){         //如果有命令，则需要打包下发
					try {
						c_dataBuffer = pl.SendCommand(c_plc_id, c_sensor_id, c_value, c_type);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}else{                    //否则发请求包
					try {
						c_dataBuffer=pl.SendProcess();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}				
			}				
			if( c_dataBuffer == null || c_dataBuffer.length<1) 
		    	return;
			else{
				c_sendBuffer=FormPackage();
			}
		}
		if( c_sendBuffer.length>0 )
	       Write(c_sendBuffer);			
	}
	
	public byte[] FormPackage()//将设备层的数据打包
	{
		//如果是登录包|心跳包|下线包等等,直接打包并发送响应报
		//如果是下发包,则需要把设备层的数据包附加在数据层的数据包中
		
		if(c_responseType==loginRe){             //登录包,或者说是心跳包
			byte[] buffer=new byte[16];
			buffer[0]=header;
			buffer[1]=loginRe;
			buffer[2]=0;
			buffer[3]=16;
			for(int i=4,j=0;i<15;i++,j++){          
				buffer[i]=c_deviceNumber[j];
			}
			buffer[15]=tail;
			return buffer;
			
		}else if(c_responseType==logoutRe){          //下线包
			byte[] buffer=new byte[16];
			buffer[0]=header;
			buffer[1]=logoutRe;
			buffer[2]=0;
			buffer[3]=16;
			for(int i=4,j=0;i<15;i++,j++){
				buffer[i]=c_deviceNumber[j];
			}
			buffer[15]=header;
			return buffer;
			
		}else if(c_responseType==downdata){                        //下发包,将MODBUS数据体进行封装起来
				int length=16+c_dataBuffer.length;
				byte[] buffer=new byte[length];
				buffer[0]=header;
				buffer[1]=downdata;
				buffer[2]=0;
				buffer[3]=(byte) length;
				for(int i=4,j=0;i<15;i++,j++){
					buffer[i]=c_deviceNumber[j];
				}							
				for(int i=15,j=0;i<length-1;i++,j++){
					buffer[i]=c_dataBuffer[j];
				}
				buffer[length-1]=tail;
				return buffer;
		}else
			return null;
	}
	
	public void Write(byte[] buffer){                     //发送包	

		OutputStream output = null;
		try {
			output = c_socket.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}		
		try {
			System.out.print("**********Write,");
			System.out.print("内容如下:");
			for(int i=0;i<buffer.length;i++)
				System.out.print(buffer[i]+" ");
			System.out.println();
			if(output != null)
				output.write(buffer);
			else{
				if(c_socket!=null)
					c_socket.close();
				else
					return;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		c_responseType = (byte) 0x00;//响应类型初始化		
	}
	
	
	
	public void MainProcess(byte[] buffer,int length) throws Exception   //主处理
	{
	  
		System.out.println("**********MainProcess处理");
		if(length>1){                               //收到数据包,做相关处理
			System.out.print("**********数据包内容如下:");
			byte[] temp=Arrays.copyOfRange(buffer, 0, length);
			c_deviceNumber = GetNumber(temp);
			for(int i=0;i<length;i++)
				System.out.print(temp[i]+"  ");
			System.out.println();
			ReceiveProcess(temp,length);
			if(canSend){
				System.out.println("**********调用SendProcess");
				SendProcess();
			}
		}else{
			if(length == 1)
				System.out.println("**********调用SendProcess,发命令包");
			else
				System.out.println("**********调用SendProcess,发请求包");
			c_responseType=downdata;
			SendProcess();	
		}
		return;
	}
	
	public void InitQuery() throws Exception{                 //查询数据层协议,建立相关协议的实例
		DataBaseComposite dc=new DataBaseComposite();
		Connection conn=dc.getmysql();
		Statement stmt=conn.createStatement();
		String queryProtocol="select * from deviceinfo where record_number='"+c_recordNumber+"'";
		ResultSet rs=stmt.executeQuery(queryProtocol);
		if(rs.next()){
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
	}
	
	public byte[] GetNumber(byte[] buffer){
		byte[] number=Arrays.copyOfRange(buffer, 4, 15);     //取出其中的设备号
		return number;
	}
}
