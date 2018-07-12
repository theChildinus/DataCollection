package com.zuowenfeng.connection.viewcomposite;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class TimeChooseDialog extends JDialog {
	private JComboBox timeCombo = new JComboBox(new Object[]{new Integer(5), new Integer(10), new Integer(15), new Integer(20), new Integer(25) });
	private int timeChoose = 5;
	
	public TimeChooseDialog ( JFrame frame ) {
		super(frame, "时间设定", true );
		Container container = getContentPane();
		container.setLayout(new BorderLayout() );
		
		JPanel comboPanel = new JPanel();
		JLabel label = new JLabel("时间设定:");
		
		comboPanel.setLayout(new FlowLayout());
		comboPanel.add(label);
		comboPanel.add(timeCombo);
		container.add(comboPanel, BorderLayout.CENTER);
		JPanel panel = new JPanel();
		JButton submit = new JButton("确定");
		JButton cancel = new JButton("取消");
		
		submit.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				timeChoose = (Integer)timeCombo.getSelectedItem();
				setVisible(false);
			}
			
		});
		
		cancel.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				setVisible(false);
			}
			
		});
		
		panel.setLayout(new FlowLayout() );
		panel.add(submit);
		panel.add(cancel);
		
		container.add(panel, BorderLayout.SOUTH);
		setSize(200, 100);
		setLocationRelativeTo(frame);
		setVisible(true);
	}
	
	public int getTimeChoose() {
		return timeChoose;
	}
	
	public static void main ( String[] args ) {
		TimeChooseDialog dialog = new TimeChooseDialog(null);
	}
}
