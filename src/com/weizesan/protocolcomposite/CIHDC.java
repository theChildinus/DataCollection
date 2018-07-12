package com.weizesan.protocolcomposite;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;

import com.weizesan.connection.databasecomposite.DataBaseComposite;
import com.weizesan.protocolcomposite.cmodbus.DVP_MODBUS_ASC;
import com.weizesan.protocolcomposite.cmodbus.DVP_MODBUS_RTU;
import com.weizesan.protocolcomposite.cmodbus.SIE_MODBUS_ASC;
import com.weizesan.protocolcomposite.cmodbus.SIE_MODBUS_RTU;
import com.weizesan.protocolcomposite.cmodbus.CSlaveProtocol;

public class CIHDC extends CDeviceProtocol {

	private Socket i_socket;// 定义socket

	private int i_recordNumber; // 保存查询记录的编号
	private int i_dataprotocoltype; // 数据层协议类型,如果是MODBUS，则其值为1等等

	private byte i_downBuffer[]; // 下发包，与i_sendBuffer不同的是，这个是只包装了MODBUS协议的包
	private byte i_sendBuffer[]; // 将要发送的数据包，已经包装了MODBUS和IHDC协议的包
	private byte i_deviceNumber[]; // 保存IHDC设备号

	private byte login = (byte) 0x03; // 登录包标识
	private byte loginRe = (byte) 0x83; // 登录响应包标识
	private byte logout = (byte) 0x82;// 下线包标识
	private byte logoutRe = (byte) 0x02;// 下线响应包标识
	private byte heart = (byte) 0x01;// 心跳包标识
	private byte heartRe = (byte) 0x81;// 心跳响应包标识
	private byte updata = (byte) 0x09;// 数据上报包标识
	private byte updataRe = (byte) 0x85;// 数据上报响应包标识（如果是UDP上报，则需要响应包，TCP方式不需要响应包）
	private byte downdata = (byte) 0x89;// 数据下发包标识
	private byte header = (byte) 0x7B;// 这里表示7B

	private byte i_responseType; // 这个表示响应类型，如登录响应、心跳响应

	private String i_connectionProtocol; // 连接类型，是TCP或UDP，在数据上报的响应时需要根据此条件判断
	
	private String dvpRtu = "DVP_MODBUS_RTU";
	private String dvpAsc = "DVP_MODBUS_ASC";
	private String sieRtu = "SIE_MODBUS_RTU";
	private String sieAsc = "SIE_MODBUS_ASC";
	
	private boolean canSend;

	CSlaveProtocol pl; // 建立一个数据层协议的实例，如果能识别是什么协议，则具体对其子类实例化

	public CIHDC(int recordoNo, Socket socket) throws Exception {
		// 构造函数，传入查询记录号和socket,查询其使用的数据层协议，建立数据层协议的实例，方便处理数据包
		i_recordNumber = recordoNo;
		i_socket = socket;
		try {
			QueryDataProtocol(); // 查询数据层协议,记录相关信息
		} catch (Exception e) {
			e.printStackTrace();
		}
		switch (i_dataprotocoltype) {// 判断数据层协议用的是什么
		case 1:
			pl = new DVP_MODBUS_RTU(i_recordNumber); 
			break;
		case 2:
			pl = new DVP_MODBUS_ASC(i_recordNumber); 
			break;
		case 3:
			pl = new SIE_MODBUS_RTU(i_recordNumber); 
			break;
		case 4:
			pl = new SIE_MODBUS_ASC(i_recordNumber); 
		default:
			break;
		}
	}

