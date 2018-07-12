package com.zuowenfeng.connection.viewcomposite;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.TitledBorder;

import com.zuowenfeng.message.AnalyzedMessage;
import com.zuowenfeng.message.DownAnalyzedMessage;
import com.zuowenfeng.message.DownMessage;
import com.zuowenfeng.message.RawMessage;
import com.zuowenfeng.message.UpMessage;
import com.zuowenfeng.monitor.monitorDAO.StationDAO;

public class MsgShowUI extends JFrame {
	private JTextPane MsgDisplayArea;
	private JButton pauseButton;
	private JButton refreshButton;
	private JMenuItem saveMenuItem;
	private JMenuItem exitMenuItem;
	private JSplitPane splitpane;
	private JMenuItem fontMenuItem;
	//private JCheckBoxMenuItem boldItem;
	//private JCheckBoxMenuItem italicItem;
	private JMenuItem stationItem;
	private JScrollBar bar;
	private String areas = "";
	private boolean states = true;
	private int length = 69;
	private JTextArea sendArea;
	private FontMetrics metrics;
	private ResourceBundle res = ResourceBundle.getBundle("com.zuowenfeng.conf.MyResource_zh", Locale.CHINESE);
	private JFrame frame;
	private String defaultStyle = "";
	private int defaultSize = 0;
	private String defaultColor = "";
	private boolean defaultBold = false;
	private boolean defaultItalic = false;
	private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
	private ArrayList<UpMessage> upMessages = new ArrayList<UpMessage> ();
	private ArrayList<DownMessage> downMessages = new ArrayList<DownMessage> ();
	private JScrollBar bar2;
	private ArrayList<String> lists = new ArrayList<String> ();
	private String chosenStation = "";
	private StationChosenDialog dialogs;
	private ArrayList<UpMessage> selectedUp = new ArrayList<UpMessage> ();
	private ArrayList<DownMessage> selectedDown = new ArrayList<DownMessage> ();
	
