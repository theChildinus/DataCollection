package com.liubao.substationprotocol.wireless;

public class MasterMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		PackageReceiver pr = new PackageReceiver();
//		MasterToSubstation m2s = new MasterToSubstation();
		Thread receive = new Thread(pr);
//		Thread send = new Thread(m2s);
		receive.start();
//		send.start();


	}

}
