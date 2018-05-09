package com.tianyue.opcclient;

import java.awt.GridLayout;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UnsupportedLookAndFeelException;

import com.factory.MsgReceiverFactory;
import com.factory.ServicemixConfFactory;
import com.zuowenfeng.AgentComposite.util.DataAnalyzeComponent;
import com.zuowenfeng.AgentComposite.util.DataAnalyzeComponentImpl;
import com.zuowenfeng.AgentComposite.util.DataBaseComponentImpl;

/**
 * 接口再包装后的顶层接口
 * @author tian
 * 
 */
public class MainControlPanel {
	public ArrayList<OpcInit> opcInitList;
	public HashMap<Integer, OpcInit> opcInitMap;
	public ArrayList<OpcClientAPI> opcClientAPIList;
	public HashMap<Integer, OpcClientAPI> opcClientAPIMap;
	public int basicAddress; //重要：basicAddress是本程序下设备地址（对应OPC服务器）的起始地址，考虑可能有连接多个OPC服务器的情况，
							//程序将在basicAddress基础上加上connectionId的值作为每个服务器的设备地址，此处设备地址要和数据表中的DEVICE_ID对应
							//有关connectionId的详细说明请查看文档
	public String tableName;// Test!H2数据库表名，测试用
	public String downAddress;//下发数据的设备地址（对应OPC服务器）
	public String downPlcAddress;//下发数据的PLC地址（对应OPC变量）
	public int downDataType;//下发数据的数据类型
	public float downValue;//下发数据的值
	public boolean needDownload;//标记是否有新的下发命令，程序会定期检查该标记以决定是否下发数据，当修改以上几个下发参数后应将needDownload标记为true
	private SimpleDateFormat format = new SimpleDateFormat(
			"yyyy-MM-dd kk:mm:ss.S");
	private SimpleDateFormat format2 = new SimpleDateFormat(
			"yyyy-MM-dd kk:mm:ss");
	private Date date;
	
	private DataAnalyzeComponent components = new DataAnalyzeComponentImpl();
	

	public JFrame jf;
	public JFrame cf;
	private JTextArea ja;
	
	private String configFileAddr;
	private String basicAddr;
	private int submitFrequency;
	
	// public int connectionId;// Device Id
	/**
	 * 构造控制台 初始化
	 * 控制台构造函数需要设置的内容：
	 * 1.建立存放opcInit对象的opcInitList表
	 * 2.建立存放opcClientAPI对象的opcClientAPIList表
	 * 3.下发请求标记为false
	 * @throws IOException 
	 */
	public MainControlPanel() throws IOException {
		opcInitList = new ArrayList<OpcInit>();
		opcClientAPIList = new ArrayList<OpcClientAPI>();
		needDownload = false;
		components.setDataBaseComponent(new DataBaseComponentImpl());
		
//		cf = new JFrame("OPC基本设置");
//		cf.setBounds(500, 200, 300, 300);
//		cf.setLayout(new GridLayout(2,2));
//		JLabel label_basic_addr = new JLabel("子站OPC地址");
//		cf.add(label_basic_addr);
//		cf.setVisible(true);
//		JTextField text_basic_addr = new JTextField();
//		cf.add(text_basic_addr);
//		JButton button_ok = new JButton("确定");
//		
		jf = new JFrame("OPC Monitor");
		jf.setBounds(200, 100, 350, 600);
		ja = new JTextArea();
		jf.add(new JScrollPane(ja));
		ja.setCaretPosition(ja.getText().length());
		ja.setEditable(false);
		ja.setText(ja.getText()+"OPC-Client is running\n");
		jf.setVisible(true);
		
	}

	/**
	 * 将变量名表添加到服务器
	 * 
	 * @param opc
	 * OpcClientAPI实例
	 * @param connectionId
	 * connectionId区分多个OPC连接
	 * @return 正常则返回0
	 */
	public int setupItems(OpcClientAPI opc, int connectionId) {
		/*
		 * try { statement.executeUpdate("delete from "+tableName); } catch
		 * (SQLException e1) { // TODO Auto-generated catch block
		 * e1.printStackTrace(); }
		 */
		Iterator<OpcItem> it = opcInitList.get(connectionId).itemList.iterator();
		while (it.hasNext()) {
			OpcItem oi = (OpcItem) it.next();
			opc.addItem(connectionId, oi.name, oi.valueType, true);
		}
		return 0;
	}

	/**
	 * 刷新表中所有变量的值 并调用copyToLocal方法存储到本地 存储于本地的值用getLocal的一系列方法即可访问
	 * 
	 * @param connectionId
	 *            connectionId区分多个OPC连接
	 * @return 成功返回0，失败返回-1
	 */
	public int refreshList(int connectionId) {
		String firstItemName = opcInitList.get(connectionId).itemList.get(0).name;
		if (firstItemName == null)      //找不到itemList中第一个OPC变量的名字 说明itemList当前为空，无需刷新，直接返回
			return 0;
		if (opcClientAPIList.get(connectionId).refreshAll(connectionId,
				firstItemName) == -1)	//调用底层函数刷新变量遇到错误，返回-1
			return -1;					
		return copyToLocal(connectionId);	//在无错误的情况下，调用copyToLocal函数将新获得的变量数据从底层更新到顶层对应的OpcItem当中
	}

