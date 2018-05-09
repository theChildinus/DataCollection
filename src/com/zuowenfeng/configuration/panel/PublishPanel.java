package com.zuowenfeng.configuration.panel;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.net.Inet4Address;
import java.net.UnknownHostException;

import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import com.configuration.NotifyConfiguration;
import com.factory.ServicemixConfFactory;

public class PublishPanel extends JPanel {
	private JLabel localLabel = new JLabel("本机IP地址:");
	private JLabel localField = new JLabel();
	private JLabel publishLabel = new JLabel("发布IP地址");
	private JTextField publishText = new JTextField(15);
	private JLabel portLabel = new JLabel("发布端口");
	private JTextField portText = new JTextField(15);
	private JLabel subscribeLabel = new JLabel("订阅IP地址");
	private JTextField subscribeText = new JTextField(15);
	private JLabel subscribePort = new JLabel("订阅端口");
	private JTextField subscribePortText = new JTextField(15);
	private JFrame panel;
	
	public PublishPanel( JFrame frame ) {
		panel = frame;
		setLayout(new BorderLayout());
		Box box = Box.createVerticalBox();
		setBorder(new TitledBorder("发布订阅配置"));
		
		JPanel localPanel = new JPanel();
		localPanel.setLayout(new FlowLayout());
		localPanel.setBorder(new TitledBorder("本机IP"));
		localPanel.add(localLabel);
		localPanel.add(localField);
		
		try {
			localField.setText(Inet4Address.getLocalHost().getHostAddress());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		box.add(localPanel);
		box.add(Box.createVerticalStrut(8));
		
		JPanel publishIpPanel = new JPanel();
		publishIpPanel.setLayout(new FlowLayout());
		publishIpPanel.setBorder(new TitledBorder("发布的IP地址"));
		publishText.setText(ServicemixConfFactory.conf2.getUrl());
		
		publishIpPanel.add(publishLabel);
		publishIpPanel.add(publishText);
		
		box.add(publishIpPanel);
		box.add(Box.createVerticalStrut(6));
		
		JPanel publishPortPanel = new JPanel();
		publishPortPanel.setLayout(new FlowLayout());
		publishPortPanel.setBorder(new TitledBorder("发布的端口"));
		
		publishPortPanel.add(portLabel);
		publishPortPanel.add(portText);
		portText.setText("" + ServicemixConfFactory.conf2.getPort());
		
		box.add(publishPortPanel);
		box.add(Box.createVerticalStrut(6));
		
		JPanel subscribeIpPanel = new JPanel();
		subscribeIpPanel.setLayout(new FlowLayout());
		subscribeIpPanel.setBorder(new TitledBorder("订阅的IP地址"));
		subscribeIpPanel.add(subscribeLabel);
		subscribeIpPanel.add(subscribeText);
		subscribeText.setText(ServicemixConfFactory.conf.getUrl());
		
		box.add(subscribeIpPanel);
		box.add(Box.createVerticalStrut(6));
		
		JPanel subscribePortPanel = new JPanel();
		subscribePortPanel.setLayout(new FlowLayout());
		subscribePortPanel.setBorder(new TitledBorder("订阅的端口"));
		subscribePortPanel.add(subscribePort);
		subscribePortPanel.add(subscribePortText);
		subscribePortText.setText("" + ServicemixConfFactory.conf.getPort());
		box.add(subscribePortPanel);
		box.add(Box.createVerticalStrut(6));
		add( box, BorderLayout.CENTER);
	}
	
	public String getLocalIPAddress() {
		return localField.getText();
	}
	
	public String getNotifyIPAddress() {
		return publishText.getText();
	}
	
	public String getNotifyPort() {
		return portText.getText();
	}
	
	public String getSubscribeIPAddress() {
		return subscribeText.getText();
	}
	
	public String getSubscribePort() {
		return subscribePortText.getText();
	}
}
