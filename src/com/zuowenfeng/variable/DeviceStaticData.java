package com.zuowenfeng.variable;

import java.io.Serializable;
import java.util.ArrayList;

public class DeviceStaticData implements Serializable {
	public static ArrayList<String> device_id = new ArrayList<String> ();    //设备号
	public static ArrayList<String> plc_id = new ArrayList<String> ();       //PLC号
	public static ArrayList<String> sensor_id = new ArrayList<String> ();    //传感器地址
	public static ArrayList<Float> value = new ArrayList<Float> ();         //下发的值
	public static ArrayList<Boolean> send = new ArrayList<Boolean> ();        //是否能下发
	public static ArrayList<Integer> type = new ArrayList<Integer> ();            //类型,0表示开关量,1表示整型模拟量,2表示符点型模拟量
	
}
