package com.weizesan.protocolcomposite.cmodbus;

public class CSlaveProtocol {
	private byte[] buffer;


	public  CSlaveProtocol(){
		
	}
	
    public void MainProcess(byte[] buffer,int length){
		
	}
	
	public byte[] SendProcess() throws Exception{
		
		return buffer;
		
	}
	
	public byte[] SendCommand(String plc_id,String sensor_id, float value, int downtype) throws Exception{
		return buffer;
	}
	
	public int ReceiveProcess(byte[] buffer) throws Exception{
		
		return 1;
		
	}
	
	public void FormPackage(byte functiontype,byte[] data){
		
	}
	
	public void Write(){
		
	}
	
	
	

}
