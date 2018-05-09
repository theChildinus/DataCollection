package com.zuowenfeng.configuration.panel;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

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

import com.configuration.SustationConfiguration;
import com.configuration.WholeStationConfiguration;

public class SubstationPanel extends JPanel {
	private JRadioButton subRadio = new JRadioButton("子站");
	private JRadioButton wholeRadio = new JRadioButton("总站");
	private JFrame parent;
	private JLabel subIpLabel = new JLabel("子站IP地址");
	private JTextField subIpField = new JTextField(15);
	private JLabel subPortLabel = new JLabel("子站端口");
	private JTextField subPortField = new JTextField(15);
	private JLabel wholePortLabel = new JLabel("总站有线端口");
	private JTextField wholePortField = new JTextField(15);
	private JButton button;
	private SustationConfiguration subs;
	private WholeStationConfiguration whole;
	private JRadioButton wireButton = new JRadioButton("有线");
	private JRadioButton wirelessButton = new JRadioButton("无线");
	private JLabel wirelessLabelField = new JLabel("总站无线端口");
	private JTextField wirelessPortField = new JTextField(15);
	
	public SubstationPanel( JFrame frame ) throws IOException {
		parent = frame;
		subs = new SustationConfiguration();
		subs.getConnectionString();
		whole = new WholeStationConfiguration();
		whole.getConnectionString();
		
		wireButton.setEnabled(false);
		wirelessButton.setEnabled(false);
		
		setLayout(new BorderLayout());
		setBorder(new TitledBorder("子站转发配置"));
		button = new JButton("总站连接测试");
		
		Box box = Box.createVerticalBox();
		
		box.add(subRadio);
		box.add(Box.createVerticalStrut(20));
		
		wholeRadio.setSelected(true);
		wholePortField.setEditable(true);
		subIpField.setEditable(false);
		subPortField.setEditable(false);
		button.setEnabled(false);
		
		subRadio.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent arg0) {
				// TODO Auto-generated method stub
				if ( subRadio.isSelected() == true ) {
					wholeRadio.setSelected(false);
					subIpField.setEditable(true);
					subPortField.setEditable(true);
					wireButton.setEnabled(true);
					wirelessButton.setEnabled(true);
					wholePortField.setEditable(false);
					button.setEnabled(true);
				}
				
				else {
					wholeRadio.setSelected(true);
					subIpField.setEditable(false);
					subPortField.setEditable(false);
					wireButton.setEnabled(false);
					wirelessButton.setEnabled(false);
					wholePortField.setEditable(true);
					button.setEnabled(false);
				}
				
			}
			
		});
		
		wholeRadio.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				// TODO Auto-generated method stub
				if ( wholeRadio.isSelected() == true ) {
					subRadio.setSelected(false);
					subIpField.setEditable(false);
					subPortField.setEditable(false);
					wireButton.setEnabled(false);
					wirelessButton.setEnabled(false);
					wholePortField.setEditable(true);
					button.setEnabled(false);
				}
				
				else {
					subRadio.setSelected(true);
					subIpField.setEditable(true);
					subPortField.setEditable(true);
					wholePortField.setEditable(false);
					wireButton.setEnabled(true);
					wirelessButton.setEnabled(true);
					button.setEnabled(true);
				}
				
			}
			
		});
		
		JPanel subIpPanel = new JPanel();
		subIpPanel.setLayout(new FlowLayout());
		subIpPanel.add(subIpLabel);
		subIpPanel.add(subIpField);
		subIpField.setText(subs.getIP());
		
		box.add(subIpPanel);
		box.add(Box.createVerticalStrut(20));
		
		JPanel subPortPanel = new JPanel();
		subPortPanel.setLayout(new FlowLayout());
		subPortPanel.add(subPortLabel);
		subPortPanel.add(subPortField);
		subPortField.setText("" + subs.getPort());
		
		box.add(subPortPanel);
		box.add(Box.createVerticalStrut(20));
		
		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				try {
					Socket socket = new Socket( subIpField.getText(), Integer.parseInt(subPortField.getText()));
					PrintWriter pw = new PrintWriter(socket.getOutputStream(), true );
					pw.println(InetAddress.getLocalHost().getHostName());
					pw.close();
					socket.close();
					JOptionPane.showMessageDialog(parent, "连接成功");
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					JOptionPane.showMessageDialog(parent, "连接失败");
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					JOptionPane.showMessageDialog(parent, "连接失败");
				} catch (IOException e) {
					e.printStackTrace();
					// TODO Auto-generated catch block
					JOptionPane.showMessageDialog(parent, "连接失败");
				}
				
			}
			
		});
		
		wireButton.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				// TODO Auto-generated method stub
				if ( wireButton.isSelected() == false ) {
					wirelessButton.setSelected(true);
				}
				
				else {
					wirelessButton.setSelected(false);
				}
				
			}
			
		});
		
		wirelessButton.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				// TODO Auto-generated method stub
				if ( wirelessButton.isSelected() == false ) {
					wireButton.setSelected(true);
				}
				
				else {
					wireButton.setSelected(false);
				}
				
			}
			
		});
		
		JPanel methodSelectionPanel = new JPanel();
		methodSelectionPanel.setLayout(new FlowLayout());
		methodSelectionPanel.add(wireButton);
		methodSelectionPanel.add(wirelessButton);
		
		box.add(methodSelectionPanel);
		box.add(Box.createVerticalStrut(20));
		box.add(button);
		box.add(Box.createVerticalStrut(70));
		
		box.add(wholeRadio);
		box.add(Box.createVerticalStrut(20));
		
		JPanel wholePortPanel = new JPanel();
		wholePortPanel.setLayout(new FlowLayout());
		wholePortPanel.add(wholePortLabel);
		wholePortPanel.add(wholePortField);
		
		JPanel wholeWirelessPanel = new JPanel();
		wholeWirelessPanel.setLayout(new FlowLayout());
		wholeWirelessPanel.add(wirelessLabelField);
		wholeWirelessPanel.add(wirelessPortField);
		
		wirelessPortField.setText("" + whole.getWirelessPort());
		wholePortField.setText("" + whole.getPort());
		
		box.add(wholePortPanel);
		box.add(Box.createVerticalStrut(20));
		
		box.add(wholeWirelessPanel);
		box.add(Box.createVerticalStrut(20));
		add(box, BorderLayout.NORTH);
	}
	
	public String getSubstationIp() {
		return subIpField.getText();
	}
	
	public String getSubstationPort() {
		return subPortField.getText();
	}
	
	public String getWholeStationPort() {
		return wholePortField.getText();
	}
	
	public boolean isWholeSelected() {
		return wholeRadio.isSelected();
	}
	
	public String getWirelessPort() {
		return this.wirelessPortField.getText();
	}
	
	public boolean isWireSelected() {
		return this.wireButton.isSelected();
	}
	
	public boolean isWirelessSelected() {
		return this.wirelessButton.isSelected();
	}
	
}
