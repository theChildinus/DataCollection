package com.weizesan.protocolcomposite.cmodbus;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import com.factory.MsgReceiverFactory;
import com.weizesan.connection.databasecomposite.DataBaseComposite;
import com.zuowenfeng.AgentComposite.util.DataAnalyzeComponent;
import com.zuowenfeng.AgentComposite.util.DataAnalyzeComponentImpl;


public class SIE_MODBUS_ASC extends CSlaveProtocol {

	private int a_recordNo;                     // 保存记录号，便于查询plc机
	public static int[] digitalCheck = { 1, 2, 4, 8, 16, 32, 64, 128 }; // 取开关量时需要比较的位数
	public static int[] analogCheck = { 0, 1, 3, 7, 15, 31, 63, 127, 255, 511,1023, 2047, 4095 };

	private String plcId;                       //保存正在使用的PLC编号
	private String deviceAddr;                // 设备地址
	private String[] s_plcId;                   // 用字符号存储的plcId
	                                            // 定义需要查询的几张数据表，并且依次轮询
	private String[] databaseTable = { "gl_analog_measure","gl_analog_control","gl_digital_measure","gl_digital_measure"};
	private int pollTable = 0;                // 轮询实时数据库表
	private int pollPlc = 0;                  // 轮询plc
	private int a_waitCount = 0;                // 接收数据的次数，如果超过一定的值，则丢弃该数据包
	private int a_shouldLength;                 // 数据包应该有的长度

	private byte a_ReadOutputStatus = (byte) 0x01;     // 读开出状态
	private byte a_ReadInputStatus = (byte) 0x02;      // 读开入状态
	private byte a_ReadOutputRegister = (byte) 0x03;   // 读模出状态

	private byte a_ForceSingleOutput = (byte) 0x05;     // 写单路开出
	private byte a_ForceMultipleRegister = (byte) 0x10; // 写多路模出

	private byte a_Stx = 0x3A;                // ASC模式的头部
	private byte[] a_End = { 0x0D, 0x0A };    // ASC模式的尾部
	private byte a_sendType;                  // 此处表示下发的类型

	private boolean init_Query = true;        // 初次处理时，需要查询，以后的话不需要查询了

	private byte d_plc_id;                     // PLC编号
	private byte[] d_sensor_id;                // 传感器地址，即为寄存器编号
	private byte d_commandType;                // 下发的命令类型，为写单路或多路
	private byte[] d_value;                    // 下发设定的值,为模拟量或者信号量
	private boolean d_receiveReply;            // 是否接收下发命令包的响应包
	private byte[] d_commandBuffer;            // 下发命令包

	private ArrayList<String> a_sensor_address; // 记录寄存器的地址
	private int a_sensor_count;                 // 记录寄存器的个数
	private int a_sensor_start = 0;             // 记录起始位置
	private int a_sensor_end;                   // 记录结束位置
	private int a_send_analog_count;            // 记录发送模拟量的个数
	private int a_send_digital_count;           // 记录发送数字量的个数
	
	private String a_address ;
	
	private DataAnalyzeComponent components = new DataAnalyzeComponentImpl();
	
	private boolean flag = true;
	
	private String mark ;
	private String beginAddr;

	public SIE_MODBUS_ASC(int recordNo) throws IOException, Exception {
		a_recordNo = recordNo;
		
		DataBaseComposite dc = new DataBaseComposite();
		Connection conn = dc.getmysql();
		Statement stmt = conn.createStatement();
		String queryTable = "select * from siemens_table";
		ResultSet rs = stmt.executeQuery(queryTable);
		while(rs.next()){
			mark = rs.getString("mark");
			beginAddr = rs.getString("beginAddress");
		}
		conn.close();
	}

	public byte[] AnalogDataToByteArray(float value ,int dataType){      //1表示为模拟量的整型值   2表示为模拟量的符点型值

		byte[] buffer = null;
		if (dataType == 1) {                                    
			int temp=(int) value;
			buffer = new byte[2];
			if(temp>0){                                     
				buffer[0] = (byte) (temp&0xFF00);
				buffer[1] = (byte) (temp&0xFF);
			}else if(temp<0){                               
				String str = Integer.toHexString(temp);
				String first = str.substring(4, 6);
				String second = str.substring(6);
				buffer[0]  = (byte) Integer.parseInt(first, 16);
				buffer[1]  = (byte) Integer.parseInt(second, 16);
			}else{                                       
				buffer[0] = 0;
				buffer[1] = 0;
			}						
			
		} else if(dataType == 2) {                               
			buffer = new byte[4];
			if((int)value != 0){
				int c = Float.floatToIntBits(value);
				String str = Integer.toHexString(c);
				for (int i = 0, j = 0; i < buffer.length; i++, j = j + 2) {
					String tempStr = str.substring(j, j + 2);
					int temp = Integer.parseInt(tempStr, 16);
					buffer[i] = (byte) temp;
				}
			}else{
				for(int i=0;i<buffer.length;i++)
					buffer[i] = 0;
			}
			
		}
		return buffer;			
	}
	


