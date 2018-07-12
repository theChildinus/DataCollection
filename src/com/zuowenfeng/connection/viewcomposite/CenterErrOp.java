package com.zuowenfeng.connection.viewcomposite;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class CenterErrOp extends JDialog {
	private JLabel label1;
	private JLabel label2;
	private JComboBox combo;
	private JButton okButton;
	private String selectedIP;
	
	public CenterErrOp( JFrame owner, String title, String shutdownIP, String[] otherIPs ) {
		super( owner, title, true );
		setLayout(new BorderLayout());
		JPanel panel1 = new JPanel();
		panel1.setLayout(new BorderLayout());
		label1 = new JLabel("Client " + shutdownIP + " shutdown.");
		label2 = new JLabel("Choose another IP to alternate.");
		panel1.add(label1, BorderLayout.NORTH);
		panel1.add(label2, BorderLayout.CENTER);
		combo = new JComboBox();
		
		for ( int i = 0; i <= otherIPs.length - 1; i++ ) {
			combo.addItem(otherIPs[i]);
		}
		
		panel1.add(combo, BorderLayout.SOUTH);
		
		JPanel panel2 = new JPanel();
		panel2.setLayout(new FlowLayout());
		okButton = new JButton("Submit");
		
		okButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				selectedIP = combo.getSelectedItem().toString();
				hide();
			}
			
		});
		panel2.add(okButton);
		add(panel1, BorderLayout.NORTH);
		add(panel2, BorderLayout.SOUTH);
		show();
	}

	public String getSelectedIP() {
		return selectedIP;
	}
	
}
