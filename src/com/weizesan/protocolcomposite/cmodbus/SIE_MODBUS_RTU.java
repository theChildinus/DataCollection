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
import com.zuowenfeng.AgentComposite.util.DataBaseComponent;
import com.zuowenfeng.AgentComposite.util.DataBaseComponentImpl;

public class SIE_MODBUS_RTU extends CSlaveProtocol {

	private int m_recordNo;                                             // 保存记录号，便于查询plc机
	public static int[] digitalCheck = { 1, 2, 4, 8, 16, 32, 64, 128 }; // 取开关量时需要比较的位数
	public static int[] analogCheck = { 0, 1, 3, 7, 15, 31, 63, 127, 255, 511,1023, 2047, 4095 };


	private String deviceAddr;                                          // 设备地址
	private String plcId;                                               // 保存正在使用的PLC的ID
	private String[] m_plcId;                                           // 用字符串存储连接的所有plcId
	private String m_address;                                           // 记录sensor_id 如D0
	
	private String[] databaseTable = { "GL_analog_measure","GL_analog_control","GL_digital_measure","GL_digital_measure"};
	private int m_pollTable = 0;                                        // 轮询实时数据库表
	private int m_pollPlc = 0;                                          // 轮询plc
	private int m_count = 0;
	private int m_shouldLength;                                         // 接收到的数据包应该为的长度

	private byte m_ReadOutputStatus = (byte) 0x01;                      // 读开出状态
	private byte m_ReadInputStatus = (byte) 0x02;                       // 读开入状态
	private byte m_ReadOutputRegister = (byte) 0x03;                    // 读模出状态

	private byte m_ForceSingleStatus = (byte) 0x05;                     // 强制单路开出
	
	private byte m_ForceMultipleRegister = (byte) 0x10;                 // 强制多路模出

	private byte m_sendType;                                            // 此处表示下发的类型

	private boolean init_Query = true;;                                 // 初次处理时，需要查询，以后的话不需要查询了

	private byte d_plc_id;                                              // PLC编号
	private byte[] d_sensor_id;                                         // 传感器地址
	private byte d_commandType;                                         // 下发的命令类型
	private byte[] d_value;                                             // 下发设定的值                                              
	private boolean d_receiveReply;                                     // 是否接收下发命令包的响应包
	private byte[] m_commandBuffer;
	
	private ArrayList<String> m_sensor_address;                         // 纪录与PLC相关的传感器地址
	private int m_sensor_start = 0;                                     // 记录从m_sensor_address开始的位置
	private int m_sensor_end = 0;                                       // 记录从m_sensor_address结束的位置
	private int m_sensor_count;                                         // 记录m_sensor_address的个数
	private int m_send_analog_count;                                    // 记录发送模拟量的个数
	private int m_send_digital_count;                                   // 记录发送开关量的个数
		
	private boolean flag = true;
	
//	private String mark ;
//	private String beginAddr ;
	
	private DataAnalyzeComponent components = new DataAnalyzeComponentImpl();

	public SIE_MODBUS_RTU(int recordNo) throws IOException, Exception {
		
		m_recordNo = recordNo;
		DataBaseComponent dbc = new DataBaseComponentImpl();
		components.setDataBaseComponent(dbc);
		
//		DataBaseComposite dc = new DataBaseComposite();
//		Connection conn = dc.getmysql();
//		Statement stmt = conn.createStatement();
//		String queryTable = "select * from siemens_table";
//		ResultSet rs = stmt.executeQuery(queryTable);
//		while(rs.next()){
//			mark = rs.getString("mark");
//			beginAddr = rs.getString("beginAddress");
//		}
//		stmt.close();
//		conn.close();
	}