	public byte[] FormCommandPackage(String plc_id,String sensor_id,byte[] value,byte command) throws Exception {           //生成命令包
		
		byte[] buffer = null;
		String str = null;
		if(plc_id != null && sensor_id != null){
			str = ConvertAddress(sensor_id);
			d_plc_id = DecimalStringToByte(plc_id);                                   // 设定PLC		
			d_sensor_id = new byte[2];                                                // 设定寄存器地址，用两个字节表示
			d_sensor_id[0] = HexStringToByte(str.substring(0, 2));              // 地址高位
			d_sensor_id[1] = HexStringToByte(str.substring(2));                 // 地址低位
			
		}
		if (command == a_ForceSingleOutput) {                         // 开关量,如 01 05 06 00 FF 00 LRC校验
		    buffer = new byte[7];
			buffer[0] = d_plc_id;                                   //从站
			buffer[1] = command;                                    //功能码
			buffer[2] = d_sensor_id[0];                             //地址高位
			buffer[3] = d_sensor_id[1];                             //地址低位
			buffer[4] = value[0];                                   //设定值高位
			buffer[5] = value[1];                                   //设定值低位
			byte lrc = FormLrc(Arrays.copyOfRange(buffer, 0, 6));   //LRC校验			
			buffer[6] = lrc;
			a_shouldLength = 7*2+3;                                 //响应和下发是一致的
			d_receiveReply = true;
			return buffer;
			
		} else if (command == a_ForceMultipleRegister) {           // 模拟量 			
			if (value.length == 2) {                                //整型值 ,如 01 10 10 00 00 01 02 00 08 LRC校验
				buffer = new byte[10];
				buffer[0] = d_plc_id;                               //从站
				buffer[1] = command;                                //功能码
				buffer[2] = d_sensor_id[0];                         //地址高位
				buffer[3] = d_sensor_id[1];                         //地址低位
				buffer[4] = 0x00;                                   //要修改地址个数高位
				buffer[5] = 0x01;                                   //要修改地址个数低位
				buffer[6] = 0x02;                                   //要写入的字节个数
				buffer[7] = value[0];                    
				buffer[8] = value[1];
				byte lrc = FormLrc(Arrays.copyOfRange(buffer, 0, 9));
				buffer[9] = lrc;
				a_shouldLength = 7*2+3;                             //应该收到的字节
				d_receiveReply = true;
				return buffer;
				
			} else {                                                //符点型值, 如 01 10 10 00 00 02 04 EB A4 41 20 LRC校验
				buffer = new byte[12];
				buffer[0] = d_plc_id;                              //从站
				buffer[1] = command;                               //功能码
				buffer[2] = d_sensor_id[0];                        //地址高位
				buffer[3] = d_sensor_id[1];                        //地址低位
				buffer[4] = 0x00;                                  //要修改地址个数高位
				buffer[5] = 0x02;                                  //要修改地址个数低位
				buffer[6] = 0x04;                                  //要写入的字节个数
				byte[] data = Sort(plc_id,sensor_id,value);
				buffer[7] = data[0];
				buffer[8] = data[1];
				buffer[9] = data[2];
				buffer[10] = data[3];
				byte lrc = FormLrc(Arrays.copyOfRange(buffer, 0, 11));
				buffer[11] = lrc;
				a_shouldLength = 7*2+3;                          //应该收到的字节
				d_receiveReply = true;
				return buffer;
				
			}
		} else {
			return null;
		}
	}
	
	public byte[] Sort(String plc_id,String sensor_id,byte[] value) throws Exception{    // 进行高低字节的校验
		
		DataBaseComposite dc = new DataBaseComposite();
		Connection conn = dc.getmysql();
		Statement stmt = conn.createStatement();
		String sql = "select * from address_division where Device_ID='"+ deviceAddr+ "' and Plc_ID='"+ plc_id+ "' and Start_Address='"+ sensor_id + "' ";
		ResultSet rs = stmt.executeQuery(sql);
		if(rs.next()){
			byte[] data = new byte[4];
			int number = rs.getInt("Arrange");
			int q,b,s,g,temp;
			q=number/1000;
			temp=number%1000;
			b=temp/100;
			temp=temp%100;
			s=temp/10;
			g=temp%10;
			data[0] = value[q-1];
			data[1] = value[b-1];
			data[2] = value[s-1];
			data[3] = value[g-1];
			return data;
			
		}else{
			byte[] data = new byte[4];
			for(int i=0;i<4;i++)
				data[i] = value[i];
			return data;
		}
	}
	
	public byte[] SendCommand(String plc_id, String sensor_id, float value,int downtype) throws Exception { // 下发命令
		System.out.println("********** SIE_MODUSBUS_ASC 处理命令");
		if (downtype == 0) {                           //开关量
			d_commandType = a_ForceSingleOutput;         //功能码05
			d_value = new byte[2];
			int x =(int) value;
			if (x == 1) {                              // 开关量,0则是0000,为1则是FF00
				d_value[0] = (byte) 0xFF;
				d_value[1] = (byte) 0x00;
			} else if(x == 0) {
				d_value[0] = (byte) 0x00;
				d_value[1] = (byte) 0x00;
			}
		} else if(downtype ==1 || downtype == 2){      //模拟量
			d_commandType = a_ForceMultipleRegister;  //功能码 10
			d_value = AnalogDataToByteArray(value,downtype);
		}
		d_commandBuffer = FormCommandPackage(plc_id, sensor_id, d_value, d_commandType);
		byte[] buffer = SendTranform(d_commandBuffer); // 将正常模式转化为ASCII码模式
		return buffer;
	}



