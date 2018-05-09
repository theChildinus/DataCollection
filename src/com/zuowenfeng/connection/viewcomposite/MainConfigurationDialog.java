package com.zuowenfeng.connection.viewcomposite;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.factory.ServicemixConfFactory;
import com.zuowenfeng.configuration.panel.H2Panel;
import com.zuowenfeng.configuration.panel.MysqlPanel;
import com.zuowenfeng.configuration.panel.OPCPanel;
import com.zuowenfeng.configuration.panel.PublishPanel;
import com.zuowenfeng.configuration.panel.SubstationPanel;

public class MainConfigurationDialog extends JDialog {
	private Container container;
	private MysqlPanel mysqlClient;
	private H2Panel h2Client;
	private PublishPanel publishClient;
	private SubstationPanel substationClient;
	private OPCPanel opcClient;
	
	public MainConfigurationDialog( JFrame frame ) throws IOException {
		super( frame, "程序配置", true );
		container = getContentPane();
		container.setLayout(new BorderLayout());
		JPanel panel1 = new JPanel();
		panel1.setLayout(new GridLayout(1,1));
		JTabbedPane pane = new JTabbedPane(JTabbedPane.TOP);
		mysqlClient = new MysqlPanel( frame );
		h2Client = new H2Panel( frame );
		publishClient = new PublishPanel( frame );
		substationClient = new SubstationPanel( frame );
		opcClient = new OPCPanel( frame );
		
		pane.add(mysqlClient, "数据库配置");
		pane.add(h2Client, "实时库配置");
		pane.add(publishClient, "发布订阅配置");
		pane.add(substationClient, "子站转发配置");
		pane.add(opcClient, "OPC配置");
		
		container.add(pane, BorderLayout.CENTER);
		
		JButton submitButton = new JButton("确定");
		JButton cancelButton = new JButton("取消");
		
		submitButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				ArrayList<String> columns = new ArrayList<String> ();
				ArrayList<String> values = new ArrayList<String> ();
				columns.add("DeviceUrl");
				values.add(mysqlClient.getIPAddress());
				columns.add("DevicePort");
				values.add(mysqlClient.getPort());
				columns.add("DeviceDatabase");
				values.add(mysqlClient.getDatabase());
				columns.add("DeviceUsername");
				values.add(mysqlClient.getUsername());
				columns.add("DevicePassword");
				values.add(mysqlClient.getPassword());
				columns.add("MemoryUrl");
				values.add(h2Client.getIPAddress());
				columns.add("MemoryDatabase");
				values.add(h2Client.getDatabase());
				columns.add("MemoryUsername");
				values.add(h2Client.getUsername());
				columns.add("MemoryPassword");
				values.add(h2Client.getPassword());
				columns.add("AnaAlarmUrl");
				values.add(publishClient.getLocalIPAddress());
				columns.add("AnaAlarmPort");
				values.add("9001");
				columns.add("AnaAlarmServicename");
				values.add("INotificationProcess");
				columns.add("SwitchAlarmUrl");
				values.add(publishClient.getLocalIPAddress());
				columns.add("SwitchAlarmPort");
				values.add("9001");
				columns.add("SwitchAlarmServicename");
				values.add("INotificationProcess");
				columns.add("DeviceAlarmUrl");
				values.add(publishClient.getLocalIPAddress());
				columns.add("DeviceAlarmPort");
				values.add("9001");
				columns.add("DeviceAlarmServicename");
				values.add("INotificationProcess");
				columns.add("ClientAlarmUrl");
				values.add(publishClient.getLocalIPAddress());
				columns.add("ClientAlarmPort");
				values.add("9001");
				columns.add("ClientAlarmServicename");
				values.add("INotificationProcess");
				columns.add("CommandUrl");
				values.add(publishClient.getLocalIPAddress());
				columns.add("CommandPort");
				values.add("9001");
				columns.add("CommandServicename");
				values.add("INotificationProcess");
				columns.add("UpdateUrl");
				values.add(publishClient.getLocalIPAddress());
				columns.add("UpdatePort");
				values.add("9001");
				columns.add("UpdateServicename");
				values.add("INotificationProcess");
				columns.add("NotifyUrl");
				values.add(publishClient.getNotifyIPAddress());
				columns.add("NotifyPort");
				values.add(publishClient.getNotifyPort());
				columns.add("SubscribeUrl");
				values.add(publishClient.getSubscribeIPAddress());
				columns.add("SubscribePort");
				values.add(publishClient.getSubscribePort());
				columns.add("SubstationUrl");
				values.add(substationClient.getSubstationIp());
				columns.add("SubstationPort");
				values.add(substationClient.getSubstationPort());
				columns.add("WholestationPort");
				values.add(substationClient.getWholeStationPort() );
				columns.add("WirelessStationPort");
				values.add(substationClient.getWirelessPort());
				columns.add("OPCDirectory");
				values.add(opcClient.getDirectory());
				columns.add("OPCDevice");
				values.add(opcClient.getDeviceid());
				columns.add("OPCTime");
				values.add(opcClient.getOpcTime());
				
				try {
					mysqlClient.getConfiguration().updateConf(columns, values);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				setVisible(false);
			}
			
		});
		
		cancelButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				setVisible(false);
			}
			
		});
		
		JPanel buttonPanel = new JPanel();
		
		buttonPanel.setLayout(new FlowLayout());
		
		buttonPanel.add(submitButton);
		buttonPanel.add(cancelButton);
		
		container.add(buttonPanel, BorderLayout.SOUTH);
		setSize(600, 600);
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	public boolean isWholeStation() {
		return substationClient.isWholeSelected();
	}
	
	public boolean isOpcSelected() {
		return opcClient.isOpcSelected();
	}
	
	public boolean isSubWireSelected() {
		return substationClient.isWireSelected();
	}
	
	public boolean isSubWirelessSelected() {
		return substationClient.isWirelessSelected();
	}
	
	public static void main ( String[] args ) throws IOException {
		ServicemixConfFactory factory = new ServicemixConfFactory();
		factory.createServicemixConfInstance();
		MainConfigurationDialog dialog = new MainConfigurationDialog(null);
	}
}
