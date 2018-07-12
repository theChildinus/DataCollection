package com.weizesan.connection.databasecomposite;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.configuration.DeviceInfoConfiguration;

//读取数据库文件，主要是为了查询对应字段的设备信息，如数据封装协议的类型
public class DataBaseComposite {

	public Connection getmysql() throws IOException {
		
		try{
		   Class.forName("com.mysql.jdbc.Driver");
		   DeviceInfoConfiguration conf = new DeviceInfoConfiguration();
		   conf.getConnectionString();
		   Connection conn=DriverManager.getConnection("jdbc:mysql://" + conf.getURL() + ":" + conf.getPort() + "/" + conf.getDatabase(), conf.getUsername(), conf.getPassword());
		   return conn;
			
		}catch(ClassNotFoundException e){
			e.printStackTrace();
		}catch(SQLException e)
		{
			e.printStackTrace();
		}
		return null;
	}


}