	public byte[] SendProcess() throws Exception {      // 发送处理
		System.out.println("********** SIE_MODUSBUS_ASC 发送请求");
		if (init_Query) {                               // 初始化查询
			QueryPLC(a_recordNo);
			init_Query = false;
		}
		byte[] buffer = null;

		System.out.println("**********没有下发命令，则读实时表");
		for (int i = pollTable; i < databaseTable.length;) {
			for (int j = pollPlc; j < s_plcId.length;) {
				if (databaseTable[i].equals("gl_analog_measure") ||databaseTable[i].equals("gl_analog_control")) {         // 锅炉的模拟量，对其进行打包
					plcId = s_plcId[j];
					buffer = FormPackage(s_plcId[j], databaseTable[i]);

				} else if (databaseTable[i].equals("gl_digital_measure")||databaseTable[i].equals("gl_digital_control")) {          // 锅炉的信号量，对其进行打包
					plcId = s_plcId[j];
					buffer = FormPackage(s_plcId[j], databaseTable[i]);
				} 
				break;
			}
			break;
		}
		if (buffer != null) {
			System.out.print("**********转码前的包内容是: ");
			for(int i=0;i<buffer.length;i++)
				System.out.print(buffer[i]+" ");
			System.out.println();
			byte[] tempBuffer = SendTranform(buffer);
			return tempBuffer;
		} else
			return null;
	}

	public byte[] FormPackage(String plc, String table) throws Exception {
		
		if (table.equalsIgnoreCase("gl_analog_measure") || table.equalsIgnoreCase("GL_analog_control")) {                       // 模拟量，则用模拟量的格式对其进行打包，
			byte[] buffer = FormAnalogPackage(plc, table);
			return buffer;
		} else {                                                 // 信号量，则用信号量的格式对其进行打包，
			byte[] buffer = FormDigitalPackage(plc, table);
			return buffer;
		} 
	}

	@SuppressWarnings("unchecked")
	public byte[] FormAnalogPackage(String plc, String table) throws Exception {       // 生成模拟量包
		
		a_sendType = a_ReadOutputRegister;	
		System.out.println("**********PLC_ID是: " + plc + " 功能码是：" + a_sendType+ " 实时数据表为：" + table);
		
		if(flag){
			ArrayList<String> title = new ArrayList<String>();
			ArrayList<Integer> count = new ArrayList<Integer>();
			
			DataBaseComposite dc = new DataBaseComposite();
			Connection conn = dc.getmysql();
			Statement stmt = conn.createStatement();
			String queryTable = "select * from " + table + " where Device_ID='"
					+ deviceAddr + "' and PLC_ID='" + plc+ "' ";
			ResultSet rs = stmt.executeQuery(queryTable);
			while(rs.next()){
				title.add(rs.getString("Sensor_ID"));
				count.add(rs.getInt("Word_Count"));
			}
			conn.close();
			if(title.size()>0){
				a_sensor_address  = GetAnalogAddress(title, count);                  //存放的是实际地址
				ContentComparator comp = new ContentComparator();    
				Collections.sort(a_sensor_address,comp);
				flag = false;
			}
		}
		                             
		a_sensor_count = a_sensor_address.size();
		byte[] a_beginAddr = new byte[2];
		byte[] buffer = new byte[7];                   //ASCII码格式的命令范例如下:01 03 16 00 00 01 xx ,其中xx是前六字节的LRC校验和
		
		if (a_sensor_count > 0) {                               // 执行打包操作

			String address = a_sensor_address.get(a_sensor_start);
			
			a_beginAddr[0] = HexStringToByte(address.substring(0, 2));       //起始地址,如16 00
			a_beginAddr[1] = HexStringToByte(address.substring(2));

			a_address = ReCovertAddress(address);
			
			if (a_sensor_start == a_sensor_count - 1) {                          //当处于末尾时,则只发送一个数据的请求
				a_send_analog_count = 1;
				a_sensor_end = a_sensor_start;
			} else {                                                             //否则按照分区的规定来取数据,如16xx和17xx
				String area = a_sensor_address.get(a_sensor_start).substring(0,
						2);
				for (int i = a_sensor_start + 1; i < a_sensor_count; i++) {
					String areaTest = a_sensor_address.get(i).substring(0, 2);
					if (area.equals(areaTest)) {
						if (i == a_sensor_count - 1) {
							a_sensor_end = i;
							break;
						} else
							i++;
					} else {
						a_sensor_end = i - 1;
						break;
					}
				}
				int last = Integer.parseInt(a_sensor_address.get(a_sensor_end),
						16);
				int begin = Integer.parseInt(
						a_sensor_address.get(a_sensor_start), 16);                         
				if (last - begin >= 100)                                                   
					a_send_analog_count = 100;
				else
					a_send_analog_count = last - begin + 1;
			}
			byte[] count = Calculate(a_send_analog_count);
			buffer[0] = DecimalStringToByte(plc);                         //执行打包过程,PLC-ID  发送类型   起始地址高位  低位  个数高位  低位   LRC校验
			buffer[1] = a_sendType;
			buffer[2] = a_beginAddr[0];
			buffer[3] = a_beginAddr[1];
			buffer[4] = count[2];
			buffer[5] = count[3];
			byte[] lrc_buf = Arrays.copyOfRange(buffer, 0, 6);
			byte lrc = FormLrc(lrc_buf);
			buffer[6] = lrc;
			// count*2表示收到的模拟量的字节个数,+4表示含plc_id\function\Count\Lrc
			// *2表示每个字节都被拆开成二个字节,+3表示含STX一个字节和End的两个字节
			a_shouldLength = (a_send_analog_count * 2 + 4) * 2 + 3;

		} else {                                                               
			if (pollTable <= databaseTable.length - 1
					&& pollPlc < s_plcId.length - 1) {                       // 不在循环边缘，内层加1即可
				pollPlc++;
				flag = true;
			} else if (pollTable < databaseTable.length - 1
					&& pollPlc == s_plcId.length - 1) {                      // 内层在边缘，外层不在，内层置0，外层加1即可
				pollTable++;
				pollPlc = 0;
				flag = true;
			} else if (pollTable == databaseTable.length - 1
					&& pollPlc == s_plcId.length - 1) {                       // 内外层都在边缘，则都置0
				pollTable = 0;
				pollPlc = 0;
				flag = true;
			}
			a_sensor_address.clear();
			buffer = null;
		}
		return buffer;
	}