	public void ReceiveProcess(byte[] i_tempBuffer, int i_tempLength)
			throws Exception {
		// 接收处理
		// 首先需要判断包的类型，如果是01，则为心跳包，03是登录包，82是下线包，这些包都需要进行响应，分别是81、83、02
		// 另外，09是上报包，89是下发包，如果是UDP上报包，则需要85对09进行响应，
		System.out.println("**********IHDC处理");
		if (i_tempBuffer[0] == header && i_tempBuffer[1] == login) {// 登录包，则做登录响应
			i_responseType = loginRe;
			canSend = true;
			System.out.println("**********登录包");

		} else if (i_tempBuffer[0] == header && i_tempBuffer[1] == heart) {// 心跳包，做心跳响应
			i_responseType = heartRe;
			canSend = true;
			System.out.println("**********心跳包");

		} else if (i_tempBuffer[0] == header && i_tempBuffer[1] == logout) { // 下线包，做下线响应
			i_responseType = logoutRe;
			canSend = true;
			System.out.println("**********下线包");

		} else if (i_tempBuffer[0] == header && i_tempBuffer[1] == updata) { // 上报包，做上报处理，根据情况决定是否响应
			System.out.println("**********是上报包");
			// 针对TCP上报和UDP上报包，应该有不同的处理过程,如果是UDP包，将字节数组后面的数据都传递过去
			if (i_connectionProtocol.equals("UDP")) {
				byte[] buffer = Arrays.copyOfRange(i_tempBuffer, 16,
						i_tempLength);// 把数据层协议的字节数组复制给其它数组
				if (pl != null){
					pl.ReceiveProcess(buffer);// 调用MODBUS协议来处理
					canSend = true;
				}else
					return;				
				
			} else if (i_connectionProtocol.equals("TCP")) {// 如果是TCP包，则需要将其中的数据传递过去，如果结尾是结尾符，则传递其中的，如果不是，则连带结尾都传递过去
				if (i_tempBuffer[i_tempLength - 1] == header) {
					byte[] buffer = Arrays.copyOfRange(i_tempBuffer, 15,
							i_tempLength - 1);
					if (pl != null)
						pl.ReceiveProcess(buffer);

				} else {
					byte[] buffer = Arrays.copyOfRange(i_tempBuffer, 15,
							i_tempLength);
					if (pl != null)
						pl.ReceiveProcess(buffer);// 调用MODBUS协议处理
				}
			}
		}

	}

	public void SendProcess() {
		// 发送处理,如果接收到数据包，则进行相关处理，如果是登录或心跳等包，则进行响应，如果是上报数据包，则剥离其设备层外壳，将使用数据层协议的方法来解析

		if (i_responseType == updataRe && i_connectionProtocol.equals("UDP")) // 如果是上报数据包,则需要判断是TCP方式还是UDP方式,TCP方式不需要响应,UDP需要响应
			FormPackage(); // 如果是UDP,则需要进行发送响应包，而且数据体在头部后面
		else if (i_responseType == loginRe || i_responseType == heartRe
				|| i_responseType == logoutRe) {
			// 如果是登录响应、心跳响应、下线响应，则可以直接打包发送
			FormPackage();
		} else {
			if (pl == null)
				return;
			else
				try {
					i_downBuffer = pl.SendProcess();
				} catch (Exception e) {
					e.printStackTrace();
				}
			if (i_downBuffer!=null && i_downBuffer.length>1){
				i_responseType = downdata;
				i_sendBuffer = FormPackage();
			}else 
				return;
		}
		if (i_sendBuffer!=null&&i_sendBuffer.length>1)
			Write(i_sendBuffer);
	}

