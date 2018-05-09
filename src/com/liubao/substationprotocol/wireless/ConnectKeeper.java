package com.liubao.substationprotocol.wireless;

import java.net.Socket;


public class ConnectKeeper {
	private int[] device_id1;
	private int[] device_id2;
	private int[] device_id3;
	private int[] device_id4;
	private byte[] dtu_id;
	private Socket socket;
	
	public int[] getDevice_id1() {
		return device_id1;
	}
	public void setDevice_id1(int[] device_id1) {
		this.device_id1 = device_id1;
	}
	public int[] getDevice_id2() {
		return device_id2;
	}
	public void setDevice_id2(int[] device_id2) {
		this.device_id2 = device_id2;
	}
	public int[] getDevice_id3() {
		return device_id3;
	}
	public void setDevice_id3(int[] device_id3) {
		this.device_id3 = device_id3;
	}
	public int[] getDevice_id4() {
		return device_id4;
	}
	public void setDevice_id4(int[] device_id4) {
		this.device_id4 = device_id4;
	}
	public byte[] getDtu_id() {
		return dtu_id;
	}
	public void setDtu_id(byte[] dtu_id) {
		this.dtu_id = dtu_id;
	}
	
	public Socket getSocket() {
		return socket;
	}
	public void setSocket(Socket socket) {
		this.socket = socket;
	}
}