	@SuppressWarnings("unchecked")
	public byte[] FormDigitalPackage(String plc, String table) throws Exception {   // 生成开关量包
		
		a_sendType = a_ReadOutputStatus;
		System.out.println("**********PLC_ID是: " + plc + " 功能码是："+ a_sendType + " 实时数据表为：" + table);
		
		a_sensor_address = new ArrayList<String>();				
		if(flag){
			
			ArrayList<String> title = new ArrayList<String>();
			
			DataBaseComposite dc = new DataBaseComposite();
			Connection conn = dc.getmysql();
			Statement stmt = conn.createStatement();
			String queryTable = "select * from " + table + " where Device_ID='"
					+ deviceAddr + "' and PLC_ID='" + plc
					+ "' order by  Sensor_ID";
			ResultSet rs = stmt.executeQuery(queryTable);
			while (rs.next()) {
				a_sensor_address.add(rs.getString("Sensor_ID"));             //将与此相关的寄存器号都存储起来
			}
			conn.close();
			
			if(title.size()>0){
				a_sensor_address  = GetDigitalAddress(title);
				ContentComparator comp = new ContentComparator();    
				Collections.sort(a_sensor_address,comp);
				flag = false;
			}
		}
		
		byte[] a_beginAddr = new byte[2];
		byte[] buffer = new byte[7];                  //ASCII码格式的命令范例如下:01 01 08 00 00 01 xx ,其中xx是前六字节的LRC校验和
		a_sensor_count = a_sensor_address.size();
		
		if (a_sensor_count > 0) {

			String address = a_sensor_address.get(a_sensor_start);
			
			a_beginAddr[0] = HexStringToByte(address.substring(0, 2));        //起始地址的高位  低位
			a_beginAddr[1] = HexStringToByte(address.substring(2));
			
			a_address = ReCovertAddress(address);                              //到起始地址,如D0

			if (a_sensor_start == a_sensor_count - 1) {
				a_send_digital_count = 1;
				a_sensor_end = a_sensor_start;
			} else {
				String area = a_sensor_address.get(a_sensor_start).substring(0,2);      //分区,将不同分区的地址隔离开来,如01xx和02xx隔离开来,分多次取数					
				for (int i = a_sensor_start + 1; i < a_sensor_count; i++) {
					String areaTest = a_sensor_address.get(i).substring(0, 2);
					if (area.equals(areaTest)) {
						if (i == a_sensor_count - 1) {
							a_sensor_end = i;
							break;
						} else
							i++;
					} else {
						a_sensor_end = i - 1;
						break;
					}
				}
				int last = Integer.parseInt(a_sensor_address.get(a_sensor_end),                //规定每次取数的个数是多少
						16);
				int begin = Integer.parseInt(
						a_sensor_address.get(a_sensor_start), 16);
				if (last - begin >= 20)
					a_send_digital_count = 20;
				else
					a_send_digital_count = last - begin + 1;
			}
			byte[] datacount = Calculate(a_send_digital_count * 8);
			buffer[0] = DecimalStringToByte(plc);                                     //执行打包过程  PLC-ID 发送类型  起始地址高位  低位  个数高位  低位  LRC校验
			buffer[1] = a_sendType;
			buffer[2] = a_beginAddr[0];
			buffer[3] = a_beginAddr[1];
			buffer[4] = datacount[2];
			buffer[5] = datacount[3];
			byte[] lrc_buf = Arrays.copyOfRange(buffer, 0, 6);
			byte lrc = FormLrc(lrc_buf);
			buffer[6] = lrc;
			// 计算应该收到的MODBUS数据包的长度
			// a_send_digital_count表示收到的数字量的字节个数,+4表示含plc_id\function\Count\Lrc
			// *2表示每个字节都被拆开成二个字节,+3表示含一个STX\End的两个字节
			a_shouldLength = (a_send_digital_count + 4) * 2 + 3;

		} else {
			if (pollTable <= databaseTable.length - 1
					&& pollPlc < s_plcId.length - 1) {          // 不在循环边缘，内层加1即可
				pollPlc++;
				flag = true;
			} else if (pollTable < databaseTable.length - 1
					&& pollPlc == s_plcId.length - 1) {         // 内层在边缘，外层不在，内层置0，外层加1即可
				pollTable++;
				pollPlc = 0;
				flag = true;
			} else if (pollTable == databaseTable.length - 1
					&& pollPlc == s_plcId.length - 1) {         // 内外层都在边缘，则都置0
				pollTable = 0;
				pollPlc = 0;
				flag = true;
			}
			buffer = null;
		}
		return buffer;
	}

