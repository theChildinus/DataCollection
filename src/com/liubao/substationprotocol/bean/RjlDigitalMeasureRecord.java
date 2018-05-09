package com.liubao.substationprotocol.bean;

import java.io.Serializable;
import java.sql.*;
class RjlDigitalMeasureRecord implements Serializable{

    /*    主键       */
    String device_id;
    String sensor_id;
    String plc_id;
    String community;
    String building;

    /*    非主键        */
    String resident;
    String field;
    String description;

    Timestamp timeStamp;
    Date      date;
    Time      time;

    String measure_type;
    int    state;
    int    value;
    int    whenout;
    int	   isBeyond;
    int    close;
    int    open;


    /*    取得各字段的值        */
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

    int getState(int n){
        state = n;
        return state;
    }

    int getValue(int n){
        value = n;
        return value;
    }

    int getWhenout(int n){
        whenout = n;
        return whenout;
    }

    int getIsByond(int n){
        isBeyond = n;
        return isBeyond;
    }

    int getClose(int n){
        close = n;
        return close;
    }

    int getOpen(int n){
        open = n;
        return open;
    }
}