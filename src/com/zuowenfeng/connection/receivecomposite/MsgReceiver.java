package com.zuowenfeng.connection.receivecomposite;


import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.UnsupportedLookAndFeelException;

import com.zuowenfeng.connection.viewcomposite.MsgShowUI;
import com.zuowenfeng.message.AnalyzedMessage;
import com.zuowenfeng.message.DownAnalyzedMessage;
import com.zuowenfeng.message.DownMessage;
import com.zuowenfeng.message.RawMessage;
import com.zuowenfeng.message.UpMessage;

public class MsgReceiver {
	private int port = 7002;
	private ServerSocket serverSocket;
	private MsgShowUI msgTest;
	private Socket socket;
	private PrintWriter pw;
	private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss.S");
	private UpMessage ups;
	private DownMessage downs;
	//private BufferedReader br2;
	
	public MsgReceiver() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, SQLException {
		//serverSocket = new ServerSocket(port);
		msgTest = new MsgShowUI();
		msgTest.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//socket = new Socket("10.108.166.252", 9999 );
		//pw = new PrintWriter(socket.getOutputStream(), true );
		//br2 = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		//pw.println(InetAddress.getLocalHost().toString() + " start");
		//Thread thread1 = new Thread(new Listener());
		//thread1.start();
	}
	
	public void receive( String deviceid, byte[] array, int length ) throws IOException, InterruptedException {
		//Socket socket = null;
		//socket = serverSocket.accept();
		//BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		
		//while ( true ) {
			//String msg = br.readLine();
			
			//if ( msgTest.getStates() == true && ( msg != null ) ) {
		String result = getRawString( array, length );
//		System.out.println(result);
		RawMessage raw = new RawMessage(format.format(new Date()), result);
		UpMessage ups = new UpMessage();
		ups.setDeviceId(deviceid);
		ups.setRawMessage(raw);
		this.ups = ups;
//		msgTest.setTexts("解析前：\n" + new Date().toString() + result + "\n");
		//msgTest.showTexts();
		//Thread.sleep(1000);
			//}
			
		//}
		
	}
	
	public void receiveDown( String deviceid, byte[] array, int length ) {
		String result = getRawString( array, length );
		RawMessage raw = new RawMessage(format.format(new Date()), result);
		DownMessage downs = new DownMessage();
		downs.setRawMessage(raw);
		this.downs = downs;
		this.downs.setDeviceid(deviceid);
		downs.setDeviceid(deviceid);
		msgTest.setDownMessage(downs);
		msgTest.showTexts();
	}
	
	public void receive( String device_id, String plc_id, String sensor_id, float value, String time ) {
//		msgTest.setTexts(receiveString + "\n");
		AnalyzedMessage amsg = new AnalyzedMessage(device_id, plc_id, sensor_id, value, time);
		this.ups.setAnalyzedMessage(amsg);
		msgTest.setUpMessage(ups);
		msgTest.showTexts();
	}
	
	public void receiveDown( String device_id, String plc_id, String sensor_id, float value, String downtype ) {
		DownAnalyzedMessage damsg = new DownAnalyzedMessage(device_id, plc_id, sensor_id, value, downtype);
		//DownMessage dmsg = new DownMessage();
		this.downs.setDownMessage(damsg);
		msgTest.setDownMessage(downs);
		msgTest.showTexts();
	}
	
	public static void main ( String[] args ) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, InterruptedException, SQLException {
		MsgReceiver msgRec = new MsgReceiver();
		//msgRec.receive();
	}
	
	public String byte2Hex( String input ) {
		String formula = "0123456789ABCDEF";
		String result = "";
		//int circles = 0;
		//int length = 0;
		result = result.concat("0x");
		
		if( input.equals("") ) {
			return "";
		}
		
		for ( int i = 0; i <= input.length() - 1; i += 4 ) {
			int oneInput = 0;
			
			for ( int j = 3; j >= 0; j-- ) {
				oneInput += (int) (Integer.parseInt("" + input.charAt(i + 3 - j )) * Math.pow(2, j));
			}
			
			result = result.concat("" + formula.charAt(oneInput));
			
			/*if ( circles % 6 == 0 ) {
				result = result.concat("    ");
			}
			
			else if ( circles % 2 == 0 ) {
				result = result.concat(" ");
			}*/
					
		}
		
		result = result.concat("\n");
		return result;
	}
	
	public String getRawString( byte[] rawByte, int length ) {
		String result = "";
		
		for ( int i = 0; i <= length - 1; i++ ) {			
			String s = Integer.toHexString(rawByte[i]);
			if(s.length()<2)
				s=0+s;
			if ( rawByte[i] < 0 ) {
				s = s.substring(6, s.length());
			}
			result = result.concat("" + s );
		}
		
		result = result.concat("\n");
		return result;
	}
	
}
