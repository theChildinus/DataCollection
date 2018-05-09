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

import com.configuration.MemoryDBConfiguration;

public class H2Panel extends JPanel {
	private JLabel urlLabel = new JLabel("H2 URL:");
	private JTextField urlText = new JTextField(15);
	private JLabel databaseLabel = new JLabel("H2库名:");
	private JTextField databaseField = new JTextField(15);
	private JLabel usernameLabel = new JLabel("H2用户名:");
	private JTextField usernameText = new JTextField(15);
	private JLabel passwordLabel = new JLabel("H2密码:");
	private JPasswordField passwordText = new JPasswordField(15);
	private JFrame parent;
	private MemoryDBConfiguration configuration;
	
	public H2Panel( JFrame frame ) {
		
		try {
			configuration = new MemoryDBConfiguration();
			configuration.getConnectionString();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		parent = frame;
		Box box = Box.createVerticalBox();
		setLayout(new BorderLayout());
		setBorder(new TitledBorder("实时库配置"));
		
		JPanel urlPanel = new JPanel();
		urlPanel.setLayout(new FlowLayout());
		urlPanel.setBorder(new TitledBorder("实时库地址"));
		
		urlPanel.add(urlLabel);
		urlPanel.add(urlText);
		urlText.setText(configuration.getUrl());
		
		box.add(urlPanel);
		box.add(Box.createVerticalStrut(8));
		
		JPanel databasePanel = new JPanel();
		databasePanel.setLayout(new FlowLayout());
		
		databasePanel.add(databaseLabel);
		databasePanel.add(databaseField);
		databaseField.setText(configuration.getDatabase());
		databasePanel.setBorder(new TitledBorder("实时库库名"));
		
		box.add(databasePanel);
		box.add(Box.createVerticalStrut(6));
		
		JPanel usernamePanel = new JPanel();
		usernamePanel.setLayout(new FlowLayout());
		usernameText.setText(configuration.getUsername());
		usernamePanel.setBorder(new TitledBorder("用户名"));
		
		usernamePanel.add(usernameLabel);
		usernamePanel.add(usernameText);
		
		box.add(usernamePanel);
		box.add(Box.createVerticalStrut(8));
		
		JPanel passwordPanel = new JPanel();
		passwordPanel.setLayout(new FlowLayout());
		
		passwordPanel.add(passwordLabel);
		passwordPanel.add(passwordText);
		passwordText.setText(configuration.getPassword());
		passwordPanel.setBorder(new TitledBorder("密码"));
		
		JButton testButton = new JButton("连接测试");
		JButton defaultButton = new JButton("还原默认值");
		
		TestHandler handler = new TestHandler();
		testButton.addActionListener(handler);
		
		
		JPanel testPanel = new JPanel();
		testPanel.setLayout(new FlowLayout());
		testPanel.setBorder(new TitledBorder("测试"));
		
		testPanel.add(testButton);
		testPanel.add(defaultButton);
		
		box.add(passwordPanel);
		box.add(Box.createVerticalStrut(8));
		
		box.add(testPanel);
		box.add(Box.createVerticalStrut(8));
		
		add( box, BorderLayout.CENTER);
	}
	
	class DefaultHandler implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			urlText.setText(configuration.getUrl());
			databaseField.setText(configuration.getDatabase());
			usernameText.setText(configuration.getUsername());
			passwordText.setText(configuration.getPassword());
		}
		
	}
	
	class TestHandler implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			String url = "jdbc:h2:tcp://" + urlText.getText() + "/" + databaseField.getText();
			
			try {
				Class.forName("org.h2.Driver");
				Connection conn = DriverManager.getConnection(url, usernameText.getText(), passwordText.getText());
				conn.close();
				JOptionPane.showMessageDialog(parent, "连接成功");
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				JOptionPane.showMessageDialog(parent, "连接失败");
			}
			
		}
		
	}
	
	public String getIPAddress() {
		return urlText.getText();
	}
	
	public String getDatabase() {
		return databaseField.getText();
	}
	
	public String getUsername() {
		return usernameText.getText();
	}
	
	public String getPassword() {
		return passwordText.getText();
	}
	
}