	public void Write(byte[] buffer) { // 发送请求给客户端，并且初始化相关变量

		OutputStream output = null;
		try {
			output = i_socket.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			output.write(buffer);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("**********发送完成");
		System.out.print("**********发送包的内容如下:");
		for (int i = 0; i < buffer.length; i++)
			System.out.print(buffer[i] + " ");
		System.out.println();
		i_responseType = (byte) 0x00;// 响应类型初始化

	}

	public byte[] FormPackage()// 将设备层的数据打包
	{
		if (i_responseType == loginRe) { // 登录包
			byte[] buffer = new byte[16];
			buffer[0] = header;
			buffer[1] = loginRe;
			buffer[2] = 0;
			buffer[3] = 16;
			for (int i = 4, j = 0; i < 15; i++, j++) {
				buffer[i] = i_deviceNumber[j];
			}
			buffer[15] = header;
			return buffer;

		} else if (i_responseType == logoutRe) { // 下线包
			byte[] buffer = new byte[16];
			buffer[0] = header;
			buffer[1] = logoutRe;
			buffer[2] = 0;
			buffer[3] = 16;
			for (int i = 4, j = 0; i < 15; i++, j++) {
				buffer[i] = i_deviceNumber[j];
			}
			buffer[15] = header;
			return buffer;

		} else if (i_responseType == heartRe) { // 心跳包
			byte[] buffer = new byte[16];
			buffer[0] = header;
			buffer[1] = heartRe;
			buffer[2] = 0;
			buffer[3] = 16;
			for (int i = 4, j = 0; i < 15; i++, j++) {
				buffer[i] = i_deviceNumber[j];
			}
			buffer[15] = header;
			return buffer;

		} else if (i_responseType == updataRe) { // 上传包
			if (i_connectionProtocol.equals("UDP")) {
				byte[] buffer = new byte[16];
				buffer[0] = header;
				buffer[1] = heartRe;
				buffer[2] = 0;
				buffer[3] = 16;
				for (int i = 4, j = 0; i < 15; i++, j++) {
					buffer[i] = i_deviceNumber[j];
				}
				buffer[15] = header;
				return buffer;
			} else
				return null;

		} else if (i_responseType == downdata) { // 下发包
			if (i_connectionProtocol.equals("TCP")) { // 如果是TCP的下发包,则如何打包
				int length = 16 + i_downBuffer.length;
				byte[] buffer = new byte[length];
				buffer[0] = header;
				buffer[1] = downdata;
				buffer[2] = 0;
				buffer[3] = 16;
				for (int i = 4, j = 0; i < 15; i++, j++) {
					buffer[i] = i_deviceNumber[j];
				}
				for (int i = 15, j = 0; i < length - 1; i++, j++) {
					buffer[i] = i_downBuffer[j];
				}
				buffer[length - 1] = header;
				return buffer;

			} else if (i_connectionProtocol.equals("UDP")) { // 如果是UDP的下发包,则如何打包
				int length = 16 + i_downBuffer.length;
				byte[] buffer = new byte[length];
				buffer[0] = header;
				buffer[1] = downdata;
				buffer[2] = 0;
				buffer[3] = 16;
				for (int i = 4, j = 0; i < 15; i++, j++) {
					buffer[i] = i_deviceNumber[j];
				}
				buffer[15] = header;
				for (int i = 16, j = 0; i < length; i++, j++)
					buffer[i] = i_downBuffer[j];
				return buffer;
			} else
				return null;
		} else
			return null;
	}

	public void MainProcess(byte[] buffer, int length) throws Exception {
		System.out.println("**********进入H700的MainProcess处理");
		if (length > 1) { // 收到数据包,做相关处理
			System.out.print("**********数据包中有数据,内容如下:");
			byte[] temp = Arrays.copyOfRange(buffer, 0, length);
			for (int i = 0; i < length; i++)
				System.out.print(temp[i] + "  ");
			System.out.println();
			i_deviceNumber = GetDeviceNumber(temp);
			ReceiveProcess(temp, length);
			if (canSend) {
				SendProcess();
			}
		} else { // 没有收到数据包,则下发请求数据包
			i_responseType = downdata;
			SendProcess();
		}
		return;
	}

	public void QueryDataProtocol() throws Exception {// 查询数据层协议,建立相关协议的实例
		DataBaseComposite dc = new DataBaseComposite();
		Connection conn = dc.getmysql();
		Statement stmt = conn.createStatement();
		String queryProtocol = "select * from deviceinfo where record_number='"+ i_recordNumber + "'";
		ResultSet rs = stmt.executeQuery(queryProtocol);
		if (rs.next()) {
			i_connectionProtocol = rs.getString("connectionprotocol");

			if (rs.getString("data_protocol").equals(dvpRtu))
				i_dataprotocoltype = 1;
			else if (rs.getString("data_protocol").equals(dvpAsc))
				i_dataprotocoltype = 2;
			else if (rs.getString("data_protocol").equals(sieRtu))
				i_dataprotocoltype = 3;
			else if (rs.getString("data_protocol").equals(sieAsc))
				i_dataprotocoltype = 4;
			
		}
	}

	public byte[] GetDeviceNumber(byte[] i_tempBuffer) {
		byte[] number = Arrays.copyOfRange(i_tempBuffer, 4, 15);
		return number;
	}
}
