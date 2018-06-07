package com.liubao.substationprotocol.wireless;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.configuration.SustationConfiguration;
import com.zuowenfeng.configuration.panel.SubstationPanel;

public class PackageReceiver implements Runnable {

	private static ConnectList cl = new ConnectList();

	public static void receive() throws IOException {
		System.out.println("tt");
		SustationConfiguration configuration = new SustationConfiguration();
		configuration.getConnectionString();
		
		try {
			ServerSocket ss = new ServerSocket(configuration.getPort());
			ExecutorService es = Executors.newCachedThreadPool();

			while (true) {
				Socket s = ss.accept();
				System.out.println(s.getInetAddress().getHostName());
				System.out.println("PackageReceiver ServerSocket is: " + s);
				es.execute(new WirelessHandler(s));
				System.out.println("开始接收");
				//加锁？
				for (int i = 0; i < cl.aConList.size(); i++) {
					if (cl.aConList.get(i).getSocket() == null	|| cl.aConList.get(i).getSocket().isClosed()) {
						System.out.println("########################貌似从来没运行到过这里。。");
						cl.aConList.remove(i);
						for (int t = 0; t < cl.threadlist.size(); t++) {
							cl.threadlist.get(t).interrupt();
							cl.threadlist.remove(t);
						}
						// threadlist.removeAll(threadlist);//清空线程池，重新注册
					}
				}
			}
		} catch (Exception e) {
			System.out.println("网络有问题！" + e);
		}
	}

	public static void main(String[] args) throws IOException {
		receive();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			receive();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
