/*
 * 该类主要负责建立及关闭与H2实时数据库的连接
 * 方法：public Connection getConnection() 主要负责建立与数据库H2的连接，并返回该连接
 * 方法：publc void closeAll(Connection conn,Statement stmt,ResultSet rs)主要负责关闭释放各种与数据库连接相关的资源
 * */
package com.liubao.substationprotocol.wireless;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.configuration.MemoryDBConfiguration;

public class H2Connection {
	private Connection conn =null;
	//建立数据库连接
	public  Connection getConnection() throws IOException{
		//Connection conn = null;
		
		MemoryDBConfiguration configuration = new MemoryDBConfiguration();
		configuration.getConnectionString();
		
		String driver = "org.h2.Driver";
		//String url="jdbc:h2:tcp://" + configuration.getUrl() + "/" + configuration.getDatabase();
		//String url = "jdbc:h2:tcp://10.108.165.37/~/myTest;DB_CLOSE_ON_EXIT=FALSE";
		String url = "jdbc:h2:tcp://" + configuration.getUrl() + "/~/" + configuration.getDatabase() + ";DB_CLOSE_ON_EXIT=FALSE";
		
		//String user= configuration.getUsername();
//		String user = "root";
		String user = configuration.getUsername();
		//String password= configuration.getPassword();
//		String password = "root";
		String password = configuration.getPassword();

			try{	
				Class.forName(driver);  //注册驱动器
				conn = DriverManager.getConnection(url, user, password); //建立数据库连接
				System.out.println("数据库连接成功：" + conn);
			}
			catch(Exception e){
				System.out.println("实时数据库h2连接失败！"+e);
				//e.printStackTrace();
			}
			
			return conn;	
		}
	
	//关闭数据库连接，释放各种资源
	public  void closeAll(Connection conn,Statement stmt,ResultSet rs){
		try{
			rs.close();
			stmt.close();
			conn.close();
		}catch(SQLException sqle){
			System.out.print("关闭数据库失败"+sqle);
		}
	}
	
	public static void main(String[] args){
		H2Connection h2 = new H2Connection();
		try {
			h2.getConnection();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
}