	public byte[] AnalogDataToByteArray(float value ,int dataType){      //1表示为模拟量的整型值   2表示为模拟量的符点型值

		byte[] data = null;
		if (dataType == 1) {                                    
			int temp=(int) value;
			data = new byte[2];
			if(temp>0){                                     
				data[0] = (byte) (temp&0xFF00);
				data[1] = (byte) (temp&0xFF);
			}else if(temp<0){                               
				String str = Integer.toHexString(temp);
				String first = str.substring(4, 6);
				String second = str.substring(6);
				data[0]  = (byte) Integer.parseInt(first, 16);
				data[1]  = (byte) Integer.parseInt(second, 16);
			}else{                                       
				data[0] = 0;
				data[1] = 0;
			}						
			
		} else if(dataType == 2) {                               
			data = new byte[4];
			if((int)value != 0){
				int c = Float.floatToIntBits(value);
				String str = Integer.toHexString(c);
				for (int i = 0, j = 0; i < data.length; i++, j = j + 2) {
					String tempStr = str.substring(j, j + 2);
					int temp = Integer.parseInt(tempStr, 16);
					data[i] = (byte) temp;
				}
			}else{
				for(int i=0;i<data.length;i++)
					data[i] = 0;
			}			
		}
		return data;			
	}
		
	
	public byte[] FormCommandPackage(String plc_id,String sensor_id,byte[] value,byte command) throws Exception{

		byte[] buffer = null;
//		String str = null;
		if(plc_id != null && sensor_id != null){
//			str = ConvertAddress(sensor_id);
			d_plc_id = DecimalStringToByte(plc_id);                                   // 设定PLC			
			d_sensor_id = new byte[2];                                                // 设定寄存器地址，用两个字节表示
			d_sensor_id[0] = HexStringToByte(sensor_id.substring(0, 2));              // 地址高位
			d_sensor_id[1] = HexStringToByte(sensor_id.substring(2));                 // 地址低位
			
		}
		if(command == m_ForceSingleStatus){                    //写开关量
			buffer = new byte[8];
			buffer[0] = d_plc_id;                            // 从站
			buffer[1] = command;                             // 功能码
			buffer[2] = d_sensor_id[0];                      // 地址高位
			buffer[3] = d_sensor_id[1];                      // 地址低位
			buffer[4] = value[0];                            // 值高位
			buffer[5] = value[1];                            // 值 低位
			byte[] crc = FormCrc(Arrays.copyOfRange(buffer, 0, 6));
			buffer[6] = crc[0];                              // CRC校验高位
			buffer[7] = crc[1];                              // CRC校验低位
			m_shouldLength = 8;
			d_receiveReply = true;

		}else if (command == m_ForceMultipleRegister){       // 写模拟量			
			if(value.length == 2){                           // 整型值
				buffer = new byte[11];
				buffer[0] = d_plc_id;                        // 从站
				buffer[1] = command;                         // 功能码
				buffer[2] = d_sensor_id[0];                  // 地址高位
				buffer[3] = d_sensor_id[1];                  // 地址低位
				buffer[4] = 0x00;                            // 要修改地址个数高位
				buffer[5] = 0x01;                            // 要修改地址个数低位
				buffer[6] = 0x02;                            // 要写入的字节个数
				buffer[7] = value[0];                    
				buffer[8] = value[1];
				byte[] crc = FormCrc(Arrays.copyOfRange(buffer, 0, 9));
				buffer[9] = crc[0];                          
				buffer[10] = crc[1];                         
				m_shouldLength = 8;                           //返回的字节数应该是8个
				d_receiveReply = true;
				
			}else if(value.length == 4){                     // 符点型值
				buffer = new byte[13];
				buffer[0] = d_plc_id;                        // 从站
				buffer[1] = command;                         // 功能码
				buffer[2] = d_sensor_id[0];                  // 地址高位
				buffer[3] = d_sensor_id[1];                  // 地址低位
				buffer[4] = 0x00;                            // 要修改地址个数高位
				buffer[5] = 0x02;                            // 要修改地址个数低位
				buffer[6] = 0x04;                            // 要写入的字节个数
				byte[] data = Sort(plc_id,sensor_id,value);  // 排序
				buffer[7] = data[0];                    
				buffer[8] = data[1];
				buffer[9] = data[2];
				buffer[10] = data[3];
				byte[] crc = FormCrc(Arrays.copyOfRange(buffer, 0, 11));
				buffer[11] = crc[0];                      
				buffer[12] = crc[1];                      
				m_shouldLength = 8;                           //返回的字节数应该是8个
				d_receiveReply = true;	
				
			}
		}
		return buffer;
	}
	
	
	
	public byte[] Sort(String plc_id,String sensor_id,byte[] value) throws Exception{
		
		byte[] data = new byte[4];
		
		DataBaseComposite dc = new DataBaseComposite();
		Connection conn = dc.getmysql();
		Statement stmt = conn.createStatement();
		String sql = "select * from address_division where Device_ID='"+ deviceAddr+ "' and Plc_ID='"+ plc_id+ "' and Start_Address='"+ sensor_id + "' ";
		ResultSet rs = stmt.executeQuery(sql);
		if(rs.next()){			
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
			conn.close();		
		}else{
			for(int i=0;i<4;i++)
				data[i] = value[i];

		}
		return data;
	}

	
	
	public byte[] SendCommand(String plc_id, String sensor_id, float value,int downtype) throws Exception { // 下发遥控指令

		if (downtype == 0) { 
			d_commandType = m_ForceSingleStatus;         //开关量
			d_value = new byte[2];
			int x =(int) value;
			if (x == 1) {                              // 0则是0000,为1则是FF00
				d_value[0] = (byte) 0xFF;
				d_value[1] = (byte) 0x00;
			} else if(x == 0) {
				d_value[0] = (byte) 0x00;
				d_value[1] = (byte) 0x00;
			}			
		} else if(downtype == 1 || downtype == 2 ) {     //模拟量,整型值   符点型值                                      			
			d_commandType = m_ForceMultipleRegister;
			d_value = AnalogDataToByteArray(value,downtype);
		}
		m_commandBuffer = FormCommandPackage(plc_id, sensor_id, d_value, d_commandType);
		return m_commandBuffer;
	}
	
	

	public byte[] SendProcess() throws Exception { // 处理发送请求

		if (init_Query) {             // 查询一次配置表
			QueryPLC(m_recordNo);
			init_Query = false;
		}
		byte[] buffer = null;
		for (int i = m_pollTable; i < databaseTable.length;) {
			for (int j = m_pollPlc; j < m_plcId.length;) {
				if (databaseTable[i].equals("GL_analog_measure") || databaseTable[i].equals("GL_analog_control")) {        // 锅炉的模拟量，对其进行打包
					plcId = m_plcId[j];
					buffer = FormPackage(m_plcId[j], databaseTable[i]);

				} else if (databaseTable[i].equals("GL_digital_measure") || databaseTable[i].equals("GL_digital_control")) {// 锅炉的信号量，对其进行打包
					plcId = m_plcId[j];
					buffer = FormPackage(m_plcId[j], databaseTable[i]);

				} 
				break;
			}
			break;
		}
		return buffer;
	}
	
	

