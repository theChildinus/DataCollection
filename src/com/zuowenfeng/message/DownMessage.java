package com.zuowenfeng.message;

public class DownMessage {
	private DownAnalyzedMessage downMessage;
	private RawMessage rawMessage;
	private String deviceid;
	
	public void setDeviceid( String deviceid ) {
		this.deviceid = deviceid;
	}
	
	public String getDeviceid() {
		return this.deviceid;
	}
	 
	public void setRawMessage( RawMessage rawMessage ) {
		this.rawMessage = rawMessage;
	}
	
	public RawMessage getRawMessage() {
		return this.rawMessage;
	}
	
	public void setDownMessage( DownAnalyzedMessage downMessage ) {
		this.downMessage = downMessage;
	}
	
	public DownAnalyzedMessage getDownAnalyzedMessage() {
		return this.downMessage;
	}
	
}