	public MsgShowUI() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, SQLException, IOException {
		setLayout(new BorderLayout());
		setTitle(res.getString("messagewindow"));
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu(res.getString("operation"));
		JMenu EditMenu = new JMenu(res.getString("edit"));
		StationDAO dao = new StationDAO();
		ResultSet set = dao.getAssignedDistinctResult("device_id");
		
		while ( set.next() ) {
			lists.add(set.getString("device_id"));
		}
		
		dao.closeConnection();
		frame = this;
		//boldItem = new JCheckBoxMenuItem(res.getString("bold"));

		/*boldItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				if ( MsgDisplayArea.getFont().isBold() ) {
					int style = MsgDisplayArea.getFont().getStyle() - Font.BOLD;
					MsgDisplayArea.setFont(MsgDisplayArea.getFont().deriveFont(style));
				}
				
				else {
					int style = MsgDisplayArea.getFont().getStyle() + Font.BOLD;
					MsgDisplayArea.setFont(MsgDisplayArea.getFont().deriveFont(style));
				}
				
			}
			
		});*/
		
		/*italicItem = new JCheckBoxMenuItem(res.getString("italic"));
		
		italicItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				if ( MsgDisplayArea.getFont().isItalic() ) {
					int style = MsgDisplayArea.getFont().getStyle() - Font.ITALIC;
					MsgDisplayArea.setFont(MsgDisplayArea.getFont().deriveFont(style));
				}
				
				else {
					int style = MsgDisplayArea.getFont().getStyle() + Font.ITALIC;
					MsgDisplayArea.setFont(MsgDisplayArea.getFont().deriveFont(style));
				}
				
			}
			
		});*/
		
		fontMenuItem = new JMenuItem(res.getString("font"));
		//fontMenuItem.add(boldItem);
		//fontMenuItem.add(italicItem);
		EditMenu.add(fontMenuItem);

		stationItem = new JMenuItem(res.getString("station"));
		
		stationItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				dialogs = new StationChosenDialog(frame, lists);
				SwingUtilities.updateComponentTreeUI(dialogs);
				chosenStation = dialogs.getChosen();
				
				if ( chosenStation == null ) {
					chosenStation = "";
				}
				
				String changeTitle = dialogs.getTitle();
				
				if ( changeTitle == null ) {
					changeTitle = "";
				}
				
				setTitle(res.getString("messagewindow") + ": 场站" + changeTitle );
				System.out.println(chosenStation); 
				
				if ( chosenStation == "") {
					
					for ( int i = upMessages.size() - 5; i <= upMessages.size() - 1; i++ ) {
						selectedUp.add(upMessages.get(i));
					}
					
					for ( int i = downMessages.size() - 5; i <= downMessages.size() - 1; i++ ) {
						selectedDown.add(downMessages.get(i));
					}
					
				}
				
				else {
					
					for ( int i = 0; i <= upMessages.size() - 1; i++ ) {
						
						if ( upMessages.get(i).getDeviceid().equals(chosenStation)) {
							selectedUp.add(upMessages.get(i));
							
							if ( selectedUp.size() == 6 ) {
								selectedUp.remove(0);
							}
							
						}
						
					}
					
					for ( int i = 0; i <= downMessages.size() - 1; i++ ) {
						
						if ( downMessages.get(i).getDeviceid().equals(chosenStation)) {
							selectedDown.add(downMessages.get(i));
							
							if ( selectedDown.size() == 6 ) {
								selectedDown.remove(0);
							}
							
						}
						
					}
				}
			}
			
		});
		
		saveMenuItem = new JMenuItem(res.getString("save"));
		exitMenuItem = new JMenuItem(res.getString("exit"));
		
		fontMenuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				FontOptDialog fod = new FontOptDialog(frame, defaultStyle, defaultSize, defaultColor, defaultBold, defaultItalic );
				String style = fod.getFontChoose();
				int size = fod.getSizeChoose();
				String color = fod.getColorChoose();
				boolean bold = fod.getBoldChoose();
				boolean italic = fod.getItalicChoose();
				int styles = 0;
				
				if ( style.equals("")) {
					return;
				}
				
				defaultStyle = style.substring(0, style.length());
				defaultSize = size;
				defaultColor = color.substring(0, color.length());
				defaultBold = bold;
				defaultItalic = italic;
				
				if ( bold == true ) {
					styles += Font.BOLD;
				}
				
				if ( italic == true ) {
					styles += Font.ITALIC;
				}
				
				Font font = new Font( style, styles, size );				
				font.deriveFont(styles);
				
				MsgDisplayArea.setFont(font);
				sendArea.setFont(font);
				
				if ( color.equals("Red")) {
					MsgDisplayArea.setForeground(Color.RED);
					sendArea.setForeground(Color.red);
				}
				
				else if ( color.equals("Green")) {
					MsgDisplayArea.setForeground(Color.GREEN);
					sendArea.setForeground(Color.green);
				}
				
				else if ( color.equals("Blue")) {
					MsgDisplayArea.setForeground(Color.BLUE);
					sendArea.setForeground(Color.blue);
				}
				
				else if ( color.equals("Black")) {
					MsgDisplayArea.setForeground(Color.BLACK);
					sendArea.setForeground(Color.black);
				}
				
				metrics = MsgDisplayArea.getFontMetrics(MsgDisplayArea.getFont());
				MsgDisplayArea.setText(change(length));
				sendArea.setText(changeDown(length));
			}
			
		});

		fileMenu.add(stationItem);
		fileMenu.add(saveMenuItem);
		fileMenu.add(exitMenuItem);

		menuBar.add(fileMenu);
		menuBar.add(EditMenu);
		//menuBar.add(stationItem);
		add( menuBar, BorderLayout.NORTH);
		
		JPanel panel1 = new JPanel();
		GridLayout layout = new GridLayout(1,1);
		panel1.setLayout(new BorderLayout());
		JPanel panel4 = new JPanel();
		panel4.setBorder(new TitledBorder(res.getString("receive")));
		panel4.setLayout(layout);
		MsgDisplayArea = new JTextPane();
		JScrollPane pane = new JScrollPane(MsgDisplayArea);
		bar = pane.getVerticalScrollBar();
		MsgDisplayArea.setCaretPosition(MsgDisplayArea.getText().length());
		pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		panel4.add(pane);
		panel1.add(panel4);
		JPanel panel3 = new JPanel();
		panel3.setBorder(new TitledBorder(res.getString("send")));
		
		sendArea = new JTextArea(10, 10);
		sendArea.setBorder(new TitledBorder(""));
		sendArea.setCaretPosition(sendArea.getText().length());
		panel3.setLayout(new BorderLayout());
		JButton okButton = new JButton(res.getString("ok"));
		
		okButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				sendArea.setText("");
			}
			
		});
		
		JButton cancelButton = new JButton(res.getString("cancel"));
		
		cancelButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				sendArea.setText("");
			}
			
		});
		
		JPanel panel5 = new JPanel();
		panel5.setLayout(new FlowLayout());
		
		panel5.add(okButton);
		panel5.add(cancelButton);
		JPanel panel6 = new JPanel();
		panel6.setLayout(new GridLayout(1,1));
		JScrollPane pane2 = new JScrollPane(sendArea);
		bar2 = pane2.getVerticalScrollBar();
		pane2.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		panel6.add(pane2);
		panel3.add(panel6, BorderLayout.CENTER);
		panel3.add(panel5, BorderLayout.SOUTH);
		//MsgDisplayArea.setCaretPosition(MsgDisplayArea.getDocument().getLength());
		//sendArea.setCaretPosition(sendArea.getDocument().getLength());
		sendArea.setLineWrap(true);
		JPanel panel2 = new JPanel();
		panel2.setLayout( new FlowLayout() );
		pauseButton = new JButton(res.getString("pause"));
		refreshButton = new JButton(res.getString("refresh"));
		
		pauseButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				states = (!states);
				
				if ( pauseButton.getLabel().equals(res.getString("pause"))) {
					pauseButton.setLabel(res.getString("resume"));
				}
				
				else {
					pauseButton.setLabel(res.getString("pause"));
				}
				
			}
			
		});
		
		refreshButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				areas = "";
				MsgDisplayArea.setText("");
			}
			
		});
		
		this.addComponentListener(new ComponentAdapter() {
			
			public void componentResized( ComponentEvent evt ) {
				length = getWidth() - 52; 
				
				MsgDisplayArea.setText(change(length));
				sendArea.setText(changeDown(length));
				//bar.setValue(bar.getMaximum());
			}
		});
		
		panel2.add(pauseButton);
		panel2.add(refreshButton);
		panel1.add(panel2, BorderLayout.SOUTH);
		splitpane = new JSplitPane( JSplitPane.VERTICAL_SPLIT, panel1, panel3 );
		add( splitpane, BorderLayout.CENTER);
		metrics = MsgDisplayArea.getFontMetrics(MsgDisplayArea.getFont());
		setSize( 500, 500 );
		//System.out.println(MsgDisplayArea.getWidth());
		splitpane.setDividerLocation((int)( getHeight() / 2 ) );
		
		setLocationRelativeTo(null);
		UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
		// show();
		setVisible(true);
	}
	
	public String changeDown( int length ) {
		String result = "";
		int rowLocation = 0;
		
		for ( int i = 0; i <= selectedDown.size() - 1; i++ ) {
			result = result.concat("解析前:\n");
			result = result.concat(selectedDown.get(i).getRawMessage().getDate() + ":");
			rowLocation = metrics.stringWidth(selectedDown.get(i).getRawMessage().getDate() + ":");
			
			for ( int j = 0; j <= selectedDown.get(i).getRawMessage().getContent().size() - 1; j++ ) {
				
				if ( rowLocation + metrics.stringWidth(selectedDown.get(i).getRawMessage().getContent().get(j)) < length ) {
					result = result.concat(selectedDown.get(i).getRawMessage().getContent().get(j));
					rowLocation += metrics.stringWidth(selectedDown.get(i).getRawMessage().getContent().get(j));
				}
				
				else {
					result = result.concat("\n");
					result = result.concat(selectedDown.get(i).getRawMessage().getContent().get(j));
					rowLocation = metrics.stringWidth(selectedDown.get(i).getRawMessage().getContent().get(j));
				}
				
				if ( ( j + 1 ) % 6 == 0 ) {
					
					if ( rowLocation + metrics.stringWidth("   ") < length ) {
						result = result.concat("   ");
						rowLocation += metrics.stringWidth("   ");
					}
					
					else {
						result = result.concat("\n");
						rowLocation = 0;
					}
					
				}
				
				else {
					
					if ( rowLocation + metrics.stringWidth(" ") < length ) {
						result = result.concat(" ");
						rowLocation += metrics.stringWidth(" ");
					}
					
					else {
						result = result.concat("\n");
						rowLocation = 0;
					}
					
				}
				
			} 
			
			result = result.concat("\n\n");
			rowLocation = 0;
			
			if ( selectedDown.get(i).getDownAnalyzedMessage() != null ) {
				result = result.concat(selectedDown.get(i).getDownAnalyzedMessage().getDeviceid() + "\n");
				result = result.concat(selectedDown.get(i).getDownAnalyzedMessage().getPlcid() + "\n");
				result = result.concat(selectedDown.get(i).getDownAnalyzedMessage().getSensorid() + "\n");
				result = result.concat(selectedDown.get(i).getDownAnalyzedMessage().getValue() + "\n");
				result = result.concat(selectedDown.get(i).getDownAnalyzedMessage().getDowntype() + "\n");
				result = result.concat("\n");
			}
			
		}
		
		return result;
	}
	
	public void showTexts() {
		
		if ( states == true ) {
			MsgDisplayArea.setText(change( length ));
			sendArea.setText(changeDown(length));
			MsgDisplayArea.setCaretPosition(MsgDisplayArea.getText().length());
			sendArea.setCaretPosition(sendArea.getText().length());
			//bar.setValue(bar.getMaximum());
			//bar2.setValue(bar2.getMaximum());
		}
		
	}
	
	public void setUpMessage(UpMessage msg ) {
		upMessages.add(msg);
		
		if ( upMessages.size() == 100 ) {
			upMessages.remove(0);
		}
		
		if ( chosenStation == "" ) {
			selectedUp.add(msg);
			
			if ( selectedUp.size() == 6 ) {
				selectedUp.remove(0);
			}
			
		}
		
		else if ( chosenStation.equals(msg.getDeviceid())) {
			
			selectedUp.add(msg);
			
			if ( selectedUp.size() == 6 ) {
				selectedUp.remove(0);
			}
		}
	}
	
	public void setDownMessage(DownMessage msg ) {
		downMessages.add(msg);
		
		if ( downMessages.size() == 100 ) {
			downMessages.remove(0);
		}
		
		if ( chosenStation == "" ) {
			selectedDown.add(msg);
			
			if ( selectedDown.size() == 6 ) {
				selectedDown.remove(0);
			}
			
		}
		
		else if ( chosenStation.equals(msg.getDeviceid())) {
			
			selectedDown.add(msg);
			
			if ( selectedDown.size() == 6 ) {
				selectedDown.remove(0);
			}
		}
		
	}
	
	public String change( int length ) {
		String result = "";
		int rowLocation = 0;
		
		for ( int i = 0; i <= selectedUp.size() - 1; i++ ) {
			result = result.concat("解析前:\n");
			result = result.concat(selectedUp.get(i).getRawMessage().getDate() + ":");
			rowLocation += metrics.stringWidth(selectedUp.get(i).getRawMessage().getDate() + ":");
			
			for ( int j = 0; j <= selectedUp.get(i).getRawMessage().getContent().size() - 1; j++ ) {
				
				if ( rowLocation + metrics.stringWidth(selectedUp.get(i).getRawMessage().getContent().get(j)) < length ) {
					result = result.concat(selectedUp.get(i).getRawMessage().getContent().get(j));
					rowLocation += metrics.stringWidth(selectedUp.get(i).getRawMessage().getContent().get(j));
				}
				
				else {
					result = result.concat("\n");
					result = result.concat(selectedUp.get(i).getRawMessage().getContent().get(j));
					rowLocation = metrics.stringWidth(selectedUp.get(i).getRawMessage().getContent().get(j));
				}
				
				if ( ( j + 1 ) % 6 == 0 ) {
					
					if ( rowLocation + metrics.stringWidth("   ") < length ) {
						result = result.concat("   ");
						rowLocation += metrics.stringWidth("   ");
					}
					
					else {
						result = result.concat("\n");
						rowLocation = 0;
					}
					
				}
				
				else {
					
					if ( rowLocation + metrics.stringWidth(" ") < length ) {
						result = result.concat(" ");
						rowLocation += metrics.stringWidth(" ");
					}
					
					else {
						result = result.concat("\n");
						rowLocation = 0;
					}
					
				}
				
			}
			
			result = result.concat("\n\n");
			rowLocation = 0;
			
			if ( selectedUp.get(i).getAnalyzedMessage() != null ) {
				result = result.concat("解析后:\n");
				result = result.concat(selectedUp.get(i).getAnalyzedMessage().getDeviceid() + "\n");
				result = result.concat(selectedUp.get(i).getAnalyzedMessage().getPlcid() + "\n");
				result = result.concat(selectedUp.get(i).getAnalyzedMessage().getSensorid() + "\n");
				result = result.concat(selectedUp.get(i).getAnalyzedMessage().getValue() + "\n");
				result = result.concat(selectedUp.get(i).getAnalyzedMessage().getTime() + "\n");
			}
			
			result = result.concat("\n\n\n");
		}
		
		//String result2 = new String(result.getBytes("utf-8"), "utf-8");
		return result;
	}
