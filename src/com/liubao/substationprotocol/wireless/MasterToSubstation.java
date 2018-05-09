package com.liubao.substationprotocol.wireless;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;



//总站向子站下发换热站数据
public class MasterToSubstation implements Runnable {
	
	private H2Connection h2con = null;
	private static Connection conn = null;
	private Statement stmt = null;
	private ResultSet rs = null;
	private String command = null;
	private static ConnectList cl = new ConnectList();
	private ConnectKeeper ck = null;
	private OutputStream os =null;
	private Socket socket = null;
	private byte[] dtuID;
	private ArrayList table = new ArrayList();
	private ArrayList col = new ArrayList();

	
	public static void main( String[] args ) throws Exception{
		//测试用
		getConnection();
		
	}
	
	public MasterToSubstation(Socket socket) throws IOException {
		this.socket = socket;
	}
	
	private void init() throws Exception{
		h2con = new H2Connection();
		conn = getConnection();
		stmt = conn.createStatement();
		
		
		
		//找出当前socket对应的ck，初始化dtuID[]，deviceID在具体表格时候从ck初始化
	}

	
	public static byte[] getByteArray(float x) {
		int index=0;
	    byte[] bb = new byte[4];
	    int l = Float.floatToIntBits(x);
	    for (int i = 0; i < 4; i++) {
	        bb[index + i] = new Integer(l).byteValue();
	        l = l >> 8;
	    }
	    return bb;
	}
	
	//float[]转换为byte[]
	public static void floatArrayToByteArray(float[] ff,byte[] bt){
		for(int i = 0;i<ff.length;i++){
			byte[] bb = new byte[4];
			bb= getByteArray(ff[i]);
			for(int j=0;j<bb.length;j++)
				bt[j+i*4] = bb[j];
		}
	}
	
	//按规则打包数据表内容并发送
	private void packData (Statement stmt , String tableName , String column) throws Exception{
		float data[] = new float[252];
		float table[] = null;
		byte[] bs = null;
		int len = 0;
		int totalLen = 0;
		int[] deviceID = null;

		
		
		/*为了解析时能够辨别该数据包是哪张表，哪个字段，用data[0]来标注
		 * 个位1~4表示是哪张表
		 * 十位(可能包括百位)表示是这张表中的哪种数据
		 * */
		System.out.println("以下是表"+tableName+"的数据");
		if(tableName.equals("GL_ANALOG_MEASURE")){
			deviceID = ck.getDevice_id1();
			System.out.println(111111);
			if(column.equals("VALUE"))
				data[0] = 11.0f;
			else if(column.equals("STATE"))
				data[0] = 21.0f;
			//十位的3暂时空着
			else if(column.equals("FACTOR"))
				data[0] = 41.0f;
			else if(column.equals("OFFSET"))
				data[0] = 51.0f;
			else if(column.equals("OUTBOUND"))
				data[0] = 61.0f;
			else if(column.equals("BLOCKFLAG"))
				data[0] = 71.0f;
			else if(column.equals("BLOCKVALUE"))
				data[0] = 81.0f;
			else System.out.println("GL_ANALOG_MEASURE表中查询了未知字段");
		
		}else if(tableName == "GL_ANALOG_CONTROL"){
			deviceID = ck.getDevice_id2();
			System.out.println(222222);
			if(column.equals("VALUE"))
				data[0] = 12.0f;
			else if(column.equals("STATE"))
				data[0] = 22.0f;
			else if(column.equals("FACTOR"))
				data[0] = 42.0f;
			else if(column.equals("OFFSET"))
				data[0] = 52.0f;
			else if(column.equals("BLOCKFLAG"))
				data[0] = 72.0f;
			else if(column.equals("BLOCKVALUE"))
				data[0] = 82.0f;
			else System.out.println("GL_ANALOG_CONTROL表中查询了未知字段");
		
		}else if(tableName == "GL_DIGITAL_MEASURE"){
			deviceID = ck.getDevice_id3();
			System.out.println(3333333);
			if(column.equals("VALUE"))
				data[0] = 13.0f;
			else if(column.equals("STATE"))
				data[0] = 23.0f;
			else if(column.equals("BLOCKFLAG"))
				data[0] = 73.0f;
			else if(column.equals("BLOCKVALUE"))
				data[0] = 83.0f;
			else if(column.equals("CLOSE"))
				data[0] = 93.0f;
			else if(column.equals("OPEN"))
				data[0] = 103.0f;
			else if(column.equals("WHENOUT"))
				data[0] = 113.0f;
			else if(column.equals("ISBEYOND"))
				data[0] = 123.0f;
			else System.out.println("GL_DIGITAL_MEASURE表中查询了未知字段");
		
		}else if(tableName == "GL_DIGITAL_CONTROL"){
			deviceID = ck.getDevice_id4();
			System.out.println(4444444);
			if(column.equals("VALUE"))
				data[0] = 14.0f;
			else if(column.equals("BLOCKFLAG"))
				data[0] = 74.0f;
			else if(column.equals("BLOCKVALUE"))
				data[0] = 84.0f;
			else if(column.equals("CLOSE"))
				data[0] = 94.0f;
			else if(column.equals("OPEN"))
				data[0] = 104.0f;
			else System.out.println("GL_DIGITAL_CONTROL表中查询了未知字段");
		}else System.out.println("查询的表名出错");
		
		if(ck == null ||deviceID == null ||dtuID ==null)
		{
			System.out.print(ck == null);
			System.out.print(deviceID == null);
			System.out.print(dtuID == null);
			System.out.println();
			return;
		}
		
		for(int j = 0 ; j < deviceID.length ; j++){
			System.out.println("当前device_id为： " + deviceID[j]);
			command = "SELECT " + column + " FROM " + tableName + " WHERE DEVICE_ID ='"+deviceID[j]+"' ORDER BY TOPIC";
			System.out.println(command);
			rs = stmt.executeQuery(command);
			if(rs == null) return;
			data[1] = deviceID[j];
			int k = 3;
			len = 0;
			while(rs.next()){
				String tmpstr = rs.getString(column);
				if(tmpstr == null) return;
				data[k] = Float.valueOf(tmpstr);
				len++;
				k++;
			}
			System.out.println("共有"+len+"条记录");
			data[2] = len;
			totalLen = len +3;
			table = new float[totalLen];
			System.arraycopy(data, 0, table, 0, totalLen);
			if(table == null) return;
			System.out.println("待发送数据为： " + Arrays.toString(table));
			bs = new byte[totalLen*4];
			floatArrayToByteArray(table, bs);
			//System.out.println("待发送字节流为： " + Arrays.toString(bs));
			byte[] toSend=formDataPackage(bs);
			System.out.println("加上各字段的字节流： " + Arrays.toString(toSend));
			//发送
			if(toSend == null) return;
			os.write(toSend);
			os.flush();
			System.out.println("已发送");
			Thread.sleep(10000);
			
			System.out.println();
		}
		
	}
	