	public byte[] FormPackage(String plc, String table) throws Exception { // 按照一 定的规则进行打包

		if (table == "GL_analog_measure" || table == "GL_analog_control") { // 是模拟量，则用模拟量的方式对其打包，
			byte[] buffer = FormAnalogPackage(plc, table);
			return buffer;
		} else {                                                             // 是数字量，则用数字量的方式打包
			byte[] buffer = FormDigitalPackage(plc, table);
			return buffer;
		}
	}
	


	@SuppressWarnings("unchecked")
	public byte[] FormAnalogPackage(String plc, String table)  throws Exception {   // 对查询模拟量的打包
			
		m_sendType = m_ReadOutputRegister;
		System.out.println("**********PLC_ID是: " + plc + " 功能码是：" + m_sendType+ " 实时数据表为：" + table);
				
		if(flag){                                  //取地址
			ArrayList<String> origin = new ArrayList<String>();
			ArrayList<Integer> count = new ArrayList<Integer>();

			DataBaseComposite dc = new DataBaseComposite();
			Connection conn = dc.getmysql();
			Statement stmt = conn.createStatement();
			String queryTable = "select * from " + table + " where Device_ID='"
					+ deviceAddr + "' and PLC_ID='" + plc+ "'";
			ResultSet rs = stmt.executeQuery(queryTable);
			while (rs.next()) {
				origin.add(rs.getString("Sensor_ID"));
				count.add(rs.getInt("Word_Count"));
			}	
			stmt.close();
			conn.close();
			if(origin.size()>0){
				
				m_sensor_address  = GetAnalogAddress(origin, count);                  //存放的是实际地址
				ContentComparator comp = new ContentComparator();    
				Collections.sort(m_sensor_address,comp);
				flag = false;
			}else{
				m_sensor_count = 0;
			}
		}
							
		byte[] m_beginAddr = new byte[2];
		byte[] buffer = new byte[8];
		
		m_sensor_count = m_sensor_address.size();
		
		if (m_sensor_count > 0) {                     // 有纪录在表中
			
			m_address = m_sensor_address.get(m_sensor_start);
			
			m_beginAddr[0] = HexStringToByte(m_address.substring(0, 2));            // 纪录起始地址
			m_beginAddr[1] = HexStringToByte(m_address.substring(2));
			
//			m_address = ReCovertAddress(m_address);
			
			if (m_sensor_start == m_sensor_count - 1) {
				m_send_analog_count = 1;
				m_sensor_end = m_sensor_start;
			} else {
				String area = m_sensor_address.get(m_sensor_start).substring(0,2);    // 进行分区采集数据
				for (int i = m_sensor_start + 1; i < m_sensor_count;) {
					String areaTest = m_sensor_address.get(i).substring(0, 2);
					if (area.equals(areaTest)) {
						if (i == m_sensor_count - 1) {
							m_sensor_end = i;
							break;
						} else
							i++;
					} else {
						m_sensor_end = i - 1;
						break;
					}
				}
				int last = Integer.parseInt(m_sensor_address.get(m_sensor_end),16);
				int begin = Integer.parseInt(m_sensor_address.get(m_sensor_start), 16);
				if (last - begin >= 100)                                                // 设定一次采集数据的个数
					m_send_analog_count = 100;
				else
					m_send_analog_count = last - begin + 1;
			}
			
			byte[] datacount = Calculate(m_send_analog_count);
			buffer[0] = DecimalStringToByte(plc);                                    // 执行打包过程,依次是PLC-ID,发送类型,起始地址高位\低位,个数高位\低位,以及CRC校验
			buffer[1] = m_sendType;
			buffer[2] = m_beginAddr[0];
			buffer[3] = m_beginAddr[1];
			buffer[4] = datacount[2];
			buffer[5] = datacount[3];
			byte[] CRC_buf = Arrays.copyOfRange(buffer, 0, 6);
			byte[] crc = FormCrc(CRC_buf);
			buffer[6] = crc[0];
			buffer[7] = crc[1];
			m_shouldLength = m_send_analog_count * 2 + 5;                            // 计算应该收到响应包的字节长度
		} else {
			if (m_pollTable <= databaseTable.length - 1
					&& m_pollPlc < m_plcId.length - 1) { // 不在循环边缘，内层加1即可
				m_pollPlc++;
				flag = true;
			} else if (m_pollTable < databaseTable.length - 1
					&& m_pollPlc == m_plcId.length - 1) { // 内层在边缘，外层不在，内层置0，外层加1即可
				m_pollTable++;
				m_pollPlc = 0;
				flag = true;
			} else if (m_pollTable == databaseTable.length - 1
					&& m_pollPlc == m_plcId.length - 1) { // 内外层都在边缘，则都置0
				m_pollTable = 0;
				m_pollPlc = 0;
				flag = true;
			}
			if(m_sensor_address!=null)
				m_sensor_address.clear();
			buffer = null;
		}
		return buffer;
	}

