package com.liubao.substationprotocol.bean;

import java.io.Serializable;
import java.sql.*;

class GlDigitalControlRecord implements Serializable{
	
	/*   主键        */
	String device_id;
	String sensor_id;
	String plc_id;
	String boilerRoom;
	
	/*   非主键       */
	String boiler;
	String field;
	String description;
	String measure_type;
	Timestamp timeStamp;
	Date      date;
	Time      time;
	int    close;
	int    open;
	int    value;
	
	
	/*    获得各字段的值    */
	String getDevice_id(String s){
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
	
	String getBoilerRoom(String s){
		boilerRoom = s;
		return boilerRoom;
	}
	
	String getBoiler(String s){
		boiler = s;
		return boiler;
	}
	
	String getField(String s){
		field = s;
		return field;
	}
	
	String getDescription(String s){
		description = s;
		return description;
	}
	
	String getMeasure_type(String s){
		measure_type = s;
		return measure_type;
	}
	
	int getClose(int n){
		close = n;
		return close;
	}
	
	int getOpen(int n){
		open = n;
		return open;
	}
	
	Timestamp getTimestamp(Timestamp ts){
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
	
	int getValue(int n){
		value = n;
		return value;
	}
}
