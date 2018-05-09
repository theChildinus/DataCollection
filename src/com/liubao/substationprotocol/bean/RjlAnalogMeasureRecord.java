package com.liubao.substationprotocol.bean;

import java.io.Serializable;
import java.sql.*;

class RjlAnalogMeasureRecord implements Serializable{
	
	/*    主键        */
	String device_id;
	String sensor_id;
	String plc_id;
	String community;
	String building;
	
	/*    非主键       */
	String resident;
	String field;
	String description;
	Timestamp timeStamp;
	Date   date;
	Time   time;
	String measure_type;
	String measure_unit;
	double value;
	double factor;
	double offset;
	byte   state;
	double highlimit;
	double lowlimit;
	double highhighlimit;
	double lowlowlimit;
	int    outbound;
	
	/*    取得各字段值       */
	String getDevice(String s){
		device_id = s;
		return device_id;
	}
	
	String getSensor_id(String s){
		sensor_id = s;
		return sensor_id;
	}
	String getPlc_id(String s){
		plc_id = s;
		return plc_id;
	}
	
	String getCommunity(String s){
		community = s;
		return community;
	}
	
	String getBuilding(String s){
		building = s;
		return building;
	}
	
	String getResident(String s){
		resident = s;
		return resident;
	}
	
	String getField(String s){
		field = s;
		return field;
	}
	
	String getDescription(String s){
		description = s;
		return description;
	}
	
	Timestamp getTimeStamp(Timestamp ts){
		timeStamp = ts;
		return timeStamp;
	}
	
	Date getDate(Date d){
		date = d;
		return date;
	}
	
	Time getTime(Time t){
		time = t;
		return time;
	}
	
	String getMeasure_type(String a){
		measure_type = a;
		return measure_type;
	}
	
	String getMeasure_unit(String s){
		measure_unit = s;
		return measure_unit;
	}
	
	double getValue(double f){
		value = f;
		return value;
	}
	
	double getFactor(double f){
		factor = f;
		return factor;
	}
	
	double getOffset(double f){
		offset = f;
		return offset;
	}
	
	byte getState(byte b){
		state = b;
		return state;
	}
	
	double getHighlimit(double f){
		highlimit = f;
		return highlimit;
	}
	
	double getLowlimit(double f){
		lowlimit = f;
		return lowlimit;
	}
	
	double getHighhighlimit(double f){
		highhighlimit = f;
		return highhighlimit;
	}
	
	double getLowlowlimit(double f){
		lowlowlimit = f;
		return lowlowlimit;
	}
	
	int getOutbound(int n){
		outbound = n;
		return outbound;
	}
}
