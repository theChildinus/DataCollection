package com.zuowenfeng.beans;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

public class GLAnalog extends GL implements Serializable {
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
	
	protected Timestamp timeStamp;
	
	public Timestamp getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(Timestamp timeStamp) {
		this.timeStamp = timeStamp;
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
	
	protected String measure_unit;

	public String getMeasure_unit() {
		return measure_unit;
	}

	public void setMeasure_unit(String measureUnit) {
		measure_unit = measureUnit;
	}
	
	protected float value;

	public float getValue() {
		return value;
	}

	public void setValue(float value) {
		this.value = value;
	}
	
	protected float factor;

	public float getFactor() {
		return factor;
	}

	public void setFactor(float factor) {
		this.factor = factor;
	}
	
	protected float offset;

	public float getOffset() {
		return offset;
	}

	public void setOffset(float offset) {
		this.offset = offset;
	}
	
	protected int state;

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}
	
}
