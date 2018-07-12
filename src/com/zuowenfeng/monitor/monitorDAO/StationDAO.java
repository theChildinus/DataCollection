package com.zuowenfeng.monitor.monitorDAO;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;
import java.util.ResourceBundle;

import com.configuration.DeviceInfoConfiguration;

public class StationDAO {
	private String url;
	private String port;
	private String username;
	private String password;
	//private ResourceBundle bundle = ResourceBundle.getBundle("com.zuowenfeng.conf.DatabaseConnection", Locale.ENGLISH);
	private DeviceInfoConfiguration conf = new DeviceInfoConfiguration();
	private Statement stmt;
	private Connection conn;
	
	public StationDAO() throws ClassNotFoundException, SQLException, IOException {
		conf.getConnectionString();
		url = conf.getURL();
		username = conf.getUsername();
		password = conf.getPassword();
		port = "" + conf.getPort();
		Class.forName("com.mysql.jdbc.Driver");
		conn = DriverManager.getConnection("jdbc:mysql://" + url + ":" + port + "/" + "deviceinfo", username, password );
		stmt = conn.createStatement();
	}
	
	public ResultSet getWholeResult() throws SQLException {
		String sql = "select * from deviceInfo";
		return stmt.executeQuery(sql);
	}
	
	public ResultSet getAssignedDistinctResult( String column ) throws SQLException {
		String sql = "select distinct ";
		sql = sql.concat(column + " from deviceinfo;");
		
		return stmt.executeQuery(sql);
	}
	
	public ResultSet getAssignedResult(String[] columns, String constraints ) throws SQLException {
		String sql = "select ";
		System.out.println(constraints);
		for ( int i = 0; i <= columns.length - 1; i++ ) {
			sql = sql.concat(columns[ i ]);
			
			if ( i != columns.length - 1 ) {
				sql = sql.concat(",");
			}
			
			sql = sql.concat(" ");
		}
		
		if ( !constraints.equals("")) {
			sql = sql.concat("from deviceinfo where " + constraints + ";");
		}
		
		else 
			sql = sql.concat("from deviceinfo;");
		System.out.println(sql);
		return stmt.executeQuery(sql);
	}
	
	public void updateAssignedResult(String[] columns, String[] values, String constraints ) throws SQLException {
		String sql = "update deviceinfo set ";
		
		for ( int i = 0; i <= columns.length - 1; i++ ) {
			sql = sql.concat(columns[i] + " = '");
			sql = sql.concat(values[i] + "'");
			
			if ( i != columns.length - 1 ) {
				sql = sql.concat(", ");
			}
			
		}
		
		sql = sql.concat(" where " + constraints + ";");
		stmt.executeUpdate(sql);
//		System.out.println(sql);
	}
	
	public void closeConnection() throws SQLException {
		stmt.close();
		conn.close();
	}
	
	public static void main ( String[] args ) throws ClassNotFoundException, SQLException, IOException {
		StationDAO dao = new StationDAO();
		dao.updateAssignedResult(new String[]{"sipaddress"}, new String[]{"10.108.166.237"}, "sport = 9001");
		//ResultSet sets = dao.getAssignedResult(new String[]{"sname", "sipaddress"}, "deviceprotocol = 485");
		
//		while ( sets.next() ) {
//			System.out.println(sets.getString("sname"));
//			System.out.println(sets.getString("sipaddress"));
//		}
//		
	}
}
