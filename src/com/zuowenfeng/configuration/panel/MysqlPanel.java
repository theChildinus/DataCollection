package com.zuowenfeng.configuration.panel;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import com.configuration.DeviceInfoConfiguration;


public class MysqlPanel extends JPanel {
	private JLabel urlLabel = new JLabel("数据库地址:");
	private JTextField urlField = new JTextField(15);
	
	private JLabel portLabel = new JLabel("端口:");
	private JTextField portField = new JTextField(15);
	
	private JLabel databaseLabel = new JLabel("库名:");
	private JTextField databaseField = new JTextField(15);
	
	private JLabel usernameLabel = new JLabel("用户名:");
	private JTextField usernameField = new JTextField(15);
	
	private JLabel passwordLabel = new JLabel("密码:");
	private JPasswordField passwordField = new JPasswordField(15);
	private JFrame parent;
	private DeviceInfoConfiguration configuration;
	
	public MysqlPanel( JFrame frame ) throws IOException {
		parent = frame;
		configuration = new DeviceInfoConfiguration();
		configuration.getConnectionString();
		Box box = Box.createVerticalBox();
		setLayout(new BorderLayout());
		
		setBorder(new TitledBorder("mysql配置"));
		
		JPanel urlPanel = new JPanel();
		urlPanel.setLayout(new FlowLayout());
		urlPanel.setBorder(new TitledBorder("数据库IP地址"));
		urlPanel.add(urlLabel);
		urlPanel.add(urlField);
		urlField.setText(configuration.getURL());
		
		box.add( urlPanel );
		box.add( Box.createVerticalStrut(8));
		
		JPanel portPanel = new JPanel();
		portPanel.setLayout(new FlowLayout());
		portPanel.setBorder(new TitledBorder("数据库端口"));
		
		portPanel.add(portLabel, BorderLayout.WEST);
		portPanel.add(portField, BorderLayout.CENTER);
		portField.setText("" + configuration.getPort());
		
		box.add( portPanel );
		box.add( Box.createVerticalStrut(6));
		
		JPanel databasePanel = new JPanel();
		databasePanel.setLayout(new FlowLayout());
		databasePanel.setBorder(new TitledBorder("库名"));
		
		databasePanel.add(databaseLabel);
		databasePanel.add(databaseField);
		databaseField.setText(configuration.getDatabase());
		
		box.add( databasePanel );
		box.add( Box.createVerticalStrut(6));
		
		JPanel usernamePanel = new JPanel();
		usernamePanel.setLayout(new FlowLayout());
		usernamePanel.setBorder(new TitledBorder("用户名"));
		
		usernamePanel.add(usernameLabel);
		usernamePanel.add(usernameField);
		usernameField.setText(configuration.getUsername());
		
		box.add( usernamePanel );
		box.add( Box.createVerticalStrut(6));
		
		JPanel passwordPanel = new JPanel();
		passwordPanel.setLayout(new FlowLayout());
		passwordPanel.setBorder(new TitledBorder("密码"));
		
		passwordPanel.add(passwordLabel);
		passwordPanel.add(passwordField);
		passwordField.setText(configuration.getPassword());
		
		JButton testButton = new JButton("连接测试");
		JButton defaultButton = new JButton("还原默认值");
		
		DefaultHandler handler2 = new DefaultHandler();
		defaultButton.addActionListener(handler2);
		
		TestHandler handler = new TestHandler();
		
		testButton.addActionListener(handler);
		
		JPanel testPanel = new JPanel();
		testPanel.setLayout(new FlowLayout());
		testPanel.setBorder(new TitledBorder("测试"));
		
		testPanel.add(testButton);
		testPanel.add(defaultButton);
		
		box.add( passwordPanel );
		box.add( Box.createVerticalStrut(6));
		
		box.add( testPanel );
		box.add( Box.createVerticalStrut(6));
		
		add( box, BorderLayout.CENTER);
	}
	
	class DefaultHandler implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			urlField.setText(configuration.getURL());
			portField.setText("" + configuration.getPort());
			databaseField.setText( configuration.getDatabase());
			usernameField.setText( configuration.getUsername());
			passwordField.setText( configuration.getPassword());
		}
		
	}
	
	class TestHandler implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			String url = "jdbc:mysql://" + urlField.getText() + ":" + portField.getText() + "/" + databaseField.getText();
			String username = usernameField.getText();
			String password = passwordField.getText();
			Connection conn;
			
			try {
				Class.forName("com.mysql.jdbc.Driver");
				conn = DriverManager.getConnection( url, username, password );
				conn.close();
				JOptionPane.showMessageDialog(parent, "连接成功");
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				JOptionPane.showMessageDialog(parent, "连接失败");
			}
			
		}
		
	}
	
	public String getIPAddress() {
		return urlField.getText();
	}
	
	public String getPort() {
		return portField.getText();
	}
	
	public String getDatabase() {
		return databaseField.getText();
	}
	
	public String getUsername() {
		return usernameField.getText();
	}
	
	public String getPassword() {
		return passwordField.getText();
	}
	
	public DeviceInfoConfiguration getConfiguration() {
		return this.configuration;
	}
}
