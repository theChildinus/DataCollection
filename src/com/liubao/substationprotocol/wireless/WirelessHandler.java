package com.liubao.substationprotocol.wireless;
import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.configuration.UpdateDBInfoConfiguration;
import com.factory.ServicemixConfFactory;
import com.zuowenfeng.AgentComposite.util.DataAnalyzeComponentImpl;
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


public class WirelessHandler implements Runnable {
	private   Socket incoming = null;
	private  byte[] isArray ;
	private  float[] result;
	private  InputStream is ;
	private  OutputStream os;
	private  static byte[] dtu_id =new byte[11];
	private  int dataLen;
	private DataBaseComponent impl;
	private Connection conn;
	private PublishComponentImpl impls;
	private int device_id_count =0;
	
	
	private float[] device_id;
	private int[] deviceId;
	
	ConnectList cl = new ConnectList();
	ConnectKeeper ck = new ConnectKeeper();
	
	public WirelessHandler(Socket incoming) throws IOException{
		this.incoming = incoming;
		System.out.println("Socket is:"+incoming);
//		impl = new DataBaseComponentImpl();
//		conn = impl.geth2();
		UpdateDBInfoConfiguration conf = new UpdateDBInfoConfiguration();
		conf.getUpdateConfiguration();
		ServicemixConfFactory factory = new ServicemixConfFactory();
		factory.createServicemixConfInstance();
		impls = new PublishComponentImpl("http://" + conf.getUrl() + ":" + conf.getPort() + "/" + conf.getServicename(), "http://" + ServicemixConfFactory.conf2.getUrl() + ":" + ServicemixConfFactory.conf2.getPort() + "/cxf/NotificationProxy");
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		int span = 0; //具有相同device_id的记录的条数
		while(true){
//			for( int s = 0 ; s < cl.aConList.size() ; s++){
//				if(cl.aConList.get(s).getSocket() == null || cl.aConList.get(s).getSocket().isClosed())
//				{
//					System.out.println("########################貌似从来没运行到过这里。。");
//					cl.aConList.remove(s);
//					for(int t = 0 ; t < threadlist.size() ; t++){
//						threadlist.get(t).interrupt();
//						threadlist.remove(t);
//					}
////					threadlist.removeAll(threadlist);//清空线程池，重新注册
//				}
//			}
			try {
				is = incoming.getInputStream();
				os = incoming.getOutputStream();
				
				//获得输入字节流
				byte[] tmp = new byte[1024];
				BufferedInputStream bis = new BufferedInputStream(is);
				int count = bis.read(tmp);
				//int count = is.available();
				//System.out.println("count="+count);
				//int count = bis.available();
			
				if(count>0){
				isArray = new byte[count];
				System.arraycopy(tmp, 0, isArray, 0, count);
				//is.read(isArray);	
				System.out.print("输入字节流长度为："+isArray.length+"  该字节流为:");
				System.out.println(Arrays.toString(isArray));
				
				//判断包类型
				getDtuId(isArray);
				System.out.print("DTU识别码: ");
				for(int cnt =0;cnt<11;cnt++){
					System.out.print(dtu_id[cnt]-'0');
				}
				switch(isArray[1]){
				case 1:{
					System.out.println("  这是登录注册包.");
				//	getDtuId(isArray);
					byte[] response = formRespondPackage(isArray[1]);
					ck.setSocket(incoming);
					ck.setDtu_id(dtu_id);
					
					
					boolean flag =true;
					for(int i = 0 ; i<cl.aConList.size() ; i++){
						if(incoming == cl.aConList.get(i).getSocket()){
							flag = false;
							break;
						}
					}
					
					if(flag){
						cl.aConList.add(ck);
						System.out.println("ConnectList中增加了Socket："+incoming);
						Thread t = new Thread(new MasterToSubstation(incoming));
						cl.threadlist.add(t);
						t.start();
						System.out.println("当前发送线程池大小： "+cl.threadlist.size());
					}
					flag = true;
					System.out.println("当前ConnectList中线形表大小："+cl.aConList.size());
					os.write(response);
					os.flush();
//					TimeUnit.MILLISECONDS.sleep(20);
					Thread.sleep(20);
//					//50
//					byte bb[]={123, -119, 1, 16, 49, 50, 51, 52, 53, 54, 55, 56, 49, 48, 48, 0, 0, 48, 65, 0, 0, -128, 64, 0, 0, 116, 66, 0, 0, 8, 66, 0, 0, -128, 63, 0, 0, -80, 65, 0, 0, -128, 63, 0, 0, -128, 63, 0, 0, -128, 63, 0, 0, 0, 64, 0, 0, 0, 64, 0, 0, 0, 64, 0, 0, 0, 64, 0, 0, -80, 65, 0, 0, -72, 65, 0, 0, 0, 64, 0, 0, 0, 64, 0, 0, -72, 65, 0, 0, 64, 65, 0, 0, 8, 66, 0, 0, -80, 65, 0, 0, 64, 65, 0, 0, 48, 65, 0, 0, -80, 65, 0, 0, -128, 63, 0, 0, -80, 65, 0, 0, -128, 63, 0, 0, -128, 63, 0, 0, -128, 63, 0, 0, -80, 65, 0, 0, -128, 63, 0, 0, -128, 63, 0, 0, -128, 63, 0, 0, -128, 63, 0, 0, -128, 63, 0, 0, -80, 65, 0, 0, -128, 63, 0, 0, -128, 63, 0, 0, -128, 63, 0, 0, -80, 65, 0, 0, -128, 63, 0, 0, -128, 63, 0, 0, -128, 63, 0, 0, -128, 63, 0, 0, -128, 63, 0, 0, -80, 65, 0, 0, -128, 63, 0, 0, -128, 63, 0, 0, -128, 63, 0, 0, -80, 65, 0, 0, -128, 63, 0, 0, -128, 63, 0, 0, -128, 63, 0, 0, -128, 63, 0, 0, -128, 63, 0, 0, 0, 0, 0, 0, -128, 63, 0, 0, -128, 63, 0, 0, -128, 63, 0, 0, -80, 65, 0, 0, -128, 63, 0, 0, -128, 63, 0, 0, -128, 63, 0, 0, -128, 63, 123};
//					os.write(bb);
//					os.flush();
					break;
				}
				case 2:{
					System.out.println("  这是请求注销包！");
					byte[] response = formRespondPackage(isArray[1]);
					os.write(response);
					//写cl的remove算法
					break;
				}
				case 9:{
					System.out.println("   这是数据包，请解析：");
					System.out.println("该数据包Socket:"+incoming);
					//	getDtuId(isArray);
					byte[] tmp1 = new byte[2];
					tmp1[0] = isArray[3];
					tmp1[1] = isArray[2];
					dataLen = getShort(tmp1)-16; //得到数据部分的长度
					byte[] dataPackage = new byte[dataLen];
					for(int i = 0;i<dataLen;i++)
						dataPackage[i] = isArray[i+15];
					result = byteArrayToFloatArray(dataPackage);
					System.out.println("接收到的结果为："+Arrays.toString(result));
					device_id_count = (int) result[1];
					device_id = new float[device_id_count];
					deviceId = new int[device_id_count];
					System.out.println("共有"+device_id_count+"个不同的device_id.");
					int flag = (int)result[0];
					switch(flag){
					case 1: {
						System.out.println("这是表GL_ANALOG_MEASURE的数据，具体解析如下：");
						for(int cnt =0;cnt<device_id_count;cnt++){
							System.out.println("span = "+span);
							device_id[cnt] = result[span+2];
							deviceId[cnt] = (int)device_id[cnt];
							System.out.print("第"+(cnt+1)+"个device_id = " + device_id[cnt]);
							System.out.print(" ,共有"+(int) result[span+3]+"条记录");
							float[] updateData = new float[(int) result[span+3]]; //直接将该数组内的数值更新入数据库中即可
							System.arraycopy(result, span+4, updateData, 0, (int) result[span+3]);
							System.out.println(",分别为："+Arrays.toString(updateData));
							//更新数据库
							ArrayList<HeatingPoint> p = checkDataType("" + deviceId[cnt], "GL_ANALOG_MEASURE");
							
							for ( int s = 0; s <= p.size() - 1; s++ ) {
								((GLAnalogMeasure)p.get(s)).setValue(updateData[s]);
							}
							
							String content = objectToUpdateMsg(p);
							impls.publish("all", content );
							System.out.println("已发布数据更新");
							span =span+ (int) result[span+3]+2;
							
						}
						span = 0;
						for(int k = 0 ; k < cl.aConList.size() ; k++){
							if(incoming == cl.aConList.get(k).getSocket()){
								cl.aConList.get(k).setDevice_id1(deviceId);
								System.out.println("对Socket"+incoming+"对应的DeviceID进行更新");
//								
								for(int i = 0 ; i<cl.aConList.get(k).getDevice_id1().length ; i++){
									System.out.print(cl.aConList.get(k).getDevice_id1()[i]+"  ");
								}
								System.out.println();
							}
						}
						break;
					}
					case 2:{
						System.out.println("这是表GL_ANALOG_CONTROL的数据");
						for(int cnt =0;cnt<device_id_count;cnt++){
							System.out.println("span = "+span);
							device_id[cnt] = result[span+2];
							deviceId[cnt] = (int)device_id[cnt];
							System.out.print("第"+(cnt+1)+"个device_id = " + device_id[cnt]);
							System.out.print(" ,共有"+(int) result[span+3]+"条记录");
							float[] updateData = new float[(int) result[span+3]]; //直接将该数组内的数值更新入数据库中即可
							System.arraycopy(result, span+4, updateData, 0, (int) result[span+3]);
							System.out.println(",分别为："+Arrays.toString(updateData));
							//更新数据库
							ArrayList<HeatingPoint> p = checkDataType("" + deviceId[cnt], "GL_ANALOG_CONTROL");
							
							for ( int s = 0; s <= p.size() - 1; s++ ) {
								((GLAnalogControl)p.get(s)).setValue(updateData[s]);
							}
							
							String content = objectToUpdateMsg(p);
							impls.publish("gl_analog_control", content );
							System.out.println("已发布数据更新");
							span =span+ (int) result[span+3]+2;
							//System.out.println(" ,共有"+(int) result[span+3]+"条记录");
						}
						span = 0;
						for(int k = 0 ; k < cl.aConList.size() ; k++){
							if(incoming == cl.aConList.get(k).getSocket()){
								cl.aConList.get(k).setDevice_id2(deviceId);
								System.out.println("对Socket"+incoming+"对应的DeviceID进行更新");
								for(int i = 0 ; i<cl.aConList.get(k).getDevice_id2().length ; i++){
									System.out.print(cl.aConList.get(k).getDevice_id2()[i]);
								}
							}
						}
						break;
					}
					case 3:{
						System.out.println("这是表GL_DIGITAL_MEASURE的数据");
						for(int cnt =0;cnt<device_id_count;cnt++){
							System.out.println("span = "+span);
							device_id[cnt] = result[span+2];
							deviceId[cnt] = (int)device_id[cnt];
							System.out.print("第"+(cnt+1)+"个device_id = " + device_id[cnt]);
							System.out.print(" ,共有"+(int) result[span+3]+"条记录");
							float[] updateData = new float[(int) result[span+3]]; //直接将该数组内的数值更新入数据库中即可
							System.arraycopy(result, span+4, updateData, 0, (int) result[span+3]);
							System.out.println(",分别为："+Arrays.toString(updateData));
							//更新数据库
							span =span+ (int) result[span+3]+2;
							//System.out.println(" ,共有"+(int) result[span+3]+"条记录");
						}
						span = 0;
						for(int k = 0 ; k < cl.aConList.size() ; k++){
							if(incoming == cl.aConList.get(k).getSocket()){
								cl.aConList.get(k).setDevice_id3(deviceId);
								System.out.println("对Socket"+incoming+"对应的DeviceID进行更新");
//								System.out.println("更新后");
								for(int i = 0 ; i<cl.aConList.get(k).getDevice_id3().length ; i++){
									System.out.print(cl.aConList.get(k).getDevice_id3()[i]);
								}
							}
						}
						break;
					}
					case 4:{
						System.out.println("以上是表GL_DIGITAL_CONTROL的数据");
						for(int cnt =0;cnt<device_id_count;cnt++){
							System.out.println("span = "+span);
							device_id[cnt] = result[span+2];
							deviceId[cnt] = (int)device_id[cnt];
							System.out.print("第"+(cnt+1)+"个device_id = " + device_id[cnt]);
							System.out.print(" ,共有"+(int) result[span+3]+"条记录");
							float[] updateData = new float[(int) result[span+3]]; //直接将该数组内的数值更新入数据库中即可
							System.arraycopy(result, span+4, updateData, 0, (int) result[span+3]);
							System.out.println(",分别为："+Arrays.toString(updateData));
							//更新数据库
							ArrayList<HeatingPoint> p = checkDataType("" + deviceId[cnt], "GL_DIGITAL_CONTROL");
							
							for ( int s = 0; s <= p.size() - 1; s++ ) {
								((GLDigitalControl)p.get(s)).setValue(updateData[s]);
							}
							
							String content = objectToUpdateMsg(p);
							impls.publish("gl_digital_control", content );
							System.out.println("已发布数据更新");
							span =span+ (int) result[span+3]+2;
							//System.out.println(" ,共有"+(int) result[span+3]+"条记录");
						}
						span = 0;
						for(int k = 0 ; k < cl.aConList.size() ; k++){
							if(incoming == cl.aConList.get(k).getSocket()){
								cl.aConList.get(k).setDevice_id4(deviceId);
								System.out.println("对Socket"+incoming+"对应的DeviceID进行更新");
//								System.out.println("更新后");
								for(int i = 0 ; i<cl.aConList.get(k).getDevice_id4().length ; i++){
									System.out.print(cl.aConList.get(k).getDevice_id4()[i]);
								}
							}
						}
						break;
					}
					default: break;
					}
					

					//在此处将数据插入到数据库中
//					int a = (int) result[ 0 ];
//					String device_id = "" + a;
//					int b = (int) result[ 1 ];
//					String table = "";
//					
//					switch( b ) {
//					case 1:
//						table = "gl_analog_measure";
//						break;
//						
//					case 3:
//						table = "gl_analog_control";
//						break;
//					
//					case 4:
//						table = "gl_digital_control";
//						break;
//					}
//					
//					ArrayList<HeatingPoint> arrays = checkDataType(device_id, table );
//					
//					for ( int i = 0; i <= arrays.size() - 1; i++ ) {
//						
//						if ( arrays.get(i).getDataType() == 0 ) {
//							GLAnalogMeasure measure = (GLAnalogMeasure)arrays.get(i);
//							measure.setValue(result[ i + 2 ] );
//						}
//						
//						else if ( arrays.get(i).getDataType() == 1 ) {
//							GLAnalogControl measure = (GLAnalogControl)arrays.get(i);
//							measure.setValue(result[ i + 2 ] );
//						}
//						
//						else if ( arrays.get(i).getDataType() == 3 ) {
//							GLDigitalControl gdc = (GLDigitalControl)arrays.get(i);
//							gdc.setValue(result[ i + 2 ] );
//						}
//						
//					}
//					
//					String publishUpdate = objectToUpdateMsg(arrays);
//					impls.publish(table, publishUpdate);
					break;
				}
				default: System.out.println("错误类型的数据包！");
						break;
				}		
			}
			//	else
//				System.out.println("空包.");
				
				} 
			catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				//System.out.print("数据流有问题，需重新接收！");
				//System.out.println("错误类型为："+e.getClass().getSimpleName());
			} 
//			catch (SQLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} 
			catch (Exception e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				//System.out.println("错误类型为："+e.getClass().getSimpleName());
			}
		}
		
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

