package com.zuowenfeng.beans;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

public class RJLDigital extends RJL {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected String field;

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}
	
	protected Timestamp timestamp;

	public Timestamp getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}
	
	protected Date date;

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
	
	protected Time time;

	public Time getTime() {
		return time;
	}

	public void setTime(Time time) {
		this.time = time;
	}
	
	protected String measure_type;

	public String getMeasure_type() {
		return measure_type;
	}

	public void setMeasure_type(String measureType) {
		measure_type = measureType;
	}
	
	protected float value;

	public float getValue() {
		return value;
	}

	public void setValue(float value) {
		this.value = value;
	}
	
	protected int close;

	public int getClose() {
		return close;
	}

	public void setClose(int close) {
		this.close = close;
	}
	
	protected int open;

	public int getOpen() {
		return open;
	}

	public void setOpen(int open) {
		this.open = open;
	}
	
	protected int state;

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}
	
	
	
}
