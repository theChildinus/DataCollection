package com.weizesan.connection.connectioncomposite;

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.ws.Endpoint;

import wsn.wsnclient.command.SendWSNCommand;

import com.configuration.CommandConfiguration;
import com.configuration.OPCConfiguration;
import com.configuration.SustationConfiguration;
import com.configuration.WholeStationConfiguration;
import com.factory.MemoryDBConFactory;
import com.factory.MsgReceiverFactory;
import com.factory.PublishFactory;
import com.factory.ServicemixConfFactory;
import com.liubao.substationprotocol.DAO.ReceiveData;
import com.liubao.substationprotocol.DAO.SendData;
import com.liubao.substationprotocol.wireless.PackageReceiver;
import com.tianyue.opcclient.MainControlPanel;
import com.weizesan.connection.databasecomposite.*;
import com.weizesan.deviceprotocolcomposite.*;
import com.weizesan.protocolcomposite.*;
import com.zuowenfeng.AgentComposite.util.NotificationProcessImpl;
import com.zuowenfeng.configuration.panel.OPCPanel;
import com.zuowenfeng.configuration.panel.SubstationPanel;
import com.zuowenfeng.connection.viewcomposite.MainConfigurationDialog;
import com.zuowenfeng.variable.DeviceStaticData;

public class SetupConnection {

	private ArrayList<String> TcpServerName;
	private ArrayList<String> TcpServerIp;
	private ArrayList<Integer> TcpServerPort;

	private ArrayList<String> UdpServerName;
	private ArrayList<String> UdpServerIp;
	private ArrayList<Integer> UdpServerPort;

	private boolean isSerial;  // 是否存在串口通信
	private boolean isSerial232; // 是232
	private boolean isSerial485; // 是485
	private boolean isNetwork; // 是否是通过网络连接的

	private boolean Server;
	private boolean TcpServer;
	private boolean UdpServer;
	
	public static boolean isWhole;

	private boolean isLogin; // 是否已经登录

	private static String localIp; // 本机ip地址

	private ArrayList<Integer> Serial232_record;
	private ArrayList<Integer> Serial485_record;

	private String device_id_232; // 下发的设备号:
	private String device_id_485;


	// 查询配置表，选取对应设备的各种参数，如IP地址、端口号、通信类型、数据封装协议
	public void init() throws Exception {
		MsgReceiverFactory factory = new MsgReceiverFactory();
		ServicemixConfFactory.createServicemixConfInstance();
		factory.createMsgReceiverInstance();
		TcpServerName = new ArrayList<String>();
		TcpServerIp = new ArrayList<String>();
		TcpServerPort = new ArrayList<Integer>();

		UdpServerName = new ArrayList<String>();
		UdpServerIp = new ArrayList<String>();
		UdpServerPort = new ArrayList<Integer>();

		Serial232_record = new ArrayList<Integer>(); 
		Serial485_record = new ArrayList<Integer>();

		MemoryDBConFactory confs = new MemoryDBConFactory();
		confs.createMemoryDBConfiguration();

		InetAddress ia = InetAddress.getLocalHost();
		localIp = ia.getHostAddress();
		System.out.println("**********local ipaddress is :" + localIp);
		
		DataBaseComposite dc = new DataBaseComposite();
		Connection conn = dc.getmysql();
		Statement stmt = conn.createStatement();
		String sqlserver = "select * from deviceinfo where server_ipaddress='"+ localIp + "'";
		ResultSet rs = stmt.executeQuery(sqlserver);
		while (rs.next()) { 
			if (rs.getString("device_protocol").equals("232")) { 
				isSerial = true;
				isSerial232 = true;
				Serial232_record.add(rs.getInt("record_number"));
				device_id_232 = rs.getString("device_id");
			} else if (rs.getString("device_protocol").equals("485")) { 
				isSerial = true;
				isSerial485 = true;
				Serial485_record.add(rs.getInt("record_number"));
				device_id_485 = rs.getString("device_id");
			} else if (rs.getString("connection_protocol").equals("TCP")) { 
				isNetwork = true;
				Server = true;
				TcpServer = true;
				TcpServerName.add(rs.getString("server_name"));
				TcpServerIp.add(rs.getString("server_ipaddress"));
				TcpServerPort.add(rs.getInt("server_port"));
			} else if (rs.getString("connection_protocol").equals("UDP")) { 
				isNetwork = true;
				Server = true;
				UdpServer = true;
				UdpServerName.add(rs.getString("device_name"));
				UdpServerIp.add(rs.getString("server_ipaddress"));
				UdpServerPort.add(rs.getInt("server_port"));
			}
		}
		stmt.close();
		conn.close();
		System.out.println("**********init completed");
	}

