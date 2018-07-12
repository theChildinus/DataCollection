package com.zuowenfeng.configuration.panel;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.configuration.OPCConfiguration;

public class OPCPanel extends JPanel {
	private JRadioButton opcCombo = new JRadioButton("OPC");
	private JLabel opcAddress = new JLabel("OPC地址");
	private JTextField opcAddressField = new JTextField(10);
	private JButton button = new JButton("测试");
	private JLabel deviceLabel = new JLabel("设备地址");
	private JTextField deviceField = new JTextField(15);
	private JLabel opcLabel = new JLabel("间隔时间");
	private JTextField opcText = new JTextField(5);
	private JFrame parent;
	private OPCConfiguration configuration;
	
	public OPCPanel( JFrame frame ) {
		parent = frame;
		Box box = Box.createVerticalBox();
		setLayout(new BorderLayout());
		setBorder(new TitledBorder("OPC配置"));
		
		try {
			configuration = new OPCConfiguration();
			configuration.getOPCConnection();
			opcAddressField.setText(configuration.getDirectory());
			deviceField.setText(configuration.getDeviceid());
			opcText.setText(configuration.getOpcTime());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		box.add(Box.createVerticalStrut(60));
		
		JPanel opcPanel = new JPanel();
		opcPanel.setLayout(new FlowLayout());
		opcPanel.add(opcCombo);
		opcAddressField.setEditable(false);
		deviceField.setEditable(false);
		button.setEnabled(false);
		
		opcCombo.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent arg0) {
				// TODO Auto-generated method stub
				if ( !opcCombo.isSelected() ) {
					opcAddressField.setEditable(false);
					deviceField.setEditable(false);
					button.setEnabled(false);
					opcText.setEnabled(false);
				}
				
				else {
					opcAddressField.setEditable(true);
					deviceField.setEditable(true);
					button.setEnabled(true);
					opcText.setEnabled(true);
				}
				
			}
			
		});
		//opcPanel.setAlignmentX(RIGHT_ALIGNMENT);
		box.add(opcPanel);
		box.add(Box.createVerticalStrut(40));
		
		JPanel opcAddrPanel = new JPanel();
		opcAddrPanel.setLayout(new FlowLayout());
		//opcAddrPanel.setAlignmentX(RIGHT_ALIGNMENT);
		opcAddrPanel.add(opcAddress);
		opcAddrPanel.add(opcAddressField);
		opcAddrPanel.add(button);
		
		button.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				String directory = opcAddressField.getText();
				
				try {
					FileInputStream file = new FileInputStream(directory);
					file.close();
					JOptionPane.showMessageDialog(parent, "文件存在");
				} 
				
				catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					JOptionPane.showMessageDialog(parent, "文件不存在");
				} 
				
				catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
		});
			
		JPanel devicePanel = new JPanel();
		//devicePanel.setAlignmentX(RIGHT_ALIGNMENT);
		devicePanel.setLayout(new FlowLayout());
		devicePanel.add(deviceLabel);
		devicePanel.add(deviceField);
		
		box.add(opcAddrPanel);
		box.add(Box.createVerticalStrut(40));
		
		box.add(devicePanel);
		box.add(Box.createVerticalStrut(40));
		
		JPanel timePanel = new JPanel();
		timePanel.setLayout(new FlowLayout());
		
		timePanel.add(opcLabel);
		timePanel.add(opcText);
		
		box.add(timePanel);
		box.add(Box.createVerticalStrut(60));
		add(box, BorderLayout.NORTH);
	}
	
	public String getOpcTime() {
		return this.opcText.getText();
	}
	
	public String getDirectory() {
		return this.opcAddressField.getText();
	}
	
	public String getDeviceid() {
		return this.deviceField.getText();
	}
	
	public boolean isOpcSelected() {
		return this.opcCombo.isSelected();
	}
	
}