	public int ReceiveProcess(byte[] buffer) throws Exception {  // 接收处理

		System.out.println("**********MODBUS_ASC处理");
		if (buffer != null) {                                            
			a_waitCount++;
			if (buffer.length == a_shouldLength) {                       
				byte[] revMsg = Arrays.copyOfRange(buffer, 0,a_shouldLength);
				if (revMsg[0] == a_Stx && revMsg[a_shouldLength - 2] == a_End[0] && revMsg[a_shouldLength - 1] == a_End[1]) { 
					byte[] LrcByte = Arrays.copyOfRange(revMsg, 1,a_shouldLength - 2);     //LrcByte为带有LRC校验码的字节数组,其中存放的是对应的ASCII码						
					byte[] dataByte = ReceiveTranform(LrcByte);                            //将LrcByte中的ASCII码转化为普通的字节数组
					
					if (d_receiveReply) {                                  // 接收下发命令包的响应包
						
						int logo = -1;
						if (CheckLrc(dataByte)) {                          // PLC校验
							for (int i = 0; i < 6; i++)								                                
								if (dataByte[i] != d_commandBuffer[i]) {   // 判断是否一致
									logo = i;
									break;
								}
							if (logo == -1) {                              // 如果标志位仍然为-1，则表示数据完全一致,可以发送请求数据包了
								d_receiveReply = false;
								a_shouldLength = 0;
								System.out.print("**********响应包是正确的,内容是: ");
								for (int i = 0; i < dataByte.length; i++)
									System.out.print(+dataByte[i] + " ");           
								System.out.println();
								return 2;                         
							} else{
								System.out.print("**********响应包有错误,内容是: ");
								for (int i = 0; i < dataByte.length; i++)
									System.out.print(+dataByte[i] + " ");           
								System.out.println();
								return 1;   
							}							                        
						} else {
							System.out.print("**********响应包CRC校验有错,内容是: ");
							for (int i = 0; i < dataByte.length; i++)
								System.out.print(+dataByte[i] + " ");           
							System.out.println();
							return 0;                             
						}
					} else {                                                            // 接收响应包
						if (dataByte[0] == DecimalStringToByte(plcId) && dataByte[1] == a_sendType  && CheckLrc(dataByte)) {
							System.out.print("*********数据包通过校验,其内容是: ");
							for (int i = 0; i < dataByte.length; i++)
								System.out.print(+dataByte[i] + " ");
							System.out.println();
							MsgReceiverFactory.receiver.receive(deviceAddr,dataByte, dataByte.length);
							byte slave_address = dataByte[0];                           // 记录收到数据包中plc的ID
							byte function = dataByte[1];                                // 记录功能号
							byte[] dataBuffer = Arrays.copyOfRange(dataByte, 3,dataByte.length - 1);    // 实际数据
							boolean sendOver = true;
							if (function == a_ReadOutputStatus) {                //开关量
								int data;
								int status;
								for (int i = 0; i < dataBuffer.length; i++) { 
									for (int j = 0; j < 8; j++) {
										if ((dataBuffer[i] & digitalCheck[j]) != 0)
											data = 1;
										else
											data = 0;
										status = 1;
										System.out.print("**********上传数据是: ");
										System.out.print("**********设备地址: "+deviceAddr+" PLC编号: "+slave_address+"  寄存器地址:"+a_address+" 值为:"+data+" 状态是:"+status);
										System.out.println();
										components.UpDigitalData(deviceAddr,slave_address, a_address, data, status);
										
										int number = Integer.parseInt(a_address.substring(mark.length()), 10) + 1;
										a_address = mark.concat(Integer.toString(number, 10));
																				
									}
								}
								if (a_send_digital_count == 20) {                       //发送信号量个数为20的时候
									if (a_sensor_end == a_sensor_count - 1) {           //判断是否为arraylist中最后一个,如果是,则跳出,不是,则继续执行
										sendOver = true;
										a_sensor_start = 0;
										a_sensor_end = 0;
									} else {
										sendOver = false;
										for (int i = a_sensor_start + 1; i < a_sensor_count;) {
											int number = Integer.parseInt(a_sensor_address.get(i), 16);
											int start = Integer.parseInt(a_sensor_address.get(a_sensor_start),16);
											if (number - start > 20) {
												a_sensor_start = i;
												break;
											} else
												i++;
										}
									}
								} else {
									if (a_sensor_end == a_sensor_count - 1) {
										sendOver = true;
										a_sensor_start = 0;
										a_sensor_end = 0;
									} else {
										sendOver = false;
										a_sensor_start = a_sensor_end + 1;
									}
								}

							} else if (function == a_ReadInputStatus) {         //信号量
								int data;
								int status;
								for (int i = 0; i < dataBuffer.length; i++) {
									for (int j = 0; j < 8; j++) {
										if ((dataBuffer[i] & digitalCheck[j]) != 0)
											data = 1;
										else
											data = 0;
										status = 1;
										System.out.print("**********上传数据是: ");
										System.out.print("**********设备地址: "+deviceAddr+" PLC编号: "+slave_address+"  寄存器地址:"+a_address+" 值为:"+data+" 状态是:"+status);
										System.out.println();
										components.UpDigitalData(deviceAddr,slave_address, a_address, data, status);
										if (j == 7) {
											int number = Integer.parseInt(a_address.substring(mark.length()), 10) + 1;
											a_address = mark.concat(Integer.toString(number, 10));
										}	
									}
								}
								if (a_send_digital_count == 20) {
									if (a_sensor_end == a_sensor_count - 1) {
										sendOver = true;
										a_sensor_start = 0;
										a_sensor_end = 0;
									} else {
										sendOver = false;
										for (int i = a_sensor_start + 1; i < a_sensor_end;) {
											int number = Integer.parseInt(a_sensor_address.get(i), 16);
											int start = Integer.parseInt(a_sensor_address.get(a_sensor_start),16);
											if (number - start > 20) {
												a_sensor_start = i;
												break;
											} else
												i++;
										}
									}
								} else {
									if (a_sensor_end == a_sensor_count - 1) {
										sendOver = true;
										a_sensor_start = 0;
										a_sensor_end = 0;
									} else {
										sendOver = false;
										a_sensor_start = a_sensor_end + 1;
									}
								}
								
							} else if (function == a_ReadOutputRegister) {       //模拟量

								// 首先需要查数据库,取得当前的地址对应的序列\小数位个数\字节个数
								float data;                         // 符点型数据
								int symbol = 0;                     // 标志位
								int arrange = 0;                    // 字节数组序列
								float rate;                         // 缩放的倍率
								float offSet;                       // 偏移量
								int word_count = 0;                 // 符点型数据占多少字
								boolean notEnd = true;              // 循环判断的结束符		
								int status;
								
								DataBaseComposite dc = new DataBaseComposite();
								Connection conn = dc.getmysql();
								Statement stmt = conn.createStatement();
								while (notEnd) {
									String sql = "select * from address_division where Device_ID='"
											+ deviceAddr
											+ "' and Plc_ID='"
											+ plcId
											+ "' and Start_Address='"
											+ a_address + "'";
									ResultSet rs = stmt.executeQuery(sql);
									if (rs.next()) {
										System.out.println(symbol+" "+a_address);
										System.out.println("**********查到记录,地址是:"+a_address);
										arrange = rs.getInt("Arrange");
										word_count = rs.getInt("Word_Count");
										rate = rs.getFloat("Rate");
										offSet = rs.getFloat("OffSet");
										
										byte[] ss = new byte[word_count * 2];		
										
										if (word_count == 1) {
											ss[0] = dataBuffer[symbol];
											ss[1] = dataBuffer[symbol + 1];											
										} else if (word_count == 2) {
											ss[0] = dataBuffer[symbol];
											ss[1] = dataBuffer[symbol + 1];
											ss[2] = dataBuffer[symbol + 2];
											ss[3] = dataBuffer[symbol + 3];											
										}
										
										data = ByteArrayToFloat(ss, arrange,rate,offSet);
										status =1;
										System.out.println("采集的数据是："+data);
										components.UpAnalogData(deviceAddr,slave_address, a_address, data, status);	
										
										if(word_count==1){
											symbol = symbol + 2;
											int number = Integer.parseInt(a_address.substring(mark.length()), 10)+1;
											a_address = a_address.substring(0, mark.length()).concat(Integer.toString(number, 10));
										
										}else if(word_count==2){
											symbol = symbol + 4;
											int number = Integer.parseInt(a_address.substring(mark.length()), 10)+2;
											a_address = a_address.substring(0, mark.length()).concat(Integer.toString(number, 10));

										}
										
										if (symbol >= dataBuffer.length)
											notEnd = false;
										else
											notEnd = true;
										System.out.println(symbol+" "+a_address);
										System.out.println();
									} else {
										System.out.println(symbol+" "+a_address);
										System.out.println("**********没查到记录,地址是："+a_address);
										symbol = symbol + 2;
										int number = Integer.parseInt(a_address.substring(mark.length()), 10)+1;
										a_address = a_address.substring(0, mark.length()).concat(Integer.toString(number, 10));
										
										if (symbol >= dataBuffer.length)
											notEnd = false;
										else
											notEnd = true;
										System.out.println(symbol+"  "+a_address);
										System.out.println();
									}
								}
								conn.close();
								if (a_send_analog_count == 100) {
									if (a_sensor_end == a_sensor_count - 1) {
										sendOver = true;
										a_sensor_start = 0;
										a_sensor_end = 0;
									} else {
										sendOver = false;
										for (int i = a_sensor_start + 1; i < a_sensor_count;) {
											int number = Integer.parseInt(a_sensor_address.get(i), 16);
											int begin = Integer.parseInt(a_sensor_address.get(a_sensor_start),16);
											if (number - begin > 100) {
												a_sensor_start = i;
												break;
											} else
												i++;
										}
									}
								} else {
									if (a_sensor_end == a_sensor_count - 1) {
										sendOver = true;
										a_sensor_start = 0;
										a_sensor_end = 0;
									} else {
										sendOver = false;
										a_sensor_start = a_sensor_end + 1;
									}
								}
							}
							if (sendOver) {
								if (pollTable <= databaseTable.length - 1
										&& pollPlc < s_plcId.length - 1) {             // 不在循环边缘，内层加1即可
									pollPlc++;
									flag = true;
								} else if (pollTable < databaseTable.length - 1
										&& pollPlc == s_plcId.length - 1) {            // 内层在边缘，外层不在，内层置0，外层加1即可
									pollTable++;
									pollPlc = 0;
									flag = true;
								} else if (pollTable == databaseTable.length - 1
										&& pollPlc == s_plcId.length - 1) {            // 内外层都在边缘，则都置0
									pollTable = 0;
									pollPlc = 0;
									flag = true;
								}
								a_sensor_start = 0;
							}
							a_sensor_address.clear();
							a_waitCount = 0;
							a_shouldLength = 0;
							return 12;                                // 12表示接收完整，并且数据校验正确
						} else {                                          
							System.out.print("**********PLC或者LRC检验有错误,包的内容是: ");
							for (int i = 0; i < dataByte.length; i++)
								System.out.print(dataByte[i] + "  ");
							System.out.println();
							a_shouldLength = 0;
							return 11;                             // 11表示PLC或者LRC检验有错误
						}
					}
				} else { 
					System.out.print("**********包头或包尾有错,包内容是: ");
					for (int i = 0; i < revMsg.length; i++)
						System.out.print(revMsg[i] + "  ");
					System.out.println();
					a_shouldLength = 0;
					return 10;
				}
			} else { 
				byte[] temp=Arrays.copyOfRange(buffer, 0, buffer.length);
				System.out.print("**********长度有错,包内容是: ");
				for (int i = 0; i < temp.length; i++)
					System.out.print(temp[i] + "  ");
				System.out.println();
				a_shouldLength = 0;
				return 9;
			}
		} else {
			System.out.println("**********没有接收到数据包");
			a_shouldLength = 0;
			return 0;
		}
	}