	@SuppressWarnings("unchecked")
	public byte[] FormDigitalPackage(String plc, String table) // 对查询数字量进行打包,其中，8个data形成一个字节数据
			throws Exception {

		m_sendType = m_ReadOutputStatus;
		System.out.println("**********PLC_ID是:  " + plc + " 功能码是： "+ m_sendType + " 实时数据表为： " + table);
						
		if(flag){                                             //取地址		
			ArrayList<String> title = new ArrayList<String>();
			
			DataBaseComposite dc = new DataBaseComposite();
			Connection conn = dc.getmysql();
			Statement stmt = conn.createStatement();
			String queryTable = "select * from " + table + " where Device_ID='"
					+ deviceAddr + "' and PLC_ID='" + plc
					+ "' order by Sensor_ID";
			ResultSet rs = stmt.executeQuery(queryTable);
			while (rs.next()) {
				title.add(rs.getString("Sensor_ID"));
			}	
			conn.close();
			if(title.size()>0){
				m_sensor_address  = GetDigitalAddress(title);
				ContentComparator comp = new ContentComparator();    
				Collections.sort(m_sensor_address,comp);
				flag = false;
			}
		}
		
		byte[] buffer = new byte[8];
		byte[] m_beginAddr = new byte[2];
		
		m_sensor_count = m_sensor_address.size();
		
		if (m_sensor_count > 0) {

			String m_address = m_sensor_address.get(m_sensor_start);
			
			m_beginAddr[0] = HexStringToByte(m_address.substring(0, 2));        // 纪录起始地址
			m_beginAddr[1] = HexStringToByte(m_address.substring(2));
			
//			m_address = ReCovertAddress(address);
						
			if (m_sensor_start == m_sensor_count - 1) {
				m_send_digital_count = 1;
				m_sensor_end = m_sensor_start;
			} else {
				String area = m_sensor_address.get(m_sensor_start).substring(0,2); // 开始分区进行采集数据
				for (int i = m_sensor_start + 1; i < m_sensor_count;) {
					String areaTest = m_sensor_address.get(i).substring(0, 2);
					if (area.equals(areaTest)) {
						if (i == m_sensor_count - 1) {
							m_sensor_end = i;
							break;
						} else
							i++;
					} else {
						m_sensor_end = i - 1;
						break;
					}
				}
				int last = Integer.parseInt(m_sensor_address.get(m_sensor_end),16);
				int begin = Integer.parseInt(m_sensor_address.get(m_sensor_start), 16);
				if (last - begin >= 20)                                           // 设定一次采集数据的个数
					m_send_digital_count = 20;
				else
					m_send_digital_count = last - begin + 1;
			}
			byte[] datacount = Calculate(m_send_digital_count * 8); // 执行打包的过程,依次为PLC-ID,发送类型,起始地址高位\低位,个数高位\低位,以及CRC校验
			buffer[0] = DecimalStringToByte(plc);
			buffer[1] = m_sendType;
			buffer[2] = m_beginAddr[0];
			buffer[3] = m_beginAddr[1];
			buffer[4] = datacount[2];
			buffer[5] = datacount[3];
			byte[] CRC_buf = Arrays.copyOfRange(buffer, 0, 6);
			byte[] crc = FormCrc(CRC_buf);
			buffer[6] = crc[0];
			buffer[7] = crc[1];
			m_shouldLength = m_send_digital_count + 5;
		} else {
			if (m_pollTable <= databaseTable.length - 1
					&& m_pollPlc < m_plcId.length - 1) {// 不在循环边缘，内层加1即可
				m_pollPlc++;
				flag = true;
			} else if (m_pollTable < databaseTable.length - 1
					&& m_pollPlc == m_plcId.length - 1) {// 内层在边缘，外层不在，内层置0，外层加1即可
				m_pollTable++;
				m_pollPlc = 0;
				flag = true;
				
			} else if (m_pollTable == databaseTable.length - 1
					&& m_pollPlc == m_plcId.length - 1) {// 内外层都在边缘，则都置0
				m_pollTable = 0;
				m_pollPlc = 0;
				flag = true;
			}
			if(m_sensor_address!=null)
				m_sensor_address.clear();
			buffer = null;
		}
		return buffer;
	}