	/**
	 * 讲OPC底层新获得的变量数据更新到本地对应的OpcItem对象中
	 * 
	 * @param connectionId
	 * connectionId区分多个OPC连接
	 * @return 成功返回0
	 */
	public int copyToLocal(int connectionId) {
		OpcItem oi;
		Iterator<OpcItem> it = opcInitList.get(connectionId).itemList.iterator();
		while (it.hasNext()) {  //根据OpcItem对象的类型调用对应的方法从底层获得数据
			oi = (OpcItem) it.next();
			switch (oi.valueType) { 
			case 4:
				oi.fltVal = opcClientAPIList.get(connectionId).getFloatData(
						connectionId, oi.name);
				break;
			case 5:
				oi.dblVal = opcClientAPIList.get(connectionId).getDoubleData(
						connectionId, oi.name);
				break;
			case 22:
				oi.intVal = opcClientAPIList.get(connectionId).getIntData(
						connectionId, oi.name);
				break;
			case 11:
				System.out.println(oi.name+">>>"+opcClientAPIList.get(connectionId).getBoolData(
						connectionId, oi.name));
				oi.boolVal = opcClientAPIList.get(connectionId).getBoolData(
						connectionId, oi.name);
				break;
			}

		}
		return 0;
	}

	/**
	 * 获取本地存储中某个单精度浮点(float)数据类型元素(Item)的值
	 * 
	 * @param connectionId
	 *            connectionId区分多个OPC连接
	 * @param itemName
	 *            访问的元素(item)名称
	 * @return 元素的单精度浮点数值
	 */
	public float getLocalFloat(int connectionId, String itemName) {
		OpcInit init = opcInitList.get(connectionId);
		int index = init.getItemIndexByName(itemName);
		if (index == -1)
			return -1;
		return init.itemList.get(index).fltVal;
	}

	/**
	 * 获取本地存储中某个双精度浮点(double)数据类型元素(Item)的值
	 * 
	 * @param connectionId
	 *            connectionId区分多个OPC连接
	 * @param itemName
	 *            访问的元素(item)名称
	 * @return 元素的双精度浮点数值
	 */
	public double getLocalDouble(int connectionId, String itemName) {
		OpcInit init = opcInitList.get(connectionId);
		int index = init.getItemIndexByName(itemName);
		if (index == -1)
			return -1;
		return init.itemList.get(index).dblVal;
	}

	/**
	 * 获取本地存储中某个整数(int)数据类型元素(Item)的值
	 * 
	 * @param connectionId
	 *            connectionId区分多个OPC连接
	 * @param itemName
	 *            访问的元素(item)名称
	 * @return 元素的整型数值
	 */
	public int getLocalInt(int connectionId, String itemName) {
		OpcInit init = opcInitList.get(connectionId);
		int index = init.getItemIndexByName(itemName);
		if (index == -1)
			return -1;
		return init.itemList.get(index).intVal;
	}

	/**
	 * 获取本地存储中某个布尔型(boolean)数据类型元素(Item)的值
	 * 
	 * @param connectionId
	 *            connectionId区分多个OPC连接
	 * @param itemName
	 *            访问的元素(item)名称
	 * @return 元素的布尔型值true/false
	 */
	public boolean getLocalBool(int connectionId, String itemName) {
		OpcInit init = opcInitList.get(connectionId);
		int index = init.getItemIndexByName(itemName);
		if (index == -1)
			return false;
		return init.itemList.get(index).boolVal;
	}
	
	/**
	 * 获取本地存储中某个字符串(string)数据类型元素(Item)的值
	 * 
	 * @param connectionId
	 *            connectionId区分多个OPC连接
	 * @param itemName
	 *            访问的元素(item)名称
	 * @return 元素的字符串数据
	 */
	public String getLocalString(int connectionId, String itemName) {
		OpcInit init = opcInitList.get(connectionId);
		int index = init.getItemIndexByName(itemName);
		if (index == -1)
			return "Failed";
		return init.itemList.get(index).strVal;
	}


	/**
	 * 通过JNI从底层获取某个单精度浮点(float)数据类型元素(Item)的值
	 * 
	 * @param connectionId
	 *            connectionId区分多个OPC连接
	 * @param itemName
	 *            访问的元素(item)名称
	 * @return 元素的单精度浮点值
	 */
	public float getFloatData(int connectionId, String itemName) {
		return opcClientAPIList.get(connectionId).getFloatData(connectionId,
				itemName);
	}

	/**
	 * 通过JNI从底层获取某个双精度浮点(double)数据类型元素(Item)的值
	 * 
	 * @param connectionId
	 *            connectionId区分多个OPC连接
	 * @param itemName
	 *            访问的元素(item)名称
	 * @return 元素的双精度浮点值
	 */
	public double getDoubleData(int connectionId, String itemName) {
		return opcClientAPIList.get(connectionId).getDoubleData(connectionId,
				itemName);
	}

