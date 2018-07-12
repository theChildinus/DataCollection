package com.zuowenfeng.monitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.ResourceBundle;

public class IPConfiguration {
	private ResourceBundle bundle = ResourceBundle.getBundle("com.zuowenfeng.conf.localConfiguration", Locale.ENGLISH);
	private String ipaddress;
	
	public IPConfiguration( String ipaddress ) {
		this.ipaddress = ipaddress;
	}
	
	public void IPAddition() throws IOException {
		String cmd = "netsh interface ip add address \""+ bundle.getString("name") + "\" " + ipaddress+" 255.255.255.0";
		Runtime runtime = Runtime.getRuntime();
		Process process = runtime.exec(cmd);
		BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String msg;
		
		while ( ( msg = br.readLine() ) != null ) {
			System.out.println(msg);
		}
		
	}
	
	public void IPDeletion() throws IOException {
		String cmd = "netsh interface ip delete address \""+bundle.getString("name") + "\" " + ipaddress;
		Runtime runtime = Runtime.getRuntime();
		Process process = runtime.exec(cmd);
		BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String msg;
		
		while ( ( msg = br.readLine() ) != null ) {
			System.out.println(msg);
		}
	}
}
