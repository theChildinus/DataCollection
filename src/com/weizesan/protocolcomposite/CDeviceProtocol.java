package com.weizesan.protocolcomposite;

import java.util.ArrayList;

public class CDeviceProtocol {
	private byte[] buffer;
	
    public CDeviceProtocol(){
		 
	 }
	
    public void MainProcess(byte[] msg,int length) throws Exception{
		
	}
	
	public byte[] Send() throws Exception{
		return buffer;
	}
	
	public void SendProcess() throws Exception{
		
	}
	
	public void RecevieProcess() throws Exception{
		
	}
	
	public void Write(){
		
	}
	
	public void Package(){
		
	}

	public boolean ReceiveCommand(ArrayList<String> device_id, ArrayList<String> plc_id, ArrayList<String> sensor_id, ArrayList<Float> value, ArrayList<Integer> type, ArrayList<Boolean> send) {
		return false;
	}
	
}