	//将字节流转换为float[]，方便插入数据库
	public float[] byteArrayToFloatArray(byte[] bb) {
		// TODO Auto-generated method stub
		float[] ff = new float[dataLen];
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
	
	//提取DTU身份识别码
	public static void getDtuId(byte[] tmp1){
		for(int i = 0;i<11;i++){
			dtu_id[i] = tmp1[i+4];
		}
	}
	
	
	//构造应答包
	public static byte[] formRespondPackage(byte b){
		byte[] bp = new byte[16];
		bp[0] = 0x7B;
		bp[2] = 0x00;
		bp[3] = 0x10;
		bp[15] = 0x7B;
		for(int i = 0;i<11;i++){
			bp[i+4] = dtu_id[i];
//			bp[i+4] = 0x7B;
		}
		
		if(b==1)
			bp[1] = (byte) 0x81;
		else
			bp[1] = (byte) 0x82;
//		//发送特定字符串，测试接受程序
//		for(int i = 0 ; i < 16 ; i++){
//			bp[i] = 0x7B;
//		}
		return bp;
	}
	
	//byte[]转换为int数据
//	public static int byteArray2int(byte[] b){
//	    byte[] a = new byte[4];
//	    int i = a.length - 1,j = b.length - 1;
//	    for (; i >= 0 ; i--,j--) {//从b的尾部(即int值的低位)开始copy数据
//	        if(j >= 0)
//	            a[i] = b[j];
//	        else
//	            a[i] = 0;//如果b.length不足4,则将高位补0
//	  }
//	    int v0 = (a[0] & 0xff) << 24;//&0xff将byte值无差异转成int,避免Java自动类型提升后,会保留高位的符号位
//	    int v1 = (a[1] & 0xff) << 16;
//	    int v2 = (a[2] & 0xff) << 8;
//	    int v3 = (a[3] & 0xff) ;
//	    return v0 + v1 + v2 + v3;
//	}
	
	//byte[]转换为short
	public static short getShort(byte[] b) {
	      return (short) (((b[1] << 8) | b[0] & 0xff));
	}
	
	public static void main ( String[] args ) {
		
	}
}
