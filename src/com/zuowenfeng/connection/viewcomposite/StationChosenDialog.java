package com.zuowenfeng.connection.viewcomposite;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class StationChosenDialog extends JDialog {
	private ArrayList<String> stationList;
	private String[] lists;
	private String chosen;
	private Container container;
	private JLabel stationLabel;
	private JComboBox stationChosenList;
	private JButton submitButton;
	private String title;
	
	public StationChosenDialog( JFrame owner, ArrayList<String> list ) {
		super( owner, "场站选择", true );
		stationList = list;
		container = getContentPane();
		container.setLayout(new BoxLayout( container, BoxLayout.Y_AXIS));
		stationLabel = new JLabel("场站选择:");
		JPanel stationLabelPanel = new JPanel();
		stationLabelPanel.setLayout(new FlowLayout());
		stationLabelPanel.add(stationLabel);
		
		lists = new String[stationList.size()];
		
		for ( int i = 0; i <= stationList.size() - 1; i++ ) {
			lists[ i ] = stationList.get(i);
		}
		
		stationChosenList = new JComboBox(lists);
		stationLabelPanel.add(stationChosenList);
		container.add(stationLabelPanel);
		JPanel submitPanel = new JPanel();
		submitPanel.setLayout(new FlowLayout());
		submitButton = new JButton("确定");
		
		submitButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				chosen = (String)stationChosenList.getSelectedItem();
				title = chosen;
				setVisible(false);
			}
			
		});
		
		submitPanel.add(submitButton);
		container.add(submitPanel);
		setSize(200, 100 );
		setLocationRelativeTo(owner);
		setVisible(true);
	}
	
	public String getTitle() {
		return this.title;
	}
	
	public String getChosen() {
		return this.chosen;
	}
	
	public static void main ( String[] args ) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
		StationChosenDialog dialog = new StationChosenDialog(null, new ArrayList<String>());
		System.out.println(dialog.getChosen());
	}
}