	private byte[] formDataPackage(byte[] b){
		byte[] bb = new byte[b.length+16];
		for(int i = 0 ; i < dtuID.length ; i++){
			System.out.print("DTU: "+dtuID[i]);
		}
		System.out.println();
		int total_len = b.length + 16;
		bb[0] = 0x7B;
		bb[1] = (byte) 0x89;
		bb[2] = (byte) (total_len >>> 8);
		bb[3] = (byte) total_len;
		bb[total_len-1] = 0x7B;
		for(int i = 0 ; i < dtuID.length ; i++){
			bb[i+4] = dtuID[i];
		}
		
		for(int i = 0 ; i < b.length ; i++){
			bb[i+15] = b[i];
		}
		
		return bb;
	}
	
	//在send()中被调用，用于按一定顺序发送每张表的每个需要发送的字段
	public void controlSend() throws Exception{
		packData(stmt, "GL_ANALOG_MEASURE", "VALUE");
		TimeUnit.SECONDS.sleep(2);
		packData(stmt, "GL_ANALOG_CONTROL", "VALUE");
		TimeUnit.SECONDS.sleep(2);
		packData(stmt, "GL_DIGITAL_MEASURE", "VALUE");
		TimeUnit.SECONDS.sleep(2);
		packData(stmt, "GL_DIGITAL_CONTROL", "VALUE");
		TimeUnit.SECONDS.sleep(2);
	}
	
	private OutputStream socketOutputStream(Socket incoming) throws IOException{
		return incoming.getOutputStream();
	}
	
