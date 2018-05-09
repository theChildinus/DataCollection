package com.zuowenfeng.beans;

import java.io.Serializable;

public class HeatingPoint implements Serializable {
	//dataType: 0 GL_ANALOG_MEASURE
	//			1 GL_ANALOG_CONTROL
	//			2 GL_DIGITAL_MEASURE
	//			3 GL_DIGITAL_CONTROL
	//			4 RJL_ANALOG_MEASURE
	//			5 RJL_ANALOG_CONTROL
	//			6 RJL_DIGITAL_MEASURE
	//			7 RJL_DIGITAL_CONTROL
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected int dataType;
	protected String device_id;
	protected String sensor_id;
	protected String plc_id;
	private int blockFlag;
	private int blockValue;
	
	public void setBlockFlag( int blockFlag ) {
		this.blockFlag = blockFlag;
	}
	
	public int getBlockFlag() {
		return this.blockFlag;
	}
	
	public void setBlockValue( int blockValue ) {
		this.blockValue = blockValue;
	}
	
	public int getBlockValue() {
		return this.blockValue;
	}
	
	public void setDataType( int dataType ) {
		this.dataType = dataType;
	}
	
	public int getDataType() {
		return this.dataType;
	}
	
	public void setDeviceID( String device_id ) {
		this.device_id = device_id;
	}
	
	public String getDeviceID() {
		return this.device_id;
	}
	 
	public void setSensorID( String sensor_id ) {
		this.sensor_id = sensor_id;
	}
	
	public String getSensorID() {
		return this.sensor_id;
	}
	
	public void setPLCID( String plc_id ) {
		this.plc_id = plc_id;
	}
	
	public String getPLCID() {
		return this.plc_id;
	}
}
