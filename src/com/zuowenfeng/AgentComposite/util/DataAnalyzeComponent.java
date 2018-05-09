package com.zuowenfeng.AgentComposite.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import com.zuowenfeng.beans.HeatingPoint;

public interface DataAnalyzeComponent {
	
	public boolean checkRedundant( HeatingPoint p, Timestamp time );
	
	public HeatingPoint checkDataType( String deviceid, String plcid, String sensorid, boolean isAnalog ) throws SQLException, FileNotFoundException;
	
	public String resouceInstanceBinding( HeatingPoint p );
	
	public float offsetCalculate( float factor, float offset, float oldValue, boolean direction );
	
	public void setDataBaseComponent(DataBaseComponent dataBaseComponent) throws IOException;
	
	public int checkOutbound( HeatingPoint p );
	
	public void publishUpdate( String topic, String content ) throws Exception;
	
	public String objectToUpdateMsg( ArrayList<HeatingPoint> heatArray );
	
	public String objectToOutbound( String boilerRoom, String topic, String location, String type, float value, String level );
	
	public HeatingPoint findDownLocation( String notification ) throws SQLException, FileNotFoundException;

	public void UpAnalogData(String address,byte plcAddress,String sensorAddr,float value, int status ) throws Exception;
	
	public void UpDigitalData(String address,byte plcAddress,String sensorAddr,int value, int status ) throws Exception;
	
	public void UpAnalogOpcData(String address,String plcAddress,String sensorAddr,float value) throws Exception;
	
	public ArrayList<HeatingPoint> getGLArrayList ();
	
	public ArrayList<HeatingPoint> getRJLArrayList ();
	
	public ArrayList<HeatingPoint> getGLDigitalArrayList ();
	
	public ArrayList<HeatingPoint> getRJLDigitalArrayList ();
	
	public void UpdateStatus( String address, String plcAddress, String sensorAddr, int status ) throws Exception;
	
	//public void UpDigitalStatus( String address, byte plcAddress, String sensorAddr, int status ) throws Exception;
}