	public int ReceiveProcess(byte[] buffer) throws Exception {// 处理数据上报报文，参数为剥离了IHDC和H700协议之后的数据
		
		
		System.out.println("**********MODBUS_RTU处理");
		if (buffer != null) {                                          //是否收到数据
			m_count = 0;
			if (buffer.length == m_shouldLength) {                     //收到的数据包长度是否正确
				byte[] recv = Arrays.copyOfRange(buffer, 0, m_shouldLength);
				byte[] CRC_buf = Arrays.copyOfRange(recv, 0,m_shouldLength - 2);
				byte[] CRC_check = Arrays.copyOfRange(recv,m_shouldLength - 2, m_shouldLength);
				
				if (d_receiveReply) { // 下发命令包的响应包

					System.out.println("**********收到响应包");
					int logo = -1;
					if (CheckCrc16(CRC_buf, CRC_check)) { // CRC校验
						for (int i = 0; i < 6; i++)
							if (recv[i] != m_commandBuffer[i]) { // 循环判断
								logo = i;
								break;
							}
						if (logo == -1) {
							d_receiveReply = false;
							m_shouldLength = 0;
							System.out.print("**********响应包是正确的,内容是: ");
							for (int i = 0; i < recv.length; i++)
								System.out.print(+recv[i] + "  ");
							System.out.println();
							return 2;
						} else {
							System.out.print("**********响应包有错误,内容是: ");
							for (int i = 0; i < recv.length; i++)
								System.out.print(+recv[i] + "  ");
							System.out.println();
							return 1;
						}
					} else {
						System.out.print("**********响应包CRC校验有错,内容是: ");
						for (int i = 0; i < recv.length; i++)
							System.out.print(+recv[i] + "  ");
						System.out.println();
						return 0;
					}

				} else {
					if (recv[0] == DecimalStringToByte(plcId)                                   //检查头\尾,以及CRC校验是否正确
							&& recv[1] == m_sendType
							&& CheckCrc16(CRC_buf, CRC_check)) {
						System.out.print("**********接收的数据包通过头 尾,以及CRC校验,内容是：");
						for (int i = 0; i < recv.length; i++)
							System.out.print(+recv[i] + "  ");
						System.out.println();
						MsgReceiverFactory.receiver.receive(deviceAddr,recv, recv.length);
						byte slave_address = recv[0];                                       // 记录收到数据包中plc的ID
						byte function = recv[1];                                            // 记录功能号
						byte[] dataBuffer = Arrays.copyOfRange(recv, 3,m_shouldLength - 2); // 实际可用的数据字节
						boolean sendOver = true;
						if (function == m_ReadOutputStatus) {
							int data;
							int status;
							for (int i = 0; i < dataBuffer.length; i++) {            //上报的数据是开关量
								for (int j = 0; j < 8; j++) {
									if ((dataBuffer[i] & digitalCheck[j]) != 0)
										data = 1;
									else
										data = 0;
									status = 1;
									System.out.print("**********上传数据是: ");
									System.out.print("**********设备地址: "+deviceAddr+" PLC编号: "+slave_address+"  寄存器地址:"+m_address+" 值为:"+data+" 状态是:"+status);
									System.out.println();
									components.UpDigitalData(deviceAddr,slave_address, m_address, data, status);
									
									int number = Integer.parseInt(m_address, 16) + 1;
									m_address = Integer.toHexString(number).toUpperCase();
									if(m_address.length() == 1){
										m_address = "000".concat(m_address).toUpperCase();
									}else if(m_address.length() == 2){
										m_address = "00".concat(m_address).toUpperCase();
									}else if(m_address.length() == 3){
										m_address = "0".concat(m_address).toUpperCase();
									}
									
//									int number = Integer.parseInt(m_address.substring(mark.length()), 10) + 1;
//									m_address = mark.concat(Integer.toString(number, 10));
										
								}
							}
							if (m_send_digital_count == 20) {
								if (m_sensor_end == m_sensor_count - 1) {
									sendOver = true;
									m_sensor_start = 0;
									m_sensor_end = 0;
								} else {
									sendOver = false;
									for (int i = m_sensor_start + 1; i < m_sensor_end;) {
										int number = Integer.parseInt(
												m_sensor_address.get(i), 16);
										int start = Integer.parseInt(
												m_sensor_address
														.get(m_sensor_start),
												16);
										if (number - start > 20) {
											m_sensor_start = i;
											break;
										} else
											i++;
									}
								}
							} else {
								if (m_sensor_end == m_sensor_count - 1) {
									sendOver = true;
									m_sensor_start = 0;
									m_sensor_end = 0;
								} else {
									sendOver = false;
									m_sensor_start = m_sensor_end + 1;
								}
							}

						} else if (function == m_ReadInputStatus) {                    // 　输入状态,这也是开关量，操作如上
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
									System.out.print("**********设备地址: "+deviceAddr+" PLC编号: "+slave_address+"  寄存器地址:"+m_address+" 值为:"+data+" 状态是:"+status);
									System.out.println();
									components.UpDigitalData(deviceAddr,slave_address, m_address, data, status);
									
									int number = Integer.parseInt(m_address, 16) + 1;
									m_address = Integer.toHexString(number).toUpperCase();
									if(m_address.length() == 1){
										m_address = "000".concat(m_address).toUpperCase();
									}else if(m_address.length() == 2){
										m_address = "00".concat(m_address).toUpperCase();
									}else if(m_address.length() == 3){
										m_address = "0".concat(m_address).toUpperCase();
									}
									
//									int number = Integer.parseInt(m_address.substring(mark.length()), 10) + 1;
//									m_address = mark.concat(Integer.toString(number, 10));
										
								}
							}
						
							if (m_send_digital_count == 20) {
								if (m_sensor_end == m_sensor_count - 1) {
									sendOver = true;
									m_sensor_start = 0;
									m_sensor_end = 0;
								} else {
									sendOver = false;
									for (int i = m_sensor_start + 1; i < m_sensor_end;) {
										int number = Integer.parseInt(
												m_sensor_address.get(i), 16);
										int start = Integer.parseInt(
												m_sensor_address
														.get(m_sensor_start),
												16);
										if (number - start > 20) {
											m_sensor_start = i;
											break;
										} else
											i++;
									}
								}
							} else {
								if (m_sensor_end == m_sensor_count - 1) {
									sendOver = true;
									m_sensor_start = 0;
									m_sensor_end = 0;
								} else {
									sendOver = false;
									m_sensor_start = m_sensor_end + 1;
								}
							}

						} else if (function == m_ReadOutputRegister) {                   // 模拟量数据,需要进行计算,得出其值
							
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
							Connection conn=dc.getmysql();
							Statement stmt = conn.createStatement();
							
							while (notEnd) {
								String sql = "select * from address_division where Device_ID='"
										+ deviceAddr
										+ "' and Plc_ID='"
										+ plcId
										+ "' and Start_Address='"
										+ m_address + "' ";
								ResultSet rs = stmt.executeQuery(sql);
								if (rs.next()) {
									System.out.println(symbol+" "+m_address);
									System.out.println("**********查到记录,地址是:"+m_address);
									arrange = rs.getInt("Arrange");
									word_count = rs.getInt("Word_Count");
									rate = rs.getFloat("Rate");
									offSet = rs.getFloat("OffSet");
									byte[] ss = new byte[word_count * 2];

									if (word_count == 1) {
										ss[0] = dataBuffer[symbol];
										ss[1] = dataBuffer[symbol + 1];							
									} else {
										ss[0] = dataBuffer[symbol]; 
										ss[1] = dataBuffer[symbol + 1];
										ss[2] = dataBuffer[symbol + 2];
										ss[3] = dataBuffer[symbol + 3];									
									}
									data = ByteArrayToFloat(ss, arrange,rate,offSet);
									status =1;
									System.out.println("采集的数据是："+data);
									components.UpAnalogData(deviceAddr,slave_address, m_address, data, status);
									
									if(word_count==1){
										symbol = symbol + 2;
										int number = Integer.parseInt(m_address, 16)+1;
										m_address = Integer.toHexString(number).toUpperCase();
										if(m_address.length() == 1){
											m_address = "000".concat(m_address).toUpperCase();
										}else if(m_address.length() == 2){
											m_address = "00".concat(m_address).toUpperCase();
										}else if(m_address.length() == 3){
											m_address = "0".concat(m_address).toUpperCase();
										}
									
									}else if(word_count==2){
										symbol = symbol + 4;
										int number = Integer.parseInt(m_address, 16)+2;
										m_address = Integer.toHexString(number).toUpperCase();
										if(m_address.length() == 1){
											m_address = "000".concat(m_address).toUpperCase();
										}else if(m_address.length() == 2){
											m_address = "00".concat(m_address).toUpperCase();
										}else if(m_address.length() == 3){
											m_address = "0".concat(m_address).toUpperCase();
										}
									}
									
//									if(word_count==1){
//										symbol = symbol + 2;
//										int number = Integer.parseInt(m_address.substring(mark.length()), 10)+1;
//										m_address = m_address.substring(0, mark.length()).concat(Integer.toString(number, 10));
//									
//									}else if(word_count==2){
//										symbol = symbol + 4;
//										int number = Integer.parseInt(m_address.substring(mark.length()), 10)+2;
//										m_address = m_address.substring(0, mark.length()).concat(Integer.toString(number, 10));
//
//									}

									if (symbol >= dataBuffer.length)
										notEnd = false;
									else
										notEnd = true;
									System.out.println(symbol+""+m_address);
									System.out.println();
								} else {
									
									System.out.println(symbol+" "+m_address);
									System.out.println("**********没查到记录,地址是："+m_address);
									symbol = symbol + 2;
									int number = Integer.parseInt(m_address, 16)+1;
									m_address = Integer.toHexString(number).toUpperCase();
									if(m_address.length() == 1){
										m_address = "000".concat(m_address).toUpperCase();
									}else if(m_address.length() == 2){
										m_address = "00".concat(m_address).toUpperCase();
									}else if(m_address.length() == 3){
										m_address = "0".concat(m_address).toUpperCase();
									}
									
//									int number = Integer.parseInt(m_address.substring(mark.length()), 10)+1;
//									m_address = m_address.substring(0, mark.length()).concat(Integer.toString(number, 10));
									
									if (symbol >= dataBuffer.length)
										notEnd = false;
									else
										notEnd = true;
									System.out.println(symbol+" "+m_address);
									System.out.println();
								}
							}
							conn.close();
							
							if (m_send_analog_count == 100) {
								if (m_sensor_end == m_sensor_count - 1) {
									sendOver = true;
									m_sensor_start = 0;
									m_sensor_end = 0;
								} else {
									sendOver = false;
									for (int i = m_sensor_start + 1; i < m_sensor_end;) {
										int number = Integer.parseInt(m_sensor_address.get(i), 16);
										int start = Integer.parseInt(m_sensor_address.get(m_sensor_start),16);
										if (number - start > 100) {
											m_sensor_start = i;
											break;
										} else
											i++;
									}
								}
							} else {
								if (m_sensor_end == m_sensor_count - 1) {
									sendOver = true;
									m_sensor_start = 0;
									m_sensor_end = 0;
								} else {
									sendOver = false;
									m_sensor_start = m_sensor_end + 1;
								}
							}
						}
						if (sendOver) {
							if (m_pollTable <= databaseTable.length - 1
									&& m_pollPlc < m_plcId.length - 1) {// 不在循环边缘，内层加1即可
								m_pollPlc++;
								flag = true;
							} else if (m_pollTable < databaseTable.length - 1
									&& m_pollPlc == m_plcId.length - 1) {// 内层在边缘，外层不在，内层置0，外层加1即可
								m_pollTable++;
								m_pollPlc = 0;
								flag = true;
							} else if (m_pollTable == databaseTable.length - 1
									&& m_pollPlc == m_plcId.length - 1) {// 内外层都在边缘，则都置0
								m_pollTable = 0;
								m_pollPlc = 0;
								flag = true;
							}
							m_sensor_start = 0;
							m_sensor_address.clear();
						}
						
						m_shouldLength = 0;
						return 12;               // 12表示接收完整，并且数据校验正确
					} else {
						System.out.print("**********头 尾以及CRC检验有错误,包的内容是:");
						byte[] temp = Arrays.copyOfRange(buffer, 0, buffer.length);				
						for (int i = 0; i < temp.length; i++)
							System.out.print(temp[i] + "  ");
						System.out.println();
						m_shouldLength = 0;
						return 11;             // 11表示头 尾以及CRC检验有错误
					}
				}
			} else {
				System.out.print("**********接收的数据包长度有错误,包的内容是:");
				byte[] temp = Arrays.copyOfRange(buffer, 0, buffer.length);				
				for (int i = 0; i < temp.length; i++)
					System.out.print(temp[i] + "  ");
				System.out.println();
				return 10;                   //10表示接收的数据包长度有错误
			}
		} else {
			System.out.println("**********没有接收到数据包");
			m_count++;
			if(m_count>15){
				if(m_sendType == m_ReadOutputRegister || m_sendType == m_ReadInputStatus ){
					int status = 0;
					for(int i=0;i<m_send_digital_count;i++){
						components.UpdateStatus(deviceAddr,plcId, m_address,status);
						
						int number = Integer.parseInt(m_address, 16) + 1;
						m_address = Integer.toHexString(number).toUpperCase();
						if(m_address.length() == 1){
							m_address = "000".concat(m_address).toUpperCase();
						}else if(m_address.length() == 2){
							m_address = "00".concat(m_address).toUpperCase();
						}else if(m_address.length() == 3){
							m_address = "0".concat(m_address).toUpperCase();
						}
						
						
//						String str = m_address.substring(0, 1);
//						if (str.equalsIgnoreCase("X")|| str.equalsIgnoreCase("Y")) {
//							int number = Integer.parseInt(m_address.substring(1), 8) + 1;
//							m_address = str.concat(Integer.toOctalString(number));
//						} else {
//							int number = Integer.parseInt(m_address.substring(1), 10) + 1;
//							m_address = str.concat(Integer.toString(number, 10));
//						}
					}
				}else if(m_sendType == m_ReadOutputRegister){
					int status = 0;
					for(int i=0;i<m_send_analog_count;i++){
						components.UpdateStatus(deviceAddr,plcId, m_address,status);
//						int number = Integer.parseInt(m_address.substring(1), 10)+1;
//						m_address = m_address.substring(0, 1).concat(Integer.toString(number, 10));
						int number = Integer.parseInt(m_address, 16) + 1;
						m_address = Integer.toHexString(number).toUpperCase();
						if(m_address.length() == 1){
							m_address = "000".concat(m_address).toUpperCase();
						}else if(m_address.length() == 2){
							m_address = "00".concat(m_address).toUpperCase();
						}else if(m_address.length() == 3){
							m_address = "0".concat(m_address).toUpperCase();
						}
					}
				}
			}
			m_shouldLength = 0;
			return 0;
		}
	}
	
	
	
	public ArrayList<String> GetAnalogAddress(ArrayList<String> origin, ArrayList<Integer> count) throws IOException, Exception{
		
		ArrayList<String> address = new ArrayList<String>();
		
		for(int i =0;i<origin.size();i++){
			if(count.get(i) == 1)
				address.add(origin.get(i));
			else if(count.get(i) == 2){
				address.add(origin.get(i));
				address.add(ConvertAddress(origin.get(i)));
				
//				int num = Integer.parseInt(pre.substring(mark.length()), 10)+1;
//				String next =pre.substring(0, mark.length())+Integer.toString(num, 10);
//				address.add(ConvertAddress(next));
			}
		}
		
		return address;
	}
	
	
	
	public ArrayList<String> GetDigitalAddress(ArrayList<String> origin) throws IOException, Exception{
		
		ArrayList<String> address = new ArrayList<String>();
			
		for(int i =0;i<origin.size();i++){
//			address.add(ConvertAddress(origin.get(i)));		
			address.add(origin.get(i));
		}
		return address;
	}
	
	
	public String ConvertAddress(String origin){
		
		String address = null;
		int num = Integer.parseInt(origin, 16) + 1;
//		int num = Integer.parseInt(beginAddr, 16)+Integer.parseInt(origin.substring(mark.length()), 10);
		address = Integer.toHexString(num).toUpperCase();
		if(address.length() == 1){
			address = "000".concat(address).toUpperCase();
		}else if(address.length() == 2){
			address = "00".concat(address).toUpperCase();
		}else if(address.length() == 3){
			address = "0".concat(address).toUpperCase();
		}
		return address;
		
	}
	
