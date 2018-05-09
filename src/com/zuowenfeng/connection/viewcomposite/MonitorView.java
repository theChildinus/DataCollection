package com.zuowenfeng.connection.viewcomposite;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.border.TitledBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public class MonitorView extends JFrame {
	private Container container;
	private JPanel hostListPanel;
	DefaultMutableTreeNode root = new DefaultMutableTreeNode("连接主机IP                                          ");
	private JTree hostListTree = new JTree(root);
	private JPanel hostDetailPanel;
	private JPanel hostStatePanel;
	private JPanel ipPanel;
	private JLabel ipLabel = new JLabel("主机IP:    ");
	private JLabel ipContent = new JLabel("XXX.XXX.XXX.XXX");
	private JPanel portPanel;
	private JLabel portLabel = new JLabel("端口:    ");
	private JLabel portContent = new JLabel("1234");
	private JLabel deviceLabel = new JLabel("对应底层设备:    ");
	private JLabel deviceContent = new JLabel("XXX.XXX.XXX.XXX");
	private JPanel devicePanel;
	private JPanel statePanel;
	private JLabel stateLabel = new JLabel("状态:    ");
	private JLabel stateContent = new JLabel("已连接");
	private JPanel commandPanel;
	private JTextArea commandArea = new JTextArea();
	private JPanel configurationPanel;
	private JLabel maxRowsLabel = new JLabel("最大行数:    ");
	private JComboBox maxRowsCombo = new JComboBox(new Object[]{new Integer(100), new Integer(200), new Integer(500), new Integer(1000)});
	private JButton applyButton = new JButton("应用设置");
	private ArrayList<String> connectedIPList = new ArrayList<String> ();
	private ArrayList<String> unconnectedIPList = new ArrayList<String> ();
	private DefaultMutableTreeNode connectedIP = new DefaultMutableTreeNode("已登录IP");;
	private DefaultMutableTreeNode unconnectedIP = new DefaultMutableTreeNode("未登录IP");
	private JFrame frame;
	private String style = "";
	private int size = 0;
	private String color = "";
	private boolean bold = false;
	private boolean italic = false;
	private JCheckBox box = new JCheckBox("自动更新");
	private JButton buttonCheck = new JButton("设置");
	private JButton buttonFresh = new JButton("更新");
	private ArrayList<String> connectedPort = new ArrayList<String> ();
	private ArrayList<String> unconnectedPort = new ArrayList<String> ();
	private ArrayList<String> connectedDevice = new ArrayList<String> ();
	private ArrayList<String> unconnectedDevice = new ArrayList<String> ();
	private int time = 5;
	
	public MonitorView() throws InterruptedException {
		final Host host = new Host();
		host.update();
		container = getContentPane();
		container.setLayout(new BorderLayout());
		JMenuBar menuBar = new JMenuBar();
		frame = this;
		
		JMenu operationMenu = new JMenu("操作");
		JMenuItem saveMenu = new JMenuItem("保存");
		JMenuItem configureMenu = new JMenuItem("配置");
		JMenuItem exitMenu = new JMenuItem("退出");
		operationMenu.add(saveMenu);
		operationMenu.add(configureMenu);
		operationMenu.add(exitMenu);
		
		JMenu helpMenu = new JMenu("帮助");
		JMenuItem helpMenuItem = new JMenuItem("使用说明");
		JMenuItem versionMenuItem = new JMenuItem("版本");
		helpMenu.add(helpMenuItem);
		helpMenu.add(versionMenuItem);
		
		menuBar.add(operationMenu);
		menuBar.add(helpMenu);
		
		setJMenuBar(menuBar);
		
		hostListPanel = new JPanel();
		hostListPanel.setBorder(new TitledBorder("主机列表"));
		root.add(connectedIP);
		root.add(unconnectedIP);
	
		
		hostListTree.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
				if ( e.getClickCount() == 2 ) {
					TreePath path = hostListTree.getSelectionPath();
					TreeNode node = (TreeNode) path.getLastPathComponent();
					
					if ( node.isLeaf() && node.getParent().equals(connectedIP)) {
						ipContent.setText(node.toString());
						
						for ( int i = 0; i <= connectedIPList.size() - 1; i++ ) {
							
							if ( connectedIPList.get(i).equals(node.toString())) {
								portContent.setText(connectedPort.get(i));
								deviceContent.setText(connectedDevice.get(i));
								stateContent.setText("已连接");
								break;
							}
						}
						
					}
					
					else {
						ipContent.setText(node.toString());
						
						for ( int i = 0; i <= unconnectedIPList.size() - 1; i++ ) {
							
							if ( unconnectedIPList.get(i).equals(node.toString())) {
								portContent.setText(unconnectedPort.get(i));
								deviceContent.setText(unconnectedDevice.get(i));
								stateContent.setText("未连接");
								break;
							}
						}
						
					}
				}
			}
		
		});
		
		hostListPanel.setLayout(new BorderLayout());
		hostListPanel.add(new JScrollPane(hostListTree), BorderLayout.CENTER);
		JPanel applyPanel = new JPanel();
		applyPanel.setLayout(new FlowLayout());
		
		applyPanel.add(box);
		
		box.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				if ( box.isSelected() == true ) {
					buttonCheck.setEnabled(true);
				}
				
				else {
					buttonCheck.setEnabled(false);
				}
				
			}
			
		});
		
		buttonCheck.setEnabled(false);
		applyPanel.add(buttonCheck);
		
		buttonCheck.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				TimeChooseDialog dialog = new TimeChooseDialog(frame);
				time = dialog.getTimeChoose();
			}
			
		});
		
		applyPanel.add(buttonFresh);
		
		buttonFresh.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				host.update();
			}
			
		});
		
		hostListPanel.add(applyPanel, BorderLayout.SOUTH);
		
		container.add(hostListPanel, BorderLayout.WEST);
		hostDetailPanel = new JPanel();
		hostDetailPanel.setLayout(new BorderLayout());
		hostStatePanel = new JPanel();
		hostStatePanel.setLayout(new BoxLayout( hostStatePanel, BoxLayout.Y_AXIS));
		hostStatePanel.setBorder(new TitledBorder("主机状态"));
		ipPanel = new JPanel();
		ipPanel.setLayout(new BorderLayout());
		ipPanel.add(ipLabel, BorderLayout.WEST);
		ipPanel.add(ipContent, BorderLayout.CENTER);

		portPanel = new JPanel();
		portPanel.setLayout(new BorderLayout());
		
		portPanel.add(portLabel, BorderLayout.WEST);
		portPanel.add(portContent, BorderLayout.CENTER);
		
		devicePanel = new JPanel();
		devicePanel.setLayout(new BorderLayout());
		
		devicePanel.add(deviceLabel, BorderLayout.WEST);
		devicePanel.add(deviceContent, BorderLayout.CENTER);
		
		statePanel = new JPanel();
		statePanel.setLayout(new BorderLayout());
		
		statePanel.add(stateLabel, BorderLayout.WEST);
		statePanel.add(stateContent, BorderLayout.CENTER);
		
		commandPanel = new JPanel();
		commandPanel.setLayout(new BorderLayout());
		commandPanel.setBorder(new TitledBorder("状态信息显示"));
		
		configurationPanel = new JPanel();
		configurationPanel.setLayout(new FlowLayout());
		
		configurationPanel.add(maxRowsLabel);
		configurationPanel.add(maxRowsCombo);
		configurationPanel.add(applyButton);
		
		maxRowsCombo.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				// TODO Auto-generated method stub
				System.out.println(maxRowsCombo.getSelectedItem().toString());
				String msg = commandArea.getText();
				String[] divide = msg.split("\n");
				int size = (Integer)maxRowsCombo.getSelectedItem();
				String result = "";
				
				if ( divide.length < size ) {
					return;
				}
				
				for ( int i = divide.length - size - 1; i <= divide.length - 1; i++ ) {
					result = result.concat(divide[ i ] + "\n");
				}
				
				commandArea.setText(result);
			}
			
		});
		
		applyButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				FontOptDialog dialog = new FontOptDialog(frame, style, size, color, bold, italic);
				String styles = dialog.getFontChoose();
				int sizes = dialog.getSizeChoose();
				String colors = dialog.getColorChoose();
				boolean bolds = dialog.getBoldChoose();
				boolean italics = dialog.getItalicChoose();
				
				if ( styles.equals("")) {
					return;
				}
				
				style = styles;
				size = sizes;
				color = colors;
				bold = bolds;
				italic = italics;
				int modifyStyle = 0;
				
				if ( bold == true ) {
					modifyStyle += Font.BOLD;
				}
				
				if ( italic == true ) {
					modifyStyle += Font.ITALIC;
				}
				
				Font font = new Font( styles, modifyStyle, size );				
				font.deriveFont(modifyStyle);
				
				commandArea.setFont(font);
				
				if ( color.equals("Red")) {
					commandArea.setForeground(Color.red);
				}
				
				else if ( color.equals("Green")) {
					commandArea.setForeground(Color.GREEN);
				}
				
				else if ( color.equals("Blue")) {
					commandArea.setForeground(Color.BLUE);
				}
				
				else if ( color.equals("Black")) {
					commandArea.setForeground(Color.BLACK);
				}
			}
			
		});
		
		commandPanel.add( new JScrollPane(commandArea), BorderLayout.CENTER );
		commandPanel.add( configurationPanel, BorderLayout.SOUTH);
		
		hostStatePanel.add(Box.createVerticalStrut(10));
		hostStatePanel.add(ipPanel);
		hostStatePanel.add(Box.createVerticalStrut(10));
		hostStatePanel.add(portPanel);
		hostStatePanel.add(Box.createVerticalStrut(10));
		hostStatePanel.add(devicePanel);
		hostStatePanel.add(Box.createVerticalStrut(10));
		hostStatePanel.add(statePanel);
		hostStatePanel.add(Box.createVerticalStrut(10));
		
		hostDetailPanel.add(hostStatePanel, BorderLayout.NORTH);
		hostDetailPanel.add(commandPanel, BorderLayout.CENTER);
		container.add(hostDetailPanel, BorderLayout.CENTER);
		setSize(700, 600);
		setLocationRelativeTo(null);
		setVisible(true);

		while ( true ) {
			
			if ( hostListTree.getSelectionPath() == null || (!((TreeNode)hostListTree.getSelectionPath().getLastPathComponent()).isLeaf()) ) {
				host.update();
			}
			
			Thread.sleep(time * 1000 );
		}
		
	}
	
	public void showMsg( String msg ) {
		commandArea.append(msg);
	}
	
	public void addConnectedList( String ipList ) {
		//System.out.println(ipList);
		connectedIPList.add(ipList);
		connectedIP.add(new DefaultMutableTreeNode(ipList));
		((DefaultTreeModel)(hostListTree.getModel())).reload();
	}
	
	public void addUnConnectedList( String ipList ) {
		unconnectedIPList.add(ipList);
		unconnectedIP.add(new DefaultMutableTreeNode(ipList));
		((DefaultTreeModel)(hostListTree.getModel())).reload();
	}
	
	public void removeUnConnectedList( String ipList ) {
		
		for ( int i = 0; i <= unconnectedIPList.size() - 1; i++ ) {
			
			if ( unconnectedIPList.get(i).equals(ipList)) {
				unconnectedIPList.remove(i);
				unconnectedIP.remove(i);
				((DefaultTreeModel)(hostListTree.getModel())).reload();
				break;
			}
			
		}
		
	}
	
	public void removeConnectedList( String ipList ) {
		
		for ( int i = 0; i <= connectedIPList.size() - 1; i++ ) {
			
			if ( connectedIPList.get(i).equals(ipList)) {
				connectedIPList.remove(i);
				connectedIP.remove(i);
				((DefaultTreeModel)(hostListTree.getModel())).reload();
				break;
			}
			
		}
		
	}
	
	class Host {
		
		public void update() {
			try {
				Class.forName("com.mysql.jdbc.Driver");
				Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/deviceinfo", "root", "123456");
				Statement stmt = conn.createStatement();
				String sql = "select sipaddress, dipaddress, dport, backup from deviceinfo;";
				ResultSet rs = stmt.executeQuery(sql);
				connectedIP.removeAllChildren();
				connectedIPList.clear();
				unconnectedIP.removeAllChildren();
				unconnectedIPList.clear();
				
				while ( rs.next() ) {

					if ( rs.getString("backup") == null ) {
						String ip = "" + rs.getString("sipaddress");
						String port = "" + rs.getInt("dport");
						String device = "" + rs.getString("dipaddress");
						addConnectedList(ip);
						connectedPort.add(port);
						connectedDevice.add(device);
					}
					
					else {
						String ip = "" + rs.getString("sipaddress");
						String port = "" + rs.getInt("dport");
						String device = "" + rs.getString("dipaddress");
						addUnConnectedList(ip);
						unconnectedPort.add(port);
						unconnectedDevice.add(device);
					}
				}
				
				stmt.close();
				conn.close();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			
		}
	}
	
	public static void main ( String[] args ) throws InterruptedException {
		MonitorView view = new MonitorView();
		
		for ( int i = 0; i <= 1000; i++ ) {
			view.showMsg("" + i + "\n");
		}
		
		view.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