	/**
	 * 通过JNI从底层获取某个整数(int)数据类型元素(Item)的值
	 * 
	 * @param connectionId
	 *            connectionId区分多个OPC连接
	 * @param itemName
	 *            访问的元素(item)名称
	 * @return 元素的整型值
	 */
	public int getIntData(int connectionId, String itemName) {
		return opcClientAPIList.get(connectionId).getIntData(connectionId,
				itemName);
	}

	/**
	 * 通过JNI从底层获取某个布尔型(boolean)数据类型元素(Item)的值
	 * 
	 * @param connectionId
	 *            connectionId区分多个OPC连接
	 * @param itemName
	 *            访问的元素(item)名称
	 * @return 元素的布尔型值true/false
	 */
	public boolean getBoolData(int connectionId, String itemName) {
		return opcClientAPIList.get(connectionId).getBoolData(connectionId,
				itemName);
	}
	
	/**
	 * 通过JNI从底层获取某个字符串(string)数据类型元素(Item)的值
	 * 
	 * @param connectionId
	 *            connectionId区分多个OPC连接
	 * @param itemName
	 *            访问的元素(item)名称
	 * @return 元素的字符串数据
	 */
	public String getStringData(int connectionId, String itemName) {
		return opcClientAPIList.get(connectionId).getStringData(connectionId,
				itemName);
	}
	