	public void startService() throws Exception { 

		if (TcpServer) {

			HashSet<Integer> hs = new HashSet<Integer>(TcpServerPort);
			TcpServerPort.clear();
			TcpServerPort.addAll(hs);
			for (int i = 0; i < TcpServerPort.size(); i++) {
				Thread TcpServerThread = new Thread(new OpenTcp(TcpServerPort.get(i)));     
				TcpServerThread.start();
			}
		} 
		else if (UdpServer) {

			HashSet<Integer> hs = new HashSet<Integer>(UdpServerPort);
			UdpServerPort.clear();
			UdpServerPort.addAll(hs);
			for (int i = 0; i < UdpServerPort.size(); i++) {
				Thread UdpServerThread = new Thread(new OpenUdp(UdpServerPort.get(i)));      
				UdpServerThread.run();
			}
		}
	}
	

	class OpenUdp implements Runnable { // 启动UDP服务器，暂时未用到
		private int port;
		public OpenUdp(int sport) {
			port = sport;
		}

		public void run() {
			try {
				DatagramSocket server = new DatagramSocket(port);
				byte[] buffer = new byte[1024];
				DatagramPacket packet = new DatagramPacket(buffer, 0,
						buffer.length);
				while (true) {
					server.receive(packet);
					Thread workThread = new Thread(new HandlerUdp(server,packet));
					workThread.start();
				}
			} catch (SocketException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	class HandlerUdp implements Runnable { // 为每一个接入的UDP socket进行处理
		private DatagramSocket server;
		private DatagramPacket packet;

		public HandlerUdp(DatagramSocket server, DatagramPacket packet) {
			this.server = server;
			this.packet = packet;
		}
		public void run() {

		}
	}

	class OpenTcp implements Runnable { // 启动TCP服务器
		private int port;
		public OpenTcp(int sport) {
			port = sport;
		}

		public void run() {
			ServerSocket TcpServer = null;
			try {
				TcpServer = new ServerSocket(port);
			} catch (IOException x) {
				x.printStackTrace();
			}

			System.out.println("**********Tcp Server running");
			int clientCount = 1;
			while (true) {
				try {
					Socket socket = null;
					socket = TcpServer.accept();
					Thread workThread = new Thread(new HandlerTcp(socket,clientCount));
					workThread.start();
					clientCount++;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	class HandlerTcp implements Runnable {
		private Socket socket;
		private int clientCount;

		public HandlerTcp(Socket socket, int clientCount) {
			this.socket = socket;
			this.clientCount = clientCount;
		}

		public void run() {

			String dip = socket.getInetAddress().toString().substring(1);
			System.out.print("**********client: " + clientCount +"  IP: "+dip+" port: "+socket.getPort());
			System.out.println();
			InputStream input = null;
			try {
				input = socket.getInputStream();
			} catch (IOException e) {
				e.printStackTrace();
			}

			CDeviceProtocol pp = null;
			DeviceProtocolSearch dps = new DeviceProtocolSearch();// 搜索协议，主要是为每个接入的客户端分析其使用的是什么协议，包括设备层协议和数据层协议
			
			isLogin = false;
			boolean isConnected = false;
			boolean hasCommand = false;
			int count = 0;
			while (true) {

				byte[] msg = new byte[1024];
				int length = 0;
				try {
					Date date = new Date();				
					length = input.read(msg);
					count++;
					System.out.println();
					System.out.println();
					System.out.println("*******************************************************************************");
					System.out.println("*******************************************************************************");
					System.out.println("**********" + date + " 第"+count+"次读数,包长度为"+length);
					if (!isLogin) {
						System.out.println("**********GPRS终端第一次登录");
						isLogin = dps.AnalyzeData(msg, socket, length);
						pp = DeviceProtocolSearch.pp;
						if (isLogin) {

							pp.MainProcess(msg, length);
							msg = null;
							length = 0;
							isConnected = true;
						}
					} else {
						System.out.println("**********终端已经登录,包长度是" + length);
						pp.MainProcess(msg, length);
					}
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("**********in catch");
					if(socket != null){
						try {
							socket.close();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						if(pp!=null){
							pp = null;
						}
					}
					isConnected = false;
						
				} finally {					
					System.out.println("**********in finally ");
					if(isConnected){
						hasCommand = pp.ReceiveCommand(DeviceStaticData.device_id,
								DeviceStaticData.plc_id,
								DeviceStaticData.sensor_id, DeviceStaticData.value,
								DeviceStaticData.type, DeviceStaticData.send);

						byte[] b = new byte[10];
						b[0] = 0;
						if(hasCommand)
							length = 1;
						else
							length = 0;
						try {
							pp.MainProcess(b, length);
						} catch (Exception e) {
							e.printStackTrace();
						}
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						b = null;	
					}else
						System.out.println("client socket is closed");								
				}
			}
		}
	}

	public void start485() { 
		for (int i = 0; i < Serial485_record.size(); i++) {
			Thread serial485 = new Thread(new Handler485(Serial485_record.get(i))); 
			serial485.start();
		}
	}

	class Handler485 implements Runnable { 
		private int record485;

		public Handler485(int record) {
			record485 = record;
		}

		public void run() {
			C485 ps = null;
			try {
				ps = new C485(record485);
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}

			int send = 1, recv = 1;
			boolean hasCommand = false, canRecv = false, sendRequest = false;
			try {
				while (true) {
					System.out.println("**********第" + send + "次发包");
					send++;
					hasCommand = ps.ReceiveCommand(DeviceStaticData.device_id,
							DeviceStaticData.plc_id,
							DeviceStaticData.sensor_id, 
							DeviceStaticData.value,
							DeviceStaticData.type,
							DeviceStaticData.send);
					if (hasCommand) {
						System.out.println("**********有命令，打包下发");
						byte[] sendBuffer = ps.Send();
						if (sendBuffer.length > 0) {
							MsgReceiverFactory.receiver.receiveDown(device_id_485, sendBuffer,sendBuffer.length);
							ps.Write(sendBuffer);
							Thread.sleep(1000);
						}
						canRecv = true;
						sendRequest = false;
					} else {
						sendRequest = true;
					}
					if (sendRequest) {
						System.out.println("**********无命令，发请求包");
						byte[] sendBuffer = ps.Send();
						if (sendBuffer != null) {

							MsgReceiverFactory.receiver.receiveDown(device_id_485, sendBuffer,sendBuffer.length);
							ps.Write(sendBuffer);
							Thread.sleep(1000);
							canRecv = true;

						} else {
							System.out.println("**********请求包为空");
							recv++;
							canRecv = false;
						}
					}
					if (canRecv) {
						System.out.println("**********读串口");
						System.out.println("**********第" + recv + "次收包");
						ps.Recevie();
						recv++;
						Thread.sleep(5000);
					}
					ps.sendBreak();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void start232() { // 启动串口232
		for (int i = 0; i < Serial232_record.size(); i++) {
			Thread serial232 = new Thread(new Handler232(Serial232_record.get(i)));
			serial232.start();
		}
	}

	class Handler232 implements Runnable { // 处理485线程，为每个与之相连的plc进行轮询请求数据
		private int record232;

		public Handler232(int record) {
			record232 = record;
		}

		public void run() {
			C232 pc = null;
			try {
				pc = new C232(record232);
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}

			boolean hasCommand = false;
			boolean canRecv = false;
			boolean sendRequest = false;
			boolean wait=true;
			int send = 1, recv = 1;
			try {
				while (true) {
					System.out.println("-------------------------------------------------------------");
					System.out.println("**********第" + send + "次发包");
					send++;
					hasCommand = pc.ReceiveCommand(DeviceStaticData.device_id,
							DeviceStaticData.plc_id,
							DeviceStaticData.sensor_id, 
							DeviceStaticData.value,
							DeviceStaticData.type,
							DeviceStaticData.send);
					if (hasCommand) {
						System.out.println("**********有控制命令，打包下发");
						byte[] sendBuffer = pc.Send();
						if (sendBuffer!=null && sendBuffer.length>0) {

							MsgReceiverFactory.receiver.receiveDown(device_id_232, sendBuffer,sendBuffer.length);
							pc.Write(sendBuffer);
							Thread.sleep(1000);
							canRecv = true;
							sendRequest = false;
							wait = true;
						}else{
							System.out.println("**********控制命令包为空");
							sendRequest = false;
						}
						
					} else
						sendRequest = true;
					if (sendRequest) {
						System.out.println("**********无控制命令，发请求包");
						byte[] sendBuffer = pc.Send();
						if (sendBuffer != null) {

							MsgReceiverFactory.receiver.receiveDown(device_id_232, sendBuffer,sendBuffer.length);
							pc.Write(sendBuffer);
							Thread.sleep(1000);
							canRecv = true;
							wait = true;
						} else {
							System.out.println("**********请求包为空");
							recv++;
							canRecv = false;
							wait = false;
						}
					}
					if (canRecv) {
						System.out.println("**********读串口");
						System.out.println("**********第" + recv + "次收包");
						pc.Recevie();
						recv++;			
					}
					pc.sendBreak();
					if(wait){
						Thread.sleep(5000);
					}
					System.out.println("-------------------------------------------------------------");
					System.out.println();
					System.out.println();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void start() throws Exception {
		if (isSerial) {
			if (isSerial485) {
				start485();
				System.out.println("**********485串口running...");
			} else if (isSerial232) {
				start232();
				System.out.println("**********232串口running...");
			}
		}
		if (isNetwork) {
			if (Server) {
				startService();
			}
			System.out.println("**********network running....");
		}
	}

	public static void main(String[] args) throws Exception {
		ServicemixConfFactory factory = new ServicemixConfFactory();
		factory.createServicemixConfInstance();
		boolean isOpc;
		boolean isWhole;
		
		if ( args.length == 0 ) {
			MainConfigurationDialog dialog = new MainConfigurationDialog(null);
			isOpc = dialog.isOpcSelected();
			isWhole = dialog.isWholeStation();
		}
		
		else {
			isOpc = Boolean.getBoolean(args[0]);
			isWhole = Boolean.getBoolean(args[1]);
		}
		
		factory.createServicemixConfInstance();
		PublishFactory pf = new PublishFactory();
		pf.create();
		
		if ( isOpc == true ) {
			
			MainControlPanel panel = new MainControlPanel();
			OPCConfiguration conf = new OPCConfiguration();
			conf.getOPCConnection();
			
			panel.oneClickRun(conf.getDirectory(), conf.getDeviceid(), Integer.parseInt(conf.getOpcTime()));
		}
		
		if ( isWhole == true ) { //主站(客户机)
			WholeStationConfiguration configuration = new WholeStationConfiguration();
			configuration.getConnectionString();
			CommandConfiguration commandConf = new CommandConfiguration();
			commandConf.getCommandConfiguration();
			Thread thread = new Thread(new PublishWebService(commandConf.getUrl(),
					commandConf.getPort(), commandConf.getWebService(),
					ServicemixConfFactory.conf.getUrl(),
					ServicemixConfFactory.conf.getPort()));
			thread.start();
			SetupConnection sc = new SetupConnection();
//			sc.init();
//			sc.start();
			sc.wirelessWholeToSubStart();
			ReceiveData receive = new ReceiveData(configuration.getPort());
			receive.startReceiver();
		}
		
		else { //从站(服务器)
			SustationConfiguration subs = new SustationConfiguration();
			subs.getConnectionString();
			Thread thread = new Thread(new DownCommandThread());
			thread.start();
			SetupConnection sc = new SetupConnection();
			sc.init();
			sc.Run();
			sc.start();
			SendData sendData = new SendData( subs.getIP(), subs.getPort());
		}
		
	}
	
	public void wirelessWholeToSubStart() {
		PackageReceiver pr = new PackageReceiver();
//		MasterToSubstation m2s = new MasterToSubstation();
		Thread receive = new Thread(pr);
//		Thread send = new Thread(m2s);
		receive.start();
	}
	
	public void wholeStart( MainConfigurationDialog dialog ) {
		
	}

	public void Run() throws IOException {
		CommandConfiguration commandConf = new CommandConfiguration();
		commandConf.getCommandConfiguration();
		Thread thread = new Thread(new PublishWebService(commandConf.getUrl(),
				commandConf.getPort(), commandConf.getWebService(),
				ServicemixConfFactory.conf.getUrl(),
				ServicemixConfFactory.conf.getPort()));
		thread.start();
		
	}
	
}

class DownCommandThread implements Runnable {

	public void run() {
		while ( true ) {
			try {
				SendData.receiveCommand();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}	
	}	
}

class PublishWebService implements Runnable {
	private String webServiceUrl;
	private int webServiceport;
	private String webServicename;
	private String servicemixUrl;
	private int servicemixPort;

	public PublishWebService(String webUrl, int webPort, String webService,
			String servicemixUrl, int servicemixPort) {
		this.webServiceUrl = webUrl;
		this.webServiceport = webPort;
		this.webServicename = webService;
		this.servicemixUrl = servicemixUrl;
		this.servicemixPort = servicemixPort;
	}

	@Override
	public void run() {
		NotificationProcessImpl a = new NotificationProcessImpl();
		Endpoint.publish("http://" + this.webServiceUrl + ":"
				+ this.webServiceport + "/" + this.webServicename, a);
		SendWSNCommand command = new SendWSNCommand("http://"
				+ this.webServiceUrl + ":" + this.webServiceport + "/"
				+ this.webServicename, "http://" + ServicemixConfFactory.conf.getUrl() + ":" + ServicemixConfFactory.conf.getPort());
		String response = "";
		try {
			response = command.createPullPoint();

			if (response.equals("ok")) {
				String response2 = command.subscribe("command");

				if (response2.equals("ok")) {
					System.out.println("Subscribe successfully.");
				}

				else {
					System.out.println("Failure in subscribe.");
				}

			}

			else {
				System.out.println("Failure in createPullPoint");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