//	public String change( String input, int length ) {
//		System.out.println("Width: " + getWidth() );
//		System.out.println("Length: " + length);
//		System.out.println(input);
//		String result = "";
//		Date date = new Date();
//		String dateString = format.format(date);
//		
//		int resultLocation = metrics.stringWidth("0x");
//		int msg = 0;
//		int i = 2;
//		
//		while ( i <= input.length() - 1 ) {
//			
//			if ( input.charAt(i) == '\n') {
//				result = result.concat("\n");
//				msg = 0;
//				resultLocation = 0;
//				i++;
//			}
//			
//			else if ( msg % 12 == 0 && ( msg != 0 ) ) {
//				//System.out.println(resultLocation + 9 + metrics.stringWidth(input.substring(i, i + 4 )) + "   " + length );
//				if ( resultLocation + metrics.stringWidth(" ") * 3 + metrics.stringWidth(input.substring(i, i + 2 ))  < length ) {
//					result = result.concat("   " + input.substring(i, i+ 2));
//					resultLocation = resultLocation + metrics.stringWidth(" ") * 3 + metrics.stringWidth(input.substring(i,i+2));
//				}
//				
//				else {
//					result = result.concat("\n" + input.substring(i, i+ 2));
//					resultLocation = metrics.stringWidth(input.substring(i,i+2));
//					System.out.println(resultLocation);
//				}
//				
//				msg += 2;
//				i += 2;
//			}
//			
//			else if ( msg % 2 == 0 && ( msg != 0 )) {
//				
//				if ( resultLocation + metrics.stringWidth(" ") + metrics.stringWidth(input.substring(i, i + 2 )) < length ) {
//					result = result.concat(" " + input.substring(i, i+ 2));
//					resultLocation = resultLocation + metrics.stringWidth(" ") + metrics.stringWidth(input.substring(i,i+2));
//				}
//				
//				else {
//					result = result.concat("\n" + input.substring(i, i+ 2));
//					resultLocation = metrics.stringWidth(input.substring(i, i+ 2));
//					System.out.println(resultLocation);
//				}
//				
//				msg += 2;
//				i += 2;
//			}
//			
//			else {
//				
//				if ( msg == 0 ) {
//					resultLocation = metrics.stringWidth("0x");
//					String sub = input.substring(i);
//					System.out.println(sub);
//					if ( sub.startsWith("解析后")) {
//						result = result.concat( sub.substring(0, sub.indexOf("\n") + 1 ) );
//						i = i + sub.indexOf("\n") + 1;
//						
//						sub = input.substring(i);
//						result = result.concat( sub.substring(0, sub.indexOf("\n") + 1 ) );
//						i = i + sub.indexOf("\n") + 1;
//						
//						sub = input.substring(i);
//						result = result.concat( sub.substring(0, sub.indexOf("\n") + 1 ) );
//						i = i + sub.indexOf("\n") + 1;
//						
//						sub = input.substring(i);
//						result = result.concat( sub.substring(0, sub.indexOf("\n") ) );
//						i = i + sub.indexOf("\n");
//						
//						msg = 0;
//					}
//					
//					else {
//						result = result.concat( "解析前:\n" + dateString + ": " + "0x");
//						i += 2;
//						
//						if ( resultLocation + metrics.stringWidth(input.substring(i, i + 2 )) < length ) {
//							result = result.concat(input.substring(i, i+ 2));
//							resultLocation = resultLocation + metrics.stringWidth(input.substring(i, i+ 2));
//						}
//						
//						else {
//							result = result.concat(input.substring(i, i+ 2) );
//							resultLocation = metrics.stringWidth(input.substring(i, i+ 2));
//							//System.out.println(resultLocation);
//						}
//						
//						msg += 2;
//						i += 2;
//					}
//					
//				}
//				
//			}
//			
//		}
//		
//		return result;
//	}
	
	public boolean getStates() {
		return states;
	}
	
	public static void main ( String[] args ) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, InterruptedException, SQLException, IOException {
		MsgShowUI msgTest = new MsgShowUI();
		
		for ( int i = 0; i <= 10000; i++ ) {
			UpMessage msg = new UpMessage();
			String s ="010101010101010101010101\n";
			RawMessage raws = new RawMessage(new Date().toString(), s);
			msg.setRawMessage(raws);
			AnalyzedMessage ana = new AnalyzedMessage("123" + i % 5, "02", "1600", 0f, new Date().toString());
			msg.setAnalyzedMessage(ana);
			msg.setDeviceId("123" + i % 5);
			msgTest.setUpMessage(msg);
			DownAnalyzedMessage dana = new DownAnalyzedMessage("123" + i % 5, "02", "1600", 0f, "digital");
			DownMessage dmsg = new DownMessage();
			dmsg.setRawMessage(raws);
			dmsg.setDownMessage(dana);
			dmsg.setDeviceid("123" + i % 5);
			msgTest.setDownMessage(dmsg);
			msgTest.showTexts();
			Thread.sleep(1000);
		}
		
		msgTest.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
}