	/**
	 * 以一定的时间间隔自动刷新数据并上传
	 * @param connectionId
	 * 区分多个OPC连接
	 * @param millisecond
	 * 间隔时间（毫秒）
	 * @throws Exception 
	 */
	public void autoSubmit(int connectionId, int millisecond) throws Exception {
		//int testCnt=0;
		while (true) {
			
			ReceiveCommand(); //每个循环周期先检查是否有下发请求，有则先执行数据下发操作
			OpcInit init = opcInitList.get(connectionId);//根据connectionId获取到当前要处理的服务器对应的OpcItem对象
			refreshList(connectionId);  //刷新整个当前要处理的服务器下的所有OPC变量的数据
			Iterator<OpcItem> it = opcInitList.get(init.connectionId).itemList
					.iterator();
			while (it.hasNext()) {   //迭代访问各OPC变量，输出、上传数据
				System.out.println(getLocalInt(init.connectionId,
						opcInitList.get(init.connectionId).itemList
								.get(0).name));
				OpcItem oi = (OpcItem) it.next();
				//String sql = "update " + tableName + " set ";
				float tmp1;
				int tmp2;
				switch (oi.valueType) {
				case 4:
					tmp1 = getLocalFloat(init.connectionId, oi.name);
				//	sql += "VALUE=" + String.valueOf(tmp1);
					components.UpAnalogOpcData(
							String.valueOf(basicAddr),
							oi.address, "0",tmp1);
					System.out.println(oi.name+">>>"+tmp1);
					addToBoard(oi.name+">>>"+tmp1);
					break;
				case 5:
					tmp1 = (float) getLocalDouble(init.connectionId,
							oi.name);
					//sql += "VALUE=" + String.valueOf(tmp1);
					components.UpAnalogOpcData(
							String.valueOf(basicAddr),
							oi.address, "0", tmp1);
					System.out.println(oi.name+">>>"+tmp1);
					addToBoard(oi.name+">>>"+tmp1);
					break;
				case 22:
					tmp1 = (float) getLocalInt(init.connectionId,
							oi.name);
					//sql += "VALUE=" + String.valueOf(tmp1);
					components.UpAnalogOpcData(
							String.valueOf(basicAddr),
							oi.address,  "0",tmp1);
					System.out.println(oi.name+">>>"+tmp1);
					addToBoard(oi.name+">>>"+tmp1);
					break;
				case 11:
					tmp2 = getLocalBool(init.connectionId, oi.name) == true ? 1
							: 0;
					//sql += "VALUE=" + String.valueOf(tmp2);
//					components.UpDigitalData(
//							String.valueOf(basicAddr),
//							oi.address, "0", tmp2);
					System.out.println(oi.name+">>>"+tmp2);
					addToBoard(oi.name+">>>"+tmp2);
					break;
				}
				
				// sql+=" where PLC_ID='"+oi.address+"'";
				
				/*
				sql += " where DEVICE_ID='"
						+ String.valueOf(basicAddr)
						+ "' and PLC_ID='" + oi.address + "'";
				try {
					statement.executeUpdate(sql);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				*/
			}
			try {
				Thread.sleep(millisecond);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			/*
			testCnt++;
			if (testCnt==5){
				opcClientAPIList.get(0).shutdownServer(0);
			break;}
			*/
		}
	}
//
//	public void publishUpdate( String topic, String content ) throws Exception {
//		UpdateDBInfoConfiguration conf = new UpdateDBInfoConfiguration();
//		conf.getUpdateConfiguration();
//		PublishComponentImpl comp = new PublishComponentImpl();
//		comp.publish("http://" + conf.getUrl() + ":" + conf.getPort() + "/" + conf.getServicename(), "http://" + ServicemixConfFactory.conf.getUrl() + ":" + ServicemixConfFactory.conf.getPort(), topic, content);
//	}
//	
//	public String objectToUpdateMsg( ArrayList<HeatingPoint> heatArray ) {
//		StringBuilder builder = new StringBuilder();
//		builder.append("<coolsql xmlns=\"\" xmlns:ns6=\"http://docs.oasis-open.org/wsn/b-2\">");
//		
//		for ( int i = 0; i <= heatArray.size() - 1; i++ ) {
//			HeatingPoint q = heatArray.get(i);
//			
//			if ( q.getDataType() == 0 ) {
//				GLAnalogMeasure gam = (GLAnalogMeasure)q;
//				builder.append("<Sql>update GL_ANALOG_MEASURE set timestamp = timestamp '" + gam.getTimeStamp() + "', date = date '" + gam.getDate() + "', time = time '" + gam.getTime() + "', value = " + gam.getValue() + ", outbound = " + 
//									gam.getOutbound() + " where device_id = '" + gam.getDeviceID() + "' and plc_id = '" + gam.getPLCID() + "' and sensor_id = '" + 
//									gam.getSensorID() + "';</Sql>");
//			}
//		
//			else if ( q.getDataType() == 2 ) {
//				GLDigitalMeasure gdm = (GLDigitalMeasure)q;
//				builder.append("<Sql>update GL_DIGITAL_MEASURE set timestamp = timestamp '" + gdm.getTimestamp() + "', date = date '" + gdm.getDate() + "', time = time '" + gdm.getTime() + "', value = " + gdm.getValue() + ", open = " + 
//									gdm.getOpen() + ", close = " + gdm.getClose() + ", isbeyond = " + gdm.getIsbeyond() + ", state = " + gdm.getState() +
//									" where device_id = '" + gdm.getDeviceID() + "' and plc_id = '" + gdm.getPLCID() + "' and sensor_id = '" + gdm.getSensorID() 
//									+ "';</Sql>");
//			}
//		
//			else if ( q.getDataType() == 4 ) {
//				RJLAnalogMeasure ram = (RJLAnalogMeasure)q;
//				builder.append("<Sql>update RJL_ANALOG_MEASURE set timestamp = timestamp '" + ram.getTimestamp() + "', date = date '" + ram.getDate() + "', time = time '" + ram.getTime() + "', value = " + ram.getValue() + ", outbound = " + 
//					ram.getOutbound() + " where device_id = '" + ram.getDeviceID() + "' and plc_id = '" + ram.getPLCID() + "' and sensor_id = '" + 
//					ram.getSensorID() + "';</Sql>");
//			}
//		
//			else if ( q.getDataType() == 6 ) {
//				RJLDigitalMeasure rdm = (RJLDigitalMeasure)q;
//				builder.append("<Sql>update RJL_DIGITAL_MEASURE set timestamp = timestamp '" + rdm.getTimestamp() + "', date = date '" + rdm.getDate() + "', time = time '" + rdm.getTime() + "', value = " + rdm.getValue() + ", open = " + 
//					rdm.getOpen() + ", close = " + rdm.getClose() + ", isbeyond = " + rdm.getIsbeyond() + ", state = " + rdm.getState() +
//					" where device_id = '" + rdm.getDeviceID() + "' and plc_id = '" + rdm.getPLCID() + "' and sensor_id = '" + rdm.getSensorID() 
//					+ "';</Sql>");
//			}
//			
//		}
//		
//		heatArray.clear();
//		builder.append("<Level>realtime</Level>");
//		builder.append("</coolsql>");
//		return builder.toString();
//	}
//
//	public String objectToOutbound(String topic, String location, String type,
//			float value, String level) {
//		String content = "<alarm:Alarm xmlns:alarm=\"http://alarms.some-host\">";
//		content = content.concat("<alarm:location>" + location
//				+ "</alarm:location>");
//		content = content.concat("<alarm:type>" + type + "</alarm:type>");
//		content = content.concat("<alarm:value>" + value + "</alarm:value>");
//		content = content.concat("<alarm:level>" + level + "</alarm:level>");
//		content = content.concat("</alarm:Alarm>");
//		return content;
//	}
//
//	public String ObjectToMess(String topic, float value) {
//		String content = "<sensor xmlns=\"\" xmlns:ns6=\"http://docs.oasis-open.org/wsn/b-2\">";
//		content = content.concat("<value>" + value + "</value>");
//		content = content.concat("<mytopic>" + topic + "</mytopic>");
//		content = content.concat("</sensor>");
//		return content;
//	}
//
//	/**
//	 * 上传开关量
//	 * 
//	 * @param address
//	 *            虚拟DEVICE_ID地址 区分服务器
//	 * @param plcAddress
//	 *            虚拟PLC_ID地址 区分变量名
//	 * @param sensorAddr
//	 *            无意义 使用0做参数
//	 * @param value
//	 *            上传的数值
//	 */
//	public void UpDigitalData(String address, String plcAddress,
//			String sensorAddr, int value) {
//		// under construction......
//		DataAnalyzeComponent analyzeComponent = new DataAnalyzeComponentImpl();
//		// String date = new Date().toString();
//		// String result = "解析后：\n" + date + "设备地址:" + address + "\n";
//		// result = result.concat("从机地址:" + "" + plcAddress + "\n");
//		// result = result.concat("传感器地址:" + "" + sensorAddr[1] + sensorAddr[0]
//		// + "\n");
//		// MsgReceiverFactory.receiver.receive(result);
//		DataBaseComponent t = new DataBaseComponentImpl();
//		analyzeComponent.setDataBaseComponent(t);
//		String sensorString = "0";
//
//		if (sensorString.length() == 1) {
//			sensorString = "0" + sensorString;
//		}
//
//		String temp = "0";
//
//		if (temp.length() == 1) {
//			temp = "0" + temp;
//		}
//
//		sensorString = sensorString + temp;
//
//		if (sensorString.length() > 4) {
//			sensorString = sensorString.substring(0, 2)
//					+ sensorString.substring(8, sensorString.length());
//		}
//
//		System.out.println("Up");
//		System.out.println("设备地址是: " + address + "PLC编号: " + plcAddress
//				+ "传感器地址:" + sensorString);
//		try {
//			MsgReceiverFactory.receiver.receiveOpc(address, "" + plcAddress,
//					sensorString, value, format.format(date));
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		} catch (InterruptedException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		//HeatingPoint p = null;
//		ArrayList<HeatingPoint> p = new ArrayList<HeatingPoint>();
//		HeatingPoint q = null;
//		try {
//			q = analyzeComponent.checkDataType(address, "" + plcAddress, ""
//					+ "0" + "0", false);
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		if (q == null) {
//			System.out.println("Message error");
//		}
//
//		else {
//			boolean isRedundant = analyzeComponent.checkRedundant(q,
//					Timestamp.valueOf(format.format(date)));
//
//			if (!isRedundant) {
//				// 资源绑定
//				String instanceMsg = analyzeComponent.resouceInstanceBinding(q);
//				float values = 0f;
//				System.out.println(instanceMsg);
//
//				if (q.getDataType() == 2 || (q.getDataType() == 6)) {
//
//					if (q.getDataType() == 2) {
//						GLDigitalMeasure gdm = (GLDigitalMeasure) q;
//						gdm.setTimestamp(Timestamp.valueOf(format
//								.format(new Date())));
//						gdm.setDate(java.sql.Date.valueOf(format2.format(
//								new Date()).split(" ")[0]));
//						gdm.setTime(java.sql.Time.valueOf(format2.format(
//								new Date()).split(" ")[1]));
//						if (gdm.getValue() == 0 && (value == 1)) {
//							int a = gdm.getOpen() + 1;
//							gdm.setOpen(a);
//						}
//
//						else if (gdm.getValue() == 1 && (value == 0)) {
//							int a = gdm.getClose() + 1;
//							gdm.setClose(a);
//						}
//
//						gdm.setValue(value);
//						values = gdm.getValue();
//					}
//
//					else if (q.getDataType() == 6) {
//						RJLDigitalMeasure rdm = (RJLDigitalMeasure) q;
//						rdm.setTimestamp(Timestamp.valueOf(format
//								.format(new Date())));
//						rdm.setDate(java.sql.Date.valueOf(format2.format(
//								new Date()).split(" ")[0]));
//						rdm.setTime(java.sql.Time.valueOf(format2.format(
//								new Date()).split(" ")[1]));
//
//						if (rdm.getValue() == 0 && (value == 1)) {
//							int a = rdm.getOpen() + 1;
//							rdm.setOpen(a);
//						}
//
//						else if (rdm.getValue() == 1 && (value == 0)) {
//							int a = rdm.getClose() + 1;
//							rdm.setClose(a);
//						}
//
//						rdm.setValue(value);
//						values = rdm.getValue();
//					}
//
//				}
//
//				System.out.println("RealValue:" + values);
//				String publishMsg = ObjectToMess(instanceMsg, values);
//				System.out.println(publishMsg);
//				PublishComponent component = new PublishComponentImpl();
//				// BindingConfiguration conf = new BindingConfiguration();
//				// conf.getBindingConfiguration();
//				// component.publish("http://" + conf.getUrl() + ":" +
//				// conf.getPort() + "/" + conf.getServicename(), "http://" +
//				// ServicemixConfFactory.conf.getUrl() + ":" +
//				// ServicemixConfFactory.conf.getPort(), instanceMsg,
//				// publishMsg);
//				p.add(q);
//				String publishString = objectToUpdateMsg(p);
//				UpdateDBInfoConfiguration conf3 = null;
//				try {
//					conf3 = new UpdateDBInfoConfiguration();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				try {
//					conf3.getUpdateConfiguration();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				String topics = "";
//
//				if (q.getDataType() == 0) {
//					topics = "GL_ANALOG_MEASURE";
//				}
//
//				else if (q.getDataType() == 2) {
//					topics = "GL_DIGITAL_MEASURE";
//				}
//
//				else if (q.getDataType() == 4) {
//					topics = "RJL_ANALOG_MEASURE";
//				}
//
//				else {
//					topics = "RJL_DIGITAL_MEASURE";
//				}
//
//				try {
//					component.publish(
//							"http://" + conf3.getUrl() + ":" + conf3.getPort()
//									+ "/" + conf3.getServicename(), "http://"
//									+ ServicemixConfFactory.conf.getUrl() + ":"
//									+ ServicemixConfFactory.conf.getPort(),
//							topics, publishString);
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				SwitchAlarmConfiguration conf2 = null;
//				try {
//					conf2 = new SwitchAlarmConfiguration();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				try {
//					conf2.getAlarmConnection();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//
//				// 事件生成
//				int outbound = analyzeComponent.checkOutbound(q);
//				System.out.println("outbound:" + outbound);
//				int types = q.getDataType();
//
//				if (types == 2) {
//					GLDigitalMeasure gdm = (GLDigitalMeasure) q;
//					String topic = gdm.getMeasure_type();
//					String location = gdm.getBoiler() + "#" + gdm.getField();
//					float finalValue = (float) gdm.getValue();
//					gdm.setIsbeyond(outbound);
//					gdm.setState(outbound);
//
//					if (outbound == 2) {
//						String msg = objectToOutbound(topic, location, topic,
//								finalValue, "" + 4);
//						try {
//							component.publish(
//									"http://" + conf2.getUrl() + ":"
//											+ conf2.getPort() + "/"
//											+ conf2.getServiceName(),
//									"http://"
//											+ ServicemixConfFactory.conf
//													.getUrl()
//											+ ":"
//											+ ServicemixConfFactory.conf
//													.getPort(), "alarmDigital",
//									msg);
//						} catch (Exception e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//					}
//
//				}
//
//				else if (types == 6) {
//					RJLDigitalMeasure rdm = (RJLDigitalMeasure) q;
//					String topic = rdm.getMeasure_type();
//					String location = rdm.getDescription().split(" ")[4];
//					float finalValue = (float) rdm.getValue();
//					rdm.setIsbeyond(outbound);
//					rdm.setState(outbound);
//
//					if (outbound == 2) {
//						String msg = objectToOutbound(topic, location, topic,
//								finalValue, "" + 4);
//						System.out.println(msg);
//						try {
//							component.publish(
//									"http://" + conf2.getUrl() + ":"
//											+ conf2.getPort() + "/"
//											+ conf2.getServiceName(),
//									"http://"
//											+ ServicemixConfFactory.conf
//													.getUrl()
//											+ ":"
//											+ ServicemixConfFactory.conf
//													.getPort(), "alarmDigital",
//									msg);
//						} catch (Exception e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//					}
//
//				}
//
//			}
//
//		}
//	}
//
//	/**
//	 * 上传模拟量
//	 * 
//	 * @param address
//	 *            虚拟DEVICE_ID地址 区分服务器
//	 * @param plcAddress
//	 *            虚拟PLC_ID地址 区分变量名
//	 * @param sensorAddr
//	 *            无意义 使用0做参数
//	 * @param value
//	 *            上传的数值
//	 */
//	public void UpAnalogData(String address, String plcAddress,
//			String sensorAddr, float value) {
//		// under construction......
//		DataAnalyzeComponent analyzeComponent = new DataAnalyzeComponentImpl();
//		DataBaseComponent t = new DataBaseComponentImpl();
//		analyzeComponent.setDataBaseComponent(t);
//		String sensorString = sensorAddr;
//
////		if (sensorString.length() == 1) {
////			sensorString = "0" + sensorString;
////		}
////
////		String temp = "0";
////
////		if (temp.length() == 1) {
////			temp = "0" + temp;
////		}
////
////		sensorString = sensorString + temp;
////		if (sensorString.length() > 4) {
////			sensorString = sensorString.substring(0, 2)
////					+ sensorString.substring(8, sensorString.length());
////		}
//        //Modified
//		date = new Date();
//		
//		System.out.println("Up");
//		System.out.println("设备地址是: " + address + "PLC编号: " + plcAddress
//				+ "传感器地址:" + sensorString);
//		
//		System.out.println(address);
//		System.out.println("" + plcAddress);
//		System.out.println(sensorString);
//		System.out.println(value);
//		System.out.println(format.format(date));
//		
//		try {
//			MsgReceiverFactory.receiver.receiveOpc(address, "" + plcAddress,
//					sensorString, value, format.format(date));
//		} catch (IOException e2) {
//			// TODO Auto-generated catch block
//			e2.printStackTrace();
//		} catch (InterruptedException e2) {
//			// TODO Auto-generated catch block
//			e2.printStackTrace();
//		}
//
//		ArrayList<HeatingPoint> p = new ArrayList<HeatingPoint>();
//		HeatingPoint q = new GLAnalogMeasure();
//		Timestamp ts = new Timestamp(System.currentTimeMillis());
//		((GLAnalog)q).setTimeStamp(ts);
//		
//		
//		/*
//		try {
//			q = analyzeComponent.checkDataType(address, "" + plcAddress,
//					sensorString, true);
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		*/
//		
//		q.setDataType(0);
//		q.setDeviceID(address);
//		q.setPLCID(plcAddress);
//		q.setSensorID("0");
//		//((GLAnalogMeasure)q).setValue(value);
//		
//
//		if (q == null) {
//			System.out.println("Message error");
//		}
//
//		else {
//			boolean isRedundant = analyzeComponent.checkRedundant(q,
//					Timestamp.valueOf(format.format(date)));
//
//			if (!isRedundant) {
//				// 资源绑定
//				String instanceMsg = analyzeComponent.resouceInstanceBinding(q);
//				float values = 0f;
//				System.out.println(instanceMsg);
//
//				if (q.getDataType() == 0 || (q.getDataType() == 4)) {
//
//					if (q.getDataType() == 0) {
//						GLAnalogMeasure glm = (GLAnalogMeasure) q;
//						System.out.println(format2.format(new Date()));
//						glm.setTimeStamp(Timestamp.valueOf(format
//								.format(new Date())));
//						glm.setDate(java.sql.Date.valueOf(format2.format(
//								new Date()).split(" ")[0]));
//						glm.setTime(java.sql.Time.valueOf(format2.format(
//								new Date()).split(" ")[1]));
//						/**
//						 * 有修改！
//						 */
//						/*
//						glm.setValue(analyzeComponent.offsetCalculate(
//								(float) glm.getFactor(),
//								(float) glm.getOffset(), value, true));
//						*/
//						glm.setValue(value);
//						values = (float) glm.getValue();
//						System.out.println("[[[[[[[[[[[[[["+values);
//					}
//
//					else {
//						RJLAnalogMeasure ram = (RJLAnalogMeasure) q;
//						ram.setTimestamp(Timestamp.valueOf(format
//								.format(new Date())));
//						System.out.println(format2.format(new Date()));
//						ram.setDate(java.sql.Date.valueOf(format2.format(
//								new Date()).split(" ")[0]));
//						ram.setTime(java.sql.Time.valueOf(format2.format(
//								new Date()).split(" ")[1]));
//						ram.setValue(analyzeComponent.offsetCalculate(
//								(float) ram.getFactor(),
//								(float) ram.getOffset(), value, true));
//						values = (float) ram.getValue();
//					}
//
//				}
//
//				System.out.println("RealValue:" + values);
//				// String publishMsg = ObjectToMess(instanceMsg, values);
//				// System.out.println(publishMsg);
//				PublishComponent component = new PublishComponentImpl();
//				// BindingConfiguration conf = new BindingConfiguration();
//				// conf.getBindingConfiguration();
//				p.add((GLAnalogMeasure) q);
//				String publishString = objectToUpdateMsg(p);
//				System.out.println(publishString);
//				UpdateDBInfoConfiguration conf3 = null;
//				try {
//					conf3 = new UpdateDBInfoConfiguration();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				try {
//					conf3.getUpdateConfiguration();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				String topics = "";
//				System.out.println(ServicemixConfFactory.conf.getUrl());
//				System.out.println(ServicemixConfFactory.conf.getPort());
//				/*
//				if (q.getDataType() == 0) {
//					topics = "GL_ANALOG_MEASURE";
//				}
//
//				else if (q.getDataType() == 2) {
//					topics = "GL_DIGITAL_MEASURE";
//				}
//
//				else if (q.getDataType() == 4) {
//					topics = "RJL_ANALOG_MEASURE";
//				}
//
//				else {
//					topics = "RJL_DIGITAL_MEASURE";
//				}
//				*/
//				topics = "tableinfo";
//
//				try {
//					component.publish(
//							"http://" + conf3.getUrl() + ":" + conf3.getPort()
//									+ "/" + conf3.getServicename(), "http://"
//									+ ServicemixConfFactory.conf.getUrl() + ":"
//									+ ServicemixConfFactory.conf.getPort(),
//							topics, publishString);
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				AnalogAlarmConfiguration conf2 = null;
//				try {
//					conf2 = new AnalogAlarmConfiguration();
//				} catch (IOException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}
//				try {
//					conf2.getConnectionString();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				// component.publish("http://" + conf.getUrl() + ":" +
//				// conf.getPort() + "/" + conf.getServicename(), "http://" +
//				// ServicemixConfFactory.conf.getUrl() + ":" +
//				// ServicemixConfFactory.conf.getPort(), instanceMsg,
//				// publishMsg);
//
//				// 事件生成
//				int outbound = analyzeComponent.checkOutbound(q);
//				System.out.println("outbound:" + outbound);
//				int types = q.getDataType();
//
//				if (types == 0) {
//					GLAnalogMeasure gam = (GLAnalogMeasure) q;
//					String topic = gam.getMeasure_type();
//					gam.setOutbound(outbound);
//					String location = gam.getBoiler() + "#" + gam.getField();
//					float finalValue = (float) gam.getValue();
//
//					if (outbound != 0) {
//						String msg = objectToOutbound(topic, location, topic,
//								finalValue, "" + outbound);
//						System.out.println(msg);
//						try {
//							component.publish(
//									"http://" + conf2.getUrl() + ":"
//											+ conf2.getPort() + "/"
//											+ conf2.getServicename(),
//									"http://"
//											+ ServicemixConfFactory.conf
//													.getUrl()
//											+ ":"
//											+ ServicemixConfFactory.conf
//													.getPort(), "alarmAnalog",
//									msg);
//						} catch (Exception e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//					}
//
//				}
//
//				else if (types == 4) {
//					RJLAnalogMeasure ram = (RJLAnalogMeasure) q;
//					String topic = ram.getMeasure_type();
//					System.out.println(ram.getDescription());
//					String location = ram.getDescription().split(" ")[4];
//					float finalValue = (float) ram.getValue();
//					ram.setOutbound(outbound);
//
//					if (outbound != 0) {
//						String msg = objectToOutbound(topic, location, topic,
//								finalValue, "" + outbound);
//						System.out.println(msg);
//						try {
//							component.publish(
//									"http://" + conf2.getUrl() + ":"
//											+ conf2.getPort() + "/"
//											+ conf2.getServicename(),
//									"http://"
//											+ ServicemixConfFactory.conf
//													.getUrl()
//											+ ":"
//											+ ServicemixConfFactory.conf
//													.getPort(), "alarmAnalog",
//									msg);
//						} catch (Exception e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//					}
//
//				}
//
//			}
//
//		}
//	}

	/**
	 * 检查并执行下发命令
	 * 下发命令时请直接修改MainControlPanel实例中的downAddress,downPlcAddress,downDataType
	 * ,downValue 并将needDownload标记为true
	 */
	public void ReceiveCommand() {
		if (needDownload == true) {
			needDownload = false;
			int realId = Integer.valueOf(downAddress);
			String itemName = opcInitList.get(realId).getItemNameByAddress(
					downPlcAddress);
			//根据下发数据的数据类型，选择对应的write方法写数据到服务器
			if (downDataType == 0) {
				opcClientAPIList.get(realId).writeFloatSync(realId, itemName,
						downValue);
			} else if (downDataType == 1) {
				opcClientAPIList.get(realId).writeBoolSync(realId, itemName,
						downValue > 0.5 ? true : false);
			}
		}
	}
	
	public void addToBoard(String str){
		ja.setText(ja.getText()+str+"\n");
		ja.setCaretPosition(ja.getText().length());
	}

	public void oneClickRun(String configFileAddr, String basicAddr, int submitFrequency){
		this.configFileAddr = configFileAddr;
		this.basicAddr = basicAddr;
		this.submitFrequency = submitFrequency;
		OpcInit init = new OpcInit(configFileAddr, 0);
		if (init.readConfig() == 0) {
			opcInitList.add(init);
			OpcClientAPI opc = new OpcClientAPI();
			opcClientAPIList.add(opc);
			if (opc.connect(init.IPAddress, init.serverName, init.groupName) == 0){
				System.out.println("成功连接至OPC服务器"+init.IPAddress+" "+init.serverName+" "+init.groupName);
				addToBoard("成功连接至OPC服务器"+init.IPAddress+" "+init.serverName+" "+init.groupName);
			}
			else{
				System.out.println("连接OPC服务器失败");
				addToBoard("连接OPC服务器失败");
			}
			if (setupItems(opc, init.connectionId)==0){
				System.out.println("成功配置OPC变量");
				addToBoard("成功配置OPC变量");
			}
			else{
				System.out.println("配置OPC变量失败");
				addToBoard("配置OPC变量失败");
			}
			try {
				autoSubmit(init.connectionId, submitFrequency);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	/**
	 * 测试用
	 * 
	 * @param args
	 * @throws SQLException 
	 * @throws UnsupportedLookAndFeelException 
	 * @throws IOException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws ClassNotFoundException 
	 * 
	 */
	public static void main(String args[]) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException, UnsupportedLookAndFeelException, SQLException {
		MsgReceiverFactory factory = new MsgReceiverFactory();
		factory.createMsgReceiverInstance();
		ServicemixConfFactory sfactory = new ServicemixConfFactory();
		sfactory.createServicemixConfInstance();
		MainControlPanel control = new MainControlPanel();
		String configFileAddr = "";
		String basicAddr = "";
		int submitFrequency = 5000;
		control.oneClickRun(configFileAddr, basicAddr, submitFrequency);
	}
}