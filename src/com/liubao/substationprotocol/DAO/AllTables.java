package com.liubao.substationprotocol.DAO;

import java.io.Serializable;
import java.util.ArrayList;

import com.zuowenfeng.beans.HeatingPoint;


class AllTables implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	ArrayList<HeatingPoint> table_GlAnalogMeasure = null;
	ArrayList<HeatingPoint> table_GlDigitalMeasure = null;
	ArrayList<HeatingPoint> table_RjlAnalogMeasure = null;
	ArrayList<HeatingPoint> table_RjlDigitalMeasure = null;
	ArrayList<HeatingPoint> table_GlAnalogControl = null;
	ArrayList<HeatingPoint> table_GlDigitalControl = null;
	ArrayList<HeatingPoint> table_RjlAnalogControl = null;
	ArrayList<HeatingPoint> table_RjlDigitalControl = null;
}
