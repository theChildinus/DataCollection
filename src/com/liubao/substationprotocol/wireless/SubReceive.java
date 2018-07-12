package com.liubao.substationprotocol.wireless;

public class SubReceive {
	
	private static C232 pc = null;
	
	public static void receive() throws Exception{
		while(true){
			pc.Read();
		}
		
	}
	
	public static void main(String[] args) throws Exception{
		pc = new C232();
//		receive();
	}

}