	//建立数据库连接
		public static Connection getConnection() throws IOException{
			//Connection conn = null;
			
			//MemoryDBConfiguration configuration = new MemoryDBConfiguration();
			//configuration.getConnectionString();
			
			String driver = "org.h2.Driver";
			//String url="jdbc:h2:tcp://" + configuration.getUrl() + "/" + configuration.getDatabase();
			String url = "jdbc:h2:tcp://localhost/mem:test;DB_CLOSE_ON_EXIT=FALSE";
			//String user= configuration.getUsername();
			String user = "root";
			//String password= configuration.getPassword();
			String password = "123456";

				try{	
					Class.forName(driver);  //注册驱动器
					conn = DriverManager.getConnection(url, user, password); //建立数据库连接
					System.out.println("数据库连接成功：" + conn);
				}
				catch(Exception e){
					e.printStackTrace();
					System.out.println("实时数据库h2连接失败！");
				}
				
				return conn;	
			}

	@Override
	public void run() {
	
		try {
			init();
			FileInputStream fi = new FileInputStream("configMasterToSub.txt");
			BufferedReader br = new BufferedReader(new InputStreamReader(fi));
			String line = "";
			while((line = br.readLine()) != null){
				table.add(line);
				System.out.println(line);
				if((line = br.readLine()) != null){
					col.add(line);
					System.out.println(line);
				}
			}
		} catch (FileNotFoundException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (Exception e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		breaklable:
		while(true)
		{

			for(int i = 0 ; i < cl.aConList.size() ; i++){
				if(socket == cl.aConList.get(i).getSocket()){
					ck = cl.aConList.get(i);
					System.out.println("找到ck");
					break;
				}
			}
			dtuID = ck.getDtu_id();
			
			System.out.println("在发送进程中,当前ConnectList大小:"+cl.aConList.size());

//			for(int j=0 ; j<cl.aConList.size() ; j++)
//			{
//				Socket socket = cl.aConList.get(j).getSocket();
//				if(socket == null || socket.isClosed()){
//					cl.aConList.remove(j);
//				}else{
					System.out.println("当前Socket: "+socket+" 是否关闭: "+ socket.isClosed());
					try {
						synchronized (socket) 
						{
						
						int len;
						if((len = table.size()) != col.size()){
							System.out.println("configMasterToSub.txt配置表有问题");
							return;
						}
						
						for(int i = 0 ; i < len ; i++){
							os = socketOutputStream(socket);
							packData(stmt, table.get(i).toString(), col.get(i).toString());
						}
						
						
//						byte bb[]={123, (byte) 0x89, (byte)(1015 >>> 8), (byte) 1015, 49, 50, 51, 52, 53, 54, 55, 56, 57, 48, 49, 
//								1 , 2 , 3 , 4 , 5 , 6 , 7 , 8 , 9 , 10 , 11 , 12 , 13 , 14 , 15 , 16 , 17 , 18 , 19 , 20 , 21 , 22 , 23 , 24 , 25 , 26 , 27 , 28 , 29 , 30 , 31 , 32 , 33 , 34 , 35 , 36 , 37 , 38 , 39 , 40 , 41 , 42 , 43 , 44 , 45 , 46 , 47 , 48 , 49 , 50 , 51 , 52 , 53 , 54 , 55 , 56 , 57 , 58 , 59 , 60 , 61 , 62 , 63 , 64 , 65 , 66 , 67 , 68 , 69 , 70 , 71 , 72 , 73 , 74 , 75 , 76 , 77 , 78 , 79 , 80 , 81 , 82 , 83 , 84 , 85 , 86 , 87 , 88 , 89 , 90 , 91 , 92 , 93 , 94 , 95 , 96 , 97 , 98 , 99 , 0 , 1 , 2 , 3 , 4 , 5 , 6 , 7 , 8 , 9 , 10 , 11 , 12 , 13 , 14 , 15 , 16 , 17 , 18 , 19 , 20 , 21 , 22 , 23 , 24 , 25 , 26 , 27 , 28 , 29 , 30 , 31 , 32 , 33 , 34 , 35 , 36 , 37 , 38 , 39 , 40 , 41 , 42 , 43 , 44 , 45 , 46 , 47 , 48 , 49 , 50 , 51 , 52 , 53 , 54 , 55 , 56 , 57 , 58 , 59 , 60 , 61 , 62 , 63 , 64 , 65 , 66 , 67 , 68 , 69 , 70 , 71 , 72 , 73 , 74 , 75 , 76 , 77 , 78 , 79 , 80 , 81 , 82 , 83 , 84 , 85 , 86 , 87 , 88 , 89 , 90 , 91 , 92 , 93 , 94 , 95 , 96 , 97 , 98 , 99 , 0 , 1 , 2 , 3 , 4 , 5 , 6 , 7 , 8 , 9 , 10 , 11 , 12 , 13 , 14 , 15 , 16 , 17 , 18 , 19 , 20 , 21 , 22 , 23 , 24 , 25 , 26 , 27 , 28 , 29 , 30 , 31 , 32 , 33 , 34 , 35 , 36 , 37 , 38 , 39 , 40 , 41 , 42 , 43 , 44 , 45 , 46 , 47 , 48 , 49 , 50 , 51 , 52 , 53 , 54 , 55 , 56 , 57 , 58 , 59 , 60 , 61 , 62 , 63 , 64 , 65 , 66 , 67 , 68 , 69 , 70 , 71 , 72 , 73 , 74 , 75 , 76 , 77 , 78 , 79 , 80 , 81 , 82 , 83 , 84 , 85 , 86 , 87 , 88 , 89 , 90 , 91 , 92 , 93 , 94 , 95 , 96 , 97 , 98 , 99 , 0 , 1 , 2 , 3 , 4 , 5 , 6 , 7 , 8 , 9 , 10 , 11 , 12 , 13 , 14 , 15 , 16 , 17 , 18 , 19 , 20 , 21 , 22 , 23 , 24 , 25 , 26 , 27 , 28 , 29 , 30 , 31 , 32 , 33 , 34 , 35 , 36 , 37 , 38 , 39 , 40 , 41 , 42 , 43 , 44 , 45 , 46 , 47 , 48 , 49 , 50 , 51 , 52 , 53 , 54 , 55 , 56 , 57 , 58 , 59 , 60 , 61 , 62 , 63 , 64 , 65 , 66 , 67 , 68 , 69 , 70 , 71 , 72 , 73 , 74 , 75 , 76 , 77 , 78 , 79 , 80 , 81 , 82 , 83 , 84 , 85 , 86 , 87 , 88 , 89 , 90 , 91 , 92 , 93 , 94 , 95 , 96 , 97 , 98 , 99 , 0 , 1 , 2 , 3 , 4 , 5 , 6 , 7 , 8 , 9 , 10 , 11 , 12 , 13 , 14 , 15 , 16 , 17 , 18 , 19 , 20 , 21 , 22 , 23 , 24 , 25 , 26 , 27 , 28 , 29 , 30 , 31 , 32 , 33 , 34 , 35 , 36 , 37 , 38 , 39 , 40 , 41 , 42 , 43 , 44 , 45 , 46 , 47 , 48 , 49 , 50 , 51 , 52 , 53 , 54 , 55 , 56 , 57 , 58 , 59 , 60 , 61 , 62 , 63 , 64 , 65 , 66 , 67 , 68 , 69 , 70 , 71 , 72 , 73 , 74 , 75 , 76 , 77 , 78 , 79 , 80 , 81 , 82 , 83 , 84 , 85 , 86 , 87 , 88 , 89 , 90 , 91 , 92 , 93 , 94 , 95 , 96 , 97 , 98 , 99 , 0 , 1 , 2 , 3 , 4 , 5 , 6 , 7 , 8 , 9 , 10 , 11 , 12 , 13 , 14 , 15 , 16 , 17 , 18 , 19 , 20 , 21 , 22 , 23 , 24 , 25 , 26 , 27 , 28 , 29 , 30 , 31 , 32 , 33 , 34 , 35 , 36 , 37 , 38 , 39 , 40 , 41 , 42 , 43 , 44 , 45 , 46 , 47 , 48 , 49 , 50 , 51 , 52 , 53 , 54 , 55 , 56 , 57 , 58 , 59 , 60 , 61 , 62 , 63 , 64 , 65 , 66 , 67 , 68 , 69 , 70 , 71 , 72 , 73 , 74 , 75 , 76 , 77 , 78 , 79 , 80 , 81 , 82 , 83 , 84 , 85 , 86 , 87 , 88 , 89 , 90 , 91 , 92 , 93 , 94 , 95 , 96 , 97 , 98 , 99 , 0 , 1 , 2 , 3 , 4 , 5 , 6 , 7 , 8 , 9 , 10 , 11 , 12 , 13 , 14 , 15 , 16 , 17 , 18 , 19 , 20 , 21 , 22 , 23 , 24 , 25 , 26 , 27 , 28 , 29 , 30 , 31 , 32 , 33 , 34 , 35 , 36 , 37 , 38 , 39 , 40 , 41 , 42 , 43 , 44 , 45 , 46 , 47 , 48 , 49 , 50 , 51 , 52 , 53 , 54 , 55 , 56 , 57 , 58 , 59 , 60 , 61 , 62 , 63 , 64 , 65 , 66 , 67 , 68 , 69 , 70 , 71 , 72 , 73 , 74 , 75 , 76 , 77 , 78 , 79 , 80 , 81 , 82 , 83 , 84 , 85 , 86 , 87 , 88 , 89 , 90 , 91 , 92 , 93 , 94 , 95 , 96 , 97 , 98 , 99 , 0 , 1 , 2 , 3 , 4 , 5 , 6 , 7 , 8 , 9 , 10 , 11 , 12 , 13 , 14 , 15 , 16 , 17 , 18 , 19 , 20 , 21 , 22 , 23 , 24 , 25 , 26 , 27 , 28 , 29 , 30 , 31 , 32 , 33 , 34 , 35 , 36 , 37 , 38 , 39 , 40 , 41 , 42 , 43 , 44 , 45 , 46 , 47 , 48 , 49 , 50 , 51 , 52 , 53 , 54 , 55 , 56 , 57 , 58 , 59 , 60 , 61 , 62 , 63 , 64 , 65 , 66 , 67 , 68 , 69 , 70 , 71 , 72 , 73 , 74 , 75 , 76 , 77 , 78 , 79 , 80 , 81 , 82 , 83 , 84 , 85 , 86 , 87 , 88 , 89 , 90 , 91 , 92 , 93 , 94 , 95 , 96 , 97 , 98 , 99 , 0 , 1 , 2 , 3 , 4 , 5 , 6 , 7 , 8 , 9 , 10 , 11 , 12 , 13 , 14 , 15 , 16 , 17 , 18 , 19 , 20 , 21 , 22 , 23 , 24 , 25 , 26 , 27 , 28 , 29 , 30 , 31 , 32 , 33 , 34 , 35 , 36 , 37 , 38 , 39 , 40 , 41 , 42 , 43 , 44 , 45 , 46 , 47 , 48 , 49 , 50 , 51 , 52 , 53 , 54 , 55 , 56 , 57 , 58 , 59 , 60 , 61 , 62 , 63 , 64 , 65 , 66 , 67 , 68 , 69 , 70 , 71 , 72 , 73 , 74 , 75 , 76 , 77 , 78 , 79 , 80 , 81 , 82 , 83 , 84 , 85 , 86 , 87 , 88 , 89 , 90 , 91 , 92 , 93 , 94 , 95 , 96 , 97 , 98 , 99 , 0 , 1 , 2 , 3 , 4 , 5 , 6 , 7 , 8 , 9 , 10 , 11 , 12 , 13 , 14 , 15 , 16 , 17 , 18 , 19 , 20 , 21 , 22 , 23 , 24 , 25 , 26 , 27 , 28 , 29 , 30 , 31 , 32 , 33 , 34 , 35 , 36 , 37 , 38 , 39 , 40 , 41 , 42 , 43 , 44 , 45 , 46 , 47 , 48 , 49 , 50 , 51 , 52 , 53 , 54 , 55 , 56 , 57 , 58 , 59 , 60 , 61 , 62 , 63 , 64 , 65 , 66 , 67 , 68 , 69 , 70 , 71 , 72 , 73 , 74 , 75 , 76 , 77 , 78 , 79 , 80 , 81 , 82 , 83 , 84 , 85 , 86 , 87 , 88 , 89 , 90 , 91 , 92 , 93 , 94 , 95 , 96 , 97 , 98 , 99 , 
//								123};
//						System.out.println("发送包的DTU_ID:"+dtuID.toString());
//						os.write(bb);
//						os.flush();
						}

						
					}
					catch (NullPointerException e){
						e.printStackTrace();
						
					}
					catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						try {
							socket.close();
							for (int i = 0; i < cl.aConList.size(); i++) {
								if (cl.aConList.get(i).getSocket() == null	|| cl.aConList.get(i).getSocket().isClosed()) {
									System.out.println("########################貌似从来没运行到过这里。。");
									cl.aConList.remove(i);
									for (int t = 0; t < cl.threadlist.size(); t++) {
										cl.threadlist.get(t).interrupt();
										cl.threadlist.remove(t);
										break breaklable;
									}
									// threadlist.removeAll(threadlist);//清空线程池，重新注册
								}
							}
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					} 
//				}
				try {
					Thread.sleep(15000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
//			}
		}
	System.out.println("跳出while(true)循环");
	}
	
}
