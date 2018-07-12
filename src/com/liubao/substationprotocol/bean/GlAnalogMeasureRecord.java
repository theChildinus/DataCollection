package com.liubao.substationprotocol.bean;

import java.io.Serializable;
import java.sql.*;
//import java.sql.Date;
//import java.util.*;

public class GlAnalogMeasureRecord implements Serializable {
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
		String measure_unit;
		double value;
		double factor;
		double offset;
		double highlimit;
		double lowlimit;
		double highhighlimit;
		double lowlowlimit;
		int    outbound; //1超下限  2超上限  3没超限  4超上上限  5超下下限  6超限回归
		byte   state;
		
		
		/*   非基本类型       */
		Timestamp timeStamp = null;
		Date      date = null;
		Time      time = null;
		
		
		/*  获取各字段的值   */
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
			return f;
		}
		
		int getOutbound(int n){
			outbound = n;
			return outbound;
		}
		
		byte getState(byte b){
			state = b;
			return state;
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

		
}
