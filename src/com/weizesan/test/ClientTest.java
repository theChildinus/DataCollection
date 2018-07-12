package com.weizesan.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientTest {
	private String ip="10.108.165.37";
	private int port=8000;
	private Socket socket;
	
	public ClientTest()throws IOException{
		socket=new Socket(ip,port);
	}
	
	public static void main(String[] args) throws IOException {
        new ClientTest().talk();
	}
	
	private PrintWriter getWriter(Socket socket)throws IOException{
		OutputStream socketOut=socket.getOutputStream();
		return new PrintWriter(socketOut,true);
	}
	
	private BufferedReader getReader(Socket socket)throws IOException{
		InputStream socketIn=socket.getInputStream();
		return new BufferedReader(new InputStreamReader(socketIn));
	}
	
	public void talk()throws IOException{
		try{
			BufferedReader br=getReader(socket);
			PrintWriter pw=getWriter(socket);
	//		System.out.println(br.readLine());
			BufferedReader localReader=new BufferedReader(new InputStreamReader(System.in));
			String msg ;
			while((msg=localReader.readLine())!=null){
				System.out.println("发送数据包: "+msg);
				pw.println(msg);
				System.out.println("已发送出去!!!!!");
				if(br.readLine()!=null){
					System.out.println("是否接收到响应包?");
					System.out.println(br.readLine());
					}
				System.out.println("没接收到响应包?");
				if(msg.equals("bye"))
					break;
			}
		}catch(IOException e){
			e.printStackTrace();
		}finally{
			try{
				socket.close();
			}catch(IOException e){
				e.printStackTrace();
			}
		}
	}

}
