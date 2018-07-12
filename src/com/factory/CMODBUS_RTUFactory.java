package com.factory;

import java.io.IOException;

import com.weizesan.protocolcomposite.cmodbus.DVP_MODBUS_RTU;

public class CMODBUS_RTUFactory {
	public static DVP_MODBUS_RTU rtu;
	
	public void createCMODBUSRTUInstance( int arg0) throws IOException {
		rtu = new DVP_MODBUS_RTU(arg0);
	}
}