//	public String ReCovertAddress(String str){
//		String address = null;
//		int num = Integer.parseInt(str, 16) - Integer.parseInt(beginAddr, 16);
//		address = mark.concat(Integer.toString(num, 10)).toUpperCase();
//		return address;
//	}
	
	

	public String ByteArrayToString(byte[] address) {                   //将字节数据表示的地址如[16,0],转换为String类型的1600
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
	

	
	public float ByteArrayToFloat(byte[] data, int sort,float rate ,float offSet) {            // 将字节数组表示的符点型,转换为真实的符点型数据	
		
		float tempValue = 0;
		float value = 0;
		if (data.length == 2) {              //1个字(即2个字节)表示的是整型值,需要进行缩放和偏移量的计算
			int tempData=0;
			int[] ints = new int[2];
			if (sort == 12) {
				ints[0] = data[0];
				ints[1] = data[1];
			} else if (sort == 21) {
				ints[0] = data[1];
				ints[1] = data[0];
			}
			tempData=ints[0]<<8+ints[1];
			tempValue=tempData*rate+offSet;
		} else if (data.length == 4){        //2个字(即4个字节)表示的是符点型,符合IEEE754标准,也需要进行缩放和偏移量计算
			int q,b,s,g,temp;
			q=sort/1000;
			temp=sort%1000;
			b=temp/100;
			temp=temp%100;
			s=temp/10;
			g=temp%10;
			byte[] buffer = new byte[4];
			buffer[q-1]=data[0];
			buffer[b-1]=data[1];
			buffer[s-1]=data[2];
			buffer[g-1]=data[3];
			tempValue=IEEEToFloat(buffer);
			tempValue=tempValue*rate+offSet;
//			if (tempValue < 1.0E-5) {
//				tempValue = 0.0f;
//			}
			BigDecimal bd = new BigDecimal(tempValue);
			value = bd.setScale(4, BigDecimal.ROUND_HALF_UP).floatValue();
		}
		return value;
		}
	
	
	
	public float IEEEToFloat(byte[] buffer){
		String str = ByteArrayToHexString(buffer).toUpperCase();
		double d = result(str);
		return (float) d;
	}
	
	
	
	public static String ByteArrayToHexString(byte[] src) {
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
	
	
	
	public double result(String z) {
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
	
	
	public static String HexStringToBinaryString(String hexString) {
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
	

	
	public boolean CheckCrc16(byte[] CRC_buff, byte[] CRC_check) { // 进行CRC-16校验，首先需要生成CRC-16,然后进行比较
		byte[] crc = FormCrc(CRC_buff); 
		if (crc[0] == CRC_check[0] && crc[1] == CRC_check[1])
			return true;
		else
			return false;
	}

	
	public byte DecimalStringToByte(String str) { // 将plc的类型值由存储的String值，转化为字节型
		byte b;
		int temp = Integer.parseInt(str, 10);
		b = (byte) temp;
		return b;
	}
	

	public byte HexStringToByte(String str) { // 数据库存放地址是用varchar形式，并且是16进制的数据,故因此需要转化成字节形式
		byte b;
		int temp = Integer.parseInt(str, 16);
		b = (byte) temp;
		return b;
	}

	
	public byte[] Calculate(int n) { // 将int类型转化为byte类型，主要应用于发送时，计算要取得传感器（地址）一定数量的数据
		byte[] b = new byte[4];
		for (int i = 0; i < 4; i++) {
			b[i] = (byte) (n >> (24 - i * 8));
		}
		return b;
	}

	
	public byte[] FormCrc(byte[] CRC_buf) { // 生成CRC-16校验码，返回的是整型形式的数据
		System.out.println("**********生成CRC-16校验码");
		byte[] CRC = new byte[2];
		int len = CRC_buf.length;
		int CRC_ReturnValue = 0xffff;
		int i = 0, j, temp;
		while (len-- != 0) {
			temp = CRC_buf[i++] & 0xff;
			CRC_ReturnValue ^= temp;
			j = 8;
			while (j != 0) {
				if ((CRC_ReturnValue & 0x01) != 0) {
					CRC_ReturnValue = (CRC_ReturnValue >> 1) ^ 0xA001;
				} else {
					CRC_ReturnValue = CRC_ReturnValue >> 1;
				}
				j--;
			}
		}
		CRC[0] = (byte) (CRC_ReturnValue & 0xff);
		CRC[1] = (byte) ((CRC_ReturnValue & 0xff00) >> 8);
		return CRC;
	}

	
	public void QueryPLC(int m_recordNo) throws Exception {// 查询与此主机相关的plc机编号，数据库中存放的是varchar类型，因此需要转化为byte类型
		String temp;
		DataBaseComposite dc = new DataBaseComposite();
		Connection conn = dc.getmysql();
		Statement stmt = conn.createStatement();
		String querySql = "select * from deviceinfo where record_number='"
				+ m_recordNo + "'";
		ResultSet rs = stmt.executeQuery(querySql);
		if (rs.next()) {
			temp = rs.getString("plc_id"); // 查询与本机相关的plc机编号
			deviceAddr = rs.getString("device_id"); // 查询与本机相关的设备号
		} else
			return;
		conn.close();
		m_plcId = temp.split(","); // 将plcId分割成单个字符串数据存放在字符串数组中
	}
}