	public String ByteArrayToString(byte[] address) {      //将字节数组表示的地址,转化为String类型表示的地址,如[16,0]转化为1600
		String result = "";
		for (int i = 0; i < address.length; i++) {
			String s = Integer.toHexString(address[i]).toUpperCase();
			if (s.length() < 2)
				s = 0 + s;
			if (address[i] < 0) {
				s = s.substring(6, s.length());
			}
			result = result.concat("" + s);
		}
		return result;
	}

	public byte[] ReceiveTranform(byte[] buffer) {           // 将接收的ASC码对应的字节数组,转化成普通表示的字节数组
		if (buffer != null) {
			int length = buffer.length / 2;
			byte[] ss = new byte[length];
			for (int i = 0, j = 0; i < buffer.length; i = i + 2, j++) {
				char[] b = new char[2];
				b[0] = (char) buffer[i];
				b[1] = (char) buffer[i + 1];
				String s = new String(b);
				ss[j] = (byte) Integer.parseInt(s, 16);
			}
			return ss;
		} else
			return null;
	}

	public byte[] SendTranform(byte[] buffer) {         //将打包好的数据包如,01 03 16 00 00 01 EA 转化为ASCII格式下的字节数组,头和尾已经赋值了
		if (buffer != null) {
			int a_sendLength = buffer.length * 2 + 3;
			byte[] bb = new byte[a_sendLength];
			String result = "";
			for (int i = 0; i < buffer.length; i++) {
				String s = Integer.toHexString(buffer[i]);
				if (s.length() < 2)
					s = 0 + s;
				if (buffer[i] < 0)
					s = s.substring(6, s.length());
				result = result.concat("" + s);
			}
			result = result.toUpperCase();
			bb[0] = a_Stx;
			char[] ss = result.toCharArray();
			for (int i = 0, j = 1; i < ss.length; i++, j++) {
				char temp = ss[i];
				byte a = (byte) temp;
				bb[j] = a;
			}
			bb[a_sendLength - 2] = a_End[0];
			bb[a_sendLength - 1] = a_End[1];
			return bb;
		} else
			return null;
	}

