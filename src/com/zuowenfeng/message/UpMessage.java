package com.zuowenfeng.message;

public class UpMessage {
	private RawMessage raw;
	private AnalyzedMessage analyzed;
	private String deviceid;
	
	public void setDeviceId( String deviceid ) {
		this.deviceid = deviceid;
	}
	
	public String getDeviceid() {
		return this.deviceid;
	}
	
	public void setRawMessage(RawMessage raw) {
		this.raw = raw;
	}
	
	public void setAnalyzedMessage(AnalyzedMessage analyzed ) {
		this.analyzed = analyzed;
	}
	
	public RawMessage getRawMessage() {
		return this.raw;
	}
	
	public AnalyzedMessage getAnalyzedMessage() {
		return this.analyzed;
	}
}
