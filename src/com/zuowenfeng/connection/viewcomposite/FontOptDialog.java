package com.zuowenfeng.connection.viewcomposite;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class FontOptDialog extends JDialog {
	private JComboBox styleList;
	private JComboBox sizeList;
	private JComboBox colorList;
	private JCheckBox boldItem;
	private JCheckBox italicItem;
	private JButton submitButton;
	private JButton cancelButton;
	private String fontChoose = "宋体";
	private int sizeChoose = 11;
	private String colorChoose = "";
	private boolean bold = false;
	private boolean italic = false;
	private static ResourceBundle res = ResourceBundle.getBundle("com.zuowenfeng.conf.MyResource", Locale.CHINESE);
	
	public FontOptDialog( JFrame owner, String inputStyle, int inputSize, String inputColor, boolean inputBold, boolean inputItalic ) {
		super( owner, res.getString("font"), true );
		setLayout(new BorderLayout());
		JPanel panel1 = new JPanel();
		JPanel panel2 = new JPanel();
		panel1.setLayout(new BorderLayout());
		
		JPanel panel3 = new JPanel();
		JLabel styleLabel = new JLabel(res.getString("style"));
		panel3.setLayout(new FlowLayout());
		//GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		final String[] fontList = {"宋体","黑体", "华文楷体", "华文隶书","华文宋体","楷体_GB2312", "隶书"};
		styleList = new JComboBox( fontList );
		
		if ( !inputStyle.equals("") ) {
			
			for ( int i = 0; i <= fontList.length - 1; i++ ) {
				
				if ( fontList[ i ].equals(inputStyle) ) {
					styleList.setSelectedIndex(i);
					break;
				}
			}

		}
		panel3.add(styleLabel);
		panel3.add(styleList);
		
		JPanel panel4 = new JPanel();
		panel4.setLayout(new FlowLayout());
		JLabel sizeLabel = new JLabel(res.getString("size"));
		final Integer[] sizeArray = { new Integer(11), new Integer(12), 
								new Integer(14), new Integer(16), 
								new Integer(18), new Integer(20), 
								new Integer(22), new Integer(24), 
								new Integer(26), new Integer(28) };
		
		sizeList = new JComboBox( sizeArray );
		
		if ( inputSize != 0 ) {
			
			for ( int i = 0; i <= sizeArray.length - 1; i++ ) {
				
				if ( sizeArray[ i ] == inputSize ) {
					sizeList.setSelectedIndex(i);
					break;
				}
				
			}
		}
		panel4.add(sizeLabel);
		panel4.add(sizeList);
		
		JPanel panel5 = new JPanel();
		panel5.setLayout(new FlowLayout());
		JLabel colorLabel = new JLabel(res.getString("color"));
		final String[] colorArray = {"Red", "Blue", "Black", "green"};
		colorList = new JComboBox( colorArray );
		
		if ( !inputColor.equals("") ) {
			
			for ( int i = 0; i <= colorArray.length - 1; i++ ) {
				
				if ( colorArray[ i ].equals(inputColor) ) {
					colorList.setSelectedIndex(i);
					break;
				}
				
			}
			
		}
		
		panel5.add(colorLabel);
		panel5.add(colorList);
		
		panel1.add(panel3, BorderLayout.NORTH);
		panel1.add(panel4, BorderLayout.CENTER);
		panel1.add(panel5, BorderLayout.SOUTH);
		
		panel2.setLayout(new FlowLayout());
		boldItem = new JCheckBox(res.getString("bold"));
		italicItem = new JCheckBox(res.getString("italic"));
		
		boldItem.setSelected(inputBold);
		italicItem.setSelected(inputItalic);
		panel2.add(boldItem);
		panel2.add(italicItem);
		
		JPanel panel6 = new JPanel();
		panel6.setLayout(new FlowLayout());
		submitButton = new JButton(res.getString("ok"));
		cancelButton = new JButton(res.getString("cancel"));
		
		submitButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				fontChoose = fontList[ styleList.getSelectedIndex()];
				sizeChoose = sizeArray[ sizeList.getSelectedIndex()];
				colorChoose = colorArray[ colorList.getSelectedIndex()];
				bold = boldItem.isSelected();
				italic = italicItem.isSelected();
				hide();
			}
			
		});
		
		cancelButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				fontChoose = "";
				hide();
			}
			
		});
		
		panel6.add(submitButton);
		panel6.add(cancelButton);
		add(panel1, BorderLayout.NORTH);
		add(panel2, BorderLayout.CENTER);
		add(panel6, BorderLayout.SOUTH);
		setSize(300, 200 );
		setLocationRelativeTo(owner);
		show();
	}
	
	public String getFontChoose() {
		return this.fontChoose;
	}
	
	public int getSizeChoose() {
		return this.sizeChoose;
	}
	
	public String getColorChoose() {
		return this.colorChoose;
	}
	
	public boolean getBoldChoose() {
		return this.bold;
	}
	
	public boolean getItalicChoose() {
		return this.italic;
	}
	
	public static void main ( String[] args ) {
		//FontOptDialog a = new FontOptDialog(null);
	}
}