	public float ByteArrayToFloat(byte[] data, int sort, float rate,float offSet) {             // 将字节数组表示的符点型数据转化为所需要的符点型值
		
		float tempValue = 0 ;
		float value = 0 ;
		if (data.length == 2) {                           // 1个字(即2个字节)表示的是整型值,需要进行缩放和偏移量的计算
			int tempData = 0;
			int[] ints = new int[2];
			if (sort == 12) {
				ints[0] = data[0];
				ints[1] = data[1];
			} else if (sort == 21) {
				ints[0] = data[1];
				ints[1] = data[0];
			}
			tempData = ints[0] << 8 + ints[1];
			tempValue = tempData * rate + offSet;
		} else if (data.length == 4) {                  // 2个字(即4个字节)表示的是符点型,符合IEEE754标准,也需要进行缩放和偏移量计算
			int q, b, s, g, temp;
			q = sort / 1000;
			temp = sort % 1000;
			b = temp / 100;
			temp = temp % 100;
			s = temp / 10;
			g = temp % 10;
			byte[] buffer = new byte[4];
			buffer[q - 1] = data[0];
			buffer[b - 1] = data[1];
			buffer[s - 1] = data[2];
			buffer[g - 1] = data[3];
			tempValue = IEEEToFloat(buffer);
			tempValue = tempValue * rate + offSet;
			if (tempValue < 1.0E-5) {
				tempValue = 0.0f;
			}
			BigDecimal bd = new BigDecimal(tempValue);
			value = bd.setScale(4, BigDecimal.ROUND_HALF_UP).floatValue();
		}
		return value;
	}
	
	
	public float IEEEToFloat(byte[] buffer){            //将IEEE754表示的符点型值,转化为真正的符点型值
		String str = ByteArrayToHexString(buffer).toUpperCase();
		double d = Result(str);
		return (float) d;
	}
	
	public static String ByteArrayToHexString(byte[] src) {        //将字节数组转化为16进制字符串
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}
	
	public double Result(String z) {                      //计算符点型值 
		String a = HexStringToBinaryString(z);
		String b, c, d, ch;
		double s = 0, h = 0, re, l = 0;
		int j;
		b = a.substring(0, 1);
		c = a.substring(1, 9);
		d = a.substring(9, 32);
		for (int i = 0; i < 23; i++) {
			ch = d.substring(0, 1);
			j = Integer.parseInt(ch, 2);
			d = d.substring(1);
			s = s + j * Math.pow(2, (-i - 1));

		}
		for (int i = 8; i > 0; i--) {
			ch = c.substring(i - 1);
			j = Integer.parseInt(ch, 2);
			c = c.substring(0, i - 1);
			l = l + j * Math.pow(2, (8 - i));

		}
		h = Integer.parseInt(b);
		if (h == 0)
			re = (s + 1) * Math.pow(2, (l - 127));
		else
			re = -(s + 1) * Math.pow(2, (l - 127));
		return re;
	}
	
	public static String HexStringToBinaryString(String hexString) {     //将HEX字符串转化为BIN进制字符串
		if (hexString == null || hexString.length() % 2 != 0)
			return null;
		String bString = "", tmp;
		for (int i = 0; i < hexString.length(); i++) {
			tmp = "0000"
					+ Integer.toBinaryString(Integer.parseInt(
							hexString.substring(i, i + 1), 16));
			bString += tmp.substring(tmp.length() - 4);
		}
		return bString;
	}


	public boolean CheckLrc(byte[] Lrc_buff) {        // 进行LRC校验，首先需要生成LRC校验码,然后进行比较
		byte[] buffer=Arrays.copyOfRange(Lrc_buff, 0, Lrc_buff.length-1);
		byte lrc = FormLrc(buffer);                   // 根据地址位以及相关的数据位，取得LRC的校验码，然后与结尾收到的校验码进行比较，并返回值
		if (lrc == Lrc_buff[Lrc_buff.length-1])
			return true;
		else
			return false;
	}

	public byte DecimalStringToByte(String str) {     // 将plc的类型值由存储的String值，转化为字节型
		byte b;
		int temp = Integer.parseInt(str, 10);
		b = (byte) temp;
		return b;
	}

	public byte HexStringToByte(String str) {        // 数据库存放地址是用varchar形式，并且是16进制的数据,故因此需要转化成字节形式
		byte b;
		int temp = Integer.parseInt(str, 16);
		b = (byte) temp;
		return b;
	}

	public byte[] Calculate(int n) {                // 将int类型转化为byte类型，主要应用于发送时，计算要取得传感器（地址）一定数量的数据
		byte[] b = new byte[4];
		for (int i = 0; i < 4; i++) {
			b[i] = (byte) (n >> (24 - i * 8));
		}
		return b;
	}

	public byte FormLrc(byte[] CRC_buf) {           // 生成LRC校验码，返回的是整型形式的数据
		byte lrc = 0;
		for (int i = 0; i < CRC_buf.length; i++) {
			lrc = (byte) (CRC_buf[i] + lrc);
		}
		lrc = (byte) (0xFF - lrc);
		lrc = (byte) (lrc + 1);
		return lrc;
	}

	public void QueryPLC(int a_recordNo) throws Exception {// 查询与此主机相关的plc机编号，数据库中存放的是varchar类型，因此需要转化为byte类型
		String temp;
		DataBaseComposite dc = new DataBaseComposite();
		Connection conn = dc.getmysql();
		Statement stmt = conn.createStatement();
		String querySql = "select * from deviceinfo where number='"
				+ a_recordNo + "'";
		ResultSet rs = stmt.executeQuery(querySql);
		if (rs.next()) {
			temp = rs.getString("plc_id");               // 查询与本机相关的plc机编号
			deviceAddr = rs.getString("dnumber");            // 查询与本机相关的设备号
		} else
			return;
		s_plcId = temp.split(",");                    // 将plcId分割成单个字符串数据存放在字符串数组中
		
	}
	
	public ArrayList<String> GetAnalogAddress(ArrayList<String> origin,
			ArrayList<Integer> count) throws IOException, Exception {

		ArrayList<String> address = new ArrayList<String>();

		for (int i = 0; i < origin.size(); i++) {
			if (count.get(i) == 1)
				address.add(ConvertAddress(origin.get(i)));
			else if (count.get(i) == 2) {
				String pre = origin.get(i);
				address.add(ConvertAddress(pre));

				int num = Integer.parseInt(pre.substring(mark.length()), 10) + 1;
				String next = pre.substring(0, mark.length())+ Integer.toString(num, 10);
				address.add(ConvertAddress(next));
			}
		}

		return address;
	}

	public ArrayList<String> GetDigitalAddress(ArrayList<String> origin)
			throws IOException, Exception {

		ArrayList<String> address = new ArrayList<String>();

		for (int i = 0; i < origin.size(); i++) {
			address.add(ConvertAddress(origin.get(i)));
		}
		return address;
	}

	public String ConvertAddress(String origin) {

		String address = null;
		int num = Integer.parseInt(beginAddr, 16)+ Integer.parseInt(origin.substring(mark.length()), 10);
		address = Integer.toHexString(num).toUpperCase();
		if (address.length() == 1) {
			address = "000".concat(address).toUpperCase();
		} else if (address.length() == 2) {
			address = "00".concat(address).toUpperCase();
		} else if (address.length() == 3) {
			address = "0".concat(address).toUpperCase();
		}
		return address;

	}

	public String ReCovertAddress(String str) {
		String address = null;
		int num = Integer.parseInt(str, 16) - Integer.parseInt(beginAddr, 16);
		address = mark.concat(Integer.toString(num, 10)).toUpperCase();
		return address;
	}
}
